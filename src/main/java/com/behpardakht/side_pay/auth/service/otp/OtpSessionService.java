package com.behpardakht.side_pay.auth.service.otp;

import com.behpardakht.side_pay.auth.model.dto.otp.SessionValidationDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.behpardakht.side_pay.auth.util.GeneralUtil.maskPhoneNumber;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpSessionService {

    private final OtpStorageService otpStorageService;

    public void storeOAuth2Parameters(HttpSession session, String clientId, String state,
                                      String redirectUri, String codeChallenge,
                                      String codeChallengeMethod, String scope) {
        session.setAttribute("client_id", clientId);
        session.setAttribute("state", state);
        session.setAttribute("redirect_uri", redirectUri);
        session.setAttribute("code_challenge", codeChallenge);
        session.setAttribute("code_challenge_method", codeChallengeMethod);
        session.setAttribute("scope", scope);
    }

    public void storePhoneNumberAndAuthSessionId(String authSessionId, String phoneNumber, HttpSession session) {
        session.setAttribute("phoneNumber", phoneNumber);
        session.setAttribute("authSessionId", authSessionId);
    }

    public SessionValidationDto validatePhoneNumberAndAuthSessionId(HttpSession session) {
        SessionDto sessionDto = getSessionDto(session);
        if (sessionDto.phoneNumber() == null || sessionDto.authSessionId() == null) {
            log.warn("OTP verification attempted without valid session");
            return SessionValidationDto.failure("Session expired. Please start again.");
        }

        String storedPhoneNumber = otpStorageService.getPhoneNumberByAuthSessionId(sessionDto.authSessionId());
        if (!sessionDto.phoneNumber().equals(storedPhoneNumber)) {
            log.warn("Session integrity check failed for phone: {}", maskPhoneNumber(sessionDto.phoneNumber()));
            return SessionValidationDto.failure("Session invalid. Please start again.");
        }
        return SessionValidationDto.success(sessionDto.phoneNumber());
    }

    public void removePhoneNumberAndAuthSessionId(HttpSession session) {
        session.removeAttribute("phoneNumber");
        session.removeAttribute("authSessionId");
    }

    public SessionDto getSessionDto(HttpSession session) {
        return new SessionDto(
                (String) session.getAttribute("client_id"),
                (String) session.getAttribute("state"),
                (String) session.getAttribute("redirect_uri"),
                (String) session.getAttribute("code_challenge"),
                (String) session.getAttribute("code_challenge_method"),
                (String) session.getAttribute("scope"),
                (String) session.getAttribute("phoneNumber"),
                (String) session.getAttribute("authSessionId")
        );
    }

    public record SessionDto(String clientId, String state, String redirectUri, String codeChallenge,
                             String codeChallengeMethod, String scope, String phoneNumber, String authSessionId
    ) {
    }
}