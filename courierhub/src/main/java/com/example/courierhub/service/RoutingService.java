// package com.example.courierhub.service;

// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import lombok.RequiredArgsConstructor;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;

// @Service
// @RequiredArgsConstructor
// public class RoutingService {
//     private static final String OSRM_URL = "http://router.project-osrm.org/route/v1/driving/";
//     private final RestTemplate restTemplate;
//     private final ObjectMapper objectMapper;

//     public Optional<RouteResult> calculateRoute(double fromLat, double fromLng, double toLat, double toLng) {
//         try {
//             String url = String.format("%s%f,%f;%f,%f?overview=full",
//                 OSRM_URL, fromLng, fromLat, toLng, toLat);
            
//             JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            
//             if (response != null && response.has("routes") && response.get("routes").size() > 0) {
//                 JsonNode route = response.get("routes").get(0);
//                 return Optional.of(new RouteResult(
//                     route.get("distance").asDouble() / 1000, // Convert to kilometers
//                     route.get("duration").asDouble() / 60,   // Convert to minutes
//                     route.get("geometry").asText()
//                 ));
//             }
//             return Optional.empty();
//         } catch (Exception e) {
//             return Optional.empty();
//         }
//     }

//     public record RouteResult(double distance, double duration, String geometry) {}
// }