# OAuth2 Authorization Server - Security Tests

## Overview
Comprehensive security test suite for the OAuth2 Authorization Server, covering all critical security aspects and attack vectors.

## Test Coverage

### 1. Authentication Security Tests (`authentication/AuthenticationSecurityTest.java`)
**Coverage**: UserDetailsService and user authentication
- ✅ Valid user loading
- ✅ Username not found scenarios
- ✅ Null/empty username handling
- ✅ Account status validation (disabled, locked, expired)
- ✅ SQL injection prevention
- ✅ XSS attack prevention
- ✅ DoS prevention (long usernames)
- ✅ Case sensitivity enforcement
- ✅ Concurrent user loading

**Total Tests**: 24 tests

### 2. Token Security Tests (`token/TokenSecurityTest.java`)
**Coverage**: JWT token customization and generation
- ✅ Access token customization with roles and scopes
- ✅ Refresh token customization
- ✅ Client ID injection in tokens
- ✅ Empty roles/scopes handling
- ✅ Malicious role content filtering
- ✅ Token type case sensitivity
- ✅ Large number of roles handling
- ✅ Null token type handling

**Total Tests**: 21 tests

### 3. OTP Security Tests (`otp/OtpSecurityTest.java`)
**Coverage**: OTP storage, validation, and rate limiting
- ✅ Rate limiting (global, IP, phone number)
- ✅ OTP expiration validation
- ✅ Brute force protection
- ✅ Replay attack prevention
- ✅ Failed attempt tracking
- ✅ Account lockout after max attempts
- ✅ Session management and state validation
- ✅ OAuth2 parameter storage
- ✅ SQL injection prevention

**Total Tests**: 44 tests

### 4. OTP Service Security Tests (`otp/OtpServiceSecurityTest.java`)
**Coverage**: OTP generation and SMS delivery
- ✅ Secure OTP generation (6 digits, SecureRandom)
- ✅ OTP entropy and randomness
- ✅ Rate limiting enforcement
- ✅ Phone number masking in logs
- ✅ SMS delivery error handling
- ✅ Input validation (phone, IP)
- ✅ Brute force prevention
- ✅ SMS cost attack prevention
- ✅ Sensitive data exposure prevention
- ✅ Timing attack prevention

**Total Tests**: 48 tests

### 5. Injection Attack Security Tests (`injection/InjectionAttackSecurityTest.java`)
**Coverage**: Prevention of various injection attacks
- ✅ SQL injection (UNION, DROP TABLE, stacked queries, blind)
- ✅ XSS (script tags, img onerror, JavaScript protocol, SVG)
- ✅ LDAP injection
- ✅ Command injection (semicolon, pipe, backticks)
- ✅ XML injection (XXE, CDATA)
- ✅ Path traversal
- ✅ Null byte injection
- ✅ Unicode normalization attacks
- ✅ URL encoding bypass
- ✅ Expression Language injection
- ✅ SSTI (Server-Side Template Injection)
- ✅ ReDoS (Regular Expression DoS)
- ✅ CRLF injection
- ✅ Mass assignment
- ✅ Insecure deserialization
- ✅ Polyglot injection payloads

**Total Tests**: 39 tests

### 6. Authorization Security Tests (`authorization/AuthorizationSecurityTest.java`)
**Coverage**: Role-based access control and authorization
- ✅ Admin role permissions
- ✅ Super admin role permissions
- ✅ User self-access permissions
- ✅ Unauthorized access prevention
- ✅ Privilege escalation prevention
- ✅ Role hierarchy enforcement
- ✅ Anonymous access denial
- ✅ IDOR (Insecure Direct Object Reference) prevention
- ✅ Session management
- ✅ Token-based authorization
- ✅ Cross-tenant access prevention
- ✅ Forced browsing prevention
- ✅ Method-level security
- ✅ SpEL injection prevention
- ✅ Audit logging

**Total Tests**: 30 tests

### 7. CSRF Security Tests (`csrf/CSRFSecurityTest.java`)
**Coverage**: Cross-Site Request Forgery protection
- ✅ CSRF token generation
- ✅ Token uniqueness per session
- ✅ Token validation (header/parameter)
- ✅ Missing/invalid/empty token handling
- ✅ CSRF attacks (different session, replay, tampering)
- ✅ HTTP method requirements (POST, PUT, DELETE)
- ✅ Token format validation
- ✅ SameSite cookie support
- ✅ Double submit cookie pattern
- ✅ Stateless CSRF tokens

**Total Tests**: 35 tests

### 8. Registered Client Security Tests (`client/RegisteredClientSecurityTest.java`) ⭐ NEW
**Coverage**: OAuth2 client registration and management
- ✅ Client secret encoding before storage
- ✅ Plain text secret never stored
- ✅ Client retrieval by ID and client ID
- ✅ SQL injection prevention in client ID
- ✅ Null/empty/long client ID handling
- ✅ Client authentication methods (BASIC, POST, JWT)
- ✅ Authorization grant types (code, client credentials, refresh token)
- ✅ PKCE requirement for public clients
- ✅ Token lifetime configuration
- ✅ Redirect URI validation (HTTPS, exact match, no wildcards)
- ✅ Scope limitation
- ✅ Concurrent client lookup handling

**Total Tests**: 46 tests

### 9. Password Security Tests (`password/PasswordSecurityTest.java`) ⭐ NEW
**Coverage**: Password encoding, validation, and attack resistance
- ✅ BCrypt encoding with proper salt
- ✅ Same password produces different hashes
- ✅ Password matching validation
- ✅ Case sensitivity enforcement
- ✅ Brute force resistance (computational cost)
- ✅ Timing attack resistance
- ✅ Rainbow table resistance (unique salts)
- ✅ Password strength validation (weak, medium, strong)
- ✅ Common password detection
- ✅ Dictionary word handling
- ✅ Password policy enforcement (length, complexity)
- ✅ Password change scenarios
- ✅ Hash format validation
- ✅ Special characters and Unicode support

**Total Tests**: 40 tests

### 10. JWT Authentication Security Tests (`jwt/JwtAuthenticationSecurityTest.java`) ⭐ NEW
**Coverage**: JWT parsing and authentication token conversion
- ✅ JWT claims extraction (roles, scopes, client-id)
- ✅ Role prefix addition (ROLE_)
- ✅ Scope prefix addition (SCOPE_)
- ✅ Null role/scope filtering
- ✅ Malicious role injection prevention
- ✅ XSS in role claims
- ✅ Very long role names (DoS)
- ✅ Duplicate roles handling
- ✅ Case-sensitive role matching
- ✅ JWT standard claims validation
- ✅ Expired/not-yet-valid token handling
- ✅ Large number of authorities
- ✅ Wrong claim type handling
- ✅ Empty claims handling
- ✅ Concurrent JWT conversion

**Total Tests**: 32 tests

### 11. OAuth2 Flow Security Tests (`oauth2/OAuth2FlowSecurityTest.java`) ⭐ NEW
**Coverage**: OAuth2 authorization flows and PKCE
- ✅ Authorization code flow parameters
- ✅ State parameter generation and validation
- ✅ CSRF prevention via state
- ✅ PKCE code verifier generation
- ✅ PKCE code challenge (S256 method)
- ✅ PKCE verification
- ✅ Authorization code interception prevention
- ✅ Redirect URI validation (HTTPS, exact match, no wildcards)
- ✅ Open redirect prevention
- ✅ Authorization code one-time use
- ✅ Authorization code short expiration
- ✅ Authorization code unpredictability
- ✅ Client authentication at token endpoint
- ✅ Token endpoint POST-only
- ✅ Authorization code bound to client
- ✅ Grant type security
- ✅ Scope validation and privilege escalation prevention
- ✅ Attack scenarios (code injection, redirect manipulation, replay)

**Total Tests**: 31 tests

## Total Security Test Coverage
**Total Test Files**: 11
**Total Test Cases**: 390+

## Attack Vectors Covered

### Injection Attacks
- SQL Injection (all variants)
- XSS (Cross-Site Scripting)
- LDAP Injection
- Command Injection
- XML Injection (XXE)
- Path Traversal
- SSTI (Server-Side Template Injection)
- Expression Language Injection
- OGNL Injection

### Authentication Attacks
- Brute Force
- Credential Stuffing
- Password Spraying
- Timing Attacks
- Session Fixation
- Session Hijacking

### Authorization Attacks
- Privilege Escalation
- IDOR (Insecure Direct Object Reference)
- Forced Browsing
- Authorization Bypass

### Token Attacks
- Token Theft
- Token Tampering
- Token Replay
- JWT Manipulation

### OAuth2 Attacks
- Authorization Code Interception
- Redirect URI Manipulation
- CSRF (Cross-Site Request Forgery)
- Open Redirect
- Code Replay
- PKCE Bypass

### DoS Attacks
- ReDoS (Regular Expression DoS)
- Resource Exhaustion
- Rate Limit Bypass
- SMS Cost Attack

### Data Exposure
- Sensitive Information Leakage
- Insecure Logging
- Mass Assignment

## Running Tests

### Run All Security Tests
```bash
cd /home/user/webapp
mvn test -Dtest="com.behpardakht.oauth_server.authorization.security.**.*Test"
```

### Run Specific Security Test Category
```bash
# Authentication tests
mvn test -Dtest="AuthenticationSecurityTest"

# Token tests
mvn test -Dtest="TokenSecurityTest"

# OTP tests
mvn test -Dtest="Otp*SecurityTest"

# Injection tests
mvn test -Dtest="InjectionAttackSecurityTest"

# Authorization tests
mvn test -Dtest="AuthorizationSecurityTest"

# CSRF tests
mvn test -Dtest="CSRFSecurityTest"

# Client tests
mvn test -Dtest="RegisteredClientSecurityTest"

# Password tests
mvn test -Dtest="PasswordSecurityTest"

# JWT tests
mvn test -Dtest="JwtAuthenticationSecurityTest"

# OAuth2 flow tests
mvn test -Dtest="OAuth2FlowSecurityTest"
```

### Run with Coverage Report
```bash
mvn clean test jacoco:report
```

## Test Categories

### 🟢 SUCCESS Scenarios
Tests that verify correct behavior under normal conditions.

### 🔴 FAIL Scenarios
Tests that verify proper error handling and validation.

### 🟡 EDGE Cases
Tests for boundary conditions and unusual inputs.

### 🔒 SECURITY Scenarios
Tests for attack prevention and security vulnerability mitigation.

### ⚡ RELIABILITY Tests
Tests for concurrent access, error handling, and system stability.

## Security Best Practices Validated

1. **Password Security**
   - BCrypt with salt
   - Proper computational cost
   - No plain text storage

2. **Token Security**
   - JWT signature validation
   - Token expiration
   - Scope enforcement

3. **OTP Security**
   - Secure random generation
   - Rate limiting
   - One-time use
   - Short expiration

4. **OAuth2 Security**
   - PKCE for public clients
   - State parameter for CSRF
   - Redirect URI exact match
   - Authorization code one-time use

5. **Input Validation**
   - All inputs sanitized
   - SQL injection prevention
   - XSS prevention
   - Command injection prevention

6. **Access Control**
   - Role-based access
   - Method-level security
   - Resource-level authorization
   - Principle of least privilege

## Compliance
These tests help ensure compliance with:
- OWASP Top 10 security risks
- OAuth 2.0 Security Best Current Practice (RFC 8252, RFC 8414)
- PKCE (RFC 7636)
- JWT Best Practices (RFC 8725)
- PCI DSS requirements
- GDPR data protection

## Maintenance
- Review and update tests quarterly
- Add tests for new features
- Update attack vectors based on OWASP updates
- Run security scans regularly
- Monitor security advisories

## Contributing
When adding new security tests:
1. Follow existing naming conventions
2. Include both success and failure scenarios
3. Document the attack vector being tested
4. Add test to appropriate category
5. Update this README
