package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.aspect.Auditable;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.oauth_server.authorization.model.dto.role.RoleDto;
import com.behpardakht.oauth_server.authorization.model.entity.Role;
import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import com.behpardakht.oauth_server.authorization.model.mapper.RoleMapper;
import com.behpardakht.oauth_server.authorization.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleAssignmentService roleAssignmentService;
    private final RoleMapper roleMapper;
    private final RoleRepository roleRepository;

    @Auditable(action = AuditAction.ROLE_CREATED, details = "#roleDto.name")
    public void save(RoleDto roleDto) {
        if (existsByName(roleDto.getName())) {
            throw new AlreadyExistException("Role", roleDto.getName());
        }
        Role role = roleMapper.toEntity(roleDto);
        insert(role);
    }

    public boolean existsByName(String roleName) {
        return roleRepository.existsByName(roleName);
    }

    public List<RoleDto> findAll() {
        List<Role> roles = roleRepository.findAll();
        return roleMapper.toDtoList(roles);
    }

    public Role findByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Role", "name", name));
    }

    public Optional<Role> findByNameOptional(String name) {
        return roleRepository.findByName(name);
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
        roleAssignmentService.checkIsExistsByRole(role);
        roleRepository.delete(role);
    }
}