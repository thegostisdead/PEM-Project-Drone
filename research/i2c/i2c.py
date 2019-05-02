import smbus
import time
import os

os.system("i2cdetect -y 1")

address = 0x08
bus = smbus.SMBus(1)


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
        data = bus.read_i2c_block_data(address, 0, 8)
    except Exception as e:
        print(e)

    print(data)

    if data != None:
        latitude = (data[0] | (data[1] << 8) | (data[2] << 16) | int32(data[3] << 24))
        longitude = (data[4] + (data[5] << 8) + (data[6] << 16) + int32(data[7] << 24))

        print("%s %s" % (type(latitude), type(longitude)))
        print("latitude %s // longitude %s" % (latitude, longitude))

    time.sleep(0.5)

