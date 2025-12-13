package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.model.dto.role.PermissionDto;
import com.behpardakht.oauth_server.authorization.model.dto.role.RoleDto;
import com.behpardakht.oauth_server.authorization.model.enums.AuthenticationMethodTypes;
import com.behpardakht.oauth_server.authorization.model.enums.AuthorizationGrantTypes;
import com.behpardakht.oauth_server.authorization.model.enums.PkceMethod;
import com.behpardakht.oauth_server.authorization.model.enums.ScopeTypes;
import com.behpardakht.oauth_server.authorization.model.mapper.PermissionMapper;
import com.behpardakht.oauth_server.authorization.model.mapper.RoleMapper;
import com.behpardakht.oauth_server.authorization.repository.PermissionRepository;
import com.behpardakht.oauth_server.authorization.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GeneralService {

    private final RoleMapper roleMapper;
    private final RoleRepository roleRepository;

    private final PermissionMapper permissionMapper;
    private final PermissionRepository permissionRepository;

    public List<AuthenticationMethodTypes> loadAuthenticationMethodType() {
        return List.of(AuthenticationMethodTypes.values());
    }

    public List<AuthorizationGrantTypes> loadAuthorizationGrantType() {
        return List.of(AuthorizationGrantTypes.values());
    }

    public List<ScopeTypes> loadScopeType() {
        return List.of(ScopeTypes.values());
    }

    public List<PkceMethod> loadPkceMethod() {
        return List.of(PkceMethod.values());
    }

    public List<RoleDto> loadUserRoles() {
        return roleMapper.toDtoList(roleRepository.findAll());
    }

    public List<PermissionDto> loadUserPermissions() {
        return permissionMapper.toDtoList(permissionRepository.findAll());
    }
}