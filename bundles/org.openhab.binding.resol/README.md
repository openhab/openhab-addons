# Resol Binding

Resol Binding connects to Solar and System Controllers of RESOL - Elektronische Regelungen GmbH, also including branded versions from Viessmann, SOLEX, COSMO, SOLTEX, DeDietrich and many more.

![Resol](doc/RESOL_Logo_de.png)

This binding is based on and includes the [Resol-VBUS-Java library](https://github.com/danielwippermann/resol-vbus-java), developed by Daniel Wippermann.


## Supported Things

VBusLAN-Bridge, DataLogger DL2 and DL3 as a live data interface between LAN and Resol VBus.
On the DL3 currently there is only the first VBUS channel supported and the sensors directly connected to the DL3 are not accessible via this binding.

On top of the bridge devices, which enables access to the VBUS many, if not all, Resol Controllers and Modules like WMZ heat meters, HKM Heating circuit extensions etc. are supported including branded versions from different suppliers. This includes

 * Solar controller DeltaSol® A/AX/AX HE
 * Solar controller DeltaSol® AL E HE
 * Solar controller DeltaSol® CS (Plus)
 * Solar controller DeltaSol® B
 * Solar controller DeltaSol® BS series
 * Solar controller DeltaSol® SLL
 * Solar controller DeltaSol® SL
 * Solar controller DeltaSol® BX series
 * System controller DeltaSol® SLT
 * System controller DeltaSol® MX
 * System controller DeltaSol® E
 * DeltaSol Fresh®
 * DeltaSol® Pool
 * DeltaSol® Minipool
 * DeltaSol® ES
 * Frista
 * DeltaTherm® HC
 * DeltaTherm® FK
 * Deltatherm® HT
 * DeltaTherm® DH
 * Sonnenkraft SKSC3
 * Sonnenkraft STRG BX PLUS
 * Sonnenkraft SKSC3+
 * Sonnenkraft SKSC3HE
 * Sonnenkraft SKSR 1/2/3
 * Vitosolic 200
 * COSMO Multi
 * Drainback DeDietrich
 * Diemasol C

A more complete list can be found in the doc of the [resol-vbus-java library](http://danielwippermann.github.io/resol-vbus/vbus-packets.html).

## Discovery

Discovery is tested for VBus-LAN adapters DL2, DL3 and KM2 devices, it should also work for other devices providing a live data port.
After a bridge is detected in the local network the password needs to be given and the things on the VBUS will popup in the inbox.

## Binding Configuration

The Resol binding doesn't need any form of configuration in files.

## Bridge Configuration

The bridge is the device connecting the Resol VBUS to the network, usually a VBus-LAN adapter or integrated in some of the solar controllers like DL3.
For the connection from the Resol binding the bridge requires the configuration of

| property             | type    | Required | description                                                |
|----------------------|---------|----------|------------------------------------------------------------|
| ipAddress            | String  | yes      | IP address or hostname of the VBUS adapter                 |
| password             | String  | yes      | Password, defaults to 'vbus' for factory setting devices   |
| port                 | Number  | no       | Port for the TCP connection, defaults to 7053              |
| adapterSerial        | String  | no       | Serialnumber of the device (informative only)              |


## Thing Configuration

Depending on the solar/heating controller you have attached to your VBUS there will be a "controller" and several other things like heat quantity meters, heating circuit controls, etc.
These do not require any configuration parameters and will pop up in your inbox after the bridge has received data from them.
The name of the devices is usually the Resol name with spaced replaced by _ and a "-Controller" suffix like "DeltaSol_MX-Controller".
For configuration in files you can enable the logging with at least INFO level for the resol binding and search the logs for "ThingHandler for (.*) not registered." to identify the names of the things you can add for your VBUS devices.


## Channels

The channels of a thing are determined automatically based on the received VBUS data and are highly dependent on the used device.
Here is a list of the channels of a DeltaSol MX with a heat quantity meter (HQM) and an extension module EM.
The channels supported for your device can be seen after autodiscovery or 

| channel                       | type                     | description                                        |
|-------------------------------|--------------------------|----------------------------------------------------|
| Pump_speed_relay_x            | Number:Dimensionless     | Percentage of the output state of relay 'x'        |
| Temperature_sensor_x          | Number:Temperature       | Temperature sensor 'x' of the controller           |
| Temperature_Module_y_Sensor_x | Number:Temperature       | Temperature sensor 'x' of the extension module 'y' |
| Pressure_sensor_x             | Number:Pressure          | Pressure sensor 'x'                                |
| Humidity_sensor_x             | Number:Dimensionless     | Humidity sensor 'x'                                |
| Irradiation_sensor_x          | Number:Intensity         | Sunlight intensity sensor                          |
| Output_M                      | Number:Dimensionless     | PWM/0-10V level value of the output 'M'            |
| System_date                   | DateTime                 | Date and time of the controller clock              |
| Error_mask                    | Number                   | Bitmask for the different errors                   |
| Error_Sensor_line_broken-str  | String                   | Sensor line broken status (details for Error_mask) |
| Flow_rate_sensor_x            | Number:VolumetricFlowRate| of sensor 'x'                                      |
| Flow_set_temperature          | Number:Temperature       | Heating circuit set temperature                    |
| Operating_state               | Number                   | Heating circuit operationg state                   |
| Operating_state-str           | String                   | Heating circuit operationg state, as text          |
| Heat_quantity                 | Number:Energy            | Total heat quantity (of a HQM)                     |
| Heat_quantity_today           | Number:Energy            | Todays heat quantity (of a HQM)                    |
| Heat_quantity_week            | Number:Energy            | This weeks heat quantity (of a HQM)                |
| Heat_quantity_month           | Number:Energy            | This months heat quantity (of a HQM)               |
| Volume_in_total               | Number:Volume            | Total volume (of a HQM)                            |
| Volume_today                  | Number:Volume            | Todays volume (of a HQM)                           |
| Volume_week                   | Number:Volume            | This weeks volume (of a HQM)                       |
| Volume_month                  | Number:Volume            | This months volume (of a HQM)                      |
| Power                         | Number:Power             | Current power (of a HQM)                           |


Channels are dynamically created dependent on the devices connected to the VBus.
So far only reading is supported.
The classical channels are for temperature sensors and the like, but also relay outputs with the output level (0-100%) are visible as numerical values with the corresponding unit.
Some data points have an enumeration type and are available in two versions, a numerical and a textual channel.
Examples are Error mask, which is a number for the complete mask and each bit is available as single string channel, or the operation state of a heating circuit.
In those cases the numerical version is hidden and have to be view explicitly if needed, while the string representation has an "-str" suffix in the name.

String values are localized as far as possible, but only French, German and English are supported by the underlaying library which is based on the vbus-specification file from Resol.

## Full Example

For a DeltaSol MX system controller with on extension module EM you can use this example:

resol.things

```
Bridge resol:vbuslan:VBUS "VBUSLAN" [ ipAddress="192.168.0.2", password="vbus", port=7053] {
      Thing device DeltaSol_MX-Controller "DeltaSol MX [Controller]" []
      Thing device DeltaSol_MX-Heating_circuit-1 "DeltaSol MX [Heating Circuit]" []
      Thing device DeltaSol_MX-HQM-1 "DeltaSol MX [WMZ 1] Solar" []
      Thing device DeltaSol_MX-Modules "DeltaSol MX [Modules]" []
}
```

resol.items
```
/*************************************************/
/* Solar system                                  */
/*************************************************/
Number:Temperature SolarTemperature "Solar Collector Temperature [%.1f °C]" <temperature> { channel="resol:device:VBUS:DeltaSol_MX-Controller:Temperature_sensor_1" }
Number:Temperature TankTemperature "Solar Tank Temperature [%.1f °C]" <temperature> { channel="resol:device:VBUS:DeltaSol_MX-Controller:Temperature_sensor_2" }
Number:Intensity Irradiation "Irradiation [%.1f W/m²]" <sun> {channel="resol:device:VBUS:DeltaSol_MX-Controller:Irradiation_sensor_16"}
Number SolarPump "Solar pump [%.0f %%]" {channel="resol:device:VBUS:DeltaSol_MX-Controller:Pump_speed_relay_1"}


/*************************************************/
/* Heating circuit                               */
/*************************************************/
Number:Temperature FlowSetTemperature "Flow Set Temperature [%.1f °C]" <temperature>  {channel="resol:device:VBUS:DeltaSol_MX-Heating_circuit-1:Flow_set_temperature"}

String HeatCircuit_OperatingState "HeatCircuit OperatingState [%s]" {channel="resol:device:VBUS:DeltaSol_MX-Heating_circuit-1:Operating_state-str"}


/*************************************************/
/* Heat quantity meter                           */
/*************************************************/
Number:Energy SolarEnergy_today "Solar Energy (today) [%.1f Wh]" {channel="resol:device:VBUS:DeltaSol_MX-HQM-1:Heat_quantity_today"}
Number:Power SolarPower "Solar Power [%.0f W]" {channel="resol:device:VBUS:DeltaSol_MX-HQM-1:Power"}


/*************************************************/
/* EM Module                                     */
/*************************************************/
Number:Temperature EM_Temperature_1 "Temperature EM sensor 1 [%.1f °C]" <temperature> {channel="resol:device:VBUS:DeltaSol_MX-Modules:Temperature_Module_1_Sensor_1"}

/*************************************************/
/* Failure handling                              */
/*************************************************/
Number Errormask "Error mask [%.0f]" {channel="resol:device:VBUS:DeltaSol_MX-Controller:Error_mask"}
Number Warningmask "Warning mask [%.0f]" {channel="resol:device:VBUS:DeltaSol_MX-Controller:Warning_mask"}
String brokenSensor "Broken Sensor [%s]" {channel="resol:device:VBUS:DeltaSol_MX-Controller:Error_Sensor_line_broken-str"}


```

resol.sitemap

```
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
        Text item=brokenSensor valuecolor=[=="Okay"="green", !="Okay"="red"]
    }
}

```