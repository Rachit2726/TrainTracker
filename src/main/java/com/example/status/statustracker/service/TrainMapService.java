package com.example.status.statustracker.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TrainMapService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${rapidapi.key}")
    private String apiKey;

    private final String API_HOST = "irctc-api2.p.rapidapi.com";

    public String getTrainSnapshotJson(String trainNumber, String journeyDate) {
        try {
            String routeUrl = "https://" + API_HOST + "/trainSchedule?trainNumber=" + trainNumber;
            String liveUrl = "https://" + API_HOST + "/liveTrain?trainNumber=" + trainNumber;

            // Headers
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-RapidAPI-Key", apiKey);
            headers.set("X-RapidAPI-Host", API_HOST);
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

            // Fetch route
            org.springframework.http.ResponseEntity<String> routeRes = restTemplate.exchange(routeUrl,
                    org.springframework.http.HttpMethod.GET, entity, String.class);

            // Fetch live status
            org.springframework.http.ResponseEntity<String> liveRes = restTemplate.exchange(liveUrl,
                    org.springframework.http.HttpMethod.GET, entity, String.class);

            JSONObject routeJson = new JSONObject(routeRes.getBody());
            JSONObject liveJson = new JSONObject(liveRes.getBody());

            JSONObject snapshot = new JSONObject();
            snapshot.put("route", routeJson.getJSONArray("data"));
            snapshot.put("currentStation", liveJson.getJSONObject("data").getString("current_station_name"));
            snapshot.put("nextStation", liveJson.getJSONObject("data")
                    .getJSONObject("next_stoppage_info").getString("next_stoppage"));
            snapshot.put("etaNextStation", liveJson.getJSONObject("data")
                    .getJSONObject("next_stoppage_info").getString("next_stoppage_time_diff"));

            return snapshot.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"Unable to fetch train data\"}";
        }
    }
}
