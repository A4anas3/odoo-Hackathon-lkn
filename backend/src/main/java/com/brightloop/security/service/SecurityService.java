package com.brightloop.security.service;

import com.brightloop.customer.entity.Customer;
import com.brightloop.customer.repo.CustomerRepository;
import com.brightloop.security.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service("securityService")
@RequiredArgsConstructor
public class SecurityService {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public boolean isCurrentCustomer(UUID customerId, UserPrincipal principal) {
        if (principal == null || customerId == null) {
            return false;
        }

        // Admins and Staff bypass customer isolation
        if ("ADMIN".equals(principal.getRoleName()) || "STAFF".equals(principal.getRoleName())) {
            return true;
        }

        Optional<Customer> customerOpt = customerRepository.findByUserId(principal.getId());
        return customerOpt.isPresent() && customerOpt.get().getId().equals(customerId);
    }
}
