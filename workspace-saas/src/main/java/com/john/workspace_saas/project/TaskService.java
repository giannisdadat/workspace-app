package com.john.workspace_saas.project;

import com.john.workspace_saas.user.user;
import com.john.workspace_saas.user.UserRepository;
import com.john.workspace_saas.workspace.MembershipRepository;
import com.john.workspace_saas.workspace.Role;
import com.john.workspace_saas.workspace.WorkspaceAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final WorkspaceAccessService access;

    @Transactional
    public TaskResponse create(Long userId, Long workspaceId, Long projectId, CreateTaskRequest req) {
        var membership = access.requireMember(userId, workspaceId);
        Project project = findProject(workspaceId, projectId);

        Task task = new Task();
        task.setWorkspace(membership.getWorkspace());
        task.setProject(project);
        task.setTitle(req.title().trim());
        task.setAssignee(resolveAssignee(workspaceId, req.assigneeId()));
        taskRepository.save(task);
        return toResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> list(Long userId, Long workspaceId, Long projectId) {
        access.requireMember(userId, workspaceId);
        findProject(workspaceId, projectId);
        return taskRepository.findByWorkspaceIdAndProjectId(workspaceId, projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TaskResponse update(Long userId, Long workspaceId, Long taskId, UpdateTaskRequest req) {
        access.requireMember(userId, workspaceId);
        Task task = findTask(workspaceId, taskId);

        if (req.title() != null) task.setTitle(req.title().trim());
        if (req.status() != null) task.setStatus(req.status());
        if (req.assigneeId() != null) task.setAssignee(resolveAssignee(workspaceId, req.assigneeId()));
        return toResponse(task);
    }

    @Transactional
    public void delete(Long userId, Long workspaceId, Long taskId) {
        access.requireRole(userId, workspaceId, Role.ADMIN);
        taskRepository.delete(findTask(workspaceId, taskId));
    }

    private Project findProject(Long workspaceId, Long projectId) {
        return projectRepository.findByIdAndWorkspaceId(projectId, workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private Task findTask(Long workspaceId, Long taskId) {
        return taskRepository.findByIdAndWorkspaceId(taskId, workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    private user resolveAssignee(Long workspaceId, Long assigneeId) {
        if (assigneeId == null) return null;
        membershipRepository.findByUserIdAndWorkspaceId(assigneeId, workspaceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Assignee is not a member of this workspace"));
        return userRepository.getReferenceById(assigneeId);
    }

    private TaskResponse toResponse(Task t) {
        return new TaskResponse(
                t.getId(),
                t.getProject().getId(),
                t.getTitle(),
                t.getStatus(),
                t.getAssignee() == null ? null : t.getAssignee().getId());
    }
}