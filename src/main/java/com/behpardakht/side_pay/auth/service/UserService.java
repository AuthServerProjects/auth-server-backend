package com.behpardakht.side_pay.auth.service;

import com.behpardakht.side_pay.auth.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.side_pay.auth.model.dto.UsersDto;
import com.behpardakht.side_pay.auth.model.entity.Role;
import com.behpardakht.side_pay.auth.model.entity.Users;
import com.behpardakht.side_pay.auth.model.mapper.UserMapper;
import com.behpardakht.side_pay.auth.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public void registerUser(UsersDto usersDto) {
        if (userExists(usersDto.getUsername())) {
            throw new AlreadyExistException("Username", usersDto.getUsername());
        } else {
            usersDto.setPassword(passwordEncoder.encode(usersDto.getPassword()));
            userRepository.save(userMapper.toEntity(usersDto));
        }
    }

    @PreAuthorize(value = "")
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

    @PreAuthorize(value = "")
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

    @PreAuthorize(value = "")
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

    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}