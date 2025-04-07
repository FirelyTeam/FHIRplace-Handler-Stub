package com.dgi.fhirplace.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FHIRHttpClient {
    private static final FhirContext fhirContext = FhirContext.forR4(); // Change to forR5() if needed
    private final String baseUrl;
    private final HttpClient httpClient;
    private String accessToken;

    // Constructor
    public FHIRHttpClient(String baseUrl, String accessTokenUrl, String fhirplaceClientId, String fhirplaceClientSecret, String scope, String audience) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();

        // Authenticate with the FHIR server
        this.accessToken = authenticate(accessTokenUrl, fhirplaceClientId, fhirplaceClientSecret, scope, audience);
    }

    // Authenticates with the FHIR server
    private String authenticate(String accessTokenUrl, String clientId, String clientSecret, String scope, String audience) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(accessTokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret + "&scope=" + scope + "&aud=" + audience))
                .build();
                
        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                // Parse response body as JSON. We want to get the access_token as a key
                String responseBody = response.body();
                // Parse response body as JSON to retrieve the access_token
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(responseBody);
                String accessToken = jsonNode.get("access_token").asText();
                // TODO: Cache this somehow
                return accessToken;
            } else {
                System.err.println("Failed to authenticate: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Failed to authenticate: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Fetches AuditEvent synchronously
    public Bundle fetchAuditEventsByClientId(String clientId) {
        String endpoint = this.baseUrl + "/AuditEvent?agent:identifier=" + clientId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Bearer " + this.accessToken)
                .header("Accept", "application/fhir+json")
                .header("Prefer", "handling=strict")
                .build();

        HttpResponse<String> fhirBundleResponse = null;
        try {
            // A FHIR search always returns a Bundle as the results of the search
            fhirBundleResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (fhirBundleResponse.statusCode() == 200) {
                System.out.println(fhirBundleResponse.body());
                return parseFhirResponse(fhirBundleResponse.body());
            } else {
                System.err.println("Failed to fetch AuditEvent: " + fhirBundleResponse.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch AuditEvent: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static Bundle parseFhirResponse(String fhirBundleResponseBody) {
        try {
            IParser parser = fhirContext.newJsonParser();
            // Parse the response body into a FHIR Bundle
            Bundle fhirBundle = parser.parseResource(null, fhirBundleResponseBody);
            return fhirBundle;
        } catch (Exception e) {
            System.err.println("Failed to parse AuditEvent: " + e.getMessage());
            throw e;
        }
    }
}
