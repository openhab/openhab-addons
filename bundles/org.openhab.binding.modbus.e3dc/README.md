# E3DC

Integrates the Home Power Plants from E3/DC GmbH into openHAB.
See [E3DC Website](https://www.e3dc.com/) to find more informations about the device.
The Power Plant handles all your Electrical Energy Resources like Photovoltaic Producers, Battery Storage, Wallbox Power Supply, Household Consumption and even more.  
E3DC devices are integrated into the Modbus Binding.

See chapter [Thing Configuration](#thing-configuration) how to set them up or check the [full example Things](#things) for manual setup.

## Supported Things

First you need a Bridge which establishes the basic connection towards your E3DC device

| Name                  | Thing Type ID | Description                                                                                          |
|-----------------------|---------------|------------------------------------------------------------------------------------------------------|
| E3DC Home Power Plant | e3dc          | Provides Power values, String Details, Emergency Power Status and general Information of your E3DC Home Power Plant    |
| E3DC Wallbox          | e3dc-wallbox  | Provides your Wallbox Settings. Switches like "Sunmode" or "1-Phase Charging" can be changed     |

## Discovery

There's no discovery.
Modbus registers are available for all devices.

## Thing Configuration

The needed Things can be found in the **Modbus Binding** and have to be added manually without Discovery

<img align="right" src="./doc/E3DC_Modbus_Settings.png"/>

1. Create _Modbus TCP Bridge_ with matching Settings of your E3DC Device

- IP Address
- Device ID
- Port ID

1. Create _E3DC Home Power Plant_ and attach it to the previous installed _Modbus TCP Bridge_. Configuration requires an approriate Data Refresh Interval with more than 1000 Milliseconds

1. If you have a Wallbox attached add _E3DC Wallbox_ Thing with your previous installed _E3DC Home Power Plant_ as Bridge. Configuration requires a Wallbox ID between 0 and 7.

Check the [full example Things](#things) for manual setup.

### Modbus TCP Slave

| Parameter       | Type    | Description                                                             |
|-----------------|---------|-------------------------------------------------------------------------|
| host            | text    | IP Address of your device                                               |
| port            | integer | TCP Port of your E3DC device Modbus Settings.. Default is 502 |
| deviceid        | integer | Modbus ID of your E3DC device Modbus Settings. Default is 1           |

### E3DC Home Power Plant

Select as Bridge your previously created Modbus TCP Slave.
| Parameter       | Type    | Description                                                             |
|-----------------|---------|-------------------------------------------------------------------------|
| refresh         | integer | Refresh Rate of E3DC values in Milliseconds                             |

### E3DC Wallbox

Select as Bridge your previously created E3DC Home Power Plant.

| Parameter       | Type    | Description                                                                 |
|-----------------|---------|-----------------------------------------------------------------------------|
| wallboxId       | integer | E3DC supports up to 8 Wallboxes - select a value from 0 to 7                |

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
| E3DC Firmware Release | info             | firmware-release    | String | Firmware installed on this particular E3DC Device |
| E3DC Serial Number    | info             | serial-number       | String | Serial Number of this particular E3DC Device      |

### Channel Group _Power Block_

| Channel Label                 |  Channel Group ID | Channel ID                   | Type                   | Description                                                                            |
|-------------------------------|-------------------|------------------------------|------------------------|----------------------------------------------------------------------------------------|
| PV Output                     | power             | pv-power-supply              |  Number:Power          | Photovoltaic Power Production                                                          |
| Battery Discharge             | power             | battery-power-supply         |  Number:Power          | Battery discharges and provides Power                                                  |
| Battery Charge                | power             | battery-power-consumption    |  Number:Power          | Battery charges and consumes Power                                                     |
| Household Consumption         | power             | household-power-consumption  |  Number:Power          | Household consuming Power                                                              |
| Grid Power Consumption        | power             | grid-power-consumption       |  Number:Power          | More Photovoltaic Power is produced than needed. Additional Power is consumed by Grid  |
| Grid Power Supply             | power             | grid-power-supply            |  Number:Power          | Grid Power is needed in order to satisfy your overall Power consumption                |
| External Power Supply         | power             | external-power-supply        |  Number:Power          | Power produced by an external device which is attached to your E3DC device             |
| Wallbox Power Consumption     | power             | wallbox-power-consumption    |  Number:Power          | Power consumption of attached Wallboxes                                                |
| Wallbox PV Power Consumption  | power             | wallbox-pv-power-consumption |  Number:Power          | Photovoltaic Power consumption (PV plus Battery) of attached Wallboxes                 |
| Autarky                       | power             | autarky-channel              |  Number:Dimensionless  | Your current Autarky Level in Percent                                                  |
| Self Consumption              | power             | self-consumption             |  Number:Dimensionless  | Your current Photovoltaic Self Consumption Level in Percent                            |
| Battery State Of Charge       | power             | battery-soc                  |  Number:Dimensionless  | Charge Level of your attached Battery in Percent                                       |

### Channel Group _String Details Block_

| Channel Label         | Channel Group ID | Channel ID         | Type                      | Description                |
|-----------------------|------------------|--------------------|---------------------------|----------------------------|
| String 1 Potential    | strings          | string1-dc-voltage |  Number:ElectricPotential | Voltage on String 1        |
| String 2 Potential    | strings          | string2-dc-voltage |  Number:ElectricPotential | Voltage on String 2        |
| String 3 Potential    | strings          | string3-dc-voltage |  Number:ElectricPotential | Voltage on String 3        |
| String 1 Current      | strings          | string1-dc-current |  Number:ElectricCurrent   | Current on String 1        |
| String 2 Current      | strings          | string2-dc-current |  Number:ElectricCurrent   | Current on String 2        |
| String 3 Current      | strings          | string3-dc-current |  Number:ElectricCurrent   | Current on String 3        |
| String 1 Power        | strings          | string1-dc-output  |  Number:Power             | Power produced by String 1 |
| String 2 Power        | strings          | string2-dc-output  |  Number:Power             | Power produced by String 2 |
| String 3 Power        | strings          | string3-dc-output  |  Number:Power             | Power produced by String 3 |

### Channel _EMS Block_

| Channel Label                               | Channel Group ID | Channel ID                 | Type           | Description                  |
|---------------------------------------------|------------------|----------------------------|----------------|------------------------------|
| Emergency Power Status                      | emergency        | emergency-power-status     |  String  | Possible values: EP not supported, EP active, EP not active, EP not available, EP Switch in wrong position, EP Status unknown |
| Battery Charging Locked                     | emergency        | battery-charging-lock      |  Switch  | Battery charging is locked          |
| Battery Discharging Locked                  | emergency        | battery-discharging-lock   |  Switch  | Battery discharging is locked |
| Emergency Power Possible                    | emergency        | emergency-power-possible   |  Switch  | Emergency Power Supply is possible          |
| Weather Predicted Battery Charging          | emergency        | weather-predicted-charging |  Switch  | Weather Predicted Battery Charging is activated |
| Regulation Status Of Max Grid Power Supply  | emergency        | regulation-status          |  Switch  | Grid Power Supply is currently regulated |
| Charge Lock time Active                     | emergency        | charge-lock-time           |  Switch  | Charge Lock Time is currently active |
| Discharge Lock time Active                  | emergency        | discharge-lock-time        |  Switch  | Discharge Lock Time is currently active |

### E3DC Wallbox Channels

Some of the Wallbox Settings can be changed. See the Access column if the actual value is Read/Write (RW) or Read Only (RO)

| Channel Label            | Channel ID          | Type    | Access | Description                  |
|--------------------------|---------------------|---------|--------|------------------------------|
| Wallbox Available        | wb-available        |  Switch | RO     | Indicates if the Wallbox is attached. Check your Wallbox ID in offline case  |
| Sun Mode                 | wb-sunmode          |  Switch | RW     | Activate / Deactivate Sun Mode. Off case takes Grid Power to ensure highest possible charging.   |
| Wallbox Charging Aborted | wb-charging-aborted |  Switch | RW     | Wallbox charging is aborted  |
| Wallbox Charging         | wb-charging         |  Switch | RO     | Wallbox is charging   |
| Jack Locked              | wb-jack-locked      |  Switch | RO     | Jack is locked   |
| Jack Plugged             | wb-jack-plugged     |  Switch | RO     | Jack is plugged    |
| Schuko Socket On         | wb-schuko-on        |  Switch | RW     | If your Wallbox has an additional Schuko Socket it provides state ON or OFF    |
| Schuko Socket Plugged    | wb-schuko-plugged   |  Switch | RO     | If your Wallbox has an additional Schuko Socket it provides plugged state ON or OFF    |
| Schuko Socket Locked     | wb-schuko-locked    |  Switch | RO     | If your Wallbox has an additional Schuko Socket it provides locked state ON or OFF |
| Schuko 16A Relay On      | wb-schuko-relay-16a |  Switch | RO     | Schuko 16A Relay is ON     |
| 16A Relay On             | wb-relay-16a        |  Switch | RO     | Wallbox 16A Relay is ON     |
| 32A Relay On             | wb-relay-32a        |  Switch | RO     | Wallbox 32A Relay is ON    |
| 1-Phase Charging         | 1-Phase Active      |  Switch | RW     | 1-phase charging is activated. If OFF 3-phase charging is activated    |

## Full Example

Following example provides the full configuration.
If you enter the correct Connection Data, IP Address, Device ID and Port number in the thing configuration you should be fine.

### Things

```java
Bridge modbus:tcp:device "E3DC Modbus TCP" [ host="192.168.178.56", port=502, id=1 ] {
 Bridge e3dc powerplant "E3DC Power Plant" [ refresh=2500 ] {
      Thing e3dc-wallbox wallbox0  "E3DC Wallbox"    [ wallboxId=0]
    }
}
```

### Items

```java
String    E3DC_ModbusId                 "E3DC Modbus ID"            (e3dc)      { channel="modbus:e3dc:device:powerplant:info#modbus-id" }
String    E3DC_ModbusFirmware           "E3DC Modbus Firmware"      (e3dc)      { channel="modbus:e3dc:device:powerplant:info#modbus-firmware" }
Number    E3DC_SupportedRegisters       "E3DC Supported Registers"  (e3dc)      { channel="modbus:e3dc:device:powerplant:info#supported-registers" }
String    E3DC_Manufacturer             "E3DC Manufacturer"         (e3dc)      { channel="modbus:e3dc:device:powerplant:info#manufacturer-name" }
String    E3DC_ModelName                "E3DC Model"                (e3dc)      { channel="modbus:e3dc:device:powerplant:info#model-name" }
String    E3DC_Firmware                 "E3DC Modbus ID"            (e3dc)      { channel="modbus:e3dc:device:powerplant:info#firmware-release" }
String    E3DC_SerialNumber             "E3DC Modbus ID"            (e3dc)      { channel="modbus:e3dc:device:powerplant:info#serial-number" }

Number:Power    E3DC_PVPower                  "E3DC PV Power"              (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:power#pv-power-supply" }
Number:Power    E3DC_BatteryDischarge         "E3DC Battery Discharge"     (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:power#battery-power-supply" }
Number:Power    E3DC_BatteryCharge            "E3DC Battery Charge"        (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:power#battery-power-consumption" }
Number:Power    E3DC_Household                "E3DC Household Consumption" (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:power#household-power-consumption" }
Number:Power    E3DC_GridConsumption          "E3DC Power to Grid"         (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:power#grid-power-consumption" }
Number:Power    E3DC_GridSupply               "E3DC Power from Grid"       (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:power#grid-power-supply" }
Number:Power    E3DC_ExternalSupply           "E3DC External Supply"       (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:power#external-power-supply" }
Number:Power    E3DC_WallboxConsumption       "E3DC Wallbox Consumption"   (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:power#wallbox-power-consumption" }
Number:Power    E3DC_WallboxPVConsumption     "E3DC Wallbox PV Consumption"         (e3dc)  { channel="modbus:e3dc:device:powerplant:power#wallbox-pv-power-consumption" }
Number:Dimensionless    E3DC_AutarkyLevel             "E3DC Autarky Level"          (e3dc)  { channel="modbus:e3dc:device:powerplant:power#autarky" }
Number:Dimensionless    E3DC_SelfConsumptionLevel     "E3DC Self Consumption Level" (e3dc)  { channel="modbus:e3dc:device:powerplant:power#self-consumption" }
Number:Dimensionless    E3DC_BatterySOC               "E3DC Battery SOC"            (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:power#battery-soc" }

Switch    E3DC_WB_Available             "E3DC WB available"          (e3dc)  { channel="modbus:e3dc-wallbox:device:powerplant:wallbox0:wb-available" }
Switch    E3DC_WB_Sunmode               "E3DC WB Sunmode"            (e3dc)  { channel="modbus:e3dc-wallbox:device:powerplant:wallbox0:wb-sunmode" }
Switch    E3DC_WB_ChargingAborted       "E3DC WB Charging Aborted"   (e3dc)  { channel="modbus:e3dc-wallbox:device:powerplant:wallbox0:wb-charging-aborted" }
Switch    E3DC_WB_Charging              "E3DC WB Charging"           (e3dc)  { channel="modbus:e3dc-wallbox:device:powerplant:wallbox0:wb-charging" }
Switch    E3DC_WB_JackLocked            "E3DC WB Jack Locked"        (e3dc)  { channel="modbus:e3dc-wallbox:device:powerplant:wallbox0:wb-jack-locked" }
Switch    E3DC_WB_JackPlugged           "E3DC WB Jack Plugged"       (e3dc)  { channel="modbus:e3dc-wallbox:device:powerplant:wallbox0:wb-jack-plugged" }
Switch    E3DC_WB_SchukoOn              "E3DC WB Schuko On"          (e3dc)  { channel="modbus:e3dc-wallbox:device:powerplant:wallbox0:wb-schuko-on" }
Switch    E3DC_WB_SchukoPlugged         "E3DC WB Schuko Plugged"     (e3dc)  { channel="modbus:e3dc-wallbox:device:powerplant:wallbox0:wb-schuko-plugged" }
Switch    E3DC_WB_SchukoLocked          "E3DC WB Schuko Locked"      (e3dc)  { channel="modbus:e3dc-wallbox:device:powerplant:wallbox0:wb-schuko-locked" }
Switch    E3DC_WB_Schuko_Relay16A       "E3DC WB Schuko 16A Relay"   (e3dc) { channel="modbus:e3dc-wallbox:device:powerplant:wallbox0:wb-schuko-relay-16a" }
Switch    E3DC_WB_Relay16A              "E3DC WB 16A Relay"          (e3dc) { channel="modbus:e3dc-wallbox:device:powerplant:wallbox0:wb-relay-16a" }
Switch    E3DC_WB_Relay32A              "E3DC WB 32A Relay"          (e3dc) { channel="modbus:e3dc-wallbox:device:powerplant:wallbox0:wb-relay-32a" }
Switch    E3DC_WB_1PhaseCharging        "E3DC WB 1-Phase Charging"   (e3dc)  { channel="modbus:e3dc-wallbox:device:powerplant:wallbox0:wb-1phase" }

Number:ElectricPotential    E3DC_String1V                 "E3DC String 1 Volt"    (e3dc)  { channel="modbus:e3dc:device:powerplant:strings#string1-dc-voltage" }
Number:ElectricPotential    E3DC_String2V                 "E3DC String 2 Volt"    (e3dc)  { channel="modbus:e3dc:device:powerplant:strings#string2-dc-voltage" }
Number:ElectricPotential    E3DC_String3V                 "E3DC String 3 Volt"    (e3dc)  { channel="modbus:e3dc:device:powerplant:strings#string3-dc-voltage" }
Number:ElectricCurrent      E3DC_String1A                 "E3DC String 1 Ampere"  (e3dc)  { channel="modbus:e3dc:device:powerplant:strings#string1-dc-current" }
Number:ElectricCurrent      E3DC_String2A                 "E3DC String 2 Ampere"  (e3dc)  { channel="modbus:e3dc:device:powerplant:strings#string2-dc-current" }
Number:ElectricCurrent      E3DC_String3A                 "E3DC String 3 Ampere"  (e3dc)  { channel="modbus:e3dc:device:powerplant:strings#string3-dc-current" }
Number:Power                E3DC_String1W                 "E3DC String 1 Watt"    (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:strings#string1-dc-output" }
Number:Power                E3DC_String2W                 "E3DC String 2 Watt"    (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:strings#string2-dc-output" }
Number:Power                E3DC_String3W                 "E3DC String 3 Watt"    (e3dc,persist)  { channel="modbus:e3dc:device:powerplant:strings#string3-dc-output" }

String    E3DC_EMS_Status                       "E3DC EMS Status"                      (e3dc)  { channel="modbus:e3dc:device:powerplant:emergency#emergency-power-status" }
Switch    E3DC_EMS_BatteryChargingLock          "E3DC EMS Battery Charging Locked"     (e3dc)  { channel="modbus:e3dc:device:powerplant:emergency#battery-charging-lock" }
Switch    E3DC_EMS_BatteryDischargingLock       "E3DC EMS Battery Discharging Locked"  (e3dc)  { channel="modbus:e3dc:device:powerplant:emergency#battery-discharging-lock" }
Switch    E3DC_EMS_EmergencyPowerPossible       "E3DC EMS Emergency Power possible"    (e3dc)  { channel="modbus:e3dc:device:powerplant:emergency#emergency-power-possible" }
Switch    E3DC_EMS_WeatherPredictedCharging     "E3DC EMS Weather Predicted Charging"  (e3dc)  { channel="modbus:e3dc:device:powerplant:emergency#weather-predicted-charging" }
Switch    E3DC_EMS_RegulationStatus             "E3DC EMS Regulation Status"           (e3dc)  { channel="modbus:e3dc:device:powerplant:emergency#regulation-status" }
Switch    E3DC_EMS_ChargeLockTime               "E3DC EMS Charge Lock Time"            (e3dc)  { channel="modbus:e3dc:device:powerplant:emergency#charge-lock-time" }
Switch    E3DC_EMS_DischargeLockTime            "E3DC EMS Discharge Lock TIme"         (e3dc)  { channel="modbus:e3dc:device:powerplant:emergency#discharge-lock-time" }
```

### Sitemap

```perl
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
      Text    item=E3DC_PVPower                 label="PV Power [%d %unit%]"
      Text    item=E3DC_BatteryDischarge        label="Battery Discharge [%d %unit%]"
      Text    item=E3DC_GridSupply              label="Power from Grid [%d %unit%]"
      Text    item=E3DC_ExternalSupply          label="External Supply [%d %unit%]"
    }
    Frame label="Power Consumer" {
      Text    item=E3DC_Household               label="Household [%d %unit%]"
      Text    item=E3DC_BatteryCharge           label="Battery Charge [%d %unit%]"
      Text    item=E3DC_GridConsumption         label="Power to Grid [%d %unit%]"
      Text    item=E3DC_WallboxConsumption      label="Wallbox [%d %unit%]"
      Text    item=E3DC_WallboxPVConsumption    label="Wallbox PV [%d %unit%]"
    }
    Frame label="Power Measures" {
      Text    item=E3DC_AutarkyLevel            label="Autarky [%d %%]"
      Text    item=E3DC_SelfConsumptionLevel    label="Self Consumption [%d %%]"
      Text    item=E3DC_BatterySOC              label="SOC [%d %%]"
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
    Switch    item=E3DC_WB_1PhaseCharging       label="1-Phase charging [%s]"
  }
  
    Frame label="String 1 Details" {
      Text    item=E3DC_String1V                label="Volt [%d %unit%]"
      Text    item=E3DC_String1A                label="Ampere [%.2f %unit%]"
      Text    item=E3DC_String1W                label="Watt [%d %unit%]"
    }
    Frame label="String 2 Details" {
      Text    item=E3DC_String2V                label="Volt [%d %unit%]"
      Text    item=E3DC_String2A                label="Ampere [%.2f %unit%]"
      Text    item=E3DC_String2W                label="Watt [%d %unit%]"
    }
    Frame label="String 3 Details" {
      Text    item=E3DC_String3V                label="Volt [%d %unit%]"
      Text    item=E3DC_String3A                label="Ampere [%.2f %unit%]"
      Text    item=E3DC_String3W                label="Watt [%d %unit%]"
    }
  
  Frame label="EMS" {
    Text      item=E3DC_EMS_Status                      label="Status [%s]"
    Switch    item=E3DC_EMS_BatteryChargingLock         label="Battery Charging Lock [%s]"
    Switch    item=E3DC_EMS_BatteryDischargingLock      label="Battery Discharging Lock [%s]"
    Switch    item=E3DC_EMS_EmergencyPowerPossible      label="Emergency Power Possible [%s]"
    Switch    item=E3DC_EMS_WeatherPredictedCharging    label="Weather Predicted Charging [%s]"
    Switch    item=E3DC_EMS_RegulationStatus            label="Regulation [%s]"
    Switch    item=E3DC_EMS_ChargeLockTime              label="Charge Lock Times [%s]"
    Switch    item=E3DC_EMS_DischargeLockTime           label="Discharge Lock Times [%s]"
  }
}
```

## Going further

Setup and configured everything the right way? Congratulations, you've now the recent E3DC values on your table. Don't stop and go ahead!

### Persistence

You can see in the example item configuration, that I added some items to the "persist".
Feel free to choose your own group name but this opens the possibility to store the items in a database.
See following *.persist file configuration how this can be established.

```text
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

After the timeline is available in your database you can continue with Visualization.
I like the Grafana approach and I used the [InfluxDB & Grafana Tutorial](https://community.openhab.org/t/influxdb-grafana-persistence-and-graphing/13761)
from the Community to set this up.
I prepared my machine and I'm quite pleased with the results.

<img align="right" src="./doc/GrafanaPV.png"/>

In the above picture there are two graphs

- The top one shows the Photovoltaic Production of my 2 attached Strings. You can clearly see when the sky wasn't bright the production goes down
- The bottom graph show the producers & consumers.
  - Battery in blue charging during the day, discharging at night
  - Household consumption in green
  - Wallbox consumption in orange
  - Grid consumption / supply in yellow

### Cross Connections

With the above setup you have now a great visualization and overview regarding your electric production and consumption.
Now use the Power of openHAB and cross connect your data.
For example you can use the [OpenweatherMap API Binding](https://www.openhab.org/addons/bindings/openweathermap/)
the cloudiness in Percent.
With a modified *.persist file I store the cloudiness forecast also in the database

```text
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

<img align="right" src="./doc/GrafanaCloudiness.png"/>

I personally would like to have more steering control of the E3DC to react on such forecast e.g. "stop charging the car if it gets too cloudy"
