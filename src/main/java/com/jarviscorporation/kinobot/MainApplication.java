/**
 * MAIN CLASS FOR RUNNING APPLICATION USING SPRING JDBC + SPRING BOOT
 */

package com.jarviscorporation.kinobot;
import com.jarviscorporation.kinobot.domain.Movie;
import com.jarviscorporation.kinobot.mappers.MovieMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;


   @SpringBootApplication
   public class MainApplication implements CommandLineRunner {


       @Autowired
       JdbcTemplate jdbcTemplate;
       @Autowired
       MovieMapper movieMapper;

     public static void main(String[] args) {

        SpringApplication.run(MainApplication.class, args);

     }

    @Bean
    public MovieMapper movieMapper(){
        return new MovieMapper();
    }

    /**
     * In this method we run our classes
     * @param strings
     * @throws Exception
     */
    @Override
    public void run(String... strings) throws Exception {


    }
}

