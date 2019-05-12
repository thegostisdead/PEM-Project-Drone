# Arduino Components
> **Description:** In this branch you can found all parts about arduino of this project 

All the arduino programs have been created with Arduino IDE.   
We use 2 arduino mini.   
1. The first arduino mini is only for the GPS.    
He is configurated on I2C slave.
2. The second is an I2C master. He collect the value of all the sensors (when we want the GPS values, the second ardunio take the control of I2C line and get the value collected by the first arduino).
We use an NVRAM memory to store some data. This memory use I2C bus.
## Parts
* GPS
* Luminosity   
We get the luminosity with Adafruit GA1A12S202 Log-scale Analog Light Sensor.
* Compass

> **Warning:** This branch is maybe not updated.
