package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.oauth_server.authorization.model.dto.client.ClientDto;
import com.behpardakht.oauth_server.authorization.model.entity.Client;
import com.behpardakht.oauth_server.authorization.model.mapper.ClientMapper;
import com.behpardakht.oauth_server.authorization.repository.ClientRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ClientService {

    private final ClientMapper clientMapper;
    private final ClientRepository clientRepository;
    private final RegisteredClientRepository registeredClientRepository;

    @PreAuthorize("hasAnyRole('ADMIN')")
    public void insertClient(ClientDto clientDto) {
        Optional<Client> client =
                clientRepository.findByClientId(clientDto.getClientId());
        if (client.isPresent()) {
            throw new AlreadyExistException("Client", clientDto.getClientId());
        } else {
            clientDto.setRegisteredClientId(UUID.randomUUID().toString());
            RegisteredClient registeredClient = clientMapper.toRegisteredClient(clientDto);
            registeredClientRepository.save(registeredClient);
        }
    }

    public ClientDto findByClientId(String clientId) {
        RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId); //TODO change repo and add mapper
        if (registeredClient == null) {
            throw new NotFoundException("Client", "ClientId", clientId);
        }
        return clientMapper.toDto(registeredClient);
    }

    public RegisteredClient findRegisteredClientByClientId(String clientId) {
        RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            throw new NotFoundException("Client", "ClientId", clientId);
        }
        return registeredClient;
    }

    public RegisteredClient findRegisteredClientByRegisterClientId(String registerClientId) {
        return clientRepository.findByRegisteredClientId(registerClientId)
                .map(clientMapper::toRegisteredClient)
                .orElseThrow(() -> new NotFoundException("Client", "RegisterClientId", registerClientId));

    }
}