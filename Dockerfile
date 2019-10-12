FROM maven:3-jdk-8 AS flair-builder

WORKDIR /opt
RUN git clone https://github.com/reynoldsnlp/flair.git
WORKDIR /opt/flair

# add russian corenlp model
RUN apt-get -y update \
    && apt-get -y upgrade \
    && apt-get install -y python3 python3-pip \
    && python3 -m pip install gdown \
    && mkdir src/main/webapp/WEB-INF/lib \
    && gdown https://drive.google.com/uc?id=1_0oU8BOiYCqHvItSsz0BjJnSNp8PRWlC \
       -O src/main/webapp/WEB-INF/lib/stanford-corenlp-russian-models-master-SNAPSHOT.jar \
    && mvn install:install-file \
       -Dfile=src/main/webapp/WEB-INF/lib/stanford-corenlp-russian-models-master-SNAPSHOT.jar \
       -DgroupId=edu.stanford.nlp \
       -DartifactId=stanford-corenlp-russian-models \
       -Dversion=master-SNAPSHOT \
       -Dpackaging=jar
# compile
RUN mvn install

# final image
FROM tomcat:8.5.41-jdk8
COPY --from=flair-builder /opt/flair/target/flair-2.0 /usr/local/tomcat/webapps/flair-2.0
CMD ["catalina.sh", "run"]
