// package com.example.courierhub.controller;

// import com.example.courierhub.service.MapService;
// import com.example.courierhub.service.RoutingService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.Map;
// import java.util.Optional;

// @RestController
// @RequestMapping("/api/map")
// @RequiredArgsConstructor
// public class MapController {
//     private final MapService geocodingService;
//     private final RoutingService routingService;

//     @GetMapping("/geocode")
//     public ResponseEntity<?> geocodeAddress(@RequestParam String address) {
//         return geocodingService.geocodeAddress(address)
//             .map(ResponseEntity::ok)
//             .orElse(ResponseEntity.notFound().build());
//     }

//     @GetMapping("/route")
//     public ResponseEntity<?> calculateRoute(
//             @RequestParam double fromLat,
//             @RequestParam double fromLng,
//             @RequestParam double toLat,
//             @RequestParam double toLng) {
//         return routingService.calculateRoute(fromLat, fromLng, toLat, toLng)
//             .map(ResponseEntity::ok)
//             .orElse(ResponseEntity.notFound().build());
//     }
// }