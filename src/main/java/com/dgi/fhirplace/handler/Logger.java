package com.dgi.fhirplace.handler;

import com.dgi.fhirplace.util.FHIRplaceUtil;

/**
 * This class is a wrapper for logging messages
 * You may incorporate your own logging utility here and or just
 * use this class as-is to log to the console.
 * 
 * Additionally, if desired, you may direct all log output to a file.
 */
public class Logger {
  private Class clazz;
  public Logger(Class clazz) {
    this.clazz = clazz;
  }
  
  /**
   * Output the specified 
   * @param text 
   */
  public void write(String text) {
    String date = FHIRplaceUtil.getPrettyDate();
    System.out.println(date + " (" + clazz.getSimpleName() + "): "  + text);
  }
  
  public void writeStackTrace(Exception ex) {
    String date = FHIRplaceUtil.getPrettyDate();
    String stackTrace = FHIRplaceUtil.getStackTrace(ex);
    System.out.println(date + " (" + clazz.getSimpleName() + ")\nStack Trace: " + stackTrace);
  }

  public void writeStackTrace(Exception ex, String testRequestID) {
    String date = FHIRplaceUtil.getPrettyDate();
    String stackTrace = FHIRplaceUtil.getStackTrace(ex);
    System.out.println(date + " (" + clazz.getSimpleName() + ") - Test Request - " + testRequestID + "\nStack trace: " + stackTrace);
  }
  
  public void writeStackTrace(String description, Exception ex) {
    String date = FHIRplaceUtil.getPrettyDate();
    String stackTrace = FHIRplaceUtil.getStackTrace(ex);
    System.out.println(date + " (" + clazz.getSimpleName() + ") - " + description + ": " + stackTrace);
  }
  
  public void writeStackTrace(String description, Exception ex, String testRequestID) {
    String date = FHIRplaceUtil.getPrettyDate();
    String stackTrace = FHIRplaceUtil.getStackTrace(ex);
    System.out.println(date + " (" + clazz.getSimpleName() + ") - Test Request - " + testRequestID  +  description + ": " + stackTrace);
  }
}
