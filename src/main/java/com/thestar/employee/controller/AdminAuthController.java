package com.thestar.employee.controller;

import com.thestar.employee.dto.EmployeeViewDTO;
import com.thestar.employee.security.EmployeeUserDetails;
import com.thestar.employee.security.AdminApiSecurityConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登入/登出改由 {@link AdminApiSecurityConfig}
 * 的表單登入（formLogin）與 logout filter 直接處理，此處僅保留取得目前登入員工資訊的端點。
 */
@RestController
@RequestMapping("/thestar/admin")
public class AdminAuthController {

    @GetMapping("/me")
    public ResponseEntity<EmployeeViewDTO> me(@AuthenticationPrincipal EmployeeUserDetails principal) {
        return ResponseEntity.ok(EmployeeViewDTO.from(principal.getEmployee()));
    }
}
