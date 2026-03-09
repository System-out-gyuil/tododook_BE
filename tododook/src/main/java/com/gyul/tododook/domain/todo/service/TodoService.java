package com.gyul.tododook.domain.todo.service;

import com.gyul.tododook.domain.todo.dto.TodoCreateRequest;
import com.gyul.tododook.domain.todo.dto.TodoDateUpdateRequest;
import com.gyul.tododook.domain.todo.dto.TodoDto;
import com.gyul.tododook.domain.todo.dto.TodoReorderRequest;
import com.gyul.tododook.domain.todo.entity.Todo;
import com.gyul.tododook.domain.todo.entity.TodoCategory;
import com.gyul.tododook.domain.todo.repository.TodoCategoryRepository;
import com.gyul.tododook.domain.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoCategoryRepository categoryRepository;

    private static final LocalTime DEFAULT_START = LocalTime.of(9, 0);
    private static final LocalTime DEFAULT_END = LocalTime.of(10, 0);

    @Transactional(readOnly = true)
    public List<TodoDto> getTodosByUserAndDate(Long userId, LocalDate date) {
        return todoRepository.findByTodoCategory_User_IdAndDateOrderByTodoOrder(userId, date).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TodoDto> getTodosByCategoryId(Long userId, Long categoryId) {
        TodoCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        if (!category.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        return todoRepository.findByTodoCategory_IdOrderByTodoOrder(categoryId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TodoDto createTodo(Long userId, TodoCreateRequest request) {
        TodoCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        if (!category.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        int nextOrder = todoRepository.countByTodoCategory_IdAndDate(
                request.getCategoryId(), request.getDate());
        Todo todo = new Todo();
        todo.setName(request.getName());
        todo.setDate(request.getDate());
        todo.setDone(false);
        todo.setTodoOrder(nextOrder);
        todo.setStartTime(request.getStartTime() != null ? request.getStartTime() : DEFAULT_START);
        todo.setEndTime(request.getEndTime() != null ? request.getEndTime() : DEFAULT_END);
        todo.setTodoCategory(category);
        todo = todoRepository.save(todo);
        return toDto(todo);
    }

    @Transactional
    public TodoDto toggleDone(Long userId, Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("할일을 찾을 수 없습니다."));
        if (!todo.getTodoCategory().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        todo.setDone(!todo.isDone());
        todo = todoRepository.save(todo);
        return toDto(todo);
    }

    @Transactional
    public void reorderTodos(Long userId, TodoReorderRequest request) {
        List<Long> todoIds = request.getTodoIds();
        for (int i = 0; i < todoIds.size(); i++) {
            Todo todo = todoRepository.findById(todoIds.get(i))
                    .orElseThrow(() -> new IllegalArgumentException("할일을 찾을 수 없습니다."));
            if (!todo.getTodoCategory().getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("권한이 없습니다.");
            }
            todo.setTodoOrder(i);
            todoRepository.save(todo);
        }
    }

    @Transactional
    public TodoDto updateDate(Long userId, Long todoId, TodoDateUpdateRequest request) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("할일을 찾을 수 없습니다."));
        if (!todo.getTodoCategory().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        int nextOrder = todoRepository.countByTodoCategory_IdAndDate(
                todo.getTodoCategory().getId(), request.getDate());
        todo.setDate(request.getDate());
        todo.setTodoOrder(nextOrder);
        todo = todoRepository.save(todo);
        return toDto(todo);
    }

    private TodoDto toDto(Todo t) {
        return new TodoDto(
                t.getId(),
                t.getName(),
                t.getDate(),
                t.isDone(),
                t.getStartTime(),
                t.getEndTime(),
                t.getTodoCategory().getId(),
                t.getTodoOrder()
        );
    }
}
