package com.example.frontend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiService {

    // specific to your backend configuration
    private static final String BACKEND_URL = "http://localhost:8080";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public String fetchData(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BACKEND_URL + endpoint))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw new IOException("HTTP " + status + " from " + endpoint + ": " + response.body());
        }

        // In a real app, you would use objectMapper.readValue(response.body(),
        // MyClass.class) here
        return response.body();
    }

    public JsonNode getJson(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BACKEND_URL + endpoint))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseJsonResponse(endpoint, response);
    }

    public JsonNode postJson(String endpoint, Object payload) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(URI.create(BACKEND_URL + endpoint))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseJsonResponse(endpoint, response);
    }

    public JsonNode deleteJson(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BACKEND_URL + endpoint))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseJsonResponse(endpoint, response);
    }

    private JsonNode parseJsonResponse(String endpoint, HttpResponse<String> response) throws IOException {
        int status = response.statusCode();
        String body = response.body();

        // Accept any 2xx (200 OK, 201 CREATED, etc.)
        if (status < 200 || status >= 300) {
            throw new IOException("HTTP " + status + " from " + endpoint + ": " + body);
        }

        if (body == null || body.isBlank()) {
            throw new IOException("Empty response body from " + endpoint + " (HTTP " + status + ")");
        }

        return objectMapper.readTree(body);
    }

    public JsonNode putJson(String endpoint, Object payload) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .uri(URI.create(BACKEND_URL + endpoint))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseJsonResponse(endpoint, response);
    }
}