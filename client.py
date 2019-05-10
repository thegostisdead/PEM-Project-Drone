# coding: utf-8

import socket as sockets
import random
import os
import time
from picamera import PiCamera
import RPi.GPIO as GPIO
import smbus

# Constants
I2C_CHANNEL = 1
RAM_ADDRESS = 0x50

HOTE = "test.boxplay.io"
PORT = 8081

GPIO_BUTTON_PIN = 17
GPIO_TRANSACTION_LOCK_PIN = 26

LOCK_DELAY = 0.1

MAX_TRY = 5
TRY_DELAY = 0.1

# Showing I2C slave available on the channel
os.system("i2cdetect -y " + str(I2C_CHANNEL))

# Setup
bus = smbus.SMBus(I2C_CHANNEL)

GPIO.setmode(GPIO.BCM)
GPIO.setup(GPIO_BUTTON_PIN, GPIO.IN)

camera = PiCamera()


# Utils
def int32(x):
    if x > 0xFFFFFFFF:
        raise OverflowError
    if x > 0x7FFFFFFF:
        x = int(0x100000000 - x)
    return x


# Run
def take_picture(name = None):
    if name == None:
        name = str(time.time())

    path = "storage/" + name + ".jpg"

    camera.capture(path)

    return path


def acquire_gps_position():
    data = None

    try:
        GPIO.output(GPIO_TRANSACTION_LOCK_PIN, GPIO.HIGH)
        time.sleep(LOCK_DELAY)
    except Exception as e:
        print(e)

    success = False
    for i in range(0, MAX_TRY):
        try:
            data = []

            bus.write_i2c_block_data(RAM_ADDRESS, 0x00, [0x00])

            for i in range(0, 12):
                data.append(bus.read_byte(RAM_ADDRESS))

            if data[3] != 255:  # Catch potential errors
                success = True
                break
        except Exception as e:
            print(e)
        print("Failed, try #%s/%s" % (i + 1, MAX_TRY))
        time.sleep(TRY_DELAY)

    try:
        GPIO.output(GPIO_TRANSACTION_LOCK_PIN, GPIO.LOW)
    except Exception as e:
        print(e)

    print(data)

    if success:
        latitude = int32(data[0] + (data[1] << 8) + (data[2] << 16) + (data[3] << 24)) * 1.0 / 1000000
        longitude = int32(data[4] + (data[5] << 8) + (data[6] << 16) + (data[7] << 24)) * 1.0 / 1000000
        altitude = int32(data[8] + (data[9] << 8) + (data[10] << 16) + (data[11] << 24)) * 1.0 / 1000000

        gps_position = (latitude, longitude, altitude)
        print(gps_position)
        return gps_position
    return None


def send_picture(file, gps_position, name = None):
    if not os.path.isfile(file):
        return

    size = os.path.getsize(file)

    header = ""

    header += "length " + str(size)
    header += ","

    if name != None:
        header += "id " + str(name)
        header += ","
    print(gps_position)
    if gps_position != None:
        header += "gps " + str(gps_position[0]) + " " + str(gps_position[1])

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
        # state = GPIO.input(GPIO_BUTTON_PIN)

        # if state == 1:
        input()
        send_picture(take_picture(), acquire_gps_position())
    except KeyboardInterrupt:
        print("KeyboardInterrupt raised; exiting...")
        GPIO.cleanup()
        exit()
    except Exception as exception:
        print("An (not handled) exception occured when running.")
        print(exception)
