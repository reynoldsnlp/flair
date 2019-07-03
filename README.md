# FLAIR (Foreign Language Acquisition Information Retrieval)

Forked from [FLAIR project](http://sifnos.sfs.uni-tuebingen.de/FLAIR/), which
provides web search for language learning. The upstream version of FLAIR has
English and German modules. We intend to extend FLAIR with other language
modules, beginning with Arabic and Russian.

## Citations

Please cite the following to refer to this work, and check back for future
publications on ongoing work.

```
@inproceedings{chinkina2016linguistically,
  title={Linguistically aware information retrieval: Providing input enrichment for second language learners},
  author={Chinkina, Maria and Meurers, Detmar},
  booktitle={Proceedings of the 11th Workshop on Innovative Use of NLP for Building Educational Applications},
  pages={188--198},
  year={2016}
}
```

## Installation 

Requirements:

* `maven`
* `java 8`
* `tomcat`

FLAIR utilizes a maven build system. In order to compile this project, maven
must be installed. For more information on using maven, click here
https://maven.apache.org/.

## Using Bing API

FLAIR utlizes the Bing search api. In order to access Bing's services, you must
create an account with Microsoft Azure. You can sign up for a free Microsoft
account here
https://azure.microsoft.com/en-us/services/cognitive-services/bing-web-search-api/.
This account will provide you with many service options. For this project you
must create a Cognitive Services resource. 

There are multiple ways of storing this api key. FLAIR is expecting the api key
to be stored in an environment variable called `BING_API`. There is also a text
file reader in the utilities folder available for use if you wish to store your
api key in a text file. 

## Third Party Dependencies 

In order to run our Russian extension, you need to use some third party
dependencies which are not supported by maven. Follow this
[link](https://stanfordnlp.github.io/CoreNLP/model-zoo.html) and download the
jar file corresponding with the Russian CoreNLP. Although the stanford website
says that the latest github code is needed to run the Russian models, we have
found that version `3.9.2` is recent enough. After dowloading the Russian
language models, you will need to add them to your local repository. Maven
provides documentation on how to accomplish this
[here](https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html).
You must update the pom to include the correct versions, artifactID's, etc. To
line things up with the provided pom.xml file, follow the naming of the
dependencies below. 

```
   <dependency>
      <groupId>edu.stanford.nlp</groupId>
      <artifactId>stanford-corenlp-russian-models</artifactId>
      <version>master-SNAPSHOT</version>
   </dependency> 
```
