package com.gyul.tododook.domain.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TodoCategoryDto {

    private Long id;
    private String name;
    private String color;
    private int categoryOrder;
    private boolean reveal;
}
