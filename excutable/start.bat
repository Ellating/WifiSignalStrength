@echo off
echo "Server start"
echo "±¾»úip"
ipconfig|find "IPv4"
java -jar Server.jar
-pause