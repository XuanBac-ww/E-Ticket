package com.example.backend.service.payment.expiration;


public interface IPendingOrderExpirationService {
    void expireOverduePendingOrders();
}
