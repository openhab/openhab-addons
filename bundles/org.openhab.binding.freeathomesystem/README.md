# ABB/Busch-free@home Smart Home binding

 openHAB ABB/Busch-free@home binding based on the offical free@home local api

![alt text](https://github.com/jannodeluxe/jannnnoooo/blob/main/free_at_home_logo_1.jpg)
![alt text](https://github.com/jannodeluxe/jannnnoooo/blob/main/abb_freeathome_2_0.png)
# Description

This openHAB binding allows you to connect your free@home Smart Home system from ABB / Busch-Jaeger to openHAB and to control and observe most of the components.
It requires a System Access Point with version 2.6.1 or higher.

# Supported Devices

- ABB / Busch-Jaeger System Access Point 2.0
- free@home Switch Actuator Sensor 1/1, 2/1, 2/2 (wired and wireless)
- free@home Dimming Actuator Sensor 1/1, 2/1 (wired and wireless)
- free@home Blind Actuator Sensor 1/1, 2/1 (wired and wireless)
- free@home Movement Detector Actuator
- free@home Radiator Thermostat
- free@home Switch Actuator 4-channel
- free@home Switch Actuator 4-channel
- free@home Dimming Actuator 4- and 6-channel
- IP-touch panel (function: door opener, door ring sensor)
- Hue devices (untested)


# Tested SysAP Versions

|Version|Supported|
|---|---|
|2.6.1+|yes|


# Setup / Installation

## Prerequisites

To make use of this Binding first the local free@home API has to be activated. The API is disabled by default!

1. Open the free@home next app
2. Browse to "Settings -> free@home settings -> local API and activate the checkbox

## Discovery

The free@home bridge shall be added manually. Once it is added as a Thing with correct credentials, the scan of free@home devices will be possible.
The devices with multiple channels are devided into multiple devices with a single channel. The label of these devices is built-up with the same deivce name and free@home device ID but the channel number is mentioned in the Thing label as well for better identification.

## Setup

1. Enter your openHAB webfrontend with `<device IP>:8080`
2. Log into openHAB with your crendetials at the lower left side
3. Browse to "Settings -> Things" and press the "+" symbol
4. Choose "FreeAtHome System Binding" and click "Free@home Bridge"
5. Add the required data: SysAP IP address, username and password
**ATTENTION:** The username here has to be from "Settings -> free@home settings -> local API, NOT the username from webfrontend or used in the app for login)

6. Press save in the righter upper corner
7. If everthing is right the Bridge should went "Online"
8. "Scan" for the free@home devices and set up the your free@home components as Thing

## free@home components as openHAB Things

The free@home system is calling its components as free@home devices. The devices in the free@home system can have one or multiple channels depending the device's setup. During the scanning process the openHAB binding for free@home will split-up devices with multiple channels into multiple openHAB Things with single channel. However the name of the openHAB Thing will reflect the channel of the free@home device for simplier identification

Example for a free@home Switch Actuator Sensor 2/2, which is having two actuator channles.

|Device in free@home| |Things in openHAB|
|---|---|---|
|Device Actuator Sensor 2/2 - Name `Actuator_Livingroom`  | ⇨ |1st Thing - Name `Actuator_Livingroom_1`<br />2nd Thing - Name `Actuator_Livingroom_2`|

## Sensors and Actuators of free@home devices as Things in openHAB

The free@home system is supporting the sensors and actuators. the connenction of sensors and actuatators are done on the free@home system dashboard. The sensors in the free@home system are not accessible via free@home API (at the time of the development of this binding). Therefore the sensor only devices or the sensor channel(s) of a combined sensor/actuator devices are not represented in the openHAB binding.

# Communities

[Busch-Jaeger Community](https://community.busch-jaeger.de/)

[free@home user group Facebook DE](https://www.facebook.com/groups/738242583015188)

[free@home user group Facebook EN](https://www.facebook.com/groups/452502972031360)
