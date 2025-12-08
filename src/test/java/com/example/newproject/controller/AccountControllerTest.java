package com.example.newproject.controller;

import com.example.newproject.model.Account;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerTest {

    @LocalServerPort
    private int port;

    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        baseUrl = "http://localhost:" + port;
    }

    @Test
    void testCreateAccountWithValidData() throws Exception {
        Account validAccount = new Account();
        validAccount.setFirstName("John");
        validAccount.setLastName("Doe");
        validAccount.setPhoneNumber("(212) 456-7890");
        validAccount.setAddress("123 Main St");
        validAccount.setApartmentNumber("Apt 4B");

        String jsonBody = objectMapper.writeValueAsString(validAccount);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/accounts"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Account created = objectMapper.readValue(response.body(), Account.class);
        assertNotNull(created);
        assertEquals("John", created.getFirstName());
        assertEquals("Doe", created.getLastName());
        assertEquals("(212) 456-7890", created.getPhoneNumber());
        assertEquals("123 Main St", created.getAddress());
        assertEquals("Apt 4B", created.getApartmentNumber());
    }

    @Test
    void testCreateAccountWithInvalidPhoneNumber() throws Exception {
        Account invalidAccount = new Account();
        invalidAccount.setFirstName("Jane");
        invalidAccount.setLastName("Smith");
        invalidAccount.setPhoneNumber("invalid");
        invalidAccount.setAddress("456 Oak Ave");

        String jsonBody = objectMapper.writeValueAsString(invalidAccount);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/accounts"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid phone number format"));
    }

    @Test
    void testCreateAccountWithNullPhoneNumber() throws Exception {
        Account nullPhoneAccount = new Account();
        nullPhoneAccount.setFirstName("Test");
        nullPhoneAccount.setLastName("User");
        nullPhoneAccount.setPhoneNumber(null);
        nullPhoneAccount.setAddress("789 Pine Rd");

        String jsonBody = objectMapper.writeValueAsString(nullPhoneAccount);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/accounts"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Phone number is required"));
    }

    @Test
    void testCreateAccountWithoutApartmentNumber() throws Exception {
        Account accountWithoutApt = new Account();
        accountWithoutApt.setFirstName("Bob");
        accountWithoutApt.setLastName("Johnson");
        accountWithoutApt.setPhoneNumber("(650) 555-1234");
        accountWithoutApt.setAddress("789 Elm St");

        String jsonBody = objectMapper.writeValueAsString(accountWithoutApt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/accounts"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Account created = objectMapper.readValue(response.body(), Account.class);
        assertNotNull(created);
        assertEquals("Bob", created.getFirstName());
        assertEquals("Johnson", created.getLastName());
        assertEquals("(650) 555-1234", created.getPhoneNumber());
        assertEquals("789 Elm St", created.getAddress());
        assertNull(created.getApartmentNumber());
    }
}
