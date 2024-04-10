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
  
  String bundleName = "";
  String bundleType = "";
  String bundleOwner = "";
  
  String patientResourceName = "";
  String patientResourceType = "";
  String patienrResourceOwner = "";
  
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
    return patientResourceName;
  }
  public void setPatientResourceName(String patientResourceName) {
    this.patientResourceName = patientResourceName;
  }
  public String getPatientResourceType() {
    return patientResourceType;
  }
  public void setPatientResourceType(String patientResourceType) {
    this.patientResourceType = patientResourceType;
  }
  public String getPatientResourceOwner() {
    return patienrResourceOwner;
  }
  public void setPatientResourceOwner(String patienrResourceOwner) {
    this.patienrResourceOwner = patienrResourceOwner;
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
    
    transData.append("Bundle Name: ").append(this.bundleName).append("\n")
             .append("Bundle Type: ").append(this.bundleType).append("\n")
             .append("Bundle Owner: ").append(this.bundleOwner).append("\n");
    
    transData.append("Patient Resource Name: ").append(!Strings.isNullOrEmpty(this.patientResourceName) ? this.patientResourceName : "Not set").append("\n")
             .append("Patient Resource Type: ").append(!Strings.isNullOrEmpty(this.patientResourceType) ? this.patientResourceType : "Not set").append("\n")
             .append("Patient Resource Owner: ").append(!Strings.isNullOrEmpty(this.patienrResourceOwner) ? this.patienrResourceOwner : "Not set").append("\n");

    // This is set while the test is running
    //transData.append("Transaction Date: ").append(this.transactionDate)
    
    transData.append("\n\n");

    return transData.toString();
  }

}
