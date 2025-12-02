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

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(path = "save")
    public ResponseEntity<ResponseDto<String>> save(@RequestBody ClientDto request) {
        clientService.save(request);
        String response = MessageResolver.getMessage(
                ExceptionMessages.CLIENT_REGISTERED_SUCCESS.getMessage(), new Object[]{request.getClientId()});
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping(path = "update/{id}")
    public ResponseEntity<ResponseDto<String>> update(@PathVariable String id,
                                                      @RequestBody ClientDto request) {
        clientService.update(id, request);
        return ResponseEntity.ok(ResponseDto.success("Client updated successfully"));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "find/{id}")
    public ResponseEntity<ResponseDto<ClientDto>> findByClientId(@PathVariable String id) {
        ClientDto response = clientService.findByClientId(id);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(path = "findAll")
    public ResponseEntity<ResponseDto<PageableResponseDto<ClientDto>>> findAll(@RequestBody
                                                                               PageableRequestDto<ClientFilterDto> request) {
        PageableResponseDto<ClientDto> response = clientService.findAll(request);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(path = "regenerateSecret/{id}")
    public ResponseEntity<ResponseDto<String>> regenerateSecret(@PathVariable String id) {
        String response = clientService.regenerateSecret(id);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping(path = "toggleStatus/{id}")
    public ResponseEntity<ResponseDto<Boolean>> toggleStatus(@PathVariable String id) {
        Boolean response = clientService.toggleStatus(id);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @GetMapping(path = "defaultRegister")
    public ResponseEntity<String> register() {
        ClientDto clientDto = new ClientDto();
        clientDto.setClientId("Wallet");
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
        clientService.save(clientDto);
        String message = MessageResolver.getMessage(
                ExceptionMessages.CLIENT_REGISTERED_SUCCESS.getMessage(), new Object[]{clientDto.getClientId()});
        return ResponseEntity.ok(message);
    }
}