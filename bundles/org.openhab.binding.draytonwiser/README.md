# Drayton Wiser Binding

This binding integrates the [Drayton Wiser Smart Heating System](https://wiser.draytoncontrols.co.uk/). The integration happens through the HeatHub, which acts as an IP gateway to the ZigBee devices (thermostats and TRVs).

## Supported Things

The Drayton Wiser binding supports the following things:
* Bridge - The network device in the controller that allows us to interact with the other devices in the system
* Controller - The HeatHub attached to the boiler. This also acts as the hub device.
* Rooms - Virtual groups of Room Stats and TRVs that can have temperatures and schedules
* Room Stats - Wireless thermostats which monitor temperature and humidity, and call for heat
* Smart TRVs - Wireless TRVs that monitor temperature and can alter the radiator valve state and call for heat

## Discovery

The HeatHub can be discovered automatically via mDNS, however the `SECRET` cannot be determined automatically. Once the `SECRET` has been configured, all other devices can be discovered by triggering device discovery again.

## Binding Configuration

None required

## Thing Configuration

### HeatHub Configuration

Once discovered, the HeatHub `SECRET` needs to be configured. There are a few ways to obtain this, assuming you have already configured the system using the Wiser App.

* Temporarily install a packet sniffing tool on your mobile device. Every request made includes the `SECRET` in the header.
* Enable setup mode on the HeatHub. Connect a machine temporarily to the `WiserHeat_XXXXX` network and browse to `http://192.168.8.1/secret` to obtain the key.

## Channels

### Readonly Channels

#### Controller

| Channel                   | Description                                              | Implemented |
|---------------------------|----------------------------------------------------------|-------------|
| `heatingOverride`         | State of the heating override button on the controller   | Yes         |
| `hotWaterOverride`        | State of the hot water override button on the controller | Yes         |
| `heatChannel1Demand`      | Current demand level of heating channel 1                | Yes         |
| `heatChannel1DemandState` | Is channel 1 calling the boiler for heat                 | Yes         |
| `heatChannel2Demand`      | Current demand level of heating channel 2                | Yes         |
| `heatChannel2DemandState` | Is channel 2 calling the boiler for heat                 | Yes         |
| `currentSignalRSSI`       | Relative Signal Strength Indicator                       | Yes         |
| `currentSignalStrength`   | Human readable signal strength                           | Yes         |

#### Room

| Channel              | Description                                                                  | Implemented |
|----------------------|------------------------------------------------------------------------------|-------------|
| `currentTemperature` | Currently reported temperature                                               | Yes         |
| `currentHumidity`    | Currently reported humidity (if there is a room stat configured in this room | Yes         |
| `currentDemand`      | Current heat demand percentage of the room                                   | Yes         |
| `heatRequest`        | Is the room actively requesting heat from the controller                     | Yes         |
| next set point temp  | The next set point temperature to be active                                  | No          |
| next set point time  | The time of the next set point temperature to be active                      | No          |

#### Room Stat

| Channel                 | Description                        | Implemented |
|-------------------------|------------------------------------|-------------|
| `currentTemperature`    | Currently reported temperature     | Yes         |
| `currentHumidity`       | Currently reported humidity        | Yes         |
| `currentSetPoint`       | Currently reported set point       | Yes         |
| `currentBatteryVoltage` | Currently reported battery voltage | Yes         |
| `currentBatteryLevel`   | Human readable battery level       | Yes         |
| `currentSignalRSSI`     | Relative Signal Strength Indicator | Yes         |
| `currentSignalStrength` | Human readable signal strength     | Yes         |

#### Smart TRV

| Channel                 | Description                               | Implemented |
|-------------------------|-------------------------------------------|-------------|
| `currentTemperature`    | Currently reported temperature            | Yes         |
| `currentDemand`         | Current heat demand percentage of the TRV | Yes         |
| `currentSetPoint`       | Currently reported set point              | Yes         |
| `currentBatteryVoltage` | Currently reported battery voltage        | Yes         |
| `currentBatteryLevel`   | Human readable battery level              | Yes         |
| `currentSignalRSSI`     | Relative Signal Strength Indicator        | Yes         |
| `currentSignalStrength` | Human readable signal strength            | Yes         |

### Writeable Channels

#### Controller

| Channel            | Description                              | Implemented |
|--------------------|------------------------------------------|-------------|
| `awayModeState`    | Has away mode been enabled               | Yes         |
| `awayModeSetPoint` | Set point of all TRVs/Stats in away mode | ReadOnly    |
| `ecoModeState`     | Has eco mode been enabled                | ReadOnly    |

#### Room

| Channel           | Description                                    | Implemented |
|-------------------|------------------------------------------------|-------------|
| `currentSetPoint` | The current set point temperature for the room | Yes         |
| `manualModeState` | Has manual mode been enabled                   | Yes         |
| Room Boosting     | Boost the room temperature for a set interval  | No          |
| Schedules         | The Time and Set Point schedule                | No          |

#### Known string responses for specific channels:

| Channel                 | Known responses                          |
|-------------------------|------------------------------------------|
| `currentSignalStrength` | `{ "VeryGood", "Good", "Medium", Poor }` |
| `currentBatteryLevel`   | `{ "Normal", "TwoThirds" }`              |

## Full Example

TODO
