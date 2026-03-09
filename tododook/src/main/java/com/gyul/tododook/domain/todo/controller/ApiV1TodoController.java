package com.gyul.tododook.domain.todo.controller;

import com.gyul.tododook.domain.todo.dto.CategoryReorderRequest;
import com.gyul.tododook.domain.todo.dto.TodoCategoryCreateRequest;
import com.gyul.tododook.domain.todo.dto.TodoCategoryDto;
import com.gyul.tododook.domain.todo.dto.TodoCategoryUpdateRequest;
import com.gyul.tododook.domain.todo.dto.TodoCreateRequest;
import com.gyul.tododook.domain.todo.dto.TodoDto;
import com.gyul.tododook.domain.todo.service.TodoCategoryService;
import com.gyul.tododook.domain.todo.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApiV1TodoController {

    private final TodoCategoryService categoryService;
    private final TodoService todoService;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("인증되지 않았습니다.");
        }
        return (Long) auth.getPrincipal();
    }

    // ========== Categories ==========
    @GetMapping("/categories")
    public ResponseEntity<List<TodoCategoryDto>> getCategories() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(categoryService.getCategoriesByUserId(userId));
    }

    @PostMapping("/categories")
    public ResponseEntity<TodoCategoryDto> createCategory(@Valid @RequestBody TodoCategoryCreateRequest request) {
        Long userId = getCurrentUserId();
        TodoCategoryDto created = categoryService.createCategory(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<TodoCategoryDto> updateCategory(@PathVariable Long id,
                                                           @Valid @RequestBody TodoCategoryUpdateRequest request) {
        Long userId = getCurrentUserId();
        TodoCategoryDto updated = categoryService.updateCategory(userId, id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        categoryService.deleteCategory(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/categories/reorder")
    public ResponseEntity<Void> reorderCategories(@Valid @RequestBody CategoryReorderRequest request) {
        Long userId = getCurrentUserId();
        categoryService.reorderCategories(userId, request.getCategoryIds());
        return ResponseEntity.noContent().build();
    }

    // ========== Todos ==========
    @GetMapping("/todos")
    public ResponseEntity<List<TodoDto>> getTodos(@RequestParam(required = false) LocalDate date,
                                                  @RequestParam(required = false) Long categoryId) {
        Long userId = getCurrentUserId();
        if (date != null) {
            return ResponseEntity.ok(todoService.getTodosByUserAndDate(userId, date));
        }
        if (categoryId != null) {
            return ResponseEntity.ok(todoService.getTodosByCategoryId(userId, categoryId));
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/todos")
    public ResponseEntity<TodoDto> createTodo(@Valid @RequestBody TodoCreateRequest request) {
        Long userId = getCurrentUserId();
        TodoDto created = todoService.createTodo(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/todos/{id}/done")
    public ResponseEntity<TodoDto> toggleTodoDone(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        TodoDto updated = todoService.toggleDone(userId, id);
        return ResponseEntity.ok(updated);
    }
}
