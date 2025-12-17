package com.behpardakht.oauth_server.authorization.controller.admin;

import com.behpardakht.oauth_server.authorization.model.dto.dashboard.DashboardStatsDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import com.behpardakht.oauth_server.authorization.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.ADMIN_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = ADMIN_PREFIX + "/dashboard/")
public class DashboardController {

    private final DashboardService dashboardService;

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'dashboard:view')")
    @GetMapping("stats")
    public ResponseEntity<ResponseDto<DashboardStatsDto>> getStats() {
        DashboardStatsDto response = dashboardService.getStats();
        return ResponseEntity.ok(ResponseDto.success(response));
    }
}