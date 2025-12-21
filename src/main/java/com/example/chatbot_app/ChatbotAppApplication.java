package com.example.chatbot_app;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChatbotAppApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
            System.out.println("Loaded Env Var: " + entry.getKey());
        });
        System.out.println("OPENAI_API_KEY set: " + (System.getProperty("OPENAI_API_KEY") != null));
        System.out.println("TELEGRAM_API_KEY set: " + (System.getProperty("TELEGRAM_API_KEY") != null));

        SpringApplication.run(ChatbotAppApplication.class, args);
    }

}
