package com.alexmorgan.portfolio.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class WeatherService {

    private final RestClient restClient;

    public WeatherService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public WeatherView currentWeather(String city, String countryCode) {
        GeocodingResponse.Result location = geocode(city, countryCode);
        ForecastResponse forecast = forecast(location.latitude(), location.longitude());
        ForecastResponse.Current current = forecast.current();

        int code = current.weatherCode();
        return new WeatherView(
                location.name(),
                safe(location.admin1()),
                safe(location.country()),
                safe(location.countryCode()),
                location.latitude(),
                location.longitude(),
                round(current.temperature2m()),
                current.relativeHumidity2m(),
                round(current.windSpeed10m()),
                code,
                condition(code),
                backgroundClass(code),
                symbolClass(code),
                buildHourly(forecast.hourly()),
                buildDaily(forecast.daily())
        );
    }

    private GeocodingResponse.Result geocode(String city, String countryCode) {
        String encoded = URLEncoder.encode(city, StandardCharsets.UTF_8);
        URI uri = URI.create("https://geocoding-api.open-meteo.com/v1/search?name="
                + encoded + "&count=10&language=en&format=json");
        GeocodingResponse response;
        try {
            response = restClient.get().uri(uri).retrieve().body(GeocodingResponse.class);
        } catch (Exception ex) {
            throw new WeatherLookupException("Could not reach the city search service.", ex);
        }
        if (response == null || response.results() == null || response.results().isEmpty()) {
            throw new WeatherLookupException("City \"" + city + "\" was not found.");
        }
        if (countryCode != null && !countryCode.isBlank()) {
            return response.results().stream()
                    .filter(r -> countryCode.equalsIgnoreCase(r.countryCode()))
                    .findFirst()
                    .orElseThrow(() -> new WeatherLookupException(
                            "\"" + city + "\" was not found in the selected country."));
        }
        return response.results().get(0);
    }

    private ForecastResponse forecast(double lat, double lon) {
        URI uri = UriComponentsBuilder.fromUriString("https://api.open-meteo.com/v1/forecast")
                .queryParam("latitude", lat)
                .queryParam("longitude", lon)
                .queryParam("current", "temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code")
                .queryParam("hourly", "temperature_2m,weather_code,precipitation_probability")
                .queryParam("daily", "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max")
                .queryParam("forecast_days", 10)
                .queryParam("timezone", "auto")
                .build().toUri();
        ForecastResponse response;
        try {
            response = restClient.get().uri(uri).retrieve().body(ForecastResponse.class);
        } catch (Exception ex) {
            throw new WeatherLookupException("Could not reach the forecast service.", ex);
        }
        if (response == null || response.current() == null) {
            throw new WeatherLookupException("Weather data unavailable for this location.");
        }
        return response;
    }

    private List<WeatherView.HourlyForecast> buildHourly(ForecastResponse.Hourly hourly) {
        if (hourly == null || hourly.time() == null) return List.of();
        List<WeatherView.HourlyForecast> items = new ArrayList<>();
        int count = Math.min(12, hourly.time().size());
        for (int i = 0; i < count; i++) {
            LocalDateTime ts = LocalDateTime.parse(hourly.time().get(i));
            int code = safeInt(hourly.weatherCode(), i);
            items.add(new WeatherView.HourlyForecast(
                    i == 0 ? "Now" : ts.format(DateTimeFormatter.ofPattern("h a", Locale.ENGLISH)),
                    symbolClass(code),
                    round(safeDouble(hourly.temperature2m(), i)),
                    safeInt(hourly.precipitationProbability(), i),
                    condition(code)
            ));
        }
        return items;
    }

    private List<WeatherView.DailyForecast> buildDaily(ForecastResponse.Daily daily) {
        if (daily == null || daily.time() == null) return List.of();
        List<WeatherView.DailyForecast> items = new ArrayList<>();
        int count = Math.min(10, daily.time().size());
        for (int i = 0; i < count; i++) {
            LocalDate date = LocalDate.parse(daily.time().get(i));
            int code = safeInt(daily.weatherCode(), i);
            items.add(new WeatherView.DailyForecast(
                    i == 0 ? "Today" : date.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)),
                    date.format(DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)),
                    symbolClass(code),
                    round(safeDouble(daily.temperatureMax(), i)),
                    round(safeDouble(daily.temperatureMin(), i)),
                    safeInt(daily.precipitationProbabilityMax(), i),
                    condition(code)
            ));
        }
        return items;
    }

    private String condition(int c) {
        if (c == 0) return "Clear Sky";
        if (c <= 2)  return "Partly Cloudy";
        if (c == 3)  return "Overcast";
        if (c <= 48) return "Foggy";
        if (c <= 57) return "Drizzle";
        if (c <= 67) return "Rain";
        if (c <= 77) return "Snow";
        if (c <= 82) return "Rain Showers";
        if (c <= 86) return "Snow Showers";
        return "Thunderstorm";
    }

    private String backgroundClass(int c) {
        if (c == 0)  return "bg-clear";
        if (c <= 3)  return "bg-cloudy";
        if (c <= 48) return "bg-fog";
        if (c <= 67) return "bg-rain";
        if (c <= 77) return "bg-snow";
        if (c <= 82) return "bg-rain";
        if (c <= 86) return "bg-snow";
        return "bg-storm";
    }

    private String symbolClass(int c) {
        if (c == 0)  return "icon-sun";
        if (c <= 2)  return "icon-partly";
        if (c == 3)  return "icon-cloud";
        if (c <= 48) return "icon-fog";
        if (c <= 67) return "icon-rain";
        if (c <= 77) return "icon-snow";
        if (c <= 82) return "icon-rain";
        if (c <= 86) return "icon-snow";
        return "icon-storm";
    }

    private double round(double v) { return Math.round(v * 10.0) / 10.0; }
    private String safe(String v)  { return v == null ? "" : v; }

    private double safeDouble(List<Double> list, int i) {
        return (list == null || i >= list.size() || list.get(i) == null) ? 0 : list.get(i);
    }
    private int safeInt(List<Integer> list, int i) {
        return (list == null || i >= list.size() || list.get(i) == null) ? 0 : list.get(i);
    }

    record GeocodingResponse(List<Result> results) {
        record Result(
                String name,
                String admin1,
                String country,
                @JsonProperty("country_code") String countryCode,
                double latitude,
                double longitude) {}
    }

    record ForecastResponse(Current current, Hourly hourly, Daily daily) {
        record Current(
                @JsonProperty("temperature_2m")        double temperature2m,
                @JsonProperty("relative_humidity_2m")  int relativeHumidity2m,
                @JsonProperty("wind_speed_10m")         double windSpeed10m,
                @JsonProperty("weather_code")           int weatherCode) {}

        record Hourly(
                List<String> time,
                @JsonProperty("temperature_2m")            List<Double>  temperature2m,
                @JsonProperty("weather_code")               List<Integer> weatherCode,
                @JsonProperty("precipitation_probability")  List<Integer> precipitationProbability) {}

        record Daily(
                List<String> time,
                @JsonProperty("weather_code")                    List<Integer> weatherCode,
                @JsonProperty("temperature_2m_max")              List<Double>  temperatureMax,
                @JsonProperty("temperature_2m_min")              List<Double>  temperatureMin,
                @JsonProperty("precipitation_probability_max")   List<Integer> precipitationProbabilityMax) {}
    }
}
