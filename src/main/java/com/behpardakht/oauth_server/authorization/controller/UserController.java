package com.behpardakht.oauth_server.authorization.controller;

import com.behpardakht.oauth_server.authorization.model.dto.UsersDto;
import com.behpardakht.oauth_server.authorization.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = API_PREFIX + "/user/")
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN') or #username == authentication.principal.username")
    @GetMapping(path = "findByUsername")
    public UsersDto findByUsername(@RequestParam String username) {
        return userService.findUserByUsername(username);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "existUsername")
    public Boolean existUsername(@RequestParam String username) {
        return userService.existUserWithUsername(username);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "existPhoneNumber")
    public Boolean existPhoneNumber(@RequestParam String phoneNumber) {
        return userService.existUserWithPhoneNumber(phoneNumber);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "register")
    public void register(@RequestBody UsersDto users) {
        userService.registerUser(users);
    }

    @PreAuthorize("#oldUsername == authentication.principal.username")
    @PostMapping(path = "changeUsername")
    public void changeUsername(@RequestParam String oldUsername,
                               @RequestParam String newUsername) {
        userService.changeUsername(oldUsername, newUsername);
    }

    @PreAuthorize("#username == authentication.principal.username")
    @PostMapping(path = "changePassword")
    public void changePassword(@RequestParam String username,
                               @RequestParam String oldPassword,
                               @RequestParam String newPassword) {
        userService.changePassword(oldPassword, newPassword);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(path = "addRoleToUser")
    public void addRoleToUser(@RequestParam String username,
                              @RequestParam String roleName) {
        userService.addRoleToUser(username, roleName);
    }
}