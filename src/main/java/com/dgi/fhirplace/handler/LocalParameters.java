package com.dgi.fhirplace.handler;

import com.dgi.fhirplace.util.FHIRplaceUtil;
import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class contains all the configuration parameters needed to run the application
 * You may want to put a small GUI in front of this or just edit a configuration file
 * containing all the necessary properties.
 */
public class LocalParameters {
  
   Logger log = new Logger(LocalParameters.class); 
  
  // So we can test multiple instances, always use the current directory
  String directory = ".";
  String configFile = "fhirplace-clientconfig.properties";

  static String TEST_REQUEST_DIR_PROP = "com.dgi.testrequestdir";
  static String STATUS_DIR_PROP = "com.dgi.statusdir";
  static String ARCHIVE_DIR_PROP = "com.dgi.archivedir";
  static String PAYLOAD_DIR_PROP = "com.dgi.payloaddir";
  static String XML_IDENTIFIER_PROP = "com.dgi.participantid";
   
  String testRequestDirectory = null;
  String statusDirectory = null;
  String archiveDirectory = null;
  String payloadDirectory = null;
  String xmlUser = null;
  
  boolean cancelledTestRequest = false;
  
  // For debugging purposes
  boolean debugMode = true;
  
  // Setting to remove payload files from the FHIRplace payload directory
  boolean deletePayload = false;

  ConcurrentHashMap cancelledTestRequests = new ConcurrentHashMap();
  ConcurrentHashMap activeTests = new ConcurrentHashMap();
  ConcurrentHashMap activeResults = new ConcurrentHashMap();
  
  public void getProperties() {
    // Read the FHIRplace configuration file and get the propeties from it
    Properties p = new Properties();
    File f = new File(directory, configFile);
    FileInputStream in = null;
    if (f.exists()) {
      try {
        in = new FileInputStream(f);
        p.load(in);
 
        // Load the directory names from the configuarion properties
        testRequestDirectory = p.getProperty(TEST_REQUEST_DIR_PROP);
        statusDirectory = p.getProperty(STATUS_DIR_PROP);
        archiveDirectory = p.getProperty(ARCHIVE_DIR_PROP);
        payloadDirectory = p.getProperty(PAYLOAD_DIR_PROP);
        xmlUser = p.getProperty(XML_IDENTIFIER_PROP);
                              
      } catch (Exception ex) {
        log.writeStackTrace(ex);
      } finally {
        if (in != null) {
          try {
            in.close();
          } catch(Exception ex2) {
            log.writeStackTrace("Caught exception while closing properties stream: ", ex2);
          }
        }
      }
    }
  }

  public boolean isDebugMode() {
    return this.debugMode;
  }
  public boolean isDeletePayload() {
    return this.deletePayload;
  }

  // Getters for the property values  
  public String getTestRequestDirectory() {
    return this.testRequestDirectory;
  }
  public String getStatusDirectory() {
    return this.statusDirectory;
  }
  public String getArchiveDirectory() {
    return this.archiveDirectory;
  }
  public String getPayloadDirectory() {
    return this.payloadDirectory;
  }
  public String getXMLIdentifier() {
    return xmlUser;
  }
  
  public void dumpValues() {
    log.write("Test Request Directory: " + this.testRequestDirectory);
    log.write("Status Directory:       " + this.statusDirectory);
    log.write("Archive Directory:      " + this.archiveDirectory);
    log.write("Payload Directory:      " + this.payloadDirectory);
    log.write("XML Identifier:         " + this.xmlUser);
  }

  public ConcurrentHashMap getCancelledTestRequests() {
    return this.cancelledTestRequests;
  }  
  public void setCancelledTestRequest(boolean cancelledTestRequest) {
    this.cancelledTestRequest = cancelledTestRequest;
  }
  public boolean getCancelledTestRequest() {
    return this.cancelledTestRequest;
  }
  public boolean isCancelledTestRequest(String testRequest) {
    return this.cancelledTestRequests.containsKey(testRequest);
  }
  public synchronized void addCancelledTestRequest(String testRequest) {
    if (this.isActiveTest(testRequest)) {
      if (this.cancelledTestRequests.isEmpty() || !this.cancelledTestRequests.containsKey(testRequest)) {
        this.cancelledTestRequests.put(testRequest, testRequest);
      }
    }
    this.removeActiveTest(testRequest);
  }
  public synchronized void removeCancelledTestRequest(String testRequest) {
    if (!this.cancelledTestRequests.isEmpty() && this.cancelledTestRequests.containsKey(testRequest)) {
       this.cancelledTestRequests.remove(testRequest);
    }
  }
  public synchronized void removeAllCancelledTestRequests() {
    if (!this.cancelledTestRequests.isEmpty()) {
      Enumeration c = this.cancelledTestRequests.keys();
      while (c.hasMoreElements()) {
        Object key = c.nextElement();
        this.cancelledTestRequests.remove(key);
      }
    }
  }
  public synchronized void addActiveTest(String testRequest) {
    if (this.activeTests.isEmpty() || !this.activeTests.containsKey(testRequest)) {
      this.activeTests.put(testRequest, "");
    }
  }
  public synchronized void setPartnerForActiveTest(String testRequest, String partner) {
    if (this.activeTests.containsKey(testRequest)) {
       this.activeTests.remove(testRequest);
       this.activeTests.put(testRequest, partner);
    }           
  }
  public String getPartnerAlreadyActiveTest(String purpose, String testRequestID, String partner) {
    // Check for partner (host) in any already active tests
    Enumeration c = this.activeTests.keys();
    while (c.hasMoreElements()) {
      String testID = c.nextElement().toString();
      String partnerValue = this.activeTests.get(testID).toString();
      // A CancelTestRequest will have the same testRequestID so allow that to exist
      if (purpose.equalsIgnoreCase("TestRequest") && partnerValue.startsWith(partner) && !testRequestID.equals(testID))
        return testID;
    }
    return null;
  }
  public String getPartnerForActiveTest(String testRequest) {
    if (this.activeTests.containsKey(testRequest))
      return this.activeTests.get(testRequest).toString();
    else 
      return null;
  }
  public synchronized void removeActiveTest(String testRequest) {
     if (!this.activeTests.isEmpty() && this.activeTests.containsKey(testRequest)) {
       this.activeTests.remove(testRequest);
       if (ProcessIncomingMessages.testRequestTable.containsKey(testRequest))
         ProcessIncomingMessages.testRequestTable.remove(testRequest);
     }
  }
  public synchronized void removeAllActiveTests() {
    if (!this.activeTests.isEmpty()) {
      Enumeration c = this.activeTests.keys();
      while (c.hasMoreElements()) {
        Object key = c.nextElement();
        this.activeTests.remove(key);
        if (ProcessIncomingMessages.testRequestTable.containsKey(key))
          ProcessIncomingMessages.testRequestTable.remove(key);
        this.addCancelledTestRequest((String)key);
      }
    }
  }
  public boolean isActiveTest(String testRequest) {
    return this.activeTests.containsKey(testRequest);
  }
  public ConcurrentHashMap getActiveTests() {
    return this.activeTests;
  }
}
