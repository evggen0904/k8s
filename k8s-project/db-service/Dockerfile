FROM eclipse-temurin:21

RUN mkdir /opt/app

COPY target/app.jar /opt/app/app.jar

EXPOSE 9081

CMD ["java", "-jar", "/opt/app/app.jar"]