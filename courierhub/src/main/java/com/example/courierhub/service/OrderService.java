package com.example.courierhub.service;

import com.example.courierhub.model.Order;
import com.example.courierhub.model.User;
import com.example.courierhub.model.Courier;
import com.example.courierhub.model.OrderStatus;
import com.example.courierhub.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Comparator;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final MapService mapService;
    private final CourierService courierService;
    
    private static final BigDecimal BASE_PRICE = new BigDecimal("200"); // Базовая стоимость
    private static final BigDecimal PRICE_PER_KM = new BigDecimal("50"); // Стоимость за километр
    
    @Transactional
    public Order createOrder(Order order, User user) {
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        
        // Рассчитываем стоимость доставки
        BigDecimal deliveryPrice = calculateDeliveryPrice(order);
        order.setDeliveryPrice(deliveryPrice);
        
        return orderRepository.save(order);
    }
    
    public BigDecimal calculateDeliveryPrice(Order order) {
        double distance = calculateDistance(
            order.getFromLatitude(),
            order.getFromLongitude(),
            order.getToLatitude(),
            order.getToLongitude()
        );
        
        BigDecimal distanceInKm = BigDecimal.valueOf(distance);
        return BASE_PRICE.add(PRICE_PER_KM.multiply(distanceInKm)).setScale(2, RoundingMode.HALF_UP);
    }
    
    public Order getOrder(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Order> getAvailableOrders() {
        return orderRepository.findByStatus(OrderStatus.PENDING);
    }

    public Order findNearestOrder(Courier courier) {
        if (courier.getCurrentLatitude() == null || courier.getCurrentLongitude() == null) {
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

    public RouteInfo getCurrentRouteInfo(Long courierId) {
        Courier courier = courierService.getCourierById(courierId);
        Order currentOrder = courierService.getCurrentOrder(courierId);

        if (currentOrder == null || courier.getCurrentLatitude() == null || courier.getCurrentLongitude() == null) {
            return null;
        }

        MapService.RouteInfo routeToPickup = null;
        MapService.RouteInfo routeToDelivery = null;

        // Calculate route from courier to pickup location
        if (currentOrder.getStatus() == OrderStatus.IN_PROGRESS) {
            routeToPickup = mapService.calculateRoute(
                new MapService.Location(courier.getCurrentLatitude(), courier.getCurrentLongitude()),
                new MapService.Location(currentOrder.getFromLatitude(), currentOrder.getFromLongitude())
            );
        }

        // Calculate route from pickup to delivery location
        routeToDelivery = mapService.calculateRoute(
            new MapService.Location(currentOrder.getFromLatitude(), currentOrder.getFromLongitude()),
            new MapService.Location(currentOrder.getToLatitude(), currentOrder.getToLongitude())
        );

        return new RouteInfo(routeToPickup, routeToDelivery);
    }

    public record RouteInfo(
        MapService.RouteInfo routeToPickup,
        MapService.RouteInfo routeToDelivery
    ) {}

    
}