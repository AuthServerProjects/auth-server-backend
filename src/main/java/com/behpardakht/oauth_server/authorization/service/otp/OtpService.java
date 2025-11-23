package com.behpardakht.oauth_server.authorization.service.otp;

import com.behpardakht.oauth_server.authorization.exception.ExceptionMessages;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.behpardakht.oauth_server.authorization.model.dto.otp.request.InitOtpRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.otp.request.SendOtpRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.otp.request.VerifyOtpRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.otp.response.OtpResponse;
import com.behpardakht.oauth_server.authorization.model.dto.otp.response.VerifyOtpResponseDto;
import com.behpardakht.oauth_server.authorization.service.UserService;
import com.behpardakht.oauth_server.authorization.service.otp.OtpStorageService.SessionDto;
import com.behpardakht.oauth_server.authorization.sms.ISmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.maskPhoneNumber;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final UserService userService;
    private final ISmsService iSmsService;
    private final OtpStorageService otpStorageService;
    private final OtpAuthorizationService otpAuthorizationService;

    private static final ThreadLocal<SecureRandom> SECURE_RANDOM = ThreadLocal.withInitial(() -> {
        try {
            return SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            log.warn("Strong SecureRandom not available, using default", e);
            return new SecureRandom();
        }
    });

    public void initializeOtpSession(InitOtpRequestDto request) {
        String state = request.getState();
        validateStateNotExists(state);
        otpStorageService.storeOAuth2Parameters(request.getClientId(), state, request.getRedirectUri(),
                request.getCodeChallenge(), request.getCodeChallengeMethod().getValue(), request.getScope());
        log.info("OTP session initialized for client: {} with state: {}", request.getClientId(), state);
    }

    private void validateStateNotExists(String state) {
        if (otpStorageService.stateExists(state)) {
            log.warn("Duplicate state parameter detected: {}", state);
            throw new CustomException(ExceptionMessages.INVALID_STATE);
        }
    }

    public String sendOtp(SendOtpRequestDto request, String ipAddress) {
        String state = request.getState();
        String phoneNumber = request.getPhoneNumber();
        validateStateExists(state);
        OtpResponse otpResponse = sendOtp(phoneNumber, ipAddress);
        if (!otpResponse.isSuccess()) {
            log.warn("Failed to send OTP for phone: {}", maskPhoneNumber(phoneNumber));
            throw new CustomException(ExceptionMessages.OTP_SEND_FAILED);
        }
        otpStorageService.storePhoneNumber(state, phoneNumber);
        log.info("OTP sent successfully for phone: {}", maskPhoneNumber(phoneNumber));
        return otpResponse.getMessage();
    }

    public OtpResponse sendOtp(String phoneNumber, String ipAddress) {
        try {
            if (otpStorageService.isGlobalRateLimited()) {
                log.warn("Global rate limit exceeded");
                return OtpResponse.rateLimited("System is busy. Please try again later.");
            }
            if (otpStorageService.isIpRateLimited(ipAddress)) {
                log.warn("Rate limit exceeded for IP: {}", ipAddress);
                return OtpResponse.rateLimited("Too many requests from your network. Please try again later.");
            }
            if (otpStorageService.isPhoneNumberRateLimited(phoneNumber)) {
                log.warn("Rate limit exceeded for phone: {}", maskPhoneNumber(phoneNumber));
                return OtpResponse.rateLimited("Too many requests. Please try again later.");
            }
            if (otpStorageService.hasValidOtp(phoneNumber)) {
                log.info("Valid OTP already exists for phone: {}", maskPhoneNumber(phoneNumber));
                return OtpResponse.alreadySent("OTP already sent. Please check your messages.");
            }
            if (!userService.existUserWithUsername(phoneNumber)) {
                userService.createUserByPhoneNumber(phoneNumber);
            }
            String otp = String.valueOf(100000 + SECURE_RANDOM.get().nextInt(900000));
            sendSms(phoneNumber, otp);
            otpStorageService.storeOtp(phoneNumber, otp, ipAddress);
            log.info("OTP generated and sent successfully to: {}", maskPhoneNumber(phoneNumber));
            return OtpResponse.success("OTP sent successfully to " + maskPhoneNumber(phoneNumber));
        } catch (Exception e) {
            log.error("Failed to generate OTP for phone: {}", maskPhoneNumber(phoneNumber), e);
            return OtpResponse.error("Failed to send OTP. Please try again.");
        }
    }

    public void sendSms(String phoneNumber, String otp) {
        try {
            iSmsService.send(phoneNumber, otp);
            log.info("OTP SMS sent successfully. To: {}", maskPhoneNumber(phoneNumber));
        } catch (Exception e) {
            log.error("Failed to send OTP SMS to {}: {}", maskPhoneNumber(phoneNumber), e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP SMS", e);
        }
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