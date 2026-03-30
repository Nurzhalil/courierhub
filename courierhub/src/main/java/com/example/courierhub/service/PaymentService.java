package com.example.courierhub.service;

import com.example.courierhub.model.*;
import com.example.courierhub.repository.PaymentRepository;
import com.example.courierhub.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Payment createPayment(Long orderId, BigDecimal amount, PaymentMethod method) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
            
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        
        return paymentRepository.save(payment);
    }
    
    @Transactional
    public Payment processPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
            
        // Здесь будет логика обработки платежа через платежную систему
        // Пока просто имитируем успешную оплату
        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setTransactionId("TRANS-" + System.currentTimeMillis());
        
        try {
            // Имитация обработки платежа
            Thread.sleep(2000);
            payment.setStatus(PaymentStatus.COMPLETED);
        } catch (InterruptedException e) {
            payment.setStatus(PaymentStatus.FAILED);
            throw new RuntimeException("Payment processing failed");
        }
        
        return paymentRepository.save(payment);
    }
    
    @Transactional
    public Payment refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
            
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new RuntimeException("Payment cannot be refunded");
        }
        
        // Здесь будет логика возврата через платежную систему
        payment.setStatus(PaymentStatus.REFUNDED);
        return paymentRepository.save(payment);
    }
    
    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
    }
}
