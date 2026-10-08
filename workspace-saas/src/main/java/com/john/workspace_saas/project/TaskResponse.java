package com.john.workspace_saas.project;

public record TaskResponse(Long id, Long projectId, String title, TaskStatus status, Long assigneeId) {}