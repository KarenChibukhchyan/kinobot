package com.jarviscorporation.kinobot.mappers;

import com.jarviscorporation.kinobot.domain.Movie;
import com.jarviscorporation.kinobot.domain.Seance;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SeanceMapper implements RowMapper {
    @Override
    public Seance mapRow(ResultSet resultSet, int i) throws SQLException {

        Seance seance = new Seance();

        seance.setSeanceID(resultSet.getInt("seanceID"));
        seance.setHallID(resultSet.getInt("hallID"));
        seance.setMovieID(resultSet.getInt("movieID"));
        seance.setStartDate(resultSet.getDate("startTime"));
        seance.setStartTime(resultSet.getTime("startTime"));
        seance.setDuration(resultSet.getInt("duration"));
        seance.setMovie(resultSet.getString("movie"));
        seance.setPrice(resultSet.getInt("price"));


        return seance;
    }
}
