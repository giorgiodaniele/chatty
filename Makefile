MVN = mvn
CLIENT_JAR = chatter-client-1.0.jar
SERVER_JAR = chatter-server-1.0.jar

.PHONY: client server
all:    client server

clean:
	rm -f $(CLIENT_JAR) $(SERVER_JAR)
	$(MVN) -f client/pom.xml clean
	$(MVN) -f server/pom.xml clean

client:
	$(MVN) clean package -f client/pom.xml -Dmaven.test.skip=true -DskipITs=true
	mv client/target/$(CLIENT_JAR) .
	rm -rf client/target

server:
	$(MVN) clean package -f server/pom.xml -Dmaven.test.skip=true -DskipITs=true
	mv server/target/$(SERVER_JAR) .
	rm -rf server/target
