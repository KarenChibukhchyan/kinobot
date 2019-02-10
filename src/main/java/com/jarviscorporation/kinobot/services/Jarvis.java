package com.jarviscorporation.kinobot.services;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;


public class Jarvis extends TelegramLongPollingBot {



    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText())
            System.out.println(update.getMessage().getFrom().getFirstName() + " : " + update.getMessage().getText());

        long chat_id = update.getMessage().getChatId();

        SendMessage message = InlineKeyboardBuilder.create(chat_id)
                .setText("Menu:")
                .row()
                .button("Action 1", "action-1")
                .button("Action 2", "action-2")
                .endRow()
                .row()
                .button("Action 3", "action-3")
                .endRow()
                .build();

        try {
            execute(message);
        }catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getBotUsername() {
        return "JarvisMark1_bot";
    }

    @Override
    public String getBotToken() {
        return "607186669:AAFFxaNLWLjQLJLeYp0hDTRbGcb9JIitJqw";
    }
}
