package com.chat.repository;

import com.chat.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, String> {

    @Query("SELECT r FROM Room r JOIN r.members m WHERE m.id = :userId")
    List<Room> findByMemberId(String userId);

    @Query("SELECT r FROM Room r JOIN r.members m WHERE m.id = :userId AND LOWER(r.name) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Room> searchByNameAndMember(String userId, String q);
}
