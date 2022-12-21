# HeliosVentilation Binding

This is the binding for Helios Ventilation Systems KWL EC 200/300/500 Pro.
It requires a connection to the RS485 bus used by the original remote controls KWL-FB (9417) and does not use the Modbus/TCP interface of the newer EasyControl devices.

For electrical connection it is recommended to use an USB-RS485 interface, but any RS485 interface that shows up as a serial port will do.
Setup the device as described in <https://www.openhab.org/docs/administration/serial.html>.

The binding will use the remote control address 15 for communication, so make sure that this is not assigned to a physically present remote control.

For Helios ventilation devices supporting the easyControls web interface, the separate binding [Helios easyControls binding](https://www.openhab.org/addons/bindings/modbus.helioseasycontrols/) can be used.

## Supported Things

There is only one thing type supported by this binding: a Helios Ventilation System KWL EC 200/300/500 Pro from Helios.
The binding was developed and test on a KWL EC 200 Pro device.

## Binding Configuration

The binding requires access to the serial device connecting to the RS485 bus as described in <https://www.openhab.org/docs/administration/serial.html>.
Otherwise only thing configuration is needed.

## Thing Configuration

The binding supports only one thing and requires the configuration of the serial port (typically /dev/ttyUSB0 on Linux and COM3 on Windows) and optionally the polling time which is the cycle time after which the binding tries to reconnect to the bus and requests data updates.

## Channels

Supported operation channels:

| channel            | type                 | description                                   |
|--------------------|----------------------|-----------------------------------------------|
| outsideTemp        | Number:Temperature   | Temperature sensor in the outside air flow    |
| outgoingTemp       | Number:Temperature   | Temperature sensor in the outgoing air flow   |
| extractTemp        | Number:Temperature   | Temperature sensor in the extract air flow    |
| supplyTemp         | Number:Temperature   | Temperature sensor in the supply air flow     |
| setTemp            | Number:Temperature   | Set temperature for supply (not always used)  |
| fanspeed           | Number               | Level of the fanspeed (1-8)                   |
| powerState         | Switch               | Main power switch                             |
| co2State           | Switch               | Switch for CO2 regulation                     |
| rhState            | Switch               | Switch for humidity regulation                |
| winterMode         | Switch               | Switch to set winter mode                     |

Supported configuration channels:

| channel            | type                 | description                                   |
|--------------------|----------------------|-----------------------------------------------|
| bypassTemp         | Number:Temperature   | Temperature to disable the bypass function    |
| supplyStopTemp     | Number:Temperature   | Temperature to stop supply fan for defrosting |
| preheatTemp        | Number:Temperature   | Temperature to enable the preheater           |
| minFanspeed        | Number               | Minimal level of the fanspeed (1-8)           |
| maxFanspeed        | Number               | Maximal level of the fanspeed (1-8)           |
| rhLimit            | Number:Dimensionless | Limit for relative humidity sensor            |
| hysteresis         | Number:Temperature   | Hysteresis on defroster temperature           |
| DCFanExtract       | Number:Dimensionless | Speed reduction for the extract fan           |
| DCFanSupply        | Number:Dimensionless | Speed reduction for the supply fan            |
| maintenanceInterval| Number:Dimensionless | Maintenance interval in months                |
| adjustInveral      | Number:Dimensionless | Adjust interval in minutes for air quality    |
| RHLevelAuto        | Switch               | Automatic base humidity determination         |
| switchType         | Switch               | External Switch type (Boost or Fireplace)     |
| radiatorType       | Switch               | Use water (ON) or electric (OFF) radiator     |
| cascade            | Switch               | System is cascaded                            |

Note: the configuration channels are not intended to be written regularly.

## Full Example

Things:

```java
heliosventilation:ventilation:MyKWL  [ serialPort="/dev/ttyUSB0" ]
```

Items:

```java
Switch KWLOnOff { channel="heliosventilation:ventilation:MyKWL:powerState" }
Switch KWLWinter { channel="heliosventilation:ventilation:MyKWL:winterMode" }

Group VentilationTemp "Measured Temperatures in Ventilation System"

Number:Temperature Outside_Temperature "Outside Temperature [%.1f °C]" <temperature> (VentilationTemp) { channel="heliosventilation:ventilation:MyKWL:outsideTemp" }
Number:Temperature Outgoing_Temperature "Outgoing Temperature [%.1f °C]" <temperature> (VentilationTemp) { channel="heliosventilation:ventilation:MyKWL:outgoingTemp" }
Number:Temperature Extract_Temperature "Extract Temperature [%.1f °C]" <temperature> (VentilationTemp) { channel="heliosventilation:ventilation:MyKWL:extractTemp" }
Number:Temperature Supply_Temperature "Supply Temperature [%.1f °C]" <temperature> (VentilationTemp) { channel="heliosventilation:ventilation:MyKWL:supplyTemp" }

Number Fan_Speed "Fan Speed" <fan> { channel="heliosventilation:ventilation:MyKWL:fanspeed" }
Number Min_Fan_Speed "Min Fan Speed" <fan> { channel="heliosventilation:ventilation:MyKWL:minFanspeed" }
Number Max_Fan_Speed "Max Fan Speed" <fan> { channel="heliosventilation:ventilation:MyKWL:maxFanspeed" }

```

Sitemap:

```perl
sitemap helios_kwl label="Helios Ventilation" {
        Frame label="Temperatures" {
              Text item=Outside_Temperature
              Text item=Outgoing_Temperature
              Text item=Extract_Temperature
              Text item=Supply_Temperature
        }
        Frame label="Control" {
              Switch item=KWLOnOff
              Switch item=KWLWinter
              Slider item=Fan_Speed icon="fan" minValue=1 maxValue=8 step=1
        }
        Frame label="Configuration" {
              Slider item=Min_Fan_Speed
              Setpoint item=Max_Fan_Speed icon="fan"
        }
}
```
