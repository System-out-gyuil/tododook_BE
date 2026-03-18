package com.gyul.tododook.domain.todo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryReorderRequest {

    @NotNull(message = "카테고리 ID 목록은 필수입니다.")
    private List<Long> categoryIds;
}
