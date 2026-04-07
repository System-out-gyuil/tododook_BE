package com.gyul.tododook.global.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

    // [최대 요청 수, 윈도우(초)]
    private static final int[] LIMIT_LOGIN    = {5,   60};      // 5회 / 1분
    private static final int[] LIMIT_SIGNUP   = {3,   3600};    // 3회 / 1시간
    private static final int[] LIMIT_GET      = {100, 60};      // 100회 / 1분
    private static final int[] LIMIT_DEFAULT  = {60,  60};      // 60회 / 1분

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain chain) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String path   = request.getRequestURI();
        String method = request.getMethod();

        try {
            int[] limit = resolveLimit(path, method);
            String key  = buildKey(request, path);

            if (!isAllowed(key, limit[0], limit[1])) {
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"message\": \"요청이 너무 많습니다. 잠시 후 다시 시도해주세요.\"}");
                return;
            }
        } catch (RedisConnectionFailureException e) {
            // Redis 연결 실패 시 레이트 리밋을 건너뛰고 요청을 통과시킴 (fail-open)
            log.warn("[RateLimit] Redis 연결 실패로 레이트 리밋 건너뜀: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("[RateLimit] 예기치 않은 오류로 레이트 리밋 건너뜀: {}", e.getMessage());
        }

        chain.doFilter(request, response);
    }

    private int[] resolveLimit(String path, String method) {
        if (path.equals("/api/v1/auth/login"))  return LIMIT_LOGIN;
        if (path.equals("/api/v1/auth/signup")) return LIMIT_SIGNUP;
        if ("GET".equalsIgnoreCase(method))     return LIMIT_GET;
        return LIMIT_DEFAULT;
    }

    /**
     * 인증된 요청은 userId 기반, 비인증 요청(로그인·회원가입 등)은 IP 기반으로 키 생성
     */
    private String buildKey(HttpServletRequest request, String path) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null
                && auth.isAuthenticated()
                && auth.getPrincipal() instanceof Long;

        String identifier = isAuthenticated
                ? "user:" + auth.getPrincipal()
                : "ip:" + getClientIp(request);

        return "rate_limit:" + identifier + ":" + path;
    }

    private boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
        return count != null && count <= maxRequests;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
