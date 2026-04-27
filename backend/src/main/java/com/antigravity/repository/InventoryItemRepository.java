package com.antigravity.repository;

import com.antigravity.entity.InventoryItem;
import com.antigravity.enums.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {

    @Query("""
        SELECT i FROM InventoryItem i
        WHERE i.isAvailable = true
        AND i.stockQuantity > 0
        AND (:category IS NULL OR i.category = :category)
        AND (:query IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(i.description) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (6371 * acos(cos(radians(:lat)) * cos(radians(i.latitude)) *
             cos(radians(i.longitude) - radians(:lng)) +
             sin(radians(:lat)) * sin(radians(i.latitude)))) <= :radiusKm
        ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(i.latitude)) *
                  cos(radians(i.longitude) - radians(:lng)) +
                  sin(radians(:lat)) * sin(radians(i.latitude))))
        """)
    List<InventoryItem> findItemsNearLocation(@Param("lat") double lat,
                                             @Param("lng") double lng,
                                             @Param("radiusKm") double radiusKm,
                                             @Param("category") Category category,
                                             @Param("query") String query,
                                             Pageable pageable);
}