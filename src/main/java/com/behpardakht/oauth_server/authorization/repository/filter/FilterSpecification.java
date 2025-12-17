package com.behpardakht.oauth_server.authorization.repository.filter;

import com.behpardakht.oauth_server.authorization.model.dto.base.BaseFilterDto;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface FilterSpecification<F extends BaseFilterDto, T> {

    Specification<T> toSpecification(F filter);

    default void addBaseFilters(List<Predicate> predicates, Root<T> root,
                                CriteriaBuilder cb, BaseFilterDto filter) {
        if (filter.getIsEnabled() != null) {
            addBooleanFilter(predicates, root, cb, "isEnabled", filter.getIsEnabled());
        }
    }

    default void addClientFilter(List<Predicate> predicates, Root<T> root,
                                 CriteriaBuilder cb, Long clientId, String joinField) {
        if (clientId != null) {
            Join<Object, Object> assignmentJoin = root.join(joinField);
            predicates.add(cb.equal(assignmentJoin.get("client").get("id"), clientId));
            predicates.add(cb.isTrue(assignmentJoin.get("isActive")));
        }
    }

    default void addIntegerFilter(List<Predicate> predicates, Root<T> root,
                                  CriteriaBuilder cb, String fieldName, Integer value) {
        if (value != null) {
            predicates.add(cb.equal(root.get(fieldName), value));
        }
    }

    default void addLongFilter(List<Predicate> predicates, Root<T> root,
                               CriteriaBuilder cb, String fieldName, Long value) {
        if (value != null) {
            predicates.add(cb.equal(root.get(fieldName), value));
        }
    }

    default void addEntityIdFilter(List<Predicate> predicates, Root<T> root,
                                   CriteriaBuilder cb, String fieldName, Long id) {
        if (id != null) {
            predicates.add(cb.equal(root.get(fieldName).get("id"), id));
        }
    }

    default void addStringFilter(List<Predicate> predicates, Root<T> root,
                                 CriteriaBuilder cb, String fieldName, String value) {
        if (value != null && !value.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get(fieldName)),
                    "%" + value.toLowerCase().trim() + "%"));
        }
    }

    default void addStringExactFilter(List<Predicate> predicates, Root<T> root,
                                      CriteriaBuilder cb, String fieldName, String value) {
        if (value != null && !value.isBlank()) {
            predicates.add(cb.equal(root.get(fieldName), value.trim()));
        }
    }

    default void addBooleanFilter(List<Predicate> predicates, Root<T> root,
                                  CriteriaBuilder cb, String fieldName, Boolean value) {
        if (value != null) {
            predicates.add(cb.equal(root.get(fieldName), value));
        }
    }

    default void addDateRangeFilter(List<Predicate> predicates, Root<T> root,
                                    CriteriaBuilder cb, String fieldName,
                                    LocalDateTime from, LocalDateTime to) {
        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(fieldName), from));
        }
        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(fieldName), to));
        }
    }

    default void addEnumFilter(List<Predicate> predicates, Root<T> root,
                               CriteriaBuilder cb, String fieldName, Enum<?> value) {
        if (value != null) {
            predicates.add(cb.equal(root.get(fieldName), value));
        }
    }

    default void addInstantRangeFilter(List<Predicate> predicates, Root<T> root,
                                       CriteriaBuilder cb, String fieldName,
                                       Instant from, Instant to) {
        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(fieldName), from));
        }
        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(fieldName), to));
        }
    }


    default void addEntityFilter(List<Predicate> predicates, Root<T> root,
                                 CriteriaBuilder cb, String fieldName, Object dto) {
        if (dto != null) {
            try {
                Method getIdMethod = dto.getClass().getMethod("getId");
                Object id = getIdMethod.invoke(dto);
                if (id != null) {
                    predicates.add(cb.equal(root.get(fieldName).get("id"), id));
                }
            } catch (Exception e) {
                System.err.println("Error accessing ID field for " + fieldName + ": " + e.getMessage());
            }
        }
    }
}