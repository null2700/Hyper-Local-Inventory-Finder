package com.antigravity.service;

import com.antigravity.entity.Store;
import com.antigravity.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public List<Store> findStoresNearLocation(double lat, double lng, double radiusKm) {
        return storeRepository.findStoresNearLocation(lat, lng, radiusKm);
    }

    public Store getStoreById(UUID id) {
        return storeRepository.findById(id).orElse(null);
    }

    public Store createStore(Store store) {
        return storeRepository.save(store);
    }
}