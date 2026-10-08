package com.john.workspace_saas.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByWorkspaceId(Long workspaceId);

    Optional<Project> findByIdAndWorkspaceId(Long id, Long workspaceId);
}