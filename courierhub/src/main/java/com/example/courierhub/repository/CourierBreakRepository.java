package com.example.courierhub.repository;

import com.example.courierhub.model.CourierBreak;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;

public interface CourierBreakRepository extends JpaRepository<CourierBreak, Long> {
    Optional<CourierBreak> findByCourierIdAndEndTimeGreaterThan(Long courierId, LocalDateTime now);
}