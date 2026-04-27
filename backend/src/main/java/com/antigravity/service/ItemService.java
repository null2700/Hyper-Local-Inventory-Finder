package com.antigravity.service;

import com.antigravity.dto.InventoryItemDTO;
import com.antigravity.enums.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ItemService {

    List<InventoryItemDTO> findItemsNearLocation(double lat, double lng, double radiusKm,
                                                Category category, String query, Pageable pageable);

    InventoryItemDTO createItem(InventoryItemDTO itemDTO);

    InventoryItemDTO updateStock(UUID itemId, int newStockQuantity);

    void deleteItem(UUID itemId);

    InventoryItemDTO getItemById(UUID itemId);

    String saveImage(MultipartFile file) throws IOException;
}