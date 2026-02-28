package com.itp41.lms.service;

import com.itp41.lms.model.User;
import com.itp41.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public User registerUser(User user) {
        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists!");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }
        
        // Check if phone number already exists
        if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists!");
        }
        
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Save user to database
        User savedUser = userRepository.save(user);
        
        System.out.println("✅ User registered successfully: " + savedUser.getUsername());
        
        return savedUser;
    }
    
    public Optional<User> loginUser(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        
        // Check if user exists and password matches
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            System.out.println("✅ Login successful for user: " + username);
            return user;
        }
        
        System.out.println("❌ Login failed for user: " + username);
        return Optional.empty();
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public User updateUserProfile(Long userId, String fullName, String email, String phoneNumber) {
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User not found!");
        }
        
        User user = userOpt.get();
        
        // Check if email is being changed and if it's already taken by another user
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists!");
        }
        
        // Check if phone number is being changed and if it's already taken by another user
        if (!user.getPhoneNumber().equals(phoneNumber) && userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new RuntimeException("Phone number already exists!");
        }
        
        // Update user fields
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        
        // Save updated user
        return userRepository.save(user);
    }
    
    public User updateUserPassword(Long userId, String currentPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User not found!");
        }
        
        User user = userOpt.get();
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect!");
        }
        
        // Encode and set new password
        user.setPassword(passwordEncoder.encode(newPassword));
        
        // Save updated user
        return userRepository.save(user);
    }
    
    public List<User> getAllActiveStudents() {
        return userRepository.findByIsActive(true);
    }
}
