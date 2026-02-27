package com.chat.dto.auth;

public record AuthResponse(String token, String userId, String username, String displayName) {}
