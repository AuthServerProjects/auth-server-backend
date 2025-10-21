package com.behpardakht.side_pay.auth.service.otp;

import com.behpardakht.side_pay.auth.model.dto.otp.SessionValidationDto;
import jakarta.servlet.http.HttpSession;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.behpardakht.side_pay.auth.util.GeneralUtil.maskPhoneNumber;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpSessionService {

    private final OtpStorageService otpStorageService;

    public void storeClientIdAndState(HttpSession session, String client_id, String state) {
        session.setAttribute("client_id", client_id);
        session.setAttribute("state", state);
    }

    public void storePhoneNumberAndSessionId(String authSessionId, String phoneNumber, HttpSession session) {
        session.setAttribute("phoneNumber", phoneNumber);
        session.setAttribute("authSessionId", authSessionId);
    }

    public void removePhoneNumberAndSessionId(HttpSession session) {
        session.removeAttribute("phoneNumber");
        session.removeAttribute("authSessionId");
    }

    public SessionDto getSessionDto(HttpSession session) {
        return SessionDto.builder()
                .clientId((String) session.getAttribute("client_id"))
                .state((String) session.getAttribute("state"))
                .phoneNumber((String) session.getAttribute("phoneNumber"))
                .authSessionId((String) session.getAttribute("authSessionId"))
                .build();
    }

    public SessionValidationDto validateOtpRequestData(HttpSession session) {
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

    @Builder
    public record SessionDto(String clientId, String state, String phoneNumber, String authSessionId) {
    }
}