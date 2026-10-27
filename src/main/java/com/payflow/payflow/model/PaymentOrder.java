
package com.payflow.payflow.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "payment_orders")
public class PaymentOrder {

    @Id
    @Column(length = 36)
    private String orderId;

    private double amount;

    private String currency;

    private String status;
    private java.time.LocalDateTime paidAt;

    public PaymentOrder() {
    }

    public PaymentOrder(double amount) {
        this.orderId = UUID.randomUUID().toString();
        this.amount = amount;
        this.currency = "INR";
        this.status = "CREATED";
    }

    public String getOrderId() {
        return orderId;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public java.time.LocalDateTime getPaidAt() {
    return paidAt;
}

public void setPaidAt(java.time.LocalDateTime paidAt) {
    this.paidAt = paidAt;
}
}
