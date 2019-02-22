package com.jarviscorporation.kinobot.mappers;

import com.jarviscorporation.kinobot.domain.Book;
import com.jarviscorporation.kinobot.domain.MovieInfo;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BookMapper implements RowMapper {
    @Override
    public Book mapRow(ResultSet resultSet, int i) throws SQLException {
        Book book = new Book();

        book.setBookCode(resultSet.getLong("bookCode"));
        book.setBookID(resultSet.getInt("bookID"));
        book.setChatID(resultSet.getLong("chatID"));
        book.setHallID(resultSet.getInt("hallID"));
        book.setMovie(resultSet.getString("movie"));
        book.setPrice(resultSet.getInt("price"));
        book.setRecordStatus(resultSet.getString("recordStatus"));
        book.setRow(resultSet.getInt("row"));
        book.setSeat(resultSet.getInt("seat"));
        book.setSeanceID(resultSet.getInt("seanceID"));

        book.setStartTime(resultSet.getDate("startTime").toString()
        +" "+resultSet.getTime("startTime").toString().substring(0,5));

        return book;
    }
}
