package com.john.workspace_saas.workspace;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByUserId(Long userId);

    Optional<Membership> findByUserIdAndWorkspaceId(Long userId, Long workspaceId);

    List<Membership> findByWorkspaceId(Long workspaceId);
}