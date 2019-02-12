package com.jarviscorporation.kinobot.services;
/**
 * A STARTER CLASS FOR JARVIS CLASS
 * RETRIEVES JARVIS'S BEAN FROM APPLICATION CONTEXT
 * AND DOES REGISTRATION FOR IT
 */

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

public class JarvisStarter implements ApplicationContextAware {

    ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;

        ApiContextInitializer.init();

        TelegramBotsApi botsApi = new TelegramBotsApi();

        Jarvis jarvis = context.getBean(Jarvis.class);

        try {
            botsApi.registerBot(jarvis);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }
}
