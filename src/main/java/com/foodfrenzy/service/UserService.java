package com.foodfrenzy.service;

import com.foodfrenzy.dto.UserProfileDto;
import com.foodfrenzy.dto.UserRegistrationDto;
import com.foodfrenzy.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {
    User registerCustomer(UserRegistrationDto registrationDto);
    Optional<User> findByEmail(String email);
    User getById(Long id);
    User updateProfile(Long userId, UserProfileDto profileDto);
    Page<User> getAllUsers(Pageable pageable);
    Page<User> searchUsers(String query, Pageable pageable);
    User createUserByAdmin(String name, String email, String password, String phone, String address, String roleName);
    User updateUserByAdmin(Long id, String name, String phone, String address, boolean active, String roleName);
    void toggleUserStatus(Long id);
    long countCustomers();
}
