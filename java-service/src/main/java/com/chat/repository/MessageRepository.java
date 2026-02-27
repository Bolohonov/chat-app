package com.chat.repository;

import com.chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessageRepository extends JpaRepository<Message, String> {

    @Query("SELECT m FROM Message m WHERE m.room.id = :roomId AND m.deleted = false ORDER BY m.createdAt DESC")
    Page<Message> findByRoomId(String roomId, Pageable pageable);
}
