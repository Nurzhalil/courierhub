package com.example.courierhub.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MapService {
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?format=json&q=";
    private static final String OSRM_URL = "http://router.project-osrm.org/route/v1/driving/";
    private final RestTemplate restTemplate;

    public Location geocodeAddress(String address) {
        try {
            String url = NOMINATIM_URL + address;
            JsonNode[] response = restTemplate.getForObject(url, JsonNode[].class);
            
            if (response != null && response.length > 0) {
                JsonNode location = response[0];
                return new Location(
                    Double.parseDouble(location.get("lat").asText()),
                    Double.parseDouble(location.get("lon").asText())
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public RouteInfo calculateRoute(Location from, Location to) {
    try {
        String url = String.format(Locale.US, "%s%f,%f;%f,%f?overview=full&geometries=polyline",
            OSRM_URL, from.longitude(), from.latitude(), to.longitude(), to.latitude());

        JsonNode response = restTemplate.getForObject(url, JsonNode.class);

        if (response != null && response.has("routes") && response.get("routes").size() > 0) {
            JsonNode route = response.get("routes").get(0);
            return new RouteInfo(
                route.get("distance").asDouble() / 1000, // km
                route.get("duration").asDouble() / 60,   // minutes
                decodePolyline(route.get("geometry").asText())
            );
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}

    private List<Location> decodePolyline(String encoded) {
        List<Location> points = new ArrayList<>();
        int index = 0;
        int len = encoded.length();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            points.add(new Location(lat / 1e5, lng / 1e5));
        }

        return points;
    }

    public record Location(double latitude, double longitude) {}
    public record RouteInfo(double distance, double duration, List<Location> points) {}
}