FROM ghcr.io/graalvm/jdk-community:17 AS builder

WORKDIR /build

COPY .. /build

# sudo apt-get install build-essential libz-dev zlib1g-dev

# Build
RUN ./gradlew nativeCompile

# The deployment Image
FROM scratch

EXPOSE 8080

# Copy the native executable into the containers
COPY --from=builder /build/workout-core/build/native/nativeCompile/workout-core app
ENTRYPOINT ["/app"]