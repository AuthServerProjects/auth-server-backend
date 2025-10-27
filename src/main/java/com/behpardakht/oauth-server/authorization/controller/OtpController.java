package com.behpardakht.side_pay.auth.controller;

import com.behpardakht.side_pay.auth.model.dto.otp.OtpResponse;
import com.behpardakht.side_pay.auth.model.dto.otp.SendOtpRequestDto;
import com.behpardakht.side_pay.auth.model.dto.otp.SessionValidationDto;
import com.behpardakht.side_pay.auth.model.dto.otp.VerifyOtpRequestDto;
import com.behpardakht.side_pay.auth.service.otp.OtpAuthorizationService;
import com.behpardakht.side_pay.auth.service.otp.OtpService;
import com.behpardakht.side_pay.auth.service.otp.OtpSessionService;
import com.behpardakht.side_pay.auth.service.otp.OtpSessionService.SessionDto;
import com.behpardakht.side_pay.auth.service.otp.OtpStorageService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.behpardakht.side_pay.auth.util.GeneralUtil.maskPhoneNumber;

@Slf4j
@Controller
@RequestMapping("/otp/")
@AllArgsConstructor
public class OtpController {

    private final OtpService otpService;
    private final OtpSessionService otpSessionService;
    private final OtpStorageService otpStorageService;
    private final OtpAuthorizationService otpAuthorizationService;

    @GetMapping("enterPhoneNumber")
    public String showPhoneInputPage(@RequestParam(required = false) String client_id,
                                     @RequestParam(required = false) String state,
                                     @RequestParam(required = false) String redirect_uri,
                                     @RequestParam(required = false) String code_challenge,
                                     @RequestParam(required = false) String code_challenge_method,
                                     @RequestParam(required = false) String scope,
                                     HttpSession session) {
        otpSessionService.storeOAuth2Parameters(session, client_id, state, redirect_uri,
                code_challenge, code_challenge_method, scope);
        return "auth/phone-input";
    }

    @PostMapping("sendOtp")
    public String sendOtp(@Valid @RequestBody SendOtpRequestDto otpRequestDto,
                          HttpSession session, RedirectAttributes redirectAttributes) {
        String phoneNumber = otpRequestDto.getPhoneNumber();
        try {
            OtpResponse otpResponse = otpService.sendOtp(phoneNumber);
            if (otpResponse.isSuccess()) {
                String sessionId = UUID.randomUUID().toString();
                otpSessionService.storePhoneNumberAndAuthSessionId(sessionId, phoneNumber, session);
                otpStorageService.storeAuthSessionId(sessionId, phoneNumber, 10);
                log.info("OTP sent successfully for phone: {}", maskPhoneNumber(phoneNumber));
                return "redirect:/otp/enterOtp";
            } else {
                redirectAttributes.addAttribute("error", otpResponse.getMessage());
                return "redirect:/otp/enterPhoneNumber";
            }
        } catch (Exception e) {
            log.error("Error sending OTP", e);
            redirectAttributes.addAttribute("error", "Failed to send OTP. Please try again.");
            return "redirect:/otp/enterPhoneNumber";
        }
    }

    @GetMapping("enterOtp")
    public String showOtpInputPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String phoneNumber = (String) session.getAttribute("phoneNumber");
        if (phoneNumber == null) {
            log.warn("OTP page accessed without phone number in session");
            redirectAttributes.addAttribute("error", "Session expired. Please start again.");
            return "redirect:/otp/enterPhoneNumber";
        }
        model.addAttribute("phoneNumber", maskPhoneNumber(phoneNumber));
        return "auth/otp-input";
    }

    @PostMapping("verifyOtp")
    public String verifyOtp(@Valid @RequestBody VerifyOtpRequestDto request,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        try {
            SessionValidationDto sessionValidation = otpSessionService.validatePhoneNumberAndAuthSessionId(session);
            if (!sessionValidation.isValid()) {
                redirectAttributes.addAttribute("error", sessionValidation.getErrorMessage());
                return "redirect:/otp/enterPhoneNumber";
            }
            String phoneNumber = sessionValidation.getPhoneNumber();
            boolean isValid = otpStorageService.validateAndConsumeOtp(phoneNumber, request.getOtp());
            if (isValid) {
                log.info("OTP validation successful for phone: {}", maskPhoneNumber(phoneNumber));
                try {
                    SessionDto sessionDto = otpSessionService.getSessionDto(session);
                    if (sessionDto.clientId() == null) {
                        log.error("Missing OAuth2 parameters in session - clientId");
                        redirectAttributes.addAttribute("error", "Invalid authorization request.");
                        return "redirect:/otp/enterPhoneNumber";
                    }
                    String authorizationCode = "auth_code_" + UUID.randomUUID().toString().replace("-", "");
                    String redirectUrl = otpAuthorizationService
                            .createAuthorization(authorizationCode, sessionDto);
                    otpStorageService.storeAuthCode(authorizationCode, sessionDto.phoneNumber(), 5); // 5 minutes
                    otpStorageService.removeAuthSessionId(sessionDto.authSessionId());
                    otpSessionService.removePhoneNumberAndAuthSessionId(session);
                    return "redirect:" + buildRedirectUrl(sessionDto, authorizationCode, redirectUrl);
                } catch (Exception e) {
                    log.error("Error completing authorization", e);
                    redirectAttributes.addAttribute("error", "Authorization failed. Please try again.");
                    return "redirect:/otp/enterPhoneNumber";
                }
            } else {
                log.warn("OTP validation failed for phone: {}", maskPhoneNumber(phoneNumber));
                redirectAttributes.addAttribute("error", "Invalid or expired OTP. Please try again.");
                return "redirect:/otp/enterOtp";
            }
        } catch (Exception e) {
            log.error("Error verifying OTP", e);
            redirectAttributes.addAttribute("error", "Verification failed. Please try again.");
            return "redirect:/otp/enterOtp";
        }
    }

    private static StringBuilder buildRedirectUrl(SessionDto sessionDto, String authorizationCode, String redirectUrl) {
        StringBuilder url = new StringBuilder(redirectUrl);
        url.append("?code=").append(authorizationCode);
        if (sessionDto.state() != null) {
            url.append("&state=").append(URLEncoder.encode(sessionDto.state(), StandardCharsets.UTF_8));
        }
        log.info("Authorization completed successfully for client: {} and phone: {}",
                sessionDto.clientId(), maskPhoneNumber(sessionDto.phoneNumber()));
        return url;
    }

    @GetMapping("welcome")
    public String welcome() {
        return "auth/welcome";
    }
}