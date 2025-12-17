package com.behpardakht.oauth_server.authorization.controller.admin;

import com.behpardakht.oauth_server.authorization.config.bundle.MessageResolver;
import com.behpardakht.oauth_server.authorization.util.Messages;
import com.behpardakht.oauth_server.authorization.model.dto.auth.AuthorizationDto;
import com.behpardakht.oauth_server.authorization.model.dto.auth.AuthorizationFilterDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import com.behpardakht.oauth_server.authorization.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.ADMIN_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = ADMIN_PREFIX + "/sessions/")
public class SessionController {

    private final AuthService authService;

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'session:revoke')")
    @DeleteMapping("revoke/{authorizationId}")
    public ResponseEntity<ResponseDto<String>> revokeSession(@PathVariable String authorizationId) {
        authService.revokeSession(authorizationId);
        String response = MessageResolver.getMessage(
                Messages.SESSION_REVOKED_SUCCESS.getMessage(), new Object[]{authorizationId});
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'session:revoke')")
    @DeleteMapping("revokeByUsername/{username}")
    public ResponseEntity<ResponseDto<String>> revokeSessionsByUsername(@PathVariable String username) {
        String response = authService.revokeSessionsByUsername(username);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'session:read')")
    @PostMapping("findAll")
    public ResponseEntity<ResponseDto<PageableResponseDto<AuthorizationDto>>> findAllSessions(@RequestBody
                                                                                              PageableRequestDto
                                                                                                      <AuthorizationFilterDto>
                                                                                                      request) {
        PageableResponseDto<AuthorizationDto> response = authService.findAllSessions(request);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'session:read')")
    @GetMapping("findByUsername/{username}")
    public ResponseEntity<ResponseDto<List<AuthorizationDto>>> findSessionsByUsername(@PathVariable String username) {
        List<AuthorizationDto> response = authService.findSessionsByUsername(username);
        return ResponseEntity.ok(ResponseDto.success(response));
    }
}