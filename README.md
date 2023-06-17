# Network Socket Programming

This program simulates two machines communcating with each other (client-server communication) by sending IPv4 packets via a socket. On the client, the user starts by entering the IP address of the receiver and the message they wish to send to the receiver (the payload). 

The sender (client) then takes that information and performs the following operations:
- Encodes the payload from plain-text into hexadecimal
- Creates an IPv4 packet containing all of the necessary headers (version, header length, service type, total length, identification, flags, fragment offset, TTL, protocol, header checksum, source IP address, destination IP address, data, padding)
- Establishes a TCP/IP socket connection with the receiver
- Sends the packet to the receiver

The receiver (server), while waiting for a packet from the sender:
- Receives the packet
- Verifies that the packet has not been corrupted by calculating the header checksum
- Decodes the payload and;
    - Prints the message to the terminal and signals to the sender that the message has been received successfully
    - Prints an error message to the terminal and signals to the sender that the message received has been corrupted

### Demo

![image](https://github.com/kienmarkdo/Socket_Programming_Lab/assets/67518620/d7777ad5-f9f3-4eb8-ae2d-e354638b9a9f)


## Instructions
- Open two terminals in root of project (VSCode is preferred)
- In terminal 1, compile and start the server
    - `javac PacketReceiver.java ; java PacketReceiver`
- In terminal 2, compile and run the program with two command-line arguments in the following format:
    - `javac PacketSender.java ; java PacketSender <IP_ADDRESS_OF_RECEIVER> "<MESSAGE>"`
    - Example: `javac PacketSender.java ; java PacketSender 192.168.0.1 "Hello! How are you?"`
