package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Product;
import com.example.demo.model.Review;
import com.example.demo.repository.ProductRepository;
import com.example.demo.strategy.DiscountContext;
import com.example.demo.strategy.DiscountStrategy;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final DiscountContext discountContext;

    @Autowired
    public ProductService(ProductRepository productRepository, DiscountContext discountContext) {
        this.productRepository = productRepository;
        this.discountContext = discountContext;
    }

    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            product.setDiscountedPrice(calculateFinalPrice(product));
        }
        return products;
    }

    public Product getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        product.setDiscountedPrice(calculateFinalPrice(product));
        return product;
    }

    public Product saveProduct(Product product) {
        if (product.getDetail() != null) {
            product.getDetail().setProduct(product);
        }

        if (product.getReviews() != null && !product.getReviews().isEmpty()) {
            List<Review> validReviews = product.getReviews().stream()
                    .filter(r -> r.getReviewer() != null && !r.getReviewer().trim().isEmpty())
                    .peek(r -> {
                        r.setProduct(product);
                        if (r.getReviewDate() == null) {
                            r.setReviewDate(LocalDate.now());
                        }
                    })
                    .toList();

            product.getReviews().clear();
            product.getReviews().addAll(validReviews);
        }

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public double calculateFinalPrice(Product product) {
        if (product.getPrice() == null)
            return 0.0;
        DiscountStrategy strategy = discountContext.getStrategy(product.getDiscountType());
        return strategy.applyDiscount(product.getPrice());
    }
}