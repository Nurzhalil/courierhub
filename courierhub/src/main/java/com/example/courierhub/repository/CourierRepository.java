package com.example.courierhub.repository;

import com.example.courierhub.model.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {
    boolean existsByPhone(String phone);
    boolean existsByPassportNumber(String passportNumber);
    boolean existsByUsername(String username);
    Optional<Courier> findByPhone(String phone);
    Optional<Courier> findByUsername(String username);
}
