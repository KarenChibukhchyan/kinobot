package com.jarviscorporation.kinobot.services;

import org.json.JSONArray;
import org.json.JSONObject;
//import sun.net.www.protocol.https.HttpsURLConnectionImpl;
//import sun.net.www.protocol.https.HttpsURLConnectionImpl;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import static com.google.common.net.HttpHeaders.USER_AGENT;

public class YoutubeConnector {

    public static String sendTrailer(String trailer) throws IOException {

        String url1 = "https://www.googleapis.com/youtube/v3/search?key=AIzaSyD5i6yGJ8i9FzlGcTQ18rzngxr0WvjeYEI&part=snippet&q="
                + trailer.replace(" ", "+") + "Trailer";

        URL obj = new URL(url1);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        //HttpsURLConnectionImpl con = (HttpsURLConnectionImpl) obj.openConnection();

        con.setRequestMethod("GET");

        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();

        //Terminal

        System.out.println("\nSending 'GET' request to URL : " + url1);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject jsonObject = new JSONObject(response.toString());

        JSONArray searchList = jsonObject.getJSONArray("items");

        String videoId = (searchList.getJSONObject(0)).getJSONObject("id").getString("videoId");
        System.out.println("https://www.youtube.com/watch?v=" + videoId);


        return videoId;

    }
}