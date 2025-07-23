# Resol Binding

Resol Binding connects to Solar and System Controllers of RESOL - Elektronische Regelungen GmbH, also including branded versions from Viessmann, SOLEX, COSMO, SOLTEX, DeDietrich and many more.

![Resol](doc/RESOL_Logo_de.png)

This binding is based on and includes the [Resol-VBUS-Java library](https://github.com/danielwippermann/resol-vbus-java), developed by Daniel Wippermann.

## Supported Things

### Bridge

For the connection of the VBUS devices a network interface is required.
Supported interfaces are VBUS-LAN, KM1, KM2, DataLogger DL2 and DL3 as live data interface between openHAB and the Resol VBUS via network.
On the DL3 currently there is only the first VBUS channel supported and the sensors directly connected to the DL3 are not accessible via this binding.
Currently only network based bridges are supported and if a USB-VBUS interface shall be used a serial-network proxy has to be used.
In the binding might support USB-type bridges in future.

### Device

On top of the bridge things, which enable access to the VBUS, many - if not all - Resol Controllers and Modules like WMZ heat meters, HKM Heating circuit extensions etc. are supported including branded versions from different suppliers as thing type _device_.
The supported devices include

- Solar controller DeltaSol® A/AX/AX HE
- Solar controller DeltaSol® AL E HE
- Solar controller DeltaSol® CS (Plus)
- Solar controller DeltaSol® B
- Solar controller DeltaSol® BS series
- Solar controller DeltaSol® SLL
- Solar controller DeltaSol® SL
- Solar controller DeltaSol® BX series
- System controller DeltaSol® SLT
- System controller DeltaSol® MX
- System controller DeltaSol® E
- DeltaSol Fresh®
- DeltaSol® Pool
- DeltaSol® Minipool
- DeltaSol® ES
- Frista
- DeltaTherm® HC
- DeltaTherm® FK
- Deltatherm® HT
- DeltaTherm® DH
- Sonnenkraft SKSC3
- Sonnenkraft STRG BX PLUS
- Sonnenkraft SKSC3+
- Sonnenkraft SKSC3HE
- Sonnenkraft SKSR 1/2/3
- Vitosolic 200
- COSMO Multi
- Drainback DeDietrich
- Diemasol C

A more complete list can be found in the doc of the [resol-vbus-java library](https://danielwippermann.github.io/resol-vbus/vbus-packets.html).

### Emulated Extension Module EM

Some controllers like the Deltasol MX can be connected to an extension module, which can be emulated by the thing type _emulatedEM_.
The emulated EM is a virtual device, visible on the VBUS to a Resol controller and provides an interface between openHAB and the controller.
Relay channels are outputs from the controller point of view and therefore read-only in OH.
The sensor channels as inputs for the solar or system controller and intended to be written by OH.

## Discovery

Discovery is tested for VBus-LAN adapters DL2, DL3 and KM2 devices, it should also work for other devices providing a live data port.
After a bridge is detected in the local network the password needs to be given and the things on the VBUS will popup in the inbox.

## Bridge Configuration

The bridge is the device connecting the Resol VBUS to the network, usually a VBus-LAN adapter or integrated in some of the solar controllers like DL3.
For the connection from the Resol binding the bridge requires the configuration of the following parameters:

| Parameter            | Type    | Required | Description                                                |
|----------------------|---------|----------|------------------------------------------------------------|
| ipAddress            | String  | yes      | IP address or hostname of the VBUS adapter                 |
| password             | String  | yes      | Password, defaults to 'vbus' for factory setting devices   |
| port                 | Number  | no       | Port for the TCP connection, defaults to 7053              |
| adapterSerial        | String  | no       | Serialnumber of the device (informative only)              |

## Device Configuration

Depending on the solar/heating controller you have attached to your VBUS there will be a "controller" and several other things like heat quantity meters, heating circuit controls, etc.
These do not require any configuration parameters and will pop up in your inbox after the bridge has received data from them.
The name of the devices is usually the Resol name with spaced replaced by `_` and a "-Controller" suffix like "DeltaSol_MX-Controller".
For configuration in files you can enable the logging with at least DEBUG level for the resol binding and search the logs for "ThingHandler for (.*) not registered." to identify the names of the things you can add for your VBUS devices.

## Emulated EM Configuration

_emulatedEM_ devices cannot be auto-discovered and require beside the bridge the following configuration:

| Parameter | Type | Required | Description                                                |
|-----------|------|----------|-----------------------------------------------------------------------------------------------------------------|
| moduleID  | int  | yes      | The module ID on the VBUS in range 0-15, but further restrictions might apply depending on the resol controller. |

## Device Channels

The channels of a thing are determined automatically based on the received VBUS data and are highly dependent on the used device.
Here is a list of the channels of a DeltaSol MX with a heat quantity meter (HQM) and an extension module EM.
The channels supported for your device can be seen in the UI or in the logs if DEBUG logging is enabled for this binding after data is received from the physical device.

| Channel                           | Type                     | Description                                        |
|-----------------------------------|--------------------------|----------------------------------------------------|
| pump_speed_relay_x                | Number:Dimensionless     | Percentage of the output state of relay 'x'        |
| temperature_sensor_x              | Number:Temperature       | Temperature sensor 'x' of the controller           |
| temperature_module_y_sensor_x     | Number:Temperature       | Temperature sensor 'x' of the extension module 'y' |
| pressure_sensor_x                 | Number:Pressure          | Pressure sensor 'x'                                |
| humidity_sensor_x                 | Number:Dimensionless     | Humidity sensor 'x'                                |
| irradiation_sensor_x              | Number:Intensity         | Sunlight intensity sensor                          |
| output_m                          | Number:Dimensionless     | PWM/0-10V level value of the output 'm'            |
| system_date                       | DateTime                 | Date and time of the controller clock              |
| error_mask                        | Number                   | Bitmask for the different errors                   |
| error_sensor_line_broken          | Number                   | Sensor line broken status (details for error_mask) |
| error_sensor_line_short-circuited | Number                   | Sensor short circuit status (details for error_mask) |
| flow_rate_sensor_x                | Number:VolumetricFlowRate| Flow rate of sensor 'x'                            |
| flow_set_temperature              | Number:Temperature       | Heating circuit set temperature                    |
| operating_state                   | Number                   | Heating circuit operating state                    |
| heat_quantity                     | Number:Energy            | Total heat quantity (of a HQM)                     |
| heat_quantity_today               | Number:Energy            | Todays heat quantity (of a HQM)                    |
| heat_quantity_week                | Number:Energy            | This weeks heat quantity (of a HQM)                |
| heat_quantity_month               | Number:Energy            | This months heat quantity (of a HQM)               |
| volume_in_total                   | Number:Volume            | Total volume (of a HQM)                            |
| volume_today                      | Number:Volume            | Todays volume (of a HQM)                           |
| volume_week                       | Number:Volume            | This weeks volume (of a HQM)                       |
| volume_month                      | Number:Volume            | This months volume (of a HQM)                      |
| power                             | Number:Power             | Current power (of a HQM)                           |

Channels are dynamically created dependent on the devices connected to the VBus.
So far only reading is supported.
The classical channels are for temperature sensors and the like, but also relay outputs with the output level (0-100%) are visible as numerical values with the corresponding unit.

String values are localized as far as possible, but only French, German and English are supported by the underlaying library which is based on the vbus-specification file from Resol.

## EmulatedEM Channels

The channels of an emulated EM modules are as for physical EMs 5 relay channels and 6 input channels.
The relays are virtual outputs and read-only in OH.
The sensors support different types like temperature input which are simulated by a PT1000 resistance value, a switch and the raw resistance value.
Additionally the virtual input device for adjusting the heating circuits as a _BAS_ is supported by two different channels for temperature and mode adjustment.
The type of the sensor inputs must be configured in the Resol Controller accordingly.
From all possible sensor channels (temperatureX, switchX, etc.) only one shall be linked to an item at a time, except for BAS which emulates a RCP12 room control unit where both, BasTempAdjustmentX and BasModeX shall be written from OH.

| Channel              | Type                      | Description                                        |
|----------------------|---------------------------|----------------------------------------------------|
| relayX               | Number:Dimensionless      | Read-only percentage of the virtual output state of relay 'x' as set by the Resol Controller.        |
| temperatureX         | Number:Temperature        | Writable temperature value for the virtual input for sensor 'x'. |
| resistorX            | Number:ElectricResistance | Writable resistance value for the virtual input for sensor 'x'.  |
| switchX              | Switch                    | Writable switch state for the virtual input for sensor 'x'.      |
| BasTempAdjustmentX   | Number:Temperature        | Writable temperature adjustment for the virtual room control module BAS on the for the virtual input for sensor 'x'. Use together with BasModeX, not effective if BasModeX is OFF or Party.           |
| BasModeX             | Number                    | Writable heating circuit mode for the virtual room control module BAS on the for the virtual input for sensor 'x'. Use together with BasTempAdjustmentX.|

## Full Example

For a DeltaSol MX system controller with on extension module EM you can use this example:

resol.things

```java
Bridge resol:vbuslan:VBUS "VBUSLAN" [ ipAddress="192.168.0.2", password="vbus", port=7053] {
      Thing device DeltaSol_MX-Controller "DeltaSol MX [Controller]" []
      Thing device DeltaSol_MX-Heating_circuit-1 "DeltaSol MX [Heating Circuit]" []
      Thing device DeltaSol_MX-HQM-1 "DeltaSol MX [WMZ 1] Solar" []
      Thing device DeltaSol_MX-Modules "DeltaSol MX [Modules]" []
      Thing emulatedEM EM2 "Emulated EM2" [deviceId=2]
}
```

resol.items

```java
/*************************************************/
/* Solar system                                  */
/*************************************************/
Number:Temperature SolarTemperature "Solar Collector Temperature [%.1f %unit%]" <temperature> { channel="resol:device:VBUS:DeltaSol_MX-Controller:temperature_sensor_1" }
Number:Temperature TankTemperature "Solar Tank Temperature [%.1f %unit%]" <temperature> { channel="resol:device:VBUS:DeltaSol_MX-Controller:temperature_sensor_2" }
Number:Intensity Irradiation "Irradiation [%.1f %unit%]" <sun> {channel="resol:device:VBUS:DeltaSol_MX-Controller:irradiation_sensor_16"}
Number SolarPump "Solar pump [%.0f %%]" {channel="resol:device:VBUS:DeltaSol_MX-Controller:pump_speed_relay_1"}

/*************************************************/
/* Heating circuit                               */
/*************************************************/
Number:Temperature FlowSetTemperature "Flow Set Temperature [%.1f %unit%]" <temperature>  {channel="resol:device:VBUS:DeltaSol_MX-Heating_circuit-1:flow_set_temperature"}

String HeatCircuit_OperatingState "HeatCircuit OperatingState [%s]" {channel="resol:device:VBUS:DeltaSol_MX-Heating_circuit-1:operating_state"}

/*************************************************/
/* Heat quantity meter                           */
/*************************************************/
Number:Energy SolarEnergy_today "Solar Energy (today) [%.1f  %unit%]" {channel="resol:device:VBUS:DeltaSol_MX-HQM-1:heat_quantity_today"}
Number:Power SolarPower "Solar Power [%.0f %unit%]" {channel="resol:device:VBUS:DeltaSol_MX-HQM-1:power"}

/*************************************************/
/* Physical EM Module 1                          */
/*************************************************/
Number:Temperature EM_Temperature_1 "Temperature EM sensor 1 [%.1f %unit%]" <temperature> {channel="resol:device:VBUS:DeltaSol_MX-Modules:temperature_module_1_sensor_1"}

/*************************************************/
/* Virtual EM Module 2, simulated by openHAB      */
/*************************************************/
Number:Dimensionless Relay_1 "Virtual Relay 1 on EM2 [%d %%] {channel="resol:emulatedEM:VBUS:EM2:relay_1"}
Number:Dimensionless Emu_Temperature_1 "Virtual temperature input 1 on EM2 [%.1f %unit%] <temperature> {channel="resol:emulatedEM:VBUS:EM2:temperature_1"}
Switch Emu_Switch_2 "Virtual switch input 2 on EM2 " {channel="resol:emulatedEM:VBUS:EM2:switch_2"}
Number:Temperature EM_BAS_Set_Temperature_3 "Set Temperature of virtual room control unit on EM2 sensor 3 [%.1f %unit%]" <temperature> {channel="resol:emulatedEM:VBUS:EM2:bas_temp_adjust_3"}
Number EM_BAS_Mode "Mode of virtual room control unit on EM2 sensor 3 [%.1f %unit%]" <temperature> {channel="resol:emulatedEM:VBUS:EM2:bas_mode_3"}

/*************************************************/
/* Failure handling                              */
/*************************************************/
Number Errormask "Error mask [%.0f]" {channel="resol:device:VBUS:DeltaSol_MX-Controller:error_mask"}
Number Warningmask "Warning mask [%.0f]" {channel="resol:device:VBUS:DeltaSol_MX-Controller:warning_mask"}
String BrokenSensor "Broken Sensor [%s]" {channel="resol:device:VBUS:DeltaSol_MX-Controller:error_Sensor_line_broken"}
```

resol.sitemap

```perl
sitemap resol label="DeltaSol MX" {
    Frame label="Solar" {
        Text item=SolarTemperature valuecolor=[<0="white", <20="blue", <50="green", <80="orange", <120="red", >=120="black"]
        Text item=TankTemperature valuecolor=[<30="blue", <50="yellow", <70="orange", >=70="red"]
        Text item=Irradiation
        Text item=SolarPump valuecolor=[==0="red",==100="green", >50="orange", >0="yellow"]
        Text item=SolarPower
        Text item=SolarEnergy_today
    }
    Frame label="Status" {
        Text item=Errormask valuecolor=[==0="green", !=0="red"]
        Text item=Warningmask valuecolor=[==0="green", !=0="red"]
        Text item=BrokenSensor valuecolor=[=="Okay"="green", !="Okay"="red"]
    }
    Frame label="Emulated EM" {
        Default item=Emu_Switch_2
        Setpoint item=EM_BAS_Set_Temperature_3 label="Room Temperature Adjust [%.1f °C]" step=0.5 minValue=-15 maxValue=15
    }
}

```
