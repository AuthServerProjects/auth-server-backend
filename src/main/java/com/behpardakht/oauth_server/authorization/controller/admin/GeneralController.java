package com.behpardakht.oauth_server.authorization.controller.admin;

import com.behpardakht.oauth_server.authorization.model.dto.PermissionDto;
import com.behpardakht.oauth_server.authorization.model.dto.RoleDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import com.behpardakht.oauth_server.authorization.model.enums.*;
import com.behpardakht.oauth_server.authorization.service.GeneralService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.ADMIN_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = ADMIN_PREFIX + "/general/")
public class GeneralController {

    private final GeneralService generalService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "loadAuthenticationMethodType")
    public ResponseEntity<ResponseDto<List<AuthenticationMethodTypes>>> loadAuthenticationMethodType() {
        List<AuthenticationMethodTypes> response = generalService.loadAuthenticationMethodType();
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "loadAuthorizationGrantType")
    public ResponseEntity<ResponseDto<List<AuthorizationGrantTypes>>> loadAuthorizationGrantType() {
        List<AuthorizationGrantTypes> response = generalService.loadAuthorizationGrantType();
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "loadScopeType")
    public ResponseEntity<ResponseDto<List<ScopeTypes>>> loadScopeType() {
        List<ScopeTypes> response = generalService.loadScopeType();
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "loadPkceMethod")
    public ResponseEntity<ResponseDto<List<PkceMethod>>> loadPkceMethod() {
        List<PkceMethod> response = generalService.loadPkceMethod();
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "loadUserRoles")
    public ResponseEntity<ResponseDto<List<RoleDto>>> loadUserRoles() {
        List<RoleDto> response = generalService.loadUserRoles();
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "loadUserPermissions")
    public ResponseEntity<ResponseDto<List<PermissionDto>>> loadUserPermissions() {
        List<PermissionDto> response = generalService.loadUserPermissions();
        return ResponseEntity.ok(ResponseDto.success(response));
    }
}