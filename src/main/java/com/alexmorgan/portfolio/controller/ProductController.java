package com.alexmorgan.portfolio.controller;

import com.alexmorgan.portfolio.model.Product;
import com.alexmorgan.portfolio.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("newProduct", new Product());
        return "index";
    }

    @PostMapping("/products/add")
    public String addProduct(@Valid @ModelAttribute("newProduct") Product product,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("products", productRepository.findAll());
            model.addAttribute("showForm", true);
            return "index";
        }
        try {
            productRepository.save(product);
            log.info("Saved product: {} ({})", product.getProductName(), product.getProductNumber());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Product \"" + product.getProductName() + "\" added successfully!");
        } catch (Exception e) {
            log.error("Error saving product", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Product number already exists. Please use a unique product number.");
        }
        return "redirect:/";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productRepository.findById(id).ifPresent(p -> {
            productRepository.deleteById(id);
            log.info("Deleted product id={} name={}", id, p.getProductName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Product \"" + p.getProductName() + "\" deleted.");
        });
        return "redirect:/";
    }
}
