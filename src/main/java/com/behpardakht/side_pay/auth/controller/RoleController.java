package com.behpardakht.side_pay.auth.controller;

import com.behpardakht.side_pay.auth.model.entity.Role;
import com.behpardakht.side_pay.auth.service.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/role")
@AllArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
//    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<String> save(@RequestBody Role role) {
        roleService.save(role);
        return ResponseEntity.ok("Role Added Successfully");
    }

    @GetMapping
    public List<Role> findAllRoles() {
        return roleService.findAllRoles();
    }
}