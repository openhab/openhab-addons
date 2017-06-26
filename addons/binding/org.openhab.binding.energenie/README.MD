---
layout: documentation
---

{% include base.html %}

# Energenie Mi|Home Binding

Mi|Home is the house home automation solution of Energenie. The system consists of Internet connected gateway and devices (called subdevices) that are communicating with the gateway using the Openthings Protocol. The subdevices are paired to the gateway.

In order to use the Mi|Home solution and the binding, you should first create an account in the [Mi|Home portal](https://mihome4u.co.uk/). The password and the username will be used to register your gateway in the portal.

## Supported Things

Currently the binding supports the following thing types:

- *gateway* - MIHO001 Home Gateway, central part of any Mi|Home system. It can control lighting, heating and electrics;
- *energyMonitor* - MIHO006 House Monitor, monitor the total energy usage of your home;
- *motionSensor* - MIHO032 Motion Sensor;
- *openSensor* - MIHO033 Open Sensor;

## Discovery

The binding implements discovery for all devices, except the Home Gateway.

Discovery results will be created for each device that is already paired to a gateway (using the Mi|Home portal or other apps) and doesn't have a Thing representation.

## Binding Configuration

Required configuration parameters:

- *userName* - User's email address in the Mi|Home portal;
- *password* - User's password in the Mi|Home portal;

## Thing Configuration

### Gateway Configuration

Required configuration parameters:

- *gatewayCode* - 10-letter gateway code that can be found underneath the device;

Optional configuration parameters:

- *updateInterval* - Update interval to poll data from a device in seconds. If not set, the default value is 30 seconds;

### Subdevices configuration

All subdevices should be paired before they can be used. When you create the thing using the PaperUI or the textual configuration the gateway will initialize the pairing process. The binding will notify you through a status change when you should press the pairing button. After three unsuccessful attempts the thing will be set to OFLINE status.

Optional configuration parameters:

- *updateInterval* - Update interval to poll data from a device in seconds. If not set, the default value is 10 seconds;

## Channels

The channels supported from the different devices are listed below:

| Channel Type ID   | Item Type | Description                                                | Thing type ID |
|-------------------|-----------|------------------------------------------------------------|---------------|
| lastSeen          | DateTime  | Time when the device was last seen from the Mi|Home server | gateway       |
| voltage           | Number    | Voltage                                                    | energyMonitor |
| real_power        | Number    | Real power                                                 | energyMonitor |
| today_consumption | Number    | Today consumption in Wh                                    | energyMonitor |
| state             | Contact   | Set to Open or Closed                                      | openSensor    |
| state             | Switch    | Set to ON when movement starts and OFF when movement stops | motionSensor  |

## Full Example

We recommend using the PaperUI instead of textual configuration, as the setup includes additional steps as pairing.
