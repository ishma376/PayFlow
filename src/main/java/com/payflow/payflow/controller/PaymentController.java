
package com.payflow.payflow.controller;

import com.payflow.payflow.model.PaymentOrder;
import com.payflow.payflow.repository.PaymentOrderRepository;
import com.payflow.payflow.service.PaymentService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.payflow.payflow.service.RazorpayService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentOrderRepository orderRepository;
    private final RazorpayService razorpayService;

  public PaymentController(
        PaymentService paymentService,
        PaymentOrderRepository orderRepository,
        RazorpayService razorpayService) {

    this.paymentService = paymentService;
    this.orderRepository = orderRepository;
    this.razorpayService = razorpayService;
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

    @PostMapping("/{orderId}/razorpay-order")
public java.util.Map<String, Object> createRazorpayOrder(
        @PathVariable String orderId) {

    PaymentOrder order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Order not found"));

    if (!"CREATED".equals(order.getStatus())) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Only CREATED orders can be sent to Razorpay");
    }

    if (order.getProviderOrderId() != null
            && !order.getProviderOrderId().isBlank()) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A Razorpay order already exists for this order");
    }

    try {
        long amountInPaise = Math.round(order.getAmount() * 100);

        String providerOrderId = razorpayService.createOrder(
                amountInPaise, order.getOrderId());

        order.setProviderOrderId(providerOrderId);
        orderRepository.save(order);

        return java.util.Map.of(
                "orderId", order.getOrderId(),
                "providerOrderId", providerOrderId,
                "keyId", razorpayService.getKeyId(),
                "amount", amountInPaise,
                "currency", order.getCurrency());

    } catch (IllegalStateException e) {
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Razorpay is not configured or the order request failed");
    } catch (Exception e) {
        throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Could not create a Razorpay order");
    }
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
