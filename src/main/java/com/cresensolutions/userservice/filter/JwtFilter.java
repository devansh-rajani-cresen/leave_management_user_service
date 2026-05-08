// Runs on every request
package com.cresensolutions.userservice.filter;

import com.cresensolutions.userservice.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static com.cresensolutions.userservice.common.UserConstants.AUTH_HEADER;
import static com.cresensolutions.userservice.common.UserConstants.HEADER_STARTING;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(AUTH_HEADER);

        String token = null;
        String username = null;
        String role = null;

        if (authHeader != null && authHeader.startsWith(HEADER_STARTING)) {
            token = authHeader.substring(7);

            try {
                if (jwtUtil.validateToken(token)) {

                    username = jwtUtil.extractUsername(token);
                    role = jwtUtil.extractRole(token);

                    // Validate and set authentication
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        username,
                                        null,
                                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                                );

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }

            } catch (Exception e) {
                // Token invalid / expired
                log.info("JWT Error: {} ", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}