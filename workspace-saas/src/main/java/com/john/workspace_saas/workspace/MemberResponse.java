package com.john.workspace_saas.workspace;

public record MemberResponse(Long userId, String email, String name, Role role) {}