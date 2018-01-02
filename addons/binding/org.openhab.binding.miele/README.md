# Miele@home Binding

This binding integrates Miele@home appliances.
Miele@home is a Zigbee based network to interconnect and control Miele appliances that are equipped with special modules.
See [www.miele.de](http://www.miele.de) for the list of available appliances.


## Supported Things

Dishwasher
Fridge
Fridge/Freezer combination
Hob
Hood
Oven
Microwave/Oven combination
Tumbledryer
Washingmachine

## Discovery

The binding is able to auto-discover the Miele XGW3000 gateway.
When an XGW3000 gateway is discovered, all appliances can be subsequently discovered.


## Thing Configuration

Each appliances needs the device Zigbee UID as a configuration parameter.
The Zigbee UID is nowhere to be found on the appliances, but since the discovery works quite reilably, a manual configuration is not needed.

However, in the thing file, a manual configuration looks like this:

```
Bridge miele:xgw3000:dilbeek [ipAddress="192.168.0.18", interface="192.168.0.5"] {
Things:
Thing fridgefreezer freezer [uid="00124b000424be44#2"]
Thing hood hood [uid="001d63fffe020685#210"]
Thing fridge fridge [uid="00124b000424bdc0#2"]
Thing oven oven [uid="001d63fffe020390#210"]
Thing oven microwave [uid="001d63fffe0206eb#210"]
Thing hob hob [uid="00124b000424bed7#2"]
Thing dishwasher dishwasher [uid="001d63fffe020683#210"]
Thing tumbledryer dryer [uid="001d63fffe0200ba#210"]
Thing washingmachine washingmachine [uid="001d63fffe020505#210"]
}
```

## Channels

The definition of the channels in use can best be checked in the [source repository](https://github.com/openhab/openhab2-addons/tree/master/addons/binding/org.openhab.binding.miele/ESH-INF/thing).

## Example

demo.items:

```
String MieleFridgeState  (gMiele,gMieleFridge) {channel="miele:fridge:dilbeek:fridge:state"}
Switch MieleFridgeSuperCool (gMiele,gMieleFridge) {channel="miele:fridge:dilbeek:fridge:supercool"}
Number MieleFridgeCurrent (gMiele,gMieleFridge) {channel="miele:fridge:dilbeek:fridge:current"}
Number MieleFridgeTarget (gMiele,gMieleFridge) {channel="miele:fridge:dilbeek:fridge:target"}
Contact MieleFridgeDoor (gMiele,gMieleFridge) {channel="miele:fridge:dilbeek:fridge:door"}
Switch MieleFridgeStart (gMiele,gMieleFridge) {channel="miele:fridge:dilbeek:fridge:start"}
```
