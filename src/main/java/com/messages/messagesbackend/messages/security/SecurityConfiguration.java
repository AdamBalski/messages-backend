package com.messages.messagesbackend.messages.security;

import com.messages.messagesbackend.messages.util.jwt.JwtUtil;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.util.List.of;

@Configuration
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    public static final JwtUtil jwtUtil;

    static {
        String hmacSha512Key = "";
        Properties properties = new Properties();

        try (InputStream inputStream = new FileInputStream("src/main/resources/application.properties")) {
            properties.load(inputStream);
            hmacSha512Key = properties.getProperty("app.security.hmacSha512JwtKey");
        }   catch(IOException e) {
            System.out.println("Sorry, resources from application.properties file couldn't be loaded.");
            System.exit(1);
        }
        SecretKey secretKey = Keys.hmacShaKeyFor(hmacSha512Key.getBytes());
        jwtUtil = new JwtUtil(secretKey);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository = new CookieCsrfTokenRepository();
        csrfTokenRepository.setCookieHttpOnly(false);

        http
                // CORS
                .cors().and()
                // CSRF
                .csrf()
                    .csrfTokenRepository(csrfTokenRepository)
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher(
                                        "/api/authenticate",
                                        "POST")).and()
                // Sessions
                .sessionManagement().disable()
                // Filters
                .addFilterAfter(new JwtAuthFilter(userDetailsService()), AbstractPreAuthenticatedProcessingFilter.class)
                // Ant Matchers
                .authorizeRequests()
                    .antMatchers("/api/authenticate").permitAll()
                    .anyRequest().authenticated();
    }

    @Bean("authenticationManager")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    @Bean
    protected UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.builder()
                        .username("username1")
                        .password(passwordEncoder().encode("password"))
                        .authorities(of())
                        .build(),
                User.builder()
                        .username("username2")
                        .password(passwordEncoder().encode("password"))
                        .authorities(of())
                        .build()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}