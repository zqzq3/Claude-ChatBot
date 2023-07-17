package com.eyu.entity.model;

import com.eyu.entity.model.enums.ModelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    private String model = ModelType.GPT_3_5_TURBO.getName();
    private List<ChatMessage> messages;
    private Float temperature;
    private Float top_p;
    private Integer n;
    private Integer max_tokens;
    private Float presence_penalty;
    private Float frequency_penalty;
    private String user;

    public ChatRequest(ChatMessage chatMessage) {
        this.messages = Collections.singletonList(chatMessage);
    }

    public ChatRequest(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public ChatRequest(String message) {
        this.messages = Collections.singletonList(new ChatMessage(message));
    }

}
