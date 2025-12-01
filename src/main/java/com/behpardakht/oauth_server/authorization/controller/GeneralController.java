package com.behpardakht.oauth_server.authorization.controller;

import com.behpardakht.oauth_server.authorization.service.GeneralService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = API_PREFIX + "/general/")
public class GeneralController {

    private final GeneralService generalService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "loadAuthenticationMethodType")
    public ResponseEntity<List<String>> loadAuthenticationMethodType() {
        List<String> typeList = generalService.loadAuthenticationMethodType();
        return ResponseEntity.ok(typeList);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "loadAuthorizationGrantType")
    public ResponseEntity<List<String>> loadAuthorizationGrantType() {
        List<String> typeList = generalService.loadAuthorizationGrantType();
        return ResponseEntity.ok(typeList);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "loadScopeType")
    public ResponseEntity<List<String>> loadScopeType() {
        List<String> typeList = generalService.loadScopeType();
        return ResponseEntity.ok(typeList);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "loadPkceMethod")
    public ResponseEntity<List<String>> loadPkceMethod() {
        List<String> methodList = generalService.loadPkceMethod();
        return ResponseEntity.ok(methodList);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "loadUserRoles")
    public ResponseEntity<List<String>> loadUserRoles() {
        List<String> roleList = generalService.loadUserRoles();
        return ResponseEntity.ok(roleList);
    }
}