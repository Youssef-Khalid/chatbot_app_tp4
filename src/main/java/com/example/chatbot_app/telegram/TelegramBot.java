package com.example.chatbot_app.telegram;

import com.example.chatbot_app.agents.AIAgent;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.springframework.ai.content.Media;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

    /*@Override
    public void onUpdateReceived(Update telegramRequest) { // une méthode qui s'exécute quand un client telegram envoie
                                                           // un message
        try {
            if (!telegramRequest.hasMessage())
                return;
            String messageText = telegramRequest.getMessage().getText();
            Long chatId = telegramRequest.getMessage().getChatId();
            List<PhotoSize> photos =telegramRequest.getMessage().getPhoto();
            String caption = telegramRequest.getMessage().getCaption();
            List<Media> mediaList = new ArrayList<>();
            for (PhotoSize ps : photos) {
                String fileId = ps.getFileId();
                GetFile getFile = new GetFile();
                getFile.setFileId(fileId);
                File file = execute(getFile);
                String filePath = file.getFilePath();
                String textUrl ="https://api.telegram.org/file/bot" + getBotToken() + "/" + file.getFilePath();
                URL fileUrl = new URL(textUrl);
                mediaList.add(Media.builder()
                                .id(fileId)
                                .mimeType(MimeTypeUtils.IMAGE_PNG)
                                .data(new UrlResource(fileUrl))
                                .build());
            }
            String query = messageText!=null?messageText:caption;
            UserMessage userMessage = UserMessage.builder()
                            .text(query)
                            .media(mediaList)
                            .build();
            sendTypingQuestion(chatId);

            System.out.println("Received message from " + chatId + ": " + messageText);

            String answer;
            try {
                answer = aiAgent.askAgent(new Prompt(userMessage));
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
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

    @Override
    public void onUpdateReceived(Update telegramRequest) {
        try {
            if (!telegramRequest.hasMessage()) return;

            Long chatId = telegramRequest.getMessage().getChatId();
            String messageText = telegramRequest.getMessage().getText();
            String caption = telegramRequest.getMessage().getCaption();
            List<PhotoSize> photos = telegramRequest.getMessage().getPhoto();

            List<Media> mediaList = new ArrayList<>();

            // 1. Handle Photos (only if present)
            if (photos != null && !photos.isEmpty()) {
                // Get the last element (Telegram always puts the largest version at the end)
                PhotoSize largestPhoto = photos.get(photos.size() - 1);

                GetFile getFile = new GetFile();
                getFile.setFileId(largestPhoto.getFileId());
                File file = execute(getFile);

                String textUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + file.getFilePath();
                URL fileUrl = new URL(textUrl);

                // Use IMAGE_JPEG usually, as Telegram converts most uploads to JPEG
                mediaList.add(new Media(MimeTypeUtils.IMAGE_JPEG, new UrlResource(fileUrl)));
            }

            // 2. Determine the Query (Text or Caption)
            String query = (messageText != null) ? messageText : caption;

            // 3. Validation: If no text and no photo, don't call the AI
            if (query == null && mediaList.isEmpty()) {
                sendTextMessage(chatId, "Please send a message or a photo!");
                return;
            }

            System.out.println("Received message from " + chatId + ": " + (query != null ? query : "[Image Only]"));
            sendTypingQuestion(chatId);

            // 4. Build Prompt and Call AI
            // Build the UserMessage using the Fluent API
            UserMessage userMessage = UserMessage.builder()
                    .text(query != null ? query : "What is in this image?")
                    .media(mediaList) // Accepts List<Media>
                    .build();

            String answer;
            try {
                answer = aiAgent.askAgent(new Prompt(userMessage));
                System.out.println("AI Response: " + answer);
            } catch (Exception e) {
                answer = "Sorry, I had trouble processing that.";
                e.printStackTrace();
            }

            sendTextMessage(chatId, answer);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
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

    private void sendTypingQuestion(long chatid) throws TelegramApiException {
        SendChatAction sendChatAction = new SendChatAction();
        sendChatAction.setChatId(String.valueOf(chatid));
        sendChatAction.setAction(ActionType.TYPING);
        execute(sendChatAction);
    }
}
