# BUILDER
FROM ibm-semeru-runtimes:open-17-jdk-jammy AS builder

ARG GITHUB_TOKEN

COPY . /build
WORKDIR /build

RUN ./gradlew build shadowJar proguard -Dorg.gradle.project.githubToken=$GITHUB_TOKEN --no-daemon

RUN \
    JAR=$(find /build/brut.apktool/apktool-cli/build/libs/apktool-*.jar |grep -v cli-all) &&\
    mv ${JAR} /build/apktool.jar

# BASE
FROM ibm-semeru-runtimes:open-17-jre-jammy AS base

ARG COMMAND_LINE_TOOLS_VERSION=10406996
ARG ANDROID_SDK_ROOT=/opt/android-sdk

COPY --from=builder /build/apktool.jar /usr/local/bin/apktool.jar
COPY --from=builder /build/scripts/linux/apktool /usr/local/bin/apktool

RUN \
    apt-get update && \
    chmod +x /usr/local/bin/apktool /usr/local/bin/apktool.jar && \
    apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        unzip \
        wget \
        git \
        openssl \
        zipalign && \
    curl -o /tmp/tools.zip https://dl.google.com/android/repository/commandlinetools-linux-${COMMAND_LINE_TOOLS_VERSION}_latest.zip && \
    mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools && \
    unzip -q /tmp/tools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools && \
    mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest && \
    rm -v /tmp/tools.zip && \
    export PATH=${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${PATH} && \
    mkdir -p /root/.android/ && touch /root/.android/repositories.cfg && \
    yes | sdkmanager --licenses && \
    sdkmanager "build-tools;34.0.0" && \
    ln -s ${ANDROID_SDK_ROOT}/build-tools/34.0.0 /opt/bin && \
    rm -rf /var/lib/apt/lists/*

ENV PATH=${PATH}:/opt/bin   # Formato moderno para evitar warning

CMD ["/usr/local/bin/apktool"]
