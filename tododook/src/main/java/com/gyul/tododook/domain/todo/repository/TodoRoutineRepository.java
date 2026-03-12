package com.gyul.tododook.domain.todo.repository;

import com.gyul.tododook.domain.todo.entity.TodoRoutine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRoutineRepository extends JpaRepository<TodoRoutine, Long> {

    List<TodoRoutine> findByTodoCategory_Id(Long categoryId);

    void deleteByTodoCategory_Id(Long categoryId);
}
