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
RUN mkdir -p /app/clientConfigs
RUN chmod 755 /app/clientConfigs

COPY --from=build /app/target/wg-admin-0.0.1-SNAPSHOT.jar /app/wg-admin-0.0.1-SNAPSHOT.jar

EXPOSE 5000

CMD cd /app/;umask 077; wg genkey | tee Server_PrivateKey | wg pubkey > Server_PublicKey;java -jar /app/wg-admin-0.0.1-SNAPSHOT.jar