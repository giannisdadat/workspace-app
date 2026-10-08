package com.john.workspace_saas.workspace;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class WorkspaceAccessService {

    private final MembershipRepository membershipRepository;
    private final EntityManager entityManager;

    /** Επιστρέφει το membership ή πετάει 404 αν ο χρήστης δεν είναι μέλος. */
    public Membership requireMember(Long userId, Long workspaceId) {
        Membership membership = membershipRepository.findByUserIdAndWorkspaceId(userId, workspaceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Workspace not found"));
        enableTenantFilter(workspaceId);
        return membership;
    }

    /** Ίδιο, αλλά απαιτεί τουλάχιστον τον ρόλο minRole (403 αλλιώς). */
    public Membership requireRole(Long userId, Long workspaceId, Role minRole) {
        Membership m = requireMember(userId, workspaceId);
        if (m.getRole().ordinal() > minRole.ordinal()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions");
        }
        return m;
    }

    private void enableTenantFilter(Long workspaceId) {
        entityManager.unwrap(Session.class)
                .enableFilter("tenantFilter")
                .setParameter("workspaceId", workspaceId);
    }
}