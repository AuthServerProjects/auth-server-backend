package com.behpardakht.oauth_server.authorization.controller;

import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import com.behpardakht.oauth_server.authorization.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.API_PREFIX;

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

    @PostMapping("logoutAll")
    public ResponseEntity<ResponseDto<String>> logoutAllDevices(@RequestHeader(value = "Authorization", required = false)
                                                                String authHeader) {
        String response = authService.logoutFromAllDevices(authHeader);
        return ResponseEntity.ok(ResponseDto.success(response));
    }
}