package com.chat.service;

import com.chat.dto.room.CreateRoomRequest;
import com.chat.dto.room.RoomDto;
import com.chat.entity.Room;
import com.chat.entity.User;
import com.chat.repository.RoomRepository;
import com.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Transactional
    public RoomDto createRoom(String ownerId, CreateRoomRequest req) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Room room = new Room();
        room.setName(req.name());
        room.setDescription(req.description());
        room.setType(req.type() != null ? req.type() : Room.RoomType.GROUP);
        room.setOwner(owner);
        roomRepository.save(room);

        Set<User> members = new HashSet<>();
        members.add(owner);
        if (req.memberIds() != null) {
            members.addAll(userRepository.findAllById(req.memberIds()));
        }
        for (User member : members) {
            if (member.getRooms() == null) member.setRooms(new HashSet<>());
            member.getRooms().add(room);
            userRepository.save(member);
        }

        return toDto(room);
    }

    @Transactional(readOnly = true)
    public List<RoomDto> getUserRooms(String userId) {
        return roomRepository.findByMemberId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addMember(String roomId, String userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRooms() == null) user.setRooms(new HashSet<>());
        user.getRooms().add(room);
        userRepository.save(user);
    }

    @Transactional
    public void deleteRoom(String roomId, String requesterId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        if (!room.getOwner().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can delete this room");
        }

        List<User> members = userRepository.findMembersByRoomId(roomId);
        for (User member : members) {
            member.getRooms().removeIf(r -> r.getId().equals(roomId));
            userRepository.save(member);
        }

        roomRepository.deleteById(roomId);
    }

    private RoomDto toDto(Room r) {
        return new RoomDto(r.getId(), r.getName(), r.getDescription(),
                r.getType().name(), r.getOwner().getId(), r.getCreatedAt());
    }
}