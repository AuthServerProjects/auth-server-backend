package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.model.dto.role.PermissionDto;
import com.behpardakht.oauth_server.authorization.model.entity.Permission;
import com.behpardakht.oauth_server.authorization.model.mapper.PermissionMapper;
import com.behpardakht.oauth_server.authorization.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionMapper permissionMapper;
    private final PermissionRepository permissionRepository;

    public boolean existsByName(String name) {
        return permissionRepository.existsByName(name);
    }

    public void insert(Permission permission) {
        permissionRepository.save(permission);
    }

    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    public List<PermissionDto> findAllDto() {
        return permissionMapper.toDtoList(permissionRepository.findAll());
    }

    public Optional<Permission> findByName(String name) {
        return permissionRepository.findByName(name);
    }
}