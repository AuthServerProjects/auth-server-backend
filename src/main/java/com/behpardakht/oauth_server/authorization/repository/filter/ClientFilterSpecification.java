package com.behpardakht.oauth_server.authorization.repository.filter;

import com.behpardakht.oauth_server.authorization.model.dto.client.ClientFilterDto;
import com.behpardakht.oauth_server.authorization.model.entity.Client;
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
public class ClientFilterSpecification implements FilterSpecification<ClientFilterDto, Client> {

    @Override
    public Specification<Client> toSpecification(ClientFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter != null) {
                addBaseFilters(predicates, root, cb, filter);
                addClientFilters(predicates, root, cb, filter);
            }
            return predicates.isEmpty() ?
                    cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addClientFilters(List<Predicate> predicates, Root<Client> root,
                                  CriteriaBuilder cb, ClientFilterDto filter) {
        addLongFilter(predicates, root, cb, "id", filter.getClientId());
        addStringFilter(predicates, root, cb, "clientId", filter.getClient());
    }
}