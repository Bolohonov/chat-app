package com.chat.controller;

import com.chat.dto.auth.*;
import com.chat.dto.message.*;
import com.chat.dto.room.*;
import com.chat.service.MessageService;
import com.chat.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<RoomDto> createRoom(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateRoomRequest req) {
        return ResponseEntity.ok(roomService.createRoom(userId, req));
    }

    @GetMapping
    public ResponseEntity<List<RoomDto>> getMyRooms(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(roomService.getUserRooms(userId));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable String roomId,
            @AuthenticationPrincipal String userId) {
        roomService.deleteRoom(roomId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roomId}/members/{userId}")
    public ResponseEntity<Void> addMember(
            @PathVariable String roomId,
            @PathVariable String userId) {
        roomService.addMember(roomId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<Page<MessageDto>> getHistory(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(messageService.getHistory(roomId, page, size));
    }

    @PostMapping("/{roomId}/messages")
    public ResponseEntity<MessageDto> sendMessage(
            @PathVariable String roomId,
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody SendMessageRequest req) {
        return ResponseEntity.ok(messageService.sendMessage(roomId, userId, req));
    }
}
