/**
 * Class-mapper for table MOVIES
 */

package com.jarviscorporation.kinobot.mappers;

import com.jarviscorporation.kinobot.domain.Movie;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MovieMapper implements RowMapper {
    @Override
    public Movie mapRow(ResultSet resultSet, int i) throws SQLException {

        Movie movie = new Movie();
        movie.setId(resultSet.getInt("movieID"));
        movie.setMovie(resultSet.getString("movie"));

        return movie;
    }
}
