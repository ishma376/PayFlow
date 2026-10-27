package com.payflow.payflow.repository;

import com.payflow.payflow.model.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentOrderRepository
        extends JpaRepository<PaymentOrder, String> {
}