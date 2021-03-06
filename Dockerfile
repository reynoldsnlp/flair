FROM reynoldsnlp/flair-base AS flair-builder

WORKDIR /opt/flair
RUN git pull && mvn clean install

# final image
FROM tomcat:8.5.41-jdk8
RUN apt-get -y update && wget https://apertium.projectjj.com/apt/install-nightly.sh -O - | bash && apt-get install -y cg3
COPY --from=flair-builder /opt/flair/target/flair-2.0 /usr/local/tomcat/webapps/flair-2.0
CMD ["catalina.sh", "run"]
