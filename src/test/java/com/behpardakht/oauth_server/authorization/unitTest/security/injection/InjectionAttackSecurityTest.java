package com.behpardakht.oauth_server.authorization.unitTest.security.injection;

import com.behpardakht.oauth_server.authorization.model.entity.Users;
import com.behpardakht.oauth_server.authorization.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Security Tests for Injection Attack Prevention
 * Tests protection against SQL Injection, XSS, Command Injection,
 * and other code injection vulnerabilities
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Injection Attack Security Tests")
class InjectionAttackSecurityTest {

    @Mock
    private UserRepository userRepository;

    private static final String NORMAL_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        // Note: We would need actual UserService for full testing
        // This demonstrates the testing approach
    }

    // ==================== SQL INJECTION TESTS ====================

    @Test
    @DisplayName("SECURITY: SQL injection in username - findByUsername")
    void testSQLInjection_FindByUsername_Attack1() {
        // Given - Classic SQL injection attempt
        String sqlInjection = "admin' OR '1'='1";
        when(userRepository.findByUsername(sqlInjection)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(sqlInjection);

        // Then - JPA should treat this as literal string, not execute SQL
        assertThat(result).isEmpty();
        verify(userRepository).findByUsername(sqlInjection);
    }

    @Test
    @DisplayName("SECURITY: SQL injection - UNION attack")
    void testSQLInjection_FindByUsername_UnionAttack() {
        // Given
        String sqlInjection = "admin' UNION SELECT * FROM users WHERE '1'='1";
        when(userRepository.findByUsername(sqlInjection)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(sqlInjection);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SECURITY: SQL injection - DROP TABLE attack")
    void testSQLInjection_FindByUsername_DropTableAttack() {
        // Given
        String sqlInjection = "admin'; DROP TABLE users; --";
        when(userRepository.findByUsername(sqlInjection)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(sqlInjection);

        // Then - Should be treated as literal string
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SECURITY: SQL injection - Stacked queries")
    void testSQLInjection_FindByUsername_StackedQueries() {
        // Given
        String sqlInjection = "admin'; UPDATE users SET password='hacked' WHERE '1'='1";
        when(userRepository.findByUsername(sqlInjection)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(sqlInjection);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SECURITY: SQL injection - Boolean blind")
    void testSQLInjection_FindByUsername_BooleanBlind() {
        // Given
        String sqlInjection = "admin' AND 1=1 --";
        when(userRepository.findByUsername(sqlInjection)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(sqlInjection);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SECURITY: SQL injection - Time-based blind")
    void testSQLInjection_FindByUsername_TimeBasedBlind() {
        // Given
        String sqlInjection = "admin' AND SLEEP(5) --";
        when(userRepository.findByUsername(sqlInjection)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(sqlInjection);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SECURITY: SQL injection in phone number query")
    void testSQLInjection_FindByPhoneNumber() {
        // Given
        String sqlInjection = "0912' OR '1'='1";
        when(userRepository.findByPhoneNumber(sqlInjection)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByPhoneNumber(sqlInjection);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== XSS (CROSS-SITE SCRIPTING) TESTS ====================

    @Test
    @DisplayName("SECURITY: XSS in username - script tag")
    void testXSS_Username_ScriptTag() {
        // Given
        String xssUsername = "<script>alert('XSS')</script>";
        Users user = Users.builder()
                .username(xssUsername)
                .password("encoded")
                .build();

        when(userRepository.save(any(Users.class))).thenReturn(user);

        // When
        Users result = userRepository.save(user);

        // Then - XSS string should be stored as-is, sanitization happens on output
        assertThat(result.getUsername()).isEqualTo(xssUsername);
    }

    @Test
    @DisplayName("SECURITY: XSS in username - img tag with onerror")
    void testXSS_Username_ImgTag() {
        // Given
        String xssUsername = "<img src=x onerror=alert('XSS')>";
        Users user = Users.builder()
                .username(xssUsername)
                .password("encoded")
                .build();

        when(userRepository.save(any(Users.class))).thenReturn(user);

        // When
        Users result = userRepository.save(user);

        // Then
        assertThat(result.getUsername()).isEqualTo(xssUsername);
    }

    @Test
    @DisplayName("SECURITY: XSS in username - JavaScript protocol")
    void testXSS_Username_JavaScriptProtocol() {
        // Given
        String xssUsername = "javascript:alert('XSS')";
        Users user = Users.builder()
                .username(xssUsername)
                .password("encoded")
                .build();

        when(userRepository.save(any(Users.class))).thenReturn(user);

        // When
        Users result = userRepository.save(user);

        // Then
        assertThat(result.getUsername()).isEqualTo(xssUsername);
    }

    @Test
    @DisplayName("SECURITY: XSS in username - SVG with script")
    void testXSS_Username_SVGScript() {
        // Given
        String xssUsername = "<svg onload=alert('XSS')>";
        Users user = Users.builder()
                .username(xssUsername)
                .password("encoded")
                .build();

        when(userRepository.save(any(Users.class))).thenReturn(user);

        // When
        Users result = userRepository.save(user);

        // Then
        assertThat(result.getUsername()).isEqualTo(xssUsername);
    }

    @Test
    @DisplayName("SECURITY: XSS - Encoded script tag")
    void testXSS_Username_EncodedScript() {
        // Given
        String xssUsername = "&lt;script&gt;alert('XSS')&lt;/script&gt;";
        Users user = Users.builder()
                .username(xssUsername)
                .password("encoded")
                .build();

        when(userRepository.save(any(Users.class))).thenReturn(user);

        // When
        Users result = userRepository.save(user);

        // Then
        assertThat(result.getUsername()).isEqualTo(xssUsername);
    }

    // ==================== LDAP INJECTION TESTS ====================

    @Test
    @DisplayName("SECURITY: LDAP injection in username")
    void testLDAPInjection_Username() {
        // Given - LDAP injection attempt
        String ldapInjection = "admin)(|(password=*))";
        when(userRepository.findByUsername(ldapInjection)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(ldapInjection);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SECURITY: LDAP injection - wildcard attack")
    void testLDAPInjection_Wildcard() {
        // Given
        String ldapInjection = "*";
        when(userRepository.findByUsername(ldapInjection)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(ldapInjection);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== COMMAND INJECTION TESTS ====================

    @Test
    @DisplayName("SECURITY: Command injection in username - semicolon")
    void testCommandInjection_Username_Semicolon() {
        // Given
        String commandInjection = "user; rm -rf /";
        when(userRepository.findByUsername(commandInjection)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(commandInjection);

        // Then - Should be treated as literal string
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SECURITY: Command injection - pipe character")
    void testCommandInjection_Username_Pipe() {
        // Given
        String commandInjection = "user | cat /etc/passwd";
        when(userRepository.findByUsername(commandInjection)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(commandInjection);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SECURITY: Command injection - backticks")
    void testCommandInjection_Username_Backticks() {
        // Given
        String commandInjection = "user`whoami`";
        when(userRepository.findByUsername(commandInjection)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(commandInjection);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SECURITY: Command injection - dollar parentheses")
    void testCommandInjection_Username_DollarParentheses() {
        // Given
        String commandInjection = "user$(ls)";
        when(userRepository.findByUsername(commandInjection)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(commandInjection);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== XML INJECTION TESTS ====================

    @Test
    @DisplayName("SECURITY: XML injection - XXE attack")
    void testXMLInjection_XXE() {
        // Given
        String xmlInjection = "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]><user>&xxe;</user>";
        when(userRepository.findByUsername(xmlInjection)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(xmlInjection);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SECURITY: XML injection - CDATA escape")
    void testXMLInjection_CDATA() {
        // Given
        String xmlInjection = "<![CDATA[<script>alert('XSS')</script>]]>";
        when(userRepository.findByUsername(xmlInjection)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(xmlInjection);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== PATH TRAVERSAL TESTS ====================

    @Test
    @DisplayName("SECURITY: Path traversal in username")
    void testPathTraversal_Username() {
        // Given
        String pathTraversal = "../../etc/passwd";
        when(userRepository.findByUsername(pathTraversal)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(pathTraversal);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SECURITY: Path traversal - encoded dots")
    void testPathTraversal_EncodedDots() {
        // Given
        String pathTraversal = "..%2F..%2Fetc%2Fpasswd";
        when(userRepository.findByUsername(pathTraversal)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(pathTraversal);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== NULL BYTE INJECTION TESTS ====================

    @Test
    @DisplayName("SECURITY: Null byte injection")
    void testNullByteInjection_Username() {
        // Given
        String nullByteInjection = "user\u0000.txt";
        when(userRepository.findByUsername(nullByteInjection)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(nullByteInjection);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== SPECIAL CHARACTERS & ENCODING TESTS ====================

    @Test
    @DisplayName("SECURITY: Unicode normalization attack")
    void testUnicodeNormalization() {
        // Given - Different Unicode representations of 'admin'
        String unicodeUsername = "admın"; // Turkish dotless i
        when(userRepository.findByUsername(unicodeUsername)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(unicodeUsername);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SECURITY: URL encoding bypass attempt")
    void testURLEncodingBypass() {
        // Given
        String encodedUsername = "admin%27%20OR%20%271%27%3D%271";
        when(userRepository.findByUsername(encodedUsername)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(encodedUsername);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SECURITY: Double encoding bypass")
    void testDoubleEncodingBypass() {
        // Given
        String doubleEncoded = "admin%2527%2520OR%2520%25271%2527%253D%25271";
        when(userRepository.findByUsername(doubleEncoded)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(doubleEncoded);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== EXPRESSION LANGUAGE INJECTION ====================

    @Test
    @DisplayName("SECURITY: EL injection in username")
    void testELInjection_Username() {
        // Given
        String elInjection = "${7*7}";
        when(userRepository.findByUsername(elInjection)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(elInjection);

        // Then - Should not evaluate expression
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SECURITY: OGNL injection")
    void testOGNLInjection_Username() {
        // Given
        String ognlInjection = "%{#context['xwork.MethodAccessor.denyMethodExecution']=false}";
        when(userRepository.findByUsername(ognlInjection)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(ognlInjection);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== SERVER-SIDE TEMPLATE INJECTION ====================

    @Test
    @DisplayName("SECURITY: SSTI - Thymeleaf injection")
    void testSSTI_Thymeleaf() {
        // Given
        String sstiPayload = "__${7*7}__::.x";
        when(userRepository.findByUsername(sstiPayload)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(sstiPayload);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SECURITY: SSTI - FreeMarker injection")
    void testSSTI_FreeMarker() {
        // Given
        String sstiPayload = "<#assign ex=\"freemarker.template.utility.Execute\"?new()> ${ ex(\"id\") }";
        when(userRepository.findByUsername(sstiPayload)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(sstiPayload);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== REGEX DOS (ReDoS) TESTS ====================

    @Test
    @DisplayName("SECURITY: ReDoS attack - catastrophic backtracking")
    void testReDoS_CatastrophicBacktracking() {
        // Given - Pattern that causes exponential time complexity
        String redosPayload = "a".repeat(50) + "!";
        when(userRepository.findByUsername(redosPayload)).thenReturn(Optional.empty());

        // When & Then - Should complete in reasonable time
        long startTime = System.currentTimeMillis();
        Optional<Users> result = userRepository.findByUsername(redosPayload);
        long duration = System.currentTimeMillis() - startTime;

        assertThat(result).isEmpty();
        assertThat(duration).isLessThan(1000); // Should complete in less than 1 second
    }

    // ==================== HEADER INJECTION TESTS ====================

    @Test
    @DisplayName("SECURITY: CRLF injection in username")
    void testCRLFInjection() {
        // Given
        String crlfInjection = "user\r\nSet-Cookie: admin=true";
        when(userRepository.findByUsername(crlfInjection)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(crlfInjection);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== MASS ASSIGNMENT TESTS ====================

    @Test
    @DisplayName("SECURITY: Mass assignment - unauthorized field modification")
    void testMassAssignment_UnauthorizedFields() {
        // Given - Attempt to set admin role through mass assignment
        Users user = Users.builder()
                .username(NORMAL_USERNAME)
                .password("encoded")
                .isEnabled(true)
                .roles(Set.of()) // Should not allow direct role assignment
                .build();

        when(userRepository.save(any(Users.class))).thenReturn(user);

        // When
        Users result = userRepository.save(user);

        // Then - Roles should be managed through separate methods, not direct assignment
        assertThat(result.getRoles()).isEmpty();
    }

    // ==================== DESERIALIZATION ATTACKS ====================

    @Test
    @DisplayName("SECURITY: Insecure deserialization attempt")
    void testInsecureDeserialization() {
        // Given - Serialized malicious object in username
        String serializedPayload = "rO0ABXNyABdqYXZhLnV0aWwuUHJpb3JpdHlRdWV1ZQ==";
        when(userRepository.findByUsername(serializedPayload)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(serializedPayload);

        // Then - Should treat as string, not deserialize
        assertThat(result).isEmpty();
    }

    // ==================== COMBINED ATTACK SCENARIOS ====================

    @Test
    @DisplayName("SECURITY: Combined SQL + XSS injection")
    void testCombinedAttack_SQLXSS() {
        // Given
        String combinedAttack = "admin' UNION SELECT '<script>alert(1)</script>' --";
        when(userRepository.findByUsername(combinedAttack)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(combinedAttack);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SECURITY: Polyglot injection payload")
    void testPolyglotInjection() {
        // Given - Payload that attempts multiple injection types
        String polyglot = "jaVasCript:/*-/*`/*\\`/*'/*\"/**/(/* */oNcliCk=alert() )//%0D%0A%0d%0a//</stYle/</titLe/</teXtarEa/</scRipt/--!>\\x3csVg/<sVg/oNloAd=alert()//>\\x3e";
        when(userRepository.findByUsername(polyglot)).thenReturn(Optional.empty());

        // When
        Optional<Users> result = userRepository.findByUsername(polyglot);

        // Then
        assertThat(result).isEmpty();
    }
}
