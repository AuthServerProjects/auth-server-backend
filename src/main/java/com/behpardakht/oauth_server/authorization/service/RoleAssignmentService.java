package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.aspect.Auditable;
import com.behpardakht.oauth_server.authorization.exception.ExceptionMessage;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.oauth_server.authorization.model.dto.role.RoleAssignmentDto;
import com.behpardakht.oauth_server.authorization.model.entity.Role;
import com.behpardakht.oauth_server.authorization.model.entity.RoleAssignment;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import com.behpardakht.oauth_server.authorization.model.mapper.RoleAssignmentMapper;
import com.behpardakht.oauth_server.authorization.repository.RoleAssignmentRepository;
import com.behpardakht.oauth_server.authorization.service.user.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleAssignmentService {

    private final RoleAssignmentMapper roleAssignmentMapper;
    private final RoleService roleService;
    private final AdminUserService adminUserService;
    private final RoleAssignmentRepository roleAssignmentRepository;

    @Transactional
    @Auditable(action = AuditAction.ROLE_ASSIGNED, details = "#userId + ':' + #roleId")
    public RoleAssignmentDto assign(Long userId, Long roleId) {
        validateNotAlreadyAssigned(userId, roleId);
        Users user = adminUserService.findById(userId);
        Role role = roleService.findById(roleId);
        RoleAssignment insertedAssignment = insert(RoleAssignment.builder().user(user).role(role).build());
        log.info("Role {} assigned to user {}", role.getName(), user.getUsername());
        return roleAssignmentMapper.toDto(insertedAssignment);
    }

    public RoleAssignment insert(RoleAssignment assignment) {
        return roleAssignmentRepository.save(assignment);
    }

    private void validateNotAlreadyAssigned(Long userId, Long roleId) {
        boolean exists = existsByUserIdAndRoleId(userId, roleId);
        if (exists) {
            throw new AlreadyExistException("RoleAssignment", "userId:" + userId + " roleId:" + roleId);
        }
    }

    public boolean existsByUserIdAndRoleId(Long userId, Long roleId) {
        return roleAssignmentRepository.existsByUserIdAndRoleId(userId, roleId);
    }

    @Transactional
    @Auditable(action = AuditAction.ROLE_UNASSIGNED, details = "#userId + ':' + #roleId")
    public void unassign(Long userId, Long roleId) {
        RoleAssignment assignment = roleAssignmentRepository.findByUserIdAndRoleId(userId, roleId)
                .orElseThrow(() -> new NotFoundException(
                        "RoleAssignment", "userId:roleId:", userId + ":" + roleId));
        roleAssignmentRepository.delete(assignment);
        log.info("Role assignment removed for user {} role {}", userId, roleId);
    }

    public List<RoleAssignmentDto> findByUserId(Long userId) {
        adminUserService.findById(userId);
        List<RoleAssignment> assignments = roleAssignmentRepository.findByUserId(userId);
        return roleAssignmentMapper.toDtoList(assignments);
    }

    public List<RoleAssignment> findByUsername(String username) {
        adminUserService.findByUsername(username);
        return roleAssignmentRepository.findByUserUsername(username);
    }

    public List<RoleAssignmentDto> findDtoByUsername(String username) {
        List<RoleAssignment> assignmentList = findByUsername(username);
        return roleAssignmentMapper.toDtoList(assignmentList);
    }

    public List<RoleAssignmentDto> findByRoleId(Long roleId) {
        roleService.findById(roleId);
        List<RoleAssignment> assignments = roleAssignmentRepository.findByRoleId(roleId);
        return roleAssignmentMapper.toDtoList(assignments);
    }

    public List<RoleAssignmentDto> findAll() {
        List<RoleAssignment> assignments = roleAssignmentRepository.findAll();
        return roleAssignmentMapper.toDtoList(assignments);
    }

    public RoleAssignmentDto findDtoById(Long id) {
        RoleAssignment assignment = findById(id);
        return roleAssignmentMapper.toDto(assignment);
    }

    private RoleAssignment findById(Long id) {
        return roleAssignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RoleAssignment", "id", id.toString()));
    }

    @Transactional
    @Auditable(action = AuditAction.ROLE_ASSIGNMENT_DELETED, details = "#id")
    public void delete(Long id) {
        RoleAssignment assignment = findById(id);
        roleAssignmentRepository.delete(assignment);
        log.info("Role assignment {} removed", id);
    }

    public void checkIsExistsByRole(Role role) {
        if (roleAssignmentRepository.existsByRoleId(role.getId())) {
            throw new CustomException(ExceptionMessage.ROLE_ASSIGNED_TO_USERS, role.getName());
        }
    }
}