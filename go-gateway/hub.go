package main

import (
	"encoding/json"
	"log"
	"sync"
	"time"
)

type Hub struct {
	rooms      map[string]map[*Client]bool
	broadcast  chan *Message
	register   chan *Client
	unregister chan *Client
	mu         sync.RWMutex
	chatClient *ChatClient
}

type Message struct {
	RoomID     string `json:"roomId"`
	UserID     string `json:"userId"`
	SenderName string `json:"senderName,omitempty"`
	Content    string `json:"content"`
	Type       string `json:"type"`
	CreatedAt  string `json:"createdAt,omitempty"`
	Token      string `json:"-"`
}

func NewHub(chatClient *ChatClient) *Hub {
	return &Hub{
		rooms:      make(map[string]map[*Client]bool),
		broadcast:  make(chan *Message, 256),
		register:   make(chan *Client),
		unregister: make(chan *Client),
		chatClient: chatClient,
	}
}

func (h *Hub) broadcastMembers(roomID string) {
	h.mu.RLock()
	var members []map[string]string
	for client := range h.rooms[roomID] {
		members = append(members, map[string]string{
			"userId":      client.userID,
			"displayName": client.displayName,
		})
	}
	data, _ := json.Marshal(map[string]interface{}{
		"type":    "members",
		"roomId":  roomID,
		"members": members,
	})
	for client := range h.rooms[roomID] {
		select {
		case client.send <- data:
		default:
		}
	}
	h.mu.RUnlock()
}

func (h *Hub) Run() {
	for {
		select {
		case client := <-h.register:
			h.mu.Lock()
			if _, ok := h.rooms[client.roomID]; !ok {
				h.rooms[client.roomID] = make(map[*Client]bool)
			}
			h.rooms[client.roomID][client] = true
			h.mu.Unlock()
			log.Printf("Client %s (%s) joined room %s", client.userID, client.displayName, client.roomID)
			h.broadcastMembers(client.roomID)

		case client := <-h.unregister:
			h.mu.Lock()
			if clients, ok := h.rooms[client.roomID]; ok {
				if _, ok := clients[client]; ok {
					delete(clients, client)
					close(client.send)
					if len(clients) == 0 {
						delete(h.rooms, client.roomID)
					}
				}
			}
			h.mu.Unlock()
			log.Printf("Client %s left room %s", client.userID, client.roomID)
			h.broadcastMembers(client.roomID)

		case msg := <-h.broadcast:
			if msg.Type == "message" {
				msg.CreatedAt = time.Now().UTC().Format(time.RFC3339)
				go func(m *Message) {
					if err := h.chatClient.SaveMessage(m); err != nil {
						log.Printf("Failed to save message from %s: %v", m.UserID, err)
					}
				}(msg)
			}
			h.mu.RLock()
			clients := h.rooms[msg.RoomID]
			data, _ := json.Marshal(msg)
			for client := range clients {
				select {
				case client.send <- data:
				default:
					close(client.send)
					delete(clients, client)
				}
			}
			h.mu.RUnlock()
		}
	}
}
