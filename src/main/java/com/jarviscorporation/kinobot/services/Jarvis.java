package com.jarviscorporation.kinobot.services;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.*;

public class Jarvis extends TelegramLongPollingBot {

    private Long chatID;

    private Map<Long, Long> chatIDs = new HashMap<>();


    public void onUpdateReceived(Update update) {

        getChatID(update);

        if (!validaion(update)) return;

        String response = update.getCallbackQuery().getData();

        switch (response) {
                     case ("startagain"):
                         showWelcomeMessage(update);
                         break;
                 }
        return;
    }

    private boolean validaion(Update update) {

        Long now = System.currentTimeMillis() / 1000;

        if (!chatIDs.keySet().contains(chatID)) {
            showWelcomeMessage(update);
            chatIDs.put(chatID, now);
            return false;
        }
        if (update.hasCallbackQuery() &&
                now - update.getCallbackQuery().getMessage().getDate()>1800){
            return false;
        }

        if (update.hasMessage() && now - chatIDs.get(chatID)>1800) {
            showWelcomeMessage(update);
            chatIDs.put(chatID, now);
            return false;
        }
        if (update.hasMessage() && update.getMessage().hasText()) {
            invalidCommand();
            chatIDs.put(chatID, now);
            return false;
        }
        return true;
    }

    private void invalidCommand() {

        SendMessage message = InlineKeyboardBuilder.create(chatID)
                .setText("Invalid command. Please choose button or start again")
                .row()
                .button("START AGAIN?", "startagain")
                .endRow()
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return;
    }

    private void getChatID(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()){

            chatID = update.getMessage().getChatId();

        } else if (update.hasCallbackQuery()) {

            chatID = update.getCallbackQuery().getMessage().getChatId();
        }
        return;
    }


    private void showWelcomeMessage(Update update) {

        SendMessage message;

                message = new SendMessage()
                .setText("Hello and welcome to our Kinohall!\n" +
                        "My name is Jarvis and I'm Kinobot.\n" +
                        "I'll help you to book tickets\n"+
                        "For navigation through menu use buttons below"+
                        "Please note that buttons are active half hour")
                .setChatId(chatID);
            try {
                execute(message);
                showSeancesAndMovies(update);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        return;
     }

    private void showSeancesAndMovies(Update update) {

        SendMessage message = InlineKeyboardBuilder.create(chatID)
                    .setText("Choose command...")
                    .row()
                    .button("SHOW MOVIES", "showmovies")
                    .button("SHOW SEANCES", "showseances")
                    .endRow()
                    .build();
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        return;
    }

    @Override
    public String getBotUsername() {
        return "Jarvis_Karen_Bot";
    }

    @Override
    public String getBotToken() {
        return "705910420:AAGbp2pTLE7Uco9Dl0F1q2VHN5xc4JuDh4M";
    }
}
