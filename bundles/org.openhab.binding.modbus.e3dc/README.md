# E3DC Binding

<img align="right" src="./doc/E3DC_logo.png" />
Integrates the Home Power Plants from E3/DC GmbH into openHAB. See [E3DC Website](https://www.e3dc.com/) to find more informations about the device.
The Power Plant handles all your Electrical Energy Resources like Photovoltaic Producers, Battery Storage, Wallbox Power Supply, Household Consumption and even more.  
E3DC devices are integrated into the Modbus Binding. If you want to install an E3DC device via PaperUI perform the following steps

1. Go to "Configuration - Things" and press the blue "+" sign in the main window
2. Choose entry "Modbus Binding". There's no discovery so choose "MANUALLY ADD THING"
3. You should see now 2 possible E3DC devices

* the "E3DC Home Power Plant" as central device 
* the "E3DC Wallbox" attached to a certain Power Plant

See chapter [Configuration](#thing-configuration) how to set them up in PaperUI or check the [full example Things](#things) for manual setup. 


## Supported Things

First you need a Bridge which establishes the basic connection towards your E3DC device

| Name                  | Thing Type ID | Description                                                                                          |
|-----------------------|---------------|------------------------------------------------------------------------------------------------------|
| E3DC Home Power Plant | e3dc          | Provides Power values, String Details, Emergency Power Status and general Information of your E3DC Home Power Plant    |
| E3DC Home Power Plant | e3dc-wallbox  | Provides your Wallbox Settings. Switches like "Sunmode" or "1-Phase Charging" can be changed!     |


## Discovery

There's no discovery. Modbus registers are available for all devices. Just install the blocks you are interested in.


## Thing Configuration

As mentioned earlier in the [Binding Description](#e3dc-binding) the needed Things can be found in the **Modbus Binding** and have to be added manually without Discovery

1. Add a "Modbux TCP Slave". IP-Address, Modbus Port and Device ID are mandatory configuration parameters and have to match your E3DC Settings
2. Now add an "E3DC Home Power Plant". Bridge is the previously created Modbus TCP Slave. The Power Plant provides 4 different Channel with all available information of your E3DC device.  
3. If you have a Wallbox attached add "E3DC Wallbox". Bridge is your attached E3DC Home Power Plant, mandatory configuration parameter is the Wallbox ID

Check the [full example Things](#things) for manual setup.

### Modbus TCP Slave 

| Parameter       | Type    | Description                                                             |           
|-----------------|---------|-------------------------------------------------------------------------|
| host            | text    | IP Address of your device                                               |
| port            | integer | Modbus Port of your E3DC device Modbus Settings.. Default is 502 |
| deviceid        | integer | Modbus Port of your E3DC device Modbus Settings. Default is 1           |

### E3DC Home Power Plant 

Select as Bridge your previously created Modbus TCP Slave.
No additional configuration parameters needed.

### E3DC Wallbox

Select as Bridge your previously created E3DC Home Power Plant.

| Parameter       | Type    | Description                                                                 |           
|-----------------|---------|-----------------------------------------------------------------------------|
| wallboxId       | integer | The E3DC device can handle up to 8 Wallboxes so select a value from 0 to 7  |

## Channels

The E3DC device offers quite an amount of channels. For clustering 4 Channel Groups are used: 

### Channel Group _Information Block_

| Channel Label         | Channel Group ID | Channel ID          | Type   | Description                                       |
|-----------------------|------------------|---------------------|--------|---------------------------------------------------|
| Modbus-ID             | info             | modbus-id           | String | Modbus ID / Magic Byte of E3DC                    |
| Modbus Firmware       | info             | modbus-firmware     | String | Version of Modbus Firmware                        |
| Supported Registers   | info             | supported-registers | Number | Number of registers supported by Modbus           |
| Manufacturer Name     | info             | manufacturer-name   | String | Name of the Device Manufacturer                   |
| E3DC Model Name       | info             | model-name          | String | Name of the E3DC Model                            |
| E3DC Firmware Release | info             | firmware-release    | String | Firmware installed on this particular E3DC Model  |
| E3DC Serial Number    | info             | serial-number       | String | Serial Number of this particular E3DC Model       |


### Channel Group _Power Block_

| Channel Label                 |  Channel Group ID | Channel ID                   | Type             | Description                  |
|-------------------------------|-------------------|------------------------------|------------------|------------------------------|
| PV Output                     | power             | pv-power-supply              |  Number:Power    | Photovoltaic Power Production    |
| Battery Discharge             | power             | battery-power-supply         |  Number:Power    | Battery discharges and provides Power    |
| Battery Charge                | power             | battery-power-consumption    |  Number:Power    | Battery charges and consumes Power    |
| Household Consumption         | power             | household-power-consumption  |  Number:Power    | Household consuming Power    |
| Grid Power Consumption        | power             | grid-power-consumption       |  Number:Power    | Grid Power is needed in order to satisfy your overall Power consumption    |
| Grid Power Supply             | power             | grid-power-supply            |  Number:Power    | More Photovoltaic Power is produced than needed. Additional Power is provided towards the Grid    |
| External Power Supply         | power             | external-power-supply        |  Number:Power    | Power produced by an external device which is attached to your E3DC device    |
| Wallbox Power Consumption     | power             | wallbox-power-consumption    |  Number:Power    | Power consumption of attached Wallboxes    |
| Wallbox PV Power Consumption  | power             | wallbox-pv-power-consumption |  Number:Power    | Photovoltaic Power consumption (PV plus Battery) of attached Wallboxes    |
| Autarky                       | power             | autarky-channel              |  Number:Percent  | Your current Autarky Level    |
| Self Consumption              | power             | self-consumption             |  Number:Percent  | Your current Photovoltaic Self Consumption Level    |
| Battery State Of Charge       | power             | battery-soc                  |  Number:Percent  | Charge Level of your attached Battery    |


### Channel Group _String Details Block_

| Channel Label         | Channel Group ID | Channel ID         | Type            | Description                  |
|-----------------------|------------------|--------------------|-----------------|------------------------------|
| String 1 Potential    | strings          | string1-dc-voltage |  Number:Volt    | Volt on String 1           |
| String 2 Potential    | strings          | string2-dc-voltage |  Number:Volt    | Volt on String 2           |
| String 3 Potential    | strings          | string3-dc-voltage |  Number:Volt    | Volt on String 3           |
| String 1 Current      | strings          | string1-dc-current |  Number:Ampere  | Ampere on String 1       |
| String 2 Current      | strings          | string2-dc-current |  Number:Ampere  | Ampere on String 2       |
| String 3 Current      | strings          | string3-dc-current |  Number:Ampere  | Ampere on String 3       |
| String 1 Power        | strings          | string1-dc-output  |  Number:Power   | Watt produced by String 1 |
| String 2 Power        | strings          | string2-dc-output  |  Number:Power   | Watt produced by String 2 |
| String 3 Power        | strings          | string3-dc-output  |  Number:Power   | Watt produced by String 3 |


### Channel _EMS Block_

| Channel Label                               | Channel Group ID | Channel ID                | Type           | Description                  |
|---------------------------------------------|------------------|---------------------------|----------------|------------------------------|
| Emergency Power Status                      | emergency        | emergency-power-status    |  String  | Indicates if Emergency Power Supply is possible or not, active or inactive |
| Battery Loading Locked                      | emergency        | battery-loading-lock      |  Switch  | Indicates if Battery Loading is locked           |
| Battery Unloading Locked                    | emergency        | battery-unloading-lock    |  Switch  | Indicates if Battery Unloading is locked |
| Emergency Power Possible                    | emergency        | emergency-power-possible  |  Switch  | Indicates if Emergency Power Supply is possible          |
| Loading Based On Weather Prediction Active  | emergency        | weather-predicted-loading |  Switch  | Indicates if Weather Predicted Battery Loading is activated |
| Regulation Status Of Max Grid Power Supply  | emergency        | regulation-status         |  Switch  | Indicates if Grid Power Supply is regulated or not |
| Loading Lock time Active                    | emergency        | loading-lock-time         |  Switch  | Indicates if Loading Lock Times are set or not |
| Unloading Lock time Active                  | emergency        | unloading-lock-time       |  Switch  | Indicates if Unloading Lock Times are set or not |

### E3DC Wallbox Channels

Some of the Wallbox Settings can be changed. See the Access column if the actual value is Read/Write (RW) or Read Only (RO)

| Channel Label            | Channel ID          | Type    | Access | Description                  |
|--------------------------|---------------------|---------|--------|------------------------------|
| Wallbox Available        | wb-available        |  Switch | RO     | Indicates if the Wallbox is attached. Check your Wallbox ID in offline case  |
| Sun Mode                 | wb-sunmode          |  Switch | RW     | Activate / Deactivate Sun Mode. Off case takes Grid Power to ensure highest possible charging.   |
| Wallbox Charging Aborted | wb-charging-aborted |  Switch | RW     | Indicates if Wallbox charging is aborted  |
| Wallbox Charging         | wb-charging         |  Switch | RO     | Indicates your Wallbox is charging   |
| Jack Locked              | wb-jack-locked      |  Switch | RO     | Indicates your Jack is locked   |
| Jack Plugged             | wb-jack-plugged     |  Switch | RO     | Indicates your Jack is plugged    |
| Schuko Socket On         | wb-schuko-on        |  Switch | RW     | If your Wallbox has an additional Schuko Socket it provides state ON or OFF    |
| Schuko Socket Plugged    | wb-schuko-plugged   |  Switch | RO     | If your Wallbox has an additional Schuko Socket it provides plugged state ON or OFF    |
| Schuko Socket Locked     | wb-schuko-locked    |  Switch | RO     | If your Wallbox has an additional Schuko Socket it provides locked state ON or OFF |
| Schuko 16A Relay On      | wb-schuko-relay-16a |  Switch | RO     | Indicates if Schuko 16A Relay is ON     |
| 16A Relay On             | wb-relay-16a        |  Switch | RO     | Indicates if 16A Relay is ON     |
| 32A Relay On             | wb-relay-32a        |  Switch | RO     | Indicates if 32A Relay is ON    |
| 1-Phase Charging         | 1-Phase Active      |  Switch | RW     | Indicates if 1-phase charging is activated. If OFF 3-phase charging is activated    |

## Full Example

Following example provides the full configuration. If you enter the correct Connection Data, IP Address, Device ID and Port number in the thing configuration you should be fine.

### Things

```
Bridge modbus:tcp:device "E3DC Modbus TCP" [ host="192.168.178.56", port=502, id=1 ] {
        Bridge e3dc powerplant "E3DC Power Plant" [ ] {
         Thing e3dc-wallbox wallbox                     "E3DC Wallbox"                              [ wallboxId=0]
    }
}
```

### Items

```
String    E3DC_Firmware                 "E3DC Modbus ID"            (e3dc)      { channel="modbus:e3dc:device:powerplant:info#firmware-release" }
String    E3DC_SerialNumber             "E3DC Modbus ID"            (e3dc)      { channel="modbus:e3dc:device:powerplant:info#serial-number" }

Number    E3DC_PVPower                  "E3DC PV Power"             (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:power#pv-power-supply" }
Number    E3DC_BatteryDischarge         "E3DC Battery Discharge"    (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:power#battery-power-supply" }
Number    E3DC_BatteryCharge            "E3DC Battery Charge"       (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:power#battery-power-consumption" }
Number    E3DC_Household                "E3DC Household Consumption"    (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:power#household-power-consumption" }
Number    E3DC_GridConsumption          "E3DC Grid Consumption"     (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:power#grid-power-consumption" }
Number    E3DC_GridSupply               "E3DC Grid Supply "         (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:power#grid-power-supply" }
Number    E3DC_ExternalSupply           "E3DC External Supply"      (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:power#external-power-supply" }
Number    E3DC_WallboxConsumption       "E3DC Wallbox Consumption"  (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:power#wallbox-power-consumption" }
Number    E3DC_WallboxPVConsumption     "E3DC Wallbox PV Consumption"   (e3dc)  { channel="modbus:e3dc:device:powerplant:power#wallbox-pv-power-consumption" }
Number    E3DC_AutarkyLevel             "E3DC Autarky Level"        (e3dc)  { channel="modbus:e3dc:device:powerplant:power#autarky" }
Number    E3DC_SelfConsumptionLevel     "E3DC Self Consumption Level"   (e3dc)  { channel="modbus:e3dc:device:powerplant:power#self-consumption" }
Number    E3DC_BatterySOC               "E3DC Battery SOC"          (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:power#battery-soc" }

Switch    E3DC_WB_Available             "E3DC WB available"     (e3dc)  { channel="modbus:e3dc-wallbox:device:powerplant:wallbox:wb-available" }
Switch    E3DC_WB_Sunmode               "E3DC WB Sunmode"       (e3dc)  { channel="modbus:e3dc-wallbox:device:powerplant:wallbox:wb-sunmode" }
Switch    E3DC_WB_ChargingAborted       "E3DC WB Charging Aborted"  (e3dc)  { channel="modbus:e3dc-wallbox:device:powerplant:wallbox:wb-charging-aborted" }
Switch    E3DC_WB_Charging              "E3DC WB Charging"      (e3dc)  { channel="modbus:e3dc-wallbox:device:powerplant:wallbox:wb-charging" }
Switch    E3DC_WB_JackLocked            "E3DC WB Jack Locked"   (e3dc)  { channel="modbus:e3dc-wallbox:device:powerplant:wallbox:wb-jack-locked" }
Switch    E3DC_WB_JackPlugged           "E3DC WB Jack Plugged"  (e3dc)  { channel="modbus:e3dc-wallbox:device:powerplant:wallbox:wb-jack-plugged" }
Switch    E3DC_WB_SchukoOn              "E3DC WB Schuko On"     (e3dc)  { channel="modbus:e3dc-wallbox:device:powerplant:wallbox:wb-schuko-on" }
Switch    E3DC_WB_SchukoPlugged         "E3DC WB Schuko Plugged"    (e3dc)  { channel="modbus:e3dc-wallbox:device:powerplant:wallbox:wb-schuko-plugged" }
Switch    E3DC_WB_SchukoLocked          "E3DC WB Schuko Locked" (e3dc)  { channel="modbus:e3dc-wallbox:device:powerplant:wallbox:wb-schuko-locked" }
Switch    E3DC_WB_Schuko_Relay16A       "E3DC WB Schuko 16A Relay"  (e3dc) { channel="modbus:e3dc-wallbox:device:powerplant:wallbox:wb-schuko-relay-16a" }
Switch    E3DC_WB_Relay16A              "E3DC WB 16A Relay"      (e3dc) { channel="modbus:e3dc-wallbox:device:powerplant:wallbox:wb-relay-16a" }
Switch    E3DC_WB_Relay32A              "E3DC WB 32A Relay"      (e3dc) { channel="modbus:e3dc-wallbox:device:powerplant:wallbox:wb-relay-32a" }
Switch    E3DC_WB_1PhaseLoading         "E3DC WB 1-Phase Loading"   (e3dc)  { channel="modbus:e3dc-wallbox:device:powerplant:wallbox:wb-1phase" }

Number    E3DC_String1V                 "E3DC String 1 Volt"    (e3dc)  { channel="modbus:e3dc:device:powerplant:strings#string1-dc-voltage" }
Number    E3DC_String2V                 "E3DC String 2 Volt"    (e3dc)  { channel="modbus:e3dc:device:powerplant:strings#string2-dc-voltage" }
Number    E3DC_String3V                 "E3DC String 3 Volt"    (e3dc)  { channel="modbus:e3dc:device:powerplant:strings#string3-dc-voltage" }
Number    E3DC_String1A                 "E3DC String 1 Ampere"  (e3dc)  { channel="modbus:e3dc:device:powerplant:strings#string1-dc-current" }
Number    E3DC_String2A                 "E3DC String 2 Ampere"  (e3dc)  { channel="modbus:e3dc:device:powerplant:strings#string2-dc-current" }
Number    E3DC_String3A                 "E3DC String 3 Ampere"  (e3dc)  { channel="modbus:e3dc:device:powerplant:strings#string3-dc-current" }
Number    E3DC_String1W                 "E3DC String 1 Watt"    (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:strings#string1-dc-output" }
Number    E3DC_String2W                 "E3DC String 2 Watt"    (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:strings#string2-dc-output" }
Number    E3DC_String3W                 "E3DC String 3 Watt"    (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:strings#string3-dc-output" }

String    E3DC_EMS_Status                       "E3DC EMS Status"                   (e3dc)  { channel="modbus:e3dc:device:powerplant:emergency#emergency-power-status" }
Switch    E3DC_EMS_BatteryLoadingLock           "E3DC EMS Battery Loading Lock"     (e3dc)  { channel="modbus:e3dc:device:powerplant:emergency#battery-loading-lock" }
Switch    E3DC_EMS_BatteryUnloadingLock         "E3DC EMS Battery Unloading Lock"   (e3dc)  { channel="modbus:e3dc:device:powerplant:emergency#battery-unloading-lock" }
Switch    E3DC_EMS_EmergencyPowerPossible       "E3DC EMS Emergency Power possible" (e3dc)  { channel="modbus:e3dc:device:powerplant:emergency#emergency-power-possible" }
Switch    E3DC_EMS_WeatherPredictedLoading      "E3DC EMS Weather Predicted Loading" (e3dc)  { channel="modbus:e3dc:device:powerplant:emergency#weather-predicted-loading" }
Switch    E3DC_EMS_RegulationStatus             "E3DC EMS Regulation Status"        (e3dc)  { channel="modbus:e3dc:device:powerplant:emergency#regulation-status" }
Switch    E3DC_EMS_LoadingLockTime              "E3DC EMS Loading Lock Time"        (e3dc)  { channel="modbus:e3dc:device:powerplant:emergency#loading-lock-time" }
Switch    E3DC_EMS_UnloadingLockTime            "E3DC EMS Unloading Lock TIme"      (e3dc)  { channel="modbus:e3dc:device:powerplant:emergency#unloading-lock-time" }
```

### Sitemap

```
sitemap E3DC label="E3DC Binding Sitemap" {
  Frame label="Info" {
    Text    item=E3DC_ModbusId                  label="Modbus-ID [%s]"
    Text    item=E3DC_ModbusFirmware            label="Modbus Firmware [%s]"
    Text    item=E3DC_SupportedRegisters        label="Registers [%s]"
    Text    item=E3DC_Manufacturer              label="Manufacturer [%s]"
    Text    item=E3DC_ModelName                 label="Model Name [%s]"
    Text    item=E3DC_Firmware                  label="Firmware [%s]"
    Text    item=E3DC_SerialNumber              label="Serial Number[%s]"
  }

    Frame label="Power Producer" {
      Text    item=E3DC_PVPower                 label="PV Power[%s]"
      Text    item=E3DC_BatteryDischarge        label="Battery Discharge [%s]"
      Text    item=E3DC_GridSupply              label="Power from Grid [%s]"
      Text    item=E3DC_ExternalSupply          label="External Supply [%s]"
    }
    Frame label="Power Consumer" {
      Text    item=E3DC_Household               label="Household [%s]"
      Text    item=E3DC_BatteryCharge           label="Battery Charge [%s]"
      Text    item=E3DC_GridConsumption         label="Power to Grid [%s]"
      Text    item=E3DC_WallboxConsumption      label="Wallbox [%s]"
      Text    item=E3DC_WallboxPVConsumption    label="Wallbox PV [%s]"
    }
    Frame label="Power Measures" {
      Text    item=E3DC_AutarkyLevel            label="Autarky [%s %%]"
      Text    item=E3DC_SelfConsumptionLevel    label="Self Consumption [%s %%]"
      Text    item=E3DC_BatterySOC              label="SOC [%s %%]"
    }

  Frame label="Wallbox" {
    Switch    item=E3DC_WB_Available            label="Available [%s]"
    Switch    item=E3DC_WB_Sunmode              label="Sunmode [%s]"
    Switch    item=E3DC_WB_ChargingAborted      label="Charging Aborted[%s]"
    Switch    item=E3DC_WB_Charging             label="Charging [%s]"
    Switch    item=E3DC_WB_JackLocked           label="Jack locked [%s]"
    Switch    item=E3DC_WB_JackPlugged          label="Jack plugged [%s]"
    Switch    item=E3DC_WB_SchukoOn             label="Schuko On [%s]"
    Switch    item=E3DC_WB_SchukoPlugged        label="Schuko plugged [%s]"
    Switch    item=E3DC_WB_SchukoLocked         label="Schuko locked [%s]"
    Switch    item=E3DC_WB_Schuko_Relay16A      label="Schuko Relay 16A [%s]"
    Switch    item=E3DC_WB_Relay16A             label="Relay 16A [%s]"
    Switch    item=E3DC_WB_Relay32A             label="Relay 32A [%s]"
    Switch    item=E3DC_WB_1PhaseLoading        label="1-Phase loading [%s]"
  }

    Frame label="String 1 Details" {
      Text    item=E3DC_String1V                label="Volt [%s]"
      Text    item=E3DC_String1A                label="Ampere [%s]"
      Text    item=E3DC_String1W                label="Watt [%s]"
    }
    Frame label="String 2 Details" {
      Text    item=E3DC_String2V                label="Volt [%s]"
      Text    item=E3DC_String2A                label="Ampere [%s]"
      Text    item=E3DC_String2W                label="Watt [%s]"
    }
    Frame label="String 3 Details" {
      Text    item=E3DC_String3V                label="Volt [%s]"
      Text    item=E3DC_String3A                label="Ampere [%s]"
      Text    item=E3DC_String3W                label="Watt [%s]"
    }

  Frame label="EMS" {
    Text      item=E3DC_EMS_Status                  label="Status [%s]"
    Switch    item=E3DC_EMS_BatteryLoadingLock      label="Batter Loading Lock [%s]"
    Switch    item=E3DC_EMS_BatteryUnloadingLock    label="Batter Unloading Lock [%s]"
    Switch    item=E3DC_EMS_EmergencyPowerPossible  label="Emergency Power Possible [%s]"
    Switch    item=E3DC_EMS_WeatherPredictedLoading label="Weather Predicted Loading [%s]"
    Switch    item=E3DC_EMS_RegulationStatus        label="Regulation [%s]"
    Switch    item=E3DC_EMS_LoadingLockTime         label="Loading Lock Times [%s]"
    Switch    item=E3DC_EMS_UnloadingLockTime       label="Unloading Lock Times [%s]"
  }
}
```


## Going further

Setup and configured everything the right way? Congratulations, you've now the actual E3DC values on your table. Don't stop and go ahead!

### Persistence

You can see in the example item configuration, that I added some items to the "persist". Feel free to choose your own group name but this opens the possibility 
to store the items in a database. See following *.persist file configuration how this can be established.

```
Strategies {
    everyMinute : "0 * * * * ?"
    everyHour : "0 0 * * * ?"
    everyDay  : "0 0 0 * * ?"
    default = everyChange
}

Items {
    // persist items on every change and every minute - used for E3DC
    persist : strategy = everyChange, everyMinute
}
```

### Visualization 

After the timeline is available in your database you can continue with Visualization. I like the Grafana approach and I used the
[InfluxDB & Grafana Tutorial](https://community.openhab.org/t/influxdb-grafana-persistence-and-graphing/13761)
from the Community to set this up.
I prepared my machine and I'm quite pleased with the results.
<img style="float: right;" src="doc/GrafanaPV.png">
In the above picture there are two graphs

* The top one shows the Photovoltaic Production of my 2 attached Strings. You can clearly see when the sky wasn't bright the production goes down
* The bottom graph show the producers & consumers. 
    * Battery in blue charging during the day, discharging at night
    * Household consumption in green 
    * Wallbox consumption in orange
    * Grid consumption / supply in yellow

### Cross Connections 

With the above setup you have now a great visualization and overview regarding your electric production and consumption. Now use the Power of openHAB and cross
connect your data. For example you can use the 
[OpenweatherMap API Binding](https://www.openhab.org/addons/bindings/openweathermap/)
the cloudiness in Percent. With a modified *.persist file I store the cloudiness forecast also in the database

```
Strategies {
    everyMinute : "0 * * * * ?"
    everyHour : "0 0 * * * ?"
    everyDay  : "0 0 0 * * ?"
    default = everyChange
}

Items {
    // persist items on every change and every minute - used for E3DC
    persist : strategy = everyChange, everyMinute
    LocalWeatherAndForecast_Current_BewLkung : strategy = everyChange,everyHour
}
```

Having these values in the timeline you're able to cross check how the forecast influences the Photovoltaic Production. 

<img style="float: right;" src="doc/GrafanaCloudiness.png">

I personally would like to have
more steering control of the E3DC to react on such forecast e.g. "stop charging the car if it gets too cloudy"
