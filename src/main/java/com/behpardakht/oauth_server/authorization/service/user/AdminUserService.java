package com.behpardakht.oauth_server.authorization.service.user;

import com.behpardakht.oauth_server.authorization.aspect.Auditable;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.user.UserFilterDto;
import com.behpardakht.oauth_server.authorization.model.dto.user.UsersDto;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import com.behpardakht.oauth_server.authorization.model.mapper.UserMapper;
import com.behpardakht.oauth_server.authorization.repository.UserRepository;
import com.behpardakht.oauth_server.authorization.repository.filter.UserFilterSpecification;
import com.behpardakht.oauth_server.authorization.sms.ISmsService;
import com.behpardakht.oauth_server.authorization.util.GeneralUtil;
import com.behpardakht.oauth_server.authorization.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.maskPhoneNumber;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final UserFilterSpecification userFilterSpecification;

    private final ISmsService iSmsService;
    private final PasswordEncoder passwordEncoder;

    public PageableResponseDto<UsersDto> findAll(PageableRequestDto<UserFilterDto> request) {
        SecurityUtils.setClientContext(request, UserFilterDto::new);
        Specification<Users> spec = userFilterSpecification.toSpecification(request.getFilters());
        Page<Users> page = userRepository.findAll(spec, request.toPageable());
        List<UsersDto> responses = userMapper.toDtoList(page.getContent());
        return PageableResponseDto.build(responses, page);
    }

    public UsersDto findDtoById(Long id) {
        Users user = findById(id);
        return userMapper.toDto(user);
    }

    public Users findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", "id", id.toString()));
    }

    public UsersDto findDtoByUsername(String username) {
        Users user = findByUsername(username);
        return userMapper.toDto(user);
    }

    public Users findByPhoneNumber(String phoneNumber) {
        return findByPhoneNumberOptional(phoneNumber)
                .orElseThrow(() -> new NotFoundException("User", "phoneNumber", phoneNumber));
    }

    public Optional<Users> findByPhoneNumberOptional(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    public void createUserByPhoneNumber(String phoneNumber) {
        UsersDto usersDto = UsersDto.builder()
                .username(phoneNumber)
                .password(GeneralUtil.generateRandomPassword())
                .phoneNumber(phoneNumber)
                .isEnabled(true)
                .build();
        save(usersDto);
        log.info("New user account created for phone: {}", maskPhoneNumber(phoneNumber));
    }

    @Auditable(action = AuditAction.USER_CREATED, username = "#usersDto.username")
    public Users save(UsersDto usersDto) {
        if (existUserWithUsername(usersDto.getUsername())) {
            throw new AlreadyExistException("Username", usersDto.getUsername());
        }
        if (!usersDto.getPassword().isBlank()) {
            usersDto.setPassword(passwordEncoder.encode(usersDto.getPassword()));
        }
        return insert(userMapper.toEntity(usersDto));
    }

    @Auditable(action = AuditAction.USER_UPDATED, username = "#usersDto.username")
    public void update(Long id, UsersDto usersDto) {
        Users user = findById(id);
        userMapper.toEntity(user, usersDto);
        insert(user);
    }

    @Auditable(action = AuditAction.RESET_PASSWORD, details = "#id")
    public void resetPassword(Long id) {
        Users user = findById(id);
        String newPassword = GeneralUtil.generateRandomPassword();
        iSmsService.send(user.getPhoneNumber(), newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        insert(user);
    }

    public Users findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User", "username", username));
    }

    public boolean existUserWithUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existUserWithPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Auditable(action = AuditAction.STATUS_CHANGED, details = "#id")
    public Boolean toggleStatus(Long id) {
        Users user = findById(id);
        user.setIsEnabled(!Boolean.TRUE.equals(user.getIsEnabled()));
        insert(user);
        return user.getIsEnabled();
    }

    public Users insert(Users user) {
        return userRepository.save(user);
    }
}