package com.taskforge_miniapi.dto;

import com.taskforge_miniapi.model.Task;
import java.time.Instant;
import java.time.LocalDate;

// DTO: 返回给客户端的任务数据结构//
public record TaskResponse(
        Long id,
        String title,
        String description,
        Task.Status status,
        Task.Priority priority,
        Long userId,
        LocalDate dueDate,
        String contactEmail,
        Boolean aiAssisted,
        // Assignment 3 Part 1 新增
        Instant createdAt,
        Instant updatedAt
) {
    //从 Task 实体对象转换为 TaskResponse DTO//
    public static TaskResponse fromEntity(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getUserId(),
                task.getDueDate(),
                task.getContactEmail(),
                task.getAiAssisted(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}