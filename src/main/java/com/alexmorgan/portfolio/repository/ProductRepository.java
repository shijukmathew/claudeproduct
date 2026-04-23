package com.alexmorgan.portfolio.repository;

import com.alexmorgan.portfolio.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
