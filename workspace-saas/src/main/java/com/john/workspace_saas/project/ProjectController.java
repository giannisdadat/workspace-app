package com.john.workspace_saas.project;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(Authentication auth,
                                  @PathVariable Long workspaceId,
                                  @Valid @RequestBody CreateProjectRequest request) {
        return projectService.create((Long) auth.getPrincipal(), workspaceId, request);
    }

    @GetMapping
    public List<ProjectResponse> list(Authentication auth, @PathVariable Long workspaceId) {
        return projectService.list((Long) auth.getPrincipal(), workspaceId);
    }
}