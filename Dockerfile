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
RUN apt-get upgrade -y
RUN apt-get install -y wireguard
RUN mkdir -p /app/clientConfigs
RUN chmod 755 /app/clientConfigs
RUN sysctl net.ipv4.ip_forward=1

COPY --from=build /app/target/WG-SAP-0.1.jar /app/WG-SAP-0.1.jar

EXPOSE 5000
EXPOSE 51820

ENV serverIP localhost

CMD cd /app/;umask 077; wg genkey | tee Server_PrivateKey | wg pubkey > Server_PublicKey;java -jar /app/WG-SAP-0.1.jar