FROM ubuntu:latest

# docker run --rm -it -p 8080:8080 --name=flair-2.0_App flair-2.0image
# docker system prune --all --force --volumes
# docker build --build-arg local_api=$BING_API -t flair-2.0image .

ARG local_api
ENV BING_API=$local_api

RUN apt-get -y update && apt-get -y upgrade && apt-get -y install openjdk-8-jdk wget && apt-get -y install maven
RUN mkdir /usr/local/tomcat && mkdir /usr/local/flair
RUN wget http://www-us.apache.org/dist/tomcat/tomcat-8/v8.5.41/bin/apache-tomcat-8.5.41.tar.gz -O /tmp/tomcat.tar.gz
RUN cd /tmp && tar xvfz tomcat.tar.gz && cp -Rv /tmp/apache-tomcat-8.5.41/* /usr/local/tomcat/

ADD . /usr/local/flair 
ADD ./localDeps/stanford-tregex-3.6.0-javadoc.jar /usr/local/flair/src/main/webapp/WEB-INF/lib/
ADD ./localDeps/stanford-tregex-3.6.0-sources.jar /usr/local/flair/src/main/webapp/WEB-INF/lib/
ADD ./localDeps/stanford-tregex-3.6.0.jar /usr/local/flair/src/main/webapp/WEB-INF/lib/
ADD ./localDeps/stanford-corenlp-russian-models-master-SNAPSHOT.jar /usr/local/flair/src/main/webapp/WEB-INF/lib/

WORKDIR /usr/local/flair
RUN mvn install:install-file -Dfile=./src/main/webapp/WEB-INF/lib/stanford-corenlp-russian-models-master-SNAPSHOT.jar -DgroupId=edu.stanford.nlp -DartifactId=stanford-corenlp-russian-models -Dversion=master-SNAPSHOT -Dpackaging=jar && mvn install
#RUN mvn install
RUN cp -r target/flair-2.0 /usr/local/tomcat/webapps/
WORKDIR /usr/local/tomcat/bin
RUN ./startup.sh


