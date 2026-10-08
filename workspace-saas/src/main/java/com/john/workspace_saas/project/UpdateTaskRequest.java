package com.john.workspace_saas.project;

import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
        @Size(min = 1, max = 200) String title,
        TaskStatus status,
        Long assigneeId
) {}