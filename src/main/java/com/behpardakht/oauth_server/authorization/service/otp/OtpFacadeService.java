package com.behpardakht.oauth_server.authorization.service.otp;

import com.behpardakht.oauth_server.authorization.exception.ExceptionMessages;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.behpardakht.oauth_server.authorization.model.dto.otp.request.InitOtpRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.otp.request.SendOtpRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.otp.request.VerifyOtpRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.otp.response.OtpResponse;
import com.behpardakht.oauth_server.authorization.model.dto.otp.response.VerifyOtpResponseDto;
import com.behpardakht.oauth_server.authorization.service.otp.OtpStorageService.SessionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.maskPhoneNumber;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpFacadeService {

    private final OtpService otpService;
    private final OtpStorageService otpStorageService;
    private final OtpAuthorizationService otpAuthorizationService;

    public void initializeOtpSession(InitOtpRequestDto request) {
        String state = request.getState();
        validateStateNotExists(state);
        otpStorageService.storeOAuth2Parameters(request.getClientId(), state, request.getRedirectUri(),
                request.getCodeChallenge(), request.getCodeChallengeMethod().getValue(), request.getScope());
        log.info("OTP session initialized for client: {} with state: {}", request.getClientId(), state);
    }

    public String sendOtp(SendOtpRequestDto request, String ipAddress) {
        String state = request.getState();
        String phoneNumber = request.getPhoneNumber();
        validateStateExists(state);
        OtpResponse otpResponse = otpService.sendOtp(phoneNumber, ipAddress);
        if (!otpResponse.isSuccess()) {
            log.warn("Failed to send OTP for phone: {}", maskPhoneNumber(phoneNumber));
            throw new CustomException(ExceptionMessages.OTP_SEND_FAILED);
        }
        otpStorageService.storePhoneNumber(state, phoneNumber);
        log.info("OTP sent successfully for phone: {}", maskPhoneNumber(phoneNumber));
        return otpResponse.getMessage();
    }

    public VerifyOtpResponseDto verifyOtpAndCreateAuthorization(VerifyOtpRequestDto request, String ipAddress) {
        String state = request.getState();
        validateStateExists(state);
        String phoneNumber = getPhoneNumberForState(state);
        String maskedPhoneNumber = maskPhoneNumber(phoneNumber);
        validateOtp(phoneNumber, request.getOtp(), ipAddress, maskedPhoneNumber);
        SessionDto sessionDto = getSessionData(state);
        String authorizationCode = generateAuthorizationCode();
        String redirectUrl = createAuthorization(authorizationCode, sessionDto, state, maskedPhoneNumber);
        log.info("Authorization successful for phone: {}, client: {}", maskedPhoneNumber, sessionDto.clientId());
        return VerifyOtpResponseDto.builder().phoneNumber(maskedPhoneNumber).state(state)
                .redirectUri(redirectUrl).authorizationCode(authorizationCode).build();
    }

    private void validateStateNotExists(String state) {
        if (otpStorageService.stateExists(state)) {
            log.warn("Duplicate state parameter detected: {}", state);
            throw new CustomException(ExceptionMessages.INVALID_STATE);
        }
    }

    private void validateStateExists(String state) {
        if (!otpStorageService.stateExists(state)) {
            log.warn("Invalid or expired state: {}", state);
            throw new CustomException(ExceptionMessages.INVALID_OR_EXPIRED_SESSION);
        }
    }

    private String getPhoneNumberForState(String state) {
        String phoneNumber = otpStorageService.getPhoneNumber(state);
        if (phoneNumber == null) {
            log.warn("Phone number not found for state: {}", state);
            throw new CustomException(ExceptionMessages.PHONE_NUMBER_NOT_FOUND);
        }
        return phoneNumber;
    }

    private void validateOtp(String phoneNumber, String otp, String ipAddress, String maskedPhoneNumber) {
        boolean isValid = otpStorageService.validateAndConsumeOtp(phoneNumber, otp, ipAddress);
        if (!isValid) {
            log.warn("OTP validation failed for phone: {}", maskedPhoneNumber);
            throw new CustomException(ExceptionMessages.INVALID_OR_EXPIRED_OTP);
        }
    }

    private SessionDto getSessionData(String state) {
        SessionDto sessionDto = otpStorageService.getSessionDto(state);
        if (sessionDto.clientId() == null) {
            log.error("Client ID not found in session for state: {}", state);
            otpStorageService.markStateAsConsumed(state);
            throw new CustomException(ExceptionMessages.CLIENT_ID_NOT_FOUND);
        }
        return sessionDto;
    }

    private String generateAuthorizationCode() {
        return "auth_code_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String createAuthorization(String authorizationCode, SessionDto sessionDto,
                                       String state, String maskedPhoneNumber) {
        try {
            String redirectUrl = otpAuthorizationService.createAuthorization(authorizationCode, sessionDto);
            otpStorageService.markStateAsConsumed(state);
            return redirectUrl;
        } catch (Exception e) {
            log.error("Failed to create authorization for phone: {}", maskedPhoneNumber, e);
            otpStorageService.markStateAsConsumed(state);
            throw new CustomException(ExceptionMessages.AUTHORIZATION_CREATION_FAILED);
        }
    }
}