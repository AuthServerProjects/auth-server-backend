package com.behpardakht.side_pay.auth.service;

import com.behpardakht.side_pay.auth.model.dto.UsersDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@AllArgsConstructor
public class OtpService {

    //    TODO it must be redis
    private static final Map<String, String> otpStore = new HashMap<>();
    private final UserService userService;

    public String generateOtp(String phoneNumber) {
        String otp = String.valueOf(new Random().nextInt(99999));
        otpStore.put(phoneNumber, otp);

        //TODO check how it works
        UsersDto usersDto = new UsersDto();
        usersDto.setUsername(phoneNumber);
        usersDto.setPassword(phoneNumber);
        usersDto.setPhoneNumber(phoneNumber);
        usersDto.setIsAccountNonExpired(true);
        usersDto.setIsAccountNonLocked(true);
        usersDto.setIsCredentialsNonExpired(true);
        usersDto.setIsEnabled(true);
        userService.registerUser(usersDto);
        return otp;
    }

    private void sendOtp(String phoneNumber, String otp) {
//        TODO ISMS
    }

    public Boolean validateOtp(String phoneNumber, String otp) {
        //TODO otp timeOut -> ExpireException
        return otp.equals(otpStore.get(phoneNumber));
    }
}