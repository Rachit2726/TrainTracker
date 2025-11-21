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
        return "redirect:/index.html";
    }

    @GetMapping("/api/train/{trainNo}")
    @ResponseBody
    public String getTrainData(
            @PathVariable String trainNo,
            @RequestParam String date) {
        return service.getCombinedTrainData(trainNo, date);
    }
}
