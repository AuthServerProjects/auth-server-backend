package com.behpardakht.oauth_server.authorization.repository.filter;

import com.behpardakht.oauth_server.authorization.model.dto.user.UserClientAssignmentFilterDto;
import com.behpardakht.oauth_server.authorization.model.entity.UserClientAssignment;
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
public class UserClientAssignmentFilterSpecification implements FilterSpecification<UserClientAssignmentFilterDto, UserClientAssignment> {

    @Override
    public Specification<UserClientAssignment> toSpecification(UserClientAssignmentFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Long clientId = SecurityUtils.getCurrentClientId();
            if (clientId != null) {
                predicates.add(cb.equal(root.get("client").get("id"), clientId));
            }
            if (filter != null) {
                addBaseFilters(predicates, root, cb, filter);
                addUserClientAssignmentFilters(predicates, root, cb, filter);
            }
            return predicates.isEmpty() ?
                    cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addUserClientAssignmentFilters(List<Predicate> predicates, Root<UserClientAssignment> root,
                                                CriteriaBuilder cb, UserClientAssignmentFilterDto filter) {
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
    }
}