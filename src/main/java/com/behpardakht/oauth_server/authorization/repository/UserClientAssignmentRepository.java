package com.behpardakht.oauth_server.authorization.repository;

import com.behpardakht.oauth_server.authorization.model.entity.Client;
import com.behpardakht.oauth_server.authorization.model.entity.UserClientAssignment;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserClientAssignmentRepository extends JpaRepository<UserClientAssignment, Long> {
    Optional<UserClientAssignment> findByUserAndClient(Users user, Client client);

    Optional<UserClientAssignment> findByUserAndClient_Id(Users user, Long clientId);

    Optional<UserClientAssignment> findByUserAndClientClientId(Users user, String clientId);

    List<UserClientAssignment> findByClient_Id(Long clientId);

    boolean existsByUserAndClient(Users user, Client client);
}