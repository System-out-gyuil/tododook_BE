package com.gyul.tododook.domain.todo.repository;

import com.gyul.tododook.domain.todo.dto.TodoCategoryDto;
import com.gyul.tododook.domain.todo.entity.TodoCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoCategoryRepository extends JpaRepository<TodoCategory, Long> {

    // getCategoriesByUserId 엔티티 로드 없이 DTO로 직접 프로젝션
    @Query("SELECT new com.gyul.tododook.domain.todo.dto.TodoCategoryDto(" +
           "c.id, c.name, c.color, c.categoryOrder, c.reveal) " +
           "FROM TodoCategory c WHERE c.user.id = :userId ORDER BY c.categoryOrder")
    List<TodoCategoryDto> findDtosByUserId(@Param("userId") Long userId);

    // 엔티티 조회가 필요한 다른 메서드(createCategory 등)에서 계속 사용
    List<TodoCategory> findByUser_IdOrderByCategoryOrder(Long userId);

    void deleteByUser_Id(Long userId);
}
