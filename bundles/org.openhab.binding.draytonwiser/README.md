# Drayton Wiser Binding

This binding integrates the [Drayton Wiser Smart Heating System](https://wiser.draytoncontrols.co.uk/).
The integration happens through the HeatHub, which acts as an IP gateway to the ZigBee devices (thermostats and TRVs).

## Supported Things

The Drayton Wiser binding supports the following things:

* Bridge - The network device in the controller that allows us to interact with the other devices in the system
* Boiler Controller - The _HeatHub_ attached to the boiler. This also acts as the hub device.
* Rooms - Virtual groups of _Room Thermostats_ and _TRVs_ that can have temperatures and schedules
* Room Thermostats - Wireless thermostats which monitor temperature and humidity, and call for heat
* Smart TRVs - Wireless TRVs that monitor temperature, alter the radiator valve state and call for heat
* Hot Water - Virtual thing to manage hot water states
* Smart Plugs - Wireless plug sockets which can be remotely switched

## Discovery

The HeatHub can be discovered automatically via mDNS, however the `secret` cannot be determined automatically.
Once the `secret` has been configured, all other devices can be discovered by triggering device discovery again.

## Binding Configuration

None required

## Thing Configuration

### HeatHub Configuration

Once discovered, the HeatHub `secret` needs to be configured.
There are a few ways to obtain this, assuming you have already configured the system using the Wiser App.

1. Temporarily install a packet sniffing tool on your mobile device. Every request made includes the `secret` in the header.
2. Enable setup mode on the HeatHub. Connect a machine temporarily to the `WiserHeat_XXXXX` network and browse to `http://192.168.8.1/secret` to obtain the `secret`.

The `refresh` interval defines in seconds, how often the binding will poll the controller for updates.

The `awaySetPoint` defines the temperature in degrees Celsius that will be sent to the heathub when away mode is activated.

## Channels

### Readonly Channels

#### Boiler Controller

| Channel                        | Description                                              |
|--------------------------------|----------------------------------------------------------|
| `heatingOverride`              | State of the heating override button on the controller   |         
| `heatChannel1Demand`           | Current demand level of heating channel 1                |
| `heatChannel1DemandState`      | Is channel 1 calling the boiler for heat                 |
| `heatChannel2Demand`           | Current demand level of heating channel 2                |
| `heatChannel2DemandState`      | Is channel 2 calling the boiler for heat                 |
| `currentSignalRSSI`            | Relative Signal Strength Indicator                       |
| `currentWiserSignalStrength`   | Human readable signal strength                           |
| `currentSignalStrength`        | Signal strength value that maps to qualityofservice icon |

#### Hot Water

| Channel                   | Description                                              |
|---------------------------|----------------------------------------------------------|
| `hotWaterOverride`        | State of the hot water override button on the controller |
| `hotWaterDemandState`     | Is hot water calling the boiler for heat                 |
| `hotWaterBoosted`         | Is hot water currently being boosted                     |
| `hotWaterBoostRemaining`  | How long until the boost deactivates in minutes          |

#### Room

| Channel              | Description                                                                  |
|----------------------|------------------------------------------------------------------------------|
| `currentTemperature` | Currently reported temperature                                               |
| `currentHumidity`    | Currently reported humidity (if there is a room stat configured in this room |
| `currentDemand`      | Current heat demand percentage of the room                                   |
| `heatRequest`        | Is the room actively requesting heat from the controller                     |
| `roomBoosted`        | Is the room currently being boosted                                          |
| `roomBoostRemaining` | How long until the boost deactivates in minutes                              |
| `windowState`        | Is the window open or closed?                                                |

#### Room Stat

| Channel                        | Description                                              |
|--------------------------------|----------------------------------------------------------|
| `currentTemperature`           | Currently reported temperature                           |
| `currentHumidity`              | Currently reported humidity                              |
| `currentSetPoint`              | Currently reported set point                             |
| `currentBatteryVoltage`        | Currently reported battery voltage                       |
| `currentWiserBatteryLevel`     | Human readable battery level                             |
| `currentBatteryLevel`          | Battery level in percent                                 |
| `currentSignalRSSI`            | Relative Signal Strength Indicator                       |
| `currentSignalLQI`             | Link Quality Indicator                                   |
| `currentWiserSignalStrength`   | Human readable signal strength                           |
| `currentSignalStrength`        | Signal strength value that maps to qualityofservice icon |
| `zigbeeConnected`              | Is the roomstat joined to network                        |

#### Smart TRV

| Channel                        | Description                                              |
|--------------------------------|----------------------------------------------------------|
| `currentTemperature`           | Currently reported temperature                           |
| `currentDemand`                | Current heat demand percentage of the TRV                |
| `currentSetPoint`              | Currently reported set point                             |
| `currentBatteryVoltage`        | Currently reported battery voltage                       |
| `currentWiserBatteryLevel`     | Human readable battery level                             |
| `currentBatteryLevel`          | Battery level in percent                                 |
| `currentSignalRSSI`            | Relative Signal Strength Indicator                       |
| `currentSignalLQI`             | Link Quality Indicator                                   |
| `currentWiserSignalStrength`   | Human readable signal strength                           |
| `currentSignalStrength`        | Signal strength value that maps to qualityofservice icon |
| `zigbeeConnected`              | Is the TRV joined to network                             |

#### Smart Plug

| Channel             | Description                        |
|---------------------|------------------------------------|
| `currentSignalRSSI` | Relative Signal Strength Indicator |
| `currentSignalLQI`  | Link Quality Indicator             |
| `zigbeeConnected`   | Is the TRV joined to network       |

### Writeable Channels

#### Boiler Controller

| Channel            | Description                |
|--------------------|----------------------------|
| `awayModeState`    | Has away mode been enabled |
| `ecoModeState`     | Has eco mode been enabled  |

#### Hot Water

| Channel                 | Description                                |
|-------------------------|--------------------------------------------|
| `manualModeState`       | Has manual mode been enabled               |
| `hotWaterSetPoint`      | The current hot water setpoint (on or off) |
| `hotWaterBoostDuration` | Period in hours to boost the hot water     |
| `masterSchedule`        | The current schedule JSON for hot water    |

#### Room

| Channel                | Description                                    |
|------------------------|------------------------------------------------|
| `currentSetPoint`      | The current set point temperature for the room |
| `manualModeState`      | Has manual mode been enabled                   |
| `roomBoostDuration`    | Period in hours to boost the room temperature  |
| `windowStateDetection` | Detect whether windows are open                |
| `masterSchedule`       | The current schedule JSON for the room         |

#### Room Stat

| Channel        | Description                      |
|----------------|----------------------------------|
| `deviceLocked` | Is the roomstat interface locked |

#### Smart TRV

| Channel        | Description                 |
|----------------|-----------------------------|
| `deviceLocked` | Are the TRV controls locked |

#### Smart Plug

| Channel           | Description                                  |
|-------------------|----------------------------------------------|
| `plugOutputState` | The current on/off state of the smart plug   |
| `plugAwayAction`  | Should the plug switch off when in away mode |
| `manualModeState` | Has manual mode been enabled                 |
| `deviceLocked`    | Are the Smart Plug controls locked           |
| `masterSchedule`  | The current schedule JSON for the smart plug |

When updating the `masterSchedule` state, only the schedule portion of the JSON that is returned when querying the state is required.
The `id`, `Type`, `CurrentSetPoint`, `NextEventTime` and `NextEventSetpoint` should not be sent.

#### Known string responses for specific channels:

| Channel                 | Known responses                                                    |
|-------------------------|--------------------------------------------------------------------|
| `currentSignalStrength` | `{ "VeryGood", "Good", "Medium", "Poor", "NoSignal" }`             |
| `currentBatteryLevel`   | `{ "Full", "Normal", "TwoThirds", "OneThird", "Low", "Critical" }` |

## Full Example

### .things file

```
Bridge draytonwiser:heathub:HeatHub [ networkAddress="192.168.1.X", refresh=60, secret="secret from hub", awaySetPoint=10 ]
{
	boiler-controller controller     "Controller"
	room              livingroom     "Living Room"            [ name="Living Room" ]
	room              bathroom       "Bathroom"               [ name="Bathroom" ]
	room              bedroom        "Bedroom"                [ name="Bedroom" ]
	roomstat          livingroomstat "Living Room Thermostat" [ serialNumber="ABCDEF1234" ]
	itrv              livingroomtrv  "Living Room - TRV"      [ serialNumber="ABCDEF1235" ]
	hotwater hotwater
    smart-plug tvplug [ serialNumber="ABCDEF1236" ]
}
```

The `name` corresponds to the room name configured in the Wiser App.
It is not case sensitive.
The `serialNumber` corresponds to the device serial number which can be found on a sticker inside the battery compartment of the Smart Valves/TRVs, and behind the wall mount of the Room Thermostats.

### .items file

```
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

```
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
