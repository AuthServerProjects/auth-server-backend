package com.behpardakht.oauth_server.authorization.repository.filter;

import com.behpardakht.oauth_server.authorization.model.dto.user.UserClientFilterDto;
import com.behpardakht.oauth_server.authorization.model.entity.UserClient;
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
public class UserClientFilterSpecification implements FilterSpecification<UserClientFilterDto, UserClient> {

    @Override
    public Specification<UserClient> toSpecification(UserClientFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter != null) {
                addBaseFilters(predicates, root, cb, filter);
                addUserClientFilters(predicates, root, cb, filter);
            }
            return predicates.isEmpty() ?
                    cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addUserClientFilters(List<Predicate> predicates, Root<UserClient> root,
                                      CriteriaBuilder cb, UserClientFilterDto filter) {
        if (filter.getUsername() != null && !filter.getUsername().isBlank()) {
            predicates.add(cb.like(
                    cb.lower(root.get("user").get("username")),
                    "%" + filter.getUsername().toLowerCase().trim() + "%"
            ));
        }
        if (filter.getPhoneNumber() != null && !filter.getPhoneNumber().isBlank()) {
            predicates.add(cb.like(
                    root.get("user").get("phoneNumber"),
                    "%" + filter.getPhoneNumber().trim() + "%"
            ));
        }
        addBooleanFilter(predicates, root, cb, "isAccountNonLocked", filter.getIsAccountNonLocked());
        addBooleanFilter(predicates, root, cb, "isAccountNonExpired", filter.getIsAccountNonExpired());
        addBooleanFilter(predicates, root, cb, "isCredentialsNonExpired", filter.getIsCredentialsNonExpired());
        addEntityFilter(predicates, root, cb, "client", "id", filter.getClientId());
    }
}