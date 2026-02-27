package main

import (
	"log"
	"net/http"
	"os"

	"github.com/gorilla/mux"
)

func main() {
	chatServiceURL := os.Getenv("CHAT_SERVICE_URL")
	if chatServiceURL == "" {
		chatServiceURL = "http://localhost:8081"
	}

	hub := NewHub(NewChatClient(chatServiceURL))
	go hub.Run()

	r := mux.NewRouter()

	r.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte(`{"status":"ok"}`))
	}).Methods("GET")

	r.HandleFunc("/ws/{roomId}", func(w http.ResponseWriter, r *http.Request) {
		vars := mux.Vars(r)
		ServeWs(hub, w, r, vars["roomId"])
	})

	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}

	log.Printf("Go WebSocket Gateway starting on :%s", port)
	if err := http.ListenAndServe(":"+port, r); err != nil {
		log.Fatal(err)
	}
}
