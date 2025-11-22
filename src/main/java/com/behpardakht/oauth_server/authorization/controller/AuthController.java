package com.behpardakht.oauth_server.authorization.controller;

import com.behpardakht.oauth_server.authorization.model.dto.ResponseDto;
import com.behpardakht.oauth_server.authorization.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}