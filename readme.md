Ikey+: Identification key generator
===================================

Authors : 

- Florian Causse 
- Thomas Burguiere (thomas.burguiere@gmail.com)

Disclaimer
----------
This is a fork of one of the app I worked on during my days at "Laboratoire d'Informatique et Systématique" at Université Pierre et Marie Curie, Paris, France, from 2011 to 2013. The original source code is publicly available [here](https://code.google.com/p/ikey-plus/) and the corresponding publication is available [there](http://sysbio.oxfordjournals.org/content/62/1/157.long). This is still a work in progress on many aspects, the code is a bug-ridden mess, but it does work :).

Contributions are welcome !

Build
-----

Run `mvn install`

Usage - API
-----------

Include the api jar in your app, then:

```java
// setup generator config with default values
IkeyConfig config = IkeyConfig.builder().build();

// initialize sdd parser
SDDSaxParser sddSaxParser = new SDDSaxParser(new File("inputFile.sdd"), config);

// initialize key generator
IdentificationKeyGenerator identificationKeyGenerator = 
	new IdentificationKeyGenerator(sddSaxParser.getDataset(), config);

// compute and get identification key
identificationKeyGenerator.createIdentificationKey();
SingleAccessKeyTree key = identificationKeyGenerator.getSingleAccessKeyTree();

// dump key to a file
boolean statisticsEnabled = true or false;
File resultFile = SingleAccessKeyTreeDumper
					.dumpFlatHtmlFile("result file header", key, statisticsEnabled, outputFolder);
```

Usage - REST API
----------------

Coming soon...




