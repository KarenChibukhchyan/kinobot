package com.jarviscorporation.kinobot.services;

import com.jarviscorporation.kinobot.domain.Movie;
import com.jarviscorporation.kinobot.domain.Place;
import com.jarviscorporation.kinobot.domain.Seance;
import com.jarviscorporation.kinobot.mappers.MovieMapper;
import com.jarviscorporation.kinobot.mappers.PlaceMapper;
import com.jarviscorporation.kinobot.mappers.SeanceMapper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.File;
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

    //this is case when button was pressed from image
    if (responseMessage!=null)
        switch (responseMessage) {
            case ("Choose command"):{
                if (responseData.equals("showseances")) showDays();
                else showMovies();
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
            case ("Today seances:"):{
                int hallID = Integer.parseInt(responseData.substring(0,1));
                int seanceID = Integer.parseInt(responseData.substring(1));
                showImageForBooking(hallID,seanceID);
                return;
            }
        }
    else
        //this is case when button pressed not from message
        //for example from photo message
        if (responseData!= null){
            switch (responseData){
                case ("startagain"):{
                    showSeancesAndMovies(update);
                    return;
                }
            }
        }
    }

    /**
     * this method retrieves:
     * 1 hall's size
     * 2 booked places for this seance
     * 3 creates new boolean[][] with places to be draw
     * 4 passes these info to ImageCreator class
     * 5 sends created image file to chat
     * @param hallID
     * @param seanceID
     */
    private void showImageForBooking(int hallID, int seanceID) {

        PlaceMapper placeMapper = new PlaceMapper();

        List<Integer> rowsTemp = jdbcTemplate.query("select b.rows, b.seats from halls s\n" +
                "join hallinfo b on s.hallID = b.hallID\n" +
                "where s.hallID="+hallID , (resultSet, i) -> resultSet.getInt("rows"));
        List<Integer> seatsTemp = jdbcTemplate.query("select b.rows, b.seats from halls s\n" +
                "join hallinfo b on s.hallID = b.hallID\n" +
                "where s.hallID="+hallID , (resultSet, i) -> resultSet.getInt("seats"));

        int rows = rowsTemp.get(0);
        int seats = seatsTemp.get(0);

        List<Place> placesTemp = jdbcTemplate.query("select b.row, b.seat from seances s\n" +
                "join books b on s.seanceID = b.seanceID\n" +
                "where s.seanceID = "+seanceID,placeMapper);

        boolean[][] placesToDraw = new boolean[rows][seats];
        for(Place place : placesTemp){
            placesToDraw[place.getRow()][place.getSeat()] = true;
        }
        ImageCreator.createImage(hallID,seanceID,placesToDraw,"red");

        SendPhoto sendPhoto = new SendPhoto().setChatId(chatID);

        sendPhoto.setNewPhoto(new File("src/main/resources/"+seanceID+".png"));

        InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(new InlineKeyboardButton()
                .setText("Press for booking places")
                .setCallbackData("pressforbookingplaces"));
        row.add(new InlineKeyboardButton()
                .setText("Start again?")
                .setCallbackData("startagain"));

        keyboard.add(row);
        replyKeyboardMarkup.setKeyboard(keyboard);

        sendPhoto.setReplyMarkup(replyKeyboardMarkup);

        try {
            sendPhoto(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return;
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
            case ("tomorrow"):{
                seances = jdbcTemplate.query(
                        "select seanceID,hallID,movieID,startTime,duration,movie from\n" +
                                "(select * from seances\n" +
                                "where startTime between (select addtime(CURDATE()+1, '00:00:00') as t) and (select addtime(CURDATE()+1, '23:59:59') as t1)) as t2\n" +
                                "join\n" +
                                "(select movieID as mm, movie from movies) as t3\n" +
                                "on t2.movieID = t3.mm\n" +
                                "order by startTime asc ",
                        seanceMapper);
                break;
            }
            case ("dayaftertomorrow"):{
                seances = jdbcTemplate.query(
                        "select seanceID,hallID,movieID,startTime,duration,movie from\n" +
                                "(select * from seances\n" +
                                "where startTime between (select addtime(CURDATE()+2, '00:00:00') as t) and (select addtime(CURDATE()+2, '23:59:59') as t1)) as t2\n" +
                                "join\n" +
                                "(select movieID as mm, movie from movies) as t3\n" +
                                "on t2.movieID = t3.mm\n" +
                                "order by startTime asc ",
                        seanceMapper);
                break;
            }

        }
        //this checks that data from databese is not empty
        if (seances.isEmpty()){
            SendMessage message = new SendMessage()
                    .setChatId(chatID)
                    .setText("No seances availabale" +
                            "\nPlease try again later");
          try{
              execute(message);
          }catch (TelegramApiException e){

          }
            return;
        }
        SendMessage message = new SendMessage().setChatId(chatID).setText("Today seances:");

        message.setReplyMarkup(new InlineKeyboardMarkup());

        for (Seance seance:seances) {
            String time = seance.getStartTime().toString();
            time = time.substring(0,time.length()-3);
            addButton(
                    message,
                    time+" Hall#"+ seance.getHallID()+" "+seance.getMovie(),
                    Integer.toString(seance.getHallID())+seance.getSeanceID()
            );
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    /**
     * Shows buttons Today Tomorrow and Day after tomorrow
     */
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

    /**
     *  TODO ARMAN !!!!!!!!!!!!!!!!!!!!!!!!!
     * @param data
     */
    private void showMovieDescription(String data) {
    }

    /**
     * method for retrieving movies from table "Movies"
     */
    private void showMovies() {

        List<Movie> movies = jdbcTemplate.query("SELECT * FROM MOVIES", movieMapper);

        if (movies.isEmpty()){
            SendMessage message = new SendMessage()
                    .setChatId(chatID)
                    .setText("No movie is availabale" +
                            "\nPlease try later");
            return;
        }
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
        return "vahe_jarvis_bot";
        //return "Jarvis_Karen_Bot";
    }

    /**
     * necessary method for bot registration
     * needed for Telegram server
     * @return
     */
    @Override
    public String getBotToken() {
        return "752324160:AAGr225EVbI9y9alRdcvUcRPU6qUZGylXTk";
        //return "705910420:AAGbp2pTLE7Uco9Dl0F1q2VHN5xc4JuDh4M";
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
