# run --rm -it -p 8080:8080 -e BING_API=$BING_API --name=reynoldsnlp/flair-2.0
FROM maven:3-jdk-8 as flair-builder

COPY . /opt/flair
WORKDIR /opt/flair
RUN mvn install


# docker system prune --all --force --volumes
# docker build -t flair-2.0image .

#ARG local_api
#ENV BING_API=$local_api

#RUN apt-get -y update && apt-get -y upgrade && apt-get -y install openjdk-8-jdk wget && apt-get -y install maven
#RUN mkdir /usr/local/tomcat && mkdir /usr/local/flair
#RUN wget http://www-us.apache.org/dist/tomcat/tomcat-8/v8.5.41/bin/apache-tomcat-8.5.41.tar.gz -O /tmp/tomcat.tar.gz
#RUN cd /tmp && tar xvfz tomcat.tar.gz && cp -Rv /tmp/apache-tomcat-8.5.41/* /usr/local/tomcat/

#ADD . /usr/local/flair 
#WORKDIR /usr/local/flair
#RUN cp localDeps/*.jar src/main/webapp/WEB-INF/lib/
#RUN mvn install:install-file -Dfile=./src/main/webapp/WEB-INF/lib/stanford-corenlp-russian-models-master-SNAPSHOT.jar -DgroupId=edu.stanford.nlp -DartifactId=stanford-corenlp-russian-models -Dversion=master-SNAPSHOT -Dpackaging=jar && mvn install
#RUN cp -r target/flair-2.0 /usr/local/tomcat/webapps/
#WORKDIR /usr/local/tomcat/webapps/
#RUN ln -s /usr/local/flair/target/flair-2.0 .
#RUN ./startup.sh



# tomcat stuff, make sure to remove target folder from dockerignore file before running this
FROM tomcat:8.5.41-jdk8

COPY --from=flair-builder /opt/flair/target/flair-2.0 /usr/local/tomcat/webapps/flair-2.0

CMD ["catalina.sh", "run"]
# docker run -it --rm -p 8080:8080 -e BING_API=$BING_API flair-2.0image

