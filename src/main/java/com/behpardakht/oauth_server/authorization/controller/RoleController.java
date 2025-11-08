package com.behpardakht.oauth_server.authorization.controller;

import com.behpardakht.oauth_server.authorization.model.entity.Role;
import com.behpardakht.oauth_server.authorization.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = API_PREFIX + "/role/")
public class RoleController {

    private final RoleService roleService;

    @PostMapping(path = "save")
//    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<String> save(@RequestBody Role role) {
        roleService.save(role);
        return ResponseEntity.ok("Role Added Successfully");
    }

    @GetMapping(path = "findAll")
    public List<Role> findAllRoles() {
        return roleService.findAllRoles();
    }
}