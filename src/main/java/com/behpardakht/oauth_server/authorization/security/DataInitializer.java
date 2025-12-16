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

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.DEFAULT_CLIENT_ID;

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
        Client defaultClient = initDefaultClient();
        initPermissions(defaultClient);
        Role superAdminRole = initSuperAdminRole(defaultClient);
        Users superAdminUser = initSuperAdminUser();
        assignSuperAdminRole(superAdminRole, superAdminUser);
    }

    private Client initDefaultClient() {
        return clientService.findByClientIdOptional(DEFAULT_CLIENT_ID).orElseGet(() -> {
            Client systemClient = Client.builder()
                    .clientId(DEFAULT_CLIENT_ID)
                    .clientSecret(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .build();
            Client client = clientService.insert(systemClient);
            log.info("SYSTEM client created");
            return client;
        });
    }

    private void initPermissions(Client defaultClient) {
        for (UserPermission userPermission : UserPermission.values()) {
            if (!permissionService.existsByNameAndClient(userPermission.getValue(), defaultClient.getId())) {
                Permission permission = Permission.builder()
                        .name(userPermission.getValue())
                        .description(userPermission.getDescription())
                        .client(defaultClient)
                        .build();
                permissionService.insert(permission);
                log.info("Created permission: {}", userPermission.getValue());
            }
        }
    }

    private Role initSuperAdminRole(Client defaultClient) {
        String roleName = UserRole.SUPER_ADMIN.getValue();
        return roleService.findByNameAndClient(roleName, defaultClient.getId()).orElseGet(() -> {
            Set<Permission> allPermissions = new HashSet<>(permissionService.findAll());
            Role supperAdminRole = Role.builder()
                    .name(roleName)
                    .permissions(allPermissions)
                    .client(defaultClient)
                    .build();
            Role role = roleService.insert(supperAdminRole);
            log.info("Created role: {} with all permissions", roleName);
            return role;
        });
    }

    private Users initSuperAdminUser() {
        String phoneNumber = properties.getSuperAdmin().getPhoneNumber();
        return adminUserService.findByPhoneNumberOptional(phoneNumber)
                .orElseGet(() -> {
                    Users superAdmin = Users.builder()
                            .username(properties.getSuperAdmin().getUsername())
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

    private void assignSuperAdminRole(Role superAdminRole, Users superAdminUser) {
        if (!roleAssignmentService.existsByUserIdAndRoleId(superAdminUser.getId(), superAdminRole.getId())) {
            RoleAssignment assignment = RoleAssignment.builder()
                    .user(superAdminUser)
                    .role(superAdminRole)
                    .build();
            roleAssignmentService.insert(assignment);
            log.info("{} role assigned to {}", superAdminRole.getName(), superAdminUser.getUsername());
        }
    }
}