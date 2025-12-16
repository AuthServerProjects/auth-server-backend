package com.behpardakht.oauth_server.authorization.repository.filter;

import com.behpardakht.oauth_server.authorization.model.dto.user.UserFilterDto;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserFilterSpecification implements FilterSpecification<UserFilterDto, Users> {

    @Override
    public Specification<Users> toSpecification(UserFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter != null) {
                addBaseFilters(predicates, root, cb, filter);
                addClientFilter(predicates, root, cb, filter.getClientId(), "clientAssignments");
                addUserFilters(predicates, root, cb, filter);
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addUserFilters(List<Predicate> predicates, Root<Users> root,
                                CriteriaBuilder cb, UserFilterDto filter) {
        addStringFilter(predicates, root, cb, "username", filter.getUsername());
        addStringFilter(predicates, root, cb, "phoneNumber", filter.getPhoneNumber());
    }
}