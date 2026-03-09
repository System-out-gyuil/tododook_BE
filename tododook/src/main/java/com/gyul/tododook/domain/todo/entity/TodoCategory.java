package com.gyul.tododook.domain.todo.entity;
import com.gyul.tododook.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.gyul.tododook.global.jpa.BaseEntity;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "todo_category")
public class TodoCategory extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false, name = "category_order")
    private int categoryOrder;

    @Column(nullable = false)
    private boolean reveal;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
