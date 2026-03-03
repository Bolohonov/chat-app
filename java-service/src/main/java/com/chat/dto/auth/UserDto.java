package com.chat.dto.auth;

public record UserDto(
        String id,
        String username,
        String displayName
) {}
