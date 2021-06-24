FROM maven:3.6.3-ibmjava-8-alpine AS postServiceBuild
ARG STAGE=dev
WORKDIR /usr/src/postService
COPY . .
RUN mvn package -P${STAGE} -DskipTests


FROM openjdk:8-jdk-alpine AS postServiceRuntime
WORKDIR /app
COPY --from=postServiceBuild /usr/src/postService/target/postService.jar ./
EXPOSE 8080
CMD java -jar postService.jar