# tomcat stuff, make sure to remove target folder from dockerignore file before running this
FROM tomcat:8.5.41-jdk8
RUN apt-get update && apt-get install -y maven
WORKDIR /usr/local/tomcat/
COPY . flair
WORKDIR /usr/local/tomcat/flair
RUN mvn install:install-file -Dfile=localDeps/stanford-russian-corenlp-models-master-SNAPSHOT.jar -DgroupId=edu.stanford.nlp -DartifactId=stanford-corenlp-russian-models -Dversion=master-SNAPSHOT -Dpackaging=jar && mvn install
RUN mv target/flair-2.0 /usr/local/tomcat/webapps/
WORKDIR /usr/local/tomcat
RUN ls webapps
RUN rm -r flair

CMD ["catalina.sh", "run"]
# to run on user defined network
# docker run -it --rm -p 8080:8080 -e BING_API=$BING_API --network flair-net --name flair-2.0image flair-2.0image
# to build
# docker build --network flair-net -t flair-2.0image .
# to create network
# docker network create flair-net
