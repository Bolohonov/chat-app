package com.chat.dto.message;

import java.time.Instant;

public record MessageDto(
        String id, String roomId, String senderId,
        String senderName, String content, Instant createdAt
) {}
