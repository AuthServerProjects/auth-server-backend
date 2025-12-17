package com.behpardakht.oauth_server.authorization.service.user;

import com.behpardakht.oauth_server.authorization.aspect.Auditable;
import com.behpardakht.oauth_server.authorization.exception.ExceptionMessage;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.user.CreateUserAssignmentDto;
import com.behpardakht.oauth_server.authorization.model.dto.user.UserClientAssignmentDto;
import com.behpardakht.oauth_server.authorization.model.dto.user.UserClientAssignmentFilterDto;
import com.behpardakht.oauth_server.authorization.model.entity.Client;
import com.behpardakht.oauth_server.authorization.model.entity.UserClientAssignment;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import com.behpardakht.oauth_server.authorization.model.mapper.UserClientAssignmentMapper;
import com.behpardakht.oauth_server.authorization.model.mapper.UserMapper;
import com.behpardakht.oauth_server.authorization.repository.UserClientAssignmentRepository;
import com.behpardakht.oauth_server.authorization.repository.UserRepository;
import com.behpardakht.oauth_server.authorization.repository.filter.UserClientAssignmentFilterSpecification;
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
public class UserClientAssignmentService {

    private final ClientService clientService;
    private final AdminUserService adminUserService;

    private final UserMapper userMapper;
    private final UserClientAssignmentMapper userClientAssignmentMapper;

    private final UserRepository userRepository;
    private final UserClientAssignmentRepository userClientAssignmentRepository;
    private final UserClientAssignmentFilterSpecification userClientAssignmentFilterSpecification;

    private final ISmsService smsService;
    private final PasswordEncoder passwordEncoder;

    public PageableResponseDto<UserClientAssignmentDto> findAll(PageableRequestDto<UserClientAssignmentFilterDto> request) {
        Specification<UserClientAssignment> spec = userClientAssignmentFilterSpecification.toSpecification(request.getFilters());
        Page<UserClientAssignment> page = userClientAssignmentRepository.findAll(spec, request.toPageable());
        List<UserClientAssignmentDto> responses = userClientAssignmentMapper.toDtoList(page.getContent());
        return PageableResponseDto.build(responses, page);
    }

    public UserClientAssignment findById(Long id) {
        return userClientAssignmentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ExceptionMessage.USER_ASSIGNMENT_NOT_FOUND));
    }

    public UserClientAssignmentDto findDtoById(Long id) {
        UserClientAssignment assignment = findById(id);
        validateOwnership(assignment);
        return userClientAssignmentMapper.toDto(assignment);
    }

    @Auditable(action = AuditAction.USER_ASSIGNED)
    public UserClientAssignmentDto save(CreateUserAssignmentDto request) {
        Long clientId = SecurityUtils.getCurrentClientId();
        Client client = clientService.findById(clientId);
        Users user;
        if (request.getUserId() != null) {
            user = adminUserService.findById(request.getUserId());
        } else if (request.getNewUser() != null) {
            user = adminUserService.save(request.getNewUser());
        } else {
            throw new CustomException(ExceptionMessage.INVALID_REQUEST);
        }
        if (userClientAssignmentRepository.existsByUserAndClient(user, client)) {
            throw new CustomException(ExceptionMessage.USER_ALREADY_ASSIGNED);
        }
        UserClientAssignment assignment = create(user, client);
        return userClientAssignmentMapper.toDto(assignment);
    }

    private UserClientAssignment create(Users user, Client client) {
        UserClientAssignment assignment = UserClientAssignment.builder()
                .user(user)
                .client(client)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        return userClientAssignmentRepository.save(assignment);
    }

    @Auditable(action = AuditAction.USER_UPDATED, details = "#id")
    public void update(Long id, UserClientAssignmentDto request) {
        UserClientAssignment assignment = findById(id);
        validateOwnership(assignment);
        if (request.getUser() != null) {
            Users user = assignment.getUser();
            userMapper.toEntity(user, request.getUser());
            userRepository.save(user);
        }
        userClientAssignmentMapper.updateEntity(assignment, request);
        userClientAssignmentRepository.save(assignment);
    }

    @Auditable(action = AuditAction.RESET_PASSWORD, details = "#id")
    public void resetPassword(Long id) {
        UserClientAssignment assignment = findById(id);
        validateOwnership(assignment);
        Users user = assignment.getUser();
        String newPassword = GeneralUtil.generateRandomPassword();
        smsService.send(user.getPhoneNumber(), newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Auditable(action = AuditAction.USER_BANNED, details = "#id")
    public void banUser(Long id) {
        UserClientAssignment assignment = findById(id);
        validateOwnership(assignment);
        assignment.setIsEnabled(false);
        assignment.setIsAccountNonLocked(false);
        userClientAssignmentRepository.save(assignment);
    }

    @Auditable(action = AuditAction.USER_UNBANNED, details = "#id")
    public void unbanUser(Long id) {
        UserClientAssignment assignment = findById(id);
        validateOwnership(assignment);
        assignment.setIsEnabled(true);
        assignment.setIsAccountNonLocked(true);
        userClientAssignmentRepository.save(assignment);
    }

    @Auditable(action = AuditAction.USER_UNASSIGNED, details = "#id")
    public void delete(Long id) {
        UserClientAssignment assignment = findById(id);
        validateOwnership(assignment);
        userClientAssignmentRepository.delete(assignment);
    }

    private void validateOwnership(UserClientAssignment assignment) {
        if (SecurityUtils.isSuperAdmin()) {
            return;
        }
        Long currentClientId = SecurityUtils.getCurrentClientId();
        if (!assignment.getClient().getId().equals(currentClientId)) {
            throw new CustomException(ExceptionMessage.ACCESS_DENIED);
        }
    }

    public UserClientAssignment findOrCreateAssignment(Users user, String clientId) {
        return userClientAssignmentRepository.findByUserAndClientClientId(user, clientId)
                .orElseGet(() -> {
                    Client client = clientService.findByClientId(clientId);
                    return create(user, client);
                });
    }

    public Optional<UserClientAssignment> findByUserAndClient(Users user, Client client) {
        return userClientAssignmentRepository.findByUserAndClient(user, client);
    }
}