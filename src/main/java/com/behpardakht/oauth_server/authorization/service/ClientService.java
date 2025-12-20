package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.aspect.Auditable;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.client.ClientDropdownDto;
import com.behpardakht.oauth_server.authorization.model.dto.client.ClientDto;
import com.behpardakht.oauth_server.authorization.model.dto.client.ClientFilterDto;
import com.behpardakht.oauth_server.authorization.model.entity.Client;
import com.behpardakht.oauth_server.authorization.model.entity.UserClient;
import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import com.behpardakht.oauth_server.authorization.model.mapper.ClientMapper;
import com.behpardakht.oauth_server.authorization.repository.ClientRepository;
import com.behpardakht.oauth_server.authorization.repository.UserClientRepository;
import com.behpardakht.oauth_server.authorization.repository.filter.ClientFilterSpecification;
import com.behpardakht.oauth_server.authorization.util.GeneralUtil;
import com.behpardakht.oauth_server.authorization.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientMapper clientMapper;
    private final PasswordEncoder passwordEncoder;
    private final ClientRepository clientRepository;
    private final RegisteredClientRepository registeredClientRepository;
    private final ClientFilterSpecification clientFilterSpecification;
    private final UserClientRepository userClientRepository;

    @Auditable(action = AuditAction.CLIENT_CREATED, clientId = "#clientDto.clientId")
    public String save(ClientDto clientDto) {
        if (clientRepository.existsByClientId(clientDto.getClientId())) {
            throw new AlreadyExistException("Client", clientDto.getClientId());
        }
        clientDto.setRegisteredClientId(UUID.randomUUID().toString());
        String secret = GeneralUtil.generateRandomPassword();
        clientDto.setClientSecret(passwordEncoder.encode(secret));
        insert(clientMapper.dtoToEntity(clientDto));
        return secret;
    }

    @Auditable(action = AuditAction.CLIENT_UPDATED, clientId = "#clientId")
    public void update(String clientId, ClientDto clientDto) {
        Client existingClient = findByClientId(clientId);
        clientMapper.dtoToEntity(existingClient, clientDto);
        insert(existingClient);
    }

    public Client findByClientId(String clientId) {
        return clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new NotFoundException("Client", "clientId", clientId));
    }

    public Optional<Client> findByClientIdOptional(String clientId) {
        return clientRepository.findByClientId(clientId);
    }

    public ClientDto findDtoByClientId(String clientId) {
        Client client = findByClientId(clientId);
        return clientMapper.entityToDto(client);
    }

    public Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client", "id", id.toString()));
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
                .orElseThrow(() -> new NotFoundException("Client", "registeredClientId", registerClientId));
    }

    public PageableResponseDto<ClientDto> findAll(PageableRequestDto<ClientFilterDto> request) {
        Specification<Client> spec = clientFilterSpecification.toSpecification(request.getFilters());
        Page<Client> page = clientRepository.findAll(spec, request.toPageable());
        List<ClientDto> responses = clientMapper.entityToDtoList(page.getContent());
        return PageableResponseDto.build(responses, page);
    }

    public List<ClientDropdownDto> getMyClients() {
        List<Client> adminClientList;
        if (SecurityUtils.isSuperAdmin()) {
            adminClientList = clientRepository.findAll();
        } else {
            String username = SecurityUtils.getCurrentUsername();
            List<UserClient> userClientList = userClientRepository.findByUserUsernameWithRolesAndPermissions(username);
            adminClientList = userClientList.stream().map(UserClient::getClient).toList();
        }
        return clientMapper.entityToDropdownDtoList(adminClientList);
    }

    @Auditable(action = AuditAction.SECRET_REGENERATED, clientId = "#clientId")
    public String regenerateSecret(String clientId) {
        Client client = findByClientId(clientId);
        String secret = GeneralUtil.generateRandomPassword();
        client.setClientSecret(passwordEncoder.encode(secret));
        insert(client);
        return secret;
    }

    @Auditable(action = AuditAction.STATUS_CHANGED, clientId = "#clientId")
    public Boolean toggleStatus(String clientId) {
        Client client = findByClientId(clientId);
        client.setIsEnabled(!Boolean.TRUE.equals(client.getIsEnabled()));
        insert(client);
        return client.getIsEnabled();
    }

    public Client insert(Client entity) {
        return clientRepository.save(entity);
    }
}