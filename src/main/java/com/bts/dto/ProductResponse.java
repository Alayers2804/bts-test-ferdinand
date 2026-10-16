
package com.bts.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bts.entity.Product;

public class ProductResponse {

    private Long id;
    private String title;
    private BigDecimal price;
    private String description;
    private String category;
    private String images;
    private LocalDateTime createdAt;
    private String createdBy;
    private Long created_by_id;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private Long updated_by_id;

    public ProductResponse(Product product) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.price = product.getPrice();
        this.description = product.getDescription();

        this.category = product.getCategory() != null
                ? product.getCategory().getDisplayName()
                : null;

        this.images = product.getImages();
        this.createdAt = product.getCreatedAt();
        this.createdBy = product.getCreatedBy();
        this.created_by_id = product.getCreated_by_id();
        this.updatedAt = product.getUpdatedAt();
        this.updatedBy = product.getUpdatedBy();
        this.updated_by_id = product.getUpdated_by_id();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getImages() {
        return images;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Long getCreated_by_id() {
        return created_by_id;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public Long getUpdated_by_id() {
        return updated_by_id;
    }
}
