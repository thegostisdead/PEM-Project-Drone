# coding: utf-8

import socket
import random
import os
import time

hote = "localhost"
port = 8081

picture_id = random.randint(1, 100);
picture_path = "./pictures/rem.jpg"
picture_size = os.path.getsize(picture_path)

gps_latitude = 1.23456
gps_longitude = 6.54321

header = ""
#header += "id " + str(picture_id) + "," # disabled to use an random name
header += "length " + str(picture_size) + ","
header += "gps " + str(gps_latitude) + " " + str(gps_longitude)
header += "\n"
 

socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
socket.connect((hote, port))
print ("Connection on {}".format(port))

socket.send(bytearray(header, 'utf-8'))
size = 0
#writter = open("hello.jpg", "wb");
with open(picture_path, 'rb') as f:
   for line in f:
      size += len(bytearray(line))
      print(line)
      #writter.write(bytearray(line))
      #time.sleep(0.02)
      socket.send(bytearray(line))
print(size)

print ("Close")
socket.close()