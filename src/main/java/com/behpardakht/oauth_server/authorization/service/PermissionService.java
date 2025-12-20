package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.aspect.Auditable;
import com.behpardakht.oauth_server.authorization.exception.ExceptionMessage;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.role.PermissionDto;
import com.behpardakht.oauth_server.authorization.model.dto.role.PermissionFilterDto;
import com.behpardakht.oauth_server.authorization.model.entity.Permission;
import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import com.behpardakht.oauth_server.authorization.model.mapper.PermissionMapper;
import com.behpardakht.oauth_server.authorization.repository.PermissionRepository;
import com.behpardakht.oauth_server.authorization.repository.RoleRepository;
import com.behpardakht.oauth_server.authorization.repository.filter.PermissionFilterSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.behpardakht.oauth_server.authorization.util.SecurityUtils.validateOwnership;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionMapper permissionMapper;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionFilterSpecification permissionFilterSpecification;

    @Auditable(action = AuditAction.PERMISSION_CREATED, details = "#permissionDto.name")
    public void save(PermissionDto permissionDto) {
        if (existsByNameAndClientId(permissionDto.getName(), permissionDto.getClientId())) {
            throw new AlreadyExistException("Permission", permissionDto.getName());
        }
        Permission permission = permissionMapper.toEntity(permissionDto);
        insert(permission);
    }

    public boolean existsByNameAndClientId(String permissionName, Long clientId) {
        return permissionRepository.existsByNameAndClientId(permissionName, clientId);
    }

    @Auditable(action = AuditAction.PERMISSION_UPDATED, details = "#id")
    public void update(Long id, PermissionDto request) {
        Permission permission = findById(id);
        validateOwnership(permission.getClient().getId());
        if (!permission.getName().equals(request.getName())
                && existsByNameAndClientId(request.getName(), permission.getClient().getId())) {
            throw new AlreadyExistException("Permission", request.getName());
        }

        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        insert(permission);
    }


    public PageableResponseDto<PermissionDto> findAll(PageableRequestDto<PermissionFilterDto> request) {
        Specification<Permission> spec = permissionFilterSpecification.toSpecification(request.getFilters());
        Page<Permission> page = permissionRepository.findAll(spec, request.toPageable());
        List<PermissionDto> responses = permissionMapper.toDtoList(page.getContent());
        return PageableResponseDto.build(responses, page);
    }

    public Permission findById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ExceptionWrapper.NotFoundException("Permission", "id", id.toString()));
    }

    public PermissionDto findDtoById(Long id) {
        Permission permission = findById(id);
        validateOwnership(permission.getClient().getId());
        return permissionMapper.toDto(permission);
    }

    @Auditable(action = AuditAction.STATUS_CHANGED, details = "#id")
    public Boolean toggleStatus(Long id) {
        Permission permission = findById(id);
        validateOwnership(permission.getClient().getId());
        permission.setIsEnabled(!Boolean.TRUE.equals(permission.getIsEnabled()));
        insert(permission);
        return permission.getIsEnabled();
    }

    public void insert(Permission permission) {
        permissionRepository.save(permission);
    }

    @Auditable(action = AuditAction.PERMISSION_DELETED, details = "#id")
    public void delete(Long id) {
        Permission permission = findById(id);
        validateOwnership(permission.getClient().getId());
        if (roleRepository.existsByPermissions_id(id)) {
            throw new CustomException(ExceptionMessage.PERMISSION_ASSIGNED_TO_ROLE, permission.getName());
        }
        permissionRepository.delete(permission);
    }

    public boolean existsByNameAndClient(String name, Long clientId) {
        return permissionRepository.existsByNameAndClientId(name, clientId);
    }
    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    public List<Permission> findAllByClientId(Long clientId) {
        return permissionRepository.findAllByClientId(clientId);
    }
}