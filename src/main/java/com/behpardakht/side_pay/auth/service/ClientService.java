package com.behpardakht.side_pay.auth.service;

import com.behpardakht.side_pay.auth.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.side_pay.auth.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.side_pay.auth.model.dto.ClientDto;
import com.behpardakht.side_pay.auth.model.entity.Client;
import com.behpardakht.side_pay.auth.model.mapper.ClientMapper;
import com.behpardakht.side_pay.auth.repository.ClientRepository;
import lombok.AllArgsConstructor;
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
        RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            throw new NotFoundException("Client", "ClientId", clientId);
        }
        return clientMapper.toDto(registeredClient);
    }
}