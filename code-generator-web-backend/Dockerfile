# Docker 镜像构建
#FROM maven:3.8.1-jdk-8-slim as builder
FROM openjdk:8-jre-slim

# Copy local code to the container image.
WORKDIR /app
#COPY pom.xml .
#COPY src ./src
# 不在构建镜像的过程去打包maven依赖 直接在本地构建好之后COPY 只需要将本地构建好的jar包文件以及dockerfile文件上传到同一目录下即可
COPY ./code-generator-web-backend-0.0.1-SNAPSHOT.jar app.jar

# Build a release artifact.
# RUN mvn package -DskipTests

# Run the web service on container startup.
CMD ["java","-jar","/app/app.jar","--spring.profiles.active=prod"]