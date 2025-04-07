package com.dgi.fhirplace.handler;

import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventEntityDetailComponent;

import com.dgi.fhirplace.util.FHIRHttpClient;

import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;

public class FHIRAuditEventService {
    Logger log;

    private static final String AUDIT_EVENT_PATIENT_EXTENSION_CANONICAL_URL = "http://hl7.org/fhir/5.0/StructureDefinition/extension-AuditEvent.patient";
    private static final String AUDIT_ENTITY_TYPE_CANONICAL_URL = "http://terminology.hl7.org/CodeSystem/audit-entity-type";

    private FHIRHttpClient fhirClient;

    public FHIRAuditEventService(FHIRServerConfig config) {
        // Create the HTTP client
        this.fhirClient = new FHIRHttpClient(config.baseUrl, config.accessTokenUrl, config.fhirplaceClientId, config.fhirplaceClientSecret, config.scope, config.audience);
    }
    
    /**
     * Queries the server's AuditEvents and prepares the response for the receiving server.
     * 
     * @param baseUrl          - the base URL of the FHIR server
     * @param clientId         - the client ID of the FHIR server
     * @param fhirOperationName - the FHIR operation name to filter by
     * @return a ReceivingServerResponse object containing the response data
     */
    public ReceivingServerResponse prepareReceivingResponse(String baseUrl, String clientId, String fhirOperationName) {
        ReceivingServerResponse response = new ReceivingServerResponse();
        // The returned client ID is just the one given to us
        response.clientId = clientId;

        // Now read audit events from the FHIR server
        // First, get all audit events for the client ID
        Bundle bundle = this.fhirClient.fetchAuditEventsByClientId(clientId);
        if (bundle == null || bundle.getEntry().isEmpty()) {
            System.out.println("No audit events found for client ID: " + clientId);
            return null;
        }

        // Second filter the audit events by the $member-match operation, where the AuditEvent.entity.detail = "/Patient/$member-match"
        // A FHIR server may be able to search for this directly, but we are doing it here manually for broader compatibility.
        AuditEvent matchedAuditEvent = filterAuditEventsByOperation(bundle, fhirOperationName);
        if (matchedAuditEvent == null) {
            System.out.println("No matching audit event found for operation: " + fhirOperationName);
            return null;
        }

        var fhirContext = FhirContext.forR4();
        var jsonParser = fhirContext.newJsonParser();
        String json = jsonParser.setPrettyPrint(true).encodeResourceToString(matchedAuditEvent);
        System.out.println("Matched AuditEvent JSON: " + json);

        // Third, get the FHIR ID from the audit event
        String fhirID = null;
        for (org.hl7.fhir.r4.model.Extension extension : matchedAuditEvent.getExtension()) {
        if (extension.getUrl().equals(AUDIT_EVENT_PATIENT_EXTENSION_CANONICAL_URL)) {
            // The FHIR ID is the value of the extension
            fhirID = extension.getValue().toString();
            break;
        }
        }
        if (fhirID == null) {
            System.out.println("No FHIR ID found in audit event");
            // return null;
        }
        // Set the return FHIR ID
        response.fhirID = fhirID;

        // Fourth, get the access token from the audit event
        // TODO: this is not supported for security reasons.
        response.accessToken = null;

        return response;
    }

    /**
     * This method filters the audit events by the specified operation name.
     * 
     * @param bundle           - the bundle containing the audit events
     * @param fhirOperationName - the FHIR operation name to filter by
     * @return the matching AuditEvent or null if not found
     */
    private AuditEvent filterAuditEventsByOperation(Bundle bundle, String fhirOperationName) {
        // For each resource in the bundle
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            AuditEvent auditEvent = (AuditEvent) entry.getResource();
            // Iterate over audit events in bundle
            for (AuditEvent.AuditEventEntityComponent entity : auditEvent.getEntity()) {
                var entityType = entity.getType();
                // See https://terminology.hl7.org/CodeSystem-audit-entity-type.html
                if (entityType.getSystem().equals(AUDIT_ENTITY_TYPE_CANONICAL_URL) && entityType.getCode().equals("2")) {
                    for (AuditEventEntityDetailComponent detail : entity.getDetail()) {
                        // Match the operation name
                        var auditEventOperation = detail.getValueStringType().getValue();
                        if (auditEventOperation.equals(fhirOperationName)) {
                            // Return this matching audit event
                            return auditEvent;
                        }
                    }
                }
            }
        }
        return null;
    }
}
