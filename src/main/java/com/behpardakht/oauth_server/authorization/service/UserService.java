package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.user.UserFilterDto;
import com.behpardakht.oauth_server.authorization.model.dto.user.UsersDto;
import com.behpardakht.oauth_server.authorization.model.entity.Role;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import com.behpardakht.oauth_server.authorization.model.enums.UserRole;
import com.behpardakht.oauth_server.authorization.model.mapper.UserMapper;
import com.behpardakht.oauth_server.authorization.repository.UserFilterSpecification;
import com.behpardakht.oauth_server.authorization.repository.UserRepository;
import com.behpardakht.oauth_server.authorization.sms.ISmsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.maskPhoneNumber;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final UserFilterSpecification userFilterSpecification;

    private final ISmsService iSmsService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public PageableResponseDto<UsersDto> findAll(PageableRequestDto<UserFilterDto> request) {
        Specification<Users> spec = userFilterSpecification.toSpecification(request.getFilters());
        Page<Users> page = userRepository.findAll(spec, request.toPageable());
        List<UsersDto> responses = userMapper.toDtoList(page.getContent());
        return PageableResponseDto.build(responses, page);
    }

    public UsersDto findById(Long id) {
        Users user = getUser(id);
        return userMapper.toDto(user);
    }

    private Users getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", "id", id.toString()));
    }

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
                .password(generateSecureRandomPassword())
                .phoneNumber(phoneNumber)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .isEnabled(true)
                .build();
        registerUser(usersDto);
        log.info("New user account created for phone: {}", maskPhoneNumber(phoneNumber));
    }

    private String generateSecureRandomPassword() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public void registerUser(UsersDto usersDto) {
        if (existUserWithUsername(usersDto.getUsername())) {
            throw new AlreadyExistException("Username", usersDto.getUsername());
        } else {
            if (!usersDto.getPassword().isBlank()) {
                usersDto.setPassword(passwordEncoder.encode(usersDto.getPassword()));
            }
            Role role = roleService.findByName(UserRole.USER.getValue());
            Users user = userMapper.toEntity(usersDto);
            user.getRoles().add(role);
            userRepository.save(user);
        }
    }

    public void updateUser(Long id, UsersDto usersDto) {
        Users user = getUser(id);
        userMapper.toEntity(user, usersDto);
        userRepository.save(user);
    }

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

    public void resetPassword(Long id) {
        Users user = getUser(id);
        String newPassword = generateSecureRandomPassword();
        iSmsService.send(user.getPhoneNumber(), newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void addRoleToUser(String username, String roleName) {
        Users user = findByUsername(username);
        Role role = roleService.findByName(roleName);
        user.getRoles().add(role);
        userRepository.save(user);
    }

    public void removeRoleFromUser(String username, String roleName) {
        Users user = findByUsername(username);
        Role role = roleService.findByName(roleName);
        user.getRoles().remove(role);
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

    public Boolean toggleStatus(Long id) {
        Users user = getUser(id);
        user.setIsEnabled(!Boolean.TRUE.equals(user.getIsEnabled()));
        userRepository.save(user);
        return user.getIsEnabled();
    }
}