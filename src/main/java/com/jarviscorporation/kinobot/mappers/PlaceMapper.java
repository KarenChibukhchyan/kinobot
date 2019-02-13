package com.jarviscorporation.kinobot.mappers;

import com.jarviscorporation.kinobot.domain.Place;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PlaceMapper implements RowMapper {
    @Override
    public Place mapRow(ResultSet resultSet, int i) throws SQLException {

        Place place = new Place();
        place.setRow(resultSet.getInt("row"));
        place.setSeat(resultSet.getInt("seat"));

        return place;
    }
}
