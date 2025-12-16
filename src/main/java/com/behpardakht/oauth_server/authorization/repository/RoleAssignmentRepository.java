package com.behpardakht.oauth_server.authorization.repository;

import com.behpardakht.oauth_server.authorization.model.entity.RoleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, Long> {

    @Query("SELECT ra FROM RoleAssignment ra " +
            "JOIN FETCH ra.role r " +
            "JOIN FETCH r.permissions " +
            "WHERE ra.user.username = :username")
    List<RoleAssignment> findByUserUsername(@Param("username") String username);

    List<RoleAssignment> findByUserId(Long userId);

    List<RoleAssignment> findByRoleId(Long roleId);


    @Query("SELECT ra FROM RoleAssignment ra " +
            "WHERE ra.user.id = :userId " +
            "AND ra.role.id = :roleId ")
    Optional<RoleAssignment> findByUserIdAndRoleId(@Param("userId") Long userId,
                                                   @Param("roleId") Long roleId);

    @Query("SELECT CASE WHEN COUNT(ra) > 0 THEN true ELSE false END FROM RoleAssignment ra " +
            "WHERE ra.user.id = :userId " +
            "AND ra.role.id = :roleId ")
    boolean existsByUserIdAndRoleId(@Param("userId") Long userId,
                                    @Param("roleId") Long roleId);

    boolean existsByRoleId(Long roleId);
}