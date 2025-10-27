package com.behpardakht.side_pay.auth.service;

import com.behpardakht.side_pay.auth.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.side_pay.auth.model.entity.Role;
import com.behpardakht.side_pay.auth.repository.RoleRepository;
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
}