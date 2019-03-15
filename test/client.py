# coding: utf-8

import socket

hote = "localhost"
port = 8081

socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
socket.connect((hote, port))
print ("Connection on {}".format(port))

socket.send(bytearray("Eclipse best IDE!", 'utf-8'))

print ("Close")
socket.close()