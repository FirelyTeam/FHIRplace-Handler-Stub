package com.dgi.fhirplace.handler;

import com.dgi.fhirplace.util.FHIRplaceUtil;
import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class polls the Test Request directory for all incoming messages
 * from the FHIRplace client and processes each message.  The messages can
 * be actual test requests or cancel/kill requests.
 * 
 */
public class ProcessIncomingMessages extends Thread {
  
  FHIRplaceHandler handler = null;
  LocalParameters params = null;

  String tesRequestFileName = null;
  String cancelTestRequestFileName = null;
  int statusCount = 0;
  
  // Holds currently active ProcessTestRequest objects
  public static ConcurrentHashMap testRequestTable = new ConcurrentHashMap();
  
  Logger log = new Logger(ProcessIncomingMessages.class);
  
  public ProcessIncomingMessages(FHIRplaceHandler handler, LocalParameters params) {
    this.handler = handler;
    this.params = params;
  }

  private boolean forever = true;

  @Override
  public void run() {
    this.setName("ProcessMessages:" + this.getName());
    
    log.write("Processing Incoming Messages...");

    // Process incoming request messages until told to stop
    while (forever) {
      // Poll FHIRplace test request directory
      tesRequestFileName = this.pollDirectory();

      if (tesRequestFileName != null) {
       // Check and manage the cancelled request parameter
        if (!this.params.getCancelledTestRequest() ||
            this.params.getActiveTests().isEmpty()) {
          this.params.setCancelledTestRequest(false);
        }

        String testRequestNum = FHIRplaceUtil.getTestRequestID(tesRequestFileName);
        if (this.params.getActiveTests().isEmpty() ||
            !this.params.isActiveTest(testRequestNum) ||
            tesRequestFileName.toLowerCase().contains("kill")) {
          this.params.addActiveTest(testRequestNum);
          log.write("Received " + tesRequestFileName + ", request " + testRequestNum);
          ProcessTestRequest request = new ProcessTestRequest(this.handler, this.params, tesRequestFileName);

          // Add request to a table so we can find associated objects for a test request number 
          // if we need to access the object later
          testRequestTable.put(testRequestNum, request);
          request.start();
          
        } else {
           // Let it do its work
           try {
             FHIRplaceUtil.wait(1);
           } catch (InterruptedException ex) {
              forever = false;
          }
        }
      }
    }
  }
  
  /**
   * Stops the processing of the thread - called externally (if desired)
   */
  public void stopProcessing() {
    forever = false;
  }
  /**
   * Poll the test request directory and when a file comes in return its name
   * @return the name of the request file
   */
  private String pollDirectory() {
    while (forever) {
      File dir = new File(params.getTestRequestDirectory());

      // Filter out all XML process request files in the request directory
      FHIRplaceFileFilter testRequests = new FHIRplaceFileFilter(".xml");
      File reqFiles[] = dir.listFiles(testRequests);
      if (reqFiles != null) {
        for (File reqFile : reqFiles) {
          // Don't allow multiple processing of the same process request file
          if (tesRequestFileName == null || !tesRequestFileName.equals(reqFile.getPath())) {
            tesRequestFileName = reqFile.getPath();
            File dupCheckFile = new File(params.getArchiveDirectory(), reqFile.getName());
            if (dupCheckFile.exists()) {
              // Log a message and remove the file
              log.write("Received and removed duplicate test request " + 
                        FHIRplaceUtil.getTestRequestID(tesRequestFileName));
              if (!reqFile.delete()) {
                boolean deleted = false;
                boolean interrupted = false;
                int count = 0;
                // try for up to one minute
                while (count++ < 60) {
                  try {
                    FHIRplaceUtil.wait(1);
                  } catch (InterruptedException e) {
                    interrupted = true;
                    break;
                  }
                  if (reqFile.delete()) {
                    deleted = true;
                    break;
                  }
                }
                if (interrupted)
                  log.write("Warning: Directory poller was interrupted during duplicate request checking.");
                else if (!deleted)
                  log.write("Warning: Duplicate request file " + tesRequestFileName + " could not be deleted.");
              }
            } else {
              return tesRequestFileName;
            }
          }
        }
      }

      // Filter out all XML cancel request files in the request directory
      FHIRplaceFileFilter cancelRequests = new FHIRplaceFileFilter(".kill");
      File cancelFiles[] = dir.listFiles(cancelRequests);
      if (cancelFiles != null) {
        for (File cancelFile : cancelFiles) {
          // Don't allow multiple processing of the same cancel request file
          if (cancelTestRequestFileName == null || !cancelTestRequestFileName.equals(cancelFile.getPath())) {
            cancelTestRequestFileName = cancelFile.getPath();
            return cancelTestRequestFileName;
          }
        }
      }

      // Check the Status folder to verify that they are getting consumed
      FHIRplaceFileFilter statusUpdates = new FHIRplaceFileFilter(".sts", ".uld");
      File sts = new File(params.getStatusDirectory());
      File stsFiles[] = sts.listFiles(statusUpdates);
      if (stsFiles != null && stsFiles.length > 0) {
        // Count how many of the entries are more than five minutes old
        int oldFileCount = 0;
        for (File stsFile : stsFiles) {
          if (stsFile.exists() && (FHIRplaceUtil.getFileAge(stsFile) >= 5L)) {
            oldFileCount++;
          }
        }
        if (oldFileCount != statusCount) {
          log.write("Warning: There are " + stsFiles.length + " old unprocessed status updates in " + 
                    params.getStatusDirectory());
        }
        statusCount = oldFileCount;
      }

      // Wait 3 seconds between checks
      try {
        FHIRplaceUtil.wait(3);
      } catch (InterruptedException ex) {
        return null;
      }
    }
    return null;
  }


  // Inner class to filter out specific incoming file types
  class FHIRplaceFileFilter implements FilenameFilter {

    private String ext   = null;
    private String ext_a = null;
    private String ext_b = null;

    public FHIRplaceFileFilter(String ext) {
      // Filters a file with a single extension
      this.ext = ext;
    }

    public FHIRplaceFileFilter(String ext_a, String ext_b) {
      // Filters two different file extensions
      this.ext_a = ext_a;
      this.ext_b = ext_b;
    }

    @Override
    public boolean accept(File dir, String name) {
      if (ext != null)
        return name.toLowerCase().endsWith(this.ext);
      else if (ext_a != null && ext_b != null)
        return name.toLowerCase().endsWith(this.ext_a) || name.toLowerCase().endsWith(this.ext_b);

      // default
      return false;
    }
  }
}
