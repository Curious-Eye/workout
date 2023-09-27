# Cache gradle dependencies
FROM gradle:8.3.0-jdk17-graal AS cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home
COPY ./workout-core/build.gradle /home/gradle/java-code/workout-core/
COPY ./core-ext/build.gradle /home/gradle/java-code/core-ext/
COPY ./settings.gradle /home/gradle/java-code/
COPY ./gradle.properties /home/gradle/java-code/
WORKDIR /home/gradle/java-code
RUN gradle clean downloadDependencies -i --stacktrace

# Build native executable with
#FROM ghcr.io/graalvm/graalvm-community:17 AS build
#COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
#RUN microdnf install -y findutils zip unzip wget && \
#    microdnf clean all
#
#WORKDIR /
#
#RUN wget -O gradle.zip https://services.gradle.org/distributions/gradle-8.3-bin.zip
#RUN unzip gradle.zip
#RUN rm gradle.zip
#RUN mv gradle-8.3 gradle
#ENV PATH="$PATH:/gradle/bin"
#
#WORKDIR /app
#COPY . .
#RUN gradle :workout-core:nativeCompile --parallel --no-daemon

FROM gradle:8.3.0-jdk17-graal AS build
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle

WORKDIR /app
COPY . .
RUN gradle :workout-core:bootJar --parallel --no-daemon

# Second stage: Lightweight debian-slim image
FROM ghcr.io/graalvm/jdk-community:20.0.1 AS run

COPY --from=build /app/workout-core/build/libs/workout-core-0.0.1.jar /app/workout-core.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/workout-core.jar"]