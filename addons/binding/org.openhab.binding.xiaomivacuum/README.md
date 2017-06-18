# Xiaomi Robot Vacuum Binding

This Binding is used to control a Xiaomi Robot Vacuum.

## Supported Things

This Binding supports Xiaomi Robot Vacuum devices.

## Discovery

The binding needs a token from the Xiaomi Robot Vacuum in order to be able to control it.
In order to fetch the token, reset the robot vacuum, connect to its the network its announcing (rockrobo-XXXX) and run the discovery again. After the token is retrieved you can connect the vacuum to your phone again.
Once connected to your phone & the regular wifi network, run discovery once more to retrieve the new ipaddress.

## Binding Configuration


## Thing Configuration


The binding needs ip address and token to be able to communicate. See discovery for details.
Optional configuration is the refresh interval.

## Channels


## Full example

```
Group   gVaccum "Xiaomi Robot Vacuum"   <fan>

```
