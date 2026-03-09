package com.gyul.tododook.domain.todo.repository;

import com.gyul.tododook.domain.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByTodoCategory_User_IdAndDateOrderByStartTime(Long userId, LocalDate date);

    List<Todo> findByTodoCategory_IdOrderByStartTime(Long categoryId);

    boolean existsByTodoCategory_Id(Long categoryId);
}
