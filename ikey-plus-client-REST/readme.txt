Contact : thomas.burguiere@upmc.fr, florian.causse@upmc.fr

This project allows you to query the identification key generation webservice.
This project must be used and configured as an Eclipse Maven project.
Before running the Main class, ensure that your client is querying the REST version of the webservice.

In the "IdentificationKeyRESTClient.java" file you can configure the server host name (line 38) and the webservice options (line 42 to line 52).
You can also set the number of times you want to query the webservice by modifying the number of thread you want launch and the number of webservice call per thread (line 27 and line 34).

Then, you just have to run the Main method to query the webservice.
