package com.behpardakht.oauth_server.authorization.repository;

import com.behpardakht.oauth_server.authorization.model.entity.Client;
import com.behpardakht.oauth_server.authorization.model.entity.UserClientAssignment;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserClientAssignmentRepository extends JpaRepository<UserClientAssignment, Long> {
    Optional<UserClientAssignment> findByUserAndClient(Users user, Client client);

    Optional<UserClientAssignment> findByUserAndClient_Id(Users user, Long clientId);

    Optional<UserClientAssignment> findByUserAndClientClientId(Users user, String clientId);

    List<UserClientAssignment> findByClient_Id(Long clientId);

    boolean existsByUserAndClient(Users user, Client client);

    boolean existsByUserUsernameAndClientId(String username, Long clientId);

    @Query("SELECT DISTINCT uca FROM UserClientAssignment uca " +
            "LEFT JOIN FETCH uca.client " +
            "LEFT JOIN FETCH uca.userRoleAssignments ura " +
            "LEFT JOIN FETCH ura.role r " +
            "LEFT JOIN FETCH r.permissions " +
            "WHERE uca.user.username = :username")
    List<UserClientAssignment> findByUserUsernameWithRolesAndPermissions(@Param("username") String username);

}