package com.example.status.statustracker.controller;

import com.example.status.statustracker.service.TrainMapService;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

@Controller
public class TrainMapController {

    private final TrainMapService service;

    public TrainMapController(TrainMapService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String index() {
        // Redirect to the static HTML
        return "redirect:/index.html";
    }

    @GetMapping("/api/train/{trainNumber}")
    @ResponseBody
    public String getTrainSnapshot(@PathVariable String trainNumber,
            @RequestParam String journeyDate) {
        return service.getTrainSnapshotJson(trainNumber, journeyDate);
    }
}
