package com.gyul.tododook.domain.todo.service;

import com.gyul.tododook.domain.todo.dto.TodoCategoryCreateRequest;
import com.gyul.tododook.domain.todo.dto.TodoCategoryDto;
import com.gyul.tododook.domain.todo.dto.TodoCategoryUpdateRequest;
import com.gyul.tododook.domain.todo.entity.TodoCategory;
import com.gyul.tododook.domain.todo.repository.TodoCategoryRepository;
import com.gyul.tododook.domain.todo.repository.TodoRepository;
import com.gyul.tododook.domain.user.entity.User;
import com.gyul.tododook.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoCategoryService {

    private final TodoCategoryRepository categoryRepository;
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TodoCategoryDto> getCategoriesByUserId(Long userId) {
        return categoryRepository.findByUser_IdOrderByCategoryOrder(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TodoCategoryDto createCategory(Long userId, TodoCategoryCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        int nextOrder = (int) categoryRepository.findByUser_IdOrderByCategoryOrder(userId).stream().count();
        TodoCategory category = new TodoCategory();
        category.setName(request.getName());
        category.setColor(request.getColor() != null ? request.getColor() : "white");
        category.setCategoryOrder(nextOrder);
        category.setReveal(true);
        category.setUser(user);
        category = categoryRepository.save(category);
        return toDto(category);
    }

    @Transactional
    public TodoCategoryDto updateCategory(Long userId, Long categoryId, TodoCategoryUpdateRequest request) {
        TodoCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        if (!category.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        category.setName(request.getName());
        if (request.getColor() != null) {
            category.setColor(request.getColor());
        }
        category = categoryRepository.save(category);
        return toDto(category);
    }

    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        TodoCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        if (!category.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        if (todoRepository.existsByTodoCategory_Id(categoryId)) {
            throw new IllegalArgumentException("이 카테고리에 할일이 있어 삭제할 수 없습니다. 할일을 먼저 삭제하거나 다른 카테고리로 옮겨 주세요.");
        }
        categoryRepository.delete(category);
    }

    @Transactional
    public void reorderCategories(Long userId, List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }
        for (int i = 0; i < categoryIds.size(); i++) {
            Long id = categoryIds.get(i);
            TodoCategory cat = categoryRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
            if (!cat.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("권한이 없습니다.");
            }
            cat.setCategoryOrder(i);
            categoryRepository.save(cat);
        }
    }

    private TodoCategoryDto toDto(TodoCategory c) {
        return new TodoCategoryDto(
                c.getId(),
                c.getName(),
                c.getColor(),
                c.getCategoryOrder(),
                c.isReveal()
        );
    }
}
