package com.example.courierhub.controller;

import com.example.courierhub.model.*;
import com.example.courierhub.service.CourierService;
import com.example.courierhub.service.OrderService;
import com.example.courierhub.service.UserService;
import com.example.courierhub.service.MapService;
import com.example.courierhub.service.CourierLocationService;
import com.example.courierhub.service.MapService.Location;
import com.example.courierhub.service.MapService.RouteInfo;
import com.example.courierhub.service.CourierLocationService.CourierLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/courier")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COURIER')")
public class CourierController {
    private final CourierService courierService;
    private final OrderService orderService;
    private final UserService userService;
    private final MapService mapService;
    private final CourierLocationService locationService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return "redirect:/login?error=not_authenticated";
            }

            Courier courier = userService.getCourierByUsername(userDetails.getUsername());
            if (courier == null) {
                return "redirect:/login?error=not_courier";
            }

            // Получаем текущий заказ курьера
            Order currentOrder = courierService.getCurrentOrder(courier.getId());
            
            // Если нет текущего заказа, ищем ближайший
            Order nearestOrder = null;
            if (currentOrder == null && courier.getStatus() == CourierStatus.AVAILABLE) {
                nearestOrder = orderService.findNearestOrder(courier);
            }

            Double nearestOrderDistance = null;
            CourierLocation location = locationService.getLocation(courier.getId());
            
            // Вычисляем расстояние для ближайшего заказа
            if (nearestOrder != null && location != null) {
                nearestOrderDistance = orderService.calculateDistance(
                    location.latitude(),
                    location.longitude(),
                    nearestOrder.getFromLatitude(),
                    nearestOrder.getFromLongitude()
                );
            }
            
            model.addAttribute("courier", courier);
            model.addAttribute("courierLocation", location);
            model.addAttribute("currentOrder", currentOrder);
            model.addAttribute("nearestOrder", nearestOrder);
            model.addAttribute("nearestOrderDistance", nearestOrderDistance);
            return "courier/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load dashboard: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/location")
    @ResponseBody
    public ResponseEntity<?> updateLocation(
            @RequestBody Map<String, Double> location,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (!location.containsKey("latitude") || !location.containsKey("longitude")) {
                return ResponseEntity.badRequest().body("Invalid location data");
            }

            Courier courier = userService.getCourierByUsername(userDetails.getUsername());
            if (courier == null) {
                return ResponseEntity.badRequest().body("Courier not found");
            }

            // Обновляем местоположение курьера
            courierService.updateLocation(
                courier.getId(),
                location.get("latitude"),
                location.get("longitude")
            );

            locationService.updateLocation(
                courier.getId(),
                location.get("latitude"),
                location.get("longitude")
            );

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update location: " + e.getMessage());
        }
    }

    @PostMapping("/route")
    @ResponseBody
    public RouteInfo calculateRoute(
            @RequestParam double fromLat, @RequestParam double fromLng,
            @RequestParam double toLat, @RequestParam double toLng) {
        Location from = new Location(fromLat, fromLng);
        Location to = new Location(toLat, toLng);
        
        RouteInfo route = mapService.calculateRoute(from, to);
        
        if (route == null) {
            throw new RuntimeException("Failed to calculate route");
        }
        
        return route;
    }

    @GetMapping("/current-route")
    @ResponseBody
    public ResponseEntity<?> getCurrentRoute(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Courier courier = userService.getCourierByUsername(userDetails.getUsername());
            if (courier == null) {
                return ResponseEntity.badRequest().body("Courier not found");
            }

            Order currentOrder = courierService.getCurrentOrder(courier.getId());
            if (currentOrder == null) {
                return ResponseEntity.ok(null);
            }

            // Calculate route from courier to pickup
            RouteInfo routeToPickup = null;
            CourierLocation location = locationService.getLocation(courier.getId());
            if (location != null) {
                routeToPickup = mapService.calculateRoute(
                    new Location(location.latitude(), location.longitude()),
                    new Location(currentOrder.getFromLatitude(), currentOrder.getFromLongitude())
                );
            }

            // Calculate route from pickup to delivery
            RouteInfo routeToDelivery = mapService.calculateRoute(
                new Location(currentOrder.getFromLatitude(), currentOrder.getFromLongitude()),
                new Location(currentOrder.getToLatitude(), currentOrder.getToLongitude())
            );

            return ResponseEntity.ok(new CourierRouteInfo(routeToPickup, routeToDelivery));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get current route: " + e.getMessage());
        }
    }

    @PostMapping("/orders/{orderId}/accept")
    @ResponseBody
    public ResponseEntity<?> acceptOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Courier courier = userService.getCourierByUsername(userDetails.getUsername());
            if (courier == null) {
                return ResponseEntity.badRequest().body("Courier not found");
            }

            courierService.acceptOrder(courier.getId(), orderId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to accept order: " + e.getMessage());
        }
    }

    @PostMapping("/orders/{orderId}/reject")
    @ResponseBody
    public ResponseEntity<?> rejectOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Courier courier = userService.getCourierByUsername(userDetails.getUsername());
            if (courier == null) {
                return ResponseEntity.badRequest().body("Courier not found");
            }

            courierService.rejectOrder(courier.getId(), orderId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to reject order: " + e.getMessage());
        }
    }

    public record CourierRouteInfo(RouteInfo routeToPickup, RouteInfo routeToDelivery) {}
}