package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"time"
)

type ChatClient struct {
	baseURL    string
	httpClient *http.Client
}

func NewChatClient(baseURL string) *ChatClient {
	return &ChatClient{
		baseURL:    baseURL,
		httpClient: &http.Client{Timeout: 5 * time.Second},
	}
}

func (c *ChatClient) SaveMessage(msg *Message) error {
	body, _ := json.Marshal(map[string]string{
		"content": msg.Content,
	})
	req, err := http.NewRequest(
		"POST",
		fmt.Sprintf("%s/api/rooms/%s/messages", c.baseURL, msg.RoomID),
		bytes.NewBuffer(body),
	)
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", "Bearer "+msg.Token)

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("chat-service returned %d", resp.StatusCode)
	}
	return nil
}
