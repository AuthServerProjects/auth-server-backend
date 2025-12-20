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

import java.util.Set;
import java.util.UUID;

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
        Client adminPanelClient = initAdminPanelClient();
        initPermissions(adminPanelClient);
        Role superAdminRole = initSuperAdminRole(adminPanelClient);
        Users superAdminUser = initSuperAdminUser();
        UserClientAssignment userClientAssignment = assignUserToClient(superAdminUser, adminPanelClient);
        assignRoleToUser(superAdminRole, userClientAssignment);
    }

    private Client initAdminPanelClient() {
        String clientId = properties.getAdminPanel().getClientId();
        return clientService.findByClientIdOptional(clientId).orElseGet(() -> {

            Client adminPanelClient = Client.builder()
                    .clientId(clientId)
                    .clientSecret(passwordEncoder.encode(properties.getAdminPanel().getClientSecret()))
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
                    .redirectUris(Set.of(properties.getAdminPanel().getRedirectUri()))
                    .setting(TokenAndClientSetting.builder()
                            .requireProofKey(true)
                            .authorizationCodeTimeToLive(5L)
                            .accessTokenTimeToLive(30L)
                            .refreshTokenTimeToLive(1000L)
                            .reuseRefreshTokens(false)
                            .build())
                    .build();
            Client client = clientService.insert(adminPanelClient);
            log.info("SYSTEM client created");
            return client;
        });
    }

    private void initPermissions(Client adminPanelClient) {
        for (UserPermission userPermission : UserPermission.values()) {
            if (!permissionService.existsByNameAndClient(userPermission.getValue(), adminPanelClient.getId())) {
                Permission permission = Permission.builder()
                        .name(userPermission.getValue())
                        .description(userPermission.getDescription())
                        .client(adminPanelClient)
                        .build();
                permissionService.insert(permission);
                log.info("Created permission: {}", userPermission.getValue());
            }
        }
    }

    private Role initSuperAdminRole(Client adminPanelClient) {
        String roleName = UserRole.SUPER_ADMIN.getValue();
        return roleService.findByNameAndClient(roleName, adminPanelClient.getId()).orElseGet(() -> {

            Role superAdminRole = Role.builder()
                    .name(roleName)
                    .client(adminPanelClient)
                    .build();
            Role role = roleService.insert(superAdminRole);
            log.info("Created role: {} with all permissions", roleName);
            return role;
        });
    }

    private Users initSuperAdminUser() {
        String phoneNumber = properties.getAdminPanel().getSuperAdmin().getPhoneNumber();
        return adminUserService.findByPhoneNumberOptional(phoneNumber)
                .orElseGet(() -> {
                    Users superAdmin = Users.builder()
                            .username(properties.getAdminPanel().getSuperAdmin().getUsername())
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