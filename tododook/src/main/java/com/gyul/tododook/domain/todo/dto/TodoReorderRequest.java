package com.gyul.tododook.domain.todo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TodoReorderRequest {

    @NotNull(message = "할일 ID 목록은 필수입니다.")
    private List<Long> todoIds;
}
