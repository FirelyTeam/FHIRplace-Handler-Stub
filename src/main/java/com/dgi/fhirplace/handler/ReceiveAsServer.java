package com.dgi.fhirplace.handler;

import com.dgi.fhirplace.parser.Transmission;

/**
 * This class encapsulates all the server (old payer) receiving from 
 * the client (new payer) and returning the response to them.
 * 
 * You will need to update the response content from the
 * appropriate response elements so that the ProcessTestRequest class
 * can access them and send them to the FHIRplace server
 */
public class ReceiveAsServer {
  
  Logger log;

  String testRequestID;
  String partner;
  String receiveDataType;
  String fhirServer;
  String authServer;
  String patientResourceName;
  String coverageResourceName;
  
  boolean receiveSuccess = true;
  boolean responseSuccess = true;

  String fhirID = "ABCDEFGHIJKLMNOP";
  String accessToken = "123456789";
  String clientID = "Some Client ID";
  String headersAndMetaData = "Sent headers and metadata";

  FHIRAuditEventService auditEventService;

  /**
   * This is the constructor that includes the necessary elements for receiving
   * 
   * @param testRequestID - the unique identifier for this test request
   * @param receiveDataType - the type of data being received
   * @param partner - the name of the participant that is sending the request
   * @param trans - the transmission object parsed from the test request's Transmission element
   * @param log -  a reference to the Logger object for logging as needed
   */
  public ReceiveAsServer(FHIRAuditEventService auditEventService, String testRequestID, String partner, String receiveDataType,
                         Transmission trans, Logger log) {
    this.testRequestID = testRequestID;
    this.partner = partner;
    this.receiveDataType = receiveDataType;

    this.fhirServer = trans.getFhirServer();
    this.authServer = trans.getAuthorizationServer();
    this.patientResourceName = trans.getPatientResourceName();
    this.coverageResourceName = trans.getCoverageResourceName();

    this.auditEventService = auditEventService;

    this.log = log;  
  }
  public void receive() {
    // When receiving, trigger off the receiveDataType value
    log.write("The received data type is: " + receiveDataType);
    log.write("Receiving request from " + partner);

    String fhirOperationName = "";
    switch (receiveDataType) {
      case FHIRplaceConstants.MEMBER_MATCH_TYPE:
        fhirOperationName = "/Patient/$member-match";
        break;

      case FHIRplaceConstants.FHIR_ID_TYPE:
        fhirOperationName = "/Patient/$member-match";
        break;

      case FHIRplaceConstants.PATIENT_REQUEST_TYPE:
        fhirOperationName = "/Patient/$everything";
        break;
    }

    // The client ID is the partner ID
    String clientId = this.partner;

    ReceivingServerResponse response = this.auditEventService.prepareReceivingResponse(this.fhirServer, clientId, fhirOperationName);
    if (response == null) {
      log.write("Unable to build a complete response from the FHIR server");
      this.receiveSuccess = false;
      return;
    }
    // Prepare response to FHIRplace server
    this.fhirID = response.fhirID;
    this.accessToken = response.accessToken;
    this.clientID = response.clientId;
    this.headersAndMetaData = response.headersAndMetaData;
    this.receiveSuccess = true;
  }

  public void returnResponse(String dataType) {
    // Request summary
    log.write("Test Request ID: " + this.testRequestID);
    log.write("Partner: " + this.partner);
    log.write("Receive Data Type: " + this.receiveDataType);
    log.write("FHIR Server: " + this.fhirServer);
    // Based on the dataType, you should return the correct response:
    // ClientID
    // AccessToken
    // FHIR ID (patient's resource ID)
    // Headers and Metadata
    log.write("FHIR ID: " + this.fhirID);
    log.write("Access Token: " + this.accessToken);
    log.write("Client ID: " + this.clientID);
    log.write("Headers and Metadata: " + this.headersAndMetaData);
    // Response
    log.write("Response success: " + this.responseSuccess);
    log.write("Receive success: " + this.receiveSuccess);
  }
  public boolean isReceiveSuccess() {
    return this.receiveSuccess;
  }
  public boolean isResponseSuccess() {
    return this.responseSuccess;
  }

  public String getFhirID() {
    return this.fhirID;
  }
  public String getAccessToken() {
    return this.accessToken;
  }
  public String getClientID() {
    return this.clientID;
  }
  public String getHeadersAndMetaData() {
    return this.headersAndMetaData;
  }
  
}
