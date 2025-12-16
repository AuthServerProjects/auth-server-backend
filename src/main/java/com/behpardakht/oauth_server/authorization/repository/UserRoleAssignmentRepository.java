package com.behpardakht.oauth_server.authorization.repository;

import com.behpardakht.oauth_server.authorization.model.entity.UserRoleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignment, Long> {
    boolean existsByUserClientAssignmentIdAndRoleId(Long userClientAssignmentId, Long roleId);

    boolean existsByRoleId(Long roleId);

    Optional<UserRoleAssignment> findByUserClientAssignmentIdAndRoleId(Long userClientAssignmentId, Long roleId);

    List<UserRoleAssignment> findByUserClientAssignmentId(Long userClientAssignmentId);

    List<UserRoleAssignment> findByRoleId(Long roleId);

    @Query("SELECT ra FROM UserRoleAssignment ra " +
            "WHERE ra.userClientAssignment.user.username = :username " +
            "AND ra.userClientAssignment.client.id = :clientId")
    List<UserRoleAssignment> findByUsernameAndClientId(@Param("username") String username,
                                                       @Param("clientId") Long clientId);

    @Query("SELECT ra FROM UserRoleAssignment ra " +
            "WHERE ra.userClientAssignment.client.id = :clientId")
    List<UserRoleAssignment> findByClientId(@Param("clientId") Long clientId);
}