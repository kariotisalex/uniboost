package com.alexkariotis.uniboost.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@WebFilter(filterName = "Logging", urlPatterns = "/")
@Order(value = Ordered.HIGHEST_PRECEDENCE)
@Component
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {


        LocalDateTime start = LocalDateTime.now();
        try {
            filterChain.doFilter(request, response);
            if (response.getStatus() >= 400) {
                log.error("Request {} {} - {} - Failed - {}ms",
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        ChronoUnit.MILLIS.between(start, LocalDateTime.now()));
            } else {
                log.info("Request {} {} - {} - Succeed - {}ms",
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        ChronoUnit.MILLIS.between(start, LocalDateTime.now()));
            }
        } catch (Exception e) {
            log.error("Request {} {} - {} - Failed - {}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    ChronoUnit.MILLIS.between(start, LocalDateTime.now()));
            throw e;
        }
    }
}
