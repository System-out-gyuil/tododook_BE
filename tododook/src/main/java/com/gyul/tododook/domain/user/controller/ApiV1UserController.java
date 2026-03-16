package com.gyul.tododook.domain.user.controller;

import com.gyul.tododook.domain.user.dto.UpdateStatusMessageRequest;
import com.gyul.tododook.domain.user.dto.UserProfileResponse;
import com.gyul.tododook.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class ApiV1UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PatchMapping(value = "/me/profile-image", consumes = "multipart/form-data")
    public ResponseEntity<UserProfileResponse> updateProfileImage(
            Authentication authentication,
            @RequestPart("file") MultipartFile file) throws Exception {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(userService.updateProfileImage(userId, file));
    }

    @DeleteMapping("/me/profile-image")
    public ResponseEntity<UserProfileResponse> deleteProfileImage(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(userService.deleteProfileImage(userId));
    }

    @PatchMapping("/me/name")
    public ResponseEntity<UserProfileResponse> updateName(
            Authentication authentication,
            @RequestBody Map<String, String> body) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(userService.updateName(userId, body.get("name")));
    }

    @PatchMapping("/me/status-message")
    public ResponseEntity<UserProfileResponse> updateStatusMessage(
            Authentication authentication,
            @RequestBody UpdateStatusMessageRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(userService.updateStatusMessage(userId, request.getStatusMessage()));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userService.deleteAccount(userId);
        return ResponseEntity.noContent().build();
    }
}
