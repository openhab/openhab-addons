# Mystrom Binding

[Mystrom](https://www.mystrom.ch/) developed Smart Home devices. The only actually supported device by this binding is the Wifi Switch, which communicates with the [Mystrom API](https://www.mystrom.ch/mobile/) over a secure, RESTful API to Mystrom's servers.

The openHAB Mystrom binding allows you to send switch command to Mystrom Wifi Switch, receive consumption number and state of device.

## Supported Things

Account (Bridge)
Wifi Switch

## Discovery

The binding is able to auto-discover Mystrom devices, when the Mystrom Account thing is configured.

## Thing Configuration

First you will have to configure the Mystrom Account thing in PaperUI oder thing config file, adding the mystrom api connection bridge thing. This thing will have mandatory configuraton (username, pasword) and refreshInterval.

Each other Mystrom devices will be automatically discovered. Device needs the device Mystrom UID as configuration parameter. The Mystrom UID is nowhere to be found on the mystrom website, but since  the discovery works quite reliable, a manual configuration is not needed.

## Channels

Wifi Switch:
|Channel|Type|Description|
| State| Switch | used to get state on/off of the device, accepted commands are `ON` or `OFF`. |
| Consumption| Number | used to receive consumption in Watts of the device. |

## Full Example

Things:
Account Bridge: mystrom:account:290792dc
Wifi Switch: mystrom:wifiswitch:290792dc:64002D04767A

Wifi Switch Channels:
mystrom:wifiswitch:290792dc:64002D04767A:consumption
mystrom:wifiswitch:290792dc:64002D04767A:state


