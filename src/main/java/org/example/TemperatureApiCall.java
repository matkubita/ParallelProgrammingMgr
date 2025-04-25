package org.example;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TemperatureApiCall implements Runnable{
    private String cityName;
    private ConcurrentLinkedQueue<Double> resultsQueue;

    public TemperatureApiCall(String cityName, ConcurrentLinkedQueue<Double> resultsQueue) {
        this.cityName = cityName;
        this.resultsQueue = resultsQueue;
    }

    @Override
    public void run() {
        try {
            Double temp = get_temperature(this.cityName);
            System.out.println(temp + " in " + this.cityName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Double get_temperature(String cityName) throws IOException {

        String apiKey = "zupa";
        String urlString = "http://api.weatherapi.com/v1/current.json?key=" + apiKey + "&q=" + cityName + "&aqi=no";

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        conn.disconnect();

        String output = content.toString();
        JSONObject ob2 = new JSONObject(output);

        Double temperature = ob2.getJSONObject("current").getDouble("temp_c");
        resultsQueue.add(temperature);
        return temperature;

    }
}
