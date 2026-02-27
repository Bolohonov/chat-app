package com.chat.dto.room;

import com.chat.entity.Room;
import java.time.Instant;

public record RoomDto(
        String id, String name, String description,
        String type, String ownerId, Instant createdAt
) {}
