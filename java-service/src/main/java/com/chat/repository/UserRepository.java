package com.chat.repository;

import com.chat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Used by SearchController
    List<User> findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(String username, String displayName);

    default List<User> searchByUsernameOrDisplayName(String q) {
        return findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(q, q);
    }

    @Query("SELECT u FROM User u JOIN u.rooms r WHERE r.id = :roomId")
    List<User> findMembersByRoomId(@Param("roomId") String roomId);
}
