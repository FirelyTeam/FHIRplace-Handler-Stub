package com.dgi.fhirplace.handler;

import com.dgi.fhirplace.parser.Description;
import com.dgi.fhirplace.parser.FHIRplaceXML;
import com.dgi.fhirplace.parser.Instructions;
import com.dgi.fhirplace.parser.ParseTestRequest;
import com.dgi.fhirplace.parser.Participant;
import com.dgi.fhirplace.parser.Transmission;
import com.dgi.fhirplace.util.FHIRplaceUtil;
import com.dgi.fhirplace.util.FileUtility;
import com.google.common.base.Strings;

import java.io.File;


/**
 * Invoked once for each incoming test request as a thread:
 *   - parses the test request
 *   - sends the ACK/NAK messages to the FHIRplace server
 *   - sets up to run the test 
 *   - parses the results
 *   - uploads the status and any other required uploads to the FHIRplace server
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
  boolean generateMismatchErrors = false;
  
  public ProcessTestRequest(FHIRplaceHandler handler, LocalParameters params, String requestFileName) {
    this.handler = handler;
    
    this.params = params;
    this.requestFileName = requestFileName;

    this.fileUtility = new FileUtility();
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
      String testCaseType = this.desc.getTestCaseType();

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
      if (!Strings.isNullOrEmpty(this.desc.getTestDescription())) {
        log.write("Description: " + this.desc.getTestDescription() + " (" + testRequestID + ")");
      }
      
      // Send the ACK that the FHIRPlace Message was successfully parsed and we're ready to start the test
      String ackMessage = "Sent ACK for " + purpose;
      FHIRplaceUtil.sendStatus(FHIRplaceConstants.ACK, this.requestFileName, this.instruct, this.trans, this.params, ackMessage);
      log.write(ackMessage  + " (" + testRequestID + ")");

      // Perform necessary test set up and send/receive here
      if (!params.getCancelledTestRequest() &&
          !params.isCancelledTestRequest(testRequestID) &&
          purpose.equalsIgnoreCase("TestRequest")) {

        String expectedResult = desc.getExpectedResult();
        boolean success = expectedResult.equalsIgnoreCase(FHIRplaceConstants.SUCCESS);
        String statusMsg = "";
        int statusType;
         
        // This is the initial send/receive as specified in the test requests
        boolean sending = FHIRplaceUtil.isSending(this.trans, this.params);
        if (sending) {
          log.write("Preparing to send " + testCaseType + " transmission for Test Case " + testCase +
                    " to " + FHIRplaceUtil.getTP(this.trans, this.params) + " (" + testRequestID + ")");

// **** Put your connection to partner code here ***

          // Check for the HTTP status of your send to determine if the transfer 
          // was a success or not (override the success boolean if unsuccessful)

          initialSendOrReceiveError = !success;
          
          switch(testCaseType) {
            case FHIRplaceConstants.CONNECTION :              // 'Establish' and 'Complete' test cases
            case FHIRplaceConstants.CLIENT_ID_MISMATCH :      // Authenticate_1 test case
            case FHIRplaceConstants.EXPIRED_ACCESS_TOKEN :    // Authenticate_2 test case  
            {  
              statusMsg =  success ? "Successfully SENT initial transmission to " + FHIRplaceUtil.getTP(trans, params) :
                                     "Error sending initial transmission to " + FHIRplaceUtil.getTP(trans, params);
            
              statusType = success ? FHIRplaceConstants.SENT_OK : FHIRplaceConstants.SENT_NOT_OK;

              FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
              break;
            }
            
            case FHIRplaceConstants.MEMBER_AUTH :            // 'Single Member' test case
            {  
              statusMsg = success ? "Successfully sent Member Identifier" :
                                    "Did not successfully send Member Identifier";

              statusType = success ? FHIRplaceConstants.SENT_FHIR_ID_OK :
                                     FHIRplaceConstants.SENT_FHIR_ID_NOT_OK;

              FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
              break;
            }
            
            case FHIRplaceConstants.PDEX_DEVICE :
            case FHIRplaceConstants.PDEX_MEDICATION :
            case FHIRplaceConstants.PDEX_PROVENANCE :
            {
              statusMsg = success ? "Successfully sent PDEX Request" :
                                    "Did not successfully send PDEX Request";
              
              statusType = success ? FHIRplaceConstants.SENT_PDEX_REQUEST_OK :
                                     FHIRplaceConstants.SENT_PDEX_REQUEST_NOT_OK;
 
              FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
              break;
            }
            
            case FHIRplaceConstants.PATIENT_ALL :
            case FHIRplaceConstants.PATIENT_NS :
            {  
              statusMsg = success ? "Successfully sent Patient Request" :
                                    "Did not successfully send Patient Request";
              statusType = success ? FHIRplaceConstants.SENT_PATIENT_REQUEST_OK : 
                                     FHIRplaceConstants.SENT_PATIENT_REQUEST_NOT_OK;
              
              FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);                 
              break;
            } 
          }
 
          // Also upload sent data where indicated
          switch (testCaseType) {
            case FHIRplaceConstants.CLIENT_ID_MISMATCH :      // Authenticate_1 test case
            {
              // Upload Client ID
              String clientID = "Some Client ID";
              uploadSentData(testRequestID, FHIRplaceConstants.CLIENT_ID_DATA, clientID);
              break;
            }  
            case FHIRplaceConstants.EXPIRED_ACCESS_TOKEN :    // Authenticate_2 test case 
            {
              // Upload transported headers and metadata
              String headersAndMetaData = "Sent headers and metadata";
              uploadSentData(testRequestID, FHIRplaceConstants.TRANSPORT_DATA, headersAndMetaData);
              break;
            }
            case FHIRplaceConstants.PDEX_DEVICE :
            case FHIRplaceConstants.PDEX_MEDICATION :
            case FHIRplaceConstants.PDEX_PROVENANCE :
            case FHIRplaceConstants.PATIENT_ALL :
            case FHIRplaceConstants.PATIENT_NS :
            {
              // Upload Access-Token
              String accessToken = "123456789";
              FHIRplaceUtil.sendStatus(FHIRplaceConstants.SENT_ACCESS_TOKEN_OK, requestFileName, this.instruct, this.trans, this.params, statusMsg);
              uploadSentData(testRequestID, FHIRplaceConstants.ACCESS_TOKEN_DATA, accessToken);
              break;
            }
            case FHIRplaceConstants.MEMBER_AUTH :
            {
              // Upload FHIR-ID
              String fhirID = "ABCDEFGHI";
              FHIRplaceUtil.sendStatus(FHIRplaceConstants.SENT_FHIR_ID_OK, requestFileName, this.instruct, this.trans, this.params, statusMsg);
              uploadSentData(testRequestID, FHIRplaceConstants.FHIR_ID_DATA, fhirID);              
              break;
            }
          }
          
        // Receiving
        } else {         
          log.write("Waiting to receive initial transmission for Test Case " + testCase +
                     " from " + FHIRplaceUtil.getTP(this.trans, this.params) + " (" + testRequestID + ")" );

// **** Put connection receiving code here ***

          // Determine if the initial receive was a success or not or fake it 
          // as a failure for the test cases expecting an error result
          // Simulate a failure for the Failure-type tests

          if (expectedResult.equals(FHIRplaceConstants.FAILURE))
            success = false;
          
          initialSendOrReceiveError = !success;

         switch(testCaseType) {
            case FHIRplaceConstants.CONNECTION :               // 'Establish' and 'Complete' test cases
            case FHIRplaceConstants.CLIENT_ID_MISMATCH :       // Authenticate_1 test case
            case FHIRplaceConstants.EXPIRED_ACCESS_TOKEN :     // Authenticate_2 test case
            {  
              // Test Case Establish and Complete
              statusMsg  = success ? "Successfully RECEIVED initial transmission from " + FHIRplaceUtil.getTP(trans, params) :
                                     "Error receiving initial transmission from " + FHIRplaceUtil.getTP(trans, params);
            
              statusType = success ? FHIRplaceConstants.RECEIVED_OK : FHIRplaceConstants.RECEIVED_NOT_OK;
              FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);              
              break;
            }
            case FHIRplaceConstants.MEMBER_AUTH :
            {
              statusMsg = success ? "Successfully received Member Identifier" :
                                  "Did not successfully receive Member Identifier";
              
              statusType = success ? FHIRplaceConstants.RECEIVED_FHIR_ID_OK :
                                     FHIRplaceConstants.RECEIVED_FHIR_ID_NOT_OK;
              
              FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
              break;
            }

            case FHIRplaceConstants.PDEX_DEVICE :
            case FHIRplaceConstants.PDEX_MEDICATION :
            case FHIRplaceConstants.PDEX_PROVENANCE :
            {
              statusMsg = success ? "Successfully received PDEX Request" :
                                    "Did not successfully receive PDEX Request";
              
              statusType = success ? FHIRplaceConstants.RECEIVED_PDEX_REQUEST_OK :
                                     FHIRplaceConstants.RECEIVED_PDEX_REQUEST_NOT_OK;
 
              FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
              break;
            }
            
            case FHIRplaceConstants.PATIENT_ALL :
            case FHIRplaceConstants.PATIENT_NS :
            {
              statusMsg = success ? "Successfully received Patient Request" :
                                    "Did not successfully receive Patient Request";
              
              statusType = success ? FHIRplaceConstants.RECEIVED_PATIENT_REQUEST_OK : 
                                     FHIRplaceConstants.RECEIVED_PATIENT_REQUEST_NOT_OK;
              
              FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg); 
              break;
            }
          }
         
          // Also upload received data and verify where indicated
          switch(testCaseType) {
            case FHIRplaceConstants.CONNECTION :   
            {
              // Don't need to do anything
              break;
            }
            
            case FHIRplaceConstants.CLIENT_ID_MISMATCH :      // Authenticate_1
            {
              // Upload Client ID
              String clientID = "A Different Client ID";
              statusMsg = "Client ID Mismatch";
              uploadReceivedData(testRequestID, FHIRplaceConstants.CLIENT_ID_DATA, clientID, statusMsg);

              // Verify whether the FHIR_ID is OK or not..     
              statusType = success ? FHIRplaceConstants.CLIENT_ID_VERIFIED_OK : 
                                     FHIRplaceConstants.CLIENT_ID_VERIFIED_NOT_OK;

              FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);              
              break;
            }
            
            case FHIRplaceConstants.EXPIRED_ACCESS_TOKEN :    // Authenticate_2 test case 
            {
              // Upload transported headers and metadata
              String headersAndMetaData = "Received headers and metadata";
              statusMsg = "Expired Access Token";
              uploadReceivedData(testRequestID, FHIRplaceConstants.TRANSPORT_DATA, headersAndMetaData, statusMsg);
              
               // Verify whether the Access Token is OK or not.. 
              statusType = success ? FHIRplaceConstants.ACCESS_TOKEN_VERIFIED_OK : 
                                     FHIRplaceConstants.ACCESS_TOKEN_VERIFIED_NOT_OK;

              FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
              break;
            }
            
            case FHIRplaceConstants.MEMBER_AUTH :            // Single_Member test case
            {
              // Upload FHIR-ID
              String fhirID = generateMismatchErrors ? "ABCDEFG" : "ABCDEFGHI";
              
              statusMsg = success ? "Successfully received Member Identifier" :
                                    "Did not successfully receive Member Identifier"; 
             
              uploadReceivedData(testRequestID, FHIRplaceConstants.FHIR_ID_DATA, fhirID, statusMsg);
              
              // Verify whether the FHIR_ID is OK or not.. 
              statusMsg =  success ? "Successfully validated the FHIR-ID" : 
                                     "FHIR-ID was invalid";        
              statusType = success ? FHIRplaceConstants.FHIR_ID_VERIFIED_OK : 
                                     FHIRplaceConstants.FHIR_ID_VERIFIED_NOT_OK;

              FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
              
              break;
            }

            case FHIRplaceConstants.PDEX_DEVICE :
            case FHIRplaceConstants.PDEX_MEDICATION :
            case FHIRplaceConstants.PDEX_PROVENANCE :
            case FHIRplaceConstants.PATIENT_ALL :
            case FHIRplaceConstants.PATIENT_NS :
            {
              // Upload Access-Token
              String accessToken = generateMismatchErrors ? "1234567890" : "123456789";
              
              statusMsg = success ? "Successfully received Access Token" :
                                    "Did not successfully receive Access Token";
              
              uploadReceivedData(testRequestID, FHIRplaceConstants.ACCESS_TOKEN_DATA, accessToken, statusMsg);
              
              // Verify whether the Access Token is OK or not.. 
              statusMsg =  success ? "Access Token is valid" : 
                                     "Access Token is invalid";        
              statusType = success ? FHIRplaceConstants.ACCESS_TOKEN_VERIFIED_OK : 
                                     FHIRplaceConstants.ACCESS_TOKEN_VERIFIED_NOT_OK;

              FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
              
              break;
            }
          }
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
      }

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
        // Respond to the initial send with a receive and to the receive with a send
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

    String testCaseType = this.desc.getTestCaseType();
    String testRequestID = this.desc.getTestRequestID();

    boolean success = true;
    boolean verificationError = false;      // set to true if there is a verification error
    String verificationErrorMsg = "";
    
    
    String statusMsg;
    int statusType;
    
    // Original receiver responds to the first part of the message 
    // and now becomes the "sender"...
    
    if (!originallySending) {
      
      switch (testCaseType) {
        
        case FHIRplaceConstants.CONNECTION :   // 'Complete' test case
        {
          // Send, report and upload the Access Token
          String accessToken = generateMismatchErrors ? "1234567890" : "123456789";

          statusMsg = success ? "Successfully returned Access Token" :
                                "Did not successfully return Access Token";         
          statusType = success ? FHIRplaceConstants.SENT_ACCESS_TOKEN_OK :
                                 FHIRplaceConstants.SENT_ACCESS_TOKEN_NOT_OK;

          FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
          uploadSentData(testRequestID, FHIRplaceConstants.ACCESS_TOKEN_DATA, accessToken);
          
          break;
        }

        case FHIRplaceConstants.MEMBER_AUTH :          // Single_Member test case
        {
          // Send, report and upload the Access Token
          String accessToken = generateMismatchErrors ? "9876543210" : "987654321";

          statusMsg = success ? "Successfully sent Access Token" :
                                "Did not successfully send Access Token";
          statusType = success ? FHIRplaceConstants.SENT_ACCESS_TOKEN_OK :
                                 FHIRplaceConstants.SENT_ACCESS_TOKEN_NOT_OK;
          FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);

          uploadSentData(testRequestID, FHIRplaceConstants.ACCESS_TOKEN_DATA, accessToken);
          
          break;
        }

        case FHIRplaceConstants.PDEX_DEVICE :
        case FHIRplaceConstants.PDEX_MEDICATION :
        case FHIRplaceConstants.PDEX_PROVENANCE :
        {
          // Return PDEX Resource, report and upload the transport data (headers and metadata)
          statusMsg = success ? "Successfully returned PDEX Resource" :
                                "Did not successfully return PDEX Resource";         
          statusType = success ? FHIRplaceConstants.SENT_PDEX_RESOURCE_OK :
                                 FHIRplaceConstants.SENT_PDEX_RESOURCE_NOT_OK;
          FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
          
          String headersAndMetaData = "Sent headers and metadata";
          uploadSentData(testRequestID, FHIRplaceConstants.TRANSPORT_DATA, headersAndMetaData);
          
          break;
        }

        case FHIRplaceConstants.PATIENT_ALL :
        case FHIRplaceConstants.PATIENT_NS :
        {
          // Send, report and upload the Patient Data
          // For Patient_All this is all the data 
          // For Patient_NS this is just the non-sensitive data

          statusMsg = success ? "Successfully sent the Patient Data" :
                                "Did not successfully send the Patient Data";         
          statusType = success ? FHIRplaceConstants.SENT_PATIENT_DATA_OK:
                                 FHIRplaceConstants.SENT_PATIENT_DATA_NOT_OK;
          FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
          
          // Upload the transported headers and metadata
          String headersAndMetaData = "Sent headers and metadata";
          uploadSentData(testRequestID, FHIRplaceConstants.TRANSPORT_DATA, headersAndMetaData);
         
          break;
        }       
      }
      
    } else {
      // Original sender becomes the receiver of the response
      statusMsg = "";
      
      switch (testCaseType) {
        // Test Case 
        case FHIRplaceConstants.CONNECTION :
        case FHIRplaceConstants.MEMBER_AUTH :
        {
          // Receive, report, upload and verify the Access Token
          String accessToken = testCaseType.equalsIgnoreCase(FHIRplaceConstants.CONNECTION) ? "123456789" : "987654321";

          statusMsg = success ? "Successfully received Access Token" :
                                "Did not successfully receive Access Token";
          statusType = success ? FHIRplaceConstants.RECEIVED_ACCESS_TOKEN_OK :
                                 FHIRplaceConstants.RECEIVED_ACCESS_TOKEN_NOT_OK;
          FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);

          uploadReceivedData(testRequestID, FHIRplaceConstants.ACCESS_TOKEN_DATA, accessToken, statusMsg);
          
          
          // Validate whether the access token is OK or not.. 
          statusMsg =  success ? "Successfully validated the Access Token" : 
                                 "Access Token was invalid";        
          statusType = success ? FHIRplaceConstants.ACCESS_TOKEN_VERIFIED_OK : 
                                 FHIRplaceConstants.ACCESS_TOKEN_VERIFIED_NOT_OK;
          
          FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
          
          verificationError = !success;
          if (verificationError) {
            verificationErrorMsg = statusMsg;
          }

          break;
        }
         
        case FHIRplaceConstants.PDEX_DEVICE :
        case FHIRplaceConstants.PDEX_MEDICATION :
        case FHIRplaceConstants.PDEX_PROVENANCE :
        {
          // Receive PDEX Resource, report and upload the transport data (headers and metadata) 
          // and verify the resource data
          
          statusMsg = success ? "Successfully received PDEX Resource" :
                                "Did not successfully receive PDEX Resource";
          
          statusType = success ? FHIRplaceConstants.RECEIVED_PDEX_RESOURCE_OK :
                                 FHIRplaceConstants.RECEIVED_PDEX_RESOURCE_NOT_OK;
          FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
          
          String headersAndMetaData = "Received headers and metadata";
          uploadReceivedData(testRequestID, FHIRplaceConstants.TRANSPORT_DATA, headersAndMetaData, statusMsg);
          
          // Validate the PDEX Resource
          statusMsg = success ? "Successfully verified PDEX Resource" : 
                                "Did not successfully verify PDEX Resource";
          statusType = success ? FHIRplaceConstants.PDEX_RESOURCE_VERIFIED_OK :
                                 FHIRplaceConstants.PDEX_RESOURCE_VERIFIED_NOT_OK;
          FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
          
          verificationError = !success;
          if (verificationError) {
            verificationErrorMsg = statusMsg;
          }
          break;
        }
       
        case FHIRplaceConstants.PATIENT_ALL :
        case FHIRplaceConstants.PATIENT_NS :
        {
          // Receive, report, upload the transport data (headers and metadata) and verify the Patient Data
          // For Patient_All this should be all the data 
          // For Patient_NS this should only be just the non-sensitive data

          statusMsg = success ? "Successfully received the Patient Data" :
                                "Did not successfully receive the Patient Data";
          statusType = success ? FHIRplaceConstants.RECEIVED_PATIENT_DATA_OK :
                                 FHIRplaceConstants.RECEIVED_PATIENT_DATA_NOT_OK;
          FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);
          
          
          String headersAndMetaData = "Received headers and metadata";
          uploadReceivedData(testRequestID, FHIRplaceConstants.TRANSPORT_DATA, headersAndMetaData, statusMsg);
          

          // Validate whether the patient data is OK or not.. 
          statusMsg = success ? "Successfully validated the Patient Data" :
                                "Patient Data was invalid";
          statusType = success ? FHIRplaceConstants.PATIENT_DATA_VERIFIED_OK :
                                 FHIRplaceConstants.PATIENT_DATA_VERIFIED_NOT_OK;
          
          verificationError = !success;
          if (verificationError) {
            verificationErrorMsg = statusMsg;
          }
          FHIRplaceUtil.sendStatus(statusType, requestFileName, this.instruct, this.trans, this.params, statusMsg);

          break;
        }

      }
      log.write(statusMsg + " - (" + this.desc.getTestRequestID() + ")");      
           
    }
    
    // Evaluate the overall outcome of the test
    evaluateTest(originallySending, success, testRequestID, verificationError, verificationErrorMsg);
   
    this.params.removeActiveTest(testRequestID);
    if (params.isDebugMode()) {
      log.write("Removed partner '" + FHIRplaceUtil.getTP(trans, params) + "' from list of active tests. (" + testRequestID + ")");
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
