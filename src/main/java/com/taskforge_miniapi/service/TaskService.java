package com.taskforge_miniapi.service;

import com.taskforge_miniapi.dto.PageResponse;
import com.taskforge_miniapi.dto.TaskCreateRequest;
import com.taskforge_miniapi.dto.TaskResponse;
import com.taskforge_miniapi.dto.TaskUpdateRequest;
import com.taskforge_miniapi.model.Task;
import com.taskforge_miniapi.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Task Business Logic Service class.
 * All userId parameters are changed to Long to align with Class 3 code.
 */
@Service
@RequiredArgsConstructor
@Slf4j //添加此注解，Lombok 会自动生成名为 log 的日志对象
public class TaskService {

    private final TaskRepository taskRepository;

    // --- DTO Conversion Method ---
    private TaskResponse toResp(Task t) {
        return new TaskResponse(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                t.getPriority(),
                t.getUserId(),
                t.getDueDate(),
                t.getContactEmail(),
                t.getAiAssisted(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }

    /**
     * Creates a new task.
     * @param request Task creation request DTO.
     * @param userId Current user ID (Long).
     * @return Created task response DTO.
     */
    @Transactional
    public TaskResponse createTask(TaskCreateRequest request, Long userId) {
        log.info("User {} is attempting to create a new task with title: {}",userId,request.title());
        // Use Builder Pattern to create Task entity
        try {
            Task task = Task.builder()
                    .userId(userId) // Long userId
                    .title(request.title())
                    .description(request.description())
                    .status(Task.Status.TODO)
                    .priority(request.priority() == null ? Task.Priority.MEDIUM : request.priority())
                    .dueDate(request.dueDate()) // LocalDate type safety
                    .aiAssisted(false)
                    .contactEmail(request.contactEmail())
                    .build();

            Task savedTask = taskRepository.save(task);
            //info log:记录操作成功
            log.info("Task successfully created and saved with ID: {}", savedTask.getId());
            return toResp(savedTask);
        }catch(Exception e) {
            log.error("CRITICAL ERROR: Failed to save task for user {}. Reason: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Task creation failed due to system error.", e);
        }
    }



    /**
     * Retrieves the task list with pagination and optional status filter.
     * @param userId Current user ID (Long).
     * @param status Optional task status filter.
     * @param page Page number (0-indexed).
     * @param size Page size.
     * @param sortBy Field to sort by.
     * @param sortDir Sort direction.
     * @return Paginated task response DTO.
     */
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getAllTasks(Long userId, Task.Status status, Task.Priority priority, int page, int size, String sortBy, String sortDir) {
        // 1. 构造分页和排序对象
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> taskPage;

        if (status != null && priority != null) {
            // 组合过滤: Status + Priority
            taskPage = taskRepository.findByUserIdAndStatusAndPriority(userId, status, priority, pageable);
        } else if (status != null) {
            // 单一过滤: Status
            taskPage = taskRepository.findByUserIdAndStatus(userId, status, pageable);
        } else if (priority != null) {
            // 单一过滤: Priority
            taskPage = taskRepository.findByUserIdAndPriority(userId, priority, pageable);
        } else {
            // 无过滤
            taskPage = taskRepository.findByUserId(userId, pageable);
        }

        List<TaskResponse> content = taskPage.getContent().stream()
                .map(this::toResp)
                .toList();

        return new PageResponse<>(
                content,
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isLast()
        );
    }

    /**
     * Retrieves a single task detail.
     * @param id Task ID.
     * @param userId Current user ID (Long).
     * @return Task response DTO.
     * @throws NoSuchElementException if the task does not exist or does not belong to the user.
     */
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id, Long userId) {
        Task task = taskRepository.findByIdAndUserId(id, userId) // Checks ID and userId (Long)
                .orElseThrow(() ->{
                    log.warn("Access Denied or Not Found: User {} attempted to retrieve task {}.", userId, id);
                       return new NoSuchElementException("Task not found with id: " + id);
                });
//info log放这里，记录查询成功
        log.info("User {} successfully retrieved task{}.",userId,id);
        return toResp(task);
    }

    /**
     * Updates a task.
     * @param id Task ID.
     * @param request Task update request DTO.
     * @param userId Current user ID (Long).
     * @return Updated task response DTO.
     * @throws NoSuchElementException if the task does not exist or does not belong to the user.
     */
    @Transactional
    public TaskResponse updateTask(Long id, TaskUpdateRequest request, Long userId) {
        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() ->{
                    //warn本身要异常的时候才记录，但是如果放在lambda外面的话他会找到任务也执行
                    //本身就是optional有值的话他是不会执行这个lambda的，但是空的话就会执行，所以warn放里面
                    log.warn("Update failed: Task ID {} not found or not owned by user {}.", id, userId);
                    return new NoSuchElementException("Task not found with id: " + id);
                });

        // Update fields (only non-null fields)
        if (StringUtils.hasText(request.title())) {
            task.setTitle(request.title());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }

        //更新的时候要被记录，info
        log.info("Task {} successfully updated by user{}",id, userId);
        Task updatedTask = taskRepository.save(task);
        return toResp(updatedTask);
    }

    /**
     * Deletes a task.
     * @param id Task ID.
     * @param userId Current user ID (Long).
     * @throws NoSuchElementException if the task does not exist or does not belong to the user.
     */
    @Transactional
    public void deleteTask(Long id, Long userId) {
        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> {
                    //warn在这里
                    log.warn("Delete Failed: Task ID {} not found or not owned by user {}.",id, userId);
                    return new NoSuchElementException("Task not found with id: " + id);
                });

        taskRepository.delete(task);
        //info 放在这里记录删除成功
        log.info("Task {} successfully deleted by user {}.",id,userId);
    }
}

