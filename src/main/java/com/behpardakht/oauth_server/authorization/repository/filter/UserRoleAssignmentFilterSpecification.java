package com.behpardakht.oauth_server.authorization.repository.filter;

import com.behpardakht.oauth_server.authorization.model.dto.role.UserRoleAssignmentFilterDto;
import com.behpardakht.oauth_server.authorization.model.entity.UserRoleAssignment;
import com.behpardakht.oauth_server.authorization.util.SecurityUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class UserRoleAssignmentFilterSpecification implements FilterSpecification<UserRoleAssignmentFilterDto, UserRoleAssignment> {

    @Override
    public Specification<UserRoleAssignment> toSpecification(UserRoleAssignmentFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by current client (critical for security)
            Long clientId = SecurityUtils.getCurrentClientId();
            if (clientId != null) {
                predicates.add(cb.equal(root.get("userClientAssignment").get("client").get("id"), clientId));
            }

            if (filter != null) {
                addBaseFilters(predicates, root, cb, filter);
                addUserRoleAssignmentFilters(predicates, root, cb, filter);
            }

            return predicates.isEmpty() ?
                    cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addUserRoleAssignmentFilters(List<Predicate> predicates, Root<UserRoleAssignment> root,
                                              CriteriaBuilder cb, UserRoleAssignmentFilterDto filter) {
        // Filter by username (through userClientAssignment -> user -> username)
        if (filter.getUsername() != null && !filter.getUsername().isBlank()) {
            predicates.add(cb.like(
                    cb.lower(root.get("userClientAssignment").get("user").get("username")),
                    "%" + filter.getUsername().toLowerCase().trim() + "%"
            ));
        }

        // Filter by role name
        if (filter.getRoleName() != null && !filter.getRoleName().isBlank()) {
            predicates.add(cb.like(
                    cb.lower(root.get("role").get("name")),
                    "%" + filter.getRoleName().toLowerCase().trim() + "%"
            ));
        }

        // Filter by roleId (exact match)
        if (filter.getRoleId() != null) {
            predicates.add(cb.equal(root.get("role").get("id"), filter.getRoleId()));
        }

        // Filter by userClientAssignmentId (exact match)
        if (filter.getUserClientAssignmentId() != null) {
            predicates.add(cb.equal(root.get("userClientAssignment").get("id"), filter.getUserClientAssignmentId()));
        }
    }
}