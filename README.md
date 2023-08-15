# FLAIR (Form-focused, Linguistically Aware Information Retrieval)

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

#### Citing Arabic module

@mastersthesis{hveem2019raft,
  author={Hveem, Joshua},
  title={RAFT: Readable Arabic Finding Tool},
  school={{B}righam {Y}oung {U}niversity},
  year={2019}
}

## Installation

### Bing API

FLAIR utilizes the Bing search API. In order to access Bing's services, you
must create an account with Microsoft Azure. You can sign up for a free
Microsoft account here
https://azure.microsoft.com/en-us/services/cognitive-services/bing-web-search-api/.
This account will provide you with many service options. For this project you
must create a Cognitive Services resource.

There are multiple ways of storing this API key. FLAIR is expecting the API key
to be stored in an environment variable called `BING_API`. If you intend to use
`docker` (recommended), store the API key in `flair-variables.env`, using
`flair-variables.env.example` as an example. Never commit this file to git!
There is also a text file reader in the utilities folder available for use if
you wish to store your API key in a text file.

### Using `docker compose` (recommended)

The simplest way to deploy the project is using `docker compose`, which relies
on the containers defined in [`compose.yaml`](compose.yaml), which in turn
depends on images defined in [Dockerfile\_base](Dockerfile_base) and
[Dockerfile](Dockerfile), as well as services for a [Stanza
API](https://github.com/lingmod-tue/stanza-api) and
[Madamira](https://github.com/reynoldsnlp/madamira_container).

To use docker, simply install `docker` (including `docker-compose`), or a
drop-in replacement, such as `podman` or `orbstack`. Then in the root directory
of the repository, run...

```bash
docker compose up -d flair
```

Note that the target `flair` implies all languages, including both Arabic (and
the dependent `madamira` container) and Persian (and its dependent `stanza-api`
container). If you wish to deploy only Arabic, use the target `arabic`.  If you
wish to deploy only Persian, use the target `stanza`. If you wish to deploy
only English, German, and Russian, use the target `corenlp`.

This command will take a while. Go play a game of rapid chess.

Once complete, visit http://localhost:8080/flair-2.0/ to test your deployment.

To see summary of all running containers, run `docker ps`.

To view the logs, run...  (replace `<container_name>` with the name of the
desired container)

```bash
docker logs <container_name>
```

...or if you want to "follow" the logs as they run, use the following command.
(When finished, use <kbd>Ctrl</kbd> + <kbd>C</kbd> to exit.)

```bash
docker logs -f <container_name>
```

To shut down the docker containers run...

```bash
docker compose down
```

#### Re-deploying local changes to docker

Run `docker ps` to see all running containers. Depending on whether your
requested target for `docker compose` was `flair`/`arabic`/`corenlp`/`stanza`,
the main container should be named `flair-<target>-1`. The following commands
assume that `stanza` was requested, so the container is named `flair-stanza-1`.

```bash
docker stop flair-stanza-1
docker rmi --force reynoldsnlp/flair
docker compose up -d stanza
```

### Installing locally

Requirements:

* `maven`
* `java 8`
* `tomcat`

FLAIR utilizes a maven build system. In order to compile this project, maven
must be installed. For more information on using maven, click here
https://maven.apache.org/.

#### Third Party Dependencies

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

##### Cg3

For the Russian module, the following command-line tools are needed:
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
