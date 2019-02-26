/**
 * CLASS IS BUILDER FOR MESSAGE WITH INLINE BUTTONS
 */
package com.jarviscorporation.kinobot.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.net.HttpHeaders.USER_AGENT;

public class OmdbConnector {

    public static  String sendGet(String matrix) throws IOException {

        String url = "http://www.omdbapi.com/?apikey=3c8b2f53&s=" + matrix.replace(" ", "+");

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject jsonObject = new JSONObject(response.toString());

        JSONArray searchList = jsonObject.getJSONArray("Search");

        List<JSONObject> list = new ArrayList<>();

        for (int i = 0; i < searchList.length(); i++) {
            list.add(searchList.getJSONObject(i));
        }

        if (!list.isEmpty()) {
            Map<String, Object> stringObjectMap = list.get(0).toMap();
            return MessageFormat.format("{0}", stringObjectMap.get("Poster"));
        }

        return "Unknown command";
    }

}
