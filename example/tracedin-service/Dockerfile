FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

ARG ARTIFACT_NAME=tracedin-service
ARG VERSION=0.0.1

COPY ../.. .

RUN ./gradlew clean build

RUN java -Djarmode=layertools -jar build/libs/${ARTIFACT_NAME}-${VERSION}.jar extract --destination build/extracted
RUN mkdir -p build/extracted && (cd build/extracted; jar -xf ../${ARTIFACT_NAME}-${VERSION}.jar)

FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
ARG EXTRACTED=/workspace/app/build/extracted
COPY --from=build ${EXTRACTED}/dependencies/ ./
COPY --from=build ${EXTRACTED}/spring-boot-loader/ ./
COPY --from=build ${EXTRACTED}/snapshot-dependencies/ ./
COPY --from=build ${EXTRACTED}/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
