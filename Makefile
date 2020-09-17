.DEFAULT_GOAL := all

all: clientAndContestant qserver

clientAndContestant:
	javac -classpath ./json-simple-1.1.1.jar client/*.java
	echo  'java -XX:+UseSerialGC -cp ./client ClientMain $$1 $$2 $${3}' > contestmeister
	echo 'java -XX:+UseSerialGC -cp ./client ContestantMain $$1 $$2' > contestant

qserver:
	javac -classpath ./json-simple-1.1.1.jar server/*.java
	echo 'java -XX:+UseSerialGC -cp ./json-simple-1.1.1.jar:./server ServerMain' > cserver

clean:
	rm contestmeister
	rm contestant
	rm cserver
