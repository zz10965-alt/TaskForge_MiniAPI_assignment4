package com.taskforge_miniapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Entity
@Table(name="tasks")
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
    private String title;

    @Size(max=10000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Status status = Status.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Priority priority = Priority.MEDIUM;

    // 任务的创建者（用户ID），用于权限控制
    @Column(nullable=false)
    private Long userId;

    private LocalDate dueDate;

    private Boolean aiAssisted = false;

    private String contactEmail;

    // --- Assignment 3 Part 1: Timestamp新增
    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;


    // 枚举Status
    public enum Status {
        TODO,
        IN_PROGRESS,
        DONE
    }

    // priority 枚举
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }
}

