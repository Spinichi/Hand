package com.finger.hand_backend.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(MdcLoggingFilter.class);

    public static final String TRACE_ID = "traceId";
    public static final String USER_ID = "userId";
    public static final String METHOD = "method";
    public static final String URI = "uri";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long startNs = System.nanoTime();

        String traceId = UUID.randomUUID().toString().replace("-", "");
        String userId = resolveUserId(request);

        MDC.put(TRACE_ID, traceId);
        MDC.put(USER_ID, userId);
        MDC.put(METHOD, request.getMethod());
        MDC.put(URI, request.getRequestURI());

        response.setHeader("X-Trace-Id", traceId);

        try {
            log.info("request_start");
            filterChain.doFilter(request, response);
        } finally {
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
            int status = response.getStatus();

            MDC.put("status", String.valueOf(status));
            MDC.put("elapsedMs", String.valueOf(elapsedMs));

            if (status >= 400) {
                log.warn("request_end");
            } else {
                log.info("request_end");
            }

            MDC.remove("status");
            MDC.remove("elapsedMs");
            MDC.clear();
        }
    }

    private String resolveUserId(HttpServletRequest request) {
        String fromHeader = request.getHeader("X-User-Id");
        if (fromHeader != null && !fromHeader.isBlank()) return fromHeader;
        return "anonymous";
    }
}

