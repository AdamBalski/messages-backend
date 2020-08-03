package com.messages.messagesbackend.messages.security;

import com.messages.messagesbackend.messages.util.jwt.JwtUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;

    private final Converter<UserDetails, Authentication> userDetailsToAuthenticationConverter =
            userDetails -> new UsernamePasswordAuthenticationToken(
                    userDetails.getUsername(),
                    userDetails.getPassword(),
                    userDetails.getAuthorities()
            );

    public JwtAuthFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(null);
        String authorizationHeader = request.getHeader("authorization");

        if(authorizationHeader == null || (!authorizationHeader.startsWith("Bearer "))) {
            filterChain.doFilter(request, response);
            return;
        }

        JwtUtil jwtUtil = SecurityConfiguration.jwtUtil;
        String token = authorizationHeader.replaceFirst("Bearer ", "");

        // It also verifies JWT, check JavaDoc for JwtUtil's extract(String) method and for what it's calling;
        String username = jwtUtil.getSubjectAndVerify(token);

        if (username != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);;
            Authentication authentication = userDetailsToAuthenticationConverter.convert(userDetails);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}