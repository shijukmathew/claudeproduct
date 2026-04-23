package com.alexmorgan.portfolio.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeatherData {
    private String cityName;
    private String region;     // admin1 — state/province for disambiguation
    private String country;
    private double latitude;
    private double longitude;
    private double temperature;
    private int weatherCode;
    private double windSpeed;
    private int humidity;
    private String condition;
    private String icon;
    private String backgroundClass;
    private String textClass;
}
