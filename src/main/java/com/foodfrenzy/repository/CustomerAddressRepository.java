package com.foodfrenzy.repository;

import com.foodfrenzy.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    List<CustomerAddress> findByUserId(Long userId);
    Optional<CustomerAddress> findByUserIdAndIsDefaultTrue(Long userId);
}
