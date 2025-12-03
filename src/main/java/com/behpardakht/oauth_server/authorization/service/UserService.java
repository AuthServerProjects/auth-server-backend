package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.exception.ExceptionMessages;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper;
import com.behpardakht.oauth_server.authorization.model.dto.user.UsersDto;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import com.behpardakht.oauth_server.authorization.model.mapper.UserMapper;
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

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final AdminUserService adminUserService;

    public void changeUsername(String oldUsername, String newUsername) {
        if (oldUsername.equals(newUsername)) {
            throw new IllegalArgumentException("New username can not be the same as old one");
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!username.equals(oldUsername)) {
            throw new IllegalArgumentException("Old username is incorrect");
        }
        UsersDto users = adminUserService.findUserByUsername(username);
        users.setUsername(newUsername);
        userRepository.save(userMapper.toEntity(users));
    }

    public void changePassword(String oldPassword, String newPassword) {
        String encodeNewPassword = passwordEncoder.encode(newPassword);
        if (passwordEncoder.matches(oldPassword, encodeNewPassword)) {
            throw new IllegalArgumentException("New Password can not be the same as old one");
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UsersDto users = adminUserService.findUserByUsername(username);
        if (!passwordEncoder.matches(oldPassword, users.getPassword())) {
            throw new IllegalArgumentException("Old Password is incorrect");
        }
        users.setPassword(encodeNewPassword);
        userRepository.save(userMapper.toEntity(users));
    }

    public String forgotPassword(String phoneNumber, String ipAddress) {
        if (!adminUserService.existUserWithPhoneNumber(phoneNumber)) {
            throw new ExceptionWrapper.NotFoundException("User", "phoneNumber", phoneNumber);
        }
        return otpService.sendOtp(phoneNumber, ipAddress).getMessage();
    }

    public void setNewPassword(String phoneNumber, String otp, String newPassword, String ipAddress) {
        boolean isValid = otpStorageService.validateAndConsumeOtp(phoneNumber, otp, ipAddress);
        if (!isValid) {
            throw new ExceptionWrapper.CustomException(ExceptionMessages.INVALID_OR_EXPIRED_OTP);
        }
        Users user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ExceptionWrapper.NotFoundException("User", "phoneNumber", phoneNumber));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}