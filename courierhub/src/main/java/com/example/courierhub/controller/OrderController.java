package com.example.courierhub.controller;

import com.example.courierhub.model.Order;
import com.example.courierhub.model.User;
import com.example.courierhub.model.Courier;
import com.example.courierhub.model.CourierStatus;
import com.example.courierhub.model.OrderStatus;
import com.example.courierhub.service.CourierService;
import com.example.courierhub.service.MapService;
import com.example.courierhub.service.OrderService;
import com.example.courierhub.service.MapService.Location;
import com.example.courierhub.service.MapService.RouteInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class OrderController {
    private final OrderService orderService;
    private final MapService mapService;
    private final CourierService courierService;
    
    @GetMapping("/new")
    public String newOrder(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("order", new Order());
        model.addAttribute("userOrders", orderService.getOrdersByUser(user));
        return "orders/create";
    }
    
    @GetMapping("/{id}/details")
    @ResponseBody
    public ResponseEntity<?> getOrderDetails(@PathVariable Long id, @AuthenticationPrincipal User user) {
        try {
            Order order = orderService.getOrder(id);
            
            // Проверяем, принадлежит ли заказ текущему пользователю
            if (!order.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Access denied");
            }

            // Получаем информацию о маршруте
            RouteInfo routeInfo = null;
            if (order.getFromLatitude() != null && order.getFromLongitude() != null &&
                order.getToLatitude() != null && order.getToLongitude() != null) {
                routeInfo = mapService.calculateRoute(
                    new Location(order.getFromLatitude(), order.getFromLongitude()),
                    new Location(order.getToLatitude(), order.getToLongitude())
                );
            }

            OrderDetailsResponse details = new OrderDetailsResponse(
                order.getId(),
                order.getFromLatitude(),
                order.getFromLongitude(),
                order.getToLatitude(),
                order.getToLongitude(),
                order.getStatus(),
                order.getCourier(),
                routeInfo,
                order.getCreatedAt(),
                order.getEstimatedPickupTime(),
                order.getEstimatedDeliveryTime()
            );
            
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get order details: " + e.getMessage());
        }
        
    }
    
    @PostMapping("/geocode")
    @ResponseBody
    public Location geocodeAddress(@RequestParam String address) {
        return mapService.geocodeAddress(address);
    }
    
    @PostMapping("/route")
    @ResponseBody
    public RouteInfo calculateRoute(
            @RequestParam double fromLat, @RequestParam double fromLng,
            @RequestParam double toLat, @RequestParam double toLng) {
        Location from = new Location(fromLat, fromLng);
        Location to = new Location(toLat, toLng);
        return mapService.calculateRoute(from, to);
    }
    
    @PostMapping
    public String createOrder(@ModelAttribute Order order, @AuthenticationPrincipal User user) {
        orderService.createOrder(order, user);
        return "redirect:/orders/success";
    }
    
    @GetMapping("/success")
    public String orderSuccess() {
        return "orders/success";
    }
    
    public record OrderDetailsResponse(
        Long id,
        Double fromLatitude,
        Double fromLongitude,
        Double toLatitude,
        Double toLongitude,
        OrderStatus status,
        Courier courier,
        RouteInfo routeInfo,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime estimatedPickupTime,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime estimatedDeliveryTime
    ) {}

    @PostMapping("/{id}/delivery")
    @ResponseBody
    public ResponseEntity<?> deliverOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        try {
            Order order = orderService.getOrder(id);
            Courier courier = order.getCourier();
            if (courier == null) {
                return ResponseEntity.badRequest().body("Courier not found");
            }

            if (courier.getStatus() != CourierStatus.ON_DELIVERY) {
                return ResponseEntity.badRequest().body("No active delivery");
            }

            courierService.completeOrder(courier, order);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to complete order: " + e.getMessage());
        }
    }
}