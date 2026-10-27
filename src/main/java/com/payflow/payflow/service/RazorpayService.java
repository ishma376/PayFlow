
package com.payflow.payflow.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {

    @Value("${razorpay.key.id:}")
    private String keyId;

    @Value("${razorpay.key.secret:}")
    private String keySecret;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String getKeyId() {
        return keyId;
    }

    public boolean isConfigured() {
        return keyId != null && !keyId.isBlank()
                && keySecret != null && !keySecret.isBlank();
    }

    public String createOrder(long amountInPaise, String receipt)
            throws Exception {

        if (!isConfigured()) {
            throw new IllegalStateException(
                    "Razorpay credentials are not configured");
        }

        if (amountInPaise <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero");
        }

        String safeReceipt = receipt.replaceAll("[^a-zA-Z0-9_-]", "_");

        String json = """
                {
                  "amount": %d,
                  "currency": "INR",
                  "receipt": "%s"
                }
                """.formatted(amountInPaise, safeReceipt);

        String credentials = keyId + ":" + keySecret;
        String basicAuth = Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.razorpay.com/v1/orders"))
                .header("Authorization", "Basic " + basicAuth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {
            throw new IllegalStateException(
                    "Razorpay order creation failed with HTTP "
                            + response.statusCode());
        }

        try {
    com.fasterxml.jackson.databind.JsonNode result =
            new com.fasterxml.jackson.databind.ObjectMapper()
                    .readTree(response.body());

    String providerOrderId = result.path("id").asText();

    if (providerOrderId.isBlank()) {
        throw new IllegalStateException(
                "Razorpay response did not contain an order ID");
    }

    return providerOrderId;

} catch (java.io.IOException e) {
    throw new IllegalStateException(
            "Could not read Razorpay order response", e);
}
    }
}
