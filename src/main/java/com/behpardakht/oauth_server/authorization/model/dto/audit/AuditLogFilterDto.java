package com.behpardakht.oauth_server.authorization.model.dto.audit;

import com.behpardakht.oauth_server.authorization.model.dto.base.BaseFilterDto;
import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuditLogFilterDto extends BaseFilterDto {
    private AuditAction action;
    private String username;
    private Boolean success;
    private Instant fromDate;
    private Instant toDate;
}