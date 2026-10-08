package com.john.workspace_saas.project;

import com.john.workspace_saas.workspace.Role;
import com.john.workspace_saas.workspace.WorkspaceAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final WorkspaceAccessService access;

    @Transactional
    public ProjectResponse create(Long userId, Long workspaceId, CreateProjectRequest request) {
        var membership = access.requireRole(userId, workspaceId, Role.ADMIN);

        Project project = new Project();
        project.setWorkspace(membership.getWorkspace());
        project.setName(request.name().trim());
        projectRepository.save(project);

        return new ProjectResponse(project.getId(), project.getName());
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> list(Long userId, Long workspaceId) {
        access.requireMember(userId, workspaceId);
        return projectRepository.findByWorkspaceId(workspaceId).stream()
                .map(p -> new ProjectResponse(p.getId(), p.getName()))
                .toList();
    }
}