package com.example.chatbot_app.agents;

import com.example.chatbot_app.tools.AITools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;

@Component
public class AIAgent {
    private ChatClient chatClient;

    public AIAgent(ChatClient.Builder builder,
                   ChatMemory memory,
                   AITools tools) {
        this.chatClient = builder
                .defaultSystem("""
                        Vous êtes un assistant qui se charge de répondre aux question 
                        de l'utilisateur en fonction du contexte fourni.
                        
                        Si aucun contexte n'est fourni, répond acec JE NE SAIS PAS
                        """)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(memory).build()
                )
                .defaultTools(tools)
                .build();
    }

    @GetMapping("/chat")
    public Flux<String> askAgent(String query) {
        return chatClient.prompt()
                .user(query)
                .stream().content();
    }
}
