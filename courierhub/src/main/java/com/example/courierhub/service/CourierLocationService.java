package com.example.courierhub.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class CourierLocationService {
    private final Map<Long, CourierLocation> courierLocations = new ConcurrentHashMap<>();
    
    public void updateLocation(Long courierId, double latitude, double longitude) {
        courierLocations.put(courierId, new CourierLocation(latitude, longitude));
    }
    
    public CourierLocation getLocation(Long courierId) {
        return courierLocations.get(courierId);
    }
    
    public record CourierLocation(double latitude, double longitude) {}
}
