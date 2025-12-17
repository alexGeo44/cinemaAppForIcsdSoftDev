package com.cinema.infrastructure.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter implements Filter {

    private final int maxRequests;
    private final long windowSeconds;
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    public RateLimitFilter(int maxRequests, long windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest http = (HttpServletRequest) request;
        String ip = http.getRemoteAddr();
        long now = Instant.now().getEpochSecond();

        Counter c = counters.compute(ip, (key, counter) -> {
            if (counter == null || now - counter.start > windowSeconds) {
                return new Counter(now, 1);
            }
            counter.count++;
            return counter;
        });

        if (c.count > maxRequests) {
            ((HttpServletResponse) response).setStatus(429);
            response.getWriter().write("Too many requests");
            return;
        }

        // ✅ ΠΑΝΤΑ προχώρα
        chain.doFilter(request, response);
    }






    private static class Counter {
        long start;
        int count;

        Counter(long start, int count) {
            this.start = start;
            this.count = count;
        }
    }

}
