import time
import RPi.GPIO as GPIO

GPIO_BUTTON_PIN = 17

GPIO.setmode(GPIO.BCM)
GPIO.setup(GPIO_BUTTON_PIN, GPIO.OUT)

GPIO.output(GPIO_BUTTON_PIN, GPIO.HIGH)


while True:
    try:
       time.sleep(1000000)
    except:
       break

GPIO.cleanup()
