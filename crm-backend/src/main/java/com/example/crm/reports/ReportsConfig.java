package com.example.crm.reports;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
class ReportsConfig {

    @Bean
    @Order(1)
    SecurityFilterChain internalSecurity(
            HttpSecurity http,
            PasswordEncoder encoder,
            @Value("${crm.reports.username}") String username,
            @Value("${crm.reports.password}") String password) throws Exception {
        var users = new InMemoryUserDetailsManager();
        if (!password.isEmpty()) {
            if (username.isBlank() || password.length() < 12) {
                throw new IllegalStateException("Configure CRM_REPORTS_USERNAME e CRM_REPORTS_PASSWORD com pelo menos 12 caracteres.");
            }
            users.createUser(User.withUsername(username).password(encoder.encode(password)).roles("REPORTS").build());
        }
        var provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(encoder);
        return http
                .securityMatcher("/internal/**")
                .authenticationManager(new ProviderManager(provider))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.GET, "/internal/leads/stage-counts").hasRole("REPORTS")
                        .anyRequest().denyAll())
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(errors -> errors.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .build();
    }

    @Bean
    RestClient reportsClient(
            @Value("${crm.reports.url}") String url,
            @Value("${crm.reports.username}") String username,
            @Value("${crm.reports.password}") String password) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder().baseUrl(url).requestFactory(factory)
                .defaultHeaders(headers -> headers.setBasicAuth(username, password))
                .build();
    }
}
