package com.dgi.fhirplace.parser;

import java.util.Vector;
import com.dgi.fhirplace.handler.FHIRplaceConstants;

/**
 * This class contains all the attributes in the Instructions
 * element of the Test Request.
 */
public class Instructions { 
  String ackID = null;
  String sendID = null;
  String receiveID = null;
  
  String sendFhirID = null;
  String receiveFhirID = null;
  String sendAccessTokenID = null;
  String receiveAccessTokenID = null;

  String sendPdexRequestID = null;
  String receivePdexRequestID = null;
  String sendPdexResourceID = null;
  String receivePdexResourceID = null;
          
  String sendPatientRequestID = null;
  String receivePatientRequestID = null;  
  String sendPatientDataID = null;
  String receivePatientDataID = null;  
  
  String accessTokenVerifyID = null;
  String clientIDVerifyID = null;
  String fhirIDVerifyID = null;
  String pdexResourceVerifyID = null;
  String patientDataVerifyID = null;

  String evaluateTestID = null;

  String sendDataType = null;
  String receiveDataType = null;

  // Contains all the upload instructions
  Vector uploadVector = new Vector();

  public Instructions() {
  }

  public void addUpload(String ID, int type, int direction) {
    Upload upload = new Upload();
    upload.setResponseID(ID);
    upload.setType(type);
    upload.setDirection(direction);
    uploadVector.add(upload);
  }
  public void addUpload(Object upload) {
    uploadVector.add(upload);
  }

  public Upload[] getUpload() {
    Upload[] upload = new Upload[uploadVector.size()];
    for (int i=0; i < uploadVector.size(); i++) {
      upload[i] = (Upload) uploadVector.get(i);
    }
    return upload;
  }

  public Upload[] getUpload(int direction, int type) {
    // Finds all upload instructions matching the direction and type
    Vector tempVector = new Vector();
    for (int i=0; i < uploadVector.size(); i++) {
      Upload thisUpload = (Upload) uploadVector.get(i);
      if (thisUpload.getDirection() == direction && thisUpload.getType() == type) {
        tempVector.add(thisUpload);
      }
    }
    Upload[] upload = new Upload[tempVector.size()];
    for (int i=0; i < tempVector.size(); i++) {
      upload[i] = (Upload) tempVector.get(i);
    }
    return upload;
  }

  public Upload findUploadRecord(String ID, int type, int direction) {
    for (int i = 0; i < uploadVector.size(); i++) {
      Upload upload = (Upload)uploadVector.get(i);
      if ((upload.responseID.equals(ID)) &&
          (upload.type == type) &&
          (upload.direction == direction)) {

        return upload; // found a match
      }
    }
    return null;
  }

  // Set and Get methods
  public void setAckID(String ackID) {
    this.ackID = ackID;
  }
  public String getAckID() {
    return this.ackID;
  }

  public void setSendID(String sendID, String dataType) {
    switch (dataType) {
      case FHIRplaceConstants.ACCESS_TOKEN_TYPE :
        this.sendAccessTokenID = sendID;
        break;
      case FHIRplaceConstants.FHIR_ID_TYPE :
        this.sendFhirID = sendID;
        break;
      case FHIRplaceConstants.PDEX_REQUEST_TYPE :
        this.sendPdexRequestID = sendID;
        break;
      case FHIRplaceConstants.PDEX_RESOURCE_TYPE :
        this.sendPdexResourceID = sendID;
        break;
      case FHIRplaceConstants.PATIENT_REQUEST_TYPE :
        this.sendPatientRequestID = sendID;
        break;
      case FHIRplaceConstants.PATIENT_DATA_TYPE :
        this.sendPatientDataID = sendID;
        break;
      default :
        this.sendID = sendID;
    }
  }
  public String getSendID() {
    return this.sendID;
  }
  public String getSendFhirID() {
    return this.sendFhirID;
  }
  public String getSendAccessTokenID() {
    return this.sendAccessTokenID;
  }

  public String getSendPdexRequestID() {
    return sendPdexRequestID;
  }
  public String getSendPdexResourceID() {
    return sendPdexResourceID;
  }
  public String getSendPatientRequestID() {
    return sendPatientRequestID;
  }
  public String getSendPatientDataID() {
    return this.sendPatientDataID;
  }

  public void setSendDataType(String sendDataType) {
    this.sendDataType = sendDataType;
  }
  public String getSendDataType() {
    return this.sendDataType;
  }

  public void setReceiveID(String receiveID, String dataType) {
    switch (dataType) {
      case FHIRplaceConstants.ACCESS_TOKEN_TYPE :
        this.receiveAccessTokenID = receiveID;
        break;
      case FHIRplaceConstants.FHIR_ID_TYPE :
        this.receiveFhirID = receiveID;
        break;
      case FHIRplaceConstants.PDEX_REQUEST_TYPE :
        this.receivePdexRequestID = receiveID;
        break;
      case FHIRplaceConstants.PDEX_RESOURCE_TYPE :
        this.receivePdexResourceID = receiveID;
        break;
      case FHIRplaceConstants.PATIENT_REQUEST_TYPE :
        this.receivePatientRequestID = receiveID;
        break;
      case FHIRplaceConstants.PATIENT_DATA_TYPE :
        this.receivePatientDataID = receiveID;
        break;
      default :
        this.receiveID = receiveID;
    }
  }
  public String getReceiveID() {
    return this.receiveID;
  }
  public String getReceiveFhirID() {
    return this.receiveFhirID;
  }
  public String getReceiveAccessTokenID() {
    return this.receiveAccessTokenID;
  }

  public String getReceivePdexRequestID() {
    return receivePdexRequestID;
  }
  public String getReceivePdexResourceID() {
    return receivePdexResourceID;
  }
  public String getReceivePatientRequestID() {
    return receivePatientRequestID;
  }
  public String getReceivePatientDataID() {
    return this.receivePatientDataID;
  }
  
  public void setReceiveDataType(String receiveDataType) {
    this.receiveDataType = receiveDataType;
  }
  public String getReceiveDataType() {
    return this.receiveDataType;
  }

  public void setAccessTokenVerifyID(String accessTokenVerifyID) {
    this.accessTokenVerifyID = accessTokenVerifyID;
  }
  public String getAccessTokenVerifyID() {
    return this.accessTokenVerifyID;
  }

  public void setClientIDVerifyID(String clientIDVerifyID) {
    this.clientIDVerifyID = clientIDVerifyID;
  }
  public String getClientIDVerifyID() {
    return clientIDVerifyID;
  }

  public void setFhirIDVerifyID(String fhirIDVerifyID) {
    this.fhirIDVerifyID = fhirIDVerifyID;
  }
  public String getFhirIDVerifyID() {
    return this.fhirIDVerifyID;
  }

  public void setPdexResourceVerifyID(String pdexResourceVerifyID) {
    this.pdexResourceVerifyID = pdexResourceVerifyID;
  }
  public String getPdexResourceVerifyID() {
    return pdexResourceVerifyID;
  }

  public void setPatientDataVerifyID(String patientDataVerifyID) {
    this.patientDataVerifyID = patientDataVerifyID;
  }
  public String getPatientDataVerifyID() {
    return patientDataVerifyID;
  }
  
  public void setEvaluateTestID(String evaluateTestID){
    this.evaluateTestID = evaluateTestID;
  }
  public String getEvaluateTestID() {
    return this.evaluateTestID;
  }
  
  @Override
  public String toString() {
    StringBuilder instrBuf = new StringBuilder();

    instrBuf.append("Ack ID: ").append(this.ackID).append("\n");    

    instrBuf.append("Send ID: ").append(this.sendID != null ? this.sendID : "<Not set>").append("\n");
    instrBuf.append("Send FHIR ID: ").append(this.sendFhirID != null ? this.sendFhirID : "<Not set>").append("\n");
    instrBuf.append("Send Access Token ID: ").append(this.sendAccessTokenID != null ? this.sendAccessTokenID : "<Not set>").append("\n");
    instrBuf.append("Send PDEX Request ID: ").append(this.sendPdexRequestID != null ? this.sendPdexRequestID : "<Not set>").append("\n");
    instrBuf.append("Send PDEX Resource ID: ").append(this.sendPdexResourceID != null ? this.sendPdexResourceID : "<Not set>").append("\n");
    instrBuf.append("Send Patient Request ID: ").append(this.sendPatientRequestID != null ? this.sendPatientRequestID : "<Not set>").append("\n");
    instrBuf.append("Send Patient Data ID: ").append(this.sendPatientDataID != null ? this.sendPatientDataID : "<Not set>").append("\n");  

    instrBuf.append("Send DataType: ").append(this.sendDataType).append("\n\n");

    instrBuf.append("Receive ID: ").append(this.receiveID != null ? this.receiveID : "<Not set>").append("\n");
    instrBuf.append("Receive FHIR ID: ").append(this.receiveFhirID != null ? this.receiveFhirID : "<Not set>").append("\n");
    instrBuf.append("Receive Access Token ID: ").append(this.receiveAccessTokenID != null ? this.receiveAccessTokenID : "<Not set>").append("\n");
    instrBuf.append("Receive PDEX Request ID: ").append(this.receivePdexRequestID != null ? this.receivePdexRequestID : "<Not set>").append("\n");
    instrBuf.append("Receive PDEX Resource ID: ").append(this.receivePdexResourceID != null ? this.receivePdexResourceID : "<Not set>").append("\n");
    instrBuf.append("Receive Patient Request ID: ").append(this.receivePatientRequestID != null ? this.receivePatientRequestID : "<Not set>").append("\n");
   instrBuf.append("Receive Patient Data ID: ").append(this.receivePatientDataID != null ? this.receivePatientDataID : "<Not set>").append("\n");

    instrBuf.append("Receive DataType: ").append(this.receiveDataType).append("\n\n"); 

    Upload[] uploadInstructions = this.getUpload();
    StringBuilder uploadBuf = new StringBuilder();

    for (Upload uploadInstruction : uploadInstructions) {
      uploadBuf.append(uploadInstruction.toString());
    }

    instrBuf.append("Upload Instructions:\n")
            .append(uploadBuf.toString()).append("\n");
    
    instrBuf.append("Access Token Verification ID: ").append(this.accessTokenVerifyID != null ? this.accessTokenVerifyID : "<Not set>").append("\n");
    instrBuf.append("Client ID Verification ID: ").append(this.clientIDVerifyID != null ? this.clientIDVerifyID : "<Not set>").append("\n");
    instrBuf.append("FHIR ID Verification ID: ").append(this.fhirIDVerifyID != null ? this.fhirIDVerifyID : "<Not set>").append("\n");
    instrBuf.append("PDEX Resource Verification ID: ").append(this.pdexResourceVerifyID != null ? this.pdexResourceVerifyID : "<Not set>").append("\n");
    instrBuf.append("Patient Data Verification ID: ").append(this.pdexResourceVerifyID != null ? this.pdexResourceVerifyID : "<Not set>").append("\n");
    instrBuf.append("EvaluateTest ID: ").append(this.evaluateTestID != null ? this.evaluateTestID : "<Not set>").append("\n\n");

    return instrBuf.toString();
  }

  // This class holds the information for an "Upload" instruction
  public class Upload {
    String responseID = "";     // Identifier of the upload
    int type = -1;              // AccessToken = 0, FHIR-responseID = 1, PatientData = 2
    int direction;              // Either sent=0 or received=1 (specified as a FHIRplace Constant)

   // Set methods
    public void setResponseID(String responseID) {
      if (responseID == null)
        responseID = "";
      this.responseID = responseID;
    }
    public void setType(int type) {
      this.type = type;
    }
    public void setDirection(int direction) {
      this.direction = direction;
    }

    // Get methods
    public String getResponseID() {
      return this.responseID;
    }
    public int getType() {
      return this.type;
    }
    public int getDirection() {
      return this.direction;
    }

    @Override
    public String toString() {
      StringBuilder uploadBuf = new StringBuilder();

      String dataType = null;
      switch (this.type) {
        case FHIRplaceConstants.ACCESS_TOKEN_DATA:
          dataType = "Access Token";
          break;
        case FHIRplaceConstants.CLIENT_ID_DATA:
          dataType = "Client ID";
          break;          
        case FHIRplaceConstants.FHIR_ID_DATA:
          dataType = "FHIR ID";
          break;
        case FHIRplaceConstants.PATIENT_DATA:
          dataType = "Patient Data";
          break;
        case FHIRplaceConstants.TRANSPORT_DATA:
          dataType = "Transport Data";
          break;          
          
        default:
          break;
      }

      String dataDirection = null;
      switch (this.direction) {
        case FHIRplaceConstants.SENT_DATA:
          dataDirection = "Sent";
        break;

        case FHIRplaceConstants.RECEIVED_DATA:
          dataDirection = "Received";
        break;

        default:
          break;
      }

      uploadBuf.append("Response ID: ").append(this.responseID)
               .append(", Type: ").append(type).append(" (").append(dataType).append(")")
               .append(", Direction: ").append(direction).append(" (").append(dataDirection).append(")");

      uploadBuf.append("\n");

      return uploadBuf.toString();
    }
  }

}

