package com.bts.service;

import com.bts.entity.CategoryEnum;
import com.bts.entity.Product;
import com.bts.handler.ProductNotFoundException;
import com.bts.repository.ProductRepository;

import jakarta.persistence.criteria.Predicate;

import com.bts.dto.ProductRequest;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Cacheable(
    cacheNames = "products",
    key = "#search + '-' + #category + '-' + #limit + '-' + #page"
    )
 public List<Product> getAllProducts(
            String search,
            String category,
            int limit,
            int page) {

        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException(
                "Limit must be between 1 and 100"
            );
        }

        if (page < 1) {
            throw new IllegalArgumentException(
                "Page must be at least 1"
            );
        }

        Specification<Product> specification =
            (root, query, cb) -> {

                List<Predicate> predicates = new ArrayList<>();

                if (search != null && !search.isBlank()) {
                    predicates.add(
                        cb.like(
                            cb.lower(root.get("title")),
                            "%" + search.trim().toLowerCase() + "%"
                        )
                    );
                }

                if (category != null && !category.isBlank()) {
                    CategoryEnum categoryEnum =
                        findCategory(category);

                    predicates.add(
                        cb.equal(
                            root.get("category"),
                            categoryEnum
                        )
                    );
                }

                return cb.and(
                    predicates.toArray(new Predicate[0])
                );
            };

        Pageable pageable = PageRequest.of(page - 1, limit);

        return productRepository
            .findAll(specification, pageable)
            .getContent();
    }

    private CategoryEnum findCategory(String category) {
        for (CategoryEnum value : CategoryEnum.values()) {
            if (value.name().equalsIgnoreCase(category.trim())) {
                return value;
            }
        }

        throw new IllegalArgumentException(
            "Invalid category: " + category
        );
    }

    @Cacheable(cacheNames = "product", key = "#id")
    public Product getProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() ->
                new ProductNotFoundException(
                    "Product with ID " + id + " was not found"
                )
            );
    }

    @Transactional
    @CacheEvict(
    cacheNames = {"products", "product"},
    allEntries = true
    )
    public Product createProduct(ProductRequest request) {
        Product product = new Product();

        product.setTitle(request.getTitle());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setImage(request.getImage());

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductRequest request) {
        Product product = getProductById(id);

        product.setTitle(request.getTitle());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setImage(request.getImage());

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }
}