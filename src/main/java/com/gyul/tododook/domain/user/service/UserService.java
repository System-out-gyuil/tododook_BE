package com.gyul.tododook.domain.user.service;

import com.gyul.tododook.domain.todo.repository.TodoCategoryRepository;
import com.gyul.tododook.domain.todo.repository.TodoRepository;
import com.gyul.tododook.domain.todo.repository.TodoRoutineRepository;
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
    private final TodoRepository todoRepository;
    private final TodoRoutineRepository todoRoutineRepository;
    private final TodoCategoryRepository todoCategoryRepository;

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
    public UserProfileResponse updateName(Long userId, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }
        String trimmed = name.trim();
        User user = findUser(userId);
        if (!trimmed.equals(user.getName()) && userRepository.existsByName(trimmed)) {
            throw new IllegalArgumentException("이미 사용 중인 이름입니다.");
        }
        user.setName(trimmed);
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateStatusMessage(Long userId, String statusMessage) {
        User user = findUser(userId);
        user.setStatusMessage(statusMessage == null || statusMessage.isBlank() ? null : statusMessage.trim());
        return toResponse(user);
    }

    @Transactional
    public void deleteAccount(Long userId) {
        User user = findUser(userId);

        if (user.getProfileImage() != null) {
            s3Service.delete(user.getProfileImage());
        }

        // FK 제약 순서: Todo → Routine → Category → User
        todoRepository.deleteByTodoCategory_User_Id(userId);
        todoRoutineRepository.deleteByTodoCategory_User_Id(userId);
        todoCategoryRepository.deleteByUser_Id(userId);
        userRepository.delete(user);
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
