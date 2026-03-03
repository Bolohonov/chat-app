package com.chat.controller;

import com.chat.dto.auth.UserDto;
import com.chat.dto.room.RoomDto;
import com.chat.dto.search.SearchResultDto;
import com.chat.repository.RoomRepository;
import com.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    @GetMapping
    public ResponseEntity<SearchResultDto> search(
            @AuthenticationPrincipal String userId,
            @RequestParam String q) {

        if (q == null || q.trim().length() < 1) {
            return ResponseEntity.ok(new SearchResultDto(List.of(), List.of()));
        }

        List<RoomDto> rooms = roomRepository.searchByNameAndMember(userId, q.trim())
                .stream()
                .map(r -> new RoomDto(r.getId(), r.getName(), r.getDescription(),
                        r.getType().name(), r.getOwner().getId(), r.getCreatedAt()))
                .toList();

        List<UserDto> users = userRepository.searchByUsernameOrDisplayName(q.trim())
                .stream()
                .filter(u -> !u.getId().equals(userId)) // exclude self
                .map(u -> new UserDto(u.getId(), u.getUsername(), u.getDisplayName()))
                .toList();

        return ResponseEntity.ok(new SearchResultDto(rooms, users));
    }
}
