# Homie Binding

This is the binding for devices that complie with the [Homie MQTT Convention]( https://github.com/marvinroger/homie).
This binding allows you to integrate all devices, as long as they complie with the specification.

## Naming definition

| Homie term | Eclipse Smart Home term |
| ---------- | ----------------------- |
| Device | Bridge |
| Node | Thing |
| Property | Channel |

## Discovery
Discovery is done by browsing the MQTT topics located below the basetopic defined in the binding configuration. So you have to make sure all your Homie devices communicate using the same basetopic.

## Addition to the Homie specification
Basically you are free to use whatever node ID you like to. If you want openHAB to render your nodes automatically, you have to use one of the [Eclipse Smart Home channel categories](http://www.eclipse.org/smarthome/documentation/development/bindings/thing-definition.html#channel-categories) as node type.
