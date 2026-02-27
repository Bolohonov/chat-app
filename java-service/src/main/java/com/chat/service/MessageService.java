package com.chat.service;

import com.chat.dto.message.MessageDto;
import com.chat.dto.message.SendMessageRequest;
import com.chat.entity.Message;
import com.chat.entity.Room;
import com.chat.entity.User;
import com.chat.repository.MessageRepository;
import com.chat.repository.RoomRepository;
import com.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Transactional
    public MessageDto sendMessage(String roomId, String senderId, SendMessageRequest req) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify sender is a member
        boolean isMember = room.getMembers().stream()
                .anyMatch(m -> m.getId().equals(senderId));
        if (!isMember) {
            throw new SecurityException("Not a room member");
        }

        Message message = new Message();
        message.setRoom(room);
        message.setSender(sender);
        message.setContent(req.content());
        messageRepository.save(message);

        return toDto(message);
    }

    @Transactional(readOnly = true)
    public Page<MessageDto> getHistory(String roomId, int page, int size) {
        return messageRepository.findByRoomId(roomId, PageRequest.of(page, size))
                .map(this::toDto);
    }

    private MessageDto toDto(Message m) {
        return new MessageDto(
                m.getId(),
                m.getRoom().getId(),
                m.getSender().getId(),
                m.getSender().getDisplayName(),
                m.getContent(),
                m.getCreatedAt()
        );
    }
}
