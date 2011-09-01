
BUILD
1)identificationKeyAPI -> run as -> maven assembly or maven package
-> generates a single .jar file named identificationKeyAPI-1.x.jar

2)Copy the jar file into the Webcontent/WEB-INF/lib folders of the webservice project (IK_WS_REST or IK_WS_SOAP)

3)Copy the sigar libraries into the Webcontent/WEB-INF/lib folders of the webservice project (IK_WS_REST or IK_WS_SOAP)

4)IK_WS_SOAP or IK_WS_REST -> run as -> maven assembly or maven package
-> generates a .war file (named IK_WS_SOAP-1.x.war or IK_WS_SOAP-1.x.war) for the selected web wervice project


DEPLOYMENT
1)IK_WS_SOAP or IK_WS_REST -> put the WAR file in the Tomcat/webapps/ directory

3)Start the server

2)Stop server and put a new confOverridable.properties file in Tomcat/webapps/IK_WS_REST-1.0/WEB-INF/classes/ directory.
This file must contain :
generatedKeyFiles.prefix = webapps/
generatedKeyFiles.folder = generatedKeyFiles/
// 2592000 is the number of second for 30 days.
generatedKeyFiles.delete.period = 2592000
host = http://yourServerHostName:8080/

3)Start the server

