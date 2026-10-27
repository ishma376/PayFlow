
package com.payflow.payflow.service;

import com.payflow.payflow.model.PaymentOrder;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public String processPayment(PaymentOrder order) {

        if (order == null) {
            return "Order not found";
        }

        if (order.getAmount() <= 0) {
            order.setStatus("FAILED");
            return "FAILED";
        }

        order.setStatus("PAID");
        order.setPaidAt(java.time.LocalDateTime.now());

        return order.getStatus();
    }
   
}
