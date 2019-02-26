/**
 * Movie entity
 */
package com.jarviscorporation.kinobot.domain;

public class Movie {

    private String movie;
    private int id;


    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", movie='" + movie + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMovie() {
        return movie;
    }

    public void setMovie(String movie) {
        this.movie = movie;
    }
}
