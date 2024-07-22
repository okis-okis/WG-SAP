#
# BUILD STAGE
#
FROM maven:3.9.8-sapmachine-22 as build
COPY /src /app/src
COPY pom.xml /app
RUN mvn -f /app/pom.xml clean package

#
# PACKAGE STAGE
#
FROM maven:3.9.8-sapmachine-22

RUN apt-get update
RUN apt-get install -y wireguard
# RUN wg pubkey < /app/Server_PrivateKey > /app/Server_PublicKey

COPY --from=build /app/target/wg-admin-0.0.1-SNAPSHOT.jar /app/wg-admin-0.0.1-SNAPSHOT.jar

EXPOSE 5000

# CMD ["wg","pubkey","<", "/app/Server_PrivateKey"]  
# cd /app/;umask 077;wg genkey | tee Server_PrivateKey | wg pubkey > Server_PublicKey;
CMD java -jar /app/wg-admin-0.0.1-SNAPSHOT.jar