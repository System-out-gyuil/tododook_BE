package com.gyul.tododook.global.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gyul.tododook.domain.user.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
public class OAuthController {

    private final OAuthService oauthService;

    @GetMapping("/kakao/callback")
    public ResponseEntity<AuthResponse> kakaoLogin(@RequestParam String code) throws JsonProcessingException {
        AuthResponse response = oauthService.kakaoLogin(code);
        return ResponseEntity.ok(response);
    }
}
