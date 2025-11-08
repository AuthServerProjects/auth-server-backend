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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.API_PREFIX;
import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.maskPhoneNumber;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = API_PREFIX + "/api/otp/")
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
    public ResponseEntity<ResponseDto<String>> sendOtp(@RequestBody @Valid SendOtpRequestDto request,
                                                       HttpServletRequest httpRequest) {
        String phoneNumber = request.getPhoneNumber();
        String ipAddress = getClientIpAddress(httpRequest);
        OtpResponse otpResponse = otpService.sendOtp(phoneNumber, ipAddress);
        if (otpResponse.isSuccess()) {
            otpStorageService.storePhoneNumber(request.getState(), phoneNumber);
            return ResponseEntity.ok(ResponseDto.success(otpResponse.getMessage()));
        } else {
            return ResponseEntity.badRequest().body(ResponseDto.failed(otpResponse.getMessage(), maskPhoneNumber(phoneNumber)));
        }
    }

    @PostMapping("verifyOtp")
    public ResponseEntity<ResponseDto<VerifyOtpResponseDto>> verifyOtp(@RequestBody @Valid VerifyOtpRequestDto request,
                                                                       HttpServletRequest httpRequest) {
        String state = request.getState();
        String phoneNumber = otpStorageService.getPhoneNumber(state);
        String maskedPhoneNumber = maskPhoneNumber(phoneNumber);
        VerifyOtpResponseDto.VerifyOtpResponseDtoBuilder responseBuilder =
                VerifyOtpResponseDto.builder().phoneNumber(phoneNumber);
        String ipAddress = getClientIpAddress(httpRequest);
        boolean isValid = otpStorageService.validateAndConsumeOtp(phoneNumber, request.getOtp(), ipAddress);
        if (isValid) {
            SessionDto sessionDto = otpStorageService.getSessionDto(state);
            if (sessionDto.clientId() == null) {
                return ResponseEntity.badRequest().body(
                        ResponseDto.failed("Client Id not Found", responseBuilder.build()));
            }
            String authorizationCode = "auth_code_" + UUID.randomUUID().toString().replace("-", "");
            String redirectUrl = otpAuthorizationService.createAuthorization(authorizationCode, sessionDto);
            otpStorageService.removePhoneNumberByState(state);
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

    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }
}