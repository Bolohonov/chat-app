package main

import (
	"encoding/json"
	"log"
	"sync"
)

// Hub maintains the set of active clients organized by room
type Hub struct {
	rooms      map[string]map[*Client]bool
	broadcast  chan *Message
	register   chan *Client
	unregister chan *Client
	mu         sync.RWMutex
	chatClient *ChatClient
}

type Message struct {
	RoomID  string `json:"roomId"`
	UserID  string `json:"userId"`
	Content string `json:"content"`
	Type    string `json:"type"`
	Token   string `json:"-"` // не сериализуется наружу
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

			h.broadcast <- &Message{
				RoomID:  client.roomID,
				UserID:  client.userID,
				Type:    "join",
				Content: client.userID + " joined",
			}
			log.Printf("Client %s joined room %s", client.userID, client.roomID)

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

			h.broadcast <- &Message{
				RoomID:  client.roomID,
				UserID:  client.userID,
				Type:    "leave",
				Content: client.userID + " left",
			}
			log.Printf("Client %s left room %s", client.userID, client.roomID)

		case msg := <-h.broadcast:
			// Сохраняем в chat-service только обычные сообщения
			if msg.Type == "message" {
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
					// Slow client - drop and close
					close(client.send)
					delete(clients, client)
				}
			}
			h.mu.RUnlock()
		}
	}
}
