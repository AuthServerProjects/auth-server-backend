package com.behpardakht.oauth_server.authorization.repository.filter;

import com.behpardakht.oauth_server.authorization.model.dto.role.RoleFilterDto;
import com.behpardakht.oauth_server.authorization.model.entity.Role;
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
public class RoleFilterSpecification implements FilterSpecification<RoleFilterDto, Role> {

    @Override
    public Specification<Role> toSpecification(RoleFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Long clientId = SecurityUtils.getCurrentClientId();
            if (clientId != null) {
                predicates.add(cb.equal(root.get("client").get("id"), clientId));
            }

            if (filter != null) {
                addBaseFilters(predicates, root, cb, filter);
                addRoleFilters(predicates, root, cb, filter);
            }

            return predicates.isEmpty() ?
                    cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addRoleFilters(List<Predicate> predicates, Root<Role> root,
                                CriteriaBuilder cb, RoleFilterDto filter) {
        addStringFilter(predicates, root, cb, "name", filter.getName());
    }
}