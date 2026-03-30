package com.example.courierhub.controller;

import com.example.courierhub.model.*;
import com.example.courierhub.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    
    @GetMapping("/order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public String showPaymentForm(@PathVariable Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "payments/form";
    }
    
    @PostMapping("/process")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<?> processPayment(
            @RequestParam Long orderId,
            @RequestParam BigDecimal amount,
            @RequestParam PaymentMethod method) {
        try {
            Payment payment = paymentService.createPayment(orderId, amount, method);
            payment = paymentService.processPayment(payment.getId());
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> refundPayment(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentService.refundPayment(paymentId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}