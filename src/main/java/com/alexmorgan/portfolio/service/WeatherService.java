package com.alexmorgan.portfolio.service;

import com.alexmorgan.portfolio.dto.GeocodingResponse;
import com.alexmorgan.portfolio.dto.WeatherApiResponse;
import com.alexmorgan.portfolio.dto.WeatherData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class WeatherService {

    private final RestClient restClient = RestClient.create();

    public WeatherData getWeather(String cityName) {
        GeocodingResponse.Result location = geocode(cityName);
        WeatherApiResponse.CurrentWeather current = fetchCurrent(location.getLatitude(), location.getLongitude());

        int code = current.getWeatherCode();
        return WeatherData.builder()
                .cityName(location.getName())
                .country(location.getCountry())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .temperature(Math.round(current.getTemperature() * 10.0) / 10.0)
                .weatherCode(code)
                .windSpeed(current.getWindSpeed())
                .humidity(current.getHumidity())
                .condition(condition(code))
                .icon(icon(code))
                .backgroundClass(backgroundClass(code))
                .textClass(textClass(code))
                .build();
    }

    private GeocodingResponse.Result geocode(String city) {
        log.info("Geocoding city: {}", city);
        GeocodingResponse response = restClient.get()
                .uri("https://geocoding-api.open-meteo.com/v1/search?name={city}&count=1&language=en&format=json", city)
                .retrieve()
                .body(GeocodingResponse.class);

        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            throw new IllegalArgumentException("City not found: " + city);
        }
        return response.getResults().get(0);
    }

    private WeatherApiResponse.CurrentWeather fetchCurrent(double lat, double lon) {
        log.info("Fetching weather for lat={} lon={}", lat, lon);
        WeatherApiResponse response = restClient.get()
                .uri("https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}" +
                     "&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code" +
                     "&timezone=auto", lat, lon)
                .retrieve()
                .body(WeatherApiResponse.class);

        if (response == null || response.getCurrent() == null) {
            throw new RuntimeException("Weather data unavailable");
        }
        return response.getCurrent();
    }

    // WMO weather code → human-readable condition
    private String condition(int code) {
        if (code == 0)           return "Clear Sky";
        if (code <= 2)           return "Partly Cloudy";
        if (code == 3)           return "Overcast";
        if (code <= 48)          return "Foggy";
        if (code <= 57)          return "Drizzle";
        if (code <= 67)          return "Rain";
        if (code <= 77)          return "Snow";
        if (code <= 82)          return "Rain Showers";
        if (code <= 86)          return "Snow Showers";
        return "Thunderstorm";
    }

    // Bootstrap icon names
    private String icon(int code) {
        if (code == 0)           return "bi-sun-fill";
        if (code <= 2)           return "bi-cloud-sun-fill";
        if (code == 3)           return "bi-clouds-fill";
        if (code <= 48)          return "bi-cloud-fog2-fill";
        if (code <= 57)          return "bi-cloud-drizzle-fill";
        if (code <= 67)          return "bi-cloud-rain-fill";
        if (code <= 77)          return "bi-cloud-snow-fill";
        if (code <= 82)          return "bi-cloud-rain-heavy-fill";
        if (code <= 86)          return "bi-cloud-snow-fill";
        return "bi-cloud-lightning-rain-fill";
    }

    // CSS gradient class applied to the page background
    private String backgroundClass(int code) {
        if (code == 0)           return "bg-clear";
        if (code <= 3)           return "bg-cloudy";
        if (code <= 48)          return "bg-fog";
        if (code <= 67)          return "bg-rain";
        if (code <= 77)          return "bg-snow";
        if (code <= 82)          return "bg-rain";
        if (code <= 86)          return "bg-snow";
        return "bg-storm";
    }

    // Light text looks good on dark backgrounds; dark text on light (snow/fog)
    private String textClass(int code) {
        if (code >= 71 && code <= 77) return "text-dark";
        if (code >= 45 && code <= 48) return "text-dark";
        return "text-white";
    }
}
