package com.dgi.fhirplace.parser;

import com.dgi.fhirplace.handler.FHIRplaceConstants;
import com.dgi.fhirplace.util.FHIRplaceUtil;
import com.dgi.fhirplace.handler.Logger;
import com.dgi.fhirplace.handler.LocalParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The class parses the Test request elements into separated 
 * Description, Participant, Transmission and Instructions objects
 */
public class ParseTestRequest {
  DocumentBuilderFactory factory = null;
  DocumentBuilder builder = null;
  Document document = null;

  MyEntityResolver er = new MyEntityResolver();
  MyErrorHandler eh = new MyErrorHandler();

  FHIRplaceXML xml = null;
  LocalParameters params = null;
  
  Logger log = new Logger(ParseTestRequest.class);

  /**
   * Parses the FHIRplace XML file
   * @param path the path to the XML file
   * @param xml the FHIRplace XML object
   * @param params the Local Parameters
   * @param debug the Debug object
   * @throws Exception 
   */
  public ParseTestRequest(File path, FHIRplaceXML xml, LocalParameters params) throws Exception {

    this.xml = xml;
    this.params = params;


/*------------------------------------------------------------------------------
 *  Get the document builder, which is used to parse in XML
 *----------------------------------------------------------------------------*/
    factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
    factory.setValidating(true);
    factory.setExpandEntityReferences(true);

    builder = factory.newDocumentBuilder();
    builder.setEntityResolver(er);
    builder.setErrorHandler(eh);

    document = builder.parse(path);
    Element element = document.getDocumentElement();
    NodeList nodelist = element.getChildNodes();
    
    Description desc = null;

    for (int i=0; i < nodelist.getLength(); i++) {
      Node node = nodelist.item(i);

      if ( FHIRplaceUtil.getNodeName(node).equalsIgnoreCase("Description") ) {
        desc = this.parseDescription(node);
        xml.setDescription(desc);

      } else if ( FHIRplaceUtil.getNodeName(node).equalsIgnoreCase("Participant") ) {
        Participant part =  this.parseParticipant(node);
        xml.setParticipant(part);

      } else if (FHIRplaceUtil.getNodeName(node).equalsIgnoreCase("Transmission") ) {
        Transmission trans = this.parseTransmission(node);
        // Force the protocol into the transmission object so we have a copy of it
        if (desc != null)
          trans.setProtocol(desc.getProtocol());
        xml.setTransmission(trans);

      } else if ( FHIRplaceUtil.getNodeName(node).equalsIgnoreCase("Instructions") ) {
        Instructions instruct = this.parseInstructions(node);
        xml.setInstructions(instruct);
      }
    }

    // Log the contents of the objects that were created
    if (params.isDebugMode()) {
      log.write("\n--- Participants ---");
      Participant[] parts = xml.getParticipant();
      for (Participant part : parts) {
        log.write(part.toString());
      }
      log.write("\n--- Description ---\n" + xml.getDescription().toString());
      log.write("\n--- Transmission ---\n" + xml.getTransmission().toString());
      log.write("\n--- Instructions ---\n" + xml.getInstructions().toString() + "\n");
    }
  }

  private Description parseDescription(Node node)
    throws org.xml.sax.SAXException {

    Description desc = new Description();
    
    // Walk through the child nodes to get the Description Parameters
    NodeList nodeList = node.getChildNodes();
    NamedNodeMap attributes;

    for (int i=0; i < nodeList.getLength(); i++) {
      Node childNode = nodeList.item(i);
      if ( childNode.hasAttributes() ) {
        attributes = childNode.getAttributes();
        int numAttr = attributes.getLength();

        if (FHIRplaceUtil.getNodeName(childNode).equalsIgnoreCase("Reason")) {
          for (int j=0; j < numAttr; j++) {
            if (attributes.item(j).getNodeName().equalsIgnoreCase("Protocol") ) {
              desc.setProtocol( attributes.item(j).getNodeValue() );
            } else if (attributes.item(j).getNodeName().equalsIgnoreCase("Purpose") ) {
              desc.setPurpose( attributes.item(j).getNodeValue());
            }
          }
        }
      }

      if (childNode.hasChildNodes()) {
        NodeList childNodeList = childNode.getChildNodes();
        if (FHIRplaceUtil.getNodeName(childNode).equalsIgnoreCase("TestRequestID")) {
          desc.setTestRequestID(childNodeList.item(0).getNodeValue());
        } else if (FHIRplaceUtil.getNodeName(childNode).equalsIgnoreCase("Version")) {
          desc.setVersion(childNodeList.item(0).getNodeValue());
        } else if (FHIRplaceUtil.getNodeName(childNode).equalsIgnoreCase("TestCase")) {
          desc.setTestCase(childNodeList.item(0).getNodeValue());
        } else if (FHIRplaceUtil.getNodeName(childNode).equalsIgnoreCase("TestCaseType")) {
          desc.setTestCaseType(childNodeList.item(0).getNodeValue());
        } else if (FHIRplaceUtil.getNodeName(childNode).equalsIgnoreCase("TestDescription")) {
          desc.setTestDescription(childNodeList.item(0).getNodeValue());
        } else if (FHIRplaceUtil.getNodeName(childNode).equalsIgnoreCase("ExpectedResult")) {
          desc.setExpectedResult(childNodeList.item(0).getNodeValue());
        } else if (FHIRplaceUtil.getNodeName(childNode).equalsIgnoreCase("TestRound")) {
          NodeList grandChildNodeList = childNode.getChildNodes();
          for (int j=0; j < grandChildNodeList.getLength(); j++) {
            Node grandChildNode = grandChildNodeList.item(j);
            NodeList newChildNodeList = grandChildNode.getChildNodes();
            if (FHIRplaceUtil.getNodeName(grandChildNode).equalsIgnoreCase("Name")) {
              desc.setTestName(newChildNodeList.item(0).getNodeValue());
            } else if (FHIRplaceUtil.getNodeName(grandChildNode).equalsIgnoreCase("Purpose")) {
              desc.setTestPurpose(newChildNodeList.item(0).getNodeValue());
            } else if (FHIRplaceUtil.getNodeName(grandChildNode).equalsIgnoreCase("Phase")) {
              desc.setTestPhase(newChildNodeList.item(0).getNodeValue());
            }
          }
        } else if (FHIRplaceUtil.getNodeName(childNode).equalsIgnoreCase("TimeStamp")) {
          NodeList grandChildNodeList = childNode.getChildNodes();
          for (int j=0; j < grandChildNodeList.getLength(); j++) {
            Node grandChildNode = grandChildNodeList.item(j);
            NodeList newChildNodeList = grandChildNode.getChildNodes();
            if (FHIRplaceUtil.getNodeName(grandChildNode).equalsIgnoreCase("Date")) {
              desc.setDate(newChildNodeList.item(0).getNodeValue());
            } else if (FHIRplaceUtil.getNodeName(grandChildNode).equalsIgnoreCase("Time")) {
              desc.setTime(newChildNodeList.item(0).getNodeValue());
            }
          }
        }
      }
    }
    return desc;
  }

  private Participant parseParticipant( Node node )
    throws org.xml.sax.SAXException {
    
    Participant part = new Participant();

    // Get participantID
    NamedNodeMap attributes = node.getAttributes();
    if ( attributes.item(0).getNodeName().equalsIgnoreCase("ParticipantID") )
      part.setParticipantID( attributes.item(0).getNodeValue() );

    // Walk through the child nodes to get the rest of the Participant Parameters
    NodeList nodeList = node.getChildNodes();

    for (int i=0; i < nodeList.getLength(); i++) {
      Node childNode = nodeList.item(i);
      if (childNode.hasChildNodes()) {
        NodeList childNodeList = childNode.getChildNodes();
        Node grandChildNode = childNodeList.item(0);
        if (FHIRplaceUtil.getNodeName(childNode).equalsIgnoreCase("Name")) {
          part.setName(grandChildNode.getNodeValue());
        } else if (FHIRplaceUtil.getNodeName(childNode).equalsIgnoreCase("Product") ) {
          part.setProduct(grandChildNode.getNodeValue());
        } else if (FHIRplaceUtil.getNodeName(childNode).equalsIgnoreCase("Version") ) {
          part.setVersion(grandChildNode.getNodeValue());
        } else if (FHIRplaceUtil.getNodeName(childNode).equalsIgnoreCase("FHIR-Version") ) {
          part.setFhirVersion(grandChildNode.getNodeValue());
        }
      }
    }
    return part;
  }

  private Transmission parseTransmission(Node node)
    throws org.xml.sax.SAXException, MalformedURLException {
    
    Transmission trans = new Transmission();

    NodeList nodeList = node.getChildNodes();
    
    for (int i=0; i < nodeList.getLength(); i++) {
      Node childNode = nodeList.item(i);
      
      if (FHIRplaceUtil.getNodeName(childNode).equalsIgnoreCase("Originator")) {
        if (childNode.hasAttributes()) {
          NamedNodeMap originatorAttributes = childNode.getAttributes();
          for (int j=0; j < originatorAttributes.getLength(); j++) {
            if ( originatorAttributes.item(j).getNodeName().equalsIgnoreCase("ParticipantID") ) {
              String nodeValue = originatorAttributes.item(j).getNodeValue();
              trans.setOriginator(nodeValue);
              trans.setSenderID(nodeValue);
            } else if ( originatorAttributes.item(j).getNodeName().equalsIgnoreCase("Role") ) {
              trans.setOriginatorRole(originatorAttributes.item(j).getNodeValue());
            }
          }
        }
      } else if (FHIRplaceUtil.getNodeName(childNode).equalsIgnoreCase("Recipient")) {
        if (childNode.hasAttributes()) {
          NamedNodeMap recipientAttributes = childNode.getAttributes();
          for (int j=0; j < recipientAttributes.getLength(); j++) {
            if ( recipientAttributes.item(j).getNodeName().equalsIgnoreCase("ParticipantID") ) {
              String nodeValue = recipientAttributes.item(j).getNodeValue();
              trans.setRecipient(nodeValue);
              trans.setReceiverID(nodeValue);
            } else if ( recipientAttributes.item(j).getNodeName().equalsIgnoreCase("Role") ) {
              trans.setRecipientRole(recipientAttributes.item(j).getNodeValue());          
            }
          }
        }
      } else if (FHIRplaceUtil.getNodeName(childNode).equalsIgnoreCase("Settings")) {
        NodeList childNodeList = childNode.getChildNodes();

        for (int j=0; j < childNodeList.getLength(); j++) {
          Node newChildNode = childNodeList.item(j);
          if (newChildNode.hasAttributes() || newChildNode.hasChildNodes()) {

            if (FHIRplaceUtil.getNodeName(newChildNode).equalsIgnoreCase("mTLS") ) {
              if (newChildNode.hasAttributes()) {
                NamedNodeMap mTLSAttributes = newChildNode.getAttributes();
           
                for (int k=0; k < mTLSAttributes.getLength(); k++) {
                  if (mTLSAttributes.item(k).getNodeName().equalsIgnoreCase("BundleName")) {
                    trans.setBundleName(mTLSAttributes.item(k).getNodeValue());
                  } else if (mTLSAttributes.item(k).getNodeName().equalsIgnoreCase("Type")) {
                    trans.setBundleType(mTLSAttributes.item(k).getNodeValue());
                  } else if (mTLSAttributes.item(k).getNodeName().equalsIgnoreCase("ParticipantID")) {
                    trans.setBundleOwner(mTLSAttributes.item(k).getNodeValue());
                  }
                }
              }
            } else if (FHIRplaceUtil.getNodeName(newChildNode).equalsIgnoreCase("Patient")) {
              if (newChildNode.hasAttributes()) {
                NamedNodeMap patientAttributes = newChildNode.getAttributes();
           
                for (int k=0; k < patientAttributes.getLength(); k++) {
                  if (patientAttributes.item(k).getNodeName().equalsIgnoreCase("ResourceName")) {
                    trans.setPatientResourceName(patientAttributes.item(k).getNodeValue());
                  } else if (patientAttributes.item(k).getNodeName().equalsIgnoreCase("Type")) {
                    trans.setPatientResourceType(patientAttributes.item(k).getNodeValue());
                  } else if (patientAttributes.item(k).getNodeName().equalsIgnoreCase("ParticipantID")) {
                    trans.setPatientResourceOwner(patientAttributes.item(k).getNodeValue());
                  }
                }
              }
            }
          }
        }
      }  
    }
    return trans;
  }

  private Instructions parseInstructions(Node node) {

    Instructions instruct = new Instructions();
    
    // We are only interested in the instructions the designated Participant ID
    NodeList nodeList = node.getChildNodes();
    NamedNodeMap attributes;
    NamedNodeMap childAttributes;

    for (int i=0; i < nodeList.getLength(); i++) {
      Node childNode = nodeList.item(i);

      if (childNode.hasAttributes()) {
        String participant = null;
        String dataType = null;

        attributes = childNode.getAttributes();
        for (int j=0; j < attributes.getLength(); j++) {
          if (FHIRplaceUtil.getNodeName(childNode).equalsIgnoreCase("Ack")) {
            if (attributes.item(j).getNodeName().equalsIgnoreCase("ParticipantID")) {
              participant = attributes.item(j).getNodeValue();

            } else if (attributes.item(j).getNodeName().equalsIgnoreCase("ResponseID")) {
              String response_id = attributes.item(j).getNodeValue();
              if (participant != null && participant.equalsIgnoreCase(params.getXMLIdentifier()))
                instruct.setAckID(response_id);
            }

          } else if (FHIRplaceUtil.getNodeName(childNode).equalsIgnoreCase("Send") )   {
            if ((attributes.item(j).getNodeName().equalsIgnoreCase("ParticipantID")) &&
                (attributes.item(j).getNodeValue().equalsIgnoreCase(params.getXMLIdentifier())) ) {
              instruct.setSendDataType(dataType);

            } else if (attributes.item(j).getNodeName().equalsIgnoreCase("DataType")) {
              dataType = attributes.item(j).getNodeValue();
            }

          } else if (FHIRplaceUtil.getNodeName(childNode).equalsIgnoreCase("Receive")) {
              if ((attributes.item(j).getNodeName().equalsIgnoreCase("ParticipantID") ) &&
                  (attributes.item(j).getNodeValue().equalsIgnoreCase(params.getXMLIdentifier())) ) {
                instruct.setReceiveDataType(dataType);

              } else if (attributes.item(j).getNodeName().equalsIgnoreCase("DataType")) {
                dataType = attributes.item(j).getNodeValue();
              }

          } else if (FHIRplaceUtil.getNodeName(childNode).equalsIgnoreCase("EvaluateTest")) {
              if ( attributes.item(j).getNodeName().equalsIgnoreCase("ParticipantID") ) {
                participant = attributes.item(j).getNodeValue();

              } else if (attributes.item(j).getNodeName().equalsIgnoreCase("ResponseID")) {
                String response_id = attributes.item(j).getNodeValue();
                if (participant != null && participant.equalsIgnoreCase(params.getXMLIdentifier()))
                  instruct.setEvaluateTestID(response_id);
              }
          }

          String responseID = null;
          String direction = FHIRplaceUtil.getNodeName(childNode);

          if (childNode.hasChildNodes()) {
            NodeList grandChildNodeList = childNode.getChildNodes();
            for (int k=0; k < grandChildNodeList.getLength(); k++) {
              Node grandChildNode = grandChildNodeList.item(k);

              if (FHIRplaceUtil.getNodeName(grandChildNode).equalsIgnoreCase("Report")) {
                childAttributes = grandChildNode.getAttributes();

                for (int m=0; m < childAttributes.getLength(); m++) {
                  if ( childAttributes.item(m).getNodeName().equalsIgnoreCase("ParticipantID") ) {
                    participant = childAttributes.item(m).getNodeValue();
                  }

                  if (childAttributes.item(m).getNodeName().equalsIgnoreCase("ResponseID")) {
                    responseID = childAttributes.item(m).getNodeValue();
                  }
                }

                // If this element belongs to us, add it to the Report instruction
                if (participant != null && participant.equalsIgnoreCase(params.getXMLIdentifier())) {

                  if (direction.equalsIgnoreCase("Send") ) {
                    instruct.setSendID(responseID, dataType);

                  } else if (direction.equalsIgnoreCase("Receive") ) {
                    instruct.setReceiveID(responseID, dataType);
                  }
                }

              } else if (FHIRplaceUtil.getNodeName(grandChildNode).equalsIgnoreCase("Upload")) {
                childAttributes = grandChildNode.getAttributes();

                // Initialize all variables that will be used below
                participant = null;
                String uploadID = null;
                String format = null;

                for (int m=0; m < childAttributes.getLength(); m++) {
                  if ( childAttributes.item(m).getNodeName().equalsIgnoreCase("ParticipantID") ) {
                    participant = childAttributes.item(m).getNodeValue();
                  }

                  if (childAttributes.item(m).getNodeName().equalsIgnoreCase("ResponseID")) {
                    uploadID = childAttributes.item(m).getNodeValue();

                  } else if (childAttributes.item(m).getNodeName().equalsIgnoreCase("Format")) {
                    format = childAttributes.item(m).getNodeValue();
                  }
                }

                // If this element belongs to us, add the appropriate upload instruction
                if (participant != null && participant.equalsIgnoreCase(params.getXMLIdentifier())) {
                  int directionType = direction.equalsIgnoreCase("Send") ? FHIRplaceConstants.SENT_DATA : 
                                                                           FHIRplaceConstants.RECEIVED_DATA;
                  int uploadType = -1;
                  if (format != null) {
                    if (format.equalsIgnoreCase("AccessToken")) {
                      uploadType = FHIRplaceConstants.ACCESS_TOKEN_DATA;
                    } else if (format.equalsIgnoreCase("ClientID")) {
                      uploadType = FHIRplaceConstants.CLIENT_ID_DATA;
                    } else if (format.equalsIgnoreCase("FHIR-ID")) {
                      uploadType = FHIRplaceConstants.FHIR_ID_DATA;
                    } else if (format.equalsIgnoreCase("PatientData")) {
                      uploadType = FHIRplaceConstants.PATIENT_DATA;
                    } else if (format.equalsIgnoreCase("Transport")) {
                      uploadType = FHIRplaceConstants.TRANSPORT_DATA;
                    }
                  }

                  if (instruct.findUploadRecord(uploadID, uploadType, directionType) == null) {
                    // Don't allow duplicate upload entries
                    instruct.addUpload(uploadID, uploadType, directionType);
                  }
                }

              } else if (FHIRplaceUtil.getNodeName(grandChildNode).equalsIgnoreCase("Verify")) {

                childAttributes = grandChildNode.getAttributes();
                String verification = null;
                responseID = null;

                for (int m=0; m < childAttributes.getLength(); m++) {
                  if ( childAttributes.item(m).getNodeName().equalsIgnoreCase("ParticipantID") ) {
                    participant = childAttributes.item(m).getNodeValue();

                  }  else if (childAttributes.item(m).getNodeName().equalsIgnoreCase("ResponseID")) {
                    responseID = childAttributes.item(m).getNodeValue();

                  } else if (childAttributes.item(m).getNodeName().equalsIgnoreCase("VerificationAspect") ) {
                    verification = childAttributes.item(m).getNodeValue();
                  }
                }

                // If this element belongs to us, update the appropriate Verification Aspect instructions
                if (participant != null && participant.equalsIgnoreCase(params.getXMLIdentifier())) {
                  if (verification != null && verification.equalsIgnoreCase("AccessToken")) {
                    instruct.setAccessTokenVerifyID(responseID);
                  } else if (verification != null && verification.equalsIgnoreCase("ClientID")) {
                    instruct.setClientIDVerifyID(responseID);
                  } else if (verification != null && verification.equalsIgnoreCase("FHIR-ID")) {
                    instruct.setFhirIDVerifyID(responseID);
                  } else if (verification != null && verification.equalsIgnoreCase("PDEXResource")) {
                    instruct.setPdexResourceVerifyID(responseID);
                  } else if (verification != null && verification.equalsIgnoreCase("PatientData")) {
                    instruct.setPatientDataVerifyID(responseID);
                  } 
                }
              }
            }
          }
        }
      }
    }
    return instruct;
  }

/*------------------------------------------------------------------------------
 * Inner Class : MyEntityResolver
 *----------------------------------------------------------------------------*/
  class MyEntityResolver implements org.xml.sax.EntityResolver {
    @Override
    public org.xml.sax.InputSource resolveEntity(String publicId, String systemId)
      throws org.xml.sax.SAXException {
      try {
        return(new org.xml.sax.InputSource(new FileInputStream(systemId.substring(6))));
      } catch (FileNotFoundException ex) {
        throw new org.xml.sax.SAXException(ex.getMessage());
      }
    }
  }

/*------------------------------------------------------------------------------
 * Inner Class : MyErrorHandler
 *----------------------------------------------------------------------------*/
  class MyErrorHandler implements org.xml.sax.ErrorHandler {
    @Override
    public void warning(org.xml.sax.SAXParseException exception)
      throws org.xml.sax.SAXException {
    }
    @Override
    public void error(org.xml.sax.SAXParseException exception)
      throws org.xml.sax.SAXException{
    }
    @Override
    public void fatalError(org.xml.sax.SAXParseException exception)
      throws org.xml.sax.SAXException {
    }
    public void UnrecoverableErrorCondition(String message)
      throws org.xml.sax.SAXException {

      throw new org.xml.sax.SAXException(message);
    }
  }
}

