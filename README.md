# FHIRplace Handler SDK

This is a stubbed version of FHIRplace Handler to help with the creation of a working FHIRplace Implementation for use during the FHIRplace Interoperability Testing.

The interactions with the FHIRplace Client have already been coded against all the elements within the current test cases.
This includes all parsing of the test requests and placement of the test request element values into the corresponding container classes.
You should only need to add your own FHIR implementation code in the specified sections of the SendAsClient class (for New Payer) and/or the
ReceiveAsServer class (for Old Payer) for each test case.

The test requests require reporting, uploading and sometimes verification of certain pieces of the sending and receiving processes.
The ProcessTestRequest class will take care of all that for you as long as you provide the correct values within your SendAsClient / ReceiveAsServer implementations. These values are currently set with placeholder values. As part of your implementation, you may record all the parts of your sending and receiving (requests and responses) into these classes, including any test statuses, HTTP responses, Access Token values, Member Identifiers, Patient Data, and so on.  The ProcessTestRequest class will extract those values using the corresponding get() methods provided in these classes.


## Building the code

Use the provided pom.xml file to build the FHIRplace-Handler jar file. You may build it either through your IDE or 
by invoking 'mvn' on the command line.

The FHIRplace-Handler jar file will be written to your target directory with an appended version number.
You may remove this version number for ease of execution.


## Running the executable

Copy the FHIRplace-Handler jar to the same directory where you are running the FHIRplace Client.  This is necessary because it will need access to the fhirplace-clientconfig.properties file created and used by the FHIRplace Client (i.e., the configured directories and your Participant identifier).
The fhirplace-clientconfig.properties file *must* be in the same directory where you are running the the FHIRplace-Handler.  To make sure this occurs,
invoke the FHIRplace Client on the command line using the '-config fhirplace-clientconfig.properties' parameter, i.e., 

	java -jar fhirplace.jar -config fhirplace-clientconfig.properties
	
Note that by default, the fhirplace-clientconfig.properties file is stored in your user directory when you don't include the -config parameter.

The FHIRplace-Handler jar has the following dependencies:
 - xercesImpl-2.11.0.jar
 - xml-apis-1.4.01.jar

These jars will be downloaded to your maven repository during the build process.

Create a lib subdirectory under the directory where you will be running the FHIRplace-Handler and place those dependent jars there.

From the command line (or via a batch or command file) run the FHIRplace-Handler using this command:

	java -jar FHIRplace-Handler.jar

Using the FHIRplace Client, you will then queue tests to be run.  The FHIRplace-Handler will process the test requests and upload the results to the FHIRplace Client.

