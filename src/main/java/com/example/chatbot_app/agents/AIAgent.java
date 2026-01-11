package com.example.chatbot_app.agents;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;

import java.util.Arrays;

@Component
public class AIAgent {
        private ChatClient chatClient;

        public AIAgent(ChatClient.Builder builder,
                        ChatMemory memory, ToolCallbackProvider tools) {
                Arrays.stream(tools.getToolCallbacks()).forEach(toolCallback -> {
                        System.out.println("---------------------");
                        System.out.println(toolCallback.getToolDefinition());
                        System.out.println("---------------------");
                });

                /*this.chatClient = builder
                                .defaultSystem("""
                                                Vous un assistant qui se charge de répondre aux question
                                                de l'utilisateur en fonction du contexte fourni.
                                                Si aucun contexte n'est fourni, répond avec JE NE SAIS PAS
                                                """)
                                .defaultAdvisors(
                                                MessageChatMemoryAdvisor.builder(memory).build())
                                .defaultToolCallbacks(tools)

                                .build();*/
                this.chatClient = builder
                        .defaultSystem("""
        Tu es un assistant utile. 
        Utilise les outils à ta disposition pour récupérer les informations nécessaires.
        Ne dis que "JE NE SAIS PAS" uniquement si, après avoir utilisé les outils, 
        tu n'as toujours pas trouvé la réponse.
        """)
                        .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
                        .defaultToolCallbacks(tools)
                        .build();
        }

        @GetMapping("/chat")
        public String askAgent(Prompt prompt) {
                System.out.println("AIAgent received query: " + prompt);
                String response = chatClient.prompt(prompt)
                                .call().content();
                System.out.println("AIAgent generated response: " + response);
                return response;
        }
}
