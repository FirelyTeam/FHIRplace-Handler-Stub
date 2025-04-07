package com.dgi.fhirplace.handler;

public class FHIRServerConfig {
    String baseUrl;
    String accessTokenUrl;
    String fhirplaceClientId;
    String fhirplaceClientSecret;
    String scope;
    String audience;

    public FHIRServerConfig() {
        // Default constructor
    }
    public boolean validate() {
        // Validate the FHIR server configuration
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("Base URL is required");
        }
        if (accessTokenUrl == null || accessTokenUrl.isEmpty()) {
            throw new IllegalArgumentException("Access token URL is required");
        }
        if (fhirplaceClientId == null || fhirplaceClientId.isEmpty()) {
            throw new IllegalArgumentException("FHIRplace client ID is required");
        }
        if (fhirplaceClientSecret == null || fhirplaceClientSecret.isEmpty()) {
            throw new IllegalArgumentException("FHIRplace client secret is required");
        }
        if (scope == null || scope.isEmpty()) {
            throw new IllegalArgumentException("Scope is required");
        }
        if (audience == null || audience.isEmpty()) {
            throw new IllegalArgumentException("Audience is required");
        }
        return true;
    }
}
