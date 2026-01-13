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

/**
 * ApiService provides HTTP client functionality for communicating with the
 * backend REST API.
 * 
 * This service handles all HTTP operations (GET, POST, PUT, DELETE) and
 * automatically
 * serializes/deserializes JSON payloads using Jackson ObjectMapper.
 * Supports Java 8 time types through JavaTimeModule.
 * 
 * All methods use HTTP/2 with a 10-second connection timeout.
 * The backend URL is configurable via the BACKEND_URL constant (default:
 * http://localhost:8080).
 */
public class ApiService {

    // specific to your backend configuration
    private static final String BACKEND_URL = "http://localhost:8080";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructs an ApiService with an HTTP client and JSON object mapper.
     * Configures the HTTP client for HTTP/2 with a 10-second connection timeout.
     * Registers JavaTimeModule for proper handling of Java 8 date/time types.
     */
    public ApiService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Fetches data from the backend API using a GET request.
     * Returns the raw response body as a string.
     * 
     * @param endpoint The API endpoint path (e.g., "/api/users"). Will be appended
     *                 to BACKEND_URL.
     * @return The response body as a string
     * @throws IOException          If the HTTP status is not 2xx or if an I/O error
     *                              occurs
     * @throws InterruptedException If the HTTP request is interrupted
     */
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

    /**
     * Sends a GET request to the backend API and parses the response as JSON.
     * 
     * @param endpoint The API endpoint path (e.g., "/api/users/1"). Will be
     *                 appended to BACKEND_URL.
     * @return A JsonNode representing the parsed JSON response
     * @throws IOException          If the HTTP status is not 2xx, response is
     *                              empty, or JSON parsing fails
     * @throws InterruptedException If the HTTP request is interrupted
     */
    public JsonNode getJson(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BACKEND_URL + endpoint))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseJsonResponse(endpoint, response);
    }

    /**
     * Sends a POST request to the backend API with a JSON payload.
     * 
     * Automatically serializes the payload object to JSON using Jackson.
     * Sets appropriate Content-Type and Accept headers for JSON.
     * 
     * @param endpoint The API endpoint path (e.g., "/api/users"). Will be appended
     *                 to BACKEND_URL.
     * @param payload  The object to serialize and send as the request body. Can be
     *                 a Map, POJO, etc.
     * @return A JsonNode representing the parsed JSON response
     * @throws IOException          If serialization fails, HTTP status is not 2xx,
     *                              or response parsing fails
     * @throws InterruptedException If the HTTP request is interrupted
     */
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

    /**
     * Sends a DELETE request to the backend API.
     * 
     * @param endpoint The API endpoint path (e.g., "/api/users/1"). Will be
     *                 appended to BACKEND_URL.
     * @return A JsonNode representing the parsed JSON response (typically a
     *         success/error message)
     * @throws IOException          If the HTTP status is not 2xx or response
     *                              parsing fails
     * @throws InterruptedException If the HTTP request is interrupted
     */
    public JsonNode deleteJson(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BACKEND_URL + endpoint))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseJsonResponse(endpoint, response);
    }

    /**
     * Parses an HTTP response as JSON with error checking.
     * 
     * Validates that:
     * - HTTP status code is 2xx (success)
     * - Response body is not null or blank
     * - Response body is valid JSON
     * 
     * @param endpoint The endpoint path (used for error messages)
     * @param response The HTTP response to parse
     * @return A JsonNode representing the parsed JSON response
     * @throws IOException If the status is not 2xx, body is empty, or JSON parsing
     *                     fails
     */
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

    /**
     * Sends a PUT request to the backend API with a JSON payload.
     * 
     * Automatically serializes the payload object to JSON using Jackson.
     * Sets appropriate Content-Type and Accept headers for JSON.
     * Typically used for updating existing resources.
     * 
     * @param endpoint The API endpoint path (e.g., "/api/users/1"). Will be
     *                 appended to BACKEND_URL.
     * @param payload  The object to serialize and send as the request body. Can be
     *                 a Map, POJO, etc.
     * @return A JsonNode representing the parsed JSON response
     * @throws IOException          If serialization fails, HTTP status is not 2xx,
     *                              or response parsing fails
     * @throws InterruptedException If the HTTP request is interrupted
     */
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