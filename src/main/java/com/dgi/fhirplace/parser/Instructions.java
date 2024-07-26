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
  String verifyID = null;
  String evaluateTestID = null;

  String sendDataType = null;
  String receiveDataType = null;
  String verificationType = null;

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
  public void setSendID(String sendID) {
    this.sendID = sendID;
  }
  public String getSendID() {
    return this.sendID;
  }
  public void setSendDataType(String sendDataType) {
    this.sendDataType = sendDataType;
  }
  public String getSendDataType() {
    return this.sendDataType;
  }

  public void setReceiveID(String receiveID) {
    this.receiveID = receiveID;
  }
  public String getReceiveID() {
    return this.receiveID;
  }
  public void setReceiveDataType(String receiveDataType) {
    this.receiveDataType = receiveDataType;
  }
  public String getReceiveDataType() {
    return this.receiveDataType;
  }

  public void setVerifyID(String verifyID, String verificationType) {
    this.verifyID = verifyID;
    this.verificationType = verificationType;
  }
  public String getVerifyID() {
    return this.verifyID;
  }
  public String getVerificationType() {
    return this.verificationType;
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

    instrBuf.append("Ack ID: ").append(this.ackID).append("\n\n");    

    if (this.sendID != null) {
      instrBuf.append("Send ID: ").append(this.sendID).append("\n");
    }
    if (this.sendDataType != null) {
      instrBuf.append("Send DataType: ").append(this.sendDataType).append("\n\n");
    }
    if (this.receiveID != null) {
      instrBuf.append("Receive ID: ").append(this.receiveID).append("\n");
    }
    if (this.receiveDataType != null) {
      instrBuf.append("Receive DataType: ").append(this.receiveDataType).append("\n\n"); 
    }

    Upload[] uploadInstructions = this.getUpload();
    StringBuilder uploadBuf = new StringBuilder();

    for (Upload uploadInstruction : uploadInstructions) {
      uploadBuf.append(uploadInstruction.toString());
    }
    instrBuf.append("Upload Instructions:\n")
            .append(uploadBuf.toString()).append("\n");

    if (this.verifyID != null) {
      instrBuf.append("Verify ID: ").append(this.verifyID).append("\n");
    }
    if (this.verificationType != null) {
      instrBuf.append("Verification Type: ").append(this.verificationType).append("\n");
    }
    if (this.evaluateTestID != null) {
      instrBuf.append("EvaluateTest ID: ").append(this.evaluateTestID).append("\n");
    }

    return instrBuf.toString();
  }

  // This class holds the information for an "Upload" instruction
  public class Upload {
    String responseID = "";     // Identifier of the upload
    int type = -1;              // AccessToken = 0, ClientID = 1, FHIR-ID = 2, PatientData = 3, Transport = 4 
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