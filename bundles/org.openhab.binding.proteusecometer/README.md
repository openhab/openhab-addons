# ProteusEcoMeter Binding

This is the binding for the Proteus EcoMeter S, which is able to report the level of a cistern or tank.

Note that this binding currently supports no write channels.
This means you have to configure your sensor by considering the manual of the product (using wireless display).
After doing that the binding comes into play and helps you to get your measured values into openHAB.
Please be patient while waiting for the first received data.
The sensor reports at an interval of approx. 1h, except when the water level changes relatively fast.

## Supported Things

Proteus EcoMeter S.
The binding has been tested with this EcoMeter sensor only.

## Discovery

No auto discovery implemented yet.

## Thing Configuration

Plug the wireless display into an USB port.
Note [openHAB Serial Port documentation](https://www.openhab.org/docs/administration/serial.html) for general serial port configuration.
After that you can add the device as thing and configure the usbPort your OS generated for the display.

```
UID: proteusecometer:EcoMeterS:e90705eaa4
label: Proteus EcoMeter S
thingTypeUID: proteusecometer:EcoMeterS
configuration:
  usbPort: /dev/ttyUSB0
```

## Channels

| channel               | type                 | description                                          |
|-----------------------|----------------------|------------------------------------------------------|
| temperature           | Number:Temperature   | Temperature measured by the sensor                   |
| sensorLevel           | Number:Length        | Distance between sensor and water surface            |
| usableLevelInLitre    | Number:Volume        | How much liquid is usable                            |
| usableLevelInPercent  | Number:Dimensionless | How much liquid is usable relative to total capacity |
| totalCapacity         | Number:Volume        | Total capacity of measured cistern/tank              |

## Full Example

```
{
  "statusInfo": {
    "status": "ONLINE",
    "statusDetail": "NONE"
  },
  "editable": true,
  "label": "Proteus EcoMeter S",
  "configuration": {
    "usbPort": "/dev/ttyUSB0"
  },
  "properties": {},
  "UID": "proteusecometer:EcoMeterS:e90705eaa4",
  "thingTypeUID": "proteusecometer:EcoMeterS",
  "channels": [
    {
      "linkedItems": [
        "ProteusEcoMeterS_Temperature"
      ],
      "uid": "proteusecometer:EcoMeterS:e90705eaa4:temperature",
      "id": "temperature",
      "channelTypeUID": "proteusecometer:Temperature",
      "itemType": "Number:CELSIUS",
      "kind": "STATE",
      "label": "Temperature",
      "description": "Temperature measured by the sensor",
      "defaultTags": [],
      "properties": {},
      "configuration": {}
    },
    {
      "linkedItems": [
        "ProteusEcoMeterS_SensorLevelCm"
      ],
      "uid": "proteusecometer:EcoMeterS:e90705eaa4:sensorLevel",
      "id": "sensorLevel",
      "channelTypeUID": "proteusecometer:SensorLevel",
      "itemType": "Number:centiMetre",
      "kind": "STATE",
      "label": "Sensor Level",
      "description": "The distance between the sensor and the water surface",
      "defaultTags": [],
      "properties": {},
      "configuration": {}
    },
    {
      "linkedItems": [
        "ProteusEcoMeterS_UsableLevelinliter"
      ],
      "uid": "proteusecometer:EcoMeterS:e90705eaa4:usableLevelInLitre",
      "id": "usableLevelInLitre",
      "channelTypeUID": "proteusecometer:UsableLevelInLitre",
      "itemType": "Number:LITRE",
      "kind": "STATE",
      "label": "Usable Level in litre",
      "description": "The usable level in litre",
      "defaultTags": [],
      "properties": {},
      "configuration": {}
    },
    {
      "linkedItems": [
        "ProteusEcoMeterS_UsableLevelinpercent"
      ],
      "uid": "proteusecometer:EcoMeterS:e90705eaa4:usableLevelInPercent",
      "id": "usableLevelInPercent",
      "channelTypeUID": "proteusecometer:UsableLevelInPercent",
      "itemType": "Number:PERCENT",
      "kind": "STATE",
      "label": "Usable Level in percent",
      "description": "The usable level in percent",
      "defaultTags": [],
      "properties": {},
      "configuration": {}
    },
    {
      "linkedItems": [
        "ProteusEcoMeterS_TotalCapacityinliter"
      ],
      "uid": "proteusecometer:EcoMeterS:e90705eaa4:totalCapacity",
      "id": "totalCapacity",
      "channelTypeUID": "proteusecometer:TotalCapacity",
      "itemType": "Number:LITRE",
      "kind": "STATE",
      "label": "Total Capacity",
      "description": "The total capacity of your cistern/tank",
      "defaultTags": [],
      "properties": {},
      "configuration": {}
    }
  ]
}
```
