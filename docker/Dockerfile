# BUILDER
# =====================================================
FROM ibm-semeru-runtimes:open-17-jdk-jammy AS builder

# Instalar JDK 8 para la toolchain de Gradle
RUN apt-get update && apt-get install -y openjdk-8-jdk && rm -rf /var/lib/apt/lists/*

# Indicar a Gradle que use JDK 8 para compilar
ENV MATRIX_JAVA=8

ARG GITHUB_TOKEN

COPY . /build
WORKDIR /build

RUN ./gradlew build shadowJar proguard -Dorg.gradle.project.githubToken=$GITHUB_TOKEN --no-daemon

RUN \
    # Relocate for easier copying
    JAR=$(find /build/brut.apktool/apktool-cli/build/libs/apktool-*.jar |grep -v cli-all) &&\
    mv ${JAR} /build/apktool.jar

# BASE
# =====================================================
FROM ibm-semeru-runtimes:open-17-jre-jammy AS base

# Latest version as of 2023.10.01
# Ref: https://developer.android.com/studio/index.html#command-line-tools-only
ARG COMMAND_LINE_TOOLS_VERSION=10406996

ARG ANDROID_SDK_ROOT=/opt/android-sdk

COPY --from=builder /build/apktool.jar /usr/local/bin/apktool.jar
COPY --from=builder /build/scripts/linux/apktool /usr/local/bin/apktool

RUN \
    # Update
    apt-get update && \
    \
    # Apktool final setup
    chmod +x /usr/local/bin/apktool /usr/local/bin/apktool.jar && \
    \
    # Instalar dependencias necesarias (sin sdkmanager del sistema)
    apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        unzip \
        wget \
        git \
        openssl \
        zipalign && \
    \
    # Descargar Command Line Tools de Android
    curl -o /tmp/tools.zip https://dl.google.com/android/repository/commandlinetools-linux-${COMMAND_LINE_TOOLS_VERSION}_latest.zip && \
    mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools && \
    unzip -q /tmp/tools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools && \
    mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest && \
    rm -v /tmp/tools.zip && \
    \
    # Configurar variables para usar el sdkmanager recién instalado
    export PATH=${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${PATH} && \
    mkdir -p /root/.android/ && touch /root/.android/repositories.cfg && \
    \
    # Aceptar licencias automáticamente (usando el sdkmanager correcto)
    yes | sdkmanager --licenses && \
    \
    # Instalar una versión específica de build-tools (cambia la versión si es necesario)
    sdkmanager "build-tools;34.0.0" && \
    ln -s ${ANDROID_SDK_ROOT}/build-tools/34.0.0 /opt/bin && \
    \
    # Limpiar
    rm -rf /var/lib/apt/lists/*

ENV PATH ${PATH}:/opt/bin

CMD ["/usr/local/bin/apktool"]
