package com.behpardakht.oauth_server.authorization.security;

import com.behpardakht.oauth_server.authorization.config.Properties;
import com.behpardakht.oauth_server.authorization.model.entity.*;
import com.behpardakht.oauth_server.authorization.model.enums.UserPermission;
import com.behpardakht.oauth_server.authorization.model.enums.UserRole;
import com.behpardakht.oauth_server.authorization.service.ClientService;
import com.behpardakht.oauth_server.authorization.service.PermissionService;
import com.behpardakht.oauth_server.authorization.service.RoleAssignmentService;
import com.behpardakht.oauth_server.authorization.service.RoleService;
import com.behpardakht.oauth_server.authorization.service.user.AdminUserService;
import com.behpardakht.oauth_server.authorization.util.GeneralUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.SYSTEM_CLIENT_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final Properties properties;
    private final PasswordEncoder passwordEncoder;

    private final ClientService clientService;
    private final AdminUserService adminUserService;

    private final RoleService roleService;
    private final PermissionService permissionService;
    private final RoleAssignmentService roleAssignmentService;

    @Override
    @Transactional
    public void run(String... args) {
        Client systemClient = initSystemClient();
        initPermissions();
        Role superAdminRole = initSuperAdminRole();
        initAdminRole();
        initUserRole();
        Users superAdminUser = initSuperAdminUser();
        assignSuperAdminRole(systemClient, superAdminRole, superAdminUser);
    }

    private Client initSystemClient() {
        return clientService.findByClientIdOptional(SYSTEM_CLIENT_ID).orElseGet(() -> {
            Client systemClient = Client.builder()
                    .clientId(SYSTEM_CLIENT_ID)
                    .clientSecret(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .build();
            Client client = clientService.insert(systemClient);
            log.info("SYSTEM client created");
            return client;
        });
    }

    private void initPermissions() {
        for (UserPermission userPermission : UserPermission.values()) {
            if (!permissionService.existsByName(userPermission.getValue())) {
                Permission permission = Permission.builder()
                        .name(userPermission.getValue())
                        .description(userPermission.getDescription())
                        .build();
                permissionService.insert(permission);
                log.info("Created permission: {}", userPermission.getValue());
            }
        }
    }

    private Role initSuperAdminRole() {
        String roleName = UserRole.SUPER_ADMIN.getValue();
        return roleService.findByNameOptional(roleName).orElseGet(() -> {
            Set<Permission> allPermissions = new HashSet<>(permissionService.findAll());
            Role supperAdminRole = Role.builder()
                    .name(roleName)
                    .permissions(allPermissions)
                    .build();
            Role role = roleService.insert(supperAdminRole);
            log.info("Created role: {} with all permissions", roleName);
            return role;
        });
    }

    private void initAdminRole() {
        String roleName = UserRole.ADMIN.getValue();
        if (!roleService.existsByName(roleName)) {
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
            roleService.insert(role);
            log.info("Created role: {} with limited permissions", roleName);
        }
    }

    private Set<Permission> getPermissionsByNames(UserPermission... enums) {
        Set<Permission> permissions = new HashSet<>();
        for (UserPermission permEnum : enums) {
            permissionService.findByName(permEnum.getValue()).ifPresent(permissions::add);
        }
        return permissions;
    }

    private void initUserRole() {
        String roleName = UserRole.USER.getValue();
        if (!roleService.existsByName(roleName)) {
            Role role = Role.builder()
                    .name(roleName)
                    .build();
            roleService.insert(role);
            log.info("Created role: {} with no admin permissions", roleName);
        }
    }

    private Users initSuperAdminUser() {
        String phoneNumber = properties.getSuperAdmin().getPhoneNumber();
        return adminUserService.findByUsernameOptional(phoneNumber)
                .orElseGet(() -> {
                    Users superAdmin = Users.builder()
                            .username(phoneNumber)
                            .password(GeneralUtil.generateRandomPassword())
                            .phoneNumber(phoneNumber)
                            .isAccountNonExpired(true)
                            .isAccountNonLocked(true)
                            .isCredentialsNonExpired(true)
                            .build();
                    Users user = adminUserService.insert(superAdmin);
                    log.info("Super admin user created: {}", phoneNumber);
                    return user;
                });
    }

    private void assignSuperAdminRole(Client systemClient, Role superAdminRole, Users superAdmin) {
        if (!roleAssignmentService.existsByUserIdAndRoleIdAndClientId(
                superAdmin.getId(), superAdminRole.getId(), systemClient.getId())) {
            RoleAssignment assignment = RoleAssignment.builder()
                    .user(superAdmin)
                    .role(superAdminRole)
                    .client(systemClient)
                    .build();
            roleAssignmentService.insert(assignment);
            log.info("SUPER_ADMIN role assigned to {} with SYSTEM client", superAdmin.getUsername());
        }
    }
}