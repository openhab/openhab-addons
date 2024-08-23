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

```text
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
| usableLevel           | Number:Volume        | How much liquid is usable                            |
| usableLevelInPercent  | Number:Dimensionless | How much liquid is usable relative to total capacity |
| totalCapacity         | Number:Volume        | Total capacity of measured cistern/tank              |

## Full Example

Thing proteusecometer:EcoMeterS:e90705eaa4 "Proteus EcoMeter S" [ usbPort="/dev/ttyUSB0" ]

Number:Temperature   Temperature          "Measured temperature [%.1f °C]" { channel="proteusecometer:EcoMeterS:e90705eaa4:temperature" }
Number:Length        SensorLevelCm        "Sensor Level"                   { channel="proteusecometer:EcoMeterS:e90705eaa4:sensorLevel" }
Number:Volume        UsableLevel          "Usable Level"                   { channel="proteusecometer:EcoMeterS:e90705eaa4:usableLevel" }
Number:Dimensionless UsableLevelinpercent "Usable Level"                   { channel="proteusecometer:EcoMeterS:e90705eaa4:usableLevelInPercent" }
Number:Volume        TotalCapacityinliter "Total Capacity"                 { channel="proteusecometer:EcoMeterS:e90705eaa4:totalCapacity" }
