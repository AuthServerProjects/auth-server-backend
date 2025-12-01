package com.behpardakht.oauth_server.authorization.repository;

import com.behpardakht.oauth_server.authorization.model.dto.auth.AuthorizationFilterDto;
import com.behpardakht.oauth_server.authorization.model.entity.Authorizations;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AuthorizationFilterSpecification implements FilterSpecification<AuthorizationFilterDto, Authorizations> {

    @Override
    public Specification<Authorizations> toSpecification(AuthorizationFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter != null) {
                addBaseFilters(predicates, root, cb, filter);
                addAuthorizationFilters(predicates, root, cb, filter);
            }
            return predicates.isEmpty() ?
                    cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addAuthorizationFilters(List<Predicate> predicates, Root<Authorizations> root,
                                         CriteriaBuilder cb, AuthorizationFilterDto filter) {
        addStringFilter(predicates, root, cb, "principalName", filter.getPrincipalName());
        addStringFilter(predicates, root, cb, "registeredClientId", filter.getRegisteredClientId());
    }
}