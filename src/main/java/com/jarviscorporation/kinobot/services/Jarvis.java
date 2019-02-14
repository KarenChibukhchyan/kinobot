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
    private boolean enteringMode = false;

    private boolean[][] placesToDraw;
    private int row = 0;
    private Integer[] seats = null;
    private int hallSizeRow = 0;
    private int hallSizeSeats = 0;
    private int hallID;
    private int seanceID;
    /**
     * MAIN METHOD WHICH RECEIVES EVENTS FROM TELEGRAM BOT
     *
     * @param update
     */
    public void onUpdateReceived(Update update) {

        getChatID(update);
        String responseData = null;
        String responseMessage = null;

        if (update.hasCallbackQuery()) {

            enteringMode = false;
            row = 0;
            seats = null;
            if (update.getCallbackQuery().getData() != null) {
                responseData = update.getCallbackQuery().getData();
            }
            if (update.getCallbackQuery().getMessage().getText() != null) {
                responseMessage = update.getCallbackQuery().getMessage().getText();
            }
        }

        if (!validaion(update)) return;
        //case when user enters row and seat numbers for booking
        if (enteringMode && update.hasMessage() && update.getMessage().getText() != null) {
            proposeBooking(update);
            return;
        }

        //this is case when button was pressed from message with button
        if (responseMessage != null)
            switch (responseMessage) {
                case ("Choose command"): {
                    if (responseData.equals("showseances")) showDays();
                    else showMovies();
                    return;
                }
                case ("Movies:"): {
                    showMovieDescription(responseData);
                    return;
                }
                case ("Invalid command. Please choose button or start again"): {
                    showWelcomeMessage(update);
                    return;
                }
                case ("Choose day"): {
                    showSeances(responseData);
                    return;
                }

                case ("Today seances:"): {
                    hallID = Integer.parseInt(responseData.substring(0, 1));
                    seanceID = Integer.parseInt(responseData.substring(1));
                    showImageForBooking(hallID, seanceID);
                    return;
                }
            }

            //this is case when button pressed not from message
            //for example from photo message
            if (responseData != null) {
                switch (responseData) {

                    case ("confirm"): {
                        addBook(placesToDraw);
                        return;
                    }
                    case ("startagain"): {
                        showSeancesAndMovies(update);
                        return;
                    }
                    case ("pressforbookingplaces"): {
                        SendMessage message = new SendMessage()
                                .setText("Enter row").setChatId(chatID);
                        enteringMode = true;
                        try {
                            execute(message);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }
            }
    }

    private void addBook(boolean[][] placesToDraw) {

        if(placesToDraw == null) return;

        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        SendMessage sendMessage = InlineKeyboardBuilder.create(chatID)
                .setText("You've just booked above mentioned places. " +
                        "\nYour booking code is "+number+
                        "\nNOTE!!! Book will be automatically cancelled if you dont buy tickets" +
                        "\n earlier than 30 minutes before beginning of movie seance!!!")
                .row()
                .button("Start new booking?","startagain")
                .endRow()
                .build();
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < placesToDraw.length; i++) {
            for (int j = 0; j < placesToDraw[0].length; j++) {
                if (placesToDraw[i][j]) {

                    jdbcTemplate.update(
                            "INSERT INTO books (" +
                            "seanceID," +
                            "row," +
                            "seat," +
                            "bookCode) " +
                            "VALUES (?,?,?,?)",
                            seanceID,i,j,number);
                }
            }
        }
        hallID=0;
        seanceID=0;
        row=0;
        placesToDraw=null;
        seats=null;
        return;
    }

    private void proposeBooking(Update update) {

        String text = update.getMessage().getText();

        try {
            //case for obtaining row
            if (row == 0) {

                row = Integer.parseInt(text);
                if (row <= 0 || row > hallSizeRow) {
                    row = 0;
                    throw new NumberFormatException();
                }
                SendMessage message = new SendMessage()
                        .setText("Your row is " + row + "\nPlease enter seats numbers\n" +
                                "You can use following formats\n" +
                                "1,2,3 or 1 - 10 or 1,2,3-10").setChatId(chatID);
                try {
                    execute(message);
                    return;
                } catch (TelegramApiException e1) {
                    e1.printStackTrace();
                    return;
                }
            }

            //case for obtaining seats
            else {
                List<Integer> list = new ArrayList<>();

                if (text.contains(",") && !text.contains("-")) {
                    list = commaSplitter(text);

                }
                else
                    if (text.contains("-") && !text.contains(",")) {
                        list = defisSplitter(text);
                    }
                else
                    if (text.contains(",") && text.contains("-")){
                        list = commaSplitter(text, true);
                    }
                else {
                        int i = Integer.parseInt(text);
                        if (i <= 0 || i > hallSizeSeats) throw new NumberFormatException();
                        list.add(i);

                    }
                list.sort(Comparator.naturalOrder());

                Set<Integer> set = new HashSet<>(list);
                list.clear();
                list.addAll(set);
                seats = list.toArray(new Integer[list.size()]);

            }

            if (placesToDraw!=null){
                for (Integer seat : seats) {
                    if (placesToDraw[row-1][seat]){

                        SendMessage sendMessage = new SendMessage()
                                .setText("Place(s) is already occupied. Try again")
                                .setChatId(chatID);
                        try {
                            execute(sendMessage);
                            seats=null;
                            return;
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < seats.length; i++) {
                if (i==seats.length-1)
                str.append(seats[i]);
                else
                str.append(seats[i]+",");
            }
            SendMessage message = InlineKeyboardBuilder.create(chatID)
            .setText("Your places: " +
                    "row="+row+
                    "  seats= "+str.toString()+
                    "\nConfirm booking?")
            .row()
            .button("Confirm","confirm")
            .button("Start again","startagain")
            .endRow()
            .build();

            placesToDraw = new boolean[hallSizeRow][hallSizeSeats];
            for (int i = 0; i < seats.length; i++) {
                placesToDraw[row-1][seats[i]-1] = true;
            }

            ImageCreator.createImage(hallID,seanceID,placesToDraw,"greem");

            SendPhoto sendPhoto = new SendPhoto().setChatId(chatID);

            sendPhoto.setNewPhoto(new File("src/main/resources/" + seanceID + ".png"));

            try {
                sendPhoto(sendPhoto);
                execute(message);
                enteringMode=false;
                return;
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        } catch (NumberFormatException e) {
            SendMessage sendMessage = new SendMessage()
                    .setText("Invalid format. Please use correct format")
                    .setChatId(chatID);

            try {
                execute(sendMessage);
                return;
            } catch (TelegramApiException e1) {
                e1.printStackTrace();
            }
        }
    }

    private List<Integer> defisSplitter(String text) {

        List<Integer> list = new ArrayList<>();

        String[] strings = text.split("-");

        for (String string : strings) {
            list.add(Integer.parseInt(string));
        }

        for (int i : list) {
            if (i <= 0 || i > hallSizeSeats) {
                throw new NumberFormatException();
            }
        }
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i) >= list.get(i + 1)) throw new NumberFormatException();
        }

        List<Integer> values = new ArrayList<>();
        int k = list.get(0);
        for (int i = 0; i < list.get(list.size()-1)-list.get(0)+1; i++) {

            values.add(k++);
        }
        return values;
    }
    private List<Integer> commaSplitter (String text) {
        List<Integer> list = new ArrayList<>();

        String[] strings = text.split(",");

        for (String string : strings) {

            string = string.trim();

            list.add(Integer.parseInt(string));
        }
        for (int i : list){
            if (i <= 0 || i > hallSizeSeats) throw new NumberFormatException();
        }
        return list;
    }
    private List<Integer> commaSplitter (String text, boolean hasDefis) {

        List<Integer> list = new ArrayList<>();

        String[] strings = text.split(",");

        for (String string : strings) {

            string = string.trim();

            if (string.contains("-")){
                list.addAll(defisSplitter(string));
            }
            else list.add(Integer.parseInt(string));
        }
        for (int i : list){
            if (i <= 0 || i > hallSizeSeats) throw new NumberFormatException();
        }
        return list;
    }

    /**
     * this method retrieves:
     * 1 hall's size
     * 2 booked places for this seance
     * 3 creates new boolean[][] with places to be draw
     * 4 passes these info to ImageCreator class
     * 5 sends created image file to chat
     *
     * @param hallID
     * @param seanceID
     */
    private void showImageForBooking(int hallID, int seanceID) {

        PlaceMapper placeMapper = new PlaceMapper();

        List<Integer> rowsTemp = jdbcTemplate.query("select b.rows, b.seats from halls s\n" +
                "join hallinfo b on s.hallID = b.hallID\n" +
                "where s.hallID=" + hallID, (resultSet, i) -> resultSet.getInt("rows"));
        List<Integer> seatsTemp = jdbcTemplate.query("select b.rows, b.seats from halls s\n" +
                "join hallinfo b on s.hallID = b.hallID\n" +
                "where s.hallID=" + hallID, (resultSet, i) -> resultSet.getInt("seats"));

        int rows = rowsTemp.get(0);
        int seats = seatsTemp.get(0);
        hallSizeRow = rows;
        hallSizeSeats = seats;

        List<Place> placesTemp = jdbcTemplate.query("select b.row, b.seat from seances s\n" +
                "join books b on s.seanceID = b.seanceID\n" +
                "where s.seanceID = " + seanceID, placeMapper);


        placesToDraw = new boolean[rows][seats];
        for (Place place : placesTemp) {
            placesToDraw[place.getRow()][place.getSeat()] = true;
        }
        ImageCreator.createImage(hallID, seanceID, placesToDraw, "red");

        SendPhoto sendPhoto = new SendPhoto().setChatId(chatID);

        sendPhoto.setNewPhoto(new File("src/main/resources/" + seanceID + ".png"));

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
     * TODO VAHE !!!!!!!!!!!!!!!!!!!!!!!!
     * shows seances of Today, Tomorrow and Day after tomorrow
     *
     * @param day
     */
    private void showSeances(String day) {

        List<Seance> seances = null;

        switch (day) {
            case ("today"): {
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
        //this checks that data from databese is not empty
        if (seances.isEmpty()) {
            SendMessage message = new SendMessage()
                    .setChatId(chatID)
                    .setText("No seances availabale" +
                            "\nPlease try again later");
            try {
                execute(message);
            } catch (TelegramApiException e) {

            }
            return;
        }
        SendMessage message = new SendMessage().setChatId(chatID).setText("Today seances:");

        message.setReplyMarkup(new InlineKeyboardMarkup());

        for (Seance seance : seances) {
            String time = seance.getStartTime().toString();
            time = time.substring(0, time.length() - 3);
            addButton(
                    message,
                    time + " Hall#" + seance.getHallID() + " " + seance.getMovie(),
                    Integer.toString(seance.getHallID()) + seance.getSeanceID()
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
    private void showDays() {

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
     * TODO ARMAN !!!!!!!!!!!!!!!!!!!!!!!!!
     *
     * @param data
     */
    private void showMovieDescription(String data) {
    }

    /**
     * method for retrieving movies from table "Movies"
     *
     * @param update
     */
    private void showMovies() {

        List<Movie> movies = jdbcTemplate.query("SELECT * FROM MOVIES", movieMapper);

        if (movies.isEmpty()) {
            SendMessage message = new SendMessage()
                    .setChatId(chatID)
                    .setText("No movie is availabale" +
                            "\nPlease try later");
            return;
        }
        SendMessage message = new SendMessage().setChatId(chatID).setText("Movies:");

        message.setReplyMarkup(new InlineKeyboardMarkup());
        for (Movie movie : movies) {
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
     *
     * @param message:      SendMessage message to which button will be added
     * @param buttonText:   text to be placed on button
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
     *
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
                now - update.getCallbackQuery().getMessage().getDate() > 1800) {
            return false;
        }

        if (update.hasMessage() && now - chatIDs.get(chatID) > 1800) {
            showWelcomeMessage(update);
            chatIDs.put(chatID, now);
            return false;
        }
        if (update.hasMessage() && update.getMessage().hasText() && !enteringMode) {
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
     *
     * @param update
     */
    private void getChatID(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            chatID = update.getMessage().getChatId();

        } else if (update.hasCallbackQuery()) {

            chatID = update.getCallbackQuery().getMessage().getChatId();
        }
        return;
    }

    /**
     * shows greeting message and calls method for showing See Seances and See Movies
     *
     * @param update
     */
    private void showWelcomeMessage(Update update) {

        SendMessage message;

        message = new SendMessage()
                .setText("Hello and welcome to our Kinohall!\n" +
                        "My name is Jarvis and I'm Kinobot.\n" +
                        "I'll help you to book tickets\n" +
                        "For navigation through menu use buttons below\n" +
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
     *
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
     *
     * @return
     */
    @Override
    public String getBotToken() {
        return "705910420:AAGbp2pTLE7Uco9Dl0F1q2VHN5xc4JuDh4M";
    }
    /**
     * this method invokes Application Context
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        movieMapper = applicationContext.getBean(MovieMapper.class);
        jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);
        seanceMapper = applicationContext.getBean(SeanceMapper.class);
    }
}
