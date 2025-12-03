package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.exception.Messages;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.oauth_server.authorization.model.dto.RoleDto;
import com.behpardakht.oauth_server.authorization.model.entity.Role;
import com.behpardakht.oauth_server.authorization.model.mapper.RoleMapper;
import com.behpardakht.oauth_server.authorization.repository.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public void save(RoleDto roleDto) {
        Role role = roleMapper.toEntity(roleDto);
        roleRepository.save(role);
    }

    public Set<RoleDto> findAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roleMapper.toDtoList(Set.copyOf(roles));
    }

    public Role findByName(String name) {
        return roleRepository.findRoleByName(name)
                .orElseThrow(() -> new NotFoundException("Role", "RoleName", name));
    }

    public Role findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role", "id", id.toString()));
    }

    public RoleDto findDtoById(Long id) {
        Role role = findById(id);
        return roleMapper.toDto(role);
    }

    public Boolean toggleStatus(Long id) {
        Role role = findById(id);
        role.setIsEnabled(!Boolean.TRUE.equals(role.getIsEnabled()));
        roleRepository.save(role);
        return role.getIsEnabled();
    }

    public void delete(Long id) {
        Role role = findById(id);
        if (roleRepository.isRoleAssignedToUsers(role.getId())) {
            throw new CustomException(Messages.ROLE_ASSIGNED_TO_USERS, role.getName());
        }
        roleRepository.delete(role);
    }
}