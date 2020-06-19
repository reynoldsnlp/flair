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

FLAIR utilizes the Bing search api. In order to access Bing's services, you must
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
found that version `3.9.2` is recent enough. After downloading the Russian
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

### Cg3

The following command-line tools are needed:
- `vislcg3`
- `cg-conv`

To install on linux use `apt-get install cg3`

To install on Mac use `brew install homebrew/science/vislcg3`

To install on Windows:

1. Go to https://apertium.projectjj.com/win32/nightly/

2. Download `cg3-latest.zip`

3. Decompress/extract the zip file

4. In the extracted directory you'll find a `cg3` directory; move this directory to `C:\Program Files`

5. Add `C:\Program Files\cg3\bin` to the `PATH` system environment variable

**Note that `cg-conv` is not fully reliable on Windows, and some analyses will not be complete if `cg-conv` is installed in a Windows environment**
(as of VISL CG-3 Disambiguator version 1.3.1.13891).
