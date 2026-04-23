package com.alexmorgan.portfolio.repository;

import com.alexmorgan.portfolio.model.FavoriteCity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteCityRepository extends JpaRepository<FavoriteCity, Long> {
    List<FavoriteCity> findByUsernameOrderBySavedAtDesc(String username);
    boolean existsByUsernameAndCityName(String username, String cityName);
}
