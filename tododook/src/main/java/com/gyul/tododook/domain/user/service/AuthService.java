package com.gyul.tododook.domain.user.service;

import com.gyul.tododook.domain.todo.entity.TodoCategory;
import com.gyul.tododook.domain.todo.repository.TodoCategoryRepository;
import com.gyul.tododook.domain.user.dto.AuthResponse;
import com.gyul.tododook.domain.user.dto.LoginRequest;
import com.gyul.tododook.domain.user.dto.SignupRequest;
import com.gyul.tododook.domain.user.entity.User;
import com.gyul.tododook.domain.user.repository.UserRepository;
import com.gyul.tododook.global.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DEFAULT_CATEGORY_NAME = "오늘의 할일";

    private final UserRepository userRepository;
    private final TodoCategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("이미 사용 중인 이름입니다.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);

        TodoCategory defaultCategory = new TodoCategory();
        defaultCategory.setName(DEFAULT_CATEGORY_NAME);
        defaultCategory.setColor("white");
        defaultCategory.setCategoryOrder(0);
        defaultCategory.setReveal(true);
        defaultCategory.setUser(user);
        categoryRepository.save(defaultCategory);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, "Bearer", user.getId(), user.getName(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, "Bearer", user.getId(), user.getName(), user.getEmail());
    }
}
