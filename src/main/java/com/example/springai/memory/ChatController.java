package com.example.springai.memory;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(
            summary = "Start context dialog",
            description = "Long-running dialog with context"
    )
    @PostMapping("/ai-chat")
    public ResponseEntity<String> chat(@RequestBody @Valid ChatRequest request) {
        String response = chatService.chat(request.getPrompt());
        return ResponseEntity.ok(response);
    }

}
