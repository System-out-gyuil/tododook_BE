package com.gyul.tododook.domain.todo.entity;

import com.gyul.tododook.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "todo_routine")
public class TodoRoutine extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean passivity;

    @Column(columnDefinition = "json", name = "repeat_days")
    private String repeatDays;

    @ManyToOne
    @JoinColumn(name = "todo_category_id", nullable = false)
    private TodoCategory todoCategory;
}
