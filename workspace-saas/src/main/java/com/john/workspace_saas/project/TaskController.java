package com.john.workspace_saas.project;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(Authentication auth,
                               @PathVariable Long workspaceId,
                               @PathVariable Long projectId,
                               @Valid @RequestBody CreateTaskRequest request) {
        return taskService.create((Long) auth.getPrincipal(), workspaceId, projectId, request);
    }

    @GetMapping("/projects/{projectId}/tasks")
    public List<TaskResponse> list(Authentication auth,
                                   @PathVariable Long workspaceId,
                                   @PathVariable Long projectId) {
        return taskService.list((Long) auth.getPrincipal(), workspaceId, projectId);
    }

    @PatchMapping("/tasks/{taskId}")
    public TaskResponse update(Authentication auth,
                               @PathVariable Long workspaceId,
                               @PathVariable Long taskId,
                               @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.update((Long) auth.getPrincipal(), workspaceId, taskId, request);
    }

    @DeleteMapping("/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth,
                       @PathVariable Long workspaceId,
                       @PathVariable Long taskId) {
        taskService.delete((Long) auth.getPrincipal(), workspaceId, taskId);
    }
}