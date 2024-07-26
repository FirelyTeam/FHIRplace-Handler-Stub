package com.dgi.fhirplace.parser;

import com.dgi.fhirplace.util.FHIRplaceUtil;
import com.google.common.base.Strings;

 /**
  * This class contains all the attributes in the Transmission
  * element of the Test Request.
  */
public class Transmission {
  String protocol = "";
    
  String senderID = null;
  String receiverID = null;
  
  String originator = null;
  String recipient = null;
  
  String originatorRole = null;
  String recipientRole = null;
  
  String fhirServer = null;
  String authServer = null;
  
  String clientJwkFileName = null;
  String clientJwkOwner = null;
  
  String bundleName = "";
  String bundleType = "";
  String bundleOwner = "";
  
  String patientResourceName = "";
  String patientResourceType = "";
  String patientResourceOwner = "";
  
  String coverageResourceName = "";
  String coverageResourceType = "";
  String coverageResourceOwner = "";
  
  String transactionDate = null;

  String testRequestID = null;

  public Transmission() {
  }
  
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public void setTestRequestID(String requestID) {
    this.testRequestID = requestID;
  }
  public void setSenderID(String senderID) {
    this.senderID = senderID;
  }
  public String getSenderID() {
    return this.senderID;
  }
  public void setReceiverID(String receiverID) {
    this.receiverID = receiverID;
  }
  public String getReceiverID() {
    return this.receiverID;
  }
  public void setOriginator(String originator){
    this.originator = originator;
  }
  public String getOriginator(){
    return this.originator;
  }
  public void setOriginatorRole(String originatorRole) {
    this.originatorRole = originatorRole;
  }
  public String getOriginatorRole() {
    return originatorRole;
  }  
  public void setRecipient(String recipient){
    this.recipient = recipient;
  }
  public String getRecipient(){
    return this.recipient;
  }
  public void setRecipientRole(String recipientRole) {
    this.recipientRole = recipientRole;
  }
  public String getRecipientRole() {
    return recipientRole;
  }
  public void setFhirServer(String fhirServer) {
    this.fhirServer = fhirServer;
  }
  public String getFhirServer() {
    return this.fhirServer;
  }
  public void setAuthorizationServer(String authServer) {
    this.authServer = authServer;
  }
  public String getAuthorizationServer() {
    return this.authServer;
  }
  public void setClientJwkFileName(String clientJwkFileName) {
    this.clientJwkFileName = clientJwkFileName;
  }
  public String getClientJwkFileName() {
    return this.clientJwkFileName;
  }
  public void setClientJwkOwner(String clientJwkOwner) {
    this.clientJwkOwner = clientJwkOwner;
  }
  public String getClientJwkOwner() {
    return this.clientJwkOwner;
  }
  public String getBundleName() {
    return bundleName;
  }
  public void setBundleName(String bundleName) {
    this.bundleName = bundleName;
  }
  public String getBundleType() {
    return bundleType;
  }
  public void setBundleType(String bundleType) {
    this.bundleType = bundleType;
  }
  public String getBundleOwner() {
    return bundleOwner;
  }
  public void setBundleOwner(String bundleOwner) {
    this.bundleOwner = bundleOwner;
  }
  public String getPatientResourceName() {
    return this.patientResourceName;
  }
  public void setPatientResourceName(String patientResourceName) {
    this.patientResourceName = patientResourceName;
  }
  public String getPatientResourceType() {
    return this.patientResourceType;
  }
  public void setPatientResourceType(String patientResourceType) {
    this.patientResourceType = patientResourceType;
  }
  public String getPatientResourceOwner() {
    return this.patientResourceOwner;
  }
  public void setPatientResourceOwner(String patienrResourceOwner) {
    this.patientResourceOwner = patienrResourceOwner;
  }
  public String getCoverageResourceName() {
    return this.coverageResourceName;
  }
  public void setCoverageResourceName(String coverageResourceName) {
    this.coverageResourceName = coverageResourceName;
  }
  public String getCoverageResourceType() {
    return this.coverageResourceType;
  }
  public void setCoverageResourceType(String coverageResourceType) {
    this.coverageResourceType = coverageResourceType;
  }
  public String getCoverageResourceOwner() {
    return this.coverageResourceOwner;
  }
  public void setCoverageResourceOwner(String coverageResourceOwner) {
    this.coverageResourceOwner = coverageResourceOwner;
  }

  public  void setDate() {
    this.transactionDate = FHIRplaceUtil.getDate();
  }
  public String getDate() {
    return this.transactionDate;
  }

  @Override
  public String toString() {
    StringBuilder transData = new StringBuilder();

    transData.append("Protocol: ").append(protocol).append("\n");

    transData.append("Sender ID: ").append(this.senderID).append("\n")
             .append("Receiver ID: ").append(this.receiverID).append("\n")
             .append("Originator: ").append(this.originator)
             .append(", Role: ").append(this.originatorRole).append("\n")
             .append("Recipient: ").append(this.recipient)
             .append(", Role: ").append(this.recipientRole).append("\n");

    if (!Strings.isNullOrEmpty(this.fhirServer)) {
      transData.append("FHIR Server: ").append(this.fhirServer).append("\n");
    }
    if (!Strings.isNullOrEmpty(this.authServer)) {
      transData.append("Authorization Server: ").append(this.authServer).append("\n");
    }
    
    if (!Strings.isNullOrEmpty(this.clientJwkFileName)) {
      transData.append("Client Jwk Filename: ").append(this.clientJwkFileName).append("\n");
    }
    if (!Strings.isNullOrEmpty(this.clientJwkOwner)) {
      transData.append("Client Jwk Owner: ").append(this.clientJwkOwner).append("\n"); 
    }
    if (!Strings.isNullOrEmpty(this.bundleName)) {   
      transData.append("Bundle Name: ").append(this.bundleName).append("\n");
    }
    if (!Strings.isNullOrEmpty(this.bundleType)) {
      transData.append("Bundle Type: ").append(this.bundleType).append("\n");
    }
    if (!Strings.isNullOrEmpty(this.bundleOwner)) {
      transData.append("Bundle Owner: ").append(this.bundleOwner).append("\n");
    }
    if (!Strings.isNullOrEmpty(this.patientResourceName)) {
      transData.append("Patient Resource Name: ").append(this.patientResourceName).append("\n");
    }
    if (!Strings.isNullOrEmpty(this.patientResourceType)) {
      transData.append("Patient Resource Type: ").append(this.patientResourceType).append("\n");
    }
    if (!Strings.isNullOrEmpty(this.patientResourceOwner)) {
      transData.append("Patient Resource Owner: ").append(this.patientResourceOwner).append("\n");
    }
    
    if (!Strings.isNullOrEmpty(this.coverageResourceName)) {
      transData.append("Coverage Resource Name: ").append(this.coverageResourceName).append("\n");
    }
    if (!Strings.isNullOrEmpty(this.coverageResourceType)) {
      transData.append("Coverage Resource Type: ").append(this.coverageResourceType).append("\n");
    }
    if (!Strings.isNullOrEmpty(this.coverageResourceOwner)) {
      transData.append("Coverage Resource Owner: ").append(this.coverageResourceOwner).append("\n");
    }

    transData.append("\n");

    return transData.toString();
  }

}
