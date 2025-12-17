package com.behpardakht.oauth_server.authorization.repository.filter;

import com.behpardakht.oauth_server.authorization.model.dto.role.PermissionFilterDto;
import com.behpardakht.oauth_server.authorization.model.entity.Permission;
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
public class PermissionFilterSpecification implements FilterSpecification<PermissionFilterDto, Permission> {

    @Override
    public Specification<Permission> toSpecification(PermissionFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Long clientId = SecurityUtils.getCurrentClientId();
            if (clientId != null) {
                predicates.add(cb.equal(root.get("client").get("id"), clientId));
            }

            if (filter != null) {
                addBaseFilters(predicates, root, cb, filter);
                addPermissionFilters(predicates, root, cb, filter);
            }

            return predicates.isEmpty() ?
                    cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addPermissionFilters(List<Predicate> predicates, Root<Permission> root,
                                      CriteriaBuilder cb, PermissionFilterDto filter) {
        addStringFilter(predicates, root, cb, "name", filter.getName());
        addStringFilter(predicates, root, cb, "description", filter.getDescription());
    }
}