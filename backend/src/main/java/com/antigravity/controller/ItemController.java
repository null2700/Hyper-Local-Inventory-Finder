package com.antigravity.controller;

import com.antigravity.dto.InventoryItemDTO;
import com.antigravity.entity.Store;
import com.antigravity.enums.Category;
import com.antigravity.exception.InvalidCoordinatesException;
import com.antigravity.service.ItemService;
import com.antigravity.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Validated
@Tag(name = "Inventory Items", description = "API for managing inventory items")
public class ItemController {

    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);

    private final ItemService itemService;
    private final StoreService storeService;

    public ItemController(ItemService itemService, StoreService storeService) {
        this.itemService = itemService;
        this.storeService = storeService;
    }

    @GetMapping("/items/search")
    @Operation(summary = "Search items near location", description = "Find inventory items near a specific location with optional filters")
    public ResponseEntity<List<InventoryItemDTO>> searchItems(
            @Parameter(description = "User latitude", required = true)
            @RequestParam @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") double lat,

            @Parameter(description = "User longitude", required = true)
            @RequestParam @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") double lng,

            @Parameter(description = "Search radius in kilometers", required = false)
            @RequestParam(defaultValue = "5.0") @DecimalMin("0.1") @DecimalMax("50.0") double radius,

            @Parameter(description = "Item category filter", required = false)
            @RequestParam(required = false) Category category,

            @Parameter(description = "Search query for item name/description", required = false)
            @RequestParam(required = false) String query,

            @Parameter(description = "Sort order: distance, price_asc, price_desc", required = false)
            @RequestParam(defaultValue = "distance") String sort,

            @Parameter(description = "Page number (0-based)", required = false)
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size", required = false)
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        logger.info("Search request: lat={}, lng={}, radius={}, category={}, query={}, sort={}, page={}, size={}",
                   lat, lng, radius, category, query, sort, page, size);

        // Validate coordinates
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new InvalidCoordinatesException("Invalid latitude or longitude values");
        }

        Sort sortOrder = getSortOrder(sort);
        Pageable pageable = sortOrder == null ? PageRequest.of(page, size) : PageRequest.of(page, size, sortOrder);

        List<InventoryItemDTO> items = itemService.findItemsNearLocation(lat, lng, radius, category, query, pageable);

        return ResponseEntity.ok(items);
    }

    @PostMapping("/items")
    @Operation(summary = "Create new inventory item", description = "Add a new item to inventory")
    public ResponseEntity<InventoryItemDTO> createItem(@Valid @RequestBody InventoryItemDTO itemDTO) {
        logger.info("Creating new item: {}", itemDTO.getName());
        InventoryItemDTO createdItem = itemService.createItem(itemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @PostMapping("/items/upload-image")
    @Operation(summary = "Upload item image", description = "Upload an image for an item")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        logger.info("Uploading image: {}", file.getOriginalFilename());
        try {
            String imagePath = itemService.saveImage(file);
            logger.info("Image saved successfully: {}", imagePath);
            return ResponseEntity.ok(imagePath);
        } catch (IOException e) {
            logger.error("Error uploading image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading image: " + e.getMessage());
        }
    }

    @PutMapping("/items/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update item stock", description = "Update stock quantity for an item (Admin only)")
    public ResponseEntity<InventoryItemDTO> updateStock(
            @PathVariable UUID id,
            @RequestParam @Min(0) int stockQuantity) {
        logger.info("Updating stock for item {} to {}", id, stockQuantity);
        InventoryItemDTO updatedItem = itemService.updateStock(id, stockQuantity);
        return ResponseEntity.ok(updatedItem);
    }

    @GetMapping("/items/{id}")
    @Operation(summary = "Get item by ID", description = "Retrieve a specific inventory item by its ID")
    public ResponseEntity<InventoryItemDTO> getItem(@PathVariable UUID id) {
        InventoryItemDTO item = itemService.getItemById(id);
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete item", description = "Remove an item from inventory (Admin only)")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        logger.info("Deleting item {}", id);
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stores/nearby")
    @Operation(summary = "Find nearby stores", description = "Find stores near a specific location")
    public ResponseEntity<List<Store>> getNearbyStores(
            @RequestParam @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") double lat,
            @RequestParam @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") double lng,
            @RequestParam(defaultValue = "10.0") @DecimalMin("0.1") @DecimalMax("50.0") double radius) {

        logger.info("Finding stores near lat={}, lng={}, radius={}", lat, lng, radius);
        List<Store> stores = storeService.findStoresNearLocation(lat, lng, radius);
        return ResponseEntity.ok(stores);
    }

    private Sort getSortOrder(String sort) {
        return switch (sort.toLowerCase()) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "distance" -> null; // Distance sorting is handled in the query
            default -> null;
        };
    }
}