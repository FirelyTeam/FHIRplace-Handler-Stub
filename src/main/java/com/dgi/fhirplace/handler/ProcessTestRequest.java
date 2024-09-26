package com.dgi.fhirplace.handler;

import com.dgi.fhirplace.parser.Description;
import com.dgi.fhirplace.parser.FHIRplaceXML;
import com.dgi.fhirplace.parser.Instructions;
import com.dgi.fhirplace.parser.ParseTestRequest;
import com.dgi.fhirplace.parser.Participant;
import com.dgi.fhirplace.parser.Transmission;
import com.dgi.fhirplace.util.FHIRplaceUtil;
import com.dgi.fhirplace.util.FileUtility;

import java.io.File;


/**
 * Invoked once for each incoming test request as a thread:
 *   - parses the test request
 *   - sends the ACK/NAK messages to the FHIRplace server
 *   - sets up to run the test 
 *   - parses the results
 *   - uploads the status and any other required data to the FHIRplace server
 */
public class ProcessTestRequest extends Thread {

  Logger log = new Logger(ProcessTestRequest.class);

  FHIRplaceHandler handler = null;
  LocalParameters params = null;

  String requestFileName = null;

  FileUtility fileUtility = null;

  FHIRplaceXML xml = null;
  Participant parts[] = null;
  Transmission trans = null;
  Description desc = null;
  Instructions instruct = null;

  String partner = null;
  
  // This setting is used for testing that sent/receive upload
  // mismatches are detected properly 

  // Note: This is not used for the "Happy Path" tests.
  boolean generateMismatchErrors = false;
  
  public ProcessTestRequest(FHIRplaceHandler handler, LocalParameters params, String requestFileName) {
    this.handler = handler;
    
    this.params = params;
    this.requestFileName = requestFileName;

    this.fileUtility = new FileUtility();
  }
  
  /**
   * Parses the FHIRplace test request into separate objects (i.e., Participant,
   * Transmission, Description and Instruction
   * @return
   * @throws Exception 
   */
  private boolean parseTestRequest() throws Exception {

    File path = new File(this.requestFileName);
    
    // Parse the XML file
    this.xml = new FHIRplaceXML();
    new ParseTestRequest( path, xml, params );
   
    // Extract the objects from the FHIRplaceXML container
    desc = xml.getDescription();
    parts = xml.getParticipant();
    trans = xml.getTransmission();
    instruct = xml.getInstructions();   

    return true;
  }
  
  @Override
  public void run() {
    // Parse the test request
    boolean parseSuccess = false;
    boolean handlerError = false;
    boolean initialSendOrReceiveError = false;

    String handlerErrorMsg = "";
    
    try {
      // Parse the test request into its separate components
      parseSuccess = this.parseTestRequest();

      String testRequestID = this.desc.getTestRequestID();
      String testCase = this.desc.getTestCase();
      String purpose = this.desc.getPurpose();
      String connectivityType = this.desc.getConnectivityType();

      // Make sure there isn't already another running test for a different test case for this trading partner            
      String tp = FHIRplaceUtil.getTP(trans, params);
      String activeTest;
      int activeCount = 0;
      while ((activeTest = params.getPartnerAlreadyActiveTest(purpose, testRequestID, tp)) != null && activeCount < 5) {
        log.write("Partner [" + tp + "] still has an active test [" + activeTest + "], waiting 30 seconds for it to complete (" + testRequestID + ")");
        activeCount++;
        FHIRplaceUtil.wait(30);
      }
      this.setName("ProcessTestRequest:" + testRequestID + "_" + this.getName());

      // If there is a test description, log it
      if (!FHIRplaceUtil.isNullOrEmpty(this.desc.getTestDescription())) {
        log.write("Description: " + this.desc.getTestDescription() + " (" + testRequestID + ")");
      }
      
      // If there is a Connectivity Type, log it
      if (!FHIRplaceUtil.isNullOrEmpty(connectivityType)) {
        log.write("Connectivity Type: " + connectivityType + " (" + testRequestID + ")");
      }
      
      // Send the ACK that the FHIRPlace Message was successfully parsed and we're ready to start the test
      String ackMessage = "Sent ACK for " + purpose;
      FHIRplaceUtil.sendStatus(FHIRplaceConstants.ACK, this.requestFileName, this.instruct, this.trans, this.params, ackMessage);
      log.write(ackMessage  + " (" + testRequestID + ")");

      // Perform necessary test set up and send/receive the message
      if (!params.getCancelledTestRequest() &&
          !params.isCancelledTestRequest(testRequestID) &&
          purpose.equalsIgnoreCase("TestRequest")) {

        // Get expected result.  If it isn't present, default it to "Success"
        String expectedResult = desc.getExpectedResult() != null ? desc.getExpectedResult() : FHIRplaceConstants.SUCCESS;
        boolean success = expectedResult.equalsIgnoreCase(FHIRplaceConstants.SUCCESS);
        String statusMsg = "";
        int statusType;
         
        // This is the initial send/receive as specified in the test requests
        boolean sending = FHIRplaceUtil.isSending(this.trans, this.params);
        if (sending) {  
          // Note:  This is the Client (New Payer) sending to the Server (Old Payer)
          
          // Trigger off the send Data Type to determine what you are going to send
          String sendDataType = instruct.getSendDataType();
          log.write("Preparing to send " + sendDataType + " transmission for Test Case " + testCase +
                    " to " + FHIRplaceUtil.getTP(this.trans, this.params) + " (" + testRequestID + ")");

// **** Put your connection to partner code or hooks to your FHIR Client here ***
           
          // Check for the HTTP status of your send to determine if the transfer 
          // was a success or not (override the success boolean if unsuccessful)
          
          // Report the result of the initial transmission
          statusMsg =  success ? "Successfully sent " + sendDataType + " to " + FHIRplaceUtil.getTP(trans, params) :
                                 "Error sending " + sendDataType + " to " + FHIRplaceUtil.getTP(trans, params);

          statusType = success ? FHIRplaceConstants.SENT_OK : FHIRplaceConstants.SENT_NOT_OK;

          FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);          

          initialSendOrReceiveError = !success;

          // Also upload sent data for the send data types indicated below
          switch(sendDataType) {
            case FHIRplaceConstants.REGISTRATION_TYPE :
              // Nothing to upload yet for this data type
              break;

            case FHIRplaceConstants.ACCESS_REQUEST_TYPE :
              // Nothing to upload yet for this data type
              break;
            
            case FHIRplaceConstants.MEMBER_MATCH_TYPE :
              // Nothing to upload yet for this data type
              break;
            
            case FHIRplaceConstants.FHIR_ID_TYPE :
              // Upload the FHIR-ID that was sent
              String fhirID = "ABCDEFGHIJKLMNOP";
              uploadSentData(testRequestID, FHIRplaceConstants.FHIR_ID_DATA, fhirID);              
              break;
            
            case FHIRplaceConstants.PATIENT_REQUEST_TYPE :
              // Upload the Access-Token
              String accessToken = "123456789";
              uploadSentData(testRequestID, FHIRplaceConstants.ACCESS_TOKEN_DATA, accessToken);
              break;
          }

          // Log the result of the sent status
          log.write(statusMsg + " - (" + this.desc.getTestRequestID() + ")");

          
        // Receiving - This is the Server (Old Payer) receiving from the Client (New Payer)
        } else {

          String receiveDataType = instruct.getReceiveDataType();
          
          log.write("Waiting to receive " + receiveDataType + " for Test Case " + testCase + 
                     " from " + FHIRplaceUtil.getTP(this.trans, this.params) + " (" + testRequestID + ")" );

// **** Put connection receiving code here ***

          // When receiving, trigger off the receiveDataType value
          log.write("The received data type is: " + receiveDataType);
                     
          // Determine if the initial receive was a success or not or fake it 
          // as a failure for the test cases expecting an error result
          // Simulate a failure for the Failure-type tests

          if (expectedResult.equals(FHIRplaceConstants.FAILURE))
            success = false;
          
          // Report the result of the initial transmission
          statusMsg  = success ? "Successfully received " + receiveDataType + " from " + FHIRplaceUtil.getTP(trans, params) :
                                 "Error receiving " + receiveDataType + " from " + FHIRplaceUtil.getTP(trans, params);
          
          statusType = success ? FHIRplaceConstants.RECEIVED_OK : FHIRplaceConstants.RECEIVED_NOT_OK;
          FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
        
          initialSendOrReceiveError = !success;

          // Also upload received data where indicated
          switch(receiveDataType) {
            case FHIRplaceConstants.REGISTRATION_TYPE :
              // Nothing to upload yet for this data type
              break;
            
            case FHIRplaceConstants.ACCESS_REQUEST_TYPE :
              // Nothing to upload yet for this data type
              break;
            
            case FHIRplaceConstants.MEMBER_MATCH_TYPE :
              // Nothing to upload yet for this data type
              break;
            
            case FHIRplaceConstants.FHIR_ID_TYPE :
              // Upload FHIR-ID
              String fhirID = generateMismatchErrors ? "ABCDEFG" : "ABCDEFGHIJKLMNOP";
              uploadReceivedData(testRequestID, FHIRplaceConstants.FHIR_ID_DATA, fhirID, statusMsg);
              
              // Verify the FHIR-ID
              statusMsg =  success ? "Successfully validated the FHIR-ID" : "FHIR-ID was invalid";        
              statusType = success ? FHIRplaceConstants.VERIFIED_OK : FHIRplaceConstants.VERIFIED_NOT_OK;
              FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
              break;
            
            case FHIRplaceConstants.PATIENT_REQUEST_TYPE :
              // Upload the Access Token
              String accessToken = generateMismatchErrors ? "1234567890" : "123456789";
              uploadReceivedData(testRequestID, FHIRplaceConstants.ACCESS_TOKEN_DATA, accessToken, statusMsg);
              
              // Verify whether the Access Token is OK or not.. 
              statusMsg =  success ? "Access Token is valid" : "Access Token is invalid";        
              statusType = success ? FHIRplaceConstants.VERIFIED_OK : FHIRplaceConstants.VERIFIED_NOT_OK;
              FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);              
              break;            
          }

          // Log the result of the received status
          log.write(statusMsg + " - (" + this.desc.getTestRequestID() + ")");
        }

        if (initialSendOrReceiveError) {
          // If there was an error, negatively evaluate the test
          FHIRplaceUtil.sendStatus(FHIRplaceConstants.RESULTS_NOT_OK, this.requestFileName, 
                                   this.instruct, this.trans, this.params, statusMsg);
          log.write("Test status(" + FHIRplaceUtil.getTP(trans, params) + "): FAILURE - " + statusMsg + " - (" +
                     this.desc.getTestRequestID() + ")");
        }

        // Update the transaction date in the Transmission class
        trans.setDate();

      } else {
        // If a CancelRequest, add this request to the cancelled request table
        // and attempt to stop the processing of this message
        if ((params.getCancelledTestRequest() && params.isCancelledTestRequest(testRequestID)) ||
             purpose.equalsIgnoreCase("CancelRequest")) {
          String messageText = "Request (" + testRequestID + ") cancelled by user";
          log.write(messageText);

          if (!params.getCancelledTestRequests().containsKey(testRequestID))
            params.addCancelledTestRequest(testRequestID);

          // Negatively evaluate this request
          FHIRplaceUtil.sendStatus(FHIRplaceConstants.RESULTS_NOT_OK, this.requestFileName, 
                                this.instruct, this.trans, this.params, messageText);
        }
      }

    } catch(Exception ex) {
      if (!parseSuccess) {
        try {        
          log.write("Could not parse test request: " + ex.getMessage() + " - (" + this.desc.getTestRequestID() + ")");
          log.writeStackTrace(ex);  // log stack trace
          String nakMessage = "Sent NAK for Test " + this.desc.getTestRequestID() + " - reason: " + ex.getMessage();
          FHIRplaceUtil.sendStatus(FHIRplaceConstants.NAK, this.requestFileName, this.instruct, this.trans, this.params, nakMessage);
          log.write(nakMessage + " - (" + this.desc.getTestRequestID() + ")");

        } catch (Exception ex2) {
          log.writeStackTrace("ERROR: Could not send NAK", ex2, this.desc.getTestRequestID());
        }
      } else {
        // Some other error, probably in the handler occurred.
        handlerError = true;
        handlerErrorMsg = "Unexpected handler error: " + ex.getMessage();
        boolean sending = FHIRplaceUtil.isSending(trans, params);
        if (sending)
          handlerErrorMsg += ", message not sent.";
        else
          handlerErrorMsg += ", receive aborted.";

        log.write(handlerErrorMsg + " - (" + this.desc.getTestRequestID() + ")");
        
        // Cancel this request
        params.addCancelledTestRequest(this.desc.getTestRequestID());
        log.writeStackTrace(ex, this.desc.getTestRequestID());
      }      
    }

    try {
      // We have processed the test request, so move it to the archive
      fileUtility.moveFileToArchiveDirectory(requestFileName, params);
    } catch (Exception e) {
      log.write("Error moving test request " + this.desc.getTestRequestID() + " to archive: " +
                e.getMessage() + " - (" + this.desc.getTestRequestID() + ")");
    }

    if (parseSuccess & !handlerError && !initialSendOrReceiveError) {
      try {
        // Respond to the initial send with a response or receive a response from the initial send
        this.respondToInitialSendOrReceive();
      } catch (Exception e) {
        String errMsg = "Error parsing results file and updating status: " + e.getMessage();
        log.write(errMsg + " - (" + this.desc.getTestRequestID() + ")");
        log.writeStackTrace(e, this.desc.getTestRequestID());  // log stack trace

        try {
          FHIRplaceUtil.sendStatus(FHIRplaceConstants.RESULTS_NOT_OK, this.requestFileName, 
                                this.instruct, this.trans, this.params, errMsg);
        } catch (Exception e2) {
           log.write("Error updating TEST STATUS (" + FHIRplaceUtil.getTP(trans, params) + ")" + " - (" +
                     this.desc.getTestRequestID() + ")");
        }
      }
    }

    // Move any resources obtained during this test to the archive 
    try {
      if (params.isDeletePayload()) {
        String resource  = trans.getBundleName() + "." + trans.getBundleType().toLowerCase();
        String resourcePath = this.params.getPayloadDirectory() + FHIRplaceConstants.FILE_SEPARATOR + resource;
        fileUtility.moveFileToArchiveDirectory(resourcePath, params);
      }
    } catch (Exception e) {
      log.write("Error moving resource for test " + this.desc.getTestRequestID() +
                " to archive: " + e.getMessage() + " - (" + this.desc.getTestRequestID() + ")");
      log.writeStackTrace(e, this.desc.getTestRequestID());   // log stack trace
    }

    try {
      if (!parseSuccess) {
        FHIRplaceUtil.sendStatus(FHIRplaceConstants.RESULTS_NOT_OK, this.requestFileName, 
                              this.instruct, this.trans, this.params, "Message NAKed");
        log.write("Test status(" + FHIRplaceUtil.getTP(trans, params) + "): FAILURE - Message NAKed" + " - (" +
                   this.desc.getTestRequestID() + ")");

      } else if (handlerError) {
        FHIRplaceUtil.sendStatus(FHIRplaceConstants.RESULTS_NOT_OK, this.requestFileName, 
                              this.instruct, this.trans, this.params, handlerErrorMsg);
        log.write("Test status(" + FHIRplaceUtil.getTP(trans, params) + "): " + handlerErrorMsg + " - (" +
                  this.desc.getTestRequestID() + ")");

        // If the request is still active in the list, make sure it is cancelled 
        if ((!params.isCancelledTestRequest(this.desc.getTestRequestID()) &&
              params.isActiveTest(this.desc.getTestRequestID()))) {
              
          // Do some things here if necessary to cancel the test....
          
          params.addCancelledTestRequest(this.desc.getTestRequestID());
        }  
      }
    } catch (Exception e) {
      log.write("Error updating TEST STATUS (" + FHIRplaceUtil.getTP(trans, params) + ")" + " - (" +
                this.desc.getTestRequestID() + ")");
      log.writeStackTrace(e, this.desc.getTestRequestID());   // log stack trace
    }    
  }

  /**
   * Perform the second set of Send/Receive commands 
   * where the initial receiver (Old Payer) responds 
   * to the initial sender (New Payer) with additional data
   * 
   * Note that this is *not* an asynchronous send or receive.  
   * This is just the recording of the response to the original send or receive.
   * 
   * @throws Exception 
   */
  private void respondToInitialSendOrReceive() throws Exception {

    // Determine if we were originally originallySending or receiving
    boolean originallySending = false;
    if (FHIRplaceUtil.isSending(trans, params))
      originallySending = true;

    String testRequestID = this.desc.getTestRequestID();

    boolean success = true;
    boolean verificationError = false;      // set to true if there is a verification error
    String verificationErrorMsg = "";
    
    
    String statusMsg;
    int statusType;
    
    // This is the Server (Old Payer) responding to the request from the Client (New Payer)
    
    if (!originallySending) {
 
      String sendDataType = instruct.getSendDataType();
      log.write("Returning a " + sendDataType + " response...");
      
 // *** Put your response to the initial send here
    
      // Report the result of that response
      statusMsg =  success ? "Successfully sent " + sendDataType + " response to " + FHIRplaceUtil.getTP(trans, params) :
                             "Error sending " + sendDataType + " response to " + FHIRplaceUtil.getTP(trans, params);
      statusType = success ? FHIRplaceConstants.SENT_OK : FHIRplaceConstants.SENT_NOT_OK;
      FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
      
      // Upload the content that was sent with the response
      switch (sendDataType) {
        case FHIRplaceConstants.CLIENT_ID_TYPE :
          // Upload Client ID
          String clientID = "Some Client ID";
          uploadSentData(testRequestID, FHIRplaceConstants.CLIENT_ID_DATA, clientID);
          break;
        
        case FHIRplaceConstants.ACCESS_TOKEN_TYPE :
          String accessToken = generateMismatchErrors ? "1234567890" : "123456789";
          uploadSentData(testRequestID, FHIRplaceConstants.ACCESS_TOKEN_DATA, accessToken);
          break;
          
        case FHIRplaceConstants.FHIR_ID_TYPE :
          String fhirID = "ABCDEFGHIJKLMNOP";
          uploadSentData(testRequestID, FHIRplaceConstants.FHIR_ID_DATA, fhirID);           
          break;
          
        case FHIRplaceConstants.PATIENT_DATA_TYPE :
          // Upload the transported headers and metadata in response
          String headersAndMetaData = "Sent headers and metadata";
          uploadSentData(testRequestID, FHIRplaceConstants.TRANSPORT_DATA, headersAndMetaData);
          break;
      }
          
    //  This is the Client (New Payer) receiving the responce from the Server (Old Payer)

    } else {  
      String receiveDataType = instruct.getReceiveDataType();
      log.write("Receiveing a " + receiveDataType + " response...");

  // **** Here's where you will either add code to receive the response or extract
  //      the result of the response from your initial message      

      // Report the result of the received response
      statusMsg  = success ? "Successfully received " + receiveDataType + " response from " + FHIRplaceUtil.getTP(trans, params) :
                             "Error receiving " + receiveDataType + " response from " + FHIRplaceUtil.getTP(trans, params);
          
      statusType = success ? FHIRplaceConstants.RECEIVED_OK : FHIRplaceConstants.RECEIVED_NOT_OK;
      FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
      
      switch (receiveDataType) {
        case FHIRplaceConstants.CLIENT_ID_TYPE :
        {
          // Upload Client ID
          String clientID = "Some Client ID";
          uploadReceivedData(testRequestID, FHIRplaceConstants.CLIENT_ID_DATA, clientID, statusMsg);

          // Verify whether the client ID is OK or not
          statusMsg =  success ? "Client ID is valid" : "Client ID is invalid";        
          statusType = success ? FHIRplaceConstants.VERIFIED_OK : FHIRplaceConstants.VERIFIED_NOT_OK;
          FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
          
          verificationError = !success;
          if (verificationError) {
            verificationErrorMsg = statusMsg;
          }
          break;
        }
        
        case FHIRplaceConstants.ACCESS_TOKEN_TYPE :
        {
          String accessToken = "123456789";
          uploadReceivedData(testRequestID, FHIRplaceConstants.ACCESS_TOKEN_DATA, accessToken, statusMsg);
          
          // Verify whether the access token is OK or not
          statusMsg =  success ? "Successfully validated the Access Token" : "Access Token was invalid";        
          statusType = success ? FHIRplaceConstants.VERIFIED_OK : FHIRplaceConstants.VERIFIED_NOT_OK;
          FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);

          verificationError = !success;
          if (verificationError) {
            verificationErrorMsg = statusMsg;
          }          
          break;
        }

        case FHIRplaceConstants.FHIR_ID_TYPE :
        {
          String fhirID = "ABCDEFGHIJKLMNOP";
          uploadReceivedData(testRequestID, FHIRplaceConstants.FHIR_ID_DATA, fhirID, statusMsg);
          
          // Validate whether the FHIR-ID (Member ID) is OK or not
          statusMsg =  success ? "Successfully validated the Member ID" : "Member ID was invalid";        
          statusType = success ? FHIRplaceConstants.VERIFIED_OK : FHIRplaceConstants.VERIFIED_NOT_OK;
          FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
          
          verificationError = !success;
          if (verificationError) {
            verificationErrorMsg = statusMsg;
          }
          break;
        }

        case FHIRplaceConstants.PATIENT_DATA_TYPE :
        {
          // Upload the transported headers and metadata in response
          String headersAndMetaData = "Received headers and metadata";
          uploadReceivedData(testRequestID, FHIRplaceConstants.TRANSPORT_DATA, headersAndMetaData, statusMsg);
          
          // Verify whether the patient data is OK or not
          statusMsg = success ? "Successfully validated the Patient Data" : "Patient Data was invalid";
          statusType = success ? FHIRplaceConstants.VERIFIED_OK : FHIRplaceConstants.VERIFIED_NOT_OK;
          FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
          
          verificationError = !success;
          if (verificationError) {
            verificationErrorMsg = statusMsg;
          }
          break;
        }
      }

      log.write(statusMsg + " - (" + this.desc.getTestRequestID() + ")");                 
    }
    
    // Evaluate the overall outcome of the test
    evaluateTest(originallySending, success, testRequestID, verificationError, verificationErrorMsg);
   
    this.params.removeActiveTest(testRequestID);
    if (params.isDebugMode()) {
      log.write("Removed partner '" + FHIRplaceUtil.getTP(trans, params) + "' from list of active tests. (" + testRequestID + ")\n");
    }  
  }  

  /**
   * Uploads sent data to the FHIRplace server
   * 
   * @param testRequestID the test testRequestID
   * @param uploadType the type of data being uploaded (i.e., Access Token, FHIR-ID or Patient Data)
   * @param sentData the data sent to the trading partner
   * @throws Exception 
   */
  private void uploadSentData(String testRequestID, int uploadType, String sentData) 
    throws Exception {

    String uploadTypeDesc = FHIRplaceUtil.getDataTypeDescription(uploadType);
    int count=0;

    try {
      // Determine the name of the expected file name for the upload
      Instructions.Upload[] uploads = instruct.getUpload(FHIRplaceConstants.SENT_DATA, uploadType);

      for (Instructions.Upload upload : uploads) {
        fileUtility.uploadData(FHIRplaceConstants.SENT_DATA, uploadType, sentData, 
                               testRequestID, upload.getResponseID(), params.getStatusDirectory());

        log.write("Uploaded " + uploadTypeDesc + " (" + sentData + ") sent to " + FHIRplaceUtil.getTP(trans, params) + " - (" + testRequestID + ")");
        count++;
      }
      if (count == 0) {
        log.write("Warning: No SENT " + uploadTypeDesc + " uploads were completed.");
        log.write("TestRequestID->" + testRequestID + ", uploadType->" + uploadType);
      }

    } catch (Exception e) {
      log.write("Error uploading " + uploadTypeDesc + " sent to " + FHIRplaceUtil.getTP(trans, params) + " - (" + testRequestID + ")");
      log.writeStackTrace(e, testRequestID);   // log stack trace
    }        
  }

  /**
   * Uploads the received data to the FHIRplace server
   * 
   * @param testRequestID the test request ID
   * @param uploadType the type of data being uploaded (i.e., Access Token, FHIR-ID or Patient Data)
   * @param receivedData received from the trading partner
   * @param recvMsg contains status information related to this upload, either the
   *                previously set status information, or overridden with errors generated here
   * 
   * @return either the current status or updated status (if an exception occurs)
   * @throws Exception 
   */
  private String uploadReceivedData(String testRequestID, int uploadType, String receivedData, String recvMsg) 
    throws Exception {

    String uploadTypeDesc = FHIRplaceUtil.getDataTypeDescription(uploadType);
    int count=0;

    try {
      // Determine the name of the expected file name for the upload
      Instructions.Upload[] uploads = instruct.getUpload(FHIRplaceConstants.RECEIVED_DATA, uploadType);  

      for (Instructions.Upload upload : uploads) {
        fileUtility.uploadData(FHIRplaceConstants.RECEIVED_DATA, uploadType, receivedData, 
                               testRequestID, upload.getResponseID(), params.getStatusDirectory());

        log.write("Uploaded " + uploadTypeDesc + " (" + receivedData +  ") received from " + 
                  FHIRplaceUtil.getTP(trans, params) + " - (" + testRequestID + ")");
        count++;
      }
      if (count == 0) {
        log.write("RequestID->" + testRequestID + ", uploadType->" + uploadType);
      }
    } catch (Exception e) {
      recvMsg = e.getMessage();
      log.write("Error uploading " + uploadTypeDesc + " received from " + FHIRplaceUtil.getTP(trans, params) + " - ( " +
                testRequestID + ")");
      
      log.writeStackTrace(e, testRequestID);
    }
    return recvMsg;
  }

  /**
   * Evaluates the overall test and upload the status to the FHIRplace server
   * @param sending true if originallySending, false if receiving
   * @param success true if the test succeeded
   * @param requestID the test request ID
   * @param verificationError true if a verification error occurred
   * @param verificationErrorMsg the verification error text, if any
   * @throws Exception 
   */
  private void evaluateTest(boolean sending, boolean success, String requestID, 
                            boolean verificationError, String verificationErrorMsg) 
    throws Exception {
    if (params.isCancelledTestRequest(requestID)) {
      String messageText = "Cancelled by user";
      FHIRplaceUtil.sendStatus(FHIRplaceConstants.RESULTS_NOT_OK, this.requestFileName, this.instruct, this.trans, this.params, messageText);
      
      log.write("Test status (" + FHIRplaceUtil.getTP(trans, params) + "): " + messageText + " - ( " + requestID + ")");
      this.params.removeCancelledTestRequest(requestID);

    } else if (verificationError) {
      String fullErrorMessage = verificationErrorMsg;
      if (verificationErrorMsg.length() > 0)
        fullErrorMessage += " ";

      FHIRplaceUtil.sendStatus(FHIRplaceConstants.RESULTS_NOT_OK, this.requestFileName, this.instruct, this.trans, this.params, fullErrorMessage);
      log.write("Test status (" + FHIRplaceUtil.getTP(trans, params) + "): " + fullErrorMessage + " - ( " + requestID + ")");

      
    } else if (success) {
      String successMsg = "Success!";

      FHIRplaceUtil.sendStatus(FHIRplaceConstants.RESULTS_OK, this.requestFileName, this.instruct, this.trans, this.params, successMsg);
      StringBuilder buf = new StringBuilder();

      buf.append("Test Status (").append(FHIRplaceUtil.getTP(trans, params)).append(") - ").append(successMsg);
      log.write(buf.toString() + " - (" + requestID + ")");

    } else {
      String messageText = null;
      if (params.isCancelledTestRequest(requestID) ) {
        messageText = "Request (" + requestID + ") cancelled by user";
        if (new File(requestFileName).exists()) {
          log.write("Attempting to move " + requestFileName + " to archive");
          fileUtility.moveFileToArchiveDirectory(requestFileName, params);
        }
      } else {
        // Make sure we always have a reason
        if (sending) {
          messageText = " could not send message";
        } else {
          messageText = " Timeout occurred while attempting to receive message or response";
        }
      }
      FHIRplaceUtil.sendStatus(FHIRplaceConstants.RESULTS_NOT_OK, this.requestFileName, this.instruct, this.trans, this.params, messageText);
      log.write("Test status (" + FHIRplaceUtil.getTP(trans, params) + "): FAILURE - " + messageText + " - ( " + requestID + ")");
    }    
  }  
}
