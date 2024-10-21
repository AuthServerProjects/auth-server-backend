package com.behpardakht.side_pay.auth.repository;

import com.behpardakht.side_pay.auth.model.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {
    Optional<Client> findByClientId(String clientId);
}