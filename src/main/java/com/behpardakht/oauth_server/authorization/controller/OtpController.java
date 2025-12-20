package com.behpardakht.oauth_server.authorization.controller;

import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.otp.SendOtpRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.otp.VerifyOtpRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.otp.VerifyOtpResponseDto;
import com.behpardakht.oauth_server.authorization.service.otp.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.API_PREFIX;
import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.getClientIpAddress;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = API_PREFIX + "/otp/")
public class OtpController {

    private final OtpService otpService;

    @PostMapping("send")
    public ResponseEntity<ResponseDto<String>> sendOtp(@RequestBody @Valid SendOtpRequestDto request,
                                                       HttpServletRequest httpRequest) {
        String response = otpService.sendOtp(request, getClientIpAddress(httpRequest));
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PostMapping("verify")
    public ResponseEntity<ResponseDto<VerifyOtpResponseDto>> verifyOtp(@RequestBody @Valid VerifyOtpRequestDto request,
                                                                       HttpServletRequest httpRequest) {
        VerifyOtpResponseDto response =
                otpService.verifyOtpAndCreateAuthorization(request, getClientIpAddress(httpRequest));
        return ResponseEntity.ok(ResponseDto.success(response));
    }
}