FROM openjdk:alpine
WORKDIR /root
COPY ./out/ .
ARG pghost
ARG pgpass
ENV PGHOST $pghost
ENV PGPASS $pgpass
EXPOSE 8080
CMD java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -jar assembly.jar
