# Philips Air Purifier Binding (org.openhab.binding.philipsair)

The OpenHAB binding provides readings and control of Philips Air Purifier devices. 

Heavily based and inspired by [rgerganov](https://github.com/rgerganov/py-air-control/commits?author=rgerganov) for his work on the protocol, communication with Philips Air Purifiers.  
See the project on [py-air-control](https://github.com/rgerganov/py-air-control)
This project mostly adopts the work done by [rgerganov](https://github.com/rgerganov/py-air-control/commits?author=rgerganov) to the needs of OpenHAB

The binding compatible with OpenHAB 2.4 can be found at https://git.dabucomp.com/borowa/org.openhab.binding.philipsair

## Supported Things

Supported Philips Air Purifiers (hardware testes) devices are

+ AC2889/10
+ AC2889/10
+ AC2729
+ AC2729/50
+ AC1214/10
+ AC3829/10

You are welcome to provide any information about compatibility with other versions.

**Features**

* first OpenHAB 2 binding poviding support for Phlips Air Purifiers devices
* auto discovery via UPNP
* power off/on,
* fan speed control,
* purification mode control,
* lights control,
* sensor readings (air quality, temperature, humidity)
* filter status
* child lock
* temperature/humidity offset

## Discovery

This binding can discover Philips Air Purifiers automatically via UPNP protocol.
Currently following devices are recognized precisely, all other Philips Air purifiers family is recognized as 'Universal' thing type

-   AC2889/10
-   AC2729
-   AC2729/50
-   AC1214/10
-   AC3829/10

## Thing Configuration

The binding in order to work does not need to configure anything, as UPNP discovery should configure everything automatically allready. After adding the thing the binding automatically exchange keys with the device used for communication encryption/decryption.
