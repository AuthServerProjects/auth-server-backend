package com.behpardakht.oauth_server.authorization.service.user;

import com.behpardakht.oauth_server.authorization.exception.ExceptionMessage;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import com.behpardakht.oauth_server.authorization.repository.UserRepository;
import com.behpardakht.oauth_server.authorization.service.otp.OtpService;
import com.behpardakht.oauth_server.authorization.service.otp.OtpStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final OtpService otpService;
    private final OtpStorageService otpStorageService;
    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final AdminUserService adminUserService;

    public void changeUsername(String oldUsername, String newUsername) {
        if (oldUsername.equals(newUsername)) {
            throw new CustomException(ExceptionMessage.USERNAME_SAME_AS_OLD);
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!username.equals(oldUsername)) {
            throw new CustomException(ExceptionMessage.USERNAME_INCORRECT);
        }
        Users user = adminUserService.findByUsername(username);
        user.setUsername(newUsername);
        userRepository.save(user);
    }

    public void changePassword(String oldPassword, String newPassword) {
        String encodeNewPassword = passwordEncoder.encode(newPassword);
        if (passwordEncoder.matches(oldPassword, encodeNewPassword)) {
            throw new CustomException(ExceptionMessage.PASSWORD_SAME_AS_OLD);
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = adminUserService.findByUsername(username);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new CustomException(ExceptionMessage.PASSWORD_INCORRECT);
        }
        user.setPassword(encodeNewPassword);
        userRepository.save(user);
    }

    public String forgotPassword(String phoneNumber, String ipAddress) {
        if (!adminUserService.existUserWithPhoneNumber(phoneNumber)) {
            throw new NotFoundException("User", "phoneNumber", phoneNumber);
        }
        return otpService.sendOtp(phoneNumber, ipAddress).getMessage();
    }

    public void setNewPassword(String phoneNumber, String otp, String newPassword, String ipAddress) {
        boolean isValid = otpStorageService.validateAndConsumeOtp(phoneNumber, otp, ipAddress);
        if (!isValid) {
            throw new CustomException(ExceptionMessage.INVALID_OR_EXPIRED_OTP);
        }
        Users user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("User", "phoneNumber", phoneNumber));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}