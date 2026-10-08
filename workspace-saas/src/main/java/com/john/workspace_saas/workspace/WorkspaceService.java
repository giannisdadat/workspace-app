package com.john.workspace_saas.workspace;

import com.john.workspace_saas.user.user;
import com.john.workspace_saas.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;

    @Transactional
    public WorkspaceResponse create(Long userId, CreateWorkspaceRequest request) {
        user user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Workspace workspace = new Workspace();
        workspace.setName(request.name().trim());
        workspaceRepository.save(workspace);

        Membership membership = new Membership();
        membership.setUser(user);
        membership.setWorkspace(workspace);
        membership.setRole(Role.OWNER);
        membershipRepository.save(membership);

        return new WorkspaceResponse(workspace.getId(), workspace.getName(), Role.OWNER);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> listForUser(Long userId) {
        return membershipRepository.findByUserId(userId).stream()
                .map(m -> new WorkspaceResponse(
                        m.getWorkspace().getId(),
                        m.getWorkspace().getName(),
                        m.getRole()))
                .toList();
    }
}