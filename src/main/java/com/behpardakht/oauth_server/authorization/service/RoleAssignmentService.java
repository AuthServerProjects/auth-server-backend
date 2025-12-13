package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.aspect.Auditable;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.oauth_server.authorization.model.dto.role.RoleAssignmentDto;
import com.behpardakht.oauth_server.authorization.model.entity.Client;
import com.behpardakht.oauth_server.authorization.model.entity.Role;
import com.behpardakht.oauth_server.authorization.model.entity.RoleAssignment;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import com.behpardakht.oauth_server.authorization.model.mapper.RoleAssignmentMapper;
import com.behpardakht.oauth_server.authorization.repository.RoleAssignmentRepository;
import com.behpardakht.oauth_server.authorization.repository.UserRepository;
import com.behpardakht.oauth_server.authorization.service.user.AdminUserService;
import com.behpardakht.oauth_server.authorization.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleAssignmentService {

    private final AdminUserService adminUserService;
    private final RoleService roleService;
    private final ClientService clientService;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final UserRepository userRepository;
    private final RoleAssignmentMapper roleAssignmentMapper;
    private final UserService userService;

    @Transactional
    @Auditable(action = AuditAction.ROLE_ASSIGNED, details = "#userId + ':' + #roleId + ':' + #clientId")
    public RoleAssignmentDto assign(Long userId, Long roleId, Long clientId) {
        validateNotAlreadyAssigned(userId, roleId, clientId);
        Users user = adminUserService.findById(userId);
        Role role = roleService.findById(roleId);
        Client client = clientService.findById(clientId);
        RoleAssignment assignment = RoleAssignment.builder().user(user).role(role).client(client).build();
        RoleAssignment insertedAssignment = roleAssignmentRepository.save(assignment);
        log.info("Role {} assigned to user {} for client {}",
                role.getName(), user.getUsername(), client.getClientId());
        return roleAssignmentMapper.toDto(insertedAssignment);
    }

    private void validateNotAlreadyAssigned(Long userId, Long roleId, Long clientId) {
        boolean exists = roleAssignmentRepository.existsByUserIdAndRoleIdAndClientId(userId, roleId, clientId);
        if (exists) {
            throw new AlreadyExistException("RoleAssignment",
                    "userId:" + userId + " roleId:" + roleId + " clientId:" + clientId);
        }
    }

    @Transactional
    @Auditable(action = AuditAction.ROLE_UNASSIGNED, details = "#userId + ':' + #roleId + ':' + #clientId")
    public void unassign(Long userId, Long roleId, Long clientId) {
        RoleAssignment assignment = roleAssignmentRepository.findByUserIdAndRoleIdAndClientId(userId, roleId, clientId)
                .orElseThrow(() -> new NotFoundException(
                        "RoleAssignment", "userId:roleId:clientId", userId + ":" + roleId + ":" + clientId));
        roleAssignmentRepository.delete(assignment);
        log.info("Role assignment removed for user {} role {} client {}", userId, roleId, clientId);
    }

    public List<RoleAssignmentDto> findByUserId(Long userId) {
        adminUserService.findById(userId);
        List<RoleAssignment> assignments = roleAssignmentRepository.findByUserId(userId);
        return roleAssignmentMapper.toDtoList(assignments);
    }

    public List<RoleAssignmentDto> findByUsername(String username) {
        adminUserService.findByUsername(username);
        List<RoleAssignment> assignments = roleAssignmentRepository.findByUserUsername(username);
        return roleAssignmentMapper.toDtoList(assignments);
    }

    public List<RoleAssignmentDto> findByRoleId(Long roleId) {
        roleService.findById(roleId);
        List<RoleAssignment> assignments = roleAssignmentRepository.findByRoleId(roleId);
        return roleAssignmentMapper.toDtoList(assignments);
    }

    public List<RoleAssignmentDto> findByClientId(Long clientId) {
        clientService.findById(clientId);
        List<RoleAssignment> assignments = roleAssignmentRepository.findByClientId(clientId);
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
}