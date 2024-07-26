package com.dgi.fhirplace.handler;

import com.dgi.fhirplace.util.FHIRplaceUtil;

/**
 * FHIRplace Handler - Monitors shared directory, parses test requests 
 * and processes the FHIR test results
 */
public class FHIRplaceHandler {

  Logger log = new Logger(FHIRplaceHandler.class);
  LocalParameters params = null;
  ProcessIncomingMessages incomingMsgProcessor = null;
  
  public FHIRplaceHandler() {
    params = new LocalParameters();
    params.getProperties();
    params.dumpValues();  
    
    // Make sure that all the required directories exist on disk
    FHIRplaceUtil.checkDirectories(params);
  }
  
  public void start() {
    // Perform any necessary set up here...
    log.write("Using XML Identifier: " + params.getXMLIdentifier() + "\n");
    
    // Poll the test request directory and process incoming test request messages
    incomingMsgProcessor = new ProcessIncomingMessages(this, params);
    incomingMsgProcessor.start();
  }
  
  public void stop() {
    incomingMsgProcessor.interrupt();
  }
  
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    FHIRplaceHandler fhirPlaceHandler = new FHIRplaceHandler();
    fhirPlaceHandler.start();
  }
  
}
