# Drayton Wiser Binding

This binding integrates the [Drayton Wiser Smart Heating System](https://wiser.draytoncontrols.co.uk/).
The integration happens through the HeatHub, which acts as an IP gateway to the ZigBee devices (thermostats and TRVs).

## Supported Things

The Drayton Wiser binding supports the following things:

| Bridge    | Label   | Description                                                                                          |
|-----------|---------|------------------------------------------------------------------------------------------------------|
| `heathub` | HeatHub | The network device in the controller that allows us to interact with the other devices in the system |

| Thing               | Label             | Description                                                                               |
|---------------------|-------------------|-------------------------------------------------------------------------------------------|
| `boiler-controller` | Boiler Controller | The _HeatHub_ attached to the boiler. This also acts as the hub device                    |
| `room`              | Room Name         | Virtual groups of _Room Thermostats_ and _TRVs_ that can have temperatures and humidities |
| `roomstat`          | Thermostat        | Wireless thermostats which monitor temperature and humidity, and call for heat            |
| `itrv`              | iTRV              | Wireless TRVs that monitor temperature, alter the radiator valve state and call for heat  |
| `hotwater`          | Hot Water         | Virtual thing to manage hot water states                                                  |
| `smart-plug`        | Smart Plug        | Wireless plug sockets which can be remotely switched                                      |

## Discovery

The HeatHub can be discovered automatically via mDNS, however the `secret` cannot be determined automatically.
Once the `secret` has been configured, all other devices can be discovered by triggering device discovery again.

## Thing Configuration

### HeatHub Configuration

Once discovered, the HeatHub `secret` needs to be configured.
There are a few ways to obtain this, assuming you have already configured the system using the Wiser App.

1. Temporarily install a packet sniffing tool on your mobile device. Every request made includes the `secret` in the header.
1. Enable setup mode on the HeatHub. Connect a machine temporarily to the `WiserHeat_XXXXX` network and browse to `http://192.168.8.1/secret` to obtain the `secret`.

The `refresh` interval defines in seconds, how often the binding will poll the controller for updates.

The `awaySetPoint` defines the temperature in degrees Celsius that will be sent to the heathub when away mode is activated.

## Channels

### Readonly Channels

#### Boiler Controller

| Channel                      | Item Type            | Description                                              |
|------------------------------|----------------------|----------------------------------------------------------|
| `heatingOverride`            | Switch               | State of the heating override button on the controller   |
| `heatChannel1Demand`         | Number:Dimensionless | Current demand level of heating channel 1                |
| `heatChannel1DemandState`    | Switch               | Is channel 1 calling the boiler for heat                 |
| `heatChannel2Demand`         | Number:Dimensionless | Current demand level of heating channel 2                |
| `heatChannel2DemandState`    | Switch               | Is channel 2 calling the boiler for heat                 |
| `currentSignalRSSI`          | Number:Power         | Relative Signal Strength Indicator                       |
| `currentWiserSignalStrength` | String               | Human readable signal strength                           |
| `currentSignalStrength`      | Number               | Signal strength value that maps to qualityofservice icon |

#### Hot Water

| Channel                  | Item Type    | Description                                              |
|--------------------------|--------------|----------------------------------------------------------|
| `hotWaterOverride`       | Switch       | State of the hot water override button on the controller |
| `hotWaterDemandState`    | Switch       | Is hot water calling the boiler for heat                 |
| `hotWaterBoosted`        | Switch       | Is hot water currently being boosted                     |
| `hotWaterBoostRemaining` | Number:Time  | How long until the boost deactivates in minutes          |

#### Room

| Channel              | Item Type            | Description                                                                  |
|----------------------|----------------------|------------------------------------------------------------------------------|
| `currentTemperature` | Number:Temperature   | Currently reported temperature                                               |
| `currentHumidity`    | Number:Dimensionless | Currently reported humidity (if there is a room stat configured in this room |
| `currentDemand`      | Number:Dimensionless | Current heat demand percentage of the room                                   |
| `heatRequest`        | Switch               | Is the room actively requesting heat from the controller                     |
| `roomBoosted`        | Switch               | Is the room currently being boosted                                          |
| `roomBoostRemaining` | Number:Time          | How long until the boost deactivates in minutes                              |
| `windowState`        | Contact              | Is the window open or closed?                                                |

#### Room Stat

| Channel                      | Item Type                | Description                                              |
|------------------------------|--------------------------|----------------------------------------------------------|
| `currentTemperature`         | Number:Temperature       | Currently reported temperature                           |
| `currentHumidity`            | Number:Dimensionless     | Currently reported humidity                              |
| `currentSetPoint`            | Number:Temperature       | Currently reported set point                             |
| `currentBatteryVoltage`      | Number:ElectricPotential | Currently reported battery voltage                       |
| `currentWiserBatteryLevel`   | String                   | Human readable battery level                             |
| `currentBatteryLevel`        | Number                   | Battery level in percent                                 |
| `currentSignalRSSI`          | Number:Power             | Relative Signal Strength Indicator                       |
| `currentSignalLQI`           | Number                   | Link Quality Indicator                                   |
| `currentWiserSignalStrength` | String                   | Human readable signal strength                           |
| `currentSignalStrength`      | Number                   | Signal strength value that maps to qualityofservice icon |
| `zigbeeConnected`            | Switch                   | Is the roomstat joined to network                        |

#### Smart TRV

| Channel                      | Item Type                | Description                                              |
|------------------------------|--------------------------|----------------------------------------------------------|
| `currentTemperature`         | Number:Temperature       | Currently reported temperature                           |
| `currentDemand`              | Number:Dimensionless     | Current heat demand percentage of the TRV                |
| `currentSetPoint`            | Number:Temperature       | Currently reported set point                             |
| `currentBatteryVoltage`      | Number:ElectricPotential | Currently reported battery voltage                       |
| `currentWiserBatteryLevel`   | String                   | Human readable battery level                             |
| `currentBatteryLevel`        | Number                   | Battery level in percent                                 |
| `currentSignalRSSI`          | Number:Power             | Relative Signal Strength Indicator                       |
| `currentSignalLQI`           | Number                   | Link Quality Indicator                                   |
| `currentWiserSignalStrength` | String                   | Human readable signal strength                           |
| `currentSignalStrength`      | Number                   | Signal strength value that maps to qualityofservice icon |
| `zigbeeConnected`            | Switch                   | Is the TRV joined to network                             |

#### Smart Plug

| Channel                  | Item Type     | Description                                |
|--------------------------|---------------|--------------------------------------------|
| `currentSignalRSSI`      | Number:Power  | Relative Signal Strength Indicator         |
| `currentSignalLQI`       | Number        | Link Quality Indicator                     |
| `zigbeeConnected`        | Switch        | Is the TRV joined to network               |
| `plugInstantaneousPower` | Number:Power  | Current Power being drawn through the plug |
| `plugEnergyDelivered`    | Number:Energy | Cumulative energy drawn through the plug   |

### Command Channels

#### Boiler Controller

| Channel            | Item Type | Description                   |
|--------------------|-----------|-------------------------------|
| `awayModeState`    | Switch    | Has away mode been enabled    |
| `ecoModeState`     | Switch    | Has eco mode been enabled     |
| `comfortModeState` | Switch    | Has comfort mode been enabled |

#### Hot Water

| Channel                 | Item Type | Description                                |
|-------------------------|-----------|--------------------------------------------|
| `manualModeState`       | Switch    | Has manual mode been enabled               |
| `hotWaterSetPoint`      | Switch    | The current hot water setpoint (on or off) |
| `hotWaterBoostDuration` | Number    | Period in hours to boost the hot water     |

#### Room

| Channel                | Item Type          | Description                                    |
|------------------------|--------------------|------------------------------------------------|
| `currentSetPoint`      | Number:Temperature | The current set point temperature for the room |
| `manualModeState`      | Switch             | Has manual mode been enabled                   |
| `roomBoostDuration`    | Number             | Period in hours to boost the room temperature  |
| `windowStateDetection` | Switch             | Detect whether windows are open                |

#### Room Stat

| Channel        | Item Type | Description                      |
|----------------|-----------|----------------------------------|
| `deviceLocked` | Switch    | Is the roomstat interface locked |

#### Smart TRV

| Channel        | Item Type | Description                 |
|----------------|-----------|-----------------------------|
| `deviceLocked` | Switch    | Are the TRV controls locked |

#### Smart Plug

| Channel           | Item Type | Description                                  |
|-------------------|-----------|----------------------------------------------|
| `plugOutputState` | Switch    | The current on/off state of the smart plug   |
| `plugAwayAction`  | Switch    | Should the plug switch off when in away mode |
| `manualModeState` | Switch    | Has manual mode been enabled                 |
| `deviceLocked`    | Switch    | Are the Smart Plug controls locked           |

#### Known Responses for Specific Channels

| Channel                      | Known responses                                                    |
|------------------------------|--------------------------------------------------------------------|
| `currentWiserSignalStrength` | `{ "VeryGood", "Good", "Medium", "Poor", "NoSignal" }`             |
| `currentWiserBatteryLevel`   | `{ "Full", "Normal", "TwoThirds", "OneThird", "Low", "Critical" }` |

## Full Example

### .things file

```java
Bridge draytonwiser:heathub:HeatHub [ networkAddress="192.168.1.X", refresh=60, secret="secret from hub", awaySetPoint=10 ] {
    boiler-controller controller     "Controller"
    room              livingroom     "Living Room"            [ name="Living Room" ]
    room              bathroom       "Bathroom"               [ name="Bathroom" ]
    room              bedroom        "Bedroom"                [ name="Bedroom" ]
    roomstat          livingroomstat "Living Room Thermostat" [ serialNumber="ABCDEF1234" ]
    itrv              livingroomtrv  "Living Room - TRV"      [ serialNumber="ABCDEF1235" ]
    hotwater          hotwater       "Hot Water"
    smart-plug        tvplug         "TV"                     [ serialNumber="ABCDEF1236" ]
}
```

The `name` corresponds to the room name configured in the Wiser App.
It is not case sensitive.
The `serialNumber` corresponds to the device serial number which can be found on a sticker inside the battery compartment of the Smart Valves/TRVs, and behind the wall mount of the Room Thermostats.

### .items file

```java
Switch Heating_Override      "Heating Override"    <fire>   (gHouse)    { channel="draytonwiser:boiler-controller:HeatHub:controller:heatingOverride" }
Number Heating_Demand      "Heating Demand [%.0f %%]"    <heating>   (gHouse)    { channel="draytonwiser:boiler-controller:HeatHub:controller:heatChannel1Demand" }
Switch Heating_Requesting_Heat      "Heating On"    <fire>   (gHouse)    { channel="draytonwiser:boiler-controller:HeatHub:controller:heatChannel1DemandState" }
Switch Heating_Away_Mode      "Away Mode"    <vacation>   (gHouse)    { channel="draytonwiser:boiler-controller:HeatHub:controller:awayModeState" }
Switch Heating_Eco_Mode      "Eco Mode"    <climate>   (gHouse)    { channel="draytonwiser:boiler-controller:HeatHub:controller:ecoModeState" }

/* Heating */
Switch Heating_GF_Living      "Heating"    <fire>   (GF_Living, Heating)    ["Heat Request"] { channel="draytonwiser:room:HeatHub:livingroom:heatRequest" }

/* Indoor Temperatures */
Number:Temperature livingroom_temperature    "Temperature [%.1f °C]" <temperature> (GF_Living, Temperature)    ["Temperature"] {channel="draytonwiser:room:HeatHub:livingroom:currentTemperature"}

/* Setpoint Temperatures */
Number:Temperature livingroom_setpoint    "Set Point [%.1f °C]" <temperature> (GF_Living)    ["Set Point"] {channel="draytonwiser:room:HeatHub:livingroom:currentSetPoint"}

/* Heat Demand */
Number livingroom_heatdemand    "Heat Demand [%.0f %%]" <heating> (GF_Living)    ["Heat Demand"] {channel="draytonwiser:room:HeatHub:livingroom:currentDemand"}

/* Manual Mode */
Switch ManualMode_GF_Living      "Manual Mode"    <switch>   (GF_Living)    ["Manual Mode"] { channel="draytonwiser:room:HeatHub:livingroom:manualModeState" }

/* Boost Mode */
Switch BoostMode_GF_Living      "Boosted"    <fire>   (GF_Living)    ["Boost Mode"] { channel="draytonwiser:room:HeatHub:livingroom:roomBoosted" }

/* Boost Duration */
Number BoostDuration_GF_Living      "Boost For[]"    <text>   (GF_Living)    ["Boost Duration"] { channel="draytonwiser:room:HeatHub:livingroom:roomBoostDuration" }

/* Boost Remaining */
Number BoostRemaining_GF_Living      "Boost Remaining"    <text>   (GF_Living)    ["Boost Remaining"] { channel="draytonwiser:room:HeatHub:livingroom:roomBoostRemaining" }

/* Humidity */
Number:Humidity livingroom_humidity  "Humidity [%.0f %%]" <humidity> (GF_Living) ["Humidity"] {channel="draytonwiser:room:HeatHub:livingroom:currentHumidity"}
```

### Sitemap

```perl
Text label="Living Room" icon="sofa" {
    Text item=livingroom_temperature
    Setpoint item=livingroom_setpoint step=0.5
    Text item=livingroom_humidity
    Text item=Heating_GF_Living
    Text item=livingroom_heatdemand
    Switch item=ManualMode_GF_Living
    Text item=BoostMode_GF_Living
    Switch item=BoostDuration_GF_Living icon="time" mappings=[0="0", 0.5="0.5", 1="1", 2="2", 3="3"]
    Text item=BoostRemaining_GF_Living icon="time"
}
```
