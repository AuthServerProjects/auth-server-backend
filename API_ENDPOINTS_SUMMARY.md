# Complete API Endpoints Summary

## Existing APIs (Currently Implemented)

### 1. AuthController - `/api/auth/`
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/logout` | Logout from current device | Authenticated |
| POST | `/logout-all` | Logout from all devices | Authenticated |

**Total: 2 endpoints**

---

### 2. ClientController - `/api/client/`
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/{clientId}` | Get client by ID | ADMIN |
| POST | `/register` | Register new OAuth2 client | SUPER_ADMIN |
| POST | `/defaultRegister` | Register default test client | Public |

**Total: 3 endpoints**

---

### 3. UserController - `/api/user/`
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/findByUsername` | Find user by username | ADMIN/Self |
| GET | `/existUsername` | Check if username exists | ADMIN |
| GET | `/existPhoneNumber` | Check if phone exists | ADMIN |
| POST | `/register` | Register new user | ADMIN |
| POST | `/changeUsername` | Change username | Self |
| POST | `/changePassword` | Change password | Self |
| POST | `/addRoleToUser` | Assign role to user | SUPER_ADMIN |

**Total: 7 endpoints**

---

### 4. RoleController - `/api/role/`
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/save` | Create new role | SUPER_ADMIN |
| GET | `/findAll` | List all roles | ADMIN |

**Total: 2 endpoints**

---

### 5. OtpController - `/api/otp/`
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/initialize` | Initialize OTP session | Public |
| POST | `/send` | Send OTP code | Public |
| POST | `/verify` | Verify OTP code | Public |

**Total: 3 endpoints**

---

### 6. GeneralController - `/api/general/`
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/loadAuthenticationMethodType` | Get available auth methods | ADMIN |
| GET | `/loadAuthorizationGrantType` | Get available grant types | ADMIN |
| GET | `/loadScopeType` | Get available scope types | ADMIN |

**Total: 3 endpoints**

---

## **Current Total: 20 endpoints across 6 controllers**

---
---

## Recommended New APIs for Admin Panel

### 7. ClientController (Extended) - `/api/client/`

#### List & Search (5 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/list` | Paginated list of all clients | ADMIN |
| GET | `/search` | Search clients by criteria | ADMIN |
| GET | `/count` | Get total client count | ADMIN |
| GET | `/{clientId}/stats` | Client usage statistics | ADMIN |
| GET | `/{clientId}/active-tokens` | Active tokens for client | ADMIN |

#### CRUD Operations (4 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| PUT | `/{clientId}` | Update client configuration | SUPER_ADMIN |
| DELETE | `/{clientId}` | Delete/deactivate client | SUPER_ADMIN |
| POST | `/{clientId}/status` | Enable/disable client | SUPER_ADMIN |
| POST | `/{clientId}/regenerate-secret` | Regenerate client secret | SUPER_ADMIN |

#### Advanced Management (8 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/{clientId}/scopes` | Get client scopes | ADMIN |
| PUT | `/{clientId}/scopes` | Update client scopes | SUPER_ADMIN |
| GET | `/{clientId}/grant-types` | Get client grant types | ADMIN |
| PUT | `/{clientId}/grant-types` | Update grant types | SUPER_ADMIN |
| GET | `/{clientId}/redirect-uris` | Get redirect URIs | ADMIN |
| PUT | `/{clientId}/redirect-uris` | Update redirect URIs | SUPER_ADMIN |
| GET | `/{clientId}/settings` | Get token/client settings | ADMIN |
| PUT | `/{clientId}/settings` | Update settings | SUPER_ADMIN |

**New endpoints to add: 17**

---

### 8. UserController (Extended) - `/api/user/`

#### List & Search (4 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/list` | Paginated list of users | ADMIN |
| GET | `/search` | Search users by criteria | ADMIN |
| GET | `/count` | Total user count | ADMIN |
| GET | `/{id}` | Get user by ID | ADMIN |

#### CRUD Operations (7 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| PUT | `/{id}` | Update user details | SUPER_ADMIN |
| DELETE | `/{id}` | Delete user (soft delete) | SUPER_ADMIN |
| POST | `/{id}/status` | Enable/disable/lock user | SUPER_ADMIN |
| POST | `/{id}/unlock` | Unlock locked account | SUPER_ADMIN |
| POST | `/{id}/reset-password` | Admin reset password | SUPER_ADMIN |
| POST | `/bulk-import` | Bulk import users | SUPER_ADMIN |
| POST | `/bulk-delete` | Bulk delete users | SUPER_ADMIN |

#### Role Management (3 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/{id}/roles` | Get user roles | ADMIN |
| PUT | `/{id}/roles` | Update user roles | SUPER_ADMIN |
| DELETE | `/{id}/roles/{roleId}` | Remove role from user | SUPER_ADMIN |

#### Account Management (5 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/{id}/expire-credentials` | Force credential expiration | SUPER_ADMIN |
| POST | `/{id}/expire-account` | Expire account | SUPER_ADMIN |
| GET | `/{id}/sessions` | Get active sessions | ADMIN |
| DELETE | `/{id}/sessions` | Terminate all user sessions | SUPER_ADMIN |
| DELETE | `/{id}/sessions/{sessionId}` | Terminate specific session | SUPER_ADMIN |

#### Monitoring (3 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/{id}/login-history` | User login history | ADMIN |
| GET | `/{id}/activity` | User activity log | ADMIN |
| GET | `/{id}/authorizations` | User's authorization history | ADMIN |

**New endpoints to add: 22**

---

### 9. RoleController (Extended) - `/api/role/`

#### List & Search (3 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/{id}` | Get role by ID | ADMIN |
| GET | `/search` | Search roles | ADMIN |
| GET | `/count` | Total role count | ADMIN |

#### CRUD Operations (3 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| PUT | `/{id}` | Update role | SUPER_ADMIN |
| DELETE | `/{id}` | Delete role | SUPER_ADMIN |
| POST | `/bulk-create` | Bulk create roles | SUPER_ADMIN |

#### Permission Management (4 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/{id}/permissions` | Get role permissions | ADMIN |
| PUT | `/{id}/permissions` | Update role permissions | SUPER_ADMIN |
| POST | `/{id}/permissions/{permId}` | Add permission to role | SUPER_ADMIN |
| DELETE | `/{id}/permissions/{permId}` | Remove permission | SUPER_ADMIN |

#### Role Assignment (2 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/{id}/users` | Get users with this role | ADMIN |
| GET | `/{id}/users/count` | Count users with role | ADMIN |

**New endpoints to add: 12**

---

### 10. AuthorizationController (NEW) - `/api/authorization/`

#### Authorization Management (4 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/list` | List all authorizations | ADMIN |
| GET | `/search` | Search authorizations | ADMIN |
| GET | `/{id}` | Get authorization details | ADMIN |
| DELETE | `/{id}` | Revoke authorization | SUPER_ADMIN |

#### Token Management (4 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/tokens/active` | List active tokens | ADMIN |
| GET | `/tokens/expired` | List expired tokens | ADMIN |
| DELETE | `/tokens/{tokenId}` | Revoke specific token | SUPER_ADMIN |
| POST | `/tokens/cleanup` | Manual cleanup expired | SUPER_ADMIN |

#### By User (2 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/user/{username}` | User's authorizations | ADMIN |
| DELETE | `/user/{username}` | Revoke all user auths | SUPER_ADMIN |

#### By Client (2 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/client/{clientId}` | Client's authorizations | ADMIN |
| DELETE | `/client/{clientId}` | Revoke all client auths | SUPER_ADMIN |

#### Statistics (3 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/stats` | Authorization statistics | ADMIN |
| GET | `/stats/by-client` | Stats grouped by client | ADMIN |
| GET | `/stats/by-user` | Stats grouped by user | ADMIN |

**New endpoints: 15**

---

### 11. AuditController (NEW) - `/api/audit/`

#### Audit Logs (5 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/logs` | Get audit logs | ADMIN |
| GET | `/logs/user/{username}` | User-specific logs | ADMIN |
| GET | `/logs/client/{clientId}` | Client-specific logs | ADMIN |
| GET | `/logs/action/{action}` | Filter by action type | ADMIN |
| GET | `/logs/export` | Export audit logs | SUPER_ADMIN |

#### Security Events (4 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/security/failed-logins` | Failed login attempts | ADMIN |
| GET | `/security/suspicious` | Suspicious activities | ADMIN |
| GET | `/security/locked-accounts` | Locked accounts list | ADMIN |
| GET | `/security/password-resets` | Password reset history | ADMIN |

#### Activity Tracking (3 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/activity/recent` | Recent system activity | ADMIN |
| GET | `/activity/by-ip` | Activity by IP address | ADMIN |
| GET | `/activity/by-user` | Activity by user | ADMIN |

**New endpoints: 12**

---

### 12. DashboardController (NEW) - `/api/dashboard/`

#### Overview (2 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/overview` | System overview statistics | ADMIN |
| GET | `/metrics` | Real-time metrics | ADMIN |

#### User Analytics (3 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/users/growth` | User growth over time | ADMIN |
| GET | `/users/active` | Active users count | ADMIN |
| GET | `/users/by-role` | Users grouped by role | ADMIN |

#### Client Analytics (3 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/clients/usage` | Client usage statistics | ADMIN |
| GET | `/clients/active` | Active clients | ADMIN |
| GET | `/clients/top` | Top clients by usage | ADMIN |

#### Token Analytics (3 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/tokens/issued` | Tokens issued over time | ADMIN |
| GET | `/tokens/active` | Active tokens count | ADMIN |
| GET | `/tokens/by-type` | Tokens by grant type | ADMIN |

#### Authentication Analytics (3 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/auth/methods` | Auth methods usage | ADMIN |
| GET | `/auth/success-rate` | Authentication success rate | ADMIN |
| GET | `/auth/failures` | Failed auth attempts | ADMIN |

#### System Health (4 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/health` | System health status | ADMIN |
| GET | `/health/database` | Database health | ADMIN |
| GET | `/health/redis` | Redis health | ADMIN |
| GET | `/health/vault` | Vault health | ADMIN |

**New endpoints: 18**

---

### 13. ConfigController (NEW) - `/api/config/`

#### OTP Configuration (3 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/otp` | Get OTP configuration | ADMIN |
| PUT | `/otp` | Update OTP configuration | SUPER_ADMIN |
| POST | `/otp/test` | Test OTP settings | SUPER_ADMIN |

#### Rate Limiting (3 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/rate-limit` | Get rate limit config | ADMIN |
| PUT | `/rate-limit` | Update rate limits | SUPER_ADMIN |
| GET | `/rate-limit/current` | Current rate limit status | ADMIN |

#### Token Settings (2 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/token-settings` | Get global token settings | ADMIN |
| PUT | `/token-settings` | Update token settings | SUPER_ADMIN |

#### Security Settings (3 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/security` | Get security settings | ADMIN |
| PUT | `/security` | Update security settings | SUPER_ADMIN |
| POST | `/security/reset` | Reset to defaults | SUPER_ADMIN |

#### SMS Provider (3 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/sms-provider` | Get SMS provider config | ADMIN |
| PUT | `/sms-provider` | Update SMS provider | SUPER_ADMIN |
| POST | `/sms-provider/test` | Test SMS sending | SUPER_ADMIN |

#### Vault Configuration (2 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/vault` | Get Vault connection status | ADMIN |
| POST | `/vault/rotate-keys` | Rotate encryption keys | SUPER_ADMIN |

**New endpoints: 16**

---

### 14. NotificationController (NEW) - `/api/notification/`

#### Email Templates (6 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/email/templates` | List email templates | ADMIN |
| GET | `/email/templates/{id}` | Get template | ADMIN |
| POST | `/email/templates` | Create template | SUPER_ADMIN |
| PUT | `/email/templates/{id}` | Update template | SUPER_ADMIN |
| DELETE | `/email/templates/{id}` | Delete template | SUPER_ADMIN |
| POST | `/email/test` | Send test email | SUPER_ADMIN |

#### SMS Templates (5 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/sms/templates` | List SMS templates | ADMIN |
| GET | `/sms/templates/{id}` | Get template | ADMIN |
| POST | `/sms/templates` | Create template | SUPER_ADMIN |
| PUT | `/sms/templates/{id}` | Update template | SUPER_ADMIN |
| DELETE | `/sms/templates/{id}` | Delete template | SUPER_ADMIN |

#### Alert Configuration (4 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/alerts` | Get alert rules | ADMIN |
| POST | `/alerts` | Create alert rule | SUPER_ADMIN |
| PUT | `/alerts/{id}` | Update alert rule | SUPER_ADMIN |
| DELETE | `/alerts/{id}` | Delete alert rule | SUPER_ADMIN |

#### Notification History (3 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/history` | Notification history | ADMIN |
| GET | `/history/failed` | Failed notifications | ADMIN |
| POST | `/history/retry/{id}` | Retry failed notification | SUPER_ADMIN |

**New endpoints: 18**

---

### 15. PermissionController (NEW) - `/api/permission/`

#### Permissions CRUD (5 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/list` | List all permissions | ADMIN |
| GET | `/{id}` | Get permission details | ADMIN |
| POST | `/` | Create permission | SUPER_ADMIN |
| PUT | `/{id}` | Update permission | SUPER_ADMIN |
| DELETE | `/{id}` | Delete permission | SUPER_ADMIN |

#### Permission Resources (2 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/resources` | List resources | ADMIN |
| GET | `/resources/{resource}/actions` | Get resource actions | ADMIN |

#### Role-Permission Mapping (4 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/role/{roleId}` | Get role permissions | ADMIN |
| PUT | `/role/{roleId}` | Set role permissions | SUPER_ADMIN |
| POST | `/role/{roleId}/add` | Add permission to role | SUPER_ADMIN |
| DELETE | `/role/{roleId}/remove` | Remove permission | SUPER_ADMIN |

**New endpoints: 11**

---

### 16. BackupController (NEW) - `/api/backup/`

#### Database Backup (5 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/database/create` | Create database backup | SUPER_ADMIN |
| GET | `/database/list` | List available backups | SUPER_ADMIN |
| GET | `/database/{id}` | Get backup details | SUPER_ADMIN |
| POST | `/database/restore/{id}` | Restore from backup | SUPER_ADMIN |
| DELETE | `/database/{id}` | Delete backup | SUPER_ADMIN |

#### Configuration Backup (3 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/config/export` | Export system config | SUPER_ADMIN |
| POST | `/config/import` | Import system config | SUPER_ADMIN |
| GET | `/config/current` | Get current config | SUPER_ADMIN |

#### Scheduled Backups (3 endpoints)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/schedule` | Get backup schedule | SUPER_ADMIN |
| PUT | `/schedule` | Set backup schedule | SUPER_ADMIN |
| POST | `/schedule/run-now` | Trigger immediate backup | SUPER_ADMIN |

**New endpoints: 11**

---

## Summary Statistics

### Current Implementation
- **Controllers**: 6
- **Endpoints**: 20

### Recommended Additions

#### By Priority

**Phase 1 - Essential (High Priority):**
- ClientController extensions: 17 endpoints
- UserController extensions: 22 endpoints
- RoleController extensions: 12 endpoints
- AuthorizationController: 15 endpoints
- **Subtotal: 66 endpoints**

**Phase 2 - Advanced Management (Medium Priority):**
- AuditController: 12 endpoints
- DashboardController: 18 endpoints
- ConfigController: 16 endpoints
- **Subtotal: 46 endpoints**

**Phase 3 - Enhanced Features (Lower Priority):**
- NotificationController: 18 endpoints
- PermissionController: 11 endpoints
- BackupController: 11 endpoints
- **Subtotal: 40 endpoints**

### Total Summary

| Category | Controllers | Endpoints |
|----------|-------------|-----------|
| **Current** | 6 | 20 |
| **Phase 1** | +4 | +66 |
| **Phase 2** | +3 | +46 |
| **Phase 3** | +3 | +40 |
| **Grand Total** | **16** | **172** |

---

## Quick Reference by HTTP Method

### Current
- GET: 8 endpoints
- POST: 12 endpoints
- PUT: 0 endpoints
- DELETE: 0 endpoints

### Recommended Total
- GET: ~80 endpoints (list, search, view, stats)
- POST: ~40 endpoints (create, action, test)
- PUT: ~30 endpoints (update, configure)
- DELETE: ~22 endpoints (delete, revoke, remove)

---

## Access Control Summary

| Role | Total Accessible Endpoints |
|------|---------------------------|
| **SUPER_ADMIN** | All 172 endpoints |
| **ADMIN** | ~120 endpoints (read + limited write) |
| **USER** | ~10 endpoints (self-management only) |
| **PUBLIC** | 3 endpoints (OTP flow) |

---

This comprehensive API structure will provide a **fully-featured admin panel** capable of managing every aspect of your OAuth2 Authorization Server!
