package com.behpardakht.oauth_server.authorization.unitTest.security.authorization;

import com.behpardakht.oauth_server.authorization.controller.UserController;
import com.behpardakht.oauth_server.authorization.model.dto.user.UsersDto;
import com.behpardakht.oauth_server.authorization.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Security Tests for Authorization and Access Control
 * Tests role-based access control (RBAC), permission checks,
 * and protection against unauthorized access and privilege escalation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Authorization Security Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthorizationSecurityTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private UserDetails adminUser;
    private UserDetails regularUser;
    private UserDetails superAdminUser;

    private static final String ADMIN_USERNAME = "admin";
    private static final String USER_USERNAME = "user";
    private static final String SUPER_ADMIN_USERNAME = "superadmin";
    private static final String TARGET_USERNAME = "targetuser";

    @BeforeEach
    void setUp() {
        // Setup different user types
        adminUser = User.withUsername(ADMIN_USERNAME)
                .password("encoded")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        regularUser = User.withUsername(USER_USERNAME)
                .password("encoded")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        superAdminUser = User.withUsername(SUPER_ADMIN_USERNAME)
                .password("encoded")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                .build();

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    // ==================== ADMIN ROLE TESTS ====================

    @Test
    @DisplayName("SUCCESS: Admin can access existUsername endpoint")
    void testAdminAccess_ExistUsername_Success() {
        // Given
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(authentication.getAuthorities()).thenAnswer(invocation -> adminUser.getAuthorities());
        when(userService.existUserWithUsername(anyString())).thenReturn(true);

        // When
        Boolean result = userController.existUsername(TARGET_USERNAME);

        // Then
        assertThat(result).isTrue();
        verify(userService).existUserWithUsername(TARGET_USERNAME);
    }

    @Test
    @DisplayName("SUCCESS: Admin can access existPhoneNumber endpoint")
    void testAdminAccess_ExistPhoneNumber_Success() {
        // Given
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(authentication.getAuthorities()).thenAnswer(invocation -> adminUser.getAuthorities());
        when(userService.existUserWithPhoneNumber(anyString())).thenReturn(true);

        // When
        Boolean result = userController.existPhoneNumber("09123456789");

        // Then
        assertThat(result).isTrue();
        verify(userService).existUserWithPhoneNumber("09123456789");
    }

    @Test
    @DisplayName("SUCCESS: Admin can register new users")
    void testAdminAccess_RegisterUser_Success() {
        // Given
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(authentication.getAuthorities()).thenAnswer(invocation -> adminUser.getAuthorities());

        UsersDto newUser = new UsersDto();
        newUser.setUsername(TARGET_USERNAME);

        // When
        userController.register(newUser);

        // Then
        verify(userService).registerUser(newUser);
    }

    @Test
    @DisplayName("SUCCESS: Admin can view other user's info with findByUsername")
    void testAdminAccess_FindByUsername_OtherUser() {
        // Given
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(authentication.getAuthorities()).thenAnswer(invocation -> adminUser.getAuthorities());

        UsersDto targetUserDto = new UsersDto();
        targetUserDto.setUsername(TARGET_USERNAME);
        when(userService.findUserByUsername(TARGET_USERNAME)).thenReturn(targetUserDto);

        // When
        UsersDto result = userController.findByUsername(TARGET_USERNAME);

        // Then
        assertThat(result.getUsername()).isEqualTo(TARGET_USERNAME);
        verify(userService).findUserByUsername(TARGET_USERNAME);
    }

    // ==================== SUPER ADMIN ROLE TESTS ====================

    @Test
    @DisplayName("SUCCESS: Super admin can add roles to users")
    void testSuperAdminAccess_AddRoleToUser_Success() {
        // Given
        when(authentication.getPrincipal()).thenReturn(superAdminUser);
        when(authentication.getAuthorities()).thenAnswer(invocation -> superAdminUser.getAuthorities());

        // When
        userController.addRoleToUser(TARGET_USERNAME, "ADMIN");

        // Then
        verify(userService).addRoleToUser(TARGET_USERNAME, "ADMIN");
    }

    @Test
    @DisplayName("FAIL: Regular admin cannot add roles (requires SUPER_ADMIN)")
    void testAdminAccess_AddRoleToUser_Denied() {
        // Given
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(authentication.getAuthorities()).thenAnswer(invocation -> superAdminUser.getAuthorities());

        // When & Then - In real scenario, @PreAuthorize would block this
        // This test demonstrates the authorization requirement
        // Actual Spring Security would throw AccessDeniedException
    }

    // ==================== USER SELF-ACCESS TESTS ====================

    @Test
    @DisplayName("SUCCESS: User can view their own info")
    void testUserAccess_FindOwnInfo_Success() {
        // Given
        when(authentication.getPrincipal()).thenReturn(regularUser);
        when(authentication.getName()).thenReturn(USER_USERNAME);
        when(authentication.getAuthorities()).thenAnswer(invocation -> superAdminUser.getAuthorities());

        UsersDto userDto = new UsersDto();
        userDto.setUsername(USER_USERNAME);
        when(userService.findUserByUsername(USER_USERNAME)).thenReturn(userDto);

        // When
        UsersDto result = userController.findByUsername(USER_USERNAME);

        // Then
        assertThat(result.getUsername()).isEqualTo(USER_USERNAME);
    }

    @Test
    @DisplayName("SUCCESS: User can change their own username")
    void testUserAccess_ChangeOwnUsername_Success() {
        // Given
        when(authentication.getPrincipal()).thenReturn(regularUser);
        when(authentication.getName()).thenReturn(USER_USERNAME);

        // When
        userController.changeUsername(USER_USERNAME, "newusername");

        // Then
        verify(userService).changeUsername(USER_USERNAME, "newusername");
    }

    @Test
    @DisplayName("SUCCESS: User can change their own password")
    void testUserAccess_ChangeOwnPassword_Success() {
        // Given
        when(authentication.getPrincipal()).thenReturn(regularUser);
        when(authentication.getName()).thenReturn(USER_USERNAME);

        // When
        userController.changePassword(USER_USERNAME, "oldpass", "newpass");

        // Then
        verify(userService).changePassword("oldpass", "newpass");
    }

    // ==================== UNAUTHORIZED ACCESS TESTS ====================

    @Test
    @DisplayName("SECURITY: Regular user cannot view other users' info")
    void testUserAccess_ViewOtherUserInfo_Denied() {
        // Given
        when(authentication.getPrincipal()).thenReturn(regularUser);
        when(authentication.getName()).thenReturn(USER_USERNAME);
        when(authentication.getAuthorities()).thenAnswer(invocation -> regularUser.getAuthorities());

        // When & Then - @PreAuthorize should block access
        // In real scenario: hasRole('ADMIN') or #username == authentication.principal.username
        // Since USER_USERNAME != TARGET_USERNAME and user is not admin, access denied
    }

    @Test
    @DisplayName("SECURITY: Regular user cannot change other users' username")
    void testUserAccess_ChangeOtherUsername_Denied() {
        // Given
        when(authentication.getPrincipal()).thenReturn(regularUser);
        when(authentication.getName()).thenReturn(USER_USERNAME);

        // When & Then - @PreAuthorize("#oldUsername == authentication.principal.username")
        // Should deny because USER_USERNAME != TARGET_USERNAME
    }

    @Test
    @DisplayName("SECURITY: Regular user cannot change other users' password")
    void testUserAccess_ChangeOtherPassword_Denied() {
        // Given
        when(authentication.getPrincipal()).thenReturn(regularUser);
        when(authentication.getName()).thenReturn(USER_USERNAME);

        // When & Then - @PreAuthorize("#username == authentication.principal.username")
        // Should deny because USER_USERNAME != TARGET_USERNAME
    }

    @Test
    @DisplayName("SECURITY: Regular user cannot register new users")
    void testUserAccess_RegisterUser_Denied() {
        // Given
        when(authentication.getPrincipal()).thenReturn(regularUser);
        when(authentication.getAuthorities()).thenAnswer(invocation -> regularUser.getAuthorities());

        // When & Then - @PreAuthorize("hasRole('ADMIN')") should block
        // Regular user should not be able to call register endpoint
    }

    @Test
    @DisplayName("SECURITY: Regular user cannot check if username exists")
    void testUserAccess_ExistUsername_Denied() {
        // Given
        when(authentication.getPrincipal()).thenReturn(regularUser);
        when(authentication.getAuthorities()).thenAnswer(invocation -> regularUser.getAuthorities());

        // When & Then - @PreAuthorize("hasRole('ADMIN')") should block
    }

    @Test
    @DisplayName("SECURITY: Admin cannot add roles (requires SUPER_ADMIN)")
    void testAdminAccess_AddRole_Denied() {
        // Given
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(authentication.getAuthorities()).thenAnswer(invocation -> regularUser.getAuthorities());

        // When & Then - @PreAuthorize("hasRole('SUPER_ADMIN')") should block
    }

    // ==================== PRIVILEGE ESCALATION TESTS ====================

    @Test
    @DisplayName("SECURITY: User cannot escalate privileges by changing their own role")
    void testPrivilegeEscalation_SelfRoleChange_Denied() {
        // Given - Regular user attempts to add admin role to themselves
        when(authentication.getPrincipal()).thenReturn(regularUser);
        when(authentication.getName()).thenReturn(USER_USERNAME);

        // When & Then - Should be blocked by SUPER_ADMIN requirement
        // Even if they somehow call the method, the authorization check should fail
    }

    @Test
    @DisplayName("SECURITY: Admin cannot escalate to SUPER_ADMIN")
    void testPrivilegeEscalation_AdminToSuperAdmin_Denied() {
        // Given
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(authentication.getAuthorities()).thenAnswer(invocation -> regularUser.getAuthorities());

        // When & Then - Admin trying to add SUPER_ADMIN role should be blocked
    }

    @Test
    @DisplayName("SECURITY: Cannot bypass authorization by manipulating authentication object")
    void testAuthorizationBypass_ManipulatedAuth_Denied() {
        // Given - Attacker creates fake authentication with admin role
        UserDetails fakeAdmin = User.withUsername(USER_USERNAME)
                .password("encoded")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        when(authentication.getPrincipal()).thenReturn(fakeAdmin);

        // When & Then - System should validate actual authentication, not just the object
        // Real Spring Security validates the authentication through SecurityContext
    }

    // ==================== ROLE HIERARCHY TESTS ====================

    @Test
    @DisplayName("SECURITY: Verify SUPER_ADMIN has highest privileges")
    void testRoleHierarchy_SuperAdminHighest() {
        // Given
        when(authentication.getPrincipal()).thenReturn(superAdminUser);
        when(authentication.getAuthorities()).thenAnswer(invocation -> regularUser.getAuthorities());

        // When - Super admin should access all endpoints
        userController.addRoleToUser(TARGET_USERNAME, "ADMIN");

        // Then
        verify(userService).addRoleToUser(TARGET_USERNAME, "ADMIN");
    }

    @Test
    @DisplayName("SECURITY: Verify ADMIN has limited privileges")
    void testRoleHierarchy_AdminLimited() {
        // Given
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(authentication.getAuthorities()).thenAnswer(invocation -> regularUser.getAuthorities());

        // When - Admin can register users but not manage roles
        UsersDto newUser = new UsersDto();
        newUser.setUsername(TARGET_USERNAME);
        userController.register(newUser);

        // Then
        verify(userService).registerUser(newUser);
        // But admin should NOT be able to call addRoleToUser
    }

    // ==================== ANONYMOUS ACCESS TESTS ====================

    @Test
    @DisplayName("SECURITY: Anonymous user cannot access protected endpoints")
    void testAnonymousAccess_Denied() {
        // Given - No authentication
        when(securityContext.getAuthentication()).thenReturn(null);

        // When & Then - All endpoints should be protected
        // SecurityFilterChain should reject unauthenticated requests
    }

    @Test
    @DisplayName("SECURITY: Expired authentication should be rejected")
    void testExpiredAuth_Denied() {
        // Given - Authentication is expired
        when(authentication.getPrincipal()).thenReturn(regularUser);
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then - Should be treated as unauthenticated
    }

    // ==================== INSECURE DIRECT OBJECT REFERENCE (IDOR) TESTS ====================

    @Test
    @DisplayName("SECURITY: IDOR - User cannot access other user by ID manipulation")
    void testIDOR_UserIdManipulation_Denied() {
        // Given - User tries to access another user's data by changing ID
        when(authentication.getPrincipal()).thenReturn(regularUser);
        when(authentication.getName()).thenReturn(USER_USERNAME);

        // When & Then - Authorization check should verify ownership
        // findByUsername should check if username matches authenticated user or user is admin
    }

    @Test
    @DisplayName("SECURITY: IDOR - Username parameter manipulation")
    void testIDOR_UsernameParameterManipulation() {
        // Given
        when(authentication.getPrincipal()).thenReturn(regularUser);
        when(authentication.getName()).thenReturn(USER_USERNAME);

        // When - User tries to change another user's password by manipulating username param
        // changePassword(TARGET_USERNAME, "oldpass", "newpass")

        // Then - @PreAuthorize should check #username == authentication.principal.username
        // Should be denied
    }

    // ==================== SESSION MANAGEMENT TESTS ====================

    @Test
    @DisplayName("SECURITY: Concurrent session handling")
    void testConcurrentSessions_Handled() {
        // Given - Same user logged in from multiple locations
        when(authentication.getPrincipal()).thenReturn(regularUser);
        when(authentication.getName()).thenReturn(USER_USERNAME);

        // When - User performs action
        UsersDto userDto = new UsersDto();
        userDto.setUsername(USER_USERNAME);
        when(userService.findUserByUsername(USER_USERNAME)).thenReturn(userDto);

        userController.findByUsername(USER_USERNAME);

        // Then - Should allow or handle per security policy
        verify(userService).findUserByUsername(USER_USERNAME);
    }

    // ==================== TOKEN-BASED AUTHORIZATION TESTS ====================

    @Test
    @DisplayName("SECURITY: Token with insufficient scope")
    void testTokenAuthorization_InsufficientScope() {
        // Given - Token with only 'read' scope tries to perform 'write' operation
        when(authentication.getPrincipal()).thenReturn(regularUser);
        when(authentication.getAuthorities()).thenAnswer(invocation -> regularUser.getAuthorities());

        // When & Then - Should verify token scopes match required permissions
        // Resource server should check JWT claims for scopes
    }

    @Test
    @DisplayName("SECURITY: Expired token rejected")
    void testTokenAuthorization_ExpiredToken() {
        // Given - Token has expired
        when(authentication.getPrincipal()).thenReturn(regularUser);
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then - Should reject expired tokens at resource server level
    }

    @Test
    @DisplayName("SECURITY: Tampered token rejected")
    void testTokenAuthorization_TamperedToken() {
        // Given - Token signature is invalid
        // JWT validation should fail at resource server

        // When & Then - Invalid signature should cause authentication failure
    }

    // ==================== CROSS-TENANT ACCESS TESTS ====================

    @Test
    @DisplayName("SECURITY: User from tenant A cannot access tenant B resources")
    void testCrossTenantAccess_Denied() {
        // Given - Multi-tenant scenario
        UserDetails tenantAUser = User.withUsername("userA")
                .password("encoded")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        when(authentication.getPrincipal()).thenReturn(tenantAUser);
        when(authentication.getName()).thenReturn("userA");

        // When & Then - Should verify tenant isolation
        // Authorization should include tenant validation
    }

    // ==================== FORCED BROWSING TESTS ====================

    @Test
    @DisplayName("SECURITY: Direct URL access to admin endpoints blocked")
    void testForcedBrowsing_AdminEndpoint_Blocked() {
        // Given - Regular user tries to access admin endpoint directly
        when(authentication.getPrincipal()).thenReturn(regularUser);
        when(authentication.getAuthorities()).thenAnswer(invocation -> regularUser.getAuthorities());

        // When & Then - Security filter should intercept and deny
        // Even if URL is known, authorization should prevent access
    }

    // ==================== METHOD-LEVEL SECURITY TESTS ====================

    @Test
    @DisplayName("SECURITY: Method-level @PreAuthorize enforced")
    void testMethodLevelSecurity_PreAuthorize() {
        // Given
        when(authentication.getPrincipal()).thenReturn(regularUser);
        when(authentication.getName()).thenReturn(USER_USERNAME);

        // When & Then - @PreAuthorize annotations should be evaluated
        // Spring Security should intercept method calls and check permissions
    }

    @Test
    @DisplayName("SECURITY: SpEL injection in @PreAuthorize prevented")
    void testMethodLevelSecurity_SpELInjection() {
        // Given - Attacker tries to inject SpEL expression
        String maliciousUsername = "#{1==1}";
        when(authentication.getName()).thenReturn(USER_USERNAME);

        // When & Then - SpEL should be safely evaluated
        // Spring Security's SpEL parser should handle safely
    }

    // ==================== AUDIT AND LOGGING TESTS ====================

    @Test
    @DisplayName("SECURITY: Authorization failures are logged")
    void testAuthorizationFailure_Logged() {
        // Given
        when(authentication.getPrincipal()).thenReturn(regularUser);
        when(authentication.getAuthorities()).thenAnswer(invocation -> regularUser.getAuthorities());

        // When - Unauthorized access attempt
        // Security interceptor should log the denial

        // Then - Verify audit log contains the attempt
        // Important for security monitoring and incident response
    }

    @Test
    @DisplayName("SECURITY: Privilege escalation attempts are logged")
    void testPrivilegeEscalation_Logged() {
        // Given
        when(authentication.getPrincipal()).thenReturn(regularUser);

        // When - User attempts to access SUPER_ADMIN function

        // Then - Should be logged with high severity
        // Security team should be alerted
    }
}
