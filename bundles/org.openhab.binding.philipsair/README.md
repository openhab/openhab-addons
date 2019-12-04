# Philips Air Purifier Binding (org.openhab.binding.philipsair)

The OpenHAB binding provides readings and control of Philips Air Purifier devices. 

Heavily based and inspired by [rgerganov](https://github.com/rgerganov/py-air-control/commits?author=rgerganov) for his work on the protocol, communication with Philips Air Purifiers.  
See the project on [py-air-control](https://github.com/rgerganov/py-air-control)
This project mostly adopts the work done by [rgerganov](https://github.com/rgerganov/py-air-control/commits?author=rgerganov) to the needs of OpenHAB

The binding compatible with OpenHAB 2.4 can be found at https://git.dabucomp.com/borowa/org.openhab.binding.philipsair

## Supported Things

Supported Philips Air Purifiers (hardware testes) devices are

+ AC2889/10
+ AC2729/50

You are welcome to provide any information about compatibility with other versions.

**Features**
* first OpenHAB 2 binding poviding support for Phlips Air Purifiers devices
* auto discovery via UPNP
* power off/on,
* fan speed control,
* purification mode control,
* lights control,
* sensor readings (air quality, temperature, humidity)

## Discovery

Via UPNP protocol

## Thing Configuration

No need to configure as UPNP discovery should configure everything automatically, including keys exchange with the device

## Installation

### Before installing a new build

- stop OH
- run "openhab-cli clean-cache"

### General installation

- copy the jar into you OH's addons folder.
- start OH, wait until initialized
