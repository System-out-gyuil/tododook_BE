package com.gyul.tododook.domain.todo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TodoDateUpdateRequest {

    @NotNull(message = "날짜는 필수입니다.")
    private LocalDate date;
}
