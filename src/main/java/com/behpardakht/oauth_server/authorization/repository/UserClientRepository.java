package com.behpardakht.oauth_server.authorization.repository;

import com.behpardakht.oauth_server.authorization.model.entity.Client;
import com.behpardakht.oauth_server.authorization.model.entity.UserClient;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserClientRepository extends JpaRepository<UserClient, Long>, JpaSpecificationExecutor<UserClient> {
    Optional<UserClient> findByUserAndClient(Users user, Client client);

    Optional<UserClient> findByUserAndClient_Id(Users user, Long clientId);

    Optional<UserClient> findByUserAndClientClientId(Users user, String clientId);

    List<UserClient> findByClient_Id(Long clientId);

    boolean existsByUserAndClient(Users user, Client client);

    boolean existsByUserUsernameAndClientId(String username, Long clientId);

    @Query("SELECT DISTINCT uca FROM UserClient uca " +
            "LEFT JOIN FETCH uca.client " +
            "LEFT JOIN FETCH uca.userRoles ura " +
            "LEFT JOIN FETCH ura.role r " +
            "LEFT JOIN FETCH r.permissions " +
            "WHERE uca.user.username = :username")
    List<UserClient> findByUserUsernameWithRolesAndPermissions(@Param("username") String username);

    Long countByClientIdAndIsEnabledTrue(Long clientId);
}