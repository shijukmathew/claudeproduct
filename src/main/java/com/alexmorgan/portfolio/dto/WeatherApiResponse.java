package com.alexmorgan.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherApiResponse {

    private CurrentWeather current;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrentWeather {

        @JsonProperty("temperature_2m")
        private double temperature;

        @JsonProperty("relative_humidity_2m")
        private int humidity;

        @JsonProperty("wind_speed_10m")
        private double windSpeed;

        @JsonProperty("weather_code")
        private int weatherCode;
    }
}
