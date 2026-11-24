# GROHE ONDUS Binding

The GROHE ONDUS Binding provides access to data collected by a GROHE ONDUS appliance, such as a [GROHE Sense Guard](https://www.grohe.de/de_de/smarthome/grohe-sense-guard/).
The binding uses the REST API interface (the same as used by the Android app) to retrieve the collected data.

## Supported Things

This binding should support all appliances from GROHE; however, only the GROHE Sense and Sense Guard are tested with it.

| Thing type               | Name                     |
|--------------------------|--------------------------|
| account                  | GROHE ONDUS Account      |
| senseguard               | GROHE SENSE Guard device |
| sense                    | GROHE SENSE device       |

## Discovery

The binding requires you to create at least one Account thing as a bridge manually.
The discovery process will look through all locations and rooms of your configured GROHE account and adds each found appliance as a new thing automatically to the inbox.

## Binding Configuration

This binding does not require any configuration outside of things.

## Thing Configuration

There is only one thing and one bridge that needs to be configured together to get this binding to work; see the full example section for a self-explanatory example.

### Account Bridge

The `groheondus:account` bridge is used to configure the API interface for a specific account, which is used to access the collected and saved data of your GROHE account.
Use the same credentials as in the mobile app.

### Appliance

The `groheondus:sense` and `groheondus:senseguard` things are used to retrieve information for a specific appliance from GROHE.
This appliance needs to be connected to your GROHE ONDUS account as configured in the corresponding Account Bridge.
The appliance needs to be configured with the unique appliance ID (with the `applianceId` configuration) as well as the `roomId` and the `locationId`.
Once the account bridge is configured, the appliances in your account will be discovered as Appliance things.
`pollingInterval` has a minimum value of 900 seconds to avoid service rate limiting.

| Configuration            | Default value            | Description                                           |
|--------------------------|--------------------------|-------------------------------------------------------|
| applianceId              | ''                       | Unique ID of the appliance in the GROHE ONDUS account |
| roomId                   | ''                       | ID of the room the appliance is in                    |
| locationId               | ''                       | ID of the location (building) the appliance is in     |
| pollingInterval          | Retrieved from API,      | Interval in seconds to get new data from the API      |
|                          | usually 900              | The `sense` thing uses 900 by default.                |

#### Properties

The following Thing properties are filled from the GROHE ONDUS cloud and refreshed on every poll.
Properties the cloud does not report for an appliance are left out, and removed if the cloud stops reporting them.

| Property            | Description                                                      |
|---------------------|------------------------------------------------------------------|
| applianceName       | Name of the appliance, as set in the GROHE ONDUS app             |
| vendor              | Always `GROHE`                                                   |
| modelId             | `Sense` or `Sense Guard`                                         |
| serialNumber        | Serial number of the appliance                                   |
| firmwareVersion     | Firmware version of the appliance                                |
| installationDate    | Date the appliance was registered, as reported by the cloud      |
| measurementInterval | Interval in seconds at which a Sense Guard takes measurements    |

#### Channels

##### senseguard

| Channel                         | Type               | Description                                      |
|---------------------------------|--------------------|--------------------------------------------------|
| pressure                        | Number:Pressure    | The pressure of your water supply                |
| temperature_guard               | Number:Temperature | The ambient temperature of the appliance         |
| pause                           | Number:Time        | Pause the appliance for a duration in minutes    |
| valve_open                      | Switch             | Valve switch                                     |
| waterconsumption                | Number:Volume      | The amount of water used in a specific timeframe |
| waterconsumption_since_midnight | Number:Volume      | The amount of water used since midnight          |
| last_update                     | DateTime           | When the appliance last sent data to the cloud   |

##### sense

| Channel                  | Type                     | Description                                           |
|--------------------------|--------------------------|-------------------------------------------------------|
| humidity                 | Number:Dimensionless     | The humidity measured by the appliance                |
| temperature              | Number:Temperature       | The ambient temperature of the appliance              |
| battery                  | Number                   | The battery level of the appliance                    |
| pause                    | Number:Time              | Pause the appliance for a duration in minutes         |
| last_update              | DateTime                 | When the appliance last sent data to the cloud        |

Note: Be aware that the Sense reports data once a day (at most), and that the value posted in the channel - however the latest - may be up to 48 hours old.

All measurements reported by the device (`temperature`, `humidity`, `pressure` and `temperature_guard`) are requested in hourly resolution and sent to persistence as a time series with their original timestamps, so the full history uploaded by the device is stored, not just the latest value.
The channel state itself always reflects the most recent measurement.

The `waterconsumption` channel has a `timeframe` parameter (1-90, default 1): the number of full days before today to include, in addition to today.
With `timeframe=3` the channel sums the three previous days and today so far.
The Sense Guard also fetches pressure and temperature history for the same period, so a long timeframe sends a large backfill to persistence on the first poll.

The binding checks whether each device is still reporting to the GROHE ONDUS cloud.
A Sense that has sent no data for 48 hours, a Sense Guard that has sent no data for 6 hours, or a device the cloud reports as disconnected is set to OFFLINE with the time of its last report.
The battery level of a silent Sense is set to `UNDEF`, since the cloud keeps reporting the last known value.

The `pause` channel accepts a duration between 0 and 240 minutes. A positive duration activates the pause, while `0 min` cancels it.

## Full Example

Things file:

```java
Bridge groheondus:account:account1 "GROHE ONDUS account" [ username="user@example.com", password="YourStrongPasswordHere!" ] {
    senseguard mainvalve "Sense Guard" [ applianceId="550e8400-e29b-11d4-a716-446655440000", roomId=456, locationId=123 ] {
        Channels:
            Type waterconsumption : waterconsumption [ timeframe=3 ]
    }
    sense kitchen "Sense" [ applianceId="444e8400-e29b-11d4-a716-446655440000", roomId=456, locationId=123 ]
}
```

Items file:

```java
Number:Pressure Pressure_Sense_Guard "Pressure [%.1f %unit%]" {channel="groheondus:senseguard:account1:mainvalve:pressure"}
Number:Temperature Temperature_Sense_Guard "Temperature [%.1f %unit%]" {channel="groheondus:senseguard:account1:mainvalve:temperature_guard"}
Number:Volume Water_Usage_Sense_Guard "Water usage 3 days + today [%.1f %unit%]" {channel="groheondus:senseguard:account1:mainvalve:waterconsumption"}
Number:Volume Water_Usage_Since_Midnight_Sense_Guard "Water usage since midnight [%.1f %unit%]" {channel="groheondus:senseguard:account1:mainvalve:waterconsumption_since_midnight"}
Switch Valve_Sense_Guard "Valve open" {channel="groheondus:senseguard:account1:mainvalve:valve_open"}
DateTime Last_Update_Sense_Guard "Last update [%1$tY-%1$tm-%1$td %1$tH:%1$tM]" {channel="groheondus:senseguard:account1:mainvalve:last_update"}

Number:Temperature Temperature_Sense "Temperature [%.1f %unit%]" {channel="groheondus:sense:account1:kitchen:temperature"}
Number:Dimensionless Humidity_Sense "Humidity [%.0f %unit%]" {channel="groheondus:sense:account1:kitchen:humidity", unit="%"}
Number Battery_Sense "Battery [%d %%]" {channel="groheondus:sense:account1:kitchen:battery"}
DateTime Last_Update_Sense "Last update [%1$tY-%1$tm-%1$td %1$tH:%1$tM]" {channel="groheondus:sense:account1:kitchen:last_update"}
```
