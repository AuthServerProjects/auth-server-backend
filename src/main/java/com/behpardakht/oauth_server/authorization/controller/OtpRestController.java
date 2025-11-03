package com.behpardakht.oauth_server.authorization.controller;

import com.behpardakht.oauth_server.authorization.model.dto.ResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.otp.request.InitOtpRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.otp.request.SendOtpRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.otp.request.VerifyOtpRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.otp.response.OtpResponse;
import com.behpardakht.oauth_server.authorization.model.dto.otp.response.VerifyOtpResponseDto;
import com.behpardakht.oauth_server.authorization.service.otp.OtpAuthorizationService;
import com.behpardakht.oauth_server.authorization.service.otp.OtpService;
import com.behpardakht.oauth_server.authorization.service.otp.OtpStorageService;
import com.behpardakht.oauth_server.authorization.service.otp.OtpStorageService.SessionDto;
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
    private final OtpStorageService otpStorageService;
    private final OtpAuthorizationService otpAuthorizationService;

    @PostMapping("initOtp")
    public ResponseEntity<ResponseDto<String>> initOtpSession(@RequestBody @Valid InitOtpRequestDto request) {
        otpStorageService.storeOAuth2Parameters(request.getClientId(), request.getState(), request.getRedirectUri(),
                request.getCodeChallenge(), request.getCodeChallengeMethod().getValue(), request.getScope());
        log.info("OTP session initialized for client: {}", request.getClientId());
        return ResponseEntity.ok(ResponseDto.success(null));
    }

    @PostMapping("sendOtp")
    public ResponseEntity<ResponseDto<String>> sendOtp(@RequestBody @Valid SendOtpRequestDto request) {
        String phoneNumber = request.getPhoneNumber();
        OtpResponse otpResponse = otpService.sendOtp(phoneNumber);
        if (otpResponse.isSuccess()) {
            otpStorageService.storePhoneNumber(request.getState(), phoneNumber);
            return ResponseEntity.ok(ResponseDto.success(otpResponse.getMessage()));
        } else {
            return ResponseEntity.badRequest().body(ResponseDto.failed(otpResponse.getMessage(), maskPhoneNumber(phoneNumber)));
        }
    }

    @PostMapping("verifyOtp")
    public ResponseEntity<ResponseDto<VerifyOtpResponseDto>> verifyOtp(@RequestBody @Valid VerifyOtpRequestDto request) {
        String state = request.getState();
        String phoneNumber = otpStorageService.getPhoneNumber(state);
        String maskedPhoneNumber = maskPhoneNumber(phoneNumber);
        VerifyOtpResponseDto.VerifyOtpResponseDtoBuilder responseBuilder =
                VerifyOtpResponseDto.builder().phoneNumber(phoneNumber);
        boolean isValid = otpStorageService.validateAndConsumeOtp(phoneNumber, request.getOtp());
        if (isValid) {
            SessionDto sessionDto = otpStorageService.getSessionDto(state);
            if (sessionDto.clientId() == null) {
                return ResponseEntity.badRequest().body(
                        ResponseDto.failed("Client Id not Found", responseBuilder.build()));
            }
            String authorizationCode = "auth_code_" + UUID.randomUUID().toString().replace("-", "");
            String redirectUrl = otpAuthorizationService.createAuthorization(authorizationCode, sessionDto);
            otpStorageService.storeAuthCode(authorizationCode, sessionDto.phoneNumber());
            otpStorageService.removePhoneNumberByAuthSessionId(state);
            return ResponseEntity.ok(ResponseDto.success(responseBuilder
                    .state(state)
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