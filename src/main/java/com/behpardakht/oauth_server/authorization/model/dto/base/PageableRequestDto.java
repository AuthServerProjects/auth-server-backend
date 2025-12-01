package com.behpardakht.oauth_server.authorization.model.dto.base;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public class PageableRequestDto<F> {

    private Integer pageNumber = 0;
    private Integer pageSize = 20;
    private String sortBy = "id";
    private String sortDirection = "ASC";
    private F filters;

    public Pageable toPageable() {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Sort sort = Sort.by(direction, sortBy);

        int validPageNumber = Math.max(0, pageNumber != null ? pageNumber : 0);
        int validPageSize = (pageSize != null && pageSize > 0 && pageSize <= 100) ? pageSize : 20;

        return PageRequest.of(validPageNumber, validPageSize, sort);
    }
}