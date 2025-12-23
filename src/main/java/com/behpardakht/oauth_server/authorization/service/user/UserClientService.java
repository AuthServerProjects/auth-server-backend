package com.behpardakht.oauth_server.authorization.service.user;

import com.behpardakht.oauth_server.authorization.aspect.Auditable;
import com.behpardakht.oauth_server.authorization.exception.ExceptionMessage;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.user.CreateUserDto;
import com.behpardakht.oauth_server.authorization.model.dto.user.UserClientDto;
import com.behpardakht.oauth_server.authorization.model.dto.user.UserClientFilterDto;
import com.behpardakht.oauth_server.authorization.model.entity.Client;
import com.behpardakht.oauth_server.authorization.model.entity.UserClient;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import com.behpardakht.oauth_server.authorization.model.mapper.UserClientMapper;
import com.behpardakht.oauth_server.authorization.model.mapper.UserMapper;
import com.behpardakht.oauth_server.authorization.repository.UserClientRepository;
import com.behpardakht.oauth_server.authorization.repository.UserRepository;
import com.behpardakht.oauth_server.authorization.repository.filter.UserClientFilterSpecification;
import com.behpardakht.oauth_server.authorization.service.ClientService;
import com.behpardakht.oauth_server.authorization.sms.ISmsService;
import com.behpardakht.oauth_server.authorization.util.GeneralUtil;
import com.behpardakht.oauth_server.authorization.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserClientService {

    private final SecurityUtils securityUtils;

    private final ClientService clientService;
    private final AdminUserService adminUserService;

    private final UserMapper userMapper;
    private final UserClientMapper userClientMapper;

    private final UserRepository userRepository;
    private final UserClientRepository userClientRepository;
    private final UserClientFilterSpecification userClientFilterSpecification;

    private final ISmsService smsService;
    private final PasswordEncoder passwordEncoder;

    public PageableResponseDto<UserClientDto> findAll(PageableRequestDto<UserClientFilterDto> request) {
        SecurityUtils.setClientContext(request, UserClientFilterDto::new);
        Specification<UserClient> spec = userClientFilterSpecification.toSpecification(request.getFilters());
        Page<UserClient> page = userClientRepository.findAll(spec, request.toPageable());
        List<UserClientDto> responses = userClientMapper.toDtoList(page.getContent());
        return PageableResponseDto.build(responses, page);
    }

    public UserClient findById(Long id) {
        return userClientRepository.findById(id)
                .orElseThrow(() -> new CustomException(ExceptionMessage.USER_CLIENT_NOT_FOUND));
    }

    public UserClientDto findDtoById(Long id) {
        UserClient userClient = findById(id);
        validateOwnership(userClient);
        return userClientMapper.toDto(userClient);
    }

    @Auditable(action = AuditAction.USER_ASSIGNED)
    public UserClientDto save(CreateUserDto request) {
        Client client = securityUtils.getCurrentClient();
        Users user;
        if (request.getUserId() != null) {
            user = adminUserService.findById(request.getUserId());
        } else if (request.getNewUser() != null) {
            user = adminUserService.save(request.getNewUser());
        } else {
            throw new CustomException(ExceptionMessage.INVALID_REQUEST);
        }
        if (userClientRepository.existsByUserAndClient(user, client)) {
            throw new CustomException(ExceptionMessage.USER_ALREADY_ASSIGNED);
        }
        UserClient userClient = create(user, client);
        return userClientMapper.toDto(userClient);
    }

    public UserClient create(Users user, Client client) {
        UserClient userClient = UserClient.builder()
                .user(user)
                .client(client)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        return userClientRepository.save(userClient);
    }

    @Auditable(action = AuditAction.USER_UPDATED, details = "#id")
    public void update(Long id, UserClientDto request) {
        UserClient userClient = findById(id);
        validateOwnership(userClient);
        if (request.getUser() != null) {
            Users user = userClient.getUser();
            userMapper.toEntity(user, request.getUser());
            userRepository.save(user);
        }
        userClientMapper.updateEntity(userClient, request);
        userClientRepository.save(userClient);
    }

    @Auditable(action = AuditAction.RESET_PASSWORD, details = "#id")
    public void resetPassword(Long id) {
        UserClient userClient = findById(id);
        validateOwnership(userClient);
        Users user = userClient.getUser();
        String newPassword = GeneralUtil.generateRandomPassword();
        smsService.send(user.getPhoneNumber(), newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Auditable(action = AuditAction.USER_BANNED, details = "#id")
    public void banUser(Long id) {
        UserClient userClient = findById(id);
        validateOwnership(userClient);
        userClient.setIsEnabled(false);
        userClient.setIsAccountNonLocked(false);
        userClientRepository.save(userClient);
    }

    @Auditable(action = AuditAction.USER_UNBANNED, details = "#id")
    public void unbanUser(Long id) {
        UserClient userClient = findById(id);
        validateOwnership(userClient);
        userClient.setIsEnabled(true);
        userClient.setIsAccountNonLocked(true);
        userClientRepository.save(userClient);
    }

    @Auditable(action = AuditAction.USER_UNASSIGNED, details = "#id")
    public void delete(Long id) {
        UserClient userClient = findById(id);
        validateOwnership(userClient);
        userClientRepository.delete(userClient);
    }

    private void validateOwnership(UserClient userClient) {
        if (SecurityUtils.isSuperAdmin()) {
            return;
        }
        Long currentClientId = SecurityUtils.getCurrentClientId();
        if (!userClient.getClient().getId().equals(currentClientId)) {
            throw new CustomException(ExceptionMessage.ACCESS_DENIED);
        }
    }

    public UserClient findOrCreateUserClient(Users user, String clientId) {
        return userClientRepository.findByUserAndClientClientId(user, clientId)
                .orElseGet(() -> {
                    Client client = clientService.findByClientId(clientId);
                    return create(user, client);
                });
    }

    public Optional<UserClient> findByUserAndClient(Users user, Client client) {
        return userClientRepository.findByUserAndClient(user, client);
    }
}