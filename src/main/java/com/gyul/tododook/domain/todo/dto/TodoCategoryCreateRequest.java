package com.gyul.tododook.domain.todo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TodoCategoryCreateRequest {

    @NotBlank(message = "카테고리 이름은 필수입니다.")
    private String name;

    private String color = "white";
}
