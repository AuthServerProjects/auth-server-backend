package com.behpardakht.oauth_server.authorization.controller;

import com.behpardakht.oauth_server.authorization.model.dto.ResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.otp.*;
import com.behpardakht.oauth_server.authorization.service.otp.OtpAuthorizationService;
import com.behpardakht.oauth_server.authorization.service.otp.OtpService;
import com.behpardakht.oauth_server.authorization.service.otp.OtpSessionService;
import com.behpardakht.oauth_server.authorization.service.otp.OtpSessionService.SessionDto;
import com.behpardakht.oauth_server.authorization.service.otp.OtpStorageService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.maskPhoneNumber;

@Slf4j
@RestController
@RequestMapping("/api/otp/")
@AllArgsConstructor
public class OtpRestController {

    private final OtpService otpService;
    private final OtpSessionService otpSessionService;
    private final OtpStorageService otpStorageService;
    private final OtpAuthorizationService otpAuthorizationService;

    @PostMapping("initOtp")
    public ResponseEntity<ResponseDto<String>> initOtpSession(@RequestBody @Valid InitOtpRequestDto request,
                                                              HttpSession session) {
        otpSessionService.storeOAuth2Parameters(
                session, request.getClientId(), request.getState(), request.getRedirectUri(),
                request.getCodeChallenge(), request.getCodeChallengeMethod().getValue(), request.getScope());
        log.info("OTP session initialized for client: {}", request.getClientId());
        return ResponseEntity.ok(ResponseDto.success(session.getId()));
    }

    @PostMapping("sendOtp")
    public ResponseEntity<ResponseDto<String>> sendOtp(@RequestBody @Valid SendOtpRequestDto request,
                                                       HttpSession session) {
        String phoneNumber = request.getPhoneNumber();
        OtpResponse otpResponse = otpService.sendOtp(phoneNumber);
        String maskedPhoneNumber = maskPhoneNumber(phoneNumber);
        if (otpResponse.isSuccess()) {
            String authSessionId = UUID.randomUUID().toString();
            otpSessionService.storePhoneNumberAndAuthSessionId(authSessionId, phoneNumber, session);
            otpStorageService.storeAuthSessionId(authSessionId, phoneNumber, 10);
            return ResponseEntity.ok(ResponseDto.success(maskedPhoneNumber));
        } else {
            return ResponseEntity.badRequest().body(ResponseDto.failed(otpResponse.getMessage(), maskedPhoneNumber));
        }
    }

    @PostMapping("verifyOtp")
    public ResponseEntity<ResponseDto<VerifyOtpResponseDto>> verifyOtp(@RequestBody @Valid VerifyOtpRequestDto request,
                                                                       HttpSession session) {
        SessionValidationDto sessionValidation = otpSessionService.validatePhoneNumberAndAuthSessionId(session);
        String phoneNumber = sessionValidation.getPhoneNumber();
        String maskedPhoneNumber = maskPhoneNumber(phoneNumber);
        VerifyOtpResponseDto.VerifyOtpResponseDtoBuilder responseBuilder =
                VerifyOtpResponseDto.builder().phoneNumber(phoneNumber);
        if (!sessionValidation.isValid()) {
            return ResponseEntity.badRequest().body(
                    ResponseDto.failed(sessionValidation.getErrorMessage(), responseBuilder.build()));
        }
        boolean isValid = otpStorageService.validateAndConsumeOtp(phoneNumber, request.getOtp());
        if (isValid) {
            SessionDto sessionDto = otpSessionService.getSessionDto(session);
            if (sessionDto.clientId() == null) {
                return ResponseEntity.badRequest().body(
                        ResponseDto.failed("Client Id not Found", responseBuilder.build()));
            }
            String authorizationCode = "auth_code_" + UUID.randomUUID().toString().replace("-", "");
            String redirectUrl = otpAuthorizationService.createAuthorization(authorizationCode, sessionDto);
            otpStorageService.storeAuthCode(authorizationCode, sessionDto.phoneNumber(), 5);
            otpStorageService.removeAuthSessionId(sessionDto.authSessionId());
            otpSessionService.removePhoneNumberAndAuthSessionId(session);
            return ResponseEntity.ok(ResponseDto.success(responseBuilder
                    .state(sessionDto.state())
                    .redirectUri(redirectUrl)
                    .authorizationCode(authorizationCode)
                    .build()));
        } else {
            log.warn("OTP validation failed for phone: {}", maskedPhoneNumber);
            return ResponseEntity.badRequest().body(
                    ResponseDto.failed("Invalid or expired OTP", responseBuilder.build()));
        }
    }
}