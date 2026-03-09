package com.gyul.tododook.domain.todo.service;

import com.gyul.tododook.domain.todo.dto.RoutineCreateRequest;
import com.gyul.tododook.domain.todo.dto.RoutineDto;
import com.gyul.tododook.domain.todo.dto.RoutineUpdateRequest;
import com.gyul.tododook.domain.todo.entity.TodoCategory;
import com.gyul.tododook.domain.todo.entity.TodoRoutine;
import com.gyul.tododook.domain.todo.repository.TodoCategoryRepository;
import com.gyul.tododook.domain.todo.repository.TodoRoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoRoutineService {

    private final TodoRoutineRepository routineRepository;
    private final TodoCategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<RoutineDto> getRoutinesByCategoryId(Long userId, Long categoryId) {
        TodoCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        if (!category.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        return routineRepository.findByTodoCategory_Id(categoryId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoutineDto createRoutine(Long userId, RoutineCreateRequest request) {
        TodoCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        if (!category.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        TodoRoutine routine = new TodoRoutine();
        routine.setName(request.getName());
        routine.setStartDate(request.getStartDate());
        routine.setEndDate(request.getEndDate());
        routine.setPassivity(request.isPassivity());
        routine.setRepeatDays(
                request.getRepeatDays() != null ? request.getRepeatDays() : "{\"type\":\"daily\"}"
        );
        routine.setTodoCategory(category);
        routine = routineRepository.save(routine);
        return toDto(routine);
    }

    @Transactional
    public RoutineDto updateRoutine(Long userId, Long routineId, RoutineUpdateRequest request) {
        TodoRoutine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new IllegalArgumentException("루틴을 찾을 수 없습니다."));
        if (!routine.getTodoCategory().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        routine.setName(request.getName());
        routine.setStartDate(request.getStartDate());
        routine.setEndDate(request.getEndDate());
        routine.setPassivity(request.isPassivity());
        if (request.getRepeatDays() != null) {
            routine.setRepeatDays(request.getRepeatDays());
        }
        routine = routineRepository.save(routine);
        return toDto(routine);
    }

    @Transactional
    public void deleteRoutine(Long userId, Long routineId) {
        TodoRoutine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new IllegalArgumentException("루틴을 찾을 수 없습니다."));
        if (!routine.getTodoCategory().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        routineRepository.delete(routine);
    }

    private RoutineDto toDto(TodoRoutine r) {
        return new RoutineDto(
                r.getId(),
                r.getName(),
                r.getStartDate().toString(),
                r.getEndDate().toString(),
                r.isPassivity(),
                r.getRepeatDays() != null ? r.getRepeatDays() : "{\"type\":\"daily\"}",
                r.getTodoCategory().getId()
        );
    }
}
