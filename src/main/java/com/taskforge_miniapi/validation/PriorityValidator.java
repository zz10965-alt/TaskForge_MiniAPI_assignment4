package com.taskforge_miniapi.validation;

import com.taskforge_miniapi.validation.ValidPriority;
import com.taskforge_miniapi.model.Task;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class PriorityValidator implements
        ConstraintValidator<ValidPriority, Task.Priority> {

    private Set<Task.Priority> allowedPriorities;

    @Override
    public void initialize(ValidPriority constraintAnnotation) {
        // 【初始化】获取所有合法的 Priority 枚举值
        allowedPriorities = Arrays.stream(Task.Priority.values()).collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(Task.Priority value,
                           ConstraintValidatorContext context) {
        // 如果值为 null，交给 @NotNull 去处理；这里我们认为通过
        if (value == null) {return true;
        }

        // 检查输入的值是否包含在合法的枚举集合中
        return allowedPriorities.contains(value);
    }
}