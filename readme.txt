Contact : thomas.burguiere@upmc.fr, florian.causse@upmc.fr

BUILD

1)identificationKeyAPI -> run as -> maven assembly or maven package
-> generates a single .jar file named identificationKeyAPI-1.x.jar


2)Copy the jar file into the Webcontent/WEB-INF/lib folders of the webservice project (IK_WS_REST or IK_WS_SOAP)


3)Copy the sigar libraries into the Webcontent/WEB-INF/lib folders of the webservice project (IK_WS_REST or IK_WS_SOAP)


4)IK_WS_SOAP or IK_WS_REST -> run as -> maven assembly or maven package
-> generates a .war file (named IK_WS_SOAP-1.x.war or IK_WS_SOAP-1.x.war) for the selected web wervice project


DEPLOYMENT

1)IK_WS_SOAP or IK_WS_REST -> put the WAR file in the Tomcat/webapps/ directory


2)Start the server


3)Stop server and put a new confOverridable.properties file in Tomcat/webapps/IK_WS_REST-1.0/WEB-INF/classes/ directory.
This file must contain (change parameters if necessary) :

generatedKeyFiles.prefix = webapps/
generatedKeyFiles.folder = generatedKeyFiles/
// 2592000 is the number of second for 30 days.
generatedKeyFiles.delete.period = 2592000
host = http://yourServerHost:8080/
message.createdBy = Created by www.identificationKey.fr


4)To restrict the access to the application you can add the following parameters in the <host> tag in the server.xml file of tomcat:

<Context path="/ApplicationName" docBase="/var/lib/tomcat6/webapps/ApplicationName" workDir="/var/lib/tomcat6/webapps/ApplicationName">
  <Valve className="org.apache.catalina.valves.RemoteAddrValve" allow="134.157.*.*" deny=""/>
</Context>


5)Start the server


