package com.behpardakht.oauth_server.authorization.controller;

import com.behpardakht.oauth_server.authorization.exception.ExceptionMessages;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
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

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.*;

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
        String state = request.getState();
        if (otpStorageService.stateExists(state)) {
            log.warn("Duplicate state parameter detected in initOtp: {}", state);
            throw new CustomException(ExceptionMessages.INVALID_STATE);
        }
        otpStorageService.storeOAuth2Parameters(request.getClientId(), state, request.getRedirectUri(),
                request.getCodeChallenge(), request.getCodeChallengeMethod().getValue(), request.getScope()
        );
        log.info("OTP session initialized for client: {} with state: {}",
                request.getClientId(), state);
        return ResponseEntity.ok(ResponseDto.success("Session initialized successfully"));
    }

    @PostMapping("sendOtp")
    public ResponseEntity<ResponseDto<String>> sendOtp(@RequestBody @Valid SendOtpRequestDto request,
                                                       HttpServletRequest httpRequest) {
        String phoneNumber = request.getPhoneNumber();
        String state = request.getState();
        if (!otpStorageService.stateExists(state)) {
            log.warn("Invalid or expired state in sendOtp: {}", state);
            throw new CustomException(ExceptionMessages.INVALID_OR_EXPIRED_SESSION);
        }
        String ipAddress = getClientIpAddress(httpRequest);
        OtpResponse otpResponse = otpService.sendOtp(phoneNumber, ipAddress);
        if (otpResponse.isSuccess()) {
            otpStorageService.storePhoneNumber(state, phoneNumber);
            log.info("OTP sent successfully for phone: {}", maskPhoneNumber(phoneNumber));
            return ResponseEntity.ok(ResponseDto.success(otpResponse.getMessage()));
        } else {
            throw new CustomException(ExceptionMessages.OTP_SEND_FAILED);
        }
    }

    @PostMapping("verifyOtp")
    public ResponseEntity<ResponseDto<VerifyOtpResponseDto>> verifyOtp(@RequestBody @Valid VerifyOtpRequestDto request,
                                                                       HttpServletRequest httpRequest) {
        String state = request.getState();
        if (!otpStorageService.stateExists(state)) {
            log.warn("Invalid or expired state in verifyOtp: {}", state);
            throw new CustomException(ExceptionMessages.INVALID_OR_EXPIRED_SESSION);
        }
        String phoneNumber = otpStorageService.getPhoneNumber(state);
        if (phoneNumber == null) {
            log.warn("Phone number not found for state: {}", state);
            throw new CustomException(ExceptionMessages.PHONE_NUMBER_NOT_FOUND);
        }
        String maskedPhoneNumber = maskPhoneNumber(phoneNumber);
        VerifyOtpResponseDto.VerifyOtpResponseDtoBuilder responseBuilder =
                VerifyOtpResponseDto.builder().phoneNumber(maskedPhoneNumber);
        String ipAddress = getClientIpAddress(httpRequest);
        boolean isValid = otpStorageService.validateAndConsumeOtp(phoneNumber, request.getOtp(), ipAddress);
        if (isValid) {
            SessionDto sessionDto = otpStorageService.getSessionDto(state);
            if (sessionDto.clientId() == null) {
                log.error("Client ID not found in session for state: {}", state);
                otpStorageService.markStateAsConsumed(state);
                throw new CustomException(ExceptionMessages.CLIENT_ID_NOT_FOUND);
            }
            String authorizationCode = "auth_code_" + UUID.randomUUID().toString().replace("-", "");
            try {
                String redirectUrl = otpAuthorizationService.createAuthorization(authorizationCode, sessionDto);
                otpStorageService.markStateAsConsumed(state);
                log.info("Authorization successful for phone: {}, client: {}",
                        maskedPhoneNumber, sessionDto.clientId());
                return ResponseEntity.ok(ResponseDto.success(responseBuilder
                        .state(state)
                        .redirectUri(redirectUrl)
                        .authorizationCode(authorizationCode)
                        .build()));
            } catch (Exception e) {
                log.error("Failed to create authorization for phone: {}", maskedPhoneNumber, e);
                otpStorageService.markStateAsConsumed(state);
                throw new CustomException(ExceptionMessages.AUTHORIZATION_CREATION_FAILED);
            }
        } else {
            log.warn("OTP validation failed for phone: {}", maskedPhoneNumber);
            throw new CustomException(ExceptionMessages.INVALID_OR_EXPIRED_OTP);
        }
    }
}