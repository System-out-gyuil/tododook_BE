package com.gyul.tododook.domain.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TodoCategoryDto implements Serializable {

    private Long id;
    private String name;
    private String color;
    private int categoryOrder;
    private boolean reveal;
}
