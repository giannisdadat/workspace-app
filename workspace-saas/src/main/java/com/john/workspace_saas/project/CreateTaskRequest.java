package com.john.workspace_saas.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank @Size(max = 200) String title,
        Long assigneeId
) {}