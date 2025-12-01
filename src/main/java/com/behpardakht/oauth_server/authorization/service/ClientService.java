package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.client.ClientDto;
import com.behpardakht.oauth_server.authorization.model.dto.client.ClientFilterDto;
import com.behpardakht.oauth_server.authorization.model.entity.Client;
import com.behpardakht.oauth_server.authorization.model.mapper.ClientMapper;
import com.behpardakht.oauth_server.authorization.repository.ClientFilterSpecification;
import com.behpardakht.oauth_server.authorization.repository.ClientRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ClientService {

    private final ClientMapper clientMapper;
    private final PasswordEncoder passwordEncoder;
    private final ClientRepository clientRepository;
    private final RegisteredClientRepository registeredClientRepository;
    private final ClientFilterSpecification clientFilterSpecification;

    public void insertClient(ClientDto clientDto) {
        Optional<Client> client = clientRepository.findByClientId(clientDto.getClientId());
        if (client.isPresent()) {
            throw new AlreadyExistException("Client", clientDto.getClientId());
        }
        clientDto.setRegisteredClientId(UUID.randomUUID().toString());
        clientDto.setClientSecret(passwordEncoder.encode(clientDto.getClientSecret()));
        Client entity = clientMapper.dtoToEntity(clientDto);
        clientRepository.save(entity);
    }

    public void updateClient(String clientId, ClientDto clientDto) {
        Client existingClient = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new NotFoundException("Client", "clientId", clientId));
        clientMapper.dtoToEntity(existingClient, clientDto);
        clientRepository.save(existingClient);
    }

    public ClientDto findByClientId(String clientId) {
        return clientRepository.findByClientId(clientId).map(clientMapper::entityToDto)
                .orElseThrow(() -> new NotFoundException("Client", "ClientId", clientId));
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
                .map(clientMapper::entityToRegisteredClient)
                .orElseThrow(() -> new NotFoundException("Client", "RegisterClientId", registerClientId));
    }

    public PageableResponseDto<ClientDto> findAll(PageableRequestDto<ClientFilterDto> request) {
        Specification<Client> spec = clientFilterSpecification.toSpecification(request.getFilters());
        Page<Client> page = clientRepository.findAll(spec, request.toPageable());
        List<ClientDto> responses = clientMapper.entityToDtoList(page.getContent());
        return PageableResponseDto.build(responses, page);
    }
}