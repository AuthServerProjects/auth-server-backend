package com.behpardakht.oauth_server.authorization.controller;

import com.behpardakht.oauth_server.authorization.model.dto.ResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.otp.request.InitOtpRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.otp.request.SendOtpRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.otp.request.VerifyOtpRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.otp.response.VerifyOtpResponseDto;
import com.behpardakht.oauth_server.authorization.service.otp.OtpFacadeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.API_PREFIX;
import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.getClientIpAddress;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = API_PREFIX + "/api/otp")
public class OtpRestController {

    private final OtpFacadeService otpFacadeService;

    @PostMapping("/initOtp")
    public ResponseEntity<ResponseDto<?>> initOtpSession(@RequestBody @Valid InitOtpRequestDto request) {
        otpFacadeService.initializeOtpSession(request);
        return ResponseEntity.ok(ResponseDto.success(null));
    }

    @PostMapping("/sendOtp")
    public ResponseEntity<ResponseDto<String>> sendOtp(@RequestBody @Valid SendOtpRequestDto request,
                                                       HttpServletRequest httpRequest) {
        String message = otpFacadeService.sendOtp(request, getClientIpAddress(httpRequest));
        return ResponseEntity.ok(ResponseDto.success(message));
    }

    @PostMapping("/verifyOtp")
    public ResponseEntity<ResponseDto<VerifyOtpResponseDto>> verifyOtp(@RequestBody @Valid VerifyOtpRequestDto request,
                                                                       HttpServletRequest httpRequest) {
        VerifyOtpResponseDto response =
                otpFacadeService.verifyOtpAndCreateAuthorization(request, getClientIpAddress(httpRequest));
        return ResponseEntity.ok(ResponseDto.success(response));
    }
}