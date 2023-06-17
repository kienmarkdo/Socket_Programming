# Socket_Programming_Lab
Network socket programming lab for the course CEG3585 Data Communications and Networking at uOttawa.

## Instructions
- Open two terminals in root of project (VSCode is preferred)
- In terminal 1, compile and start the server
    - `javac PacketReceiver.java ; java PacketReceiver`
- In terminal 2, compile and run the program with two command-line arguments in the following format:
    - `javac PacketSender.java ; java PacketSender <IP_ADDRESS_OF_RECEIVER> "<MESSAGE>"`
    - Example: `javac PacketSender.java ; java PacketSender 192.168.0.1 "Hello! How are you?"`
