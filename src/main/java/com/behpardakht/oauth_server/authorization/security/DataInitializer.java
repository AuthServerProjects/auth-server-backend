package com.behpardakht.oauth_server.authorization.security;

import com.behpardakht.oauth_server.authorization.config.Properties;
import com.behpardakht.oauth_server.authorization.model.entity.*;
import com.behpardakht.oauth_server.authorization.model.enums.*;
import com.behpardakht.oauth_server.authorization.service.ClientService;
import com.behpardakht.oauth_server.authorization.service.PermissionService;
import com.behpardakht.oauth_server.authorization.service.RoleService;
import com.behpardakht.oauth_server.authorization.service.user.AdminUserService;
import com.behpardakht.oauth_server.authorization.service.user.UserClientAssignmentService;
import com.behpardakht.oauth_server.authorization.service.user.UserRoleAssignmentService;
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
    private final UserRoleAssignmentService userRoleAssignmentService;
    private final UserClientAssignmentService userClientAssignmentService;

    @Override
    @Transactional
    public void run(String... args) {
        Client defaultClient = initDefaultClient();
        initPermissions(defaultClient);
        Role superAdminRole = initSuperAdminRole(defaultClient);
        Users superAdminUser = initSuperAdminUser();
        UserClientAssignment userClientAssignment = assignUserToClient(superAdminUser, defaultClient);
        assignRoleToUser(superAdminRole, userClientAssignment);
    }

    private Client initDefaultClient() {
        return clientService.findByClientIdOptional(DEFAULT_CLIENT_ID).orElseGet(() -> {

            Client systemClient = Client.builder()
                    .clientId(DEFAULT_CLIENT_ID)
                    .clientSecret(passwordEncoder.encode(GeneralUtil.generateRandomPassword()))
                    .registeredClientId(UUID.randomUUID().toString())
                    .clientAuthenticationMethods(Set.of(
                            AuthenticationMethodTypes.NONE,
                            AuthenticationMethodTypes.CLIENT_SECRET_JWT,
                            AuthenticationMethodTypes.CLIENT_SECRET_POST,
                            AuthenticationMethodTypes.CLIENT_SECRET_BASIC))
                    .authorizationGrantTypes(Set.of(
                            AuthorizationGrantTypes.AUTHORIZATION_CODE,
                            AuthorizationGrantTypes.REFRESH_TOKEN))
                    .scopes(Set.of(
                            ScopeTypes.OPENID))
                    .redirectUris(Set.of("http://localhost:9090/callback"))
                    .setting(TokenAndClientSetting.builder()
                            .requireProofKey(true)
                            .authorizationCodeTimeToLive(5L)
                            .accessTokenTimeToLive(30L)
                            .refreshTokenTimeToLive(1000L)
                            .reuseRefreshTokens(false)
                            .build())
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

            Set<Permission> allPermissions = new HashSet<>(permissionService.findAllByClientId(defaultClient.getId()));

            Role superAdminRole = Role.builder()
                    .name(roleName)
                    .permissions(allPermissions)
                    .client(defaultClient)
                    .build();
            Role role = roleService.insert(superAdminRole);
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
                            .password(passwordEncoder.encode(GeneralUtil.generateRandomPassword()))
                            .phoneNumber(phoneNumber)
                            .build();
                    Users user = adminUserService.insert(superAdmin);
                    log.info("Super admin user created: {}", phoneNumber);
                    return user;
                });
    }

    private UserClientAssignment assignUserToClient(Users user, Client client) {
        return userClientAssignmentService.findByUserAndClient(user, client)
                .orElseGet(() -> {
                    UserClientAssignment userClientAssignment = userClientAssignmentService.create(user, client);
                    log.info("User {} assigned to client {}", user.getUsername(), client.getClientId());
                    return userClientAssignment;
                });
    }

    private void assignRoleToUser(Role superAdminRole, UserClientAssignment userClientAssignment) {
        if (!userRoleAssignmentService.existsByUserClientAssignmentIdAndRoleId(
                userClientAssignment.getId(), superAdminRole.getId())) {

            UserRoleAssignment userRoleAssignment = UserRoleAssignment.builder()
                    .userClientAssignment(userClientAssignment)
                    .role(superAdminRole)
                    .build();
            userRoleAssignmentService.insert(userRoleAssignment);
            log.info("{} role assigned to {}", superAdminRole.getName(), userClientAssignment.getUser().getUsername());
        }
    }
}