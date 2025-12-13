package com.behpardakht.oauth_server.authorization.security;

import com.behpardakht.oauth_server.authorization.model.entity.Permission;
import com.behpardakht.oauth_server.authorization.model.entity.Role;
import com.behpardakht.oauth_server.authorization.model.enums.UserPermission;
import com.behpardakht.oauth_server.authorization.model.enums.UserRole;
import com.behpardakht.oauth_server.authorization.repository.PermissionRepository;
import com.behpardakht.oauth_server.authorization.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        initPermissions();
        initRoles();
    }

    private void initPermissions() {
        for (UserPermission userPermission : UserPermission.values()) {
            if (!permissionRepository.existsByName(userPermission.getValue())) {
                Permission permission = Permission.builder()
                        .name(userPermission.getValue())
                        .description(userPermission.getDescription())
                        .build();
                permissionRepository.save(permission);
                log.info("Created permission: {}", userPermission.getValue());
            }
        }
    }

    private void initRoles() {
        initSuperAdmin();
        initAdmin();
        initUser();
    }

    private void initSuperAdmin() {
        String roleName = UserRole.SUPER_ADMIN.getValue();
        if (!roleRepository.existsByName(roleName)) {
            Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());
            Role role = Role.builder()
                    .name(roleName)
                    .permissions(allPermissions)
                    .build();
            roleRepository.save(role);
            log.info("Created role: {} with all permissions", roleName);
        }
    }

    private void initAdmin() {
        String roleName = UserRole.ADMIN.getValue();
        if (!roleRepository.existsByName(roleName)) {
            Set<Permission> permissions = getPermissionsByNames(
                    UserPermission.DASHBOARD_VIEW,
                    UserPermission.USER_READ,
                    UserPermission.USER_CREATE,
                    UserPermission.USER_UPDATE,
                    UserPermission.CLIENT_READ,
                    UserPermission.SESSION_READ,
                    UserPermission.AUDIT_READ
            );
            Role role = Role.builder()
                    .name(roleName)
                    .permissions(permissions)
                    .build();
            roleRepository.save(role);
            log.info("Created role: {} with limited permissions", roleName);
        }
    }

    private Set<Permission> getPermissionsByNames(UserPermission... enums) {
        Set<Permission> permissions = new HashSet<>();
        for (UserPermission permEnum : enums) {
            permissionRepository.findByName(permEnum.getValue()).ifPresent(permissions::add);
        }
        return permissions;
    }

    private void initUser() {
        String roleName = UserRole.USER.getValue();
        if (!roleRepository.existsByName(roleName)) {
            Role role = Role.builder()
                    .name(roleName)
                    .build();
            roleRepository.save(role);
            log.info("Created role: {} with no admin permissions", roleName);
        }
    }
}