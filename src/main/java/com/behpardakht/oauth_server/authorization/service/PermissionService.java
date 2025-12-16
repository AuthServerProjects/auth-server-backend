package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.aspect.Auditable;
import com.behpardakht.oauth_server.authorization.exception.ExceptionMessage;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.oauth_server.authorization.model.dto.role.PermissionDto;
import com.behpardakht.oauth_server.authorization.model.entity.Permission;
import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import com.behpardakht.oauth_server.authorization.model.mapper.PermissionMapper;
import com.behpardakht.oauth_server.authorization.repository.PermissionRepository;
import com.behpardakht.oauth_server.authorization.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionMapper permissionMapper;
    private final PermissionRepository permissionRepository;
    private final RoleService roleService;


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

    public List<PermissionDto> findAllDto() {
        Long clientId = SecurityUtils.getCurrentClientId();
        List<Permission> permissions = permissionRepository.findAllByClientId(clientId);
        return permissionMapper.toDtoList(permissions);
    }

    public Permission findById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ExceptionWrapper.NotFoundException("Permission", "id", id.toString()));
    }

    public PermissionDto findDtoById(Long id) {
        Permission permission = findById(id);
        return permissionMapper.toDto(permission);
    }

    @Auditable(action = AuditAction.STATUS_CHANGED, details = "#id")
    public Boolean toggleStatus(Long id) {
        Permission permission = findById(id);
        permission.setIsEnabled(!Boolean.TRUE.equals(permission.getIsEnabled()));
        insert(permission);
        return permission.getIsEnabled();
    }

    public void insert(Permission permission) {
        permissionRepository.save(permission);
    }

    @Auditable(action = AuditAction.ROLE_DELETED, details = "#id")
    public void delete(Long id) {
        Permission permission = findById(id);
        if (roleService.existByPermission(id)) {
            throw new ExceptionWrapper.CustomException(ExceptionMessage.ROLE_ASSIGNED_TO_USERS, permission.getName());
        }
        permissionRepository.delete(permission);
    }

    public boolean existsByNameAndClient(String name, Long clientId) {
        return permissionRepository.existsByNameAndClientId(name, clientId);
    }
    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }
}