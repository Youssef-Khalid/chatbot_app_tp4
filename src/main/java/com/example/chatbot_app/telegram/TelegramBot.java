package com.example.chatbot_app.telegram;

import com.example.chatbot_app.agents.AIAgent;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.api.key}")
    private String telegramBotToken;

    private AIAgent aiAgent;

    public TelegramBot(AIAgent aiAgent) {
        this.aiAgent = aiAgent;
    }

    @PostConstruct // une méthode qui s'exécute juste apres le constructeur
    public void registerTelegramBot() {
        if (telegramBotToken == null || telegramBotToken.isEmpty()) {
            System.out.println("WARN: Telegram Bot Token is missing. Skipping registration.");
            return;
        }
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(this);
            System.out.println("Telegram Bot registered successfully!");
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpdateReceived(Update telegramRequest) { // une méthode qui s'exécute quand un client telegram envoie
                                                           // un message
        try {
            if (!telegramRequest.hasMessage())
                return;
            String messageText = telegramRequest.getMessage().getText();
            Long chatId = telegramRequest.getMessage().getChatId();

            System.out.println("Received message from " + chatId + ": " + messageText);

            String answer;
            try {
                answer = aiAgent.askAgent(messageText);
                System.out.println("AI Response: " + answer);
            } catch (Exception e) {
                System.err.println("Error generating AI response: " + e.getMessage());
                e.printStackTrace();
                answer = "Sorry, I encountered an error while processing your request: " + e.getMessage();
            }

            sendTextMessage(chatId, answer);
        } catch (TelegramApiException e) {
            System.err.println("Telegram API Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "EcomAppChatBot";
    }

    @Override
    public String getBotToken() {
        return telegramBotToken;
    }

    private void sendTextMessage(long chatid, String text) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatid), text);
        execute(sendMessage);
    }
}
