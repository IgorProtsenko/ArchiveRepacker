Archive repacker and Phone/Email extracter manual.

Test data - https://www.dropbox.com/s/djyf0yz2u18rblr/inputs.zip

Declaimer: The application has to be used only within Microsoft Windows environment.
Proper functioning on Mac OS / Linux family OS's is not guaranteed.

Usage:
Compile the application from sources (JRE/JDK7).

	a. Install Apache Maven tool (http://maven.apache.org) and set environment variables as it 
	recommended on the distributor website.

	b. Proceed to project root (where .pom file is located), open command prompt and execute the command:
		mvn clean package

	c. Get compiled application from directory:
		<project root> \ target \ contactsparcer-1.0.jar
		
	d. Run application: java -jar contactsparcer.jar inputs.zip
