package com.example.frontend;

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

    public ApiService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String fetchData(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BACKEND_URL + endpoint))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // In a real app, you would use objectMapper.readValue(response.body(),
        // MyClass.class) here
        return response.body();
    }
}