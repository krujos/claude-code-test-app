package com.example.newproject.controller;

import com.example.newproject.model.Account;
import com.example.newproject.repository.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.security.user.name=test",
    "spring.security.user.password=test"
})
class AdminControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AccountRepository accountRepository;

    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        baseUrl = "http://localhost:" + port;
    }

    private String getBasicAuthHeader() {
        String auth = "admin:password";
        return "Basic " + java.util.Base64.getEncoder().encodeToString(auth.getBytes());
    }

    @AfterEach
    void cleanup() {
        accountRepository.deleteAll();
    }

    @Test
    void testGetAllAccountsReturnsListOfAccounts() throws Exception {
        // Create test data
        Account account1 = new Account("John", "Doe", "(212) 456-7890", "123 Main St");
        Account account2 = new Account("Jane", "Smith", "(415) 789-0123", "456 Oak Ave");
        account2.setApartmentNumber("Unit 5B");
        accountRepository.save(account1);
        accountRepository.save(account2);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/admin/accounts"))
            .header("Authorization", getBasicAuthHeader())
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Account[] accounts = objectMapper.readValue(response.body(), Account[].class);
        assertEquals(2, accounts.length);
        assertEquals("John", accounts[0].getFirstName());
        assertEquals("Jane", accounts[1].getFirstName());
        assertEquals("Unit 5B", accounts[1].getApartmentNumber());
    }

    @Test
    void testGetAllAccountsReturnsEmptyList() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/admin/accounts"))
            .header("Authorization", getBasicAuthHeader())
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Account[] accounts = objectMapper.readValue(response.body(), Account[].class);
        assertEquals(0, accounts.length);
    }

    @Test
    void testGetAccountByIdReturnsAccount() throws Exception {
        Account account = new Account("John", "Doe", "(212) 456-7890", "123 Main St");
        account.setApartmentNumber("Apt 4B");
        Account saved = accountRepository.save(account);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/admin/accounts/" + saved.getId()))
            .header("Authorization", getBasicAuthHeader())
            .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Account retrieved = objectMapper.readValue(response.body(), Account.class);
        assertEquals("John", retrieved.getFirstName());
        assertEquals("Doe", retrieved.getLastName());
        assertEquals("(212) 456-7890", retrieved.getPhoneNumber());
        assertEquals("123 Main St", retrieved.getAddress());
        assertEquals("Apt 4B", retrieved.getApartmentNumber());
    }

    @Test
    void testUpdateAccountWithValidData() throws Exception {
        Account existing = new Account("John", "Doe", "(212) 456-7890", "123 Main St");
        Account saved = accountRepository.save(existing);

        Account updateData = new Account();
        updateData.setFirstName("John");
        updateData.setLastName("Doe");
        updateData.setPhoneNumber("(415) 987-6543");
        updateData.setAddress("456 New St");
        updateData.setApartmentNumber("Suite 100");

        String jsonBody = objectMapper.writeValueAsString(updateData);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/admin/accounts/" + saved.getId()))
            .header("Content-Type", "application/json")
            .header("Authorization", getBasicAuthHeader())
            .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Account updated = objectMapper.readValue(response.body(), Account.class);
        assertEquals("(415) 987-6543", updated.getPhoneNumber());
        assertEquals("456 New St", updated.getAddress());
        assertEquals("Suite 100", updated.getApartmentNumber());
    }

    @Test
    void testUpdateAccountWithInvalidPhoneNumber() throws Exception {
        Account invalid = new Account();
        invalid.setFirstName("John");
        invalid.setLastName("Doe");
        invalid.setPhoneNumber("invalid");
        invalid.setAddress("123 Main St");

        String jsonBody = objectMapper.writeValueAsString(invalid);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/admin/accounts/1"))
                .header("Content-Type", "application/json")
            .header("Authorization", getBasicAuthHeader())
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Phone number must be in format"));
    }

    @Test
    void testDeleteAccountReturnsNoContent() throws Exception {
        Account account = new Account("John", "Doe", "(212) 456-7890", "123 Main St");
        Account saved = accountRepository.save(account);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/admin/accounts/" + saved.getId()))
            .header("Authorization", getBasicAuthHeader())
            .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode());
        assertFalse(accountRepository.findById(saved.getId()).isPresent());
    }
}
