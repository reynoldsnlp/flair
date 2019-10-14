FROM reynoldsnlp/flair-base AS flair-builder

WORKDIR /opt/flair
RUN git pull && mvn clean install

# final image
FROM tomcat:8.5.41-jdk8
COPY --from=flair-builder /opt/flair/target/flair-2.0 /usr/local/tomcat/webapps/flair-2.0
CMD ["catalina.sh", "run"]
