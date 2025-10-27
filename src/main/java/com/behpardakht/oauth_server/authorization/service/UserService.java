package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.oauth_server.authorization.model.dto.UsersDto;
import com.behpardakht.oauth_server.authorization.model.entity.Role;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import com.behpardakht.oauth_server.authorization.model.mapper.UserMapper;
import com.behpardakht.oauth_server.authorization.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.maskPhoneNumber;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UsersDto findUserByUsername(String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
        return userMapper.toDto(user);
    }

    public UsersDto findByPhoneNumber(String phoneNumber) {
        Users user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
        return userMapper.toDto(user);
    }

    public void createUserByPhoneNumber(String phoneNumber) {
        UsersDto usersDto = UsersDto.builder()
                .username(phoneNumber)
                .password("otp_user")
                .phoneNumber(phoneNumber)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .isEnabled(true)
                .build();
        registerUser(usersDto);
        log.info("New user account created for phone: {}", maskPhoneNumber(phoneNumber));
    }

    public void registerUser(UsersDto usersDto) {
        if (existUserWithUsername(usersDto.getUsername())) {
            throw new AlreadyExistException("Username", usersDto.getUsername());
        } else {
            usersDto.setPassword(passwordEncoder.encode(usersDto.getPassword()));
            userRepository.save(userMapper.toEntity(usersDto));
        }
    }

    @PreAuthorize(value = "Admin")
    public void changeUsername(String oldUsername, String newUsername) {
        if (oldUsername.equals(newUsername)) {
            throw new IllegalArgumentException("New username can not be the same as old one");
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!username.equals(oldUsername)) {
            throw new IllegalArgumentException("Old username is incorrect");
        }
        UsersDto users = findUserByUsername(username);
        users.setUsername(newUsername);
        userRepository.save(userMapper.toEntity(users));
    }

    @PreAuthorize(value = "Admin")
    public void changePassword(String oldPassword, String newPassword) {
        String encodeNewPassword = passwordEncoder.encode(newPassword);
        if (passwordEncoder.matches(oldPassword, encodeNewPassword)) {
            throw new IllegalArgumentException("New Password can not be the same as old one");
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UsersDto users = findUserByUsername(username);
        if (!passwordEncoder.matches(oldPassword, users.getPassword())) {
            throw new IllegalArgumentException("Old Password is incorrect");
        }
        users.setPassword(encodeNewPassword);
        userRepository.save(userMapper.toEntity(users));
    }

    @PreAuthorize(value = "Admin")
    public void addRoleToUser(String username, String roleName) {
        Users user = findByUsername(username);
        Role role = roleService.findByName(roleName);
        user.getRoles().add(role);
        userRepository.save(user);
    }

    private Users findByUsername(String username) {
        if (username != null) {
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User Not Found!"));
        } else {
            throw new IllegalArgumentException("username is null");
        }
    }

    public boolean existUserWithUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean existUserWithPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).isPresent();
    }
}