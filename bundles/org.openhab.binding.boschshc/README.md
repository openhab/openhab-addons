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
 - Bosch Climate Control

## Limitations

 - Discovery of Things
 - Discovery of Bridge

## Discovery

Not yet implemented. Configuration via configuration files or Paper UI (see below).

## Binding Configuration

You need to provide at least the IP address and the system password of your Bosch Smart Home Controller. The IP address of the controller is visible in the Bosch Smart Home Mobile App (More -> System -> Smart Home Controller) or in your network router UI.

A keystore file with a self signed certificate is created automatically. This certificate is used for pairing between the Bridge and the Bosch SHC. 

*Press and hold the Bosch Smart Home Controller Bridge button until the LED starts blinking after you save your settings for pairing*.
        
## Getting the device IDs

Bosch IDs for found devices are displayed in the openHAB log on bootup (`OPENHAB_FOLDER/userdata/logs/openhab.log`)

Example:
```
2020-08-11 12:42:49.490 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=Heizung id=hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX
2020-08-11 12:42:49.495 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=-RoomClimateControl- id=roomClimateControl_hz_1
2020-08-11 12:42:49.497 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=-VentilationService- id=ventilationService
2020-08-11 12:42:49.498 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=Großes Fenster id=hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX
2020-08-11 12:42:49.501 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=-IntrusionDetectionSystem- id=intrusionDetectionSystem
2020-08-11 12:42:49.502 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=Rollladen id=hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX
2020-08-11 12:42:49.502 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=Heizung id=hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX
2020-08-11 12:42:49.503 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=Heizung Haus id=hdm:ICom:819410185:HC1
2020-08-11 12:42:49.503 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=-RoomClimateControl- id=roomClimateControl_hz_6
2020-08-11 12:42:49.504 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=PhilipsHueBridgeManager id=hdm:PhilipsHueBridge:PhilipsHueBridgeManager
2020-08-11 12:42:49.505 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=Rollladen id=hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX
2020-08-11 12:42:49.506 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=Rollladen id=hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX
2020-08-11 12:42:49.507 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=Central Heating id=hdm:ICom:819410185
```

## Thing Configuration

You define your Bosch devices by adding them either to a `.things` file in your `$OPENHAB_CONF/things` folder like this:

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

Or by adding them via Paper UI -> Configuration -> Things -> "+" -> Bosch Smart Home Binding.

For more details see https://www.openhab.org/docs/configuration/things.html.

## Item Configuration

You define the items which should be linked to your Bosch devices via a `.items` file in your `$OPENHAB_CONF/items` folder like this:

```
Switch Bosch_Bathroom    "Bath Room"    { channel="boschshc:in-wall-switch:1:bathroom:power-switch" }
Switch Bosch_Bedroom     "Bed Room"     { channel="boschshc:in-wall-switch:1:bedroom:power-switch" }
Switch Bosch_Kitchen     "Kitchen"      { channel="boschshc:in-wall-switch:1:kitchen:power-switch" }
Switch Bosch_Corridor    "Corridor"     { channel="boschshc:in-wall-switch:1:corridor:power-switch" }
Switch Bosch_Living_Room "Living Room"  { channel="boschshc:in-wall-switch:1:livingroom:power-switch" }

Switch Bosch_Lelit       "Lelit"        { channel="boschshc:in-wall-switch:1:coffeemachine:power-switch" }
```

Or by adding them via Paper UI -> Configuration -> Items -> "+".

For more details see https://www.openhab.org/docs/configuration/items.html.
