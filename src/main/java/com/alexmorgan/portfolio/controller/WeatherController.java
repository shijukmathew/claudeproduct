package com.alexmorgan.portfolio.controller;

import com.alexmorgan.portfolio.model.FavoriteCity;
import com.alexmorgan.portfolio.repository.FavoriteCityRepository;
import com.alexmorgan.portfolio.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WeatherController {

    private final WeatherService weatherService;
    private final FavoriteCityRepository favoriteCityRepository;

    @GetMapping("/")
    public String index(@RequestParam(required = false) String city,
                        @RequestParam(required = false) String countryCode,
                        Model model,
                        @AuthenticationPrincipal OAuth2User principal) {
        String cityToLoad = (city != null && !city.isBlank()) ? city : "London";
        loadWeather(cityToLoad, countryCode, model);
        model.addAttribute("selectedCountry", countryCode != null ? countryCode : "");
        loadUser(model, principal);
        return "index";
    }

    @PostMapping("/cities/save")
    public String saveCity(@RequestParam String cityName,
                           @RequestParam String country,
                           @RequestParam double latitude,
                           @RequestParam double longitude,
                           @AuthenticationPrincipal OAuth2User principal,
                           RedirectAttributes redirectAttributes) {
        String username = principal.getAttribute("login");
        if (favoriteCityRepository.existsByUsernameAndCityName(username, cityName)) {
            redirectAttributes.addFlashAttribute("info", cityName + " is already in your favourites.");
        } else {
            favoriteCityRepository.save(FavoriteCity.builder()
                    .cityName(cityName)
                    .country(country)
                    .latitude(latitude)
                    .longitude(longitude)
                    .username(username)
                    .build());
            log.info("User {} saved city {}", username, cityName);
            redirectAttributes.addFlashAttribute("success", cityName + " added to your favourites!");
        }
        return "redirect:/?city=" + URLEncoder.encode(cityName, StandardCharsets.UTF_8);
    }

    @PostMapping("/cities/delete/{id}")
    public String deleteCity(@PathVariable Long id,
                             @RequestParam(required = false) String currentCity,
                             @AuthenticationPrincipal OAuth2User principal,
                             RedirectAttributes redirectAttributes) {
        String username = principal.getAttribute("login");
        favoriteCityRepository.findById(id).ifPresent(c -> {
            if (c.getUsername().equals(username)) {
                favoriteCityRepository.deleteById(id);
                log.info("User {} removed city {}", username, c.getCityName());
                redirectAttributes.addFlashAttribute("success", c.getCityName() + " removed from favourites.");
            }
        });
        String redirect = (currentCity != null && !currentCity.isBlank())
                ? "redirect:/?city=" + URLEncoder.encode(currentCity, StandardCharsets.UTF_8)
                : "redirect:/";
        return redirect;
    }

    private void loadWeather(String city, String countryCode, Model model) {
        try {
            model.addAttribute("weather", weatherService.getWeather(city, countryCode));
        } catch (IllegalArgumentException e) {
            model.addAttribute("weatherError", e.getMessage());
        } catch (Exception e) {
            log.error("Weather API error for city={} country={}", city, countryCode, e);
            model.addAttribute("weatherError", "Weather data unavailable. Please try again shortly.");
        }
    }

    private void loadUser(Model model, OAuth2User principal) {
        if (principal != null) {
            String username = principal.getAttribute("login");
            model.addAttribute("username", username);
            model.addAttribute("avatarUrl", principal.getAttribute("avatar_url"));
            model.addAttribute("savedCities",
                    favoriteCityRepository.findByUsernameOrderBySavedAtDesc(username));
        }
    }
}
