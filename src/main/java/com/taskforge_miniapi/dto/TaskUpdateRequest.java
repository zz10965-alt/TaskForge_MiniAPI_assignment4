package com.taskforge_miniapi.dto;

import com.taskforge_miniapi.model.Task;
import com.taskforge_miniapi.validation.ValidPriority;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO: 任务更新请求体 (PUT /api/v1/tasks/{id})。
 */
public record TaskUpdateRequest(
        @Size(min = 1, max = 140, message = "Title must be between 1 and 140 characters")
        String title,

        @Size(max = 10000, message = "Description cannot exceed 10000 characters")
        String description,

        @Email(message="Email must be effective")
        String contactEmail,

        Task.Status status,
        @ValidPriority
        Task.Priority priority,

        LocalDate dueDate
) {}
