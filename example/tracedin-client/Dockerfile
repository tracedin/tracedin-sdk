FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

ARG APP_NAME=tracedin-client
ARG VERSION=0.0.1
ARG ARTIFACT_NAME=${APP_NAME}-${VERSION}
ARG OUTPUT=example/${APP_NAME}/build

COPY . .

RUN ./gradlew :example:${APP_NAME}:build

RUN java -Djarmode=layertools -jar ${OUTPUT}/libs/${ARTIFACT_NAME}.jar extract --destination ${OUTPUT}/extracted

FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp

ARG OUTPUT=example/tracedin-client/build
ARG EXTRACTED=/workspace/app/${OUTPUT}/extracted

COPY --from=build ${EXTRACTED}/dependencies/ ./
COPY --from=build ${EXTRACTED}/spring-boot-loader/ ./
COPY --from=build ${EXTRACTED}/snapshot-dependencies/ ./
COPY --from=build ${EXTRACTED}/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
