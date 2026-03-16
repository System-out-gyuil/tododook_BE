package com.gyul.tododook.domain.user.service;

import com.gyul.tododook.domain.user.dto.UserProfileResponse;
import com.gyul.tododook.domain.user.entity.User;
import com.gyul.tododook.domain.user.repository.UserRepository;
import com.gyul.tododook.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = findUser(userId);
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfileImage(Long userId, MultipartFile file) throws IOException {
        User user = findUser(userId);

        if (user.getProfileImage() != null) {
            s3Service.delete(user.getProfileImage());
        }

        String imageUrl = s3Service.upload(file, "profiles");
        user.setProfileImage(imageUrl);
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse deleteProfileImage(Long userId) {
        User user = findUser(userId);

        if (user.getProfileImage() != null) {
            s3Service.delete(user.getProfileImage());
            user.setProfileImage(null);
        }
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateStatusMessage(Long userId, String statusMessage) {
        User user = findUser(userId);
        user.setStatusMessage(statusMessage == null || statusMessage.isBlank() ? null : statusMessage.trim());
        return toResponse(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProfileImage(),
                user.getStatusMessage()
        );
    }
}
