package com.gyul.tododook.domain.todo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TodoCategoryUpdateRequest {

    @NotBlank
    private String name;

    private String color;
}
