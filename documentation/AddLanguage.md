# Language Addition Guide

This is a guide for adding language support for FLAIR. This guide will be
written with the Stanford Core NLP in mind. Although the project is abstracted
enough to support custom parsers, it will be simplest to use the Stanford Core
NLP. 

## Shared

1. Add a case in `/com/flair/shared/grammar/Language.java` for the language you
   wish to add. 
2. Add a case in `/com/flair/shared/grammar/DefaultVocabularyList.java` for the
   language you wish to add. It is not required to have an actual vocabulary
   list for the new language, you can just use an empty array list. However,
   having a vocabulary list will provide additional criteria for ranking
   documents. 

## Server 

1. Add a case in `/com/flair/server/grammar/BingSearchAgent.java` for the
   language you wish to add. You will also need to add parameters for the
   search market and the query prefix.
   [Bing](https://docs.microsoft.com/en-us/azure/cognitive-services/bing-web-search/language-support)
   provides documentation for the languages they support, including the region
   and market codes necessary for the search market and query prefix
   parameters.  
2. In the folder `/com/flair/server/parser/` you will need to create a parsing
   strategy for the new language. This is where you will tag the document with
   different grammar constructions. The new parsing strategy class will need to
   extend the `BasicStanfordDocumentParserStrategy` class.
3. In the file
   `/com/flair/server/parser/BasicStanfordDocumentParserStrategy.java` you will
   need to add a case for your language so you can use your parsing strategy. 
4. The FLAIR server implements an `AbstractDocument` interface which represents
   a parsed text document. To add a new language, you have two options. One
   option is to add a language case in the
   `/com/flair/server/parser/Document.java` class. If you wish to have your new
   language work like our English, German and Russian, implementations, this
   option is recommended. Another option is to create another implementation of
   the `AbstractDocument` interface. This option is intended for languages that
   require an implementation that differs greatly from the current language
   implementations. Our Arabic implementation uses a different ranking scale,
   so we have a different class for Arabic documents. 
5. In the file `/com/flair/server/utilities/AbstractTextExtractor.java`, add a
   case for the new language following the example of previous language cases,
   utilizing the language code for the new language.  
6. In the file `/com/flair/server/parser/StanfordDocumentParser.java`, add a
   new language case in the class constructor. You will need to set the
   appropriate pipeline properties for the new language. The
   [Stanford Core NLP](https://stanfordnlp.github.io/CoreNLP/index.html) site
   provides information on implementing a language other than English,
   including the necessary dependencies and pipeline properties. 
7. In the file `/com/flair/server/taskmanager/MasterJobPipeline.java`, you will
   need to add an `AbstractParsingStrategyFactory` object and a
   `DocumentParserPool` object for your new language. In the
   `MasterJobPipeline` constructor, set the document pool to `null` and set the
   new language parsing strategy to the `MasterParsingFactoryGenerator` result
   that corresponds to the new language. In the `getStrategyForLanguage()`
   method, add a case for the new language. In the `getParserPoolForLanguage()`
   method, add a case for the new language as well. 
8. If you simply added a case to the `Document.java` object, then there is
   nothing else to be done on the server. If you used a unique implementation
   of the `AbstractDocument` interface, you will need to add a case for your
   new document if it uses methods unique to that class in the file
   `/com/flair/server/interop/session/SessionState.java`. Within this file,
   inspect the functions `generateRankableDocument()` and
   `generateRankableWebSearchResult()` to add document properties that you will
   be sending to the client.  

## Client

Adding new language support on the front end is much more open ended than
adding new language support on the backend due to the variety of design
decisions one can make. Therefore, this section will contain notes about the
front end rather than a step by step tutorial. The front end is built on the
Google Web Toolkit (GWT) framework. If you are unfamiliar with GWT development,
documentation and introductory tutorials can be found
[here](http://www.gwtproject.org/). 

### Internationalization and Localization 

FLAIR was developed with internationalization in mind, and in it's current
state the front end can be displayed in both German and English. With that in
mind, FLAIR makes use of localized resources to quickly switch between
languages. The localized string resources can be found in the
`/com/flair/client/localization/resources/` folder. The application makes use
of two resource files per language. One for general strings, and one for
strings specific to grammatical constructions. There is a string value that is
associated with tags, making these strings accessible interchangeably in the
client code. For most widgets on the site, if you wish to add a field, you will
first need to add a localized resource associated with the field you would like
to add. This will be done in the `/com/flair/client/localization/resources/`
folder in the `strings-lang-general.tsv` files. Then you can add your new
string as part an element in the `ui.xml` file corresponding to your widget.
This will need to be done for all supported languages. If you are adding unique
grammatical constructions, you follow the same general process, except you will
be placing your localized resource in the `strings-lang-constructions.tsv`. The
files that correspond with the grammar sliders can be found in the
`/com/flair/client/presentation/widgets/sliderbundles/` folder. To learn more
about GWT internationalization and localization, click
[here](http://www.gwtproject.org/doc/latest/DevGuideI18n.html).
