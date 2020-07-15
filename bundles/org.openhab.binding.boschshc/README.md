# BoschSHC Binding

Binding for the Bosch Smart Home Controller:

## Supported Things

 - Bosch In-Wall switches
 - Bosch Smart Plugs (use "in-wall-switch" thing too).
 - Bosch TwinGuard smoke detector
 - Bosch Window/Door contacts
 - Bosch Motion Detector
 - Bosch Shutter Control in-wall
 - Bosch Thermostat

## Limitations

 - Discovery of Things
 - Discovery of Bridge

## Discovery

Not yet implemented. Configuration via configuration files or Paper UI.

## Binding Configuration

You need to provide at least the ipAddress and the system password of your Bosch Smart Home Controller
and a path to a JKS keystore file location.
At this location a keystore file with a self signed certificate is created and stored if not present at the first setup.
This certificate is used for pairing between the Bridge and the Bosch SHC. 

*Press the Bosch Smart Home Controller Bridge button until the LED starts blinking after you save your settings for pairing*.
        
## Thing Configuration

Bosch IDs for devices are displayed in the OpenHab log on bootup.

```
Bridge boschshc:shc:1 [ ipAddress="192.168.x.y" ] {
  Thing in-wall-switch bathroom "Bathroom" [ id="hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX" ]
  Thing in-wall-switch bedroom "Bedroom" [ id="hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX" ]
  Thing in-wall-switch kitchen "Kitchen" [ id="hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX" ]
  Thing in-wall-switch corridor "Corridor" [ id="hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX" ]
  Thing in-wall-switch livingroom "Living Room" [ id="hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX" ]

  Thing in-wall-switch coffeemachine "Coffee Machine" [ id="hdm:HomeMaticIP:3014F711A0000XXXXXXXXXXXX" ]

  Thing twinguard      tg-corridor    "Twinguard Smoke Detector" [ id="hdm:ZigBee:000d6f000XXXXXXX" ]
  Thing window-contact window-kitchen "Window Kitchen"           [ id="hdm:HomeMaticIP:3014F711A00000XXXXXXXXXX" ]
  Thing window-contact entrance       "Entrance door"            [ id="hdm:HomeMaticIP:3014F711A00000XXXXXXXXXX" ]

  Thing motion-detector  motion-corridor "Bewegungsmelder"      [ id="hdm:ZigBee:000d6f000XXXXXXX" ]
}
```

## Item Configuration

```
Switch Bosch_Bathroom    "Bath Room"    { channel="boschshc:in-wall-switch:1:bathroom:power-switch" }
Switch Bosch_Bedroom     "Bed Room"     { channel="boschshc:in-wall-switch:1:bedroom:power-switch" }
Switch Bosch_Kitchen     "Kitchen"      { channel="boschshc:in-wall-switch:1:kitchen:power-switch" }
Switch Bosch_Corridor    "Corridor"     { channel="boschshc:in-wall-switch:1:corridor:power-switch" }
Switch Bosch_Living_Room "Living Room"  { channel="boschshc:in-wall-switch:1:livingroom:power-switch" }

Switch Bosch_Lelit       "Lelit"        { channel="boschshc:in-wall-switch:1:coffeemachine:power-switch" }
```

## Channels

| channel      | type   | description                                   |
|--------------|--------|-----------------------------------------------|
| power-switch | Switch | This is the control channel for ON/OFF events |
| power-meter  | Switch | This is the control channel for ON/OFF events |

Surely this can be auto-generated from thing-types.xml

## Full Example

TODO

## Any custom content here!

TODO
