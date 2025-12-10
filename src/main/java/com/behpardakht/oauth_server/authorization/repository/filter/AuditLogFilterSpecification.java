package com.behpardakht.oauth_server.authorization.repository.filter;

import com.behpardakht.oauth_server.authorization.model.dto.audit.AuditLogFilterDto;
import com.behpardakht.oauth_server.authorization.model.entity.AuditLog;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AuditLogFilterSpecification implements FilterSpecification<AuditLogFilterDto, AuditLog> {

    @Override
    public Specification<AuditLog> toSpecification(AuditLogFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter != null) {
                addAuditFilters(predicates, root, cb, filter);
            }
            return predicates.isEmpty() ?
                    cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addAuditFilters(List<Predicate> predicates, Root<AuditLog> root,
                                 CriteriaBuilder cb, AuditLogFilterDto filter) {
        addEnumFilter(predicates, root, cb, "action", filter.getAction());
        addStringFilter(predicates, root, cb, "username", filter.getUsername());
        addStringExactFilter(predicates, root, cb, "clientId", filter.getClientId());
        addBooleanFilter(predicates, root, cb, "success", filter.getSuccess());
        addInstantRangeFilter(predicates, root, cb, "createdAt", filter.getFromDate(), filter.getToDate());
    }
}