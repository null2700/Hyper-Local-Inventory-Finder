package com.antigravity.service;

import com.antigravity.dto.InventoryItemDTO;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class DistanceService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculate distance between two points using Haversine formula
     * @param lat1 Latitude of first point
     * @param lng1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lng2 Longitude of second point
     * @return Distance in kilometers
     */
    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Sort items by distance from user location
     * @param items List of items to sort
     * @param userLat User's latitude
     * @param userLng User's longitude
     */
    public void sortByDistance(List<InventoryItemDTO> items, double userLat, double userLng) {
        items.forEach(item -> {
            double distance = calculateDistance(userLat, userLng, item.getLatitude(), item.getLongitude());
            item.setDistance(distance);
        });

        items.sort(Comparator.comparing(InventoryItemDTO::getDistance));
    }

    /**
     * Filter items within radius and sort by distance
     * @param items List of items to filter and sort
     * @param userLat User's latitude
     * @param userLng User's longitude
     * @param radiusKm Maximum radius in kilometers
     * @return Filtered and sorted list
     */
    public List<InventoryItemDTO> filterAndSortByDistance(List<InventoryItemDTO> items,
                                                        double userLat, double userLng,
                                                        double radiusKm) {
        items.removeIf(item -> {
            double distance = calculateDistance(userLat, userLng, item.getLatitude(), item.getLongitude());
            item.setDistance(distance);
            return distance > radiusKm;
        });

        items.sort(Comparator.comparing(InventoryItemDTO::getDistance));
        return items;
    }
}