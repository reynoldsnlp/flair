# FLAIR (Foreign Language Acquisition Information Retrieval)

Forked from [FLAIR project](http://sifnos.sfs.uni-tuebingen.de/FLAIR/), which
provides web search for language learning. The upstream version of FLAIR has
English and German modules. We intend to extend FLAIR with other language
modules, beginning with Russian.

# Relevant citations

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

FLAIR utilizes a maven build system. In order to compile this project, maven must be installed. For more information on using maven, click here https://maven.apache.org/. Additionally, please make sure you have the correct java version. FLAIR is built using java 8. 

Additionally, FLAIR relies on some local dependencies. [Click here](https://drive.google.com/open?id=1TE2x4iUilulFpGwi0xgZmZmLy2R0Di2c) to get the zip file of the necessary local dependencies. Unzip this file in src/main/webapp/WEB-INF.

## Using Bing API

FLAIR utlizes the Bing search api. In order to access Bing's services, you must create an account with Microsoft Azure. You can sign up for a free Microsoft account here https://azure.microsoft.com/en-us/services/cognitive-services/bing-web-search-api/. This account will provide you with many service options. For this project you must create a Cognitive Services resource. 

There are multiple ways of storing this api key. FLAIR is expecting the api key to be stored in an environment variable called BING_API. There is also a text file reader in the utilities folder available for use if you wish to store your api key in a text file. 

## Third Party Dependencies 

In order to run our russian extension, you may need to use some third party dependencies which are not supported by maven. Follow this [link](https://stanfordnlp.github.io/CoreNLP/model-zoo.html) to access the necessary russian language models. You will note that the russian models uses the latest code on github. In order to use the latest github code with maven, you will need to create a jar of the most current CoreNLP code. Click [here](https://stanfordnlp.github.io/CoreNLP/download.html) and then read the "Steps to setup from the GitHub HEAD version" section to get a jar file of the current code. After acquiring both the CoreNLP jar and the russian models jar, you will need to install both jars to your local maven repository. Maven provides documentation on how to accomplish this [here](https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html). You must update the pom to include the correct versions, artifactID's, ect. To line things up with the provided pom.xml file, follow the naming of the dependencies below. 

```
  <dependency>
      <groupId>edu.stanford.nlp</groupId>
      <artifactId>stanford-corenlp</artifactId>
      <version>master-SNAPSHOT</version>
   </dependency>
   <dependency>
      <groupId>edu.stanford.nlp</groupId>
      <artifactId>stanford-corenlp-russian-models</artifactId>
      <version>master-SNAPSHOT</version>
   </dependency>
    
```
