@echo off
echo "Server start"
echo "����ip"
ipconfig|find "IPv4"
java -jar Server.jar
-pause