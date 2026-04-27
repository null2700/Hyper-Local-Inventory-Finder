package com.antigravity.dto;

import com.antigravity.enums.Category;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class InventoryItemDTO {

    private UUID id;
    private String name;
    private String description;
    private Category category;
    private BigDecimal price;
    private Integer stockQuantity;
    private String storeName;
    private UUID storeId;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private String unit;
    private Boolean isAvailable;
    private LocalDateTime lastUpdated;
    private Double distance; // Calculated distance in km
    private Integer estimatedDeliveryMins; // Calculated delivery time

    // Constructors
    public InventoryItemDTO() {}

    public InventoryItemDTO(UUID id, String name, String description, Category category,
                           BigDecimal price, Integer stockQuantity, String storeName, UUID storeId,
                           Double latitude, Double longitude, String imageUrl, String unit,
                           Boolean isAvailable, LocalDateTime lastUpdated, Double distance) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.storeName = storeName;
        this.storeId = storeId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.unit = unit;
        this.isAvailable = isAvailable;
        this.lastUpdated = lastUpdated;
        this.distance = distance;
        this.estimatedDeliveryMins = distance != null ? (int) Math.round(distance * 2) : null;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
        this.estimatedDeliveryMins = distance != null ? (int) Math.round(distance * 2) : null;
    }

    public Integer getEstimatedDeliveryMins() {
        return estimatedDeliveryMins;
    }

    public void setEstimatedDeliveryMins(Integer estimatedDeliveryMins) {
        this.estimatedDeliveryMins = estimatedDeliveryMins;
    }
}