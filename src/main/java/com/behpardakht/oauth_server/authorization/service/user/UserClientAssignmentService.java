package com.behpardakht.oauth_server.authorization.service.user;

import com.behpardakht.oauth_server.authorization.exception.ExceptionMessage;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.behpardakht.oauth_server.authorization.model.dto.user.CreateUserAssignmentDto;
import com.behpardakht.oauth_server.authorization.model.dto.user.UserClientAssignmentDto;
import com.behpardakht.oauth_server.authorization.model.entity.Client;
import com.behpardakht.oauth_server.authorization.model.entity.UserClientAssignment;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import com.behpardakht.oauth_server.authorization.model.mapper.UserClientAssignmentMapper;
import com.behpardakht.oauth_server.authorization.repository.UserClientAssignmentRepository;
import com.behpardakht.oauth_server.authorization.service.ClientService;
import com.behpardakht.oauth_server.authorization.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserClientAssignmentService {

    private final ClientService clientService;
    private final AdminUserService adminUserService;

    private final UserClientAssignmentMapper userClientAssignmentMapper;
    private final UserClientAssignmentRepository userClientAssignmentRepository;

    public List<UserClientAssignmentDto> findAllByCurrentClient() {
        Long clientId = SecurityUtils.getCurrentClientId();
        List<UserClientAssignment> userClientAssignmentList = userClientAssignmentRepository.findByClient_Id(clientId);
        return userClientAssignmentMapper.toDtoList(userClientAssignmentList);
    }

    public UserClientAssignment findById(Long id) {
        return userClientAssignmentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ExceptionMessage.USER_ASSIGNMENT_NOT_FOUND));
    }

    public UserClientAssignmentDto findDtoById(Long id) {
        UserClientAssignment userClientAssignment = findById(id);
        return userClientAssignmentMapper.toDto(userClientAssignment);
    }

    public List<UserClientAssignmentDto> findByUserId(Long userId) {
        return userClientAssignmentRepository.findByUserId(userId).stream()
                .map(userClientAssignmentMapper::toDto)
                .toList();
    }

    public UserClientAssignmentDto save(CreateUserAssignmentDto request) {
        Long clientId = SecurityUtils.getCurrentClientId();
        Users user = adminUserService.findById(request.getUserId());
        Client client = clientService.findById(clientId);
        if (userClientAssignmentRepository.existsByUserAndClient(user, client)) {
            throw new CustomException(ExceptionMessage.USER_ALREADY_ASSIGNED);
        }
        UserClientAssignment userClientAssignment = create(user, client);
        return userClientAssignmentMapper.toDto(userClientAssignment);
    }

    private UserClientAssignment create(Users user, Client client) {
        UserClientAssignment assignment = UserClientAssignment.builder()
                .user(user)
                .client(client)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        return insert(assignment);
    }

    public UserClientAssignment insert(UserClientAssignment assignment) {
        return userClientAssignmentRepository.save(assignment);
    }

    public void banUser(Long id) {
        UserClientAssignment userClientAssignment = findById(id);
        userClientAssignment.setIsEnabled(false);
        userClientAssignment.setIsAccountNonLocked(false);
        userClientAssignmentRepository.save(userClientAssignment);
    }

    public void unbanUser(Long id) {
        UserClientAssignment userClientAssignment = findById(id);
        userClientAssignment.setIsEnabled(true);
        userClientAssignment.setIsAccountNonLocked(true);
        userClientAssignmentRepository.save(userClientAssignment);
    }

    public void delete(Long id) {
        UserClientAssignment userClientAssignment = findById(id);
        userClientAssignmentRepository.delete(userClientAssignment);
    }

    public UserClientAssignment findOrCreateAssignment(Users user, String clientId) {
        return userClientAssignmentRepository.findByUserAndClientClientId(user, clientId)
                .orElseGet(() -> {
                    Client client = clientService.findByClientId(clientId);
                    return create(user, client);
                });
    }

    public Optional<UserClientAssignment> findByUserAndClient(Users user, Client client) {
        return userClientAssignmentRepository.findByUserAndClient(user, client);
    }
}