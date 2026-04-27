package com.antigravity.repository;

import com.antigravity.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

    @Query("""
        SELECT s FROM Store s
        WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(s.latitude)) *
             cos(radians(s.longitude) - radians(:lng)) +
             sin(radians(:lat)) * sin(radians(s.latitude)))) <= :radiusKm
        ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(s.latitude)) *
                  cos(radians(s.longitude) - radians(:lng)) +
                  sin(radians(:lat)) * sin(radians(s.latitude))))
        """)
    List<Store> findStoresNearLocation(@Param("lat") double lat,
                                      @Param("lng") double lng,
                                      @Param("radiusKm") double radiusKm);
}