# E3DC Binding

<img style="float: right;" src="doc/E3DC_logo.png">
Integrates the Home Power Plants from E3/DC GmbH into openhab. The Power Plant handles all your Electrical Energy Resources like Photovoltaic Producers, Battery Storage, Wallbox Power Supply, Household consumption and even more.  
The binding operates via Modbus to read and write values towards the E3DC device. Please refer to the <a href="./doc/ModBus_E3DC_Speichersysteme_V1.70_2020-06-18.pdf">official Modbus documentation</a> for more details.  
The binding is designed the following way  

1. Create Bridge **E3DC Home Power Plant** and provide IP-Address and Port Number for the general Device Conncetion
2. Add your wanted Blocks 

* if you have a Wallbox connected - add Wallbox Control Block 
* if you want Details of your attached Strings - add String Details Block

With this design it's possible for you to install only the parts you are interested in.


## Supported Things

First you need a Bridge which establishes the basic connection towards your E3DC device

| Name               | Bridge Type ID | Description                                                                                            |
|--------------------|----------------|--------------------------------------------------------------------------------------------------------|
| E3DC Home Power Plant | e3dc-device    | Establishes Modbus Connection to your Device. Add your desired Blocks to this Bridge afterwards.     |

After establishing the Bridge add certain Blocks to gather Informations and Settings

| Name               | Thing Type ID | Description                                                                                            |
|--------------------|----------------|--------------------------------------------------------------------------------------------------------|
| E3DC Information Block | e3dc-info    | Basic Information of your E3DC Device like Model Name, Serial Number and Software Versions             |
| E3DC Power Block | e3dc-power    | Provides values of your attached eletrical Producers (Photovoltaic, Battery, ... and Consumers (Household, Wallbox, ...) |
| E3DC Wallbox Control Block | e3dc-wallbox    | Provides your Wallbox Settings. Switches like "Sunmode" or "3Phase Charging" can be changed! |
| E3DC String Details Block | e3dc-strings    | Provides detailed values of your attached Photovoltaic Strings. Evaluate how much Power each String provides |
| E3DC EMS Block | e3dc-emergency    | Provides values of Emergency Power Status (EMS) and regulations like Battery loading / unloading restrictions |


## Discovery

There's no discovery. Modbus registers are available for all devices. Just install the blocks you are interested in.


## Thing Configuration

The Binding Design requires two steps

1. Create the E3DC Home Power Plant Bridge (e3dc-device) which requires

* IP Address of your device
* Port Number of your device
* optional refresh time in ms, default is set to 2000 = 2 seconds

2. Add your desired Blocks

* each Block requires the created Bridge from point 1
* only the Wallbox Control Block requires an additional Wallbox ID. The E3DC device can handle up to 8 Wallboxes so select a value from 0 to 7

### E3DC Home Power Plant 

See <a href="./doc/ModBus_E3DC_Speichersysteme_V1.70_2020-06-18.pdf">official Modbus documentation</a> for more details

| Parameter        | Type   | Description                                               |           
|-----------------|----------------------------------------------------------------------|
| host            | text    | IP Address of your device   |
| port            | integer | Modbus Port of your device. Default is 502   |
| refresh         | integer | data refresh rate in milliseconds. Default is 2000   |

### E3DC Wallbox Control Block

| Parameter        | type   | Description                                                |
|-----------------|----------------------------------------------------------------------|
| wallboxId        | integer    | The E3DC device can handle up to 8 Wallboxes so select a value from 0 to 7  |

## Channels

The E3DC device offers a huge amount of channels. Due to the Block design you can allocate only your wanted blocks with a restricted amount of Channels. See each Block which Channels are offered

### E3DC Info Block

| Channel Label         | Channel ID | Type   | Description                  |
|-----------------------|------------|--------|------------------------------|
| Modbus-ID             |modbus-id   |  String | Modbus ID / Magic Byte of E3DC  |
| Modbus Firmware       |modbus-firmware| String | Version of Modbus Firmware  |
| Supported Registers   |supported-registers| Number | Number of registers supported by Modbus  |
| Manufacturer Name     | manufacturer-name  | String | Name of the Device Manufacturer  |
| E3DC Model Name       | model-name | String | Name of the E3DC Model  |
| E3DC Firmware Release | firmware-release | String | Firmware installed on this particular E3DC Model  |
| E3DC Serial Number    | serial-number| String | Serial Number of this particular E3DC Model  |


### E3DC Power Block

| Channel Label         | Channel ID      | Type           | Description                  |
|-----------------------|-----------------|----------------|------------------------------|
| PV Output             | pv-power-supply |  Number:Power  | Photovoltaic Power Production    |
| Battery Discharge     | battery-power-supply |  Number:Power  | Battery discharges and provides Power    |
| Battery Charge        | battery-power-consumption |  Number:Power  | Battery charges and consumes Power    |
| Household Consumption | household-power-consumption |  Number:Power  | Household consuming Power    |
| Grid Power Consumption| grid-power-consumption |  Number:Power  | Grid Power is needed in order to satisfy your overall Power consumption    |
| Grid Power Supply     | grid-power-supply |  Number:Power  | More Photovoltaic Power is produced than needed. Additional Power is provided towards the Grid    |
| External Power Supply | external-power-supply |  Number:Power  | Power produced by an external device which is attached to your E3DC device    |
| Wallbox Power Consumption | wallbox-power-consumption |  Number:Power  | Power consumption of attached Wallboxes    |
| Wallbox PV Power Consumption  | wallbox-pv-power-consumption |  Number:Power  | Photovoltaic Power consumption (PV plus Battery) of attached Wallboxes    |
| Autarky               |autarky-channel |  Number:Percent  | Your current Autarky Level    |
| Self Consumtion       | self-consumption |  Number:Percent  | Your current Photovoltaic Self Consumption Level    |
| Battery State Of Charge | battery-soc |  Number:Percent  | Charge Level of your attached Battery    |

### E3DC Wallbox Control Block

| Channel Label         | Channel ID      | Type           | Description                  |
|-----------------------|-----------------|----------------|------------------------------|
| Wallbox Available     | wb-available |  Switch  | Indicates if the Wallbox is attached. Check your Wallbox ID in offline case.  **read-only**  |
| Sun Mode              | wb-sunmode-channel |  Switch  | Activate / Deactivate Sun Mode. Off case takes Grid Power to ensure highest possible charging.  **read-write**  |
| Wallbox Charging      | wb-charging-channel |  Switch  | Indicates your Wallbox is charging.  **read-write**    |
| Jack Locked           | wb-jack-locked |  Switch  | Indicates your Jack is locked.  **read-only**    |
| Jack Plugged          | wb-jack-plugged |  Switch  | Indicates your Jack is plugged.  **read-only**    |
| Schuko Socket On      | wb-schuko-on |  Switch  | If your Wallbox has an additional Schuko Socket it provides state ON or OFF.  **read-write**    |
| Schuko Socket Plugged | wb-schuko-plugged |  Switch  |If your Wallbox has an additional Schuko Socket it provides plugged state ON or OFF.  **read-only**     |
| Schuko Socket Locked  | wb-schuko-locked-channel |  Switch  | If your Wallbox has an additional Schuko Socket it provides locked state ON or OFF.  **read-only** |
| 16A Relay On          | wb-relay-16a |  Switch  | Indicates if 16A Relay is ON  **read-only**    |
| 32A Relay On          | wb-relay-32a |  Switch  | Indicates if 32A Relay is ON  **read-only**    |
| 3-Phase Charging      | 3-Phase Active |  Switch  | Indicates if 3-phase charging is activated. If OFF 1-phase charging is activated  **read-write**   |

### E3DC String Details Block

| Channel Label         | Channel ID      | Type           | Description                  |
|-----------------------|-----------------|----------------|------------------------------|
| String 1 Potential    | string1-dc-voltage |  Number:Volt  | Volt on String 1           |
| String 2 Potential    | string2-dc-voltage |  Number:Volt  | Volt on String 2           |
| String 3 Potential    | string3-dc-voltage |  Number:Volt  | Volt on String 3           |
| String 1 Current      | string1-dc-current |  Number:Ampere  | Ampere on String 1       |
| String 2 Current      | string2-dc-current |  Number:Ampere  | Ampere on String 2       |
| String 3 Current      | string3-dc-current |  Number:Ampere  | Ampere on String 3       |
| String 1 Power        | string1-dc-output  |  Number:Power   | Watt produced by String 1 |
| String 2 Power        | string2-dc-output  |  Number:Power   | Watt produced by String 2 |
| String 3 Power        | string3-dc-output  |  Number:Power   | Watt produced by String 3 |


### E3DC EMS Block

| Channel Label         | Channel ID      | Type           | Description                  |
|-----------------------|-----------------|----------------|------------------------------|
| Emergency Power Status| emergency-power-status |  String  | Indicates if Emergency Power Supply is possible or not, active or inactive |
| Battery Loading Locked | battery-loading-lock |  Switch  | Indictes if Battery Loading is locked           |
| Battery Unloading Locked | battery-unloading-lock |  Switch  | Indictes if Battery Unloading is locked |
| Emergency Power Possible| emergency-power-possible |  Switch  | Indicates if Emergency Power Supply is possible          |
| Loading Based On Weather Prediction Active| weather-predicted-loading |  Switch  | Indicates if Weather Predicted Battery Loading is actived |
| Regultation Status Of Max Grid Power Supply| regulation-status |  Switch  | Indicates if Grid Power Supply is regulated or not |
| Loading Locktime Active| loading-lock-time |  Switch  | Indicates if Loading Lock Times are set or not |
| Unloading Locktime Active| unloading-lock-time |  Switch  |Indicates if Unloading Lock Times are set or not |

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
