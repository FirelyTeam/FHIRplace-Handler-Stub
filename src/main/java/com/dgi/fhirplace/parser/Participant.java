package com.dgi.fhirplace.parser;

/**
 * This class contains all the attributes in the Participant element
 * element of the Test Request.
 */
public class Participant {
  String participantID = null;
  String name = null;
  String product = null;
  String version = null;
  String fhirVersion = null;

  public Participant() {
  }

  public void setParticipantID(String ID) {
    this.participantID = ID;
  }
  public String getParticipantID() {
    return this.participantID;
  }

  public void setName(String name) {
    this.name = name;
  }
  public String getName() {
    return this.name;
  }

  public void setProduct(String product) {
    this.product = product;
  }
  public String getProduct() {
    return this.product;
  }

  public void setVersion(String Version) {
    this.version = Version;
  }
  public String getVersion() {
    return this.version;
  }
  
  public void setFhirVersion(String fhirVersion) {
    this.fhirVersion = fhirVersion;
  }
  public String getFhirVersion() {
    return this.fhirVersion;
  }

  @Override
  public String toString() {
    StringBuilder partString = new StringBuilder();
    partString.append("\n");
    partString.append("Participant ID: ").append(this.participantID).append("\n");
    partString.append("Name: ").append(this.name).append("\n");
    partString.append("Product: ").append(this.product).append("\n");
    partString.append("Version: " ).append(this.version).append("\n");
    partString.append("FHIR Version: " ).append(this.fhirVersion).append("\n");

    return partString.toString();
  }
}
