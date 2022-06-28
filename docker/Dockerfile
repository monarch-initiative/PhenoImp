FROM ubuntu:bionic

COPY phenoimp-cli-*-distribution.zip /opt/phenoimp/phenoimp-cli-distribution.zip

RUN apt-get update && apt-get --yes upgrade && apt-get --yes install openjdk-17-jre \
    ca-certificates-java \
    procps \
    unzip

RUN mkdir -p /opt/phenoimp /opt/bin \
    && cd /opt/phenoimp \
    && unzip phenoimp-cli-distribution.zip \
    && rm phenoimp-cli-distribution.zip

COPY phenoimp /opt/bin/phenoimp

ENV PATH="/opt/bin:${PATH}"
