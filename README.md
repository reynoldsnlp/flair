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

## Using Bing API

FLAIR utlizes the Bing search api. In order to access Bing's services, you must create an account with Microsoft Azure. You can sign up for a free Microsoft account here https://azure.microsoft.com/en-us/services/cognitive-services/bing-web-search-api/. This account will provide you with many service options. For this project you must create a Cognitive Services resource. 

There are multiple ways of storing this api key. FLAIR is expecting the api key to be stored in an environment variable called BING_API. There is also a text file reader in the utilities folder available for use if you wish to store your api key in a text file. 
