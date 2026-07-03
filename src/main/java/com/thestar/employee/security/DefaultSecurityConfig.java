package com.thestar.employee.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 兜底規則：除了 {@link AdminApiSecurityConfig} 明確匹配的員工登入/管理路徑外，
 * 其餘所有既有路徑（訂單、住宿、會員、金流、房型等，隊友既有功能）一律放行，
 * 不受新加入 spring-boot-starter-security 影響，維持原有的手動 session 判斷邏輯。
 */
@Configuration
public class DefaultSecurityConfig {

    @Bean
    @Order(99)
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
