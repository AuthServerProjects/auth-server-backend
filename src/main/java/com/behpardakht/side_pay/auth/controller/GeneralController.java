package com.behpardakht.side_pay.auth.controller;

import com.behpardakht.side_pay.auth.service.GeneralService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class GeneralController {

    private final GeneralService generalService;

    @GetMapping(path = "/loadAuthenticationMethodType")
    public ResponseEntity<List<String>> loadAuthenticationMethodType() {
        List<String> typeList = generalService.loadAuthenticationMethodType();
        return ResponseEntity.ok(typeList);
    }

    @GetMapping(path = "/loadAuthorizationGrantType")
    public ResponseEntity<List<String>> loadAuthorizationGrantType() {
        List<String> typeList = generalService.loadAuthorizationGrantType();
        return ResponseEntity.ok(typeList);
    }

    @GetMapping(path = "/loadScopeType")
    public ResponseEntity<List<String>> loadScopeType() {
        List<String> typeList = generalService.loadScopeType();
        return ResponseEntity.ok(typeList);
    }
}