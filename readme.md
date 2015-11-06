# Ikey+: Identification key generator #

[![Build Status](https://travis-ci.org/thomasburguiere/ikey-plus.svg?branch=master)](https://travis-ci.org/thomasburguiere/ikey-plus) [![codecov.io](https://codecov.io/github/thomasburguiere/ikey-plus/coverage.svg?branch=master)](https://codecov.io/github/thomasburguiere/ikey-plus?branch=master) [![Codacy Badge](https://api.codacy.com/project/badge/grade/4d3260ee5aab4f03bd70f643d503bb41)](https://www.codacy.com/app/thomas-burguiere/ikey-plus)

Authors :

- Florian Causse
- Thomas Burguiere (thomas.burguiere@gmail.com)
- Visotheary Ung
- Régine Vignes-Lebbe

## Disclaimer ##

This is a fork of one of the app I worked on during my days at "Laboratoire d'Informatique et Systématique" at Université Pierre et Marie Curie, Paris, France, from 2011 to 2013. The original source code is publicly available [here](https://code.google.com/p/ikey-plus/) and the corresponding publication is available [there](http://sysbio.oxfordjournals.org/content/62/1/157.long). This is still a work in progress on many aspects, the code is a bug-ridden mess, but it does work :).

## What is this app doing?

This app generates [single access identification keys](https://en.wikipedia.org/wiki/Single-access_key), using [SDD](http://wiki.tdwg.org/twiki/bin/view/SDD/Version1dot1) formatted file as input. SDD is an XML standard used in systematic biology to store descriptive data.

If your descriptive data is stored in the delta format you can convert it to sdd using the [deltaToSdd](http://www.identificationkey.fr/deltatosdd/) webservice. If your descriptive data is stored in the Xper2 format you can convert it to sdd using the Xper2 software Export option.

## Building the app##
To build the app, you will need [Apache Maven](https://maven.apache.org/). Once you have maven installed, go to the root of the project, and run `mvn install`.

#### Maven dependency
```xml
<dependency>
    <groupId>fr.lis.ikey-plus</groupId>
    <artifactId>ikey-plus-api</artifactId>
    <version>${}</version>
</dependency>
```

## Usage ##

### Java API ###

```java
// setup generator config with default values
IkeyConfig config = IkeyConfig.builder().build();

// initialize SDD parser
SDDSaxParser parser = new SDDSaxParser(new File("inputFile.sdd"), config);

// initialize key generator
IdentificationKeyGenerator identificationKeyGenerator =
	new IdentificationKeyGenerator(parser.getDataset(), config);

// compute and get identification key
identificationKeyGenerator.createIdentificationKey();
SingleAccessKeyTree key = identificationKeyGenerator.getSingleAccessKeyTree();

// dump key to a file
boolean statisticsEnabled = true or false;
File resultFile = SingleAccessKeyTreeDumper
					.dumpFlatHtmlFile("result file header", key, statisticsEnabled, outputFolder);
```

### REST API ##

Coming soon...

## Contributing ##
Feel free to contribute. If you don't know how, code [coverage](https://codecov.io/github/thomasburguiere/ikey-plus?branch=master) and code [quality](https://www.codacy.com/app/thomas-burguiere/ikey-plus/dashboard) can always be improved.
