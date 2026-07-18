package com.thestar.employee.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * 後台員工 API / 頁面專用的 SecurityFilterChain。
 *
 * 登入採真正的表單登入（比照 security.md guideline 的 SecurityFilterChain 表單登入設計），
 * 而非 AJAX/JSON 登入：GET /thestar/admin/login 顯示登入頁，
 * POST /thestar/admin/login 由 Spring Security 的登入 filter 直接處理帳密驗證。
 */
@Configuration
@EnableWebSecurity
public class AdminApiSecurityConfig {

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public AuthenticationManager adminAuthenticationManager(UserDetailsService employeeUserDetailsService,
                                                              PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(employeeUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain adminApiFilterChain(HttpSecurity http,
                                                     SecurityContextRepository securityContextRepository,
                                                     AuthenticationManager adminAuthenticationManager,
                                                     AdminAuthenticationSuccessHandler adminAuthenticationSuccessHandler,
                                                     RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                                                     RestAccessDeniedHandler restAccessDeniedHandler) throws Exception {
        http
                .securityMatcher(
                        "/thestar/admin", "/thestar/admin/**",
                        "/admin/restaurant/**", "/admin/shop/**",
                        "/admin/members/**", "/admin/coupons/**",
                        "/room/**", "/roomtype/**", "/roomtypephoto/delete/**",
                        "/find/admin/room")
                .authenticationManager(adminAuthenticationManager)
                .csrf(csrf -> csrf.disable())
                .sessionManagement((SessionManagementConfigurer<HttpSecurity> sm) ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .securityContext(ctx -> ctx.securityContextRepository(securityContextRepository))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/thestar/admin/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/admin/restaurant/menu/DBGifReader").permitAll()
                        .requestMatchers("/thestar/admin", "/thestar/admin/home",
                                "/thestar/admin/me", "/thestar/admin/logout").authenticated()
                        .requestMatchers("/thestar/admin/order/**", "/thestar/admin/stayrecord/**",
                                "/thestar/admin/refund/**", "/room/**", "/roomtype/**",
                                "/roomtypephoto/delete/**", "/find/admin/room")
                            .hasAnyAuthority(RoleCodes.FRONT_DESK, RoleCodes.SUPER_ADMIN)
                        .requestMatchers("/admin/restaurant/**")
                            .hasAnyAuthority(RoleCodes.RESTAURANT_STAFF, RoleCodes.SUPER_ADMIN)
                        .requestMatchers("/admin/shop/**")
                            .hasAnyAuthority(RoleCodes.PRODUCT_ADMIN, RoleCodes.SUPER_ADMIN)
                        .requestMatchers("/thestar/admin/content/**")
                            .hasAnyAuthority(RoleCodes.CONTENT_ADMIN, RoleCodes.SUPER_ADMIN)
                        .requestMatchers("/admin/members/**", "/admin/coupons/**")
                            .hasAnyAuthority(RoleCodes.MEMBER_ADMIN, RoleCodes.SUPER_ADMIN)
                        .requestMatchers("/thestar/admin/access",
                                "/thestar/admin/employee/*/roles",
                                "/thestar/admin/employee/*/delete",
                                "/thestar/admin/role/**", "/thestar/admin/permission/**")
                            .hasAuthority(RoleCodes.SUPER_ADMIN)
                        .requestMatchers("/thestar/admin/employee/**")
                            .hasAnyAuthority(RoleCodes.HR, RoleCodes.SUPER_ADMIN)
                        .anyRequest().denyAll()
                )
                .formLogin(form -> form
                        .loginPage("/thestar/admin/login")
                        .loginProcessingUrl("/thestar/admin/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(adminAuthenticationSuccessHandler)
                        .failureUrl("/thestar/admin/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/thestar/admin/logout")
                        .logoutSuccessUrl("/thestar/admin/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
