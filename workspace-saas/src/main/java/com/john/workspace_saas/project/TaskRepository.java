package com.john.workspace_saas.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByWorkspaceIdAndProjectId(Long workspaceId, Long projectId);

    Optional<Task> findByIdAndWorkspaceId(Long id, Long workspaceId);
}