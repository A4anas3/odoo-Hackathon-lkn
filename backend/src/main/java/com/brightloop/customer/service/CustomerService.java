package com.brightloop.customer.service;

import com.brightloop.common.dto.PageResponse;
import com.brightloop.customer.dto.CustomerRequest;
import com.brightloop.customer.dto.CustomerResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {

    PageResponse<CustomerResponse> listCustomers(String search, String status, String customerType, Pageable pageable);

    CustomerResponse createCustomer(CustomerRequest request);

    CustomerResponse getCustomerById(UUID id);

    CustomerResponse updateCustomer(UUID id, CustomerRequest request);

    void deleteCustomer(UUID id);
}
