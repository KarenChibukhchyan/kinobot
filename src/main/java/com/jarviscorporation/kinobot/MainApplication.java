/**
 * MAIN CLASS FOR RUNNING APPLICATION USING SPRING JDBC + SPRING BOOT
 */
package com.jarviscorporation.kinobot;

import com.jarviscorporation.kinobot.mappers.MovieMapper;
import com.jarviscorporation.kinobot.mappers.SeanceMapper;
import com.jarviscorporation.kinobot.services.Jarvis;
import com.jarviscorporation.kinobot.services.JarvisStarter;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
   public class MainApplication {

    public static void main(String[] args) {

        SpringApplication.run(MainApplication.class, args);
    }

    @Bean
    JarvisStarter jarvisStarter() {
        return new JarvisStarter();
    }
    @Bean
    SeanceMapper seanceMapper() {
        return new SeanceMapper();
    }

    @Bean
    public Jarvis jarvis() {
        return new Jarvis();
    }

    @Bean
    public MovieMapper movieMapper() {
        return new MovieMapper();
    }

}




