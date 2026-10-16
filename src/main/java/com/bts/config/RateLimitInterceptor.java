package com.bts.config;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final ConcurrentHashMap<String, Deque<Long>> requests =
            new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        int maxRequests;
        long windowMillis;

        boolean authEndpoint =
                (uri.endsWith("/auth/register")
                || uri.endsWith("/auth/login"))
                && "POST".equalsIgnoreCase(method);

        boolean productWrite =
                uri.matches(".*/products(?:/[^/]+)?$")
                && ("POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method));

        if (authEndpoint) {
            maxRequests = 3;
            windowMillis = 60_000;
        } else if (productWrite) {
            maxRequests = 1;
            windowMillis = 5_000;
        } else {
            return true;
        }

        String clientIp = request.getRemoteAddr();
        String key = clientIp + ":" + method + ":" + uri;
        long now = System.currentTimeMillis();

        Deque<Long> timestamps = requests.computeIfAbsent(
                key, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty()
                    && now - timestamps.peekFirst() >= windowMillis) {
                timestamps.removeFirst();
            }

            if (timestamps.size() >= maxRequests) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"error\":\"Too many requests. Please try again later.\"}"
                );
                return false;
            }

            timestamps.addLast(now);
        }

        return true;
    }
}