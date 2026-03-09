package com.gyul.tododook.domain.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TodoDto {

    private Long id;
    private String name;
    private LocalDate date;
    private boolean done;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long categoryId;
}
