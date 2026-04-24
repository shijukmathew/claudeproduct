package com.alexmorgan.portfolio.weather;

import java.util.List;

public record WeatherView(
        String cityName,
        String region,
        String country,
        String countryCode,
        double latitude,
        double longitude,
        double temperature,
        int humidity,
        double windSpeed,
        int weatherCode,
        String condition,
        String backgroundClass,
        String symbolClass,
        List<HourlyForecast> hourlyForecasts,
        List<DailyForecast> dailyForecasts
) {

    public static WeatherView fallback(String city) {
        return new WeatherView(
                city, "Preview Mode", "Local", "", 44.6, -63.6,
                18.4, 62, 14.2, 2, "Partly Cloudy", "bg-cloudy", "icon-partly",
                List.of(
                        new HourlyForecast("Now",  "icon-partly", 18.4, 15, "Partly Cloudy"),
                        new HourlyForecast("1 PM", "icon-sun",    19.1,  8, "Clear Sky"),
                        new HourlyForecast("2 PM", "icon-sun",    20.2,  4, "Clear Sky"),
                        new HourlyForecast("3 PM", "icon-partly", 19.8, 12, "Partly Cloudy"),
                        new HourlyForecast("4 PM", "icon-cloud",  18.6, 20, "Overcast"),
                        new HourlyForecast("5 PM", "icon-rain",   16.9, 36, "Rain")
                ),
                List.of(
                        new DailyForecast("Today", "Apr 23", "icon-partly", 19.0, 11.0, 20, "Partly Cloudy"),
                        new DailyForecast("Fri",   "Apr 24", "icon-sun",    21.0, 12.0,  4, "Clear Sky"),
                        new DailyForecast("Sat",   "Apr 25", "icon-rain",   17.0,  9.0, 48, "Rain"),
                        new DailyForecast("Sun",   "Apr 26", "icon-cloud",  16.0,  8.0, 24, "Overcast"),
                        new DailyForecast("Mon",   "Apr 27", "icon-sun",    18.0,  7.0,  6, "Clear Sky"),
                        new DailyForecast("Tue",   "Apr 28", "icon-partly", 20.0, 10.0, 14, "Partly Cloudy"),
                        new DailyForecast("Wed",   "Apr 29", "icon-rain",   15.0,  8.0, 42, "Rain"),
                        new DailyForecast("Thu",   "Apr 30", "icon-sun",    17.0,  9.0,  5, "Clear Sky"),
                        new DailyForecast("Fri",   "May  1", "icon-cloud",  16.0,  8.0, 18, "Overcast"),
                        new DailyForecast("Sat",   "May  2", "icon-partly", 18.0,  9.0, 16, "Partly Cloudy")
                )
        );
    }

    public record HourlyForecast(
            String label,
            String symbolClass,
            double temperature,
            int precipitationProbability,
            String condition
    ) {}

    public record DailyForecast(
            String dayLabel,
            String dateLabel,
            String symbolClass,
            double highTemperature,
            double lowTemperature,
            int precipitationProbability,
            String condition
    ) {}
}
