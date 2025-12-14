package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.aspect.Auditable;
import com.behpardakht.oauth_server.authorization.exception.ExceptionMessage;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
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

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleMapper roleMapper;
    private final RoleRepository roleRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;

    @Auditable(action = AuditAction.ROLE_CREATED, details = "#roleDto.name")
    public void save(RoleDto roleDto) {
        if (roleRepository.existsByName(roleDto.getName())) {
            throw new AlreadyExistException("Role", roleDto.getName());
        }
        Role role = roleMapper.toEntity(roleDto);
        roleRepository.save(role);
    }

    public List<RoleDto> findAll() {
        List<Role> roles = roleRepository.findAll();
        return roleMapper.toDtoList(roles);
    }

    public Role findByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Role", "name", name));
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
        roleRepository.save(role);
        return role.getIsEnabled();
    }

    @Auditable(action = AuditAction.ROLE_DELETED, details = "#id")
    public void delete(Long id) {
        Role role = findById(id);
        if (roleAssignmentRepository.existsByRoleId(role.getId())) {
            throw new CustomException(ExceptionMessage.ROLE_ASSIGNED_TO_USERS, role.getName());
        }
        roleRepository.delete(role);
    }
}