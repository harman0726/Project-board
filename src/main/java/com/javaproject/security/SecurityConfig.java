
package com.javaproject.security;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.jdbc.JdbcUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            JdbcUserDetailsManager users,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(users);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public CommandLineRunner initializeUsers(
            JdbcTemplate jdbcTemplate,
            JdbcUserDetailsManager users,
            PasswordEncoder passwordEncoder) {

        return args -> {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(50) NOT NULL PRIMARY KEY,
                    password VARCHAR(500) NOT NULL,
                    enabled BOOLEAN NOT NULL
                )
                """);

            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS authorities (
                    username VARCHAR(50) NOT NULL,
                    authority VARCHAR(50) NOT NULL,
                    CONSTRAINT fk_authorities_users
                        FOREIGN KEY (username) REFERENCES users(username),
                    CONSTRAINT ix_auth_username UNIQUE (username, authority)
                )
                """);

            if (!users.userExists("bugs")) {
                users.createUser(
                    org.springframework.security.core.userdetails.User
                        .withUsername("bugs")
                        .password(passwordEncoder.encode("bunny"))
                        .roles("USER")
                        .build()
                );
            }

            if (!users.userExists("daffy")) {
                users.createUser(
                    org.springframework.security.core.userdetails.User
                        .withUsername("daffy")
                        .password(passwordEncoder.encode("duck"))
                        .roles("USER", "MANAGER")
                        .build()
                );
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            LoggingAccessDeniedHandler accessDeniedHandler,
            AuthenticationProvider authenticationProvider) throws Exception {

        http
            .authenticationProvider(authenticationProvider)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/user/**").hasAnyRole("USER", "MANAGER")
                .requestMatchers("/secured/**").hasAnyRole("USER", "MANAGER")
                .requestMatchers("/manager/**").hasRole("MANAGER")
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/", "/**").permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/secured")
                .permitAll()
            )
            .logout(logout -> logout
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .exceptionHandling(exceptions ->
                exceptions.accessDeniedHandler(accessDeniedHandler)
            )
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable())
            );

        return http.build();
    }
}
