package com.behpardakht.oauth_server.authorization.controller;

import com.behpardakht.oauth_server.authorization.config.bundle.MessageResolver;
import com.behpardakht.oauth_server.authorization.util.Messages;
import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import com.behpardakht.oauth_server.authorization.service.AuthService;
import com.behpardakht.oauth_server.authorization.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.API_PREFIX;
import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.getClientIpAddress;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = API_PREFIX + "/auth/")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("logout")
    public ResponseEntity<ResponseDto<String>> logout(@RequestHeader(value = "Authorization", required = false)
                                                      String authHeader) {
        authService.logout(authHeader);
        String response = MessageResolver.getMessage(Messages.LOGOUT_SUCCESS.getMessage());
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PostMapping("logoutAll")
    public ResponseEntity<ResponseDto<String>> logoutAllDevices(@RequestHeader(value = "Authorization", required = false)
                                                                String authHeader) {
        String response = authService.logoutFromAllDevices(authHeader);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("#oldUsername == authentication.name")
    @PostMapping(path = "changeUsername")
    public ResponseEntity<ResponseDto<String>> changeUsername(@RequestParam String oldUsername,
                                                              @RequestParam String newUsername) {
        userService.changeUsername(oldUsername, newUsername);
        String response = MessageResolver.getMessage(Messages.USERNAME_CHANGED_SUCCESS.getMessage());
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("#username == authentication.name")
    @PostMapping(path = "changePassword")
    public ResponseEntity<ResponseDto<String>> changePassword(@RequestParam String username,
                                                              @RequestParam String oldPassword,
                                                              @RequestParam String newPassword) {
        userService.changePassword(oldPassword, newPassword);
        String response = MessageResolver.getMessage(Messages.PASSWORD_CHANGED_SUCCESS.getMessage());
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PostMapping(path = "forgotPassword")
    public ResponseEntity<ResponseDto<String>> forgotPassword(@RequestParam String phoneNumber,
                                                              HttpServletRequest httpRequest) {
        String response = userService.forgotPassword(phoneNumber, getClientIpAddress(httpRequest));
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PostMapping(path = "setNewPassword")
    public ResponseEntity<ResponseDto<String>> setNewPassword(@RequestParam String phoneNumber,
                                                              @RequestParam String otp,
                                                              @RequestParam String newPassword,
                                                              HttpServletRequest httpRequest) {
        userService.setNewPassword(phoneNumber, otp, newPassword, getClientIpAddress(httpRequest));
        String response = MessageResolver.getMessage(Messages.PASSWORD_CHANGED_SUCCESS.getMessage());
        return ResponseEntity.ok(ResponseDto.success(response));
    }
}