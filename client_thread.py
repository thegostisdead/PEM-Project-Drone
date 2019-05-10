# coding: utf-8

import threading
import sys
import traceback
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
API_SERVER_PORT = 8080
PICTURE_SERVER_PORT = 8081

GPIO_BUTTON_PIN = 17
GPIO_TRANSACTION_LOCK_PIN = 26

ACQUIRE_DELAY = 0.2
LOCK_DELAY = 0.1

MAX_TRY = 5
TRY_DELAY = 0.1

# Showing I2C slave available on the channel
os.system("i2cdetect -y " + str(I2C_CHANNEL))


# Utils
def int32(x):
    if x > 0xFFFFFFFF:
        raise OverflowError
    if x > 0x7FFFFFFF:
        x = int(0x100000000 - x)
    return x


class Transactor(threading.Thread):

    def __init__(self):
        threading.Thread.__init__(self)

        self.bus = smbus.SMBus(I2C_CHANNEL)

        GPIO.setmode(GPIO.BCM)
        GPIO.setup(GPIO_BUTTON_PIN, GPIO.IN)
        GPIO.setup(GPIO_TRANSACTION_LOCK_PIN, GPIO.OUT)

        self.valid_data = None
        self.last_datasender_instance = None

    def aquire_data_from_i2c(self):
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

                self.bus.write_i2c_block_data(RAM_ADDRESS, 0x00, [0x00])

                for i in range(0, 19):
                    data.append(self.bus.read_byte(RAM_ADDRESS))

                if data[3] != 255:  # Catch potential errors
                    success = True
                    break
            except Exception as e:
                print(e)
            print("Transactor: Failed, try #%s/%s" % (i + 1, MAX_TRY))
            time.sleep(TRY_DELAY)

        try:
            GPIO.output(GPIO_TRANSACTION_LOCK_PIN, GPIO.LOW)
        except Exception as e:
            print(e)

        print("Transactor: Received data -> " + str(data))

        if success:
            return data

        return None

    def compute_gps_position(self):
        data = self.valid_data

        if data == None:
            return None

        latitude = int32(data[0] + (data[1] << 8) + (data[2] << 16) + (data[3] << 24)) * 1.0 / 1000000
        longitude = int32(data[4] + (data[5] << 8) + (data[6] << 16) + (data[7] << 24)) * 1.0 / 1000000
        altitude = int32(data[8] + (data[9] << 8) + (data[10] << 16) + (data[11] << 24)) * 1.0 / 1000000

        gps_position = (latitude, longitude, altitude)

        return gps_position

    def compute_luminosity_value(self):
        data = self.valid_data

        if data == None:
            return None

        luminosity = int32(data[12] + (data[13] << 8))

        return luminosity

    def compute_orientation_axis(self):
        data = self.valid_data

        if data == None:
            return None

        yaw = int32(data[14] + (data[15] << 8))
        pitch = int32(data[16])
        roll = int32(data[17])

        orientation_axis = (yaw, pitch, roll)

        return orientation_axis

    def run(self):
        while True:
            data = self.aquire_data_from_i2c()

            if data != None:
                self.valid_data = data

                can_create = True
                if self.last_datasender_instance != None:
                    if self.last_datasender_instance.running:
                        print("Transactor: WARNING -> DataSender seems slow...")
                        can_create = False

                if can_create:
                    self.last_datasender_instance = sender = DataSender(self)
                    sender.start()

            time.sleep(ACQUIRE_DELAY)


class DataSender(threading.Thread):

    def __init__(self, sourceTransactor):
        threading.Thread.__init__(self)

        self.transactor = sourceTransactor
        self.running = False

    def send(self):
        gps_position = self.transactor.compute_gps_position()
        orientation_axis = self.transactor.compute_orientation_axis()
        luminosity = self.transactor.compute_luminosity_value()

        socket = sockets.socket(sockets.AF_INET, sockets.SOCK_STREAM)
        socket.connect((HOTE, API_SERVER_PORT))
        socket.settimeout(0.5)

        payload = ""
        payload += "{"

        if gps_position != None:
            payload += "\"gps_position\":[{\"content\":" + str(':'.join(str(x) for x in gps_position)) + "}],"

        if orientation_axis != None:
            payload += "\"orientation_axis\":[{\"content\":" + str(':'.join(str(x) for x in orientation_axis)) + "}],"

        payload += "\"luminosity\":[{\"content\":" + str(luminosity) + "}]"
        payload += "}"

        header = ""
        header += "POST /qualities/push HTTP/1.1\n"
        header += "Host: " + str(HOTE) + ":" + str(API_SERVER_PORT) + "\n"
        header += "content-type: application/json;charset=UTF-8\n"
        header += "Content-Length: " + str(len(payload)) + "\n"
        header += "\n"

        request = header + payload

        for line in request.split("\n"):
            socket.send(bytearray(line + "\n", "utf-8"))

        print ("DataSender: Received from API -> " + str(socket.recv(512)))
        socket.close()

    def run(self):
        self.running = True
        self.send()
        self.running = False


class Camera(threading.Thread):

    def __init__(self):
        threading.Thread.__init__(self)

        self.transactor = None

        self.picamera = PiCamera()

    def attachTransactor(self, targetTransactor):
        self.transactor = targetTransactor

    def take_picture(self, name = None):
        if name == None:
            name = str(time.time())

        path = "storage/" + name + ".jpg"

        self.picamera.capture(path)

        return path

    def send_picture(self, file, gps_position, name = None):
        if not os.path.isfile(file):
            return

        size = os.path.getsize(file)

        header = "drone\n"

        header += "length " + str(size)
        header += ","

        if name != None:
            header += "id " + str(name)
            header += ","
        print(gps_position)
        if gps_position != None:
            header += "gps " + str(gps_position[0]) + " " + str(gps_position[1])

        header += "\n"

        print ("Camera: Connecting...")
        try:
            socket = sockets.socket(sockets.AF_INET, sockets.SOCK_STREAM)
            socket.connect((HOTE, PICTURE_SERVER_PORT))
            print ("Camera: Connected")

            socket.send(bytearray(header, 'utf-8'))

            send = 0
            with open(file, 'rb') as f:
                for line in f:
                    part = bytearray(line)
                    socket.send(bytearray(line))
                    size += len(part)
            print("Camera: Sended {} bytes".format(size))

            print ("Camera: Close")
            socket.close()
        except Exception as exception:
            print ("Camera: Failed")
            print (exception)

    def take_and_send_picture(self):
        self.send_picture(self.take_picture(), self.transactor.compute_gps_position())

    def run(self):
        pass  # TODO Listen Interrupt


def main():
    transactor = Transactor()
    camera = Camera()

    camera.attachTransactor(transactor)

    transactor.start()
    camera.start()

    while True:
        try:
            # state = GPIO.input(GPIO_BUTTON_PIN)

            # if state == 1:
            input()
            camera.take_and_send_picture()
        except KeyboardInterrupt:
            print("KeyboardInterrupt raised; exiting...")
            GPIO.cleanup()
            sys.exit(0)
        except Exception as exception:
            print("An (not handled) exception occured when running.")
            print(exception)


if __name__ == '__main__': main()
