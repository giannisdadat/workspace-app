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
public class MemberService {

    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final WorkspaceAccessService access;

    @Transactional
    public MemberResponse addMember(Long actorId, Long workspaceId, AddMemberRequest request) {
        Membership actor = access.requireRole(actorId, workspaceId, Role.ADMIN);

        if (request.role() == Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot add another OWNER");
        }

        user user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (membershipRepository.findByUserIdAndWorkspaceId(user.getId(), workspaceId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already a member");
        }

        Membership m = new Membership();
        m.setUser(user);
        m.setWorkspace(actor.getWorkspace());
        m.setRole(request.role());
        membershipRepository.save(m);

        return new MemberResponse(user.getId(), user.getEmail(), user.getName(), m.getRole());
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> list(Long actorId, Long workspaceId) {
        access.requireMember(actorId, workspaceId);
        return membershipRepository.findByWorkspaceId(workspaceId).stream()
                .map(m -> new MemberResponse(
                        m.getUser().getId(), m.getUser().getEmail(),
                        m.getUser().getName(), m.getRole()))
                .toList();
    }
}