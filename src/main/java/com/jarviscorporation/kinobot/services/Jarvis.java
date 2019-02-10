package com.jarviscorporation.kinobot.services;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;


public class Jarvis extends TelegramLongPollingBot {



    public void onUpdateReceived(Update update) {

        System.out.println(update.getMessage().getFrom().getFirstName() + " : " + update.getMessage().getText());

        if (update.hasMessage() && update.getMessage().hasText()) {
            String msg = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();


            if (msg.startsWith("hello") || msg.startsWith("Hello") || msg.startsWith("Hey") || msg.startsWith("Hi")) {
                SendMessage sendMessage = new SendMessage()
                        .setChatId(chat_id)
                        .setText("Hey " + update.getMessage().getFrom().getFirstName() + "\n Please Select Command and let's start");
                try {
                    sendMessage(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
