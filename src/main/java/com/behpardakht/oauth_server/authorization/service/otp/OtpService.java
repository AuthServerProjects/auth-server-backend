package com.behpardakht.oauth_server.authorization.service.otp;

import com.behpardakht.oauth_server.authorization.aspect.Auditable;
import com.behpardakht.oauth_server.authorization.config.bundle.MessageResolver;
import com.behpardakht.oauth_server.authorization.exception.ExceptionMessage;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.behpardakht.oauth_server.authorization.model.dto.otp.*;
import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import com.behpardakht.oauth_server.authorization.service.MetricsService;
import com.behpardakht.oauth_server.authorization.service.otp.OtpStorageService.SessionDto;
import com.behpardakht.oauth_server.authorization.service.user.AdminUserService;
import com.behpardakht.oauth_server.authorization.sms.ISmsService;
import com.behpardakht.oauth_server.authorization.util.Messages;
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

    private final MetricsService metricsService;
    private final AdminUserService adminUserService;
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
            throw new CustomException(ExceptionMessage.INVALID_STATE);
        }
    }

    @Auditable(action = AuditAction.OTP_SENT, username = "#request.phoneNumber")
    public String sendOtp(SendOtpRequestDto request, String ipAddress) {
        String state = request.getState();
        String phoneNumber = request.getPhoneNumber();
        validateStateExists(state);
        OtpResponse otpResponse = sendOtp(phoneNumber, ipAddress);
        if (!otpResponse.isSuccess()) {
            log.warn("Failed to send OTP for phone: {}", maskPhoneNumber(phoneNumber));
            return otpResponse.getMessage();
        }
        otpStorageService.storePhoneNumber(state, phoneNumber);
        log.info("OTP sent successfully for phone: {}", maskPhoneNumber(phoneNumber));
        return otpResponse.getMessage();
    }

    public OtpResponse sendOtp(String phoneNumber, String ipAddress) {
        String maskedPhoneNumber = maskPhoneNumber(phoneNumber);
        try {
            if (otpStorageService.isGlobalRateLimited()) {
                metricsService.incrementRateLimitHit("Global");
                log.warn("Global rate limit exceeded");
                return OtpResponse.rateLimited(
                        MessageResolver.getMessage(Messages.SYSTEM_BUSY.getMessage()));
            }
            if (otpStorageService.isIpRateLimited(ipAddress)) {
                metricsService.incrementRateLimitHit("ip");
                log.warn("Rate limit exceeded for IP: {}", ipAddress);
                return OtpResponse.rateLimited(
                        MessageResolver.getMessage(Messages.RATE_LIMIT_IP.getMessage()));
            }
            if (otpStorageService.isPhoneNumberRateLimited(phoneNumber)) {
                metricsService.incrementRateLimitHit("phoneNumber");
                log.warn("Rate limit exceeded for phone: {}", maskedPhoneNumber);
                return OtpResponse.rateLimited(
                        MessageResolver.getMessage(Messages.RATE_LIMIT_PHONE.getMessage()));
            }
            if (otpStorageService.hasValidOtp(phoneNumber)) {
                log.info("Valid OTP already exists for phone: {}", maskedPhoneNumber);
                return OtpResponse.alreadySent(
                        MessageResolver.getMessage(Messages.OTP_ALREADY_SENT.getMessage()));
            }
            if (!adminUserService.existUserWithPhoneNumber(phoneNumber)) {
                adminUserService.createUserByPhoneNumber(phoneNumber);
            }
            String otp = String.valueOf(100000 + SECURE_RANDOM.get().nextInt(900000));
            sendSms(phoneNumber, otp);
            otpStorageService.storeOtp(phoneNumber, otp, ipAddress);
            log.info("OTP generated and sent successfully to: {}", maskedPhoneNumber);
            return OtpResponse.success(
                    MessageResolver.getMessage(Messages.OTP_SENT_SUCCESS.getMessage(),
                            new Object[]{maskedPhoneNumber}));
        } catch (Exception e) {
            log.error("Failed to generate OTP for phone: {}", maskedPhoneNumber, e);
            return OtpResponse.error(
                    MessageResolver.getMessage(Messages.OTP_SEND_FAILED.getMessage()));
        }
    }

    public void sendSms(String phoneNumber, String otp) {
        try {
            iSmsService.send(phoneNumber, otp);
            log.info("OTP SMS sent successfully. To: {}", maskPhoneNumber(phoneNumber));
        } catch (Exception e) {
            log.error("Failed to send OTP SMS to {}: {}", maskPhoneNumber(phoneNumber), e.getMessage(), e);
            throw new CustomException(ExceptionMessage.OTP_SEND_FAILED, e);
        }
    }

    @Auditable(action = AuditAction.OTP_VERIFIED, username = "#request.phoneNumber")
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
            throw new CustomException(ExceptionMessage.INVALID_OR_EXPIRED_SESSION);
        }
    }

    private String getPhoneNumberForState(String state) {
        String phoneNumber = otpStorageService.getPhoneNumber(state);
        if (phoneNumber == null) {
            log.warn("Phone number not found for state: {}", state);
            throw new CustomException(ExceptionMessage.PHONE_NUMBER_NOT_FOUND);
        }
        return phoneNumber;
    }

    private void validateOtp(String phoneNumber, String otp, String ipAddress, String maskedPhoneNumber) {
        boolean isValid = otpStorageService.validateAndConsumeOtp(phoneNumber, otp, ipAddress);
        if (!isValid) {
            log.warn("OTP validation failed for phone: {}", maskedPhoneNumber);
            throw new CustomException(ExceptionMessage.INVALID_OR_EXPIRED_OTP);
        }
    }

    private SessionDto getSessionData(String state) {
        SessionDto sessionDto = otpStorageService.getSessionDto(state);
        if (sessionDto.clientId() == null) {
            log.error("Client ID not found in session for state: {}", state);
            otpStorageService.markStateAsConsumed(state);
            throw new CustomException(ExceptionMessage.CLIENT_ID_NOT_FOUND);
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
            throw new CustomException(ExceptionMessage.AUTHORIZATION_CREATION_FAILED);
        }
    }
}