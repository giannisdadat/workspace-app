package com.john.workspace_saas.workspace;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkspaceResponse create(Authentication auth,
                                    @Valid @RequestBody CreateWorkspaceRequest request) {
        return workspaceService.create((Long) auth.getPrincipal(), request);
    }

    @GetMapping
    public List<WorkspaceResponse> list(Authentication auth) {
        return workspaceService.listForUser((Long) auth.getPrincipal());
    }
}