package com.behpardakht.oauth_server.authorization.service.user;

import com.behpardakht.oauth_server.authorization.aspect.Auditable;
import com.behpardakht.oauth_server.authorization.exception.ExceptionMessage;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.oauth_server.authorization.model.dto.role.UserRoleAssignmentDto;
import com.behpardakht.oauth_server.authorization.model.entity.Role;
import com.behpardakht.oauth_server.authorization.model.entity.UserClientAssignment;
import com.behpardakht.oauth_server.authorization.model.entity.UserRoleAssignment;
import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import com.behpardakht.oauth_server.authorization.model.mapper.UserRoleAssignmentMapper;
import com.behpardakht.oauth_server.authorization.repository.UserClientAssignmentRepository;
import com.behpardakht.oauth_server.authorization.repository.UserRoleAssignmentRepository;
import com.behpardakht.oauth_server.authorization.service.RoleService;
import com.behpardakht.oauth_server.authorization.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleAssignmentService {
    private final UserRoleAssignmentMapper userRoleAssignmentMapper;
    private final RoleService roleService;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final UserClientAssignmentRepository userClientAssignmentRepository;

    @Transactional
    @Auditable(action = AuditAction.ROLE_ASSIGNED, details = "#userClientAssignmentId + ':' + #roleId")
    public UserRoleAssignmentDto assign(Long userClientAssignmentId, Long roleId) {
        validateNotAlreadyAssigned(userClientAssignmentId, roleId);

        UserClientAssignment userClientAssignment = userClientAssignmentRepository.findById(userClientAssignmentId)
                .orElseThrow(() -> new NotFoundException("UserClientAssignment", "id", userClientAssignmentId.toString()));
        Role role = roleService.findById(roleId);
        validateSameClient(userClientAssignment, role);
        UserRoleAssignment assignment = UserRoleAssignment.builder()
                .userClientAssignment(userClientAssignment)
                .role(role)
                .build();

        UserRoleAssignment inserted = insert(assignment);
        log.info("Role {} assigned to user {} for client {}",
                role.getName(),
                userClientAssignment.getUser().getUsername(),
                userClientAssignment.getClient().getClientId());

        return userRoleAssignmentMapper.toDto(inserted);
    }

    public UserRoleAssignment insert(UserRoleAssignment assignment) {
        return userRoleAssignmentRepository.save(assignment);
    }

    private void validateNotAlreadyAssigned(Long userClientAssignmentId, Long roleId) {
        if (userRoleAssignmentRepository.existsByUserClientAssignmentIdAndRoleId(userClientAssignmentId, roleId)) {
            throw new AlreadyExistException("UserRoleAssignment",
                    "userClientAssignmentId:" + userClientAssignmentId + " roleId:" + roleId);
        }
    }

    private void validateSameClient(UserClientAssignment userClientAssignment, Role role) {
        if (!userClientAssignment.getClient().getId().equals(role.getClient().getId())) {
            throw new CustomException(ExceptionMessage.ROLE_CLIENT_MISMATCH);
        }
    }

    @Transactional
    @Auditable(action = AuditAction.ROLE_UNASSIGNED, details = "#userClientAssignmentId + ':' + #roleId")
    public void unassign(Long userClientAssignmentId, Long roleId) {
        UserRoleAssignment assignment = userRoleAssignmentRepository
                .findByUserClientAssignmentIdAndRoleId(userClientAssignmentId, roleId)
                .orElseThrow(() -> new NotFoundException(
                        "UserRoleAssignment", "userClientAssignmentId:roleId", userClientAssignmentId + ":" + roleId));

        userRoleAssignmentRepository.delete(assignment);
        log.info("Role assignment removed for userClientAssignment {} role {}", userClientAssignmentId, roleId);
    }

    public List<UserRoleAssignmentDto> findByUserClientAssignmentId(Long userClientAssignmentId) {
        List<UserRoleAssignment> assignments = userRoleAssignmentRepository
                .findByUserClientAssignmentId(userClientAssignmentId);
        return userRoleAssignmentMapper.toDtoList(assignments);
    }

    public List<UserRoleAssignment> findByUsernameAndClientId(String username, Long clientId) {
        return userRoleAssignmentRepository.findByUsernameAndClientId(username, clientId);
    }

    public List<UserRoleAssignmentDto> findByRoleId(Long roleId) {
        roleService.findById(roleId);
        List<UserRoleAssignment> assignments = userRoleAssignmentRepository.findByRoleId(roleId);
        return userRoleAssignmentMapper.toDtoList(assignments);
    }

    public List<UserRoleAssignmentDto> findAll() {
        Long clientId = SecurityUtils.getCurrentClientId();
        List<UserRoleAssignment> assignments = userRoleAssignmentRepository.findByClientId(clientId);
        return userRoleAssignmentMapper.toDtoList(assignments);
    }

    public UserRoleAssignmentDto findDtoById(Long id) {
        UserRoleAssignment assignment = findById(id);
        return userRoleAssignmentMapper.toDto(assignment);
    }

    private UserRoleAssignment findById(Long id) {
        return userRoleAssignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("UserRoleAssignment", "id", id.toString()));
    }

    @Transactional
    @Auditable(action = AuditAction.ROLE_ASSIGNMENT_DELETED, details = "#id")
    public void delete(Long id) {
        UserRoleAssignment assignment = findById(id);
        userRoleAssignmentRepository.delete(assignment);
        log.info("Role assignment {} removed", id);
    }

    public boolean existsByUserClientAssignmentIdAndRoleId(Long userClientAssignmentId, Long roleId) {
        return userRoleAssignmentRepository
                .existsByUserClientAssignmentIdAndRoleId(userClientAssignmentId, roleId);
    }
}