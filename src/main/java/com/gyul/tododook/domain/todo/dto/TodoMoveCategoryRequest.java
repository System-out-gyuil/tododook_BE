package com.gyul.tododook.domain.todo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TodoMoveCategoryRequest {

    @NotNull
    private Long categoryId;
}
