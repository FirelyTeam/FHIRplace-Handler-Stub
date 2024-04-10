# FHIRplace Handler SDK

This is a stubbed version of FHIRplace Handler to help with the creation of a working FHIRplace Implementation for the FHIRplace Pilot Testing.

The interactions with the FHIRplace Client have already been coded against the test cases.
You should only need to add your own FHIR implementation code in the specified sections of the ProcessTestRequest class for each test case.

Please note that there may be some confusion regarding the ProcessTestRequest respondToInitialSendOrReceive() method:
The test requests require reporting, uploading and sometimes verification of certain pieces of the 
sending and receiving processes.  In actuality, this is all one step, however pieces of this step need to be 
extracted and uploaded individually.  To accommodate this, you may record all the parts of your sending
and receiving (requests and responses) into a container class, including test status, HTTP responses, 
Access Token values, Member Identifiers, Patient Data, etc., and then extract those values for recording the values 
identified in the provided stubbed source code and then test your updated code.


## Building the code

Use the provided pom.xml file to build the FHIRplace-Handler jar file. You may build it either through your IDE or 
by invoking 'mvn' on the command line.

The FHIRplace-Handler jar file will be written to your target directory with an appended version number.
You may remove this version number for ease of execution.


## Running the executable

Copy the FHIRplace-Handler jar to the same directory where you are running the FHIRplace Client.  This is necessary because it will need access to the fhirplace-clientconfig.properties file created and used by the FHIRplace Client.  

The FHIRplace-Handler jar has the following dependencies:
 - guava-11.0.2.jar
 - xercesImpl-2.11.0.jar
 - xml-apis-1.4.01.jar

These jars will be downloaded to your maven repository during the build process.

Create a lib subdirectory under the directory where you will be running the FHIRplace-Handler and place those dependent jars there.

From the command line (or via a batch or command file) run the FHIRplace-Handler using this command:

	java -jar FHIRplace-Handler.jar

Using the FHIRplace Client, you will then queue tests to be run.  The FHIRplace-Handler will process the test requests and upload the results to the FHIRplace Client.

