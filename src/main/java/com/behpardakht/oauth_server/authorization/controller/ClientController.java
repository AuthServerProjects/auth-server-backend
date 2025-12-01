package com.behpardakht.oauth_server.authorization.controller;

import com.behpardakht.oauth_server.authorization.config.bundle.MessageResolver;
import com.behpardakht.oauth_server.authorization.exception.ExceptionMessages;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.client.ClientDto;
import com.behpardakht.oauth_server.authorization.model.dto.client.ClientFilterDto;
import com.behpardakht.oauth_server.authorization.model.dto.client.TokenAndClientSettingDto;
import com.behpardakht.oauth_server.authorization.model.enums.AuthenticationMethodTypes;
import com.behpardakht.oauth_server.authorization.model.enums.AuthorizationGrantTypes;
import com.behpardakht.oauth_server.authorization.model.enums.ScopeTypes;
import com.behpardakht.oauth_server.authorization.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.ADMIN_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = ADMIN_PREFIX + "/client/")
public class ClientController {

    private final ClientService clientService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "{clientId}")
    public ResponseEntity<ClientDto> findByClientId(@PathVariable String clientId) {
        ClientDto client = clientService.findByClientId(clientId);
        return ResponseEntity.ok(client);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(path = "register")
    public ResponseEntity<String> register(@RequestBody ClientDto client) {
        clientService.insertClient(client);
        String message = MessageResolver.getMessage(
                ExceptionMessages.CLIENT_REGISTERED_SUCCESS.getMessage(), new Object[]{client.getClientId()});
        return ResponseEntity.ok(message);
    }

    @GetMapping(path = "defaultRegister")
    public ResponseEntity<String> register() {
        ClientDto clientDto = new ClientDto();
        clientDto.setClientId("SidePay");
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
        clientDto.setRedirectUris(Set.of("http://localhost:9090/otp/welcome"));
        TokenAndClientSettingDto settingDto = new TokenAndClientSettingDto();
        settingDto.setRequireProofKey(false);
        settingDto.setAccessTokenTimeToLive(30L);
        settingDto.setRefreshTokenTimeToLive(30L);
        settingDto.setReuseRefreshTokens(false);
        clientDto.setSetting(settingDto);
        clientService.insertClient(clientDto);
        String message = MessageResolver.getMessage(
                ExceptionMessages.CLIENT_REGISTERED_SUCCESS.getMessage(), new Object[]{clientDto.getClientId()});
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(path = "findAll")
    public ResponseEntity<ResponseDto<PageableResponseDto<ClientDto>>> findAll(@RequestBody
                                                                               PageableRequestDto<ClientFilterDto> request) {
        PageableResponseDto<ClientDto> clients = clientService.findAll(request);
        return ResponseEntity.ok(ResponseDto.success(clients));
    }
}