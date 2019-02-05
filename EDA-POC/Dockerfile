FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD /build/libs/EDA-POC-1.0.jar app.jar
RUN adduser -D myuser
USER myuser
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
CMD java -Dserver.port=$PORT -jar app.jar