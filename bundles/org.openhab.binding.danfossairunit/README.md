# DanfossAirUnit Binding

This binding supports controlling and monitoring Danfoss Air heat recovery ventilation units via Ethernet connection.

## Supported Things

This binding has been tested/reported to work with the Danfoss Air w2 / a2 / a3 devices.

## Discovery

Air units in the LAN are automatically discovered via broadcast and added to the Inbox.

## Thing Configuration

These are the available configuration parameters:

- `host` Hostname/IP of the air unit (automatically set by discovery service)
- `refreshInterval` Time (in seconds) between monitoring requests to the air unit. Smaller values mean more network load, typically set between a few seconds and a minute. Defaults to 10 seconds.
- `updateUnchangedValuesEveryMillis` Minimum time between state updates sent to the event bus for a particular channel when the state of the channel didn't change. This should avoid spamming the event bus with unnecessary updates. When set to 0, all channel state are updated every time the air unit requests are sent (see refresh interval). When set to a non zero value, unchanged values are only reported after the configured timespan has passed. Changed values are always sent to the event bus. Defaults to 60.000 (one minute), so updates are sent every minute or if the state of the channel changes.
- `timeZone` Time zone of the air unit. Leave empty for defaulting to openHAB time zone.

## Channels

| channel              | channel group | type                 | readable only (RO) or writable (RW) | description                                                                                                                                                                              |
|----------------------|---------------|----------------------|-------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| current_time         | main          | DateTime             | RO                                  | Current time reported by the air unit.                                                                                                                                                   |
| mode                 | main          | String               | RW                                  | Value to control the operation mode of the air unit. One of DEMAND, PROGRAM, MANUAL and OFF                                                                                              |
| manual_fan_step      | main          | Dimmer               | RW                                  | Value to control the fan step when in MANUAL mode (10 steps)                                                                                                                             |
| supply_fan_speed     | main          | Number:Frequency     | RO                                  | Current rotation of the fan supplying air to the rooms (in rpm)                                                                                                                          |
| extract_fan_speed    | main          | Number:Frequency     | RO                                  | Current rotation of the fan extracting air from the rooms (in rpm)                                                                                                                       |
| supply_fan_step      | main          | Dimmer               | RO                                  | Current step setting of the fan supplying air to the rooms                                                                                                                               |
| extract_fan_step     | main          | Dimmer               | RO                                  | Current step setting of the fan extracting air from the rooms                                                                                                                            |
| boost                | main          | Switch               | RW                                  | Enables fan boost                                                                                                                                                                        |
| night_cooling        | main          | Switch               | RW                                  | Enables night cooling                                                                                                                                                                    |
| room_temp            | temps         | Number:Temperature   | RO                                  | Temperature of the air in the room of the Air Dial                                                                                                                                       |
| room_temp_calculated | temps         | Number:Temperature   | RO                                  | Calculated Room Temperature                                                                                                                                                              |
| outdoor_temp         | temps         | Number:Temperature   | RO                                  | Temperature of the air outside                                                                                                                                                           |
| humidity             | humidity      | Number:Dimensionless | RO                                  | Current relative humidity measured by the air unit                                                                                                                                       |
| bypass               | recuperator   | Switch               | RW                                  | Disables the heat exchange. Useful in summer when room temperature is above target and outside temperature is below target.                                                              |
| defrost              | recuperator   | Switch               | RO                                  | Defrost status. Active when low outdoor temperatures pose a risk of ice formation in the heat exchanger.                                                                                 |
| supply_temp          | recuperator   | Number:Temperature   | RO                                  | Temperature of air which is passed to the rooms                                                                                                                                          |
| extract_temp         | recuperator   | Number:Temperature   | RO                                  | Temperature of the air as extracted from the rooms                                                                                                                                       |
| exhaust_temp         | recuperator   | Number:Temperature   | RO                                  | Temperature of the air when pushed outside                                                                                                                                               |
| battery_life         | service       | Number               | RO                                  | Remaining Air Dial Battery Level (percentage)                                                                                                                                            |
| filter_life          | service       | Number               | RO                                  | Remaining life of filter until exchange is necessary (percentage)                                                                                                                        |
| filter_period        | service       | Number               | RW                                  | Number of months between filter replacements (between 3 and 12). This value affects calculation of filter_life by the unit, and might get overwritten by Air Dial or Link CC Controller. |
| power_cycles         | operation     | Number               | RO                                  | The total count of power cycles, indicating how many times the unit has been turned off and on again.                                                                                    |
| operating_hours      | operation     | Number:Time          | RO                                  | The number of hours the unit has been in operation (in minutes).                                                                                                                         |

## Full Example

### Things

Suppose your autodiscovered air unit is identified by the id "danfossairunit:airunit:myairunit" (see section "Discovery").
The channel will then be identified by `<air unit id>:<channel group>#<channel>`

You can also manually configure your air unit in case you don't want to use autodiscovery
 (e. g. if you want to have a portable configuration):
Create a new file, e. g. `danfoss.things`, in your _things_ configuration folder:

```java
Thing danfossairunit:airunit:myairunit [host="192.168.0.7",
refreshInterval=5,
updateUnchangedValuesEveryMillis=30000]
```

### Items

```java
Dimmer DanfossHRV_ManualFanStep "Manual Fan Step [%s]" { channel = "danfossairunit:airunit:myairunit:main#manual_fan_step" }
Number:Frequency DanfossHRV_SupplyFanSpeed "Supply Fan Speed" { channel = "danfossairunit:airunit:myairunit:main#supply_fan_speed", unit="rpm" }
Number:Frequency DanfossHRV_ExtractFanSpeed "Extract Fan Speed" { channel = "danfossairunit:airunit:myairunit:main#extract_fan_speed", unit="rpm" }
String DanfossHRV_Mode "Operation Mode" { channel = "danfossairunit:airunit:myairunit:main#mode" }
Switch DanfossHRV_Boost "Boost" { channel = "danfossairunit:airunit:myairunit:main#boost" }
Switch DanfossHRV_Bypass "Bypass" { channel = "danfossairunit:airunit:myairunit:recuperator#bypass" }
Number:Dimensionless DanfossHRV_Humidity "Relative humidity" <humidity> { channel = "danfossairunit:airunit:myairunit:humidity#humidity" }
Number:Temperature DanfossHRV_RoomTemperature "Room air temperatuyre" <temperature> { channel = "danfossairunit:airunit:myairunit:temps#room_temp" }
Number:Temperature DanfossHRV_OutdoorTemperature "Outdoor air temperature" <temperature> { channel = "danfossairunit:airunit:myairunit:temps#outdoor_temp" }
Number:Temperature DanfossHRV_SupplyAirTemperature "Supply air temperature" <temperature> { channel = "danfossairunit:airunit:myairunit:recuperator#supply_temp" }
Number:Temperature DanfossHRV_ExtractAirTemperature "Extract air temperature" <temperature> { channel = "danfossairunit:airunit:myairunit:recuperator#extract_temp" }
Number:Temperature DanfossHRV_ExhaustAirTemperature "Exhaust air temperature" <temperature> { channel = "danfossairunit:airunit:myairunit:recuperator#exhaust_temp" }
Number DanfossHRV_RemainingFilterLife "Remaining filter life" { channel = "danfossairunit:airunit:myairunit:service#filter_life" }
Number DanfossHRV_FilterPeriod "Filter period" { channel = "danfossairunit:airunit:myairunit:service#filter_period" }
```

### Sitemap

```perl
sitemap danfoss label="Danfoss" {
    Frame label="Control" {
        Selection item=DanfossHRV_Mode mappings=[DEMAND="Demand", OFF="Off", PROGRAM="Program", MANUAL="Manual"]
        Slider item=DanfossHRV_ManualFanStep step=10 visibility=[DanfossHRV_Mode=="MANUAL"]
        Switch item=DanfossHRV_Bypass
        Switch item=DanfossHRV_Boost
    }
    Frame label="Measurements" {
        Text item=DanfossHRV_Humidity
        Text item=DanfossHRV_RoomTemperature
        Text item=DanfossHRV_OutdoorTemperature
        Text item=DanfossHRV_SupplyAirTemperature
        Text item=DanfossHRV_ExtractAirTemperature
        Text item=DanfossHRV_ExhaustAirTemperature
    }
    Frame label="Fan" {
        Text item=DanfossHRV_SupplyFanSpeed
        Text item=DanfossHRV_ExtractFanSpeed
    }
    Frame label="Filter" {
         Text item=DanfossHRV_RemainingFilterLife
         Slider item=DanfossHRV_FilterPeriod minValue=3 maxValue=12
    }
}
```
