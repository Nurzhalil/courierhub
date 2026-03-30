package com.example.courierhub.service;

import com.example.courierhub.model.User;
import com.example.courierhub.model.Courier;
import com.example.courierhub.repository.UserRepository;
import com.example.courierhub.repository.CourierRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final CourierRepository courierRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // First try to find in users table
        var userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            return userOptional.get();
        }
        
        // If not found in users, try couriers table
        var courierOptional = courierRepository.findByUsername(username);
        if (courierOptional.isPresent()) {
            Courier courier = courierOptional.get();
            return new org.springframework.security.core.userdetails.User(
                courier.getUsername(),
                courier.getPasswordHash(),
                true, true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_COURIER"))
            );
        }
        
        throw new UsernameNotFoundException("User not found with username: " + username);
    }
    
    public User registerUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        if (courierRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists as courier");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setAdmin(false);
        
        return userRepository.save(user);
    }

    public Courier getCourierByUsername(String username) {
        return courierRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Courier not found with username: " + username));
    }
}