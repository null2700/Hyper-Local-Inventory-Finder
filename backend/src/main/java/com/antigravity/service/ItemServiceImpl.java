package com.antigravity.service;

import com.antigravity.dto.InventoryItemDTO;
import com.antigravity.entity.InventoryItem;
import com.antigravity.entity.Store;
import com.antigravity.enums.Category;
import com.antigravity.exception.ResourceNotFoundException;
import com.antigravity.repository.InventoryItemRepository;
import com.antigravity.repository.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    private static final Logger logger = LoggerFactory.getLogger(ItemServiceImpl.class);

    private final InventoryItemRepository itemRepository;
    private final StoreRepository storeRepository;
    private final DistanceService distanceService;

    public ItemServiceImpl(InventoryItemRepository itemRepository,
                          StoreRepository storeRepository,
                          DistanceService distanceService) {
        this.itemRepository = itemRepository;
        this.storeRepository = storeRepository;
        this.distanceService = distanceService;
    }

    @Override
    @Cacheable(value = "items", key = "#lat + '_' + #lng + '_' + #radiusKm + '_' + #category + '_' + #query + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public List<InventoryItemDTO> findItemsNearLocation(double lat, double lng, double radiusKm,
                                                       Category category, String query, Pageable pageable) {
        MDC.put("userLat", String.valueOf(lat));
        MDC.put("userLng", String.valueOf(lng));
        logger.info("Searching items near location: lat={}, lng={}, radius={}km, category={}, query={}",
                   lat, lng, radiusKm, category, query);

        List<InventoryItem> items = itemRepository.findItemsNearLocation(lat, lng, radiusKm, category, query, pageable);

        List<InventoryItemDTO> dtos = items.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        distanceService.sortByDistance(dtos, lat, lng);

        logger.info("Found {} items near location", dtos.size());
        MDC.clear();
        return dtos;
    }

    @Override
    public InventoryItemDTO createItem(InventoryItemDTO itemDTO) {
        logger.info("Creating new inventory item: {}", itemDTO.getName());

        Store store = storeRepository.findById(itemDTO.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Store not found: " + itemDTO.getStoreId()));

        InventoryItem item = new InventoryItem(
                itemDTO.getName(),
                itemDTO.getDescription(),
                itemDTO.getCategory(),
                itemDTO.getPrice(),
                itemDTO.getStockQuantity(),
                itemDTO.getStoreName(),
                store,
                itemDTO.getLatitude(),
                itemDTO.getLongitude(),
                itemDTO.getImageUrl(),
                itemDTO.getUnit(),
                itemDTO.getIsAvailable()
        );

        InventoryItem savedItem = itemRepository.save(item);
        logger.info("Created inventory item with ID: {}", savedItem.getId());
        return mapToDTO(savedItem);
    }

    @Override
    public InventoryItemDTO updateStock(UUID itemId, int newStockQuantity) {
        logger.info("Updating stock for item {} to {}", itemId, newStockQuantity);

        InventoryItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));

        item.setStockQuantity(newStockQuantity);
        item.setIsAvailable(newStockQuantity > 0);

        InventoryItem updatedItem = itemRepository.save(item);
        logger.info("Updated stock for item {}", itemId);
        return mapToDTO(updatedItem);
    }

    @Override
    public void deleteItem(UUID itemId) {
        logger.info("Deleting item {}", itemId);

        if (!itemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Item not found: " + itemId);
        }

        itemRepository.deleteById(itemId);
        logger.info("Deleted item {}", itemId);
    }

    @Override
    public InventoryItemDTO getItemById(UUID itemId) {
        logger.debug("Fetching item {}", itemId);

        InventoryItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));

        return mapToDTO(item);
    }

    private InventoryItemDTO mapToDTO(InventoryItem item) {
        return new InventoryItemDTO(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getCategory(),
                item.getPrice(),
                item.getStockQuantity(),
                item.getStoreName(),
                item.getStore().getId(),
                item.getLatitude(),
                item.getLongitude(),
                item.getImageUrl(),
                item.getUnit(),
                item.getIsAvailable(),
                item.getLastUpdated(),
                null // distance will be set later if needed
        );
    }

    @Override
    public String saveImage(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            fileName = UUID.randomUUID().toString();
        }
        String uploadDir = "src/main/resources/static/images/";
        
        // Create directory if it doesn't exist
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // Save the file
        String filePath = uploadDir + fileName;
        Files.write(Paths.get(filePath), file.getBytes());
        
        logger.info("Image saved successfully: {}", filePath);
        
        // Return the absolute URL to be used by the frontend
        return "http://localhost:8081/images/" + fileName;
    }
}