package com.gyul.tododook.domain.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TodoCreateRequest {

    @NotNull(message = "카테고리 ID는 필수입니다.")
    private Long categoryId;

    @NotBlank(message = "할일 이름은 필수입니다.")
    private String name;

    @NotNull(message = "날짜는 필수입니다.")
    private LocalDate date;

    private LocalTime startTime;
    private LocalTime endTime;
}
