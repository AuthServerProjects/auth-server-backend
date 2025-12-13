package com.behpardakht.oauth_server.authorization.controller.admin;

import com.behpardakht.oauth_server.authorization.config.bundle.MessageResolver;
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
import com.behpardakht.oauth_server.authorization.util.Messages;
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
                Messages.CLIENT_REGISTERED_SUCCESS.getMessage(), new Object[]{request.getClientId()});
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping(path = "update/{clientId}")
    public ResponseEntity<ResponseDto<String>> update(@PathVariable String clientId,
                                                      @RequestBody ClientDto request) {
        clientService.update(clientId, request);
        String response = MessageResolver.getMessage(
                Messages.CLIENT_UPDATED_SUCCESS.getMessage(), new Object[]{clientId});
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "find/{clientId}")
    public ResponseEntity<ResponseDto<ClientDto>> findByClientId(@PathVariable String clientId) {
        ClientDto response = clientService.findDtoByClientId(clientId);
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
    @PostMapping(path = "regenerateSecret/{clientId}")
    public ResponseEntity<ResponseDto<String>> regenerateSecret(@PathVariable String clientId) {
        String secret = clientService.regenerateSecret(clientId);
        String response = MessageResolver.getMessage(
                Messages.CLIENT_SECRET_REGENERATED_SUCCESS.getMessage(), new Object[]{clientId, secret});
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping(path = "toggleStatus/{clientId}")
    public ResponseEntity<ResponseDto<Boolean>> toggleStatus(@PathVariable String clientId) {
        Boolean response = clientService.toggleStatus(clientId);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @GetMapping(path = "defaultRegister")
    public ResponseEntity<String> register() {
        ClientDto clientDto = ClientDto.builder()
                .clientId("Wallet")
                .clientSecret("secret")
                .clientAuthenticationMethods(Set.of(
                        AuthenticationMethodTypes.NONE,
                        AuthenticationMethodTypes.CLIENT_SECRET_JWT,
                        AuthenticationMethodTypes.CLIENT_SECRET_POST,
                        AuthenticationMethodTypes.CLIENT_SECRET_BASIC))
                .authorizationGrantTypes(Set.of(
                        AuthorizationGrantTypes.AUTHORIZATION_CODE,
                        AuthorizationGrantTypes.CLIENT_CREDENTIALS,
                        AuthorizationGrantTypes.REFRESH_TOKEN))
                .scopes(Set.of(
                        ScopeTypes.OPENID,
                        ScopeTypes.PROFILE,
                        ScopeTypes.ADDRESS,
                        ScopeTypes.EMAIL,
                        ScopeTypes.PHONE))
                .redirectUris(Set.of("http://localhost:9090/otp/welcome"))
                .setting(TokenAndClientSettingDto.builder()
                        .requireProofKey(false)
                        .accessTokenTimeToLive(30L)
                        .refreshTokenTimeToLive(30L)
                        .reuseRefreshTokens(false)
                        .build())
                .build();
        clientService.save(clientDto);
        String message = MessageResolver.getMessage(
                Messages.CLIENT_REGISTERED_SUCCESS.getMessage(), new Object[]{clientDto.getClientId()});
        return ResponseEntity.ok(message);
    }
}