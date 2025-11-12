package com.behpardakht.oauth_server.authorization.unitTest.security.authentication;

import com.behpardakht.oauth_server.authorization.model.entity.Role;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import com.behpardakht.oauth_server.authorization.repository.UserRepository;
import com.behpardakht.oauth_server.authorization.security.common.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Security Tests for Authentication Mechanisms
 * Tests authentication security including user loading, credentials validation,
 * and protection against authentication attacks
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Authentication Security Tests")
class AuthenticationSecurityTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private Users validUser;
    private Users disabledUser;
    private Users lockedUser;
    private Users expiredUser;

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName("ROLE_USER");

        // Valid active user
        validUser = Users.builder()
                .id(1L)
                .username("testuser")
                .password("$2a$10$encodedPassword")
                .phoneNumber("09123456789")
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .roles(Set.of(userRole))
                .build();

        // Disabled user
        disabledUser = Users.builder()
                .id(2L)
                .username("disableduser")
                .password("$2a$10$encodedPassword")
                .isEnabled(false)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .roles(Set.of(userRole))
                .build();

        // Locked user
        lockedUser = Users.builder()
                .id(3L)
                .username("lockeduser")
                .password("$2a$10$encodedPassword")
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(false)
                .isCredentialsNonExpired(true)
                .roles(Set.of(userRole))
                .build();

        // Expired user
        expiredUser = Users.builder()
                .id(4L)
                .username("expireduser")
                .password("$2a$10$encodedPassword")
                .isEnabled(true)
                .isAccountNonExpired(false)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(false)
                .roles(Set.of(userRole))
                .build();
    }

    // ==================== SUCCESS SCENARIOS ====================

    @Test
    @DisplayName("SUCCESS: Load valid active user")
    void testLoadUserByUsername_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(validUser));

        // When
        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getPassword()).isEqualTo("$2a$10$encodedPassword");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.isAccountNonExpired()).isTrue();
        assertThat(result.isAccountNonLocked()).isTrue();
        assertThat(result.isCredentialsNonExpired()).isTrue();
        assertThat(result.getAuthorities()).hasSize(1);
        
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("SUCCESS: Load user with correct authorities")
    void testLoadUserByUsername_WithCorrectAuthorities() {
        // Given
        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ROLE_ADMIN");
        
        validUser.setRoles(Set.of(adminRole));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(validUser));

        // When
        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        // Then
        assertThat(result.getAuthorities())
                .hasSize(1)
                .extracting("authority")
                .contains("ROLE_ADMIN");
    }

    // ==================== FAILURE SCENARIOS ====================

    @Test
    @DisplayName("FAIL: Username not found")
    void testLoadUserByUsername_UserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User Not Found");
        
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("FAIL: Null username throws IllegalArgumentException")
    void testLoadUserByUsername_NullUsername() {
        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("username is null");
        
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    @DisplayName("FAIL: Empty username throws UsernameNotFoundException")
    void testLoadUserByUsername_EmptyUsername() {
        // Given
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(""))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    // ==================== ACCOUNT STATUS TESTS ====================

    @Test
    @DisplayName("SECURITY: Disabled user can be loaded but flag is false")
    void testLoadUserByUsername_DisabledUser() {
        // Given
        when(userRepository.findByUsername("disableduser")).thenReturn(Optional.of(disabledUser));

        // When
        UserDetails result = userDetailsService.loadUserByUsername("disableduser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getUsername()).isEqualTo("disableduser");
    }

    @Test
    @DisplayName("SECURITY: Locked user can be loaded but flag is false")
    void testLoadUserByUsername_LockedUser() {
        // Given
        when(userRepository.findByUsername("lockeduser")).thenReturn(Optional.of(lockedUser));

        // When
        UserDetails result = userDetailsService.loadUserByUsername("lockeduser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isAccountNonLocked()).isFalse();
        assertThat(result.getUsername()).isEqualTo("lockeduser");
    }

    @Test
    @DisplayName("SECURITY: Expired user can be loaded but flags are false")
    void testLoadUserByUsername_ExpiredUser() {
        // Given
        when(userRepository.findByUsername("expireduser")).thenReturn(Optional.of(expiredUser));

        // When
        UserDetails result = userDetailsService.loadUserByUsername("expireduser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isAccountNonExpired()).isFalse();
        assertThat(result.isCredentialsNonExpired()).isFalse();
        assertThat(result.getUsername()).isEqualTo("expireduser");
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("EDGE: Username with special characters")
    void testLoadUserByUsername_SpecialCharacters() {
        // Given
        validUser.setUsername("user@example.com");
        when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.of(validUser));

        // When
        UserDetails result = userDetailsService.loadUserByUsername("user@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("EDGE: Username with whitespace (should be handled)")
    void testLoadUserByUsername_WithWhitespace() {
        // Given
        when(userRepository.findByUsername(" testuser ")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(" testuser "))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("EDGE: User with no roles")
    void testLoadUserByUsername_NoRoles() {
        // Given
        validUser.setRoles(Set.of());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(validUser));

        // When
        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("EDGE: User with multiple roles")
    void testLoadUserByUsername_MultipleRoles() {
        // Given
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName("ROLE_USER");
        
        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ROLE_ADMIN");
        
        validUser.setRoles(Set.of(userRole, adminRole));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(validUser));

        // When
        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        // Then
        assertThat(result.getAuthorities()).hasSize(2);
    }

    // ==================== SECURITY ATTACK SCENARIOS ====================

    @Test
    @DisplayName("SECURITY: SQL Injection attempt in username")
    void testLoadUserByUsername_SQLInjectionAttempt() {
        // Given
        String maliciousUsername = "admin' OR '1'='1";
        when(userRepository.findByUsername(maliciousUsername)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(maliciousUsername))
                .isInstanceOf(UsernameNotFoundException.class);
        
        // Verify the malicious string is passed as-is (JPA should handle sanitization)
        verify(userRepository, times(1)).findByUsername(maliciousUsername);
    }

    @Test
    @DisplayName("SECURITY: XSS attempt in username")
    void testLoadUserByUsername_XSSAttempt() {
        // Given
        String xssUsername = "<script>alert('XSS')</script>";
        when(userRepository.findByUsername(xssUsername)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(xssUsername))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("SECURITY: Very long username (DoS attempt)")
    void testLoadUserByUsername_VeryLongUsername() {
        // Given
        String longUsername = "a".repeat(10000);
        when(userRepository.findByUsername(longUsername)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(longUsername))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("SECURITY: Case sensitivity in username")
    void testLoadUserByUsername_CaseSensitivity() {
        // Given
        when(userRepository.findByUsername("TestUser")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(validUser));

        // When & Then - Different case should not find the user
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("TestUser"))
                .isInstanceOf(UsernameNotFoundException.class);
        
        // Correct case should work
        UserDetails result = userDetailsService.loadUserByUsername("testuser");
        assertThat(result).isNotNull();
    }

    // ==================== PERFORMANCE & RELIABILITY ====================

    @Test
    @DisplayName("RELIABILITY: Repository exception is propagated")
    void testLoadUserByUsername_RepositoryException() {
        // Given
        when(userRepository.findByUsername("testuser"))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("testuser"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database connection failed");
    }

    @Test
    @DisplayName("RELIABILITY: Concurrent user loading")
    void testLoadUserByUsername_ConcurrentAccess() throws InterruptedException {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(validUser));

        // When - Simulate concurrent requests
        Thread thread1 = new Thread(() -> userDetailsService.loadUserByUsername("testuser"));
        Thread thread2 = new Thread(() -> userDetailsService.loadUserByUsername("testuser"));
        
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // Then - Should handle concurrent access
        verify(userRepository, atLeast(2)).findByUsername("testuser");
    }
}
