package com.chat.dto.search;

import com.chat.dto.auth.UserDto;
import com.chat.dto.room.RoomDto;

import java.util.List;

public record SearchResultDto(
        List<RoomDto> rooms,
        List<UserDto> users
) {}
