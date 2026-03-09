package com.gyul.tododook.domain.todo.repository;

import com.gyul.tododook.domain.todo.entity.TodoCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoCategoryRepository extends JpaRepository<TodoCategory, Long> {

    List<TodoCategory> findByUser_IdOrderByCategoryOrder(Long userId);
}
