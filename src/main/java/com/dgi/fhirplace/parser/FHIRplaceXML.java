package com.dgi.fhirplace.parser;

import java.util.ArrayList;

/**
 * This class represents an FHIRplace XML container with the parsed elements from
 * that file:  
 *    Description - describes the test
 *    Participant - two objects, one for the sender and one for the receiver
 *    Transmission - contains information about the connection 
 *    Instructions - describes all the artifacts that need to be uploaded
 */
public class FHIRplaceXML {
  Description desc = null;
  ArrayList<Participant> parts = new ArrayList<>();
  Transmission trans = null;
  Instructions instruct = null;

  String testRequestID = null;
  
  public FHIRplaceXML() {
  }
  
  public void setTestRequestID(String testRequestID) {
    this.testRequestID = testRequestID;
  }
  
  public void setDescription(Description desc) {
    this.desc = desc;
    this.testRequestID = desc.getTestRequestID();
  }
  public Description getDescription() {
    return this.desc;
  }
  
  public void setParticipant(Participant part) {
    parts.add(part);
  }
  public Participant[] getParticipant() {
    return (Participant[])parts.toArray(new Participant[0]);
  }
  
  public void setTransmission(Transmission trans) {
    this.trans = trans;
    this.trans.setTestRequestID(testRequestID);
  }
  public Transmission getTransmission() {
    return this.trans;
  }
  
  public void setInstructions(Instructions instruct) {
    this.instruct = instruct;
  }
  public Instructions getInstructions() {
    return this.instruct;
  } 
}
