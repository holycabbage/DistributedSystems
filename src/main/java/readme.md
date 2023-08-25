Method to test the code:
1. cd to DistributedSystems

2. Compile mavens
code: mvn compile

3. Build the java project(jar file)
code: mvn clean package

4. Run the jar file
code: cd target
code: java -jar service.registry-1.0-SNAPSHOT-jar-with-dependencies.jar 8080
code: java -jar service.registry-1.0-SNAPSHOT-jar-with-dependencies.jar 8081
code: java -jar service.registry-1.0-SNAPSHOT-jar-with-dependencies.jar 8082


Code to run zookeeper:
1. cd /Users/zhanghaosong/zookeeper/bin
2. sudo ./zkServer.sh start
3. ./zkCli.sh -server 127.0.0.1:2181