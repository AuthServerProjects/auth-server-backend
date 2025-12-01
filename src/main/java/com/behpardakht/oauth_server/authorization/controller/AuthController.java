package com.behpardakht.oauth_server.authorization.controller;

import com.behpardakht.oauth_server.authorization.model.dto.auth.AuthorizationDto;
import com.behpardakht.oauth_server.authorization.model.dto.auth.AuthorizationFilterDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import com.behpardakht.oauth_server.authorization.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.API_PREFIX;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = API_PREFIX + "/auth/")
public class AuthController {

    private final AuthService authService;

    @PostMapping("logout")
    public ResponseEntity<ResponseDto<?>> logout(@RequestHeader(value = "Authorization", required = false)
                                                 String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.ok(ResponseDto.success(null));
    }

    @PostMapping("logout-all")
    public ResponseEntity<ResponseDto<String>> logoutAllDevices(@RequestHeader(value = "Authorization", required = false)
                                                                String authHeader) {
        String message = authService.logoutFromAllDevices(authHeader);
        return ResponseEntity.ok(ResponseDto.success(message));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("sessions/revoke/{authorizationId}")
    public ResponseEntity<ResponseDto<String>> revokeSession(@PathVariable String authorizationId) {
        authService.revokeSession(authorizationId);
        return ResponseEntity.ok(ResponseDto.success("Session revoked successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("sessions/findAll")
    public ResponseEntity<ResponseDto<PageableResponseDto<AuthorizationDto>>> findAllSessions(@RequestBody
                                                                                              PageableRequestDto
                                                                                                      <AuthorizationFilterDto>
                                                                                                      request) {
        PageableResponseDto<AuthorizationDto> sessions = authService.findAllSessions(request);
        return ResponseEntity.ok(ResponseDto.success(sessions));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("sessions/findByUsername/{username}")
    public ResponseEntity<ResponseDto<List<AuthorizationDto>>> findSessionsByUsername(@PathVariable String username) {
        List<AuthorizationDto> sessions = authService.findSessionsByUsername(username);
        return ResponseEntity.ok(ResponseDto.success(sessions));
    }
}