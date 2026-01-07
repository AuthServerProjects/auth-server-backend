package com.behpardakht.oauth_server.authorization.repository.filter;

import com.behpardakht.oauth_server.authorization.model.dto.role.UserRoleFilterDto;
import com.behpardakht.oauth_server.authorization.model.entity.UserRole;
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
public class UserRoleFilterSpecification implements FilterSpecification<UserRoleFilterDto, UserRole> {

    @Override
    public Specification<UserRole> toSpecification(UserRoleFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter != null) {
                addBaseFilters(predicates, root, cb, filter);
                addUserRoleFilters(predicates, root, cb, filter);
            }
            return predicates.isEmpty() ?
                    cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addUserRoleFilters(List<Predicate> predicates, Root<UserRole> root,
                                    CriteriaBuilder cb, UserRoleFilterDto filter) {
        addEntityFilter(predicates, root, cb, "role", "id", filter.getRoleId());
        addEntityFilter(predicates, root, cb, "role", "name", filter.getRoleName());
        addJoinFilter(predicates, root, cb, "role", "client", "id", filter.getClientId());
        addEntityFilter(predicates, root, cb, "userClient", "id", filter.getUserClientId());
        addJoinFilter(predicates, root, cb, "userClient", "client", "id", filter.getClientId());
        addJoinFilter(predicates, root, cb, "userClient", "user", "username", filter.getUsername());
    }
}