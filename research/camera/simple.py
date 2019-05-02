# coding: utf-8

import socket

HOTE = "test.boxplay.io"
PORT = 8081

content = input("send ?") + "\x00"
size = len(content)

header = ""
header += "id 4g,"
header += "length " + str(size)
header += ","
header += "\n"

print ("Connecting...")
socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
socket.connect((HOTE, PORT))
print ("Connected")

socket.send(bytearray(header, 'utf-8'))

part = bytearray(content, "utf-8")
socket.send(part)
size += len(part)
print("Sended {} bytes".format(size))

print ("Close")
socket.close()
