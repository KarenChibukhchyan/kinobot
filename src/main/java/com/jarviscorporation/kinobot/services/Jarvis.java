package com.jarviscorporation.kinobot.services;

import com.jarviscorporation.kinobot.domain.*;
import com.jarviscorporation.kinobot.mappers.*;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Jarvis extends TelegramLongPollingBot implements ApplicationContextAware {

    //storage of Jarvis instances
    public static Map<Long, Jarvis> map = new HashMap<>();

    //variable for storing current chat ID
    private Long chatID;

    //hashmap in which are stored chatID as key
    // and last message time as value
    private Map<Long, Long> chatIDs = new HashMap<>();


    //application context variable which is retrieved in
    //setApplicationContext() method
    private static ApplicationContext applicationContext;
    private static JdbcTemplate jdbcTemplate;
    private static MovieMapper movieMapper;
    private static SeanceMapper seanceMapper;

    //flag which indicates,
    //whether digits input mode is enabled or not
    private boolean enteringMode;
    private int hallSizeRow;
    private int hallSizeSeats;
    private int hallID;
    private int seanceID;
    private int row;

    public Jarvis() {

        enteringMode = false;
        hallSizeRow = 0;
        hallSizeSeats = 0;
        hallID = 0;
        seanceID = 0;
        row = 0;
    }

    /**
     * MAIN METHOD WHICH RECEIVES EVENTS FROM TELEGRAM BOT
     * and for each chat creates new instance of Jarvis
     *  @param
     */
    public void onUpdateReceived(Update update) {

        getChatID(update);
        if (map.keySet().contains(chatID))
            map.get(chatID).process(update);

        else {
            map.put(chatID, new Jarvis());
            map.get(chatID).process(update);
        }
    }

    //processor of incoming messages and button clicking
    public void process(Update update){

        getChatID(update);

        String responseData = null;
        String responseMessage = null;

        if (update.hasCallbackQuery()) {

            enteringMode = false;

            if (update.getCallbackQuery().getData() != null) {
                responseData = update.getCallbackQuery().getData();
            }
            if (update.getCallbackQuery().getMessage().getText() != null) {
                responseMessage = update.getCallbackQuery().getMessage().getText();
            }
        }

        if (!enteringMode) row = 0;

        if (!validaion(update)) return;
        //case when user enters row and seat numbers for booking
        if (enteringMode && update.hasMessage() && update.getMessage().getText() != null) {
            proposeBooking(update);
            return;
        }
        //this is case when button pressed not from message
        //for example from photo message
        if (responseData != null) {
            switch (responseData) {

                case ("startagain"): {
                    showSeancesAndMovies(update);
                    return;
                }
                case ("backtomovielist"): {
                    showMovies();
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
                case ("showmybooks"):{
                    showMyBooks(update);
                    return;
                }

            }
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
                case ("Today's seances:"):
                case ("Tomorrow's seances:"):
                case ("Day after tomorrow's seances:"): {
                    String[] strings = responseData.split(",");

                    hallID = Integer.parseInt(strings[0]);
                    seanceID = Integer.parseInt(strings[1]);
                    showImageForBooking(hallID, seanceID);
                    return;
                }

            }

        if (responseMessage != null && responseMessage.contains("Seances for")) {
            hallID = Integer.parseInt(responseData.substring(0, 1));
            seanceID = Integer.parseInt(responseData.substring(1));
            showImageForBooking(hallID, seanceID);
            return;
        }
        if (responseMessage != null && responseMessage.contains("Book code")) {
            deleteBook(update);
            return;
        }
        if (responseMessage!=null && responseMessage.contains("Age restriction")
                && !responseData.equals("backtomovielist")){
            showSeancesforMovie(responseData);
        }
        if (responseData != null && responseMessage != null && responseMessage.contains("Confirm booking")) {

            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
            editMessageReplyMarkup.setChatId(chatID);
            editMessageReplyMarkup.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
            editMessageReplyMarkup.setReplyMarkup(null);

            EditMessageText text = new EditMessageText();
            text.setChatId(chatID);
            text.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
            String text1 = update.getCallbackQuery().getMessage().getText();
            text1 = text1.substring(0,text1.indexOf("Confirm"));
            text.setText(text1);
            try {
                editMessageReplyMarkup(editMessageReplyMarkup);
                editMessageText(text);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            addBook(responseData);
        }


    }

    private void deleteBook(Update update) {

        jdbcTemplate.update(
                "UPDATE books set recordStatus= \"deleted\""+
                "where bookCode="+update.getCallbackQuery().getData());

        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();

        editMessageReplyMarkup.setChatId(chatID);

        editMessageReplyMarkup.setMessageId(update.getCallbackQuery().getMessage().getMessageId());

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        int c = 0x21A9;

        String g = Character.toString((char)c);
        row.add(new InlineKeyboardButton().setText("Again "+g).setCallbackData("startagain"));

        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);

        editMessageReplyMarkup.setReplyMarkup(keyboardMarkup);

        EditMessageText text = new EditMessageText();
        text.setChatId(chatID);
        text.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        String text1 = update.getCallbackQuery().getMessage().getText()+"\nDeleted!";
        text.setText(text1);
        try {
            editMessageText(text);
            editMessageReplyMarkup(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return;
    }

    private void showMyBooks(Update update) {
        List<Book> books =
                jdbcTemplate.query(
                        "select bookID, s.seanceID, row, seat, bookCode, status, recordStatus,\n"+
                        "chatID, hallID, startTime, duration, price, movie\n"+
                        "from books join seances s on books.seanceID = s.seanceID\n"+
                        "join movies m on s.movieID = m.movieID\n"+
                        "where chatID = "+chatID+
                        "\nand recordStatus = \"active\"", new BookMapper()
                );
        if (books.isEmpty()){
            int c = 0x21A9;

            String g = Character.toString((char)c);
            SendMessage message = InlineKeyboardBuilder.create(chatID)
                    .setText("You dont have any books")
                    .row()
                    .button("Again "+g,"startagain")
                    .endRow()
                    .build();
            try {
                execute(message);
                return;
            } catch (TelegramApiException e) {
                e.printStackTrace();
                return;
            }
        }

        Set<Long> set = new HashSet<>();

        for(Book book:books){
            set.add(book.getBookCode());
        }

        StringBuilder text;

        for (long l : set){
            text = new StringBuilder();

            for (Book book : books){
                if (book.getBookCode()== l) {
                    text.append("Movie " + book.getMovie());
                    text.append("\nTime " + book.getStartTime())
                            .append("\nHall "+book.getHallID())
                            .append("\nBook code "+book.getBookCode());
                    text.append("\nRow "+book.getRow());

                    break;
                }
            }
            text.append("\nSeat(s) ");

            for (Book book : books){
                if (book.getBookCode()== l) {
                    text.append(book.getSeat());
                    text.append(",");

                }
            }
            String str = text.toString();
            if (str.charAt(str.length()-1)==','){
                str=str.substring(0,str.length()-1);
            }
            int c = 0x274E;

            String g = Character.toString((char)c);

            SendMessage message = InlineKeyboardBuilder
                    .create(chatID)
                    .setText(str)
                    .row()
                    .button("Delete book " + g,Long.toString(l))
                    .endRow()
                    .build();


            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void addBook(String responseData) {

        int number;
        try {
            String[] strings = responseData.split(",");

            int seanceID = Integer.parseInt(strings[0]);
            int row = Integer.parseInt(strings[1]);

            String[] stringSeats = strings[2].split("z");
            int[] seats = new int[stringSeats.length];

            for (int i = 0; i < seats.length; i++) {
                seats[i]=Integer.parseInt(stringSeats[i]);
            }
            Random rnd = new Random();
            number = rnd.nextInt(999999);


            for (int i = 0; i < seats.length; i++) {

                jdbcTemplate.update(
                        "INSERT INTO books (" +
                                "seanceID," +
                                "row," +
                                "seat," +
                                "bookCode," +
                                "status," +
                                "recordStatus," +
                                "chatID) " +
                                "VALUES (?,?,?,?,?,?,?)",
                        seanceID, row, seats[i], number, "booked","active",chatID);
            }
        }catch (Exception e){
            int c = 0x21A9;

            String nn = Character.toString((char)c);
            SendMessage message = InlineKeyboardBuilder
                    .create(chatID)
                    .setText("Cannot complete booking!")
                    .row()
                    .button("Again? "+nn, "startagain")
                    .endRow()
                    .build();
            this.row=0;
            try{
                execute(message);
            } catch(TelegramApiException e1){
                e1.printStackTrace();
                    return;
                }
            return;
        }
        this.row=0;
        int c = 0x21A9;

        String r = Character.toString((char)c);
    SendMessage sendMessage = InlineKeyboardBuilder.create(chatID)
            .setText("You've just booked the above mentioned place(s). " +
                    "\nYour booking code is " + "*" + number + "*" +
                    "\n*NOTE!!!* Book will be automatically cancelled if you dont buy tickets" +
                    "\n earlier than 30 minutes before beginning of movie seance!")
            .row()
            .button("Again? "+r, "startagain")
            .endRow()
            .build();
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        try{
            execute(sendMessage);
        }catch(TelegramApiException e){ e.printStackTrace();}

        return;
    }

    private void proposeBooking(Update update) {

        String text = update.getMessage().getText();


            //case for obtaining row
            if (row == 0) {
                try {
                    row = Integer.parseInt(text);
                    if (row <= 0 || row > hallSizeRow) {
                        row = 0;
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    SendMessage message = new SendMessage().setText("Not valid row number\n" +
                            "Please enter row in interval between 1 and " + hallSizeRow +
                            "\nEnter row again").setChatId(chatID);
                    try {
                        execute(message);
                        return;
                    } catch (TelegramApiException e1) {
                        e1.printStackTrace();
                        return;
                    }
                }

                SendMessage message = new SendMessage()
                        .setText("Your row is " + row + "\nPlease enter seats\n" +
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

                try {

                    if (text.contains(",") && !text.contains("-")) {
                        list = commaSplitter(text);
                    } else if (text.contains("-") && !text.contains(",")) {
                        list = defisSplitter(text);
                    } else if (text.contains(",") && text.contains("-")) {
                        list = commaSplitter(text, true);
                    } else {
                        int i = Integer.parseInt(text);
                        if (i <= 0 || i > hallSizeSeats) throw new NumberFormatException();
                        list.add(i);

                    }
                }catch (NumberFormatException e) {
                    SendMessage sendMessage = new SendMessage()
                            .setText("Invalid format. Please use correct format\nEnter row")
                            .setChatId(chatID);

                    try {
                        execute(sendMessage);
                        row=0;
                        return;
                    } catch (TelegramApiException e1) {
                        e1.printStackTrace();
                    }
                }
                list.sort(Comparator.naturalOrder());

                Set<Integer> set = new HashSet<>(list);
                list.clear();
                list.addAll(set);

                Integer[] seats = list.toArray(new Integer[list.size()]);

                for (int i = 0; i < seats.length; i++) {

                    List<Integer> rowsTemp = jdbcTemplate.query(
                            "select count(*) as s from books " +
                                    "where books.seanceID = " + seanceID +
                                    " and row = " + row +
                                    " and seat =" + seats[i] +
                                    " and recordStatus = \"active\"",
                            (resultSet, j) -> resultSet.getInt("s"));

                    int count = rowsTemp.get(0);
                    if (count != 0) {

                        SendMessage sendMessage = new SendMessage()
                                .setText("Row " + row + " seat "+seats[i]+" is already occupied. Try again" +
                                        "\nEnter row:")
                                .setChatId(chatID);
                        try {
                            execute(sendMessage);
                            row = 0;
                            seats = null;
                            return;
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                    }
                }

                StringBuilder str = new StringBuilder();
                StringBuilder seatsString = new StringBuilder();
                for (int i = 0; i < seats.length; i++) {
                    if (i == seats.length - 1) {
                        seatsString.append(seats[i]);
                        str.append(seats[i]);
                    } else {
                        str.append(seats[i] + ",");
                        seatsString.append(seats[i] + "z");
                    }
                }

                int c = 0x2705;
                String g = Character.toString((char)c);

                int b = 0x21A9;
                String s = Character.toString((char)b);

                System.out.println("g = "+g);
                SendMessage message = InlineKeyboardBuilder.create(chatID)
                        .setText("Your places: " +
                                "row=" + row +
                                "  seats= " + str.toString() +
                                "\nConfirm booking?")
                        .row()
                        .button("Confirm "+g, seanceID + "," + row + "," + seatsString)
                        .button("Again "+s, "startagain")
                        .endRow()
                        .build();

                boolean[][] placesToDraw = new boolean[hallSizeRow][hallSizeSeats];
                for (int i = 0; i < seats.length; i++) {
                    placesToDraw[row - 1][seats[i] - 1] = true;
                }

                 row = 0;
                ImageCreator.createImage(hallID, seanceID, placesToDraw, "greem");

                SendPhoto sendPhoto = new SendPhoto().setChatId(chatID);

                sendPhoto.setNewPhoto(new File("src/main/resources/" + seanceID + "_green" + ".png"));

                try {
                    sendPhoto(sendPhoto);
                    execute(message);
                    enteringMode = false;
                    return;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
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

        List<Place> placesTemp = jdbcTemplate.query(
                "select b.row, b.seat from seances s\n" +
                "join books b on s.seanceID = b.seanceID\n" +
                "where s.seanceID = " + seanceID +
                " and (status = \"booked\" or status = \"bought\")" +
                        " and recordStatus!= \"deleted\"",
                placeMapper);

        boolean[][] placesToDraw = new boolean[rows][seats];
        for (Place place : placesTemp) {
            placesToDraw[place.getRow()-1][place.getSeat()-1] = true;
        }
        ImageCreator.createImage(hallID, seanceID, placesToDraw, "red");

        SendPhoto sendPhoto = new SendPhoto().setChatId(chatID);

        sendPhoto.setNewPhoto(new File("src/main/resources/" + seanceID + ".png"));

        InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();

        int c = 0x2197;

        String g = Character.toString((char)c);

        row.add(new InlineKeyboardButton()
                .setText("Book "+g)
                .setCallbackData("pressforbookingplaces"));
        int cc = 0x21A9;
        String gg = Character.toString((char)cc);
        row.add(new InlineKeyboardButton()
                .setText("Again "+gg)
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
        SendMessage message = new SendMessage().setChatId(chatID);
        switch (day) {
            case ("today"): {
                seances = jdbcTemplate.query(
                        "select seanceID,hallID,movieID,startTime,duration,movie,price from\n" +
                                "(select * from seances\n" +
                                "where startTime between now() and (select addtime(CURDATE(), '23:59:59') as t)" +
                                "and expirationStatus != \"deleted\") as t1\n" +
                                "join\n" +
                                "(select movieID as mm, movie from movies) as t2\n" +
                                "on t1.movieID = t2.mm\n" +
                                "order by startTime asc ",
                        seanceMapper);
                message.setText("Today's seances:");
                break;
            }
            case ("tomorrow"):{
                seances = jdbcTemplate.query(
                        "select seanceID,hallID,movieID,startTime,duration,movie,price from\n" +
                                "(select * from seances\n" +
                                "where startTime between (select addtime(CURDATE()+1, '00:00:00') as t) " +
                                "and (select addtime(CURDATE()+1, '23:59:59') as t1)" +
                                "and expirationStatus != \"deleted\") as t2\n" +
                                "join\n" +
                                "(select movieID as mm, movie from movies) as t3\n" +
                                "on t2.movieID = t3.mm\n" +
                                "order by startTime asc ",
                        seanceMapper);
                message.setText("Tomorrow's seances:");
                break;
            }
            case ("dayaftertomorrow"):{
                seances = jdbcTemplate.query(
                        "select seanceID,hallID,movieID,startTime,duration,movie,price from\n" +
                                "(select * from seances\n" +
                                "where startTime between (select addtime(CURDATE()+2, '00:00:00') as t) " +
                                "and (select addtime(CURDATE()+2, '23:59:59') as t1) " +
                                "and expirationStatus != \"deleted\") as t2\n" +
                                "join\n" +
                                "(select movieID as mm, movie from movies) as t3\n" +
                                "on t2.movieID = t3.mm\n" +
                                "order by startTime asc ",
                        seanceMapper);
                message.setText("Day after tomorrow's seances:");
                break;
            }

        }
        //this checks that data from databese is not empty
        if (seances.isEmpty()) {
            SendMessage message1 = new SendMessage()
                    .setChatId(chatID)
                    .setText("No seances availabale" +
                            "\nPlease try again later");
            try {
                execute(message1);
            } catch (TelegramApiException e) {

            }
            return;
        }

        message.setReplyMarkup(new InlineKeyboardMarkup());

        for (Seance seance : seances) {
            String time = seance.getStartTime().toString();
            time = time.substring(0, time.length() - 3);
            addButton(
                    message,
                    time + " Hall#" + seance.getHallID() + " " + seance.getMovie()+" "+seance.getPrice()+" AMD",
                    Integer.toString(seance.getHallID()) + ","+seance.getSeanceID()+","+seance.getPrice()
            );
        }
        int c = 0x21A9;
        String g = Character.toString((char)c);
        addButton(message, "Again "+g, "startagain");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showSeancesforMovie(String movie) {

        List<Seance> seances =

                jdbcTemplate.query(
                        "select seanceID,hallID,seances.movieID,startTime,duration,movies.movie,price\n"+
                        "from seances\n"+
                        "join movies\n"+
                        "on seances.movieID = movies.movieID\n"+
                        "where movies.movie="+
                         "\""+movie+"\" " +
                          " and startTime > now() "+
                         "and expirationStatus != \"deleted\""+
                        "\norder by startTime asc",
                        seanceMapper);



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
        SendMessage message = new SendMessage().setChatId(chatID).setText("Seances for "+movie);

        message.setReplyMarkup(new InlineKeyboardMarkup());

        for (Seance seance : seances) {
            String time = seance.getStartTime().toString();
            time = time.substring(0, time.length() - 3);
            addButton(
                    message,
                    seance.getStartDate()+" "+time + " Hall#" + seance.getHallID(),
                    Integer.toString(seance.getHallID()) + seance.getSeanceID()
            );
        }
        int k = 0x21A9;
        String g = Character.toString((char)k);
        addButton(message,"To movie list "+g, "backtomovielist");
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
     *  @param data
     */
    private void showMovieDescription(String data) {

        MovieInfoMapper movieInfoMapper = new MovieInfoMapper();

        List<MovieInfo> movieInfos =
                jdbcTemplate.query(
                        "select m.movieID, m.actors,m.ageRestriction,m.director,\n"+
                        "m.genre,m.pathToPoster,m.productionYear,m.ranking,movies.movie\n"+
                        "from movies\n"+
                        "join movieinfo m on movies.movieID = m.movieID\n"+
                        "where movie = "+"\""+data+"\"",
                        movieInfoMapper);

        //    src\main\resources\posters\Godfather

        if (movieInfos.isEmpty()){
            int c = 0x21A9;

            String g = Character.toString((char)c);
            SendMessage message = InlineKeyboardBuilder.create(chatID)
            .setText("Cannot retrieve movie info...\nStart again")
                    .row()
                    .button("Again "+c, "startagain")
                    .endRow()
                    .build();
            try {
                execute(message);
                return;
            } catch (TelegramApiException e) {
                e.printStackTrace();
                return;
            }
        }
        MovieInfo movieInfo = movieInfos.get(0);

        int c = 0x23E9;
        int j = 0x21A9;

        String g = Character.toString((char)c);
        String h = Character.toString((char)j);

        SendMessage message = InlineKeyboardBuilder
                .create(chatID)
                .setText(
                   "Movie : " + movieInfo.getMovie() +"\n" +
                   "Production year : " + movieInfo.getProductionYear() +"\n" +
                   "Genre : " + movieInfo.getGenre() +"\n" +
                   "Director : " + movieInfo.getDirector() +"\n" +
                   "Actors : " + movieInfo.getActors() +"\n" +
                   "Ranking : " + movieInfo.getRanking() +"\n" +
                   "Age restriction : " + movieInfo.getAgeRestriction() +"\n"
                   )
                .row()
                .button("To seances "+g,movieInfo.getMovie())
                .button("To movie list "+h, "backtomovielist")
                .endRow()
                .build();

        SendMessage omdbMessage = null;

            try {
                omdbMessage = new SendMessage()
                        .setChatId(chatID)
                        .setText(OmdbConnector.sendGet(movieInfo.getMovie()));

                SendMessage youtubeMessage = new SendMessage()
                        .setChatId(chatID)
                        .setText("https://www.youtube.com/watch?v=" + YoutubeConnector.sendTrailer(movieInfo.getMovie()));

                execute(omdbMessage);
                execute(youtubeMessage);
                execute(message);
            return;
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return;
        }catch (IOException e1){
                e1.printStackTrace();
            }
    }

    /**
     * method for retrieving movies from table "Movies"
     * @param update
     */
    private void showMovies() {

        List<Movie> movies = jdbcTemplate.query("SELECT * FROM MOVIES", movieMapper);

        if (movies.isEmpty()) {
            SendMessage message = new SendMessage()
                    .setChatId(chatID)
                    .setText("No movie availabale" +
                            "\nPlease try later");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }
        SendMessage message = new SendMessage().setChatId(chatID).setText("Movies:");
        int b = 0x21A9;
        String s = Character.toString((char)b);

        message.setReplyMarkup(new InlineKeyboardMarkup());
        for (Movie movie : movies) {
            addButton(message, movie.getMovie(), movie.getMovie());
        }

        addButton(message,"Again "+s,"startagain");
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
        if (update.hasMessage() && update.getMessage().hasText()) {

            if (update.getMessage().getText().equals("/start")) {
                showSeancesAndMovies(update);
                return false;
            }
            if (!enteringMode) {

                try {
                    SendMessage youtubeMessage = new SendMessage()
                            .setChatId(chatID)
                            .setText("https://www.youtube.com/watch?v="
                                    + YoutubeConnector.sendTrailer(update.getMessage().getText()));

                    InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

                    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

                    List<InlineKeyboardButton> row = new ArrayList<>();

                    int c = 0x21A9;

                    String g = Character.toString((char)c);
                    row.add(new InlineKeyboardButton().setText("Again "+g).setCallbackData("startagain"));

                    keyboard.add(row);

                    keyboardMarkup.setKeyboard(keyboard);

                    youtubeMessage.setReplyMarkup(keyboardMarkup);

                    execute(youtubeMessage);
                } catch (Exception e) {

                    SendMessage errorMessage = new SendMessage()
                            .setChatId(chatID)
                            .setText("Unable to retrieve movie trailer...");
                    InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

                    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

                    List<InlineKeyboardButton> row = new ArrayList<>();
                    int c = 0x21A9;

                    String g = Character.toString((char)c);
                    row.add(new InlineKeyboardButton().setText("Again "+g).setCallbackData("startagain"));

                    keyboard.add(row);

                    keyboardMarkup.setKeyboard(keyboard);

                    errorMessage.setReplyMarkup(keyboardMarkup);

                    try {
                        execute(errorMessage);
                    } catch (TelegramApiException e1) {
                        e1.printStackTrace();
                    }


                }

                return false;
            }
        }
        return true;
    }
    /**
     * shows invalid command message and offers start again
     */
    private void invalidCommand() {
        int c = 0x21A9;

        String g = Character.toString((char)c);
        SendMessage message = InlineKeyboardBuilder.create(chatID)
                .setText("Invalid command. Please choose button or start again")
                .row()
                .button("Again "+g, "startagain")
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
        if (update.hasMessage() && update.getMessage().hasText()) {

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
        int c = 0x270C;

        String g = Character.toString((char)c);
        message = new SendMessage()
                .setText("Hello and welcome to Kinohall! "+g+"\n" +
                        "I'm your *Kinohall Bot*\n" +
                        "I'll help you book tickets\n" +
                        "For navigation through menu use buttons below\n" +
                        "Please note that buttons are active for half an hour\n"+
                        "*Additional feature for You:*\n"+
                        "Get any movie trailer from Youtube by simple typing movie name here and pressing Enter!")
                .setChatId(chatID);
        message.setParseMode(ParseMode.MARKDOWN);
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

        int a = 0x1F3A6;
        int b = 0x1F3AC;
        int c = 0x1F4D9;

        String kamera = Character.toString((char)a);
        String seans = Character.toString((char)b);
        String kniga = Character.toString((char)c);

        SendMessage message = InlineKeyboardBuilder.create(chatID)
                .setText("Choose command")
                .row()
                .button("Movies", "showmovies")
                .button("Seances", "showseances")
                .button("My books", "showmybooks")
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
