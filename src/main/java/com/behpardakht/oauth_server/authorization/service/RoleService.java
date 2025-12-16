package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.aspect.Auditable;
import com.behpardakht.oauth_server.authorization.exception.ExceptionMessage;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.oauth_server.authorization.model.dto.role.RoleDto;
import com.behpardakht.oauth_server.authorization.model.entity.Role;
import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import com.behpardakht.oauth_server.authorization.model.mapper.RoleMapper;
import com.behpardakht.oauth_server.authorization.repository.RoleAssignmentRepository;
import com.behpardakht.oauth_server.authorization.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleMapper roleMapper;
    private final RoleRepository roleRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;

    @Auditable(action = AuditAction.ROLE_CREATED, details = "#roleDto.name")
    public void save(RoleDto roleDto) {
        if (existsByNameAndClientId(roleDto.getName(), roleDto.getClientId())) {
            throw new AlreadyExistException("Role", roleDto.getName());
        }
        Role role = roleMapper.toEntity(roleDto);
        insert(role);
    }

    public boolean existsByNameAndClientId(String roleName, Long clientId) {
        return roleRepository.existsByNameAndClientId(roleName, clientId);
    }

    public boolean existByPermission(Long permissionsId) {
        return roleRepository.existsByPermissions_id(permissionsId);
    }

    public List<RoleDto> findAll() {
        List<Role> roles = roleRepository.findAll();
        return roleMapper.toDtoList(roles);
    }

    public Optional<Role> findByNameAndClient(String name, Long clientId) {
        return roleRepository.findByNameAndClientId(name, clientId);
    }

    public Role findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role", "id", id.toString()));
    }

    public RoleDto findDtoById(Long id) {
        Role role = findById(id);
        return roleMapper.toDto(role);
    }

    @Auditable(action = AuditAction.STATUS_CHANGED, details = "#id")
    public Boolean toggleStatus(Long id) {
        Role role = findById(id);
        role.setIsEnabled(!Boolean.TRUE.equals(role.getIsEnabled()));
        insert(role);
        return role.getIsEnabled();
    }

    public Role insert(Role role) {
        return roleRepository.save(role);
    }

    @Auditable(action = AuditAction.ROLE_DELETED, details = "#id")
    public void delete(Long id) {
        Role role = findById(id);
        if (roleAssignmentRepository.existsByRoleId(role.getId())) {
            throw new ExceptionWrapper.CustomException(ExceptionMessage.ROLE_ASSIGNED_TO_USERS, role.getName());
        }
        roleRepository.delete(role);
    }
}