

package com.jarviscorporation.kinobot.mappers;

import com.jarviscorporation.kinobot.domain.MovieInfo;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MovieInfoMapper implements RowMapper {
    @Override
    public MovieInfo mapRow(ResultSet resultSet, int i) throws SQLException {

        MovieInfo movieInfo = new MovieInfo();
        movieInfo.setMovie(resultSet.getString("movie"));
        movieInfo.setActors(resultSet.getString("actors"));
        movieInfo.setMovieID(resultSet.getInt("movieID"));
        movieInfo.setProductionYear(resultSet.getInt("productionYear"));
        movieInfo.setGenre(resultSet.getString("genre"));
        movieInfo.setDirector(resultSet.getString("director"));
        movieInfo.setAgeRestriction(resultSet.getInt("ageRestriction"));
        movieInfo.setRanking(resultSet.getDouble("ranking"));
        movieInfo.setPathToPoster(resultSet.getString("pathToPoster"));
        return movieInfo;
    }
}
