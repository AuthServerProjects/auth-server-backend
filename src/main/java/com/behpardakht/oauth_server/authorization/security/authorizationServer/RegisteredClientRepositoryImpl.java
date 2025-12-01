package com.behpardakht.oauth_server.authorization.security.authorizationServer;

import com.behpardakht.oauth_server.authorization.model.entity.Client;
import com.behpardakht.oauth_server.authorization.model.mapper.ClientMapper;
import com.behpardakht.oauth_server.authorization.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

@Configuration
@RequiredArgsConstructor
public class RegisteredClientRepositoryImpl implements RegisteredClientRepository {

    private final PasswordEncoder passwordEncoder;
    private final ClientMapper clientMapper;
    private final ClientRepository clientRepository;

    @Override
    public void save(RegisteredClient registeredClient) {
        Client client = clientMapper.registeredClientToEntity(registeredClient);
        client.setClientSecret(passwordEncoder.encode(client.getClientSecret()));
        clientRepository.save(client);
    }

    @Override
    public RegisteredClient findById(String id) {
        return clientRepository.findById(id)
                .map(clientMapper::entityToRegisteredClient).orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return clientRepository.findByClientId(clientId)
                .map(clientMapper::entityToRegisteredClient).orElse(null);
    }
}