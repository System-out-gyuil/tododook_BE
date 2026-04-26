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
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Redis 기반 Rate Limiting 필터
 *
 * 모든 HTTP 요청에 대해 단위 시간(윈도우) 내 최대 허용 횟수를 초과하면
 * HTTP 429 Too Many Requests 응답을 반환하여 과도한 요청을 차단한다.
 *
 * - 인증된 사용자: userId 기반으로 요청 수 카운트
 * - 비인증 요청 (로그인·회원가입 등): 클라이언트 IP 기반으로 카운트
 * - Redis 장애 시: Fail-Open 전략으로 요청을 차단하지 않고 통과시킨다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

    // [최대 요청 수, 윈도우(초)] 형태의 제한 정책 상수
    private static final int[] LIMIT_LOGIN    = {5,      60};    // 로그인: 60초 동안 최대 5회 (브루트포스 방지)
    private static final int[] LIMIT_SIGNUP   = {3,    3600};    // 회원가입: 1시간 동안 최대 3회 (어뷰징 방지)
    private static final int[] LIMIT_GET      = {100000, 3600};  // 조회 요청: 10분 동안 최대 100회
    private static final int[] LIMIT_DEFAULT  = {60,     60};    // 그 외 요청: 60초 동안 최대 60회

    /**
     * INCR + EXPIRE를 하나의 Lua 스크립트로 원자적으로 처리하는 스크립트
     *
     * - 두 명령을 별도로 호출하면 그 사이에 다른 요청이 끼어드는 Race Condition이 발생할 수 있다.
     * - Lua 스크립트는 Redis에서 단일 명령으로 실행되므로 원자성이 보장된다.
     * - Redis 왕복 횟수: 2번 → 1번으로 감소
     * - 동작 순서:
     *   1. KEYS[1] 키를 1 증가시킨다 (최초 호출 시 자동 생성)
     *   2. 카운트가 1이면 (첫 요청) ARGV[1]초 후 키가 만료되도록 TTL 설정
     *   3. 현재 카운트를 반환
     */
    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>(
            "local count = redis.call('INCR', KEYS[1])\n" +
            "if count == 1 then\n" +
            "  redis.call('EXPIRE', KEYS[1], ARGV[1])\n" +
            "end\n" +
            "return count",
            Long.class
    );

    /**
     * 요청마다 실행되는 필터 본체
     *
     * OPTIONS 요청(CORS Preflight)은 레이트 리밋 없이 즉시 통과시키고,
     * 그 외 요청은 경로·메서드에 따라 제한 정책을 적용한다.
     */
    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain chain) throws ServletException, IOException {

        // CORS Preflight 요청은 레이트 리밋 대상에서 제외
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String path   = request.getRequestURI();
        String method = request.getMethod();

        try {
            // 요청 경로·메서드에 맞는 제한 정책 결정
            int[] limit = resolveLimit(path, method);
            // 사용자 식별자(userId 또는 IP) 기반의 Redis 키 생성
            String key  = buildKey(request, path);

            // 현재 요청이 허용 범위를 초과하면 429 반환
            if (!isAllowed(key, limit[0], limit[1])) {
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"message\": \"요청이 너무 많습니다. 잠시 후 다시 시도해주세요.\"}");
                return;
            }
        } catch (RedisConnectionFailureException e) {
            // Redis 연결 장애 시 Fail-Open: 서비스 가용성을 위해 요청을 차단하지 않고 통과
            log.warn("[RateLimit] Redis 연결 실패로 레이트 리밋 건너뜀: {}", e.getMessage());
        } catch (Exception e) {
            // 그 외 예외도 동일하게 Fail-Open 처리
            log.warn("[RateLimit] 예기치 않은 오류로 레이트 리밋 건너뜀: {}", e.getMessage());
        }

        chain.doFilter(request, response);
    }

    /**
     * 요청 경로와 HTTP 메서드를 기준으로 적용할 제한 정책을 반환한다.
     *
     * @param path   요청 URI
     * @param method HTTP 메서드 (GET, POST 등)
     * @return [최대 요청 수, 윈도우(초)] 배열
     */
    private int[] resolveLimit(String path, String method) {
        if (path.equals("/api/v1/auth/login"))  return LIMIT_LOGIN; // 로그인
        if (path.equals("/api/v1/auth/signup")) return LIMIT_SIGNUP; // 회원가입
        if ("GET".equalsIgnoreCase(method))     return LIMIT_GET; // 조회
        return LIMIT_DEFAULT; // 그 외 요청
    }

    /**
     * Redis에 저장할 레이트 리밋 키를 생성한다.
     *
     * - 인증된 사용자: "rate_limit:user:{userId}:{path}"
     * - 비인증 요청:   "rate_limit:ip:{clientIp}:{path}"
     *
     * 인증 여부는 SecurityContext의 Authentication 객체로 판단하며,
     * principal이 Long 타입(userId)인 경우에만 인증된 사용자로 간주한다.
     */
    private String buildKey(HttpServletRequest request, String path) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null
                && auth.isAuthenticated()
                && auth.getPrincipal() instanceof Long;

        String identifier = isAuthenticated
                ? "user:" + auth.getPrincipal()   // 인증 사용자: userId 기반
                : "ip:" + getClientIp(request);   // 비인증 사용자: IP 기반

        return "rate_limit:" + identifier + ":" + path;
    }

    /**
     * Lua 스크립트를 실행하여 현재 요청이 허용 범위 내인지 확인한다.
     *
     * @param key            Redis 키 (사용자 식별자 + 경로)
     * @param maxRequests    윈도우 내 최대 허용 요청 수
     * @param windowSeconds  윈도우 크기 (초 단위)
     * @return 허용이면 true, 초과이면 false
     */
    private boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        Long count = redisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                List.of(key),
                String.valueOf(windowSeconds)
        );
        return count != null && count <= maxRequests;
    }

    /**
     * 프록시·로드밸런서 환경을 고려하여 실제 클라이언트 IP를 추출한다.
     *
     * 우선순위:
     * 1. X-Forwarded-For 헤더 (프록시가 원본 IP를 전달하는 표준 헤더, 첫 번째 값 사용)
     * 2. X-Real-IP 헤더 (Nginx 등이 설정하는 헤더)
     * 3. request.getRemoteAddr() (직접 연결 시 소켓 주소)
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            // 여러 프록시를 거친 경우 콤마로 구분된 IP 목록의 첫 번째가 원본 클라이언트 IP
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
