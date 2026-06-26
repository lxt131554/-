package com.pm.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "requestId";
    private static final String HEADER_REQUEST_ID = "X-Request-Id";

    @Value("${app.request-log.enabled:true}")
    private boolean requestLogEnabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = request.getHeader(HEADER_REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().replace("-", "");
        }
        long start = System.currentTimeMillis();
        MDC.put(REQUEST_ID, requestId);
        response.setHeader(HEADER_REQUEST_ID, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (requestLogEnabled) {
                logger.info(String.format("%s %s -> %d (%d ms)",
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        System.currentTimeMillis() - start));
            }
            MDC.remove(REQUEST_ID);
        }
    }
}
