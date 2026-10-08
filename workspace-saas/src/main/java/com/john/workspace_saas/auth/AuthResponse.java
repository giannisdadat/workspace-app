package com.john.workspace_saas.auth;

public record AuthResponse(String token, UserResponse user) {}