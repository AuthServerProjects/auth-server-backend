package com.behpardakht.side_pay.auth.controller;

import com.behpardakht.side_pay.auth.enums.AuthenticationMethodTypes;
import com.behpardakht.side_pay.auth.enums.AuthorizationGrantTypes;
import com.behpardakht.side_pay.auth.enums.ScopeTypes;
import com.behpardakht.side_pay.auth.model.dto.ClientDto;
import com.behpardakht.side_pay.auth.model.dto.TokenAndClientSettingDto;
import com.behpardakht.side_pay.auth.service.ClientService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping(path = "/client")
@AllArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PreAuthorize("hasRole('Admin')")
    @GetMapping(path = "/{clientId}")
    public ResponseEntity<ClientDto> findByClientId(@PathVariable String clientId) {
        ClientDto client = clientService.findByClientId(clientId);
        return ResponseEntity.ok(client);
    }

    @PreAuthorize("hasRole('Admin')")
    @PostMapping(path = "/register")
    public ResponseEntity<String> register(@RequestBody ClientDto client) {
        clientService.insertClient(client);
        return ResponseEntity.ok("Client registered Successfully : " + client.getClientId());
    }

    @PreAuthorize("hasRole('Admin')")
    @PostMapping(path = "/defaultRegister")
    public ResponseEntity<String> register() {
        ClientDto clientDto = new ClientDto();
        clientDto.setClientId("android");
        clientDto.setClientSecret("secret");
        clientDto.setClientAuthenticationMethods(Set.of(
                AuthenticationMethodTypes.NONE,
                AuthenticationMethodTypes.CLIENT_SECRET_JWT,
                AuthenticationMethodTypes.CLIENT_SECRET_POST,
                AuthenticationMethodTypes.CLIENT_SECRET_BASIC));
        clientDto.setAuthorizationGrantTypes(Set.of(
                AuthorizationGrantTypes.AUTHORIZATION_CODE,
                AuthorizationGrantTypes.CLIENT_CREDENTIALS,
                AuthorizationGrantTypes.REFRESH_TOKEN));
        clientDto.setScopes(Set.of(
                ScopeTypes.OPENID,
                ScopeTypes.PROFILE,
                ScopeTypes.ADDRESS,
                ScopeTypes.EMAIL,
                ScopeTypes.PHONE));
        clientDto.setRedirectUris(Set.of("http://127.0.0.1:9000"));
        TokenAndClientSettingDto settingDto = new TokenAndClientSettingDto();
        settingDto.setRequireProofKey(false);
        settingDto.setAccessTokenTimeToLive(30L);
        settingDto.setRefreshTokenTimeToLive(30L);
        clientDto.setSetting(settingDto);
        clientService.insertClient(clientDto);
        return ResponseEntity.ok("Client registered Successfully : " + clientDto.getClientId());
    }
}