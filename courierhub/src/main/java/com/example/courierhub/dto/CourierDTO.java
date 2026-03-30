package com.example.courierhub.dto;

import com.example.courierhub.model.Courier;
import com.example.courierhub.model.CourierStatus;
import lombok.Data;

@Data
public class CourierDTO {
    private Long id;
    private String fullName;
    private String phone;
    private String address;
    private String city;
    private String birthDate;
    private String passportNumber;
    private String username;
    private Double currentLatitude;
    private Double currentLongitude;
    private String lastLocationUpdate;
    private CourierStatus status;
    private Integer dailyRejections;
    private String rejectionResetDate;
    private String createdAt;
    private String updatedAt;

    public CourierDTO(Courier courier) {
        this.id = courier.getId();
        this.fullName = courier.getFullName();
        this.phone = courier.getPhone();
        this.address = courier.getAddress();
        this.city = courier.getCity();
        this.birthDate = courier.getBirthDate() != null ? courier.getBirthDate().toString() : null;
        this.passportNumber = courier.getPassportNumber();
        this.username = courier.getUsername();
        this.currentLatitude = courier.getCurrentLatitude();
        this.currentLongitude = courier.getCurrentLongitude();
        this.lastLocationUpdate = courier.getLastLocationUpdate() != null ? courier.getLastLocationUpdate().toString() : null;
        this.status = courier.getStatus();
        this.dailyRejections = courier.getDailyRejections();
        this.rejectionResetDate = courier.getRejectionResetDate() != null ? courier.getRejectionResetDate().toString() : null;
        this.createdAt = courier.getCreatedAt() != null ? courier.getCreatedAt().toString() : null;
        this.updatedAt = courier.getUpdatedAt() != null ? courier.getUpdatedAt().toString() : null;
    }
}
