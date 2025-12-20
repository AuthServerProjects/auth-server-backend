package com.behpardakht.oauth_server.authorization.service.user;

import com.behpardakht.oauth_server.authorization.aspect.Auditable;
import com.behpardakht.oauth_server.authorization.exception.ExceptionMessage;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.role.UserRoleDto;
import com.behpardakht.oauth_server.authorization.model.dto.role.UserRoleFilterDto;
import com.behpardakht.oauth_server.authorization.model.entity.Role;
import com.behpardakht.oauth_server.authorization.model.entity.UserClient;
import com.behpardakht.oauth_server.authorization.model.entity.UserRole;
import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import com.behpardakht.oauth_server.authorization.model.mapper.UserRoleMapper;
import com.behpardakht.oauth_server.authorization.repository.UserClientRepository;
import com.behpardakht.oauth_server.authorization.repository.UserRoleRepository;
import com.behpardakht.oauth_server.authorization.repository.filter.UserRoleFilterSpecification;
import com.behpardakht.oauth_server.authorization.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.behpardakht.oauth_server.authorization.util.SecurityUtils.validateOwnership;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleService {
    private final UserRoleMapper userRoleMapper;
    private final RoleService roleService;
    private final UserRoleRepository userRoleRepository;
    private final UserClientRepository userClientRepository;
    private final UserRoleFilterSpecification userRoleFilterSpecification;

    @Transactional
    @Auditable(action = AuditAction.ROLE_ASSIGNED, details = "#userClientId + ':' + #roleId")
    public UserRoleDto assign(Long userClientId, Long roleId) {
        validateNotAlreadyAssigned(userClientId, roleId);

        UserClient userClient = userClientRepository.findById(userClientId)
                .orElseThrow(() -> new NotFoundException("UserClient", "id", userClientId.toString()));
        Role role = roleService.findById(roleId);
        validateOwnership(userClient.getClient().getId());
        validateSameClient(userClient, role);
        UserRole userRole = UserRole.builder()
                .userClient(userClient)
                .role(role)
                .build();

        UserRole inserted = insert(userRole);
        log.info("Role {} assigned to user {} for client {}",
                role.getName(),
                userClient.getUser().getUsername(),
                userClient.getClient().getClientId());

        return userRoleMapper.toDto(inserted);
    }

    public UserRole insert(UserRole userRole) {
        return userRoleRepository.save(userRole);
    }

    private void validateNotAlreadyAssigned(Long userClientId, Long roleId) {
        if (userRoleRepository.existsByUserClientIdAndRoleId(userClientId, roleId)) {
            throw new AlreadyExistException("UserRole", "userClientId:" + userClientId + " roleId:" + roleId);
        }
    }

    private void validateSameClient(UserClient userClient, Role role) {
        if (!userClient.getClient().getId().equals(role.getClient().getId())) {
            throw new CustomException(ExceptionMessage.ROLE_CLIENT_MISMATCH);
        }
    }

    @Transactional
    @Auditable(action = AuditAction.ROLE_UNASSIGNED, details = "#userClientId + ':' + #roleId")
    public void unassign(Long userClientId, Long roleId) {
        UserRole userRole = userRoleRepository
                .findByUserClientIdAndRoleId(userClientId, roleId)
                .orElseThrow(() -> new NotFoundException(
                        "UserRole", "userClientId:roleId", userClientId + ":" + roleId));
        validateOwnership(userRole.getUserClient().getClient().getId());
        userRoleRepository.delete(userRole);
        log.info("Role userRole removed for userClient {} role {}", userClientId, roleId);
    }

    public List<UserRoleDto> findByUserClientId(Long userClientId) {
        List<UserRole> userRoleList = userRoleRepository
                .findByUserClientId(userClientId);
        return userRoleMapper.toDtoList(userRoleList);
    }

    public List<UserRole> findByUsernameAndClientId(String username, Long clientId) {
        return userRoleRepository.findByUsernameAndClientId(username, clientId);
    }

    public List<UserRoleDto> findByRoleId(Long roleId) {
        roleService.findById(roleId);
        List<UserRole> userRoleList = userRoleRepository.findByRoleId(roleId);
        return userRoleMapper.toDtoList(userRoleList);
    }

    public PageableResponseDto<UserRoleDto> findAll(PageableRequestDto<UserRoleFilterDto> request) {
        Specification<UserRole> spec = userRoleFilterSpecification.toSpecification(request.getFilters());
        Page<UserRole> page = userRoleRepository.findAll(spec, request.toPageable());
        List<UserRoleDto> responses = userRoleMapper.toDtoList(page.getContent());
        return PageableResponseDto.build(responses, page);
    }

    public UserRoleDto findDtoById(Long id) {
        UserRole userRole = findById(id);
        return userRoleMapper.toDto(userRole);
    }

    private UserRole findById(Long id) {
        return userRoleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("UserRole", "id", id.toString()));
    }

    @Transactional
    @Auditable(action = AuditAction.ROLE_UNASSIGNED, details = "#id")
    public void delete(Long id) {
        UserRole userRole = findById(id);
        validateOwnership(userRole.getUserClient().getClient().getId());
        userRoleRepository.delete(userRole);
        log.info("Role userRole {} removed", id);
    }

    public boolean existsByUserClientIdAndRoleId(Long userClientId, Long roleId) {
        return userRoleRepository
                .existsByUserClientIdAndRoleId(userClientId, roleId);
    }
}