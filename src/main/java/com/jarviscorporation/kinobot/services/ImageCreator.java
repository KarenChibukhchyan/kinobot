package com.jarviscorporation.kinobot.services;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageCreator {

    private final static int WIDTH = 600;
    private final static int HEIGHT = 400;

    // overloaded method for showing halls whole  image
    //should be called when Jarvis shows available places
    public static void createImage(int hallID, int seanceID, boolean[][] placesToDraw, String color) {

        Color busyColor;
        Color freeColor = Color.white;

        BufferedImage bufferedImage = drawHallID(hallID);
        if (color.equals("red")) {
            busyColor = Color.red;
            bufferedImage = drawFreePlaces(bufferedImage);
        } else {
            busyColor = Color.green;
            bufferedImage = drawYourPlaces(bufferedImage);
        }

        Graphics2D graphics = bufferedImage.createGraphics();

        int rows = placesToDraw.length;
        int seats = placesToDraw[0].length;

        float kvadratikLength = 360 / (float) (1.1 * seats - 0.1);
        float kvadratikHeight = 240 / (float) (1.1 * rows - 0.1);

        float horizontalPagging = kvadratikLength / 10;
        float verticalPagging = kvadratikHeight / 10;

        int fontSizeRows = (int) kvadratikLength;
        int fontSizeSeats = (int) kvadratikHeight;

        if (kvadratikHeight < 15) fontSizeSeats = 10;
        if (kvadratikHeight > 30) fontSizeSeats = 30;

        if (kvadratikHeight < 15) fontSizeRows = 10;
        if (kvadratikHeight > 30) fontSizeRows = 30;

        for (int i = 0; i < rows; i++) {

            graphics.setColor(Color.BLACK);
            graphics.setFont(new Font("Times New Roman", Font.BOLD, fontSizeSeats));
            graphics.drawString("row " + (i + 1), 20, (int) (320 - 30 / rows - (kvadratikHeight + verticalPagging) * i));


            for (int j = 0; j < seats; j++) {
                if (placesToDraw[i][j])
                    graphics.setColor(busyColor);
                else graphics.setColor(freeColor);

                graphics.fillRect((int) (120 + j * (kvadratikLength + horizontalPagging)),
                        (int) (320 - kvadratikHeight - (kvadratikHeight + verticalPagging) * i),
                        (int) kvadratikLength,
                        (int) kvadratikHeight);

                if (i == 1) {

                    graphics.setColor(Color.BLACK);
                    graphics.setFont(new Font("Times New Roman", Font.BOLD, fontSizeRows));
                    graphics.drawString(String.valueOf(j + 1), (int) (120 + 20 / seats + (kvadratikLength + horizontalPagging) * j), 70);
                }
            }
        }

        File file=null;
        if (busyColor.equals(Color.green))
        file = new File("src/main/resources/"+seanceID+"_green" + ".png");
        else
            file = new File("src/main/resources/"+seanceID + ".png");

        try {
            ImageIO.write(bufferedImage, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        graphics.dispose();
    }

    //draws legend in left upper corner with green square and "your places"
    private static BufferedImage drawYourPlaces(BufferedImage bufferedImage) {

        Graphics2D graphics = bufferedImage.createGraphics();

        graphics.setColor(Color.green);
        graphics.fillRect(20, 347, 20, 20);

        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font("Times New Roman", Font.BOLD, 20));
        graphics.drawString("your places", 50, 362);

        graphics.dispose();
        return bufferedImage;
    }

    //draws legend in left upper corner with two squares: "free places" and "booked places"
    private static BufferedImage drawFreePlaces(BufferedImage bufferedImage) {

        Graphics2D graphics = bufferedImage.createGraphics();

        graphics.setColor(Color.red);
        graphics.fillRect(453, 35, 20, 20);

        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font("Times New Roman", Font.BOLD, 20));
        graphics.drawString("free places", 475, 27);

        graphics.setColor(Color.white);
        graphics.fillRect(453, 10, 20, 20);

        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font("Times New Roman", Font.BOLD, 20));
        graphics.drawString("booked places", 475, 52);

        graphics.dispose();
        return bufferedImage;
    }

    //first method which draws background and "hall 1"
    private static BufferedImage drawHallID(int hallID) {

        BufferedImage bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setColor(new Color(255, 239, 213));
        graphics.fillRect(0, 0, WIDTH, HEIGHT);

        graphics.setFont(new Font("Gungsuh", Font.PLAIN, 40));
        graphics.setColor(Color.black);
        graphics.drawString("Hall " + hallID, WIDTH / 2 - WIDTH / 10 - 10, HEIGHT / 10);

        graphics.setColor(new Color(210, 105, 30));
        graphics.fillRect(150, 380, WIDTH / 2, HEIGHT / 10);

        graphics.setColor(Color.black);
        graphics.setFont(new Font("Gungsuh", Font.BOLD, 16));
        graphics.drawString("screen", 270, 395);

        graphics.dispose();
        return bufferedImage;
    }
}


