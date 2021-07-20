FROM adoptopenjdk/openjdk11 as build

RUN mkdir /tmp/build

WORKDIR /tmp/build

COPY ../.  /tmp/build

RUN ./mvnw package

FROM adoptopenjdk/openjdk11

RUN mkdir -p /deployments/lib/

COPY --from=build /tmp/build/adoptopenjdk-updater-parent/adoptopenjdk-api-v3-frontend/target/lib/* /deployments/lib/

COPY --from=build /tmp/build/adoptopenjdk-updater-parent/adoptopenjdk-api-v3-updater/target/adoptopenjdk-api-v3-updater-*-jar-with-dependencies.jar /deployments/adoptopenjdk-api-v3-updater-runner.jar
COPY --from=build /tmp/build/adoptopenjdk-updater-parent/adoptopenjdk-api-v3-frontend/target/adoptopenjdk-api-*-runner.jar /deployments/adoptopenjdk-api-v3-frontend.jar

RUN useradd java -m;chown -R java /deployments
USER java

CMD ["java", "-jar", "/deployments/adoptopenjdk-api-v3-updater-runner.jar"]
