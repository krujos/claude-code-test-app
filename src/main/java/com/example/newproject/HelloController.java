package com.example.newproject;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String hello() {
        return """
            <html>
            <head><title>Account Management System</title></head>
            <body>
                <h1>Account Management System</h1>
                <h2>Public Endpoints:</h2>
                <ul>
                    <li>POST /api/accounts - Create new account</li>
                    <li>GET /api/accounts/stream - Stream all accounts (SSE)</li>
                </ul>
                <h2>Admin Endpoints (requires authentication):</h2>
                <ul>
                    <li>GET /api/admin/accounts - Get all accounts</li>
                    <li>GET /api/admin/accounts/{id} - Get account by ID</li>
                    <li>PUT /api/admin/accounts/{id} - Update account</li>
                    <li>DELETE /api/admin/accounts/{id} - Delete account</li>
                </ul>
                <p>Admin credentials: username=admin, password=password</p>
                <p>H2 Console: <a href="/h2-console">/h2-console</a></p>
            </body>
            </html>
            """;
    }
}
