package com.behpardakht.oauth_server.authorization.repository;

import com.behpardakht.oauth_server.authorization.model.entity.AuditLog;
import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    Long countByActionAndSuccessAndCreatedAtAfter(AuditAction action, Boolean success, Instant after);

    Long countByActionInAndCreatedAtAfter(List<AuditAction> actions, Instant after);

    @Query("SELECT a.ipAddress, COUNT(a) as cnt FROM AuditLog a " +
            "WHERE a.success = false AND a.createdAt > :after " +
            "GROUP BY a.ipAddress ORDER BY cnt DESC LIMIT 10")
    List<Object[]> findTopFailedIps(@Param("after") Instant after);

    List<AuditLog> findTop20ByOrderByCreatedAtDesc();
}