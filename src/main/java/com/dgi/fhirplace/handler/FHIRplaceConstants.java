package com.dgi.fhirplace.handler;

/**
 * Constants used for FHIRplace Handler
 */
public class FHIRplaceConstants {
  public final static String FILE_SEPARATOR = "/";

  // Send/Receive data types
  public final static String REGISTRATION_TYPE     = "Registration";
  public final static String ACCESS_REQUEST_TYPE   = "AccessRequest";
  public final static String ACCESS_TOKEN_TYPE     = "AccessToken";
  public final static String CLIENT_ID_TYPE        = "ClientID";
  public final static String MEMBER_MATCH_TYPE     = "MemberMatchQuery"; 
  public final static String FHIR_ID_TYPE          = "FHIR-ID";
  public final static String PDEX_REQUEST_TYPE     = "PDEXRequest";
  public final static String PDEX_RESOURCE_TYPE    = "PDEXResource";
  public final static String PATIENT_REQUEST_TYPE  = "PatientRequest";
  public final static String PATIENT_DATA_TYPE     = "PatientData";
  
  // Expected Results
  public final static String SUCCESS = "Success";
  public final static String FAILURE = "Failure";
  
  // Data upload types
  public final static int ACCESS_TOKEN_DATA  = 0;
  public final static int CLIENT_ID_DATA     = 1;
  public final static int FHIR_ID_DATA       = 2;
  public final static int PATIENT_DATA       = 3;
  public final static int TRANSPORT_DATA     = 4;

  public final static int SENT_DATA          = 0;
  public final static int RECEIVED_DATA      = 1;    

  // Status codes
  public final static int ACK                = 0;
  public final static int NAK                = 1;
  public final static int SENT_OK            = 2;
  public final static int SENT_NOT_OK        = 3;
  public final static int RECEIVED_OK        = 4;
  public final static int RECEIVED_NOT_OK    = 5;
  public final static int VERIFIED_OK        = 6;
  public final static int VERIFIED_NOT_OK    = 7;
  public final static int RESULTS_OK         = 8;
  public final static int RESULTS_NOT_OK     = 9; 
}
