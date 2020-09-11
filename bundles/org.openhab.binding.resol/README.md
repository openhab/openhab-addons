# Resol Binding

Resol Binding connects to Solar and System Controllers of RESOL - Elektronische Regelungen GmbH, also including branded versions from Viessmann, SOLEX, COSMO, SOLTEX, DeDietrich and many more.

This binding is based on and includes the [Resol-VBUS-Java library](https://github.com/danielwippermann/resol-vbus-java), developed by Daniel Wippermann.

## Supported Things

VBusLAN-Bridge, DataLogger DL2 and DL3 as a live data interface between LAN and Resol VBus.
On the DL3 currently there is only the first VBUS channel supported and the sensors directly connected tot he DL3 are not accessible via this binding.

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

## Discovery

Discovery is tested for VBus-LAN adapters DL2, DL3 and KM2 devices, it should also work for other devices providing a live data port.

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
| adapterSerial        | String  | no       | Serialnumber of the device                                 |


## Thing Configuration

Depending on the solar/heating controller you have attached to your VBUS there will be a "controller" and several other things like heat quantity meters, heating circuit controls, etc.
These do not require any specific configuration but will pop up in your inbox after the bridge has received data from them.

## Channels
The channels of a thing are determined automatically based on the received VBUS data.

| channel                       | type                 | description                                        |
|-------------------------------|----------------------|----------------------------------------------------|
| Pump_speed_relay_x            | Number               |                                                    |
| Temperature_sensor_x          | Number:Temperature   | Temperature sensor 'x' of the controller           |
| Temperature_Module_y_Sensor_x | Number:Temperature   | Temperature sensor 'x' of the extension module 'y' |
| Flow_rate_sensor_x            | Number               |                                                    |
| Pressure_sensor_x             | Number               |                                                    |
| Pump_speed_relay_x            | Number               |                                                    |
| Humidity_sensor_x             | Number               |                                                    |
| Irradiation_sensor_x          | Number               |                                                    |
| Runtime_relay_x               | Number               | Runtime for relay 'x' in seconds                   |
| Output_M                      | Number               | PWM/0-10V level value of the output 'M'            |
| Flow_set_temperature          | Number               | Heating circuit set temperature                    |
| Operating_state               | Number               | Heating circuit operationg state                   |
| Operating_state-str           | String               | Heating circuit operationg state, as text          |
| System_date                   | DateTime             | Date and time of the controller clock              |
| Error_mask                    | Number               |                                                    |
| Error_Sensor_line_broken-str  | String               | 

Channels are dynamically created dependent on the devices connected to the VBus.
So far only reading is supported.
The classical channels are for temperature sensors and the like, but also relais outputs with the output level (0-100%) are visible as numerical values with the corresponding unit.
Some datapoints have an enumeration type and are available in two versions, a numerical and a textual channel.
Examples are Error mask, which is a number for the complete mask and each bit is available as single string channel, or the operation state of a heating circuit.
In those cases the numerical version is hidden and have to be view explicitly if needed, while the string representation has an "-str" suffix in the name.

String values are localized as far as possible, but only French, German and English are supported by the underlaying library which is based on the vbus-specification file from Resol.

## Full Example
Bridge resol:vbuslan:MyController "VBUSLAN" @ "Cellar" [ ipAddress="192.168.0.9", password="vbus", port=7053, adapterSerial="00\
1e66421ddf"] {
      Thing device DeltaSol_MX-Controller "DeltaSol MX [Controller]" @ "Cellar" []
      Thing device DeltaSol_MX-Heating_circuit-1 "DeltaSol MX [Heating Circuit]" @ "Cellar" []
}

