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
  
  /**
   * This is the constructor that includes the necessary elements for receiving
   * 
   * @param testRequestID - the unique identifier for this test request
   * @param receiveDataType - the type of data being received
   * @param partner - the name of the participant that is sending the request
   * @param trans - the transmission object parsed from the test request's Transmission element
   * @param log -  a reference to the Logger object for logging as needed
   */
  public ReceiveAsServer(String testRequestID, String partner, String receiveDataType,
                         Transmission trans, Logger log) {
    this.testRequestID = testRequestID;
    this.partner = partner;
    this.receiveDataType = receiveDataType;

    this.fhirServer = trans.getFhirServer();
    this.authServer = trans.getAuthorizationServer();
    this.patientResourceName = trans.getPatientResourceName();
    this.coverageResourceName = trans.getCoverageResourceName();
    
    this.log = log;  
  }
  
  public void receive() {
    // When receiving, trigger off the receiveDataType value
    log.write("The received data type is: " + receiveDataType);
  }
  
  public void returnResponse(String dataType) {
    log.write("Returning " + dataType + " response...");
    
    // Based on the dataType, you should return the correct response:
    
    // ClientID type
    // AccessToken Type
    // FHIR ID Type
    // Headers and Metadata Type
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
