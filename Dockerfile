FROM ubuntu:latest

RUN apt update
RUN apt upgrade
RUN apt install -y openjdk-11-jdk python3-pip
RUN pip install --upgrade youtube_dl
RUN curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp
RUN chmod a+rx /usr/local/bin/yt-dlp

WORKDIR /usr/src/app

COPY . .

CMD [ "./gradlew", "bootJar" ]
CMD [ "java", "-jar", "./build/libs/musicmanager-*.jar" ]
