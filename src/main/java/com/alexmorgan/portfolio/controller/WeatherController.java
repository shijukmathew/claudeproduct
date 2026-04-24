package com.alexmorgan.portfolio.controller;

import com.alexmorgan.portfolio.weather.WeatherLookupException;
import com.alexmorgan.portfolio.weather.WeatherService;
import com.alexmorgan.portfolio.weather.WeatherView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) String city,
                        @RequestParam(required = false) String countryCode,
                        @RequestParam(required = false) Integer dayIndex,
                        Model model) {

        String selectedCity    = (city == null || city.isBlank()) ? "London" : city.trim();
        String selectedCountry = (countryCode == null) ? "" : countryCode.trim();

        WeatherView weather;
        try {
            weather = weatherService.currentWeather(selectedCity, selectedCountry);
        } catch (WeatherLookupException ex) {
            model.addAttribute("weatherError", ex.getMessage());
            weather = WeatherView.fallback(selectedCity);
        }

        int selectedDayIndex = 0;
        if (!weather.dailyForecasts().isEmpty() && dayIndex != null) {
            selectedDayIndex = Math.max(0, Math.min(dayIndex, weather.dailyForecasts().size() - 1));
        }

        model.addAttribute("weather", weather);
        model.addAttribute("selectedCountry", selectedCountry);
        model.addAttribute("selectedDayIndex", selectedDayIndex);
        if (!weather.dailyForecasts().isEmpty()) {
            model.addAttribute("selectedDailyForecast", weather.dailyForecasts().get(selectedDayIndex));
        }
        return "index";
    }
}
