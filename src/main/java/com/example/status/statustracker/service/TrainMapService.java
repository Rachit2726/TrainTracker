package com.example.status.statustracker.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TrainMapService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${rapidapi.key}")
    private String apiKey;

    private final String HOST = "irctc-api2.p.rapidapi.com";

    public String getCombinedTrainData(String trainNo, String date) {

        try {
            JSONObject output = new JSONObject();

            // 1️⃣ Convert date -> startDay
            LocalDate selected = LocalDate.parse(date);
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));

            int startDay = (int) ChronoUnit.DAYS.between(selected, today);
            if (startDay < 0)
                startDay = 0;
            if (startDay > 2)
                startDay = 2;

            output.put("startDay", startDay);
            output.put("selectedDate", date);

            // 2️⃣ FULL ROUTE (trainSchedule)
            String scheduleUrl = "https://" + HOST + "/trainSchedule?trainNumber=" + trainNo;
            JSONObject scheduleJSON = callApi(scheduleUrl);

            JSONArray fullRoute = scheduleJSON.getJSONArray("data");
            output.put("route", fullRoute);

            // 3️⃣ LIVE STATUS (liveTrain)
            String liveUrl = "https://" + HOST + "/liveTrain?trainNumber=" + trainNo + "&startDay=" + startDay;

            JSONObject liveData = callApi(liveUrl).getJSONObject("data");

            output.put("trainName", liveData.optString("train_name"));
            output.put("currentStation", liveData.optString("current_station_name"));
            output.put("delay", liveData.optInt("delay"));
            output.put("eta", liveData.optString("eta"));
            output.put("etd", liveData.optString("etd"));
            output.put("status", liveData.optString("status"));

            // NEXT STOPPAGE
            JSONObject nextObj = liveData.optJSONObject("next_stoppage_info");
            output.put("nextStoppage", nextObj);

            // LOCATION FEED
            output.put("locationInfo", liveData.optJSONArray("current_location_info"));

            // ⭐⭐⭐ ONLY NEW CODE BELOW (FACILITIES) ⭐⭐⭐

            String currName = liveData.optString("current_station_name", "").trim();
            String nextName = nextObj != null
                    ? nextObj.optString("next_stoppage", "").trim()
                    : null;

            JSONObject currStationObj = null;
            JSONObject nextStationObj = null;

            // match names with schedule data
            for (int i = 0; i < fullRoute.length(); i++) {
                JSONObject st = fullRoute.getJSONObject(i);
                String name = st.optString("station_name", "").trim();

                if (name.equalsIgnoreCase(currName)) {
                    currStationObj = st;
                }

                if (nextName != null && name.equalsIgnoreCase(nextName)) {
                    nextStationObj = st;
                }
            }

            // Current station facilities
            output.put("currentHasFood", currStationObj != null && currStationObj.optBoolean("food_available"));
            output.put("currentHasHotel", currStationObj != null && currStationObj.optBoolean("hotel_available"));
            output.put("currentHasHospital", currStationObj != null && currStationObj.optBoolean("hospital_available"));

            // Next station facilities
            output.put("nextHasFood", nextStationObj != null && nextStationObj.optBoolean("food_available"));
            output.put("nextHasHotel", nextStationObj != null && nextStationObj.optBoolean("hotel_available"));
            output.put("nextHasHospital", nextStationObj != null && nextStationObj.optBoolean("hospital_available"));

            // ⭐⭐⭐ END OF NEW CODE ⭐⭐⭐

            return output.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"Failed\"}";
        }
    }

    private JSONObject callApi(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-key", apiKey);
        headers.set("x-rapidapi-host", HOST);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        return new JSONObject(response.getBody());
    }
}
