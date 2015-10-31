Ikey+: Identification key generation API
========================================

Authors : 

- Florian Causse 
- Thomas Burguiere (thomas.burguiere@gmail.com)

Disclaimer
----------
This is a fork of one of the app I worked on during my days at "Laboratoire d'Informatique et Systématique" at Université Pierre et Marie Curie, Paris, France, from 2011 to 2013. The original source code is publicly available [here](https://code.google.com/p/ikey-plus/). The corresponding publication is available [here](http://sysbio.oxfordjournals.org/content/62/1/157.long). Still a work in progress on many aspects, the code is a bug-ridden mess, but it does the job :).

Contributions are welcome !

Build
-----

Run `mvn install`

Usage
-----

Include the generated jar in your app, then:

```java
# setup generator conf example
Utils conf = new Utils();
conf.setFewStatesCharacterFirst(true);
conf.setMergeCharacterStatesIfSameDiscrimination(false);
conf.setPruning(false);
conf.setVerbosity("hs");
conf.setScoreMethod(Utils.XPER);
conf.setWeightContext("CostEffectiveness");
conf.setWeightType(Utils.GLOBAL_CHARACTER_WEIGHT);

# initialize sdd parser
SDDSaxParser sddSaxParser = new SDDSaxParser(new File("inputFile.sdd"), conf);

# initialize key generator
IdentificationKeyGenerator identificationKeyGenerator = 
	new IdentificationKeyGenerator(sddSaxParser.getDataset(), conf);

# compute identification key
SingleAccessKeyTree resultKey = identificationKeyGenerator.getSingleAccessKeyTree();

# dump key to a file
SingleAccessKeyTreeDumper.dumpFlatHtmlFile(header.toString(), resultKey,
					conf.getVerbosity().contains(Utils.STATISTIC_TAG)).getName();
```




