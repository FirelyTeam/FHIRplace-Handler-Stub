package com.dgi.fhirplace.util;

import com.dgi.fhirplace.handler.FHIRplaceConstants;
import com.dgi.fhirplace.handler.LocalParameters;
import com.dgi.fhirplace.parser.Transmission;
import com.dgi.fhirplace.parser.Instructions;
import com.google.common.base.Strings;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.w3c.dom.Node;


/**
 *  Utility Classes
 */
public class FHIRplaceUtil {

  static String APPEND_POSTIVE = "_P.sts";
  static String APPEND_NEGATIVE = "_N.sts";
  
  /**
   * Checks for the existence of all needed directories and creates them 
   * if they don't already exist.
   * @param params - the object containing the names of all directories to check.
   */
  public static synchronized void checkDirectories(LocalParameters params) {
    // Request Directory
    if (!Strings.isNullOrEmpty(params.getTestRequestDirectory())) {   
      File reqDir = new File(params.getTestRequestDirectory());
      if (!reqDir.exists()) {
        reqDir.mkdirs();
      }
    }
    if (!Strings.isNullOrEmpty(params.getStatusDirectory())) {
      // Status Directory
      File statDir = new File(params.getStatusDirectory());
      if (!statDir.exists()) {
       statDir.mkdirs();
      }
    }
    // Payload Directory
    if (!Strings.isNullOrEmpty(params.getPayloadDirectory())) {
      File payloadDir = new File(params.getPayloadDirectory());
      if (!payloadDir.exists()) {
        payloadDir.mkdirs();
      }
    }
    // Archive Directory
    if (!Strings.isNullOrEmpty(params.getArchiveDirectory())) {
      File archiveDir = new File(params.getArchiveDirectory());
      if (!archiveDir.exists()) {
        archiveDir.mkdirs();
      }
    }
  }

  /**
   * Extracts the test request from the file name in the form: 
   * Req-XXXX.xml (or Req-XXXX.kill for a cancelTestRequestID)
   * @param fileName - the file name to use
   * @return the request ID
   */

  public static String getTestRequestID(String fileName) {
    int start_idx = fileName.toLowerCase().indexOf("req-");
    int end_idx = fileName.toLowerCase().indexOf(".xml");
    
    // If the end index was not found, maybe it's a CancelRequest
    if (end_idx < 0)
      end_idx = fileName.toLowerCase().indexOf(".kill");
    return fileName.substring(start_idx+4, end_idx);
  }

  /**
   * Gets the trading partner name / identifier
   * @param trans - the Transmission object
   * @param params - the LocalParameters object
   * @return the trading partner name / identifier
   */
  public static String getTP(Transmission trans, LocalParameters params) {
    if (trans.getSenderID().equalsIgnoreCase(params.getXMLIdentifier())) {
      return trans.getReceiverID();
    } else {
      return trans.getSenderID();
    }
  }  

  /**
   * Returns a formatted date string
   * @return the formatted date string
   */
  public static String getDate() {
    Date curDate = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    return sdf.format(curDate);
  }

  public static String getPrettyDate() {
    Date curDate = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    return sdf.format(curDate);
  }
  
  /**
   * Returns the age of the file in minutes
   * @param thisFile - the file to check
   * @return the age of the specified file
   */
  public static long getFileAge(File thisFile) {
    if (!thisFile.exists()) {
      return -1;
    }
    Date now = new Date();

    // Compute the age of the file in milliseconds
    long now_ms = now.getTime();
    long age_ms = thisFile.lastModified();
    long diff = now_ms - age_ms;

    return ( diff / 60000);   // divide by the number of msec/min (1000 ms/sec * 60 sec/min)
  }

  /**
   * Returns the node name
   * @param node - the node to be checked
   * @return the node name
   */
  public static String getNodeName(Node node) {
    return node.getLocalName() != null ? node.getLocalName() : node.getNodeName();
  }

 /**
   * Determines if we are sending
   * @param trans - the Transmission object
   * @param params - the LocalParameters object
   * @return true if we are sending, otherwise false
   */
  public static boolean isSending(Transmission trans, LocalParameters params) {
    return (trans != null && 
            trans.getOriginator().equalsIgnoreCase(params.getXMLIdentifier()));
  }  

  /**
   * Waits the prescribed number of seconds
   * @param seconds - the number of seconds to wait
   * @throws java.lang.InterruptedException
   */
  public static void wait(int seconds) throws InterruptedException {
    Thread.sleep(seconds * 1000L);
  } 

  /**
   * Waits the prescribed number of milliseconds
   * @param msec the number of milliseconds to wait
   * @throws InterruptedException
   */
  public static void waitMilliseconds(long msec) throws InterruptedException {
    Thread.sleep(msec);
  }

  /**
   * Returns the stack trace from an exception 
   * @param ex - the Exception
   * @return the stack trace
   */
  public static String getStackTrace(Exception ex) {
    StringWriter sw = new StringWriter();      
    PrintWriter pw = new PrintWriter(sw, true);
    ex.printStackTrace(pw);
    return sw.toString();
  }
  
  /**
   * Returns a user-friendly string for the specified dataType
   */
  public static String getDataTypeDescription(int dataType) {
    switch (dataType) {
      case FHIRplaceConstants.ACCESS_TOKEN_DATA :
        return "Access Token";
      case FHIRplaceConstants.CLIENT_ID_DATA :
        return "Client ID";
      case FHIRplaceConstants.FHIR_ID_DATA :
        return "FHIR ID";
      case FHIRplaceConstants.PATIENT_DATA :
        return "Patient Data";
      case FHIRplaceConstants.TRANSPORT_DATA :
        return "Transport Data";
      default : 
        return null;
    }
  }

  /**
   * Writes the appropriate status file to the status directory
   * @param status_type - the type of status to be sent (e.g., ACK, NAK, etc)
   * @param fileName - the file name containing the testID
   * @param instruct - the InStructions object used to determine the appropriate identifier
   * @param trans - the Transmission object
   * @param params - the LocalParameters object, used for getting the status directory
   * @param content - the text to be written to the status file
   * @throws Exception 
   */
  public static synchronized void sendStatus(int status_type, String fileName, Instructions instruct,
                                             Transmission trans, LocalParameters params, String content)
    throws Exception {

    String statusFile = null;
    switch (status_type) {
      case FHIRplaceConstants.ACK:
        statusFile = params.getStatusDirectory() + FHIRplaceConstants.FILE_SEPARATOR + instruct.getAckID() + APPEND_POSTIVE;
        break;
      case FHIRplaceConstants.NAK:
        // Compose an ACK ID if we couldn't get one
        String ackID = instruct.getAckID();
        if (ackID == null) {
          File rName = new File(fileName);
          // The file name is in the form Req-Txxxx.xml
          // Grab the part of the file name between the "-" and the "."
          int sIdx = rName.getName().indexOf("-");
          int eIdx = rName.getName().indexOf(".");
          String tName = rName.getName().substring(sIdx+1, eIdx);
          String sendCode = FHIRplaceUtil.isSending(trans, params) ? "A" : "B" ;
          ackID = tName + "_" + sendCode + "ACK";
        }
        statusFile = params.getStatusDirectory() + FHIRplaceConstants.FILE_SEPARATOR + ackID + APPEND_NEGATIVE;
        break;
      case FHIRplaceConstants.SENT_OK:
        if (instruct.getSendID() != null)
          statusFile = params.getStatusDirectory() + FHIRplaceConstants.FILE_SEPARATOR + instruct.getSendID() + APPEND_POSTIVE;
        break;
      case FHIRplaceConstants.SENT_NOT_OK:
        if (instruct.getSendID() != null)
          statusFile = params.getStatusDirectory() + FHIRplaceConstants.FILE_SEPARATOR + instruct.getSendID() + APPEND_NEGATIVE;
        break; 
      case FHIRplaceConstants.RECEIVED_OK:
        if (instruct.getReceiveID() != null)
          statusFile = params.getStatusDirectory() + FHIRplaceConstants.FILE_SEPARATOR + instruct.getReceiveID() + APPEND_POSTIVE;
        break;
      case FHIRplaceConstants.RECEIVED_NOT_OK:
        if (instruct.getReceiveID() != null)
          statusFile = params.getStatusDirectory() + FHIRplaceConstants.FILE_SEPARATOR + instruct.getReceiveID() + APPEND_NEGATIVE;
        break;
      case FHIRplaceConstants.VERIFIED_OK :
        if (instruct.getVerifyID() != null)
          statusFile = params.getStatusDirectory() + FHIRplaceConstants.FILE_SEPARATOR + instruct.getVerifyID() + APPEND_POSTIVE;
        break;
      case FHIRplaceConstants.VERIFIED_NOT_OK :
        if (instruct.getVerifyID() != null)
          statusFile = params.getStatusDirectory() + FHIRplaceConstants.FILE_SEPARATOR + instruct.getVerifyID() + APPEND_NEGATIVE;
        break;
      case FHIRplaceConstants.RESULTS_OK:
        if (instruct.getEvaluateTestID() != null)
          statusFile = params.getStatusDirectory() + FHIRplaceConstants.FILE_SEPARATOR + instruct.getEvaluateTestID() + APPEND_POSTIVE;
        break;
      case FHIRplaceConstants.RESULTS_NOT_OK:
        String evaluateTestID = instruct.getEvaluateTestID();
        if (evaluateTestID == null) {
          File rName = new File(fileName);
          // The file name is in the form Req-Txxxx.xml
          // Grab the part of the file name between the "-" and the "."
          int sIdx = rName.getName().indexOf("-");
          int eIdx = rName.getName().indexOf(".");
          String tName = rName.getName().substring(sIdx+1, eIdx);
          String sendCode = FHIRplaceUtil.isSending(trans, params) ? "A" : "B" ;
          evaluateTestID = tName + "_" + sendCode + "VotedOK";
        }
        statusFile = params.getStatusDirectory() + FHIRplaceConstants.FILE_SEPARATOR + evaluateTestID + APPEND_NEGATIVE;
        break;
     }
     // Make sure there is a status file to open
     if (statusFile == null)
       return;

    try (FileOutputStream fout = new FileOutputStream(statusFile)) {
      if (content != null) {
        fout.write(content.getBytes());
      }
    }
  }
}
