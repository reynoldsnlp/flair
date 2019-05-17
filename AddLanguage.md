# Language Addition Guide
This is a guide for adding language support for FLAIR. This guide will be written with the Stanford Core NLP in mind. Although the project is abstracted enough to support custom parsers, it will be simplest to use the Stanford Core NLP. 

## Shared
1. Add a case in /com/flair/shared/grammar/Language.java for the language you wish to add. 
2. Add a case in /com/flair/shared/grammar/DefaultVocabularyList.java for the language you wish to add. It is not required to have an actual word list for the new language, but it will provide an additional means of analysis. 

## Server 

1. Add a case in /com/flair/server/grammar/BingSearchAgent.java for the language you wish to add. You will also need to add parameters for the search market and the query prefix. [Bing](https://docs.microsoft.com/en-us/azure/cognitive-services/bing-web-search/language-support) provides documentation for the languages they support, including region and market codes.  
2. In the folder /com/flair/server/parser/ you will need to create a parsing strategy for the new language. This is where you will tag the document with different gramma constructions. The new parsing strategy class will need to extend the BasicStanfordDocumentParserStrategy class.
3. In the file /com/flair/server/parser/BasicStanfordDocumentParserStrategy.java you will need to add a case for your language so you can use your parsing strategy. 
4. The FLAIR server implements an AbstractDocument interface which represents a parsed text document. To add a new language, you have two options. One option is to add a language case in the /com/flair/server/parser/Document.java class. If you wish to have your new language work like our english, german and russian, implementations, this option is recommended. Another option is to create another implementation of the AbstractDocument interface. This option is intended for languages that require an implementation that differs greatly from the current language implementations. Our arabic implementation uses a different ranking scale, so we have a different class for arabic documents. 
5. In the file /com/flair/server/utilities/AbstractTextExtractor.java, add a case for the new language following the example of previous language cases, utilizing the language code for the new language.  
6. In the file /com/flair/server/parser/StanfordDocumentParser.java, add a new language case in the class constructor. You will need to set the appropriate pipeline properties for the new language. The [Stanford Core NLP](https://stanfordnlp.github.io/CoreNLP/index.html) site provides information on implementing a language other than english, including the necessary dependencies and customizing pipeline properties. 
7. In the file /com/flair/server/taskmanager/MasterJobPipeline.java, you will need to add an AbstractParsingStrategyFactory object and an DocumentParserPool object for your new language. In the MasterJobPipeline constructor, set the document pool to null and set the new language parsing strategy to the MasterParsingFactoryGenerator result that corresponds to the new language. In the getStrategyForLanguage() method, add a case for the new language. In the getParserPoolForLanguage() method, add a case for the new language as well. 
8. If you simply added a case to the Document.java object, then there is nothing else to be done on the server. If you used a unique implementation of the AbstractDocument interface, you will need to add a case for your new document if it uses methods unique to that class. 

## Client