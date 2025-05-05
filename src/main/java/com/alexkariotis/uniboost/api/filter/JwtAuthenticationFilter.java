package com.alexkariotis.uniboost.api.filter;

import com.alexkariotis.uniboost.api.filter.utils.JwtUtils;
import com.alexkariotis.uniboost.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson mapper

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        System.out.println();
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String jwt = null;
        String username = null;

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                username = jwtUtils.extractUsername(jwt);
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtils.isTokenValid(jwt, userDetails) && tokenService.isTokenValid(jwt)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            filterChain.doFilter(request, response);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            writeProblemDetailResponse(response, HttpStatus.UNAUTHORIZED, "JWT token is expired");
        } catch (io.jsonwebtoken.JwtException e) {
            writeProblemDetailResponse(response, HttpStatus.UNAUTHORIZED, "Invalid JWT token");
        }
    }

    private void writeProblemDetailResponse(HttpServletResponse response, HttpStatus status, String detail) throws IOException {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle("Authentication Failed");

        response.setStatus(status.value());
        response.setContentType("application/problem+json");
        response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
    }
}
