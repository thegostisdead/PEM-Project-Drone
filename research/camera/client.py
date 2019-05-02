# coding: utf-8

import socket as sockets
import random
import os
import time
from picamera import PiCamera
import RPi.GPIO as GPIO

HOTE = "test.boxplay.io"
PORT = 8081
GPIO_BUTTON_PIN = 17

# Setup
GPIO.setmode(GPIO.BCM)
GPIO.setup(GPIO_BUTTON_PIN, GPIO.IN)

camera = PiCamera()

# Run
def take_picture(name = None):
    if name == None:
        name = str(time.time())
    path = "storage/" + name + ".jpg"
    
    camera.capture(path)
    
    return path

def send_picture(file, name = None, gps_position = None):
    if not os.path.isfile(file):
        return

    size = os.path.getsize(file)
    
    header = ""
    
    header += "length " + str(size)
    header += ","
    
    if name != None:
        header += "id " + str(name)
        header += ","
    
    if gps_position != None:
        header += "gps " + str(gps_position[0]) + " " + str(gps_position)
    
    header += "\n"
    
    print ("Connecting...")
    try:
        socket = sockets.socket(sockets.AF_INET, sockets.SOCK_STREAM)
        socket.connect((HOTE, PORT))
        print ("Connected")

        socket.send(bytearray(header, 'utf-8'))
        
        send = 0
        with open(file, 'rb') as f:
            for line in f:
                part = bytearray(line)
                socket.send(bytearray(line))
                size += len(part)
        print("Sended {} bytes".format(size))

        print ("Close")
        socket.close()
    except Exception as exception:
        print ("Failed")
        print (exception)

while True:
    try:
        #state = GPIO.input(GPIO_BUTTON_PIN)
        
        #if state == 1:
        if input() != "        ":
            send_picture(take_picture(), gps_position = (1.23456, 6.54321))
    except KeyboardInterrupt:
        print("KeyboardInterrupt raised; exiting...")
        GPIO.cleanup() 
        exit()
    except Exception as exception:
        print("An (not handled) exception occured when running.")
        print(exception)
