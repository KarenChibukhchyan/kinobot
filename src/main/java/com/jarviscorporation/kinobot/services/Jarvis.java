package com.jarviscorporation.kinobot.services;

import com.jarviscorporation.kinobot.domain.Movie;
import com.jarviscorporation.kinobot.domain.Seance;
import com.jarviscorporation.kinobot.mappers.MovieMapper;
import com.jarviscorporation.kinobot.mappers.SeanceMapper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import java.util.*;

public class Jarvis extends TelegramLongPollingBot implements ApplicationContextAware {

    //variable for storing current chat ID
    private Long chatID;

    //hashmap in which are stored chatID as key
    // and last message time as value
    private Map<Long, Long> chatIDs = new HashMap<>();

    //application context variable which is retrieved in
    //setApplicationContext() method
    private ApplicationContext applicationContext;

    private JdbcTemplate jdbcTemplate;

    private MovieMapper movieMapper;
    private SeanceMapper seanceMapper;

    /**
     * MAIN METHOD WHICH RECEIVES EVENTS FROM TELEGRAM BOT
     * @param update
     */
    public void onUpdateReceived(Update update) {

        getChatID(update);

        if (!validaion(update)) return;

        String responseData = update.getCallbackQuery().getData();
        String responseMessage = update.getCallbackQuery().getMessage().getText();

        switch (responseMessage) {
            case ("Choose command"):{
                if (responseData.equals("showseances")) showDays();
                else showMovies(update);
                return;
            }
            case ("Movies:"): {
                showMovieDescription(responseData);
                return;
            }
            case ("Invalid command. Please choose button or start again"):{
                showWelcomeMessage(update);
                return;
            }
            case ("Choose day"): {
                showSeances(responseData);
                return;
            }


        }
    }

    /**
     * shows seances of Today, Tomorrow and Day after tomorrow
     * @param day
     */
    private void showSeances(String day) {

        List<Seance> seances = null;

        switch (day){
            case ("today"):{
                seances = jdbcTemplate.query(
                                "select seanceID,hallID,movieID,startTime,duration,movie from\n" +
                                        "(select * from seances\n" +
                                        "where startTime between now() and (select addtime(CURDATE(), '23:59:59') as t)) as t1\n" +
                                        "join\n" +
                                        "(select movieID as mm, movie from movies) as t2\n" +
                                        "on t1.movieID = t2.mm\n" +
                                        "order by startTime asc ",
                        seanceMapper);
                break;
            }
        }
        SendMessage message = new SendMessage().setChatId(chatID).setText("Today seances:");

        message.setReplyMarkup(new InlineKeyboardMarkup());

        for (Seance seance:seances) {
            addButton(
                    message,
                    seance.getStartTime().toString()+" Hall#"+ seance.getHallID()+" "+seance.getMovie(),
                    Integer.toString(seance.getSeanceID())
            );
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        System.out.println(seances);
    }
    private void showDays(){

        SendMessage message = InlineKeyboardBuilder.create(chatID)
                .setText("Choose day")
                .row()
                .button("Today", "today")
                .button("Tomorrow", "tomorrow")
                .button("Day after tomorrow", "dayaftertomorrow")
                .endRow()
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void showMovieDescription(String data) {
    }

    /**
     * method for retrieving movies from table "Movies"
     * @param update
     */
    private void showMovies(Update update) {

        List<Movie> movies = jdbcTemplate.query("SELECT * FROM MOVIES", movieMapper);

        SendMessage message = new SendMessage().setChatId(chatID).setText("Movies:");

        message.setReplyMarkup(new InlineKeyboardMarkup());
        for (Movie movie:movies) {
            addButton(message, movie.getMovie(), movie.getMovie());
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return;
    }

    /**
     * this method adds button to SendMessage
     * @param message: SendMessage message to which button will be added
     * @param buttonText: text to be placed on button
     * @param callbackData: identifier of this button
     * @return message which contains this button
     */
    private SendMessage addButton(SendMessage message, String buttonText, String callbackData) {

        InlineKeyboardMarkup keyboardMarkup = (InlineKeyboardMarkup) message.getReplyMarkup();

        List<List<InlineKeyboardButton>> keyboard = keyboardMarkup.getKeyboard();

        List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(new InlineKeyboardButton().setText(buttonText).setCallbackData(callbackData));

        keyboard.add(row);

        return message;
    }

    /**
     * does followes:
     * 1 if this is very first chat with bot - shows welcome message
     * 2 if inline button was clicked which is older than 30 minutes -  breaks running
     * 3 if was received message after 30 minutes of idle - shows welcome message
     * 4 if was received message - calls InvalidCommand method
     * @param update
     * @return
     */
    private boolean validaion(Update update) {

        Long now = System.currentTimeMillis() / 1000;

        if (!chatIDs.keySet().contains(chatID)) {
            chatIDs.put(chatID, now);
            showWelcomeMessage(update);
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

    /**
     * shows invalid command message and offers start again
     */
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

    /**
     * gets chat ID
     * @param update
     */
    private void getChatID(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()){

            chatID = update.getMessage().getChatId();

        } else if (update.hasCallbackQuery()) {

            chatID = update.getCallbackQuery().getMessage().getChatId();
        }
        return;
    }

    /**
     * shows greeting message and calls method for showing See Seances and See Movies
     * @param update
     */
    private void showWelcomeMessage(Update update) {

        SendMessage message;

                message = new SendMessage()
                .setText("Hello and welcome to our Kinohall!\n" +
                        "My name is Jarvis and I'm Kinobot.\n" +
                        "I'll help you to book tickets\n"+
                        "For navigation through menu use buttons below\n"+
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

    /**
     * Shows buttons SEE MOVIES and SEE SEANCES
     * @param update
     */
    private void showSeancesAndMovies(Update update) {

        SendMessage message = InlineKeyboardBuilder.create(chatID)
                    .setText("Choose command")
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

    /**
     * necessary method for bot registration
     * needed for Telegram server
     *
     * @return
     */
    @Override
    public String getBotUsername() {
        return "Jarvis_Karen_Bot";
    }

    /**
     * necessary method for bot registration
     * needed for Telegram server
     * @return
     */
    @Override
    public String getBotToken() {
        return "705910420:AAGbp2pTLE7Uco9Dl0F1q2VHN5xc4JuDh4M";
    }

    /**
     * this method invokes Application Context
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
         movieMapper = applicationContext.getBean(MovieMapper.class);
         jdbcTemplate= applicationContext.getBean(JdbcTemplate.class);
         seanceMapper = applicationContext.getBean(SeanceMapper.class);
    }
}
