package com.jarviscorporation.kinobot.domain;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Seance {


    private int seanceID;
    private int hallID;
    private int movieID;
    private Date startDate;
    private Time startTime;
    private int duration;
    private String movie;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }
    public Time getStartTime() {
        return startTime;
    }

    @Override
    public String toString() {
        return "Seance{\n" +
                "seanceID=" + seanceID +"\n"+
                ", hallID=" + hallID +"\n"+
                ", movieID=" + movieID +"\n"+
                ", startDate=" + startDate +"\n"+
                ", startTime=" + startTime +"\n"+
                ", duration=" + duration +"\n"+
                ", movie=" + movie +"\n"+
                '}';
    }

    public int getSeanceID() {
        return seanceID;
    }

    public void setSeanceID(int seanceID) {
        this.seanceID = seanceID;
    }

    public int getHallID() {
        return hallID;
    }

    public void setHallID(int hallID) {
        this.hallID = hallID;
    }

    public int getMovieID() {
        return movieID;
    }

    public void setMovieID(int movieID) {
        this.movieID = movieID;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setMovie(String movie) {
        this.movie = movie;
    }

    public String getMovie(){
        return movie;
    }
}