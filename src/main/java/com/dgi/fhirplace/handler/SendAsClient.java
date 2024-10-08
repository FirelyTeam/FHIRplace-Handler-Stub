package com.dgi.fhirplace.handler;

import com.dgi.fhirplace.parser.Transmission;

/**
 * This class encapsulates all the client (new payer) sending to 
 * the server (old payer) and receiving the response.
 * 
 * You will need to parse the response content and update the
 * appropriate elements so that the ProcessTestRequest class
 * can access them and send them to the FHIRplace server
 */
public class SendAsClient {

  Logger log;

  String testRequestID;
  String partner;
  String sendDataType;
  String fhirServer;
  String authServer;
  String patientResourceName;
  String coverageResourceName;
  
  boolean sendSuccess = true;
  boolean responseSuccess = true;

  String fhirID = "ABCDEFGHIJKLMNOP";
  String accessToken = "123456789";
  String clientID = "Some Client ID";
  String headersAndMetaData = "Sent headers and metadata";
  
  /**
   * This is the constructor that includes the necessary elements for sending
   * 
   * @param testRequestID - the unique identifier for this test request
   * @param sendDataType - the type of data being sent
   * @param partner - the name of the participant that is receiving your request
   * @param trans - the transmission object parsed from the test request's Transmission element
   * @param log - a reference to the Logger object for logging as needed
   */
  public SendAsClient(String testRequestID, String partner, String sendDataType,  
                      Transmission trans, Logger log) {
    this.testRequestID = testRequestID;
    this.partner = partner;
    this.sendDataType = sendDataType;

    this.fhirServer = trans.getFhirServer();
    this.authServer = trans.getAuthorizationServer();
    this.patientResourceName = trans.getPatientResourceName();
    this.coverageResourceName = trans.getCoverageResourceName();
    
    this.log = log;
  }
  
  /**
   * Use this method to send the message and wait for a response.
   * After the response if received, if it was not successful,
   * change the 'success' value to false.
   * 
   * If it was successful, update the FHIR_ID (Member ID)
   * and the Access Token where appropriate
   */
  public void send() {
    log.write("Sending request to " + partner);
    
    // Sit in a loop and wait for the response
    
    // Update the results
  }
  
  /**
   * This method receives the response if needed.
   * You will either add code to receive the response or extract
   * the result of the response from your initial message
   * 
   * @param dataType - the data type that is part of the response
   */
  public void receiveResponseForDataType(String dataType) {
    log.write("Receiving " + dataType + " response...");
    
    // Based on the dataType, you should receive the correct response
    // ClientID type
    // AccessToken Type
    // FHIR ID Type
    // Headers and Metadata Type
  }
  
  public boolean isSendSuccess() {
    return this.sendSuccess;
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
