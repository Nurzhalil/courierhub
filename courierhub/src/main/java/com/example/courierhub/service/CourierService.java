package com.example.courierhub.service;

import  com.example.courierhub.model.Courier;
import  com.example.courierhub.model.CourierStatus;
import  com.example.courierhub.model.Order;
import  com.example.courierhub.model.OrderStatus;
import  com.example.courierhub.repository.CourierRepository;
import  com.example.courierhub.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class CourierService {
    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Courier> getAllCouriers() {
        return courierRepository.findAll();
    }

    public Courier getCourierById(Long id) {
        return courierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Courier not found"));
    }

    public Courier getCourierByUsername(String username) {
        return courierRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Courier not found"));
    }

    @Transactional
    public Courier saveCourier(Courier courier, String rawPassword) {
        validateCourier(courier);
        
        if (rawPassword != null && !rawPassword.isEmpty()) {
            courier.setPasswordHash(passwordEncoder.encode(rawPassword));
        } else if (courier.getId() != null) {
            Courier existingCourier = getCourierById(courier.getId());
            courier.setPasswordHash(existingCourier.getPasswordHash());
        } else {
            throw new RuntimeException("Password is required for new couriers");
        }

        // Ensure new couriers start with AVAILABLE status
        if (courier.getId() == null) {
            courier.setStatus(CourierStatus.AVAILABLE);
        }
        
        return courierRepository.save(courier);
    }

    @Transactional
    public void deleteCourierById(Long id) {
        Courier courier = getCourierById(id);
        
        // Check if courier has active orders
        if (courier.getStatus() == CourierStatus.ON_DELIVERY) {
            throw new RuntimeException("Cannot delete courier with active deliveries");
        }
        
        // Check for any assigned orders
        Order currentOrder = getCurrentOrder(id);
        if (currentOrder != null) {
            throw new RuntimeException("Cannot delete courier with assigned orders");
        }
        
        courierRepository.deleteById(id);
    }

    private void validateCourier(Courier courier) {
        // Check phone number uniqueness
        if (courierRepository.existsByPhone(courier.getPhone()) &&
            (courier.getId() == null || !courierRepository.findById(courier.getId()).get().getPhone().equals(courier.getPhone()))) {
            throw new RuntimeException("Phone number already exists");
        }
        
        // Check passport number uniqueness
        if (courierRepository.existsByPassportNumber(courier.getPassportNumber()) &&
            (courier.getId() == null || !courierRepository.findById(courier.getId()).get().getPassportNumber().equals(courier.getPassportNumber()))) {
            throw new RuntimeException("Passport number already exists");
        }
        
        // Check username uniqueness
        if (courierRepository.existsByUsername(courier.getUsername()) &&
            (courier.getId() == null || !courierRepository.findById(courier.getId()).get().getUsername().equals(courier.getUsername()))) {
            throw new RuntimeException("Username already exists");
        }
    }

    @Transactional
    public void updateLocation(Long courierId, Double latitude, Double longitude) {
        Courier courier = getCourierById(courierId);
        courier.setCurrentLatitude(latitude);
        courier.setCurrentLongitude(longitude);
        courier.setLastLocationUpdate(LocalDateTime.now());
        courierRepository.save(courier);
    }

    @Transactional
    public void acceptOrder(Long courierId, Long orderId) {
        Courier courier = getCourierById(courierId);
        
        // Verify courier is available
        if (courier.getStatus() != CourierStatus.AVAILABLE) {
            throw new RuntimeException("Courier is not available");
        }
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
            
        // Verify order is still pending
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Order is no longer available");
        }
        
        // Update courier status
        courier.setStatus(CourierStatus.ON_DELIVERY);
        courierRepository.save(courier);
        
        // Update order status and assign courier
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setCourier(courier);
        orderRepository.save(order);
    }

    @Transactional
    public void rejectOrder(Long courierId, Long orderId) {
        Courier courier = getCourierById(courierId);
        
        // Reset rejections if it's a new day
        LocalDate today = LocalDate.now();
        if (courier.getRejectionResetDate() == null || !courier.getRejectionResetDate().equals(today)) {
            courier.setDailyRejections(0);
            courier.setRejectionResetDate(today);
        }
        
        // Check rejection limit
        if (courier.getDailyRejections() >= 3) {
            throw new RuntimeException("Daily rejection limit reached");
        }
        
        // Increment rejection count
        courier.setDailyRejections(courier.getDailyRejections() + 1);
        courierRepository.save(courier);
        
        // Reset order status
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
            
        order.setStatus(OrderStatus.PENDING);
        order.setCourier(null);
        orderRepository.save(order);
    }

    @Transactional
    public void completeOrder(Long courierId, Long orderId) {
        Courier courier = getCourierById(courierId);
        if (courier.getStatus() != CourierStatus.ON_DELIVERY) {
            throw new RuntimeException("No active delivery");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Завершаем доставку
        courier.setStatus(CourierStatus.AVAILABLE);
        courierRepository.save(courier);

        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
    }


    public Order getCurrentOrder(Long courierId) {
        return orderRepository.findFirstByCourierIdAndStatus(courierId, OrderStatus.IN_PROGRESS)
            .orElse(null);
    }

    public Order findNearestOrder(Courier courier) {
        if (courier.getStatus() != CourierStatus.AVAILABLE || 
            courier.getCurrentLatitude() == null || 
            courier.getCurrentLongitude() == null) {
            return null;
        }

        return orderRepository.findByStatus(OrderStatus.PENDING)
            .stream()
            .min(Comparator.comparingDouble(order -> 
                calculateDistance(
                    courier.getCurrentLatitude(),
                    courier.getCurrentLongitude(),
                    order.getFromLatitude(),
                    order.getFromLongitude()
                )
            ))
            .orElse(null);
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    @Transactional
    public void completeOrder(Courier courier, Order order) {
        courier.setStatus(CourierStatus.AVAILABLE);
        order.setStatus(OrderStatus.DELIVERED);
        courierRepository.save(courier);
        orderRepository.save(order);
    }

}