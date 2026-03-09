package com.gyul.tododook.domain.todo.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalTime;

import com.gyul.tododook.global.jpa.BaseEntity;
import jakarta.persistence.Entity;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "todo")
public class Todo extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private boolean done;

    private LocalTime startTime;

    private LocalTime endTime;

    @Column(nullable = false, name = "todo_order")
    private int todoOrder;

    @ManyToOne
    @JoinColumn(name = "todo_category_id", nullable = false)
    private TodoCategory todoCategory;

}
