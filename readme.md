# Ikey+: Identification key generator #

[![Join the chat at https://gitter.im/thomasburguiere/ikey-plus](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/thomasburguiere/ikey-plus?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

### Build Status `master` [![Build Status](https://travis-ci.org/thomasburguiere/ikey-plus.svg?branch=master)](https://travis-ci.org/thomasburguiere/ikey-plus) [![codecov.io](https://codecov.io/github/thomasburguiere/ikey-plus/coverage.svg?branch=master)](https://codecov.io/github/thomasburguiere/ikey-plus?branch=master) [![Codacy Badge](https://api.codacy.com/project/badge/grade/4d3260ee5aab4f03bd70f643d503bb41)](https://www.codacy.com/app/thomas-burguiere/ikey-plus)

### Build Status `develop` [![Build Status](https://travis-ci.org/thomasburguiere/ikey-plus.svg?branch=develop)](https://travis-ci.org/thomasburguiere/ikey-plus) [![codecov.io](https://codecov.io/github/thomasburguiere/ikey-plus/coverage.svg?branch=develop)](https://codecov.io/github/thomasburguiere/ikey-plus?branch=develop) [![Codacy Badge](https://api.codacy.com/project/badge/grade/4d3260ee5aab4f03bd70f643d503bb41)](https://www.codacy.com/app/thomas-burguiere/ikey-plus)

## Authors :

- Thomas Burguiere (thomas.burguiere@gmail.com)
- Florian Causse
- Visotheary Ung
- Régine Vignes-Lebbe

## Disclaimer ##

This is a fork of one of the app I worked on during my days at [Laboratoire d'Informatique et Systématique](http://www.infosyslab.fr) (LIS) at Université Pierre et Marie Curie, Paris, France, from 2011 to 2013. The original source code is publicly available [here](https://code.google.com/p/ikey-plus/) and the corresponding publication is available [there](http://sysbio.oxfordjournals.org/content/62/1/157.long). This is still a work in progress on many aspects, the code is a bug-ridden mess, but it does work :).

## What is this app doing?

This app generates [single access identification keys](https://en.wikipedia.org/wiki/Single-access_key), using [SDD](http://wiki.tdwg.org/twiki/bin/view/SDD/Version1dot1) formatted file as input. SDD is an XML standard used in systematic biology to store descriptive data.

If your descriptive data is stored in the delta format you can convert it to sdd using the [deltaToSdd](http://www.identificationkey.fr/deltatosdd/) webservice. If your descriptive data is stored in the Xper2 format you can convert it to sdd using the Xper2 software Export option.

## Building the app##
To build and use the app, you will need [Apache Maven](https://maven.apache.org/). Once you have maven installed, go to the root of the project, and run `mvn package`.


## Usage ##

### Java API ###

```java
// setup generator config with default values
IkeyConfig config = IkeyConfig.builder().build();

// initialize SDD parser
SDDParser parser = new SDDSaxParser();
DataSet dataset = sddParser.parseDataset(sddURL, config);

// get key
IdentificationKeyGenerator generator = new IdentificationKeyGeneratorImpl();
SingleAccessKeyTree key = generator.getIdentificationKey(dataset, config);

// dump key to a file
boolean statisticsEnabled = true or false;
File resultFile = SingleAccessKeyTreeDumper
					.dumpFlatHtmlFile("result file header", key, statisticsEnabled, outputFolder);
```

### REST API ##

Coming soon...

## Contributing ##
Feel free to contribute. If you don't know how, code [coverage](https://codecov.io/github/thomasburguiere/ikey-plus?branch=master) and code [quality](https://www.codacy.com/app/thomas-burguiere/ikey-plus/dashboard) can always be improved.
