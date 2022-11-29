FROM ubuntu:latest

RUN apt update
RUN apt install -y openjdk-11-jdk
RUN apt install -y youtube-dl

WORKDIR /usr/src/app

COPY . .

CMD [ "java", "-jar", "./package/musicmanager-*.jar" ]