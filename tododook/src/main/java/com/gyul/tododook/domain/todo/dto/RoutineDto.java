package com.gyul.tododook.domain.todo.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoutineDto {

    private Long id;
    private String name;
    private String startDate;
    private String endDate;
    private boolean passivity;

    @JsonRawValue
    private String repeatDays;

    private Long categoryId;
}
