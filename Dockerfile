FROM ubuntu:latest

RUN apt update
RUN apt upgrade
RUN apt install -y openjdk-11-jdk python3-pip
RUN pip install --upgrade youtube_dl

WORKDIR /usr/src/app

COPY . .

CMD [ "./gradlew", "bootJar" ]
CMD [ "java", "-jar", "./build/libs/musicmanager-*.jar" ]
