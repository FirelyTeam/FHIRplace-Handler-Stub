package com.dgi.fhirplace.handler;

/**
 * Constants used for FHIRplace Handler
 */
public class FHIRplaceConstants {
  public final static String FILE_SEPARATOR = "/";

//  Possible TestCaseType values:
//    - Connection
//    - Client-ID-Mismatch
//    - Expired-Access-Token
//    - Member-Authorization
//    - PDEX-Device
//    - PDEX-Medication
//    - PDEX-Provenance
//    - Patient-Everything-All
//    - Patient-Everything-NS
                        
  // Test case types
  public final static String CONNECTION            = "Connection";
  public final static String CLIENT_ID_MISMATCH    = "Client-ID-Mismatch";
  public final static String EXPIRED_ACCESS_TOKEN  = "Expired-Access-Token";
  public final static String MEMBER_AUTH           = "Member-Authorization";
  public final static String PDEX_DEVICE           = "PDEX-Device";
  public final static String PDEX_MEDICATION       = "PDEX-Medication";
  public final static String PDEX_PROVENANCE       = "PDEX-Provenance";
  public final static String PATIENT_ALL           = "Patient-Everything-All";
  public final static String PATIENT_NS            = "Patient-Everything-NS";

  // Send/Receive data types
  public final static String ACCESS_TOKEN_TYPE     = "AccessToken";
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

  public final static int SENT_DATA       = 0;
  public final static int RECEIVED_DATA   = 1;    

  // Status codes
  public final static int ACK                              = 0;
  public final static int NAK                              = 1;
  public final static int SENT_OK                          = 2;
  public final static int SENT_NOT_OK                      = 3;
  public final static int SENT_ACCESS_TOKEN_OK             = 4;
  public final static int SENT_ACCESS_TOKEN_NOT_OK         = 5;
  public final static int SENT_FHIR_ID_OK                  = 6;
  public final static int SENT_FHIR_ID_NOT_OK              = 7;
  public final static int SENT_PDEX_REQUEST_OK             = 8;
  public final static int SENT_PDEX_REQUEST_NOT_OK         = 9;
  public final static int SENT_PDEX_RESOURCE_OK            = 10;
  public final static int SENT_PDEX_RESOURCE_NOT_OK        = 11;
  public final static int SENT_PATIENT_REQUEST_OK          = 12;
  public final static int SENT_PATIENT_REQUEST_NOT_OK      = 13;
  public final static int SENT_PATIENT_DATA_OK             = 14;
  public final static int SENT_PATIENT_DATA_NOT_OK         = 15;
  
  public final static int RECEIVED_OK                      = 16;
  public final static int RECEIVED_NOT_OK                  = 17;
  public final static int RECEIVED_ACCESS_TOKEN_OK         = 18;
  public final static int RECEIVED_ACCESS_TOKEN_NOT_OK     = 19;
  public final static int RECEIVED_FHIR_ID_OK              = 20;
  public final static int RECEIVED_FHIR_ID_NOT_OK          = 21;
  public final static int RECEIVED_PDEX_REQUEST_OK         = 22;
  public final static int RECEIVED_PDEX_REQUEST_NOT_OK     = 23;
  public final static int RECEIVED_PDEX_RESOURCE_OK        = 24;
  public final static int RECEIVED_PDEX_RESOURCE_NOT_OK    = 25;
  public final static int RECEIVED_PATIENT_REQUEST_OK      = 26;
  public final static int RECEIVED_PATIENT_REQUEST_NOT_OK  = 27;
  public final static int RECEIVED_PATIENT_DATA_OK         = 28;
  public final static int RECEIVED_PATIENT_DATA_NOT_OK     = 29;
  
  public final static int ACCESS_TOKEN_VERIFIED_OK         = 30;
  public final static int ACCESS_TOKEN_VERIFIED_NOT_OK     = 31;
  public final static int CLIENT_ID_VERIFIED_OK            = 32;
  public final static int CLIENT_ID_VERIFIED_NOT_OK        = 33;
  public final static int FHIR_ID_VERIFIED_OK              = 34;
  public final static int FHIR_ID_VERIFIED_NOT_OK          = 35;
  public final static int PDEX_RESOURCE_VERIFIED_OK        = 36;
  public final static int PDEX_RESOURCE_VERIFIED_NOT_OK    = 37;
  public final static int PATIENT_DATA_VERIFIED_OK         = 38;
  public final static int PATIENT_DATA_VERIFIED_NOT_OK     = 39;
  public final static int RESULTS_OK                       = 40;
  public final static int RESULTS_NOT_OK                   = 41; 
}
