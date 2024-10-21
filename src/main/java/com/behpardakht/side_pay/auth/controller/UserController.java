package com.behpardakht.side_pay.auth.controller;

import com.behpardakht.side_pay.auth.model.dto.UsersDto;
import com.behpardakht.side_pay.auth.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public UsersDto findByUsername(@RequestParam String username) {
        return userService.findUserByUsername(username);
    }

    @GetMapping(path = "/exist")
    public Boolean userExists(@RequestParam String username) {
        return userService.userExists(username);
    }

    @PostMapping(path = "/register")
    public void register(@RequestBody UsersDto users) {
        userService.registerUser(users);
    }

    @PreAuthorize("#oldUsername == authentication.principal.username")
    @PostMapping(path = "/changeUsername")
    public void changeUsername(@RequestParam String oldUsername,
                               @RequestParam String newUsername) {
        userService.changeUsername(oldUsername, newUsername);
    }

    @PreAuthorize("#username == authentication.principal.username")
    @PostMapping(path = "/changePassword")
    public void changePassword(@RequestParam String username,
                               @RequestParam String oldPassword,
                               @RequestParam String newPassword) {
        userService.changePassword(oldPassword, newPassword);
    }

    @PreAuthorize("hasRole('Admin')")
    @PostMapping(path = "/addRoleToUser")
    public void addRoleToUser(@RequestParam String username,
                              @RequestParam String roleName) {
        userService.addRoleToUser(username, roleName);
    }
}