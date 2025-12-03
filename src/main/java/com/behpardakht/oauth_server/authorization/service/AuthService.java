package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.config.bundle.MessageResolver;
import com.behpardakht.oauth_server.authorization.exception.ExceptionMessages;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.oauth_server.authorization.model.dto.auth.AuthorizationDto;
import com.behpardakht.oauth_server.authorization.model.dto.auth.AuthorizationFilterDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.entity.Authorizations;
import com.behpardakht.oauth_server.authorization.model.mapper.AuthorizationMapper;
import com.behpardakht.oauth_server.authorization.repository.AuthorizationFilterSpecification;
import com.behpardakht.oauth_server.authorization.repository.AuthorizationRepository;
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

    public void logout(String authHeader) {
        validateAuthHeader(authHeader);
        OAuth2Authorization authorization = getAuthorization(authHeader);
        if (authorization != null) {
            removeAndBlackListToken(authorization);
        } else {
            log.warn("Logout attempted with token that doesn't exist in database: ...");
            throw new CustomException(ExceptionMessages.TOKEN_NOT_FOUND);
        }
    }

    private static void validateAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Logout attempt without valid Authorization header");
            throw new CustomException(ExceptionMessages.INVAlID_AUTH_HEADER);
        }
    }

    private OAuth2Authorization getAuthorization(String authHeader) {
        String token = authHeader.substring(7);
        OAuth2Authorization authorization = authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN);
        if (authorization == null) {
            authorization = authorizationService.findByToken(token, OAuth2TokenType.REFRESH_TOKEN);
        }
        return authorization;
    }

    private void removeAndBlackListToken(OAuth2Authorization authorization) {
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

    public void revokeSession(String authorizationId) {
        Authorizations authorization = authorizationRepository.findByAuthorizationId(authorizationId)
                .orElseThrow(() -> new NotFoundException("Session", "authorizationId", authorizationId));
        removeAndBlacklistByEntity(authorization);
    }

    private void removeAndBlacklistByEntity(Authorizations authorization) {
        if (authorization.getAccessToken() != null) {
            tokenBlacklistService.blacklistAccessToken(
                    authorization.getAccessToken(), authorization.getAccessTokenExpiresAt());
        }
        if (authorization.getRefreshToken() != null) {
            tokenBlacklistService.blacklistRefreshToken(
                    authorization.getRefreshToken(), authorization.getRefreshTokenExpiresAt());
        }
        authorizationRepository.deleteByAuthorizationId(authorization.getAuthorizationId());
    }

    public String revokeSessionsByUsername(String username) {
        List<Authorizations> userAuthorizationList = authorizationRepository.findByPrincipalName(username);
        if (userAuthorizationList.isEmpty()) {
            throw new CustomException(ExceptionMessages.NO_ACTIVE_SESSIONS_FOUND);
        }
        return removeAndBlackListToken(userAuthorizationList, maskPhoneNumber(username));
    }

    public String logoutFromAllDevices(String authHeader) {
        validateAuthHeader(authHeader);
        OAuth2Authorization authorization = getAuthorization(authHeader);
        String principalName = authorization.getPrincipalName();
        String maskedPrincipalName = maskPhoneNumber(principalName);
        List<Authorizations> userAuthorizationList = authorizationRepository.findByPrincipalName(principalName);
        if (userAuthorizationList.isEmpty()) {
            log.info("Logout-all: No active sessions found for user {}", maskedPrincipalName);
            throw new CustomException(ExceptionMessages.NO_ACTIVE_SESSIONS_FOUND);
        }
        return removeAndBlackListToken(userAuthorizationList, maskedPrincipalName);
    }

    private String removeAndBlackListToken(List<Authorizations> userAuthorizationList, String maskedPrincipalName) {
        int revokedCount = 0;
        int failedCount = 0;
        for (Authorizations authEntity : userAuthorizationList) {
            try {
                if (authEntity.getAccessToken() != null) {
                    tokenBlacklistService.blacklistAccessToken(
                            authEntity.getAccessToken(), authEntity.getAccessTokenExpiresAt());
                }
                if (authEntity.getRefreshToken() != null) {
                    tokenBlacklistService.blacklistRefreshToken(
                            authEntity.getRefreshToken(), authEntity.getRefreshTokenExpiresAt());
                }
                authorizationRepository.delete(authEntity);
                revokedCount++;
                log.debug("Revoked session: Authorization ID {}", authEntity.getAuthorizationId());
            } catch (Exception e) {
                failedCount++;
                log.error("Failed to revoke authorization {} for user {}",
                        authEntity.getAuthorizationId(), maskedPrincipalName, e);
            }
        }
        log.info("LOGOUT-ALL SUCCESS: User {} logged out from {} device(s) ({} failed)",
                maskedPrincipalName, revokedCount, failedCount);

        String message = MessageResolver.getMessage(
                ExceptionMessages.SESSIONS_REVOKED_SUCCESS.getMessage(), new Object[]{revokedCount});
        if (failedCount > 0) {
            message += " " + MessageResolver.getMessage(
                    ExceptionMessages.SESSIONS_REVOKED_FAILED.getMessage(), new Object[]{failedCount});
        }
        return message;
    }

    public PageableResponseDto<AuthorizationDto> findAllSessions(PageableRequestDto<AuthorizationFilterDto> request) {
        Specification<Authorizations> spec = authorizationFilterSpecification.toSpecification(request.getFilters());
        Page<Authorizations> page = authorizationRepository.findAll(spec, request.toPageable());
        List<AuthorizationDto> responses = authorizationMapper.toDtoList(page.getContent());
        return PageableResponseDto.build(responses, page);
    }

    public List<AuthorizationDto> findSessionsByUsername(String username) {
        List<Authorizations> sessions = authorizationRepository.findByPrincipalName(username);
        return authorizationMapper.toDtoList(sessions);
    }
}