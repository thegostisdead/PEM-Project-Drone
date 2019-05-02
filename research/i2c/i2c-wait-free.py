import smbus
import time
import os

# Constants
I2C_CHANNEL = 1
ADDRESS = 0x08
GPIO_TRANSACTION_LOCK_PIN = 27

# Showing I2C slave available on the channel
os.system("i2cdetect -y " + str(I2C_CHANNEL))

# Connecting to the channel
bus = smbus.SMBus(I2C_CHANNEL)

# Setup
GPIO.setmode(GPIO.BCM)


def int32(x):
    if x > 0xFFFFFFFF:
        raise OverflowError
    if x > 0x7FFFFFFF:
        x = int(0x100000000 - x)
    if x < 2147483648:
        return -x
    else:
        return -2147483648
    return x


while True:
    data = None

    try:
        GPIO.setup(GPIO_TRANSACTION_LOCK_PIN, GPIO.IN)
        while GPIO.input(GPIO_TRANSACTION_LOCK_PIN) == 1:
            pass
        
        GPIO.setup(GPIO_TRANSACTION_LOCK_PIN, GPIO.OUT)
        GPIO.output(GPIO_TRANSACTION_LOCK_PIN, GPIO.HIGH)
    except:
        pass

    try:
        data = bus.read_i2c_block_data(ADDRESS, 0, 8)
    except Exception as e:
        print(e)

    try:
        GPIO.output(GPIO_TRANSACTION_LOCK_PIN, GPIO.LOW)
    except:
        pass

    print(data)

    if data != None:
        latitude = (data[0] | (data[1] << 8) | (data[2] << 16) | int32(data[3] << 24))
        longitude = (data[4] + (data[5] << 8) + (data[6] << 16) + int32(data[7] << 24))

        print("%s %s" % (type(latitude), type(longitude)))
        print("latitude %s // longitude %s" % (latitude, longitude))

    time.sleep(0.5)

