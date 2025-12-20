package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.aspect.Auditable;
import com.behpardakht.oauth_server.authorization.exception.ExceptionMessage;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.role.RoleDto;
import com.behpardakht.oauth_server.authorization.model.dto.role.RoleFilterDto;
import com.behpardakht.oauth_server.authorization.model.entity.Permission;
import com.behpardakht.oauth_server.authorization.model.entity.Role;
import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import com.behpardakht.oauth_server.authorization.model.mapper.RoleMapper;
import com.behpardakht.oauth_server.authorization.repository.RoleRepository;
import com.behpardakht.oauth_server.authorization.repository.UserRoleRepository;
import com.behpardakht.oauth_server.authorization.repository.filter.RoleFilterSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.behpardakht.oauth_server.authorization.util.SecurityUtils.validateOwnership;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final PermissionService permissionService;

    private final RoleMapper roleMapper;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleFilterSpecification roleFilterSpecification;

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

    @Auditable(action = AuditAction.ROLE_UPDATED, details = "#id")
    public void update(Long id, RoleDto request) {
        Role role = findById(id);
        validateOwnership(role.getClient().getId());
        if (!role.getName().equals(request.getName())
                && existsByNameAndClientId(request.getName(), role.getClient().getId())) {
            throw new AlreadyExistException("Role", request.getName());
        }

        role.setName(request.getName());
        insert(role);
    }


    public PageableResponseDto<RoleDto> findAll(PageableRequestDto<RoleFilterDto> request) {
        Specification<Role> spec = roleFilterSpecification.toSpecification(request.getFilters());
        Page<Role> page = roleRepository.findAll(spec, request.toPageable());
        List<RoleDto> responses = roleMapper.toDtoList(page.getContent());
        return PageableResponseDto.build(responses, page);
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
        validateOwnership(role.getClient().getId());
        return roleMapper.toDto(role);
    }

    @Auditable(action = AuditAction.STATUS_CHANGED, details = "#id")
    public Boolean toggleStatus(Long id) {
        Role role = findById(id);
        validateOwnership(role.getClient().getId());
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
        validateOwnership(role.getClient().getId());
        if (userRoleRepository.existsByRoleId(role.getId())) {
            throw new CustomException(ExceptionMessage.ROLE_ASSIGNED_TO_USERS, role.getName());
        }
        roleRepository.delete(role);
    }

    @Transactional
    @Auditable(action = AuditAction.PERMISSION_ADDED_TO_ROLE, details = "#roleId + ':' + #permissionId")
    public void addPermission(Long roleId, Long permissionId) {
        Role role = findById(roleId);
        validateOwnership(role.getClient().getId());
        Permission permission = permissionService.findById(permissionId);
        if (!role.getClient().getId().equals(permission.getClient().getId())) {
            throw new CustomException(ExceptionMessage.PERMISSION_CLIENT_MISMATCH);
        }
        if (role.getPermissions().contains(permission)) {
            throw new AlreadyExistException("Permission", permissionId.toString());
        }
        role.getPermissions().add(permission);
        insert(role);
    }

    @Transactional
    @Auditable(action = AuditAction.PERMISSION_REMOVED_FROM_ROLE, details = "#roleId + ':' + #permissionId")
    public void removePermission(Long roleId, Long permissionId) {
        Role role = findById(roleId);
        validateOwnership(role.getClient().getId());
        Permission permission = permissionService.findById(permissionId);
        if (!role.getPermissions().contains(permission)) {
            throw new NotFoundException("Permission", "id", permissionId.toString());
        }
        role.getPermissions().remove(permission);
        insert(role);
    }
}