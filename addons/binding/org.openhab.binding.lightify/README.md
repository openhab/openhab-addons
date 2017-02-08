# OSRAM Lightify Binding

This binding integrates the OSRAM Lightify lightning system. This integration uses the standard OSRAM Lightify gateway to integrate Zigbee devices (for now only light bulbs). 

## Introduction

Binding works with the standard OSRAM Lightify gateway and uses the OSRAM internal binary protocol for communication between the gateway and OpenHAB. This plugin does not work using the cloud hosted REST API.

## Supported Things

This binding supports the OSRAM Lightify light bulbs. Further Lightify gateway / Zigbee compatible devices would work too but are neither supported as Things nor Items due to a lack of test hardware.


## Discovery

This binding can discover OSRAM Lightify gateways automatically, as well as paired light bulbs and created groups of lights (as far as this is useful).

At the moment the binding always expects a RGBW (that is color and white temperature) bulb item and does not yet recognize the actual capabilities of the paired devices. This is mainly to the fact of missing non-RGBW hardware devices again.

## Binding Configuration

Discovery of OSRAM Lightify gateways and paired lights is an automatic process. Install the binding and the gateway will show up in the Inbox. After adding the found gateways, the paired devices will be discovered and will end up in the Inbox as well. 

Currently there is no further configuration necessary.

At the moment it is also not possible to kick off a pairing session from the binding due to a lack of understanding the protocol.

## Channels

The OSRAM Lightify supports the following channels:

| Channel Type ID         | Item Type    | Description  |
|-------------------------|--------------|--------------|
| rgbw#power              | Switch       | Switch the bulb / group on/off |
| rgbw#dimmer             | Dimmer       | Control the luminance |
| rgbw#temperature        | Number       | Set the white temperature between 2,000 and 6,500 Kelvin |
| rgbw#color              | Color        | Changes the color of the RGB LEDs |
