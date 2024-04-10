package com.dgi.fhirplace.util;

import com.dgi.fhirplace.handler.LocalParameters;
import com.dgi.fhirplace.handler.Logger;
import com.dgi.fhirplace.handler.FHIRplaceConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * File Utility Classes
 * 
 */
public class FileUtility {
  Logger log = new Logger(FileUtility.class);
  
 /**
   * Move the specified file (obtained from the FHIRplace Server) to the archive directory
   * 
   * @param fileName file to move
   * @param params params object used to get the archive directory
   * 
   * @throws Exception 
   */
  public void moveFileToArchiveDirectory(String fileName, LocalParameters params)
    throws Exception {

    File thisFile = new File(fileName);

    // Just return if the file no longer exists
    if (!thisFile.exists())
      return;

    // Open the source and destination files
    FileOutputStream fout;
    try ( 
      FileInputStream fin = new FileInputStream(thisFile)) {
      fout = new FileOutputStream(params.getArchiveDirectory() + FHIRplaceConstants.FILE_SEPARATOR + thisFile.getName());
      // Write the contents of the source to the destination
      int numRead;
      byte[] buf = new byte[1024];
      while ((numRead = fin.read(buf)) > 0) {
        fout.write(buf, 0, numRead);
      }
    }
    fout.close();

    // Attempt to delete the source
    if (!thisFile.delete()) {
      // File couldn't be removed.  Wait a second and try for a full minute.
      int count = 0;
      while (count++ < 60) {
        FHIRplaceUtil.wait(1);
        if (thisFile.delete())
          return;
      }

      // If we get here, it's because the file couldn't be removed
      log.write("Error: Could not delete " + fileName + "...");
    }
  }

  /**
   * Upload the data to the status directory
   * 
   * @param direction either sending or receiving
   * @param dataType either Access Token, FHIR ID or Patient Data
   * @param content the string content to upload
   * @param testRequestID the testRequest ID
   * @param uploadID the name of the destination file
   * @param destinationDirectory the name of the destination directory
   * 
   * @throws Exception 
   */
  public void uploadData(int direction, int dataType, String content,
                         String testRequestID, String uploadID, 
                         String destinationDirectory)
    throws Exception {

    String statusFile = null;

    if (direction == FHIRplaceConstants.SENT_DATA) {
      switch(dataType) {
        case FHIRplaceConstants.ACCESS_TOKEN_DATA :
        case FHIRplaceConstants.CLIENT_ID_DATA :
        case FHIRplaceConstants.FHIR_ID_DATA :
        case FHIRplaceConstants.PATIENT_DATA :
        case FHIRplaceConstants.TRANSPORT_DATA :
          statusFile = destinationDirectory + FHIRplaceConstants.FILE_SEPARATOR + uploadID + ".uld";
          break;       

        default:
          // Should never get here
          log.write("Invalid upload data type (" + dataType + ") for sent data - (" + testRequestID + ")");
          break;
      }
    } else {
      switch(dataType) {
        case FHIRplaceConstants.ACCESS_TOKEN_DATA :
        case FHIRplaceConstants.CLIENT_ID_DATA :
        case FHIRplaceConstants.FHIR_ID_DATA :
        case FHIRplaceConstants.PATIENT_DATA :
        case FHIRplaceConstants.TRANSPORT_DATA :
          statusFile = destinationDirectory + FHIRplaceConstants.FILE_SEPARATOR + uploadID + ".uld";
          break;

      default:
        // Should never get here
        log.write("Invalid upload data type (" + dataType + ") for received data - (" + testRequestID + ")");
        break;
      }
    }
    
    // Make sure there is a status file to open
    if (statusFile == null) {
      log.write("Warning: Upload status file is null");
      return;
    }

    try (FileOutputStream fout = new FileOutputStream(statusFile)) {
      if (content != null) {
        fout.write(content.getBytes());
      }
    }    
  }

}
