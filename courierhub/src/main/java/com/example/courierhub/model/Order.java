package com.example.courierhub.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "courier_id")
    private Courier courier;
    
    @Column(nullable = false)
    private String fromAddress;
    
    @Column(nullable = false)
    private String toAddress;
    
    @Column(nullable = false)
    private String receiverName;
    
    @Column(nullable = false)
    private String itemDescription;
    
    @Column(name = "from_latitude", nullable = false)
    private Double fromLatitude;
    
    @Column(name = "from_longitude", nullable = false)
    private Double fromLongitude;
    
    @Column(name = "to_latitude", nullable = false)
    private Double toLatitude;
    
    @Column(name = "to_longitude", nullable = false)
    private Double toLongitude;
    
    @Column(name = "delivery_price", nullable = false)
    private BigDecimal deliveryPrice;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;
    
    @Column(name = "estimated_pickup_time")
    private LocalDateTime estimatedPickupTime;
    
    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;
}