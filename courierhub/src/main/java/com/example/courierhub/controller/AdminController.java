package com.example.courierhub.controller;

import com.example.courierhub.model.Courier;
import com.example.courierhub.model.Order;
import com.example.courierhub.model.CourierStatus;
import com.example.courierhub.service.CourierService;
import com.example.courierhub.service.OrderService;
import com.example.courierhub.service.OrderService.RouteInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final CourierService courierService;
    private final OrderService orderService;

    @GetMapping("/couriers")
    public String listCouriers(Model model) {
        model.addAttribute("couriers", courierService.getAllCouriers());
        return "admin/couriers/list";
    }

    @GetMapping("/couriers/new")
    public String showCreateForm(Model model) {
        model.addAttribute("courier", new Courier());
        return "admin/couriers/form";
    }

    @PostMapping("/couriers")
    public String createCourier(@ModelAttribute Courier courier, @RequestParam String password, RedirectAttributes redirectAttributes) {
        try {
            courierService.saveCourier(courier, password);
            redirectAttributes.addFlashAttribute("success", "Courier created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/couriers";
    }

    @GetMapping("/couriers/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("courier", courierService.getCourierById(id));
        return "admin/couriers/form";
    }

    @PostMapping("/couriers/{id}")
    public String updateCourier(@PathVariable Long id, @ModelAttribute Courier courier, 
                              @RequestParam(required = false) String password,
                              RedirectAttributes redirectAttributes) {
        try {
            courier.setId(id);
            courierService.saveCourier(courier, password);
            redirectAttributes.addFlashAttribute("success", "Courier updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/couriers";
    }

    @PostMapping("/couriers/{id}/delete")
    public String deleteCourier(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Courier courier = courierService.getCourierById(id);
            
            // Check if courier has active orders
            if (courier.getStatus() == CourierStatus.ON_DELIVERY) {
                throw new RuntimeException("Cannot delete courier with active deliveries");
            }
            
            // Check if courier has any pending orders
            Order currentOrder = courierService.getCurrentOrder(id);
            if (currentOrder != null) {
                throw new RuntimeException("Cannot delete courier with assigned orders");
            }
            
            // Proceed with deletion
            courierService.deleteCourierById(id);
            redirectAttributes.addFlashAttribute("success", "Courier deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/couriers";
    }

    @GetMapping("/couriers/{id}/location")
    public String showCourierLocation(@PathVariable Long id, Model model) {
        Courier courier = courierService.getCourierById(id);
        model.addAttribute("courier", courier);
        model.addAttribute("currentOrder", courierService.getCurrentOrder(id));
        model.addAttribute("routeInfo", orderService.getCurrentRouteInfo(id));
        return "admin/couriers/location";
    }

    @GetMapping("/api/couriers/{id}/status")
    @ResponseBody
    public CourierStatusResponse getCourierStatus(@PathVariable Long id) {
        Courier courier = courierService.getCourierById(id);
        Order currentOrder = courierService.getCurrentOrder(id);
        RouteInfo routeInfo = orderService.getCurrentRouteInfo(id);
        
        return new CourierStatusResponse(
            courier.getCurrentLatitude(),
            courier.getCurrentLongitude(),
            courier.getStatus(),
            currentOrder,
            routeInfo
        );
    }

    public record CourierStatusResponse(
        Double latitude,
        Double longitude,
        CourierStatus status,
        Order currentOrder,
        RouteInfo routeInfo
    ) {}
}