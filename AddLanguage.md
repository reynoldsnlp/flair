# Language Addition Guide
This is a guide for adding language support for FLAIR. This guide will be written with the Stanford Core NLP in mind. Although the project is abstracted enough to support custom parsers, it will be simplest to use the Stanford Core NLP. 

## Shared
1. Add a case in /com/flair/shared/grammar/Language.java for the language you wish to add. 
2. Add a case in /com/flair/shared/grammar/DefaultVocabularyList.java for the language you wish to add. It is not required to have an actual word list for the new language, but it will provide an additional means of analysis. 

## Server 

1. Add a case in /com/flair/server/grammar/BingSearchAgent.java for the language you wish to add. You will also need to add parameters for the search market and the query prefix. [Bing] (https://docs.microsoft.com/en-us/azure/cognitive-services/bing-web-search/language-support) provides documentation for the languages they support, including region and market codes.  

## Client