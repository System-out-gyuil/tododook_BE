package com.gyul.tododook.global.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyul.tododook.domain.todo.entity.TodoCategory;
import com.gyul.tododook.domain.todo.repository.TodoCategoryRepository;
import com.gyul.tododook.domain.user.dto.AuthResponse;
import com.gyul.tododook.domain.user.entity.User;
import com.gyul.tododook.domain.user.repository.UserRepository;
import com.gyul.tododook.global.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private static final String DEFAULT_CATEGORY_NAME = "오늘의 할일";

    private final UserRepository userRepository;
    private final TodoCategoryRepository categoryRepository;
    private final JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.client-secret:}")
    private String kakaoClientSecret;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Transactional
    public AuthResponse kakaoLogin(String code) throws JsonProcessingException {
        System.out.println("redirect_uri = " + kakaoRedirectUri);
        // 1. 인가 코드 → 카카오 액세스 토큰
        String kakaoAccessToken = getToken(code);

        // 2. 카카오 액세스 토큰 → 유저 정보
        KakaoUserInfoDto kakaoUserInfo = getKakaoUserInfo(kakaoAccessToken);

        // 3. DB에서 kakaoId로 조회 → 없으면 이메일로 조회(기존 계정 연동) → 없으면 신규 가입
        boolean[] isNew = {false};
        User user = userRepository.findByKakaoId(kakaoUserInfo.getId().toString())
                .orElseGet(() -> {
                    String email = resolveEmail(kakaoUserInfo);
                    return userRepository.findByEmail(email)
                            .map(existing -> {
                                existing.setKakaoId(kakaoUserInfo.getId().toString());
                                return userRepository.save(existing);
                            })
                            .orElseGet(() -> {
                                isNew[0] = true;
                                return registerOAuthUser(kakaoUserInfo);
                            });
                });

        // 4. 서비스 JWT 발급
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, "Bearer", user.getId(), user.getName(), user.getEmail(), isNew[0]);
    }

    // 카카오 서버에서 액세스 토큰 발급
    private String getToken(String code) throws JsonProcessingException {
        RestTemplate rt = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoClientId);
        body.add("redirect_uri", kakaoRedirectUri);
        body.add("code", code);
        if (kakaoClientSecret != null && !kakaoClientSecret.isBlank()) {
            body.add("client_secret", kakaoClientSecret);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                request,
                String.class
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> tokenMap = objectMapper.readValue(response.getBody(), Map.class);
        return (String) tokenMap.get("access_token");
    }

    // 카카오 서버에서 유저 정보 조회
    private KakaoUserInfoDto getKakaoUserInfo(String kakaoAccessToken) throws JsonProcessingException {
        RestTemplate rt = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(kakaoAccessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                request,
                String.class
        );

        return objectMapper.readValue(response.getBody(), KakaoUserInfoDto.class);
    }

    // 신규 OAuth 유저 등록
    private User registerOAuthUser(KakaoUserInfoDto info) {
        String email = resolveEmail(info);
        String nickname = resolveNickname(info);

        // 닉네임 중복 방지
        String uniqueName = nickname;
        int suffix = 1;
        while (userRepository.existsByName(uniqueName)) {
            uniqueName = nickname + "_" + suffix++;
        }

        User user = new User();
        user.setKakaoId(info.getId().toString());
        user.setEmail(email);
        user.setName(uniqueName);
        user.setPassword(UUID.randomUUID().toString()); // OAuth 유저는 비밀번호 미사용
        user = userRepository.save(user);

        // 기본 카테고리 생성
        TodoCategory defaultCategory = new TodoCategory();
        defaultCategory.setName(DEFAULT_CATEGORY_NAME);
        defaultCategory.setColor("white");
        defaultCategory.setCategoryOrder(0);
        defaultCategory.setReveal(true);
        defaultCategory.setUser(user);
        categoryRepository.save(defaultCategory);

        return user;
    }

    private String resolveEmail(KakaoUserInfoDto info) {
        if (info.getKakaoAccount() != null && info.getKakaoAccount().getEmail() != null) {
            return info.getKakaoAccount().getEmail();
        }
        return "kakao_" + info.getId() + "@kakao.local";
    }

    private String resolveNickname(KakaoUserInfoDto info) {
        if (info.getKakaoAccount() != null
                && info.getKakaoAccount().getProfile() != null
                && info.getKakaoAccount().getProfile().getNickname() != null) {
            return info.getKakaoAccount().getProfile().getNickname();
        }
        return "카카오유저" + info.getId();
    }
}
