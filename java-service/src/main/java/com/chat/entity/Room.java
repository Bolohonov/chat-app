package com.chat.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"members", "owner"})
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private RoomType type = RoomType.GROUP;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @CreationTimestamp
    private Instant createdAt;

    @ManyToMany(mappedBy = "rooms", fetch = FetchType.LAZY)
    private Set<User> members;

    public enum RoomType {
        DIRECT, GROUP
    }
}
