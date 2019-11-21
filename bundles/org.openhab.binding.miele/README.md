# Miele@home Binding

This binding integrates Miele@home appliances.
Miele@home allows controlling Miele appliances that are equipped with special communication modules. 
There are devices that communicate through ZigBee and others that use WiFi.

See [www.miele.de](https://www.miele.de) for the list of available appliances.

## Supported Things

This binding requires the XGW3000 gateway from Miele as all integration with openHAB is done through this gateway.
While users with ZigBee-enabled Miele appliances usually own such a gateway, this is often not the case for people that have only WiFi-enabled appliances.

The types of appliances that are supported by this binding are: 

- Coffeemachine
- Dishwasher
- Fridge
- Fridge/Freezer combination
- Hob
- Hood
- Oven
- Microwave/Oven combination
- Tumbledryer
- Washingmachine

## Discovery

The binding is able to auto-discover the Miele XGW3000 gateway.
When an XGW3000 gateway is discovered, all appliances can be subsequently discovered.


## Thing Configuration

Each appliances needs the device UID as a configuration parameter.
The UID is nowhere to be found on the appliances, but since the discovery works quite reliably, a manual configuration is not needed.

Once you got hold of the IDs, a manual configuration looks like this:

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
Thing coffeemachine coffeemachine [uid="001d63fffe020505#190"]
}
```

## Channels

The definition of the channels in use can best be checked in the [source repository](https://github.com/openhab/openhab2-addons/tree/master/bundles/org.openhab.binding.miele/src/main/resources/ESH-INF/thing).

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
