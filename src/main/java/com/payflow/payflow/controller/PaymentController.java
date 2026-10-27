
package com.payflow.payflow.controller;

import com.payflow.payflow.model.PaymentOrder;
import com.payflow.payflow.repository.PaymentOrderRepository;
import com.payflow.payflow.service.PaymentService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentOrderRepository orderRepository;

    public PaymentController(
            PaymentService paymentService,
            PaymentOrderRepository orderRepository) {
        this.paymentService = paymentService;
        this.orderRepository = orderRepository;
    }
    
@GetMapping("/orders")
public java.util.List<PaymentOrder> getAllOrders() {
    return orderRepository.findAll();
}
@GetMapping("/orders/{orderId}")
public PaymentOrder getOrderById(@PathVariable String orderId) {

    return orderRepository.findById(orderId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Order not found"
            ));
}


    @PostMapping("/orders")
    public PaymentOrder createOrder(@RequestBody OrderRequest request) {

        if (request.getAmount() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Amount must be greater than zero"
            );
        }

        PaymentOrder order = new PaymentOrder(request.getAmount());

        return orderRepository.save(order);
    }

   
@PostMapping("/{orderId}/process")
public PaymentOrder processPayment(
        @PathVariable String orderId,
        @RequestParam(defaultValue = "false") boolean fail) {

    PaymentOrder order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Order not found"
            ));
            if ("PAID".equals(order.getStatus())) {
    throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "This order has already been paid"
    );
}
if ("FAILED".equals(order.getStatus())) {
    throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "This payment has failed and cannot be processed again"
    );
}

    if (fail) {
        order.setStatus("FAILED");
    } else {
        paymentService.processPayment(order);
    }

    return orderRepository.save(order);
}


    public static class OrderRequest {

        private double amount;

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }
}

