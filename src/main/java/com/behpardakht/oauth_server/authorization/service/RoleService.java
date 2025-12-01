package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.exception.ExceptionMessages;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.oauth_server.authorization.model.entity.Client;
import com.behpardakht.oauth_server.authorization.model.entity.Role;
import com.behpardakht.oauth_server.authorization.repository.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public void save(Role role) {
        roleRepository.save(role);
    }

    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    public Role findByName(String name) {
        return roleRepository.findRoleByName(name)
                .orElseThrow(() -> new NotFoundException("Role", "RoleName", name));
    }

    public Role findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role", "id", id.toString()));
    }

    public void delete(Long id) {
        Role role = findById(id);
        if (roleRepository.isRoleAssignedToUsers(role.getId())) {
            throw new CustomException(ExceptionMessages.ROLE_ASSIGNED_TO_USERS);
        }
        roleRepository.delete(role);
    }

    public Boolean toggleStatus(Long roleId) {
        Role role = findById(roleId);
        role.setIsEnabled(!Boolean.TRUE.equals(role.getIsEnabled()));
        roleRepository.save(role);
        return role.getIsEnabled();
    }
}