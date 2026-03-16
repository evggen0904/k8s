FROM eclipse-temurin:21

RUN mkdir /opt/app

COPY target/app.jar /opt/app/app.jar

EXPOSE 9080

CMD ["java", "-jar", "/opt/app/app.jar"]