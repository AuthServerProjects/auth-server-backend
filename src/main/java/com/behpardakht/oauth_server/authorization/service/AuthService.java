package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.aspect.Auditable;
import com.behpardakht.oauth_server.authorization.config.bundle.MessageResolver;
import com.behpardakht.oauth_server.authorization.exception.ExceptionMessage;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.oauth_server.authorization.model.dto.auth.AuthorizationDto;
import com.behpardakht.oauth_server.authorization.model.dto.auth.AuthorizationFilterDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.entity.Authorizations;
import com.behpardakht.oauth_server.authorization.model.entity.Client;
import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import com.behpardakht.oauth_server.authorization.model.mapper.AuthorizationMapper;
import com.behpardakht.oauth_server.authorization.repository.AuthorizationRepository;
import com.behpardakht.oauth_server.authorization.repository.UserClientAssignmentRepository;
import com.behpardakht.oauth_server.authorization.repository.filter.AuthorizationFilterSpecification;
import com.behpardakht.oauth_server.authorization.util.Messages;
import com.behpardakht.oauth_server.authorization.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.maskPhoneNumber;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthorizationFilterSpecification authorizationFilterSpecification;
    private final AuthorizationMapper authorizationMapper;

    private final OAuth2AuthorizationService authorizationService;
    private final AuthorizationRepository authorizationRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final ClientService clientService;
    private final UserClientAssignmentRepository userClientAssignmentRepository;

    @Auditable(action = AuditAction.LOGOUT)
    public void logout(String authHeader) {
        validateAuthHeader(authHeader);
        OAuth2Authorization authorization = getAuthorization(authHeader);
        removeAndBlacklistToken(authorization);
    }

    private static void validateAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Logout attempt without valid Authorization header");
            throw new CustomException(ExceptionMessage.INVALID_AUTH_HEADER);
        }
    }

    private OAuth2Authorization getAuthorization(String authHeader) {
        String token = authHeader.substring(7);
        OAuth2Authorization authorization = authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN);
        if (authorization == null) {
            authorization = authorizationService.findByToken(token, OAuth2TokenType.REFRESH_TOKEN);
        }
        if (authorization == null) {
            log.warn("Logout attempted with token that doesn't exist in database: ...");
            throw new CustomException(ExceptionMessage.TOKEN_NOT_FOUND);
        }
        return authorization;
    }

    private void removeAndBlacklistToken(OAuth2Authorization authorization) {
        OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();
        if (accessToken != null && accessToken.getToken() != null) {
            tokenBlacklistService.blacklistAccessToken(
                    accessToken.getToken().getTokenValue(), accessToken.getToken().getExpiresAt());
        }
        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization.getRefreshToken();
        if (refreshToken != null && refreshToken.getToken() != null) {
            tokenBlacklistService.blacklistRefreshToken(
                    refreshToken.getToken().getTokenValue(), refreshToken.getToken().getExpiresAt());
        }
        authorizationService.remove(authorization);
        log.info("LOGOUT SUCCESS: User {} logged out. Authorization ID: {}",
                maskPhoneNumber(authorization.getPrincipalName()), authorization.getId());
    }

    @Auditable(action = AuditAction.SESSION_REVOKED, details = "#authorizationId")
    public void revokeSession(String authorizationId) {
        Authorizations authorization = authorizationRepository.findByAuthorizationId(authorizationId)
                .orElseThrow(() -> new NotFoundException("Session", "authorizationId", authorizationId));
        validateSessionAccess(authorization);
        removeAndBlacklistToken(authorization);
    }

    private void removeAndBlacklistToken(Authorizations authorization) {
        if (authorization.getAccessToken() != null) {
            tokenBlacklistService.blacklistAccessToken(
                    authorization.getAccessToken(), authorization.getAccessTokenExpiresAt());
        }
        if (authorization.getRefreshToken() != null) {
            tokenBlacklistService.blacklistRefreshToken(
                    authorization.getRefreshToken(), authorization.getRefreshTokenExpiresAt());
        }
        authorizationRepository.delete(authorization);
        log.debug("Revoked session: Authorization ID {}", authorization.getAuthorizationId());
    }

    @Auditable(action = AuditAction.ALL_SESSION_REVOKED, username = "#username")
    public String revokeSessionsByUsername(String username) {
        validateUserAccess(username);
        List<Authorizations> userAuthorizationList = findSessionsByCurrentClient(username);
        if (userAuthorizationList.isEmpty()) {
            throw new CustomException(ExceptionMessage.NO_ACTIVE_SESSIONS_FOUND);
        }
        return removeAndBlacklistToken(userAuthorizationList, maskPhoneNumber(username));
    }

    @Auditable(action = AuditAction.LOGOUT_ALL)
    public String logoutFromAllDevices(String authHeader) {
        validateAuthHeader(authHeader);
        OAuth2Authorization authorization = getAuthorization(authHeader);
        String principalName = authorization.getPrincipalName();
        String maskedPrincipalName = maskPhoneNumber(principalName);
        List<Authorizations> userAuthorizationList = authorizationRepository.findByPrincipalName(principalName);
        if (userAuthorizationList.isEmpty()) {
            log.info("Logout-all: No active sessions found for user {}", maskedPrincipalName);
            throw new CustomException(ExceptionMessage.NO_ACTIVE_SESSIONS_FOUND);
        }
        return removeAndBlacklistToken(userAuthorizationList, maskedPrincipalName);
    }

    private String removeAndBlacklistToken(List<Authorizations> userAuthorizationList, String maskedPrincipalName) {
        int revokedCount = 0;
        int failedCount = 0;
        for (Authorizations authorization : userAuthorizationList) {
            try {
                removeAndBlacklistToken(authorization);
                revokedCount++;
            } catch (Exception e) {
                failedCount++;
                log.error("Failed to revoke authorization {} for user {}",
                        authorization.getAuthorizationId(), maskedPrincipalName, e);
            }
        }
        log.info("LOGOUT-ALL SUCCESS: User {} logged out from {} device(s) ({} failed)",
                maskedPrincipalName, revokedCount, failedCount);

        String message = MessageResolver.getMessage(
                Messages.SESSIONS_REVOKED_SUCCESS.getMessage(), new Object[]{revokedCount});
        if (failedCount > 0) {
            message += " " + MessageResolver.getMessage(
                    Messages.SESSIONS_REVOKED_FAILED.getMessage(), new Object[]{failedCount});
        }
        return message;
    }

    public PageableResponseDto<AuthorizationDto> findAllSessions(PageableRequestDto<AuthorizationFilterDto> request) {
        setClientFilter(request);
        Specification<Authorizations> spec = authorizationFilterSpecification.toSpecification(request.getFilters());
        Page<Authorizations> page = authorizationRepository.findAll(spec, request.toPageable());
        List<AuthorizationDto> responses = authorizationMapper.toDtoList(page.getContent());
        return PageableResponseDto.build(responses, page);
    }

    public List<AuthorizationDto> findSessionsByUsername(String username) {
        validateUserAccess(username);
        List<Authorizations> sessions = findSessionsByCurrentClient(username);
        return authorizationMapper.toDtoList(sessions);
    }

    // ============ CLIENT FILTER METHODS ============

    private void setClientFilter(PageableRequestDto<AuthorizationFilterDto> request) {
        if (SecurityUtils.isSuperAdmin()) {
            return;
        }
        if (request.getFilters() == null) {
            request.setFilters(new AuthorizationFilterDto());
        }
        Client client = clientService.findById(SecurityUtils.getCurrentClientId());
        request.getFilters().setRegisteredClientId(client.getRegisteredClientId());
    }

    private List<Authorizations> findSessionsByCurrentClient(String username) {
        if (SecurityUtils.isSuperAdmin()) {
            return authorizationRepository.findByPrincipalName(username);
        }
        Client client = clientService.findById(SecurityUtils.getCurrentClientId());
        return authorizationRepository.findByPrincipalNameAndRegisteredClientId(username, client.getRegisteredClientId());
    }

    private void validateSessionAccess(Authorizations authorization) {
        if (SecurityUtils.isSuperAdmin()) {
            return;
        }
        Client client = clientService.findById(SecurityUtils.getCurrentClientId());
        if (!authorization.getRegisteredClientId().equals(client.getRegisteredClientId())) {
            throw new CustomException(ExceptionMessage.ACCESS_DENIED);
        }
    }

    private void validateUserAccess(String username) {
        if (SecurityUtils.isSuperAdmin()) {
            return;
        }
        Long clientId = SecurityUtils.getCurrentClientId();
        boolean hasAccess = userClientAssignmentRepository.existsByUserUsernameAndClientId(username, clientId);
        if (!hasAccess) {
            throw new CustomException(ExceptionMessage.ACCESS_DENIED);
        }
    }
}