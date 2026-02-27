package com.chat.dto.room;

import com.chat.entity.Room;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateRoomRequest(
        @NotBlank String name,
        String description,
        Room.RoomType type,
        List<String> memberIds
) {}
