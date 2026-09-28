# Philips Air Purifier Binding

This binding provides readings and control of Philips Air Purifier devices.

This binding builds on the protocol reverse engineering done by [rgerganov](https://github.com/rgerganov/py-air-control/commits?author=rgerganov) in [py-air-control](https://github.com/rgerganov/py-air-control), many thanks for that work.

## Supported Things

The following devices have been tested:

- AC2889/10
- AC2729
- AC2729/50
- AC1214/10
- AC3829/10

Other Philips Air Purifiers are likely to work as well; feedback on compatibility with other models is welcome.

| Thing Type | Description                                                                          |
|------------|--------------------------------------------------------------------------------------|
| ac2889-10  | Philips Air Purifier AC2889/10 (HTTP protocol)                                       |
| ac2729     | Philips Air Purifier/Humidifier AC2729 (HTTP protocol)                               |
| ac1214-10  | Philips Air Purifier AC1214/10 (HTTP protocol)                                       |
| ac3829-10  | Philips Air Purifier/Humidifier AC3829/10 (HTTP protocol)                            |
| universal  | Any other Philips Air Purifier using the HTTP protocol                               |
| coap       | Philips Air Purifiers using the CoAP protocol (most models released from 2019 on)    |

### Features

- discovery via UPnP (HTTP devices) and CoAP (CoAP devices)
- power on/off
- fan speed and purification mode control
- light control
- sensor readings (air quality, temperature, humidity)
- filter status
- child lock
- temperature and humidity offsets

## Discovery

The binding discovers Philips Air Purifiers on the local network automatically.
The models listed above are recognized as their own thing type, all other devices using the HTTP protocol are discovered as `universal` thing.
Devices using the CoAP protocol are discovered as `coap` thing.

Background discovery is enabled by default.
It can be disabled in the UI under Settings → Add-on Settings → Philips Air Purifier Binding, or by adding the following line to `services/runtime.cfg`:

```text
discovery.philipsair:background=false
```

This setting applies to both the UPnP and the CoAP discovery.

## Thing Configuration

Discovered things do not need any configuration.
For HTTP devices, the binding exchanges the encryption key with the device automatically after the thing is added.

The following parameters can be set manually:

| Parameter         | Description                                                                                                  |
|-------------------|--------------------------------------------------------------------------------------------------------------|
| host              | IP address or hostname of the device. Set automatically upon discovery.                                      |
| key               | Encryption key for the communication with HTTP devices. Optional, exchanged with the device automatically.   |
| deviceUUID        | Device ID. Optional, set automatically upon discovery.                                                       |
| refreshInterval   | Refresh interval in seconds. Optional, the default is 60 seconds.                                            |
| humidityOffset    | Offset added to the humidity readings. Optional, the default is 0 %.                                         |
| temperatureOffset | Offset added to the temperature readings. Optional, the default is 0 °C.                                     |

CoAP devices push their state changes to openHAB.
For these devices the refresh interval is only used to check that the device still sends updates; the thing goes offline when no update is received within twice the refresh interval (at least 60 seconds).

demo.things

```java
philipsair:ac2889-10:livingroom "Philips Air AC2889/10" @ "Living Room" [ host="192.168.1.10", refreshInterval=15 ]
philipsair:ac3829-10:bedroom "Philips Air AC3829/10" @ "Bedroom" [ host="192.168.1.11", refreshInterval=15 ]
```

## Channels

| Channel Group | Channel ID            | Item Type            | Description                                                                          |
|---------------|-----------------------|----------------------|--------------------------------------------------------------------------------------|
| controls      | power                 | Switch               | Device power on/off                                                                  |
| controls      | fan-speed             | String               | Fan speed (s - silent, 1, 2, 3, t - turbo)                                           |
| controls      | mode                  | String               | Mode (P - auto, A - allergen, S - sleep, M - manual, B - bacteria, N - night)        |
| controls      | timer                 | Number               | Timer in hours (0-5)                                                                 |
| controls      | timer-remaining       | Number:Time          | Time left until the timer switches the device off                                    |
| controls      | child-lock            | Switch               | Child lock on/off                                                                    |
| controls      | target-humidity       | Number:Dimensionless | Humidity setpoint                                                                    |
| controls      | function              | String               | Function (P - purification, PH - purification and humidification)                    |
| controls-ui   | button-light          | Switch               | Button light on/off                                                                  |
| controls-ui   | light-level           | Number:Dimensionless | LED light level (0, 25, 50, 75, 100 %)                                               |
| controls-ui   | displayed-index       | String               | Index shown on the display (1 - PM2.5, 0 - allergen index)                           |
| sensors       | pm25                  | Number:Density       | PM2.5 particle concentration                                                         |
| sensors       | allergen-index        | Number               | Allergen index                                                                       |
| sensors       | air-quality-threshold | Number               | Air quality index at which the device notifies                                       |
| sensors       | error-code            | String               | Error code                                                                           |
| sensors       | humidity              | Number:Dimensionless | Current humidity                                                                     |
| sensors       | temperature           | Number:Temperature   | Current temperature                                                                  |
| sensors       | water-level           | Number:Dimensionless | Water tank level                                                                     |
| filters       | pre-filter-life       | Number:Time          | Estimated time until the pre-filter needs to be cleaned (in hours)                   |
| filters       | hepa-filter-life      | Number:Time          | Estimated remaining lifetime of the HEPA filter (in hours)                           |
| filters       | carbon-filter-life    | Number:Time          | Estimated remaining lifetime of the active carbon filter (in hours)                  |
| filters       | wick-filter-life      | Number:Time          | Estimated remaining lifetime of the wick filter (in hours)                           |

The channels `target-humidity`, `function`, `humidity`, `temperature`, `water-level` and `wick-filter-life` are only supported by some models.
For thing types that do not define them (e.g. `coap` and `universal`), they are added automatically once the device reports the corresponding value.

## Thing Properties

| Property        | Description                                          |
|-----------------|------------------------------------------------------|
| vendor          | Always `Philips`                                     |
| modelId         | Model reported by the device, e.g. `AC2889/10`       |
| firmwareVersion | Firmware version of the device                       |
| name            | Name of the device as configured in the Philips app |

Discovered things are identified by their `deviceUUID` configuration parameter.

## Full Example

demo.items

```java
Switch                ac2889_10_power          "Power"                <switch>       { channel="philipsair:ac2889-10:livingroom:controls#power" }
String                ac2889_10_fan_speed      "Fan Speed"            <fan>          { channel="philipsair:ac2889-10:livingroom:controls#fan-speed" }
String                ac2889_10_mode           "Mode"                 <text>         { channel="philipsair:ac2889-10:livingroom:controls#mode" }
Switch                ac2889_10_button_light   "Button Light"         <lightbulb>    { channel="philipsair:ac2889-10:livingroom:controls-ui#button-light" }
String                ac2889_10_index          "Displayed Index"      <text>         { channel="philipsair:ac2889-10:livingroom:controls-ui#displayed-index" }
Number:Dimensionless  ac2889_10_light_level    "LED Light Level"      <lightbulb>    { channel="philipsair:ac2889-10:livingroom:controls-ui#light-level" }
Number:Time           ac2889_10_timer_left     "Timer Remaining"      <time>         { channel="philipsair:ac2889-10:livingroom:controls#timer-remaining" }
Number                ac2889_10_timer          "Timer"                <time>         { channel="philipsair:ac2889-10:livingroom:controls#timer" }
Number:Density        ac2889_10_pm25           "PM2.5"                <smoke>        { channel="philipsair:ac2889-10:livingroom:sensors#pm25" }
Number                ac2889_10_allergen       "Allergen Index"       <text>         { channel="philipsair:ac2889-10:livingroom:sensors#allergen-index" }
String                ac2889_10_error          "Error"                <error>        { channel="philipsair:ac2889-10:livingroom:sensors#error-code" }
Number:Time           ac2889_10_pre_filter     "Pre-filter"           <text>         { channel="philipsair:ac2889-10:livingroom:filters#pre-filter-life" }
Number:Time           ac2889_10_carbon_filter  "Carbon Filter"        <text>         { channel="philipsair:ac2889-10:livingroom:filters#carbon-filter-life" }
Number:Time           ac2889_10_hepa_filter    "HEPA Filter"          <text>         { channel="philipsair:ac2889-10:livingroom:filters#hepa-filter-life" }

Switch                ac3829_10_child_lock     "Child Lock"           <lock>         { channel="philipsair:ac3829-10:bedroom:controls#child-lock" }
Number:Time           ac3829_10_wick_filter    "Wick Filter"          <text>         { channel="philipsair:ac3829-10:bedroom:filters#wick-filter-life" }
Number:Dimensionless  ac3829_10_humidity       "Humidity"             <humidity>     { channel="philipsair:ac3829-10:bedroom:sensors#humidity" }
Number:Dimensionless  ac3829_10_target_hum     "Humidity Setpoint"    <humidity>     { channel="philipsair:ac3829-10:bedroom:controls#target-humidity" }
Number:Temperature    ac3829_10_temperature    "Temperature"          <temperature>  { channel="philipsair:ac3829-10:bedroom:sensors#temperature" }
String                ac3829_10_function       "Function"             <text>         { channel="philipsair:ac3829-10:bedroom:controls#function" }
Number:Dimensionless  ac3829_10_water_level    "Water Level"          <cistern>      { channel="philipsair:ac3829-10:bedroom:sensors#water-level" }
```

demo.sitemap

```java
sitemap philips_air_purifier label="Philips Air Purifier" {
    Frame label="Control" {
        Switch item=ac2889_10_power
        Selection item=ac2889_10_fan_speed
        Selection item=ac2889_10_mode
    }
    Frame label="Display" {
        Switch item=ac2889_10_button_light
        Selection item=ac2889_10_index
    }
    Frame label="Sensors" {
        Text item=ac2889_10_pm25
        Text item=ac3829_10_temperature
        Text item=ac3829_10_humidity
    }
}
```
