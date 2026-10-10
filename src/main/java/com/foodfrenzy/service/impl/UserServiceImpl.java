package com.foodfrenzy.service.impl;

import com.foodfrenzy.dto.UserProfileDto;
import com.foodfrenzy.dto.UserRegistrationDto;
import com.foodfrenzy.entity.Cart;
import com.foodfrenzy.entity.Role;
import com.foodfrenzy.entity.User;
import com.foodfrenzy.exception.DuplicateEmailException;
import com.foodfrenzy.exception.ResourceNotFoundException;
import com.foodfrenzy.repository.CartRepository;
import com.foodfrenzy.repository.RoleRepository;
import com.foodfrenzy.repository.UserRepository;
import com.foodfrenzy.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           CartRepository cartRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerCustomer(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException("An account with email " + dto.getEmail() + " already exists.");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setActive(true);

        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_CUSTOMER")));
        user.addRole(customerRole);

        User savedUser = userRepository.save(user);

        // Pre-create empty cart for user
        Cart cart = new Cart(savedUser);
        cartRepository.save(cart);

        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim());
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public User updateProfile(Long userId, UserProfileDto dto) {
        User user = getById(userId);
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.searchUsers(query.trim(), pageable);
    }

    @Override
    public User createUserByAdmin(String name, String email, String password, String phone, String address, String roleName) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("Email " + email + " is already in use.");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email.toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setAddress(address);
        user.setActive(true);

        String targetRole = (roleName != null && roleName.toUpperCase().contains("ADMIN")) ? "ROLE_ADMIN" : "ROLE_CUSTOMER";
        Role role = roleRepository.findByName(targetRole)
                .orElseGet(() -> roleRepository.save(new Role(targetRole)));
        user.addRole(role);

        User saved = userRepository.save(user);
        Cart cart = new Cart(saved);
        cartRepository.save(cart);

        return saved;
    }

    @Override
    public User updateUserByAdmin(Long id, String name, String phone, String address, boolean active, String roleName) {
        User user = getById(id);
        user.setName(name);
        user.setPhone(phone);
        user.setAddress(address);
        user.setActive(active);

        if (roleName != null && !roleName.trim().isEmpty()) {
            user.getRoles().clear();
            String targetRole = roleName.toUpperCase().contains("ADMIN") ? "ROLE_ADMIN" : "ROLE_CUSTOMER";
            Role role = roleRepository.findByName(targetRole)
                    .orElseGet(() -> roleRepository.save(new Role(targetRole)));
            user.addRole(role);
        }

        return userRepository.save(user);
    }

    @Override
    public void toggleUserStatus(Long id) {
        User user = getById(id);
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCustomers() {
        return userRepository.countCustomers();
    }
}
