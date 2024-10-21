package com.behpardakht.side_pay.auth.security;

import com.behpardakht.side_pay.auth.model.entity.Client;
import com.behpardakht.side_pay.auth.model.mapper.ClientMapper;
import com.behpardakht.side_pay.auth.repository.ClientRepository;
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
        Client client = clientMapper.toEntity(registeredClient);
        client.setClientSecret(passwordEncoder.encode(client.getClientSecret()));
        clientRepository.save(client);
    }

    @Override
    public RegisteredClient findById(String id) {
        return clientRepository.findById(id)
                .map(clientMapper::toRegisteredClient).orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return clientRepository.findByClientId(clientId)
                .map(clientMapper::toRegisteredClient).orElse(null);
    }
}