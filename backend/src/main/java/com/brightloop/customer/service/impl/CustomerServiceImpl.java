package com.brightloop.customer.service.impl;

import com.brightloop.common.dto.PageResponse;
import com.brightloop.common.exception.BadRequestException;
import com.brightloop.common.exception.ResourceNotFoundException;
import com.brightloop.common.service.SequenceGeneratorService;
import com.brightloop.customer.dto.CustomerRequest;
import com.brightloop.customer.dto.CustomerResponse;
import com.brightloop.customer.entity.Customer;
import com.brightloop.customer.repo.CustomerRepository;
import com.brightloop.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> listCustomers(String search, String status, String customerType, Pageable pageable) {
        Page<Customer> page = customerRepository.searchCustomers(search, status, customerType, pageable);
        return PageResponse.from(page.map(this::mapToResponse));
    }

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Customer with email " + request.getEmail() + " already exists");
        }

        String code = sequenceGeneratorService.getNextSequence("CUSTOMER");

        Customer customer = Customer.builder()
                .customerCode(code)
                .customerType(request.getCustomerType() != null ? request.getCustomerType() : "BUSINESS")
                .companyName(request.getCompanyName())
                .contactName(request.getContactName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .billingAddress(request.getBillingAddress())
                .shippingAddress(request.getShippingAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry() != null ? request.getCountry() : "India")
                .postalCode(request.getPostalCode())
                .gstin(request.getGstin())
                .taxRegistrationType(request.getTaxRegistrationType())
                .status("ACTIVE")
                .notes(request.getNotes())
                .build();

        return mapToResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return mapToResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(UUID id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        customer.setCustomerType(request.getCustomerType());
        customer.setCompanyName(request.getCompanyName());
        customer.setContactName(request.getContactName());
        customer.setPhone(request.getPhone());
        customer.setBillingAddress(request.getBillingAddress());
        customer.setShippingAddress(request.getShippingAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setCountry(request.getCountry());
        customer.setPostalCode(request.getPostalCode());
        customer.setGstin(request.getGstin());
        customer.setTaxRegistrationType(request.getTaxRegistrationType());
        if (request.getStatus() != null) {
            customer.setStatus(request.getStatus());
        }
        customer.setNotes(request.getNotes());

        return mapToResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public void deleteCustomer(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        customer.setDeletedAt(Instant.now());
        customer.setStatus("ARCHIVED");
        customerRepository.save(customer);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .userId(customer.getUser() != null ? customer.getUser().getId() : null)
                .customerCode(customer.getCustomerCode())
                .customerType(customer.getCustomerType())
                .companyName(customer.getCompanyName())
                .contactName(customer.getContactName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .billingAddress(customer.getBillingAddress())
                .shippingAddress(customer.getShippingAddress())
                .city(customer.getCity())
                .state(customer.getState())
                .country(customer.getCountry())
                .postalCode(customer.getPostalCode())
                .gstin(customer.getGstin())
                .taxRegistrationType(customer.getTaxRegistrationType())
                .status(customer.getStatus())
                .notes(customer.getNotes())
                .activeSubscriptionsCount(0)
                .totalOutstanding(BigDecimal.ZERO)
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
