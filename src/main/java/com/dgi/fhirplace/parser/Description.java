package com.dgi.fhirplace.parser;

/**
 * This class contains all the attributes in the Description 
 * element of the Test Request.
 *
 */
public class Description {
  String testDescription = null;
  String testRequestID = null;
  String version = null;
  String protocol = null;
  String purpose = null;
  String testCase = null;
  String testCaseType = null;
  String expectedResult = null;
  String testName = null;
  String testPurpose = null;
  String testPhase = null;
  String date = null;
  String time = null;

  public Description() {
  }

  public void setTestDescription(String testDescription) {
    this.testDescription = testDescription;
  }
  public String getTestDescription() {
    return this.testDescription;
  }
  public void setTestRequestID(String testRequestID) {
    this.testRequestID = testRequestID;
  }
  public String getTestRequestID() {
    return this.testRequestID;
  }
  public void setVersion(String version) {
    this.version = version;
  }
  public String getversion() {
    return this.version;
  }
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }
  public String getProtocol() {
    return this.protocol.toUpperCase();
  }
  public void setPurpose(String purpose) {
    this.purpose = purpose;
  }
  public String getPurpose() {
    return this.purpose;
  }
  public void setTestCase(String testCase) {
    this.testCase = testCase;
  }
  public String getTestCase() {
    return this.testCase;
  }
  public void setTestCaseType(String testCaseType) {
    this.testCaseType = testCaseType;
  }
  public String getTestCaseType() {
    return this.testCaseType;
  }
  public void setExpectedResult(String expectedResult) {
    this.expectedResult = expectedResult;
  }
  public String getExpectedResult() {
    return this.expectedResult;
  }
  public void setTestName(String testName) {
    this.testName = testName;
  }
  public String getTestName() {
    return this.testName;
  }
  public void setTestPurpose(String testPurpose) {
    this.testPurpose = testPurpose;
  }
  public String getTestPurpose() {
    return this.testPurpose;
  }
  public void setTestPhase(String testPhase) {
    this.testPhase = testPhase;
  }
  public String getTestPhase() {
    return this.testPhase;
  }
  public void setDate(String date) {
    this.date = date;
  }
  public String getDate() {
    return this.date;
  }
  public void setTime(String time) {
    this.time = time;
  }
  public String getTime() {
    return this.time;
  }

  @Override
  public String toString() {
    StringBuilder descrip = new StringBuilder();
    descrip.append("\n");
    
    if (this.testDescription != null)
      descrip.append("Test Description: ").append(this.testDescription).append("\n\n");

    descrip.append("TestRequestID: ").append(this.testRequestID).append("\n")
           .append("Version: ").append(this.version).append("\n")
           .append("Protocol: ").append(this.protocol).append("\n")
           .append("Purpose: ").append(this.purpose).append("\n\n");

    descrip.append("TestCase: ").append(this.testCase).append("\n")
           .append("TestCaseType: ").append(this.testCaseType).append("\n")
           .append("Expected Result: ").append(this.expectedResult).append("\n")
           .append("TestName: ").append(this.testName).append("\n")
           .append("TestPurpose: ").append(this.testPurpose).append("\n")
           .append("TestPhase: ").append(this.testPhase).append("\n\n");

    descrip.append("Test Date: ").append(this.date).append("\n")
           .append("Test Time: ").append(this.time).append("\n");

    return descrip.toString();
  }
}
