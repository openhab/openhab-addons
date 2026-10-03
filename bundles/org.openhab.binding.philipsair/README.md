# Philips Air Purifier Binding

This binding monitors and controls Philips air purifiers and combined air purifier/humidifiers over the local network.
No cloud account is needed.

The binding builds on the protocol reverse engineering done by [rgerganov](https://github.com/rgerganov/py-air-control/commits?author=rgerganov) in [py-air-control](https://github.com/rgerganov/py-air-control), many thanks for that work.

## Supported Things

Philips air purifiers use one of two local protocols, depending on their age:

- **HTTP**: older models, discovered using UPnP. The communication is encrypted with a key that the binding exchanges with the device automatically.
- **CoAP**: models released from about 2019 on. These devices push their state changes to openHAB.

Some models, such as the AC2889 and AC3829, used HTTP with early firmware and use CoAP after a firmware update.
Use the `coap` thing type for a device with updated firmware, and the thing type of the model (or `universal`) for a device with HTTP firmware.

| Thing Type | Protocol | Description                                                     |
|------------|----------|-----------------------------------------------------------------|
| ac2889-10  | HTTP     | Philips Air Purifier AC2889/10                                  |
| ac2729     | HTTP     | Philips Air Purifier/Humidifier AC2729                          |
| ac1214-10  | HTTP     | Philips Air Purifier AC1214/10                                  |
| ac3829-10  | HTTP     | Philips Air Purifier/Humidifier AC3829/10                       |
| universal  | HTTP     | Any other Philips air purifier using the HTTP protocol          |
| coap       | CoAP     | Philips air purifiers using the CoAP protocol                   |

The following models have been tested: AC1214/10, AC2729, AC2729/50, AC2889/10, AC2939/10, AC3033/10, AC3829/10 and AC5659/10.
Other models using the same protocols are likely to work as well; feedback on compatibility with other models is welcome.

### Recent CoAP Models

CoAP devices such as the AC2889, AC3033, AC3829 and AC4236 series (the AC2889 and AC3829 only with updated firmware, see above) report their state with the classic field names (e.g. `pwr`, `om`, `pm25`) and are fully supported.

Recent models report their state with numbered field names instead: newer models use names like `D03102`, older ones names like `D03-02`.
Examples are the AC0850, AC0950, AC1715, AC2210, AC3210, AC3420, AC3737, AMF and HU series.
Their modes, fan speeds and other settings are encoded differently per model, so the binding uses a _device profile_ per model to translate them.

#### Device Profiles

The profile is detected from the model that the device reports and is shown in the thing property `deviceProfile`, see [Thing Properties](#thing-properties).
When the model of your device is not detected, you can select the profile of a model that is the same or similar in the advanced thing configuration parameter `deviceProfile`.
The device then is controlled like that model, without the binding needing an update.
Select `basic` again if the device does not behave as expected.

| Profile (`deviceProfile`) | Models                                                                  | Field names |
|---------------------------|-------------------------------------------------------------------------|-------------|
| `auto`                    | Detects the profile                                                     |             |
| `basic`                   | Other recent models: sensors, power and child lock                      | both        |
| `unicorn`                 | AC2210, AC2220, AC2221, AC3210, AC3220, AC3221, AC4220, AC4221          | `D03102`    |
| `ac3737`                  | AC3737                                                                  | `D03102`    |
| `ac1715`                  | AC1715                                                                  | `D03-02`    |
| `ac0850`                  | AC0850 (with the `AWS_Philips_AIR` Wi-Fi firmware)                      | `D03-02`    |

A profile only applies to a device with the field names it is for, otherwise the profile is detected.

| Channel                                     | `basic`                    | `unicorn`                                | `ac3737`                    | `ac1715`, `ac0850`          |
|---------------------------------------------|----------------------------|------------------------------------------|-----------------------------|-----------------------------|
| `power`                                     | read and write             | read and write                           | read and write              | read and write              |
| `child-lock`                                | read and write<sup>1</sup> | read and write                           | read and write              | read and write              |
| `pm25`, `allergen-index`                    | read only                  | read only                                | read only                   | read only                   |
| `humidity`, `temperature`, `error-code`     | read only                  | read only                                | read only                   | read only                   |
| `tvoc`                                      | read only                  | read only                                | read only                   | read only                   |
| `air-quality-threshold`, `displayed-index`  | read only                  | read and write                           | read and write              | read and write              |
| `mode`                                      | not supported              | `P` (auto), `S` (sleep)                  | `P`, `S`                    | `P`, `S`                    |
| `fan-speed`                                 | not supported              | `1` to `5`, `m` (medium), `t` (turbo)    | `1`, `2`, `t`               | `1`, `2`, `t` (`t` only: ac0850) |
| `timer`                                     | not supported              | 0 (off) to 12 hours                      | not supported               | not supported               |
| `timer-remaining`                           | read only<sup>2</sup>      | read only                                | read only                   | not supported               |
| `target-humidity`                           | read only<sup>2</sup>      | read and write                           | read and write              | not supported               |
| `beep`, `standby-sensors`, `display`, `lamp-mode` | not supported        | read and write                           | not supported               | not supported               |
| `display-brightness`                        | not supported              | `0` (off), `101` (auto), `115` (low), `123` (bright) | `0`, `50`, `100` | not supported       |
| `allergy-sleep`                             | not supported              | read and write                           | read and write              | not supported               |
| `pre-filter-life`, `hepa-filter-life`       | read only                  | read only                                | read only                   | read only                   |
| all other channels                          | not supported, they stay `NULL` | not supported                       | not supported               | not supported               |

- <sup>1</sup> The older field names (`D03-xx`) only support reading the child lock.
- <sup>2</sup> Only the newer field names (`D03xxx`).

Notes on the profiles:

- The mode is `M` (manual) while a fan speed is selected, and the fan speed shows the speed the device chose in auto and sleep mode (Unicorn).
  Selecting a fan speed switches the device to manual mode; there is no separate command for the manual mode.
  For the `ac3737`, `ac1715` and `ac0850` profiles selecting a mode or fan speed also switches the device on.
- The timer, `target-humidity` and the settings channels (`beep`, `standby-sensors`, `allergy-sleep`, `display`, `display-brightness` and `lamp-mode`) are added once the device reports them.
  The `display` switch is on while the display is on.
- Commands are only sent for the values listed in the table. Other values are ignored.
- The `display-brightness` values `101`, `115` and `123` are the codes the device uses, not percentages.

The profiles follow the Philips Air+ app and the [philips-airpurifier-coap](https://github.com/kongo09/philips-airpurifier-coap) integration.
Reading the status of an AC3210/12 has been confirmed on a real device; the commands have not been confirmed on a device yet, and neither has any other recent model.
Feedback is welcome.

The thing properties `modelId`, `firmwareVersion` and `name` are set for these models as well.
If such a device is not discovered, add it manually as `coap` thing with its IP address.

### Helping to Support a New Model

Adding full support for a model requires knowing which field holds which setting and which values it takes.
If you own a model that is not fully supported, you can help by collecting this information:

1. Add the device as `coap` thing (or `universal` thing for an HTTP device) and make sure it is online.
1. Enable debug logging for the binding in the openHAB console:

   ```shell
   log:set DEBUG org.openhab.binding.philipsair
   ```

1. Change one setting at a time with the buttons on the device or in the Philips app, and note what you changed, e.g. "mode from Auto to Sleep" or "fan speed from 1 to 2".
   Wait until a new status line appears in `openhab.log` after each change: `Status from <ip>: {...}` for CoAP devices, `Philips Air Purifier device response: '{...}'` for HTTP devices.
   Go through all modes, fan speeds, light levels, the timer, the child lock and, for humidifiers, the humidity setpoint.
1. Note the values the Philips app shows at the same time, e.g. PM2.5, humidity, temperature and the remaining filter lifetimes.
1. Reset the log level with `log:set INFO org.openhab.binding.philipsair`.

Then open an issue on [GitHub](https://github.com/openhab/openhab-addons/issues) or a topic on the [openHAB community forum](https://community.openhab.org), and include:

- the model number printed on the device label (e.g. `AC3737/10`) and its firmware version
- the status log lines, each with the change you made just before it
- the values shown in the Philips app

The status lines do not contain passwords or keys, but they do contain the device name and ID; you may replace those before posting.

## Discovery

The binding discovers Philips air purifiers on the local network automatically.
HTTP devices are found using UPnP: the models listed above are discovered as their own thing type, all other HTTP devices as `universal` thing.
CoAP devices are found with a CoAP broadcast and discovered as `coap` thing.

Background discovery is enabled by default.
It can be disabled in the UI under Settings → Add-on Settings → Philips Air Purifier Binding, or by adding the following line to `services/runtime.cfg`:

```text
discovery.philipsair:background=false
```

This setting applies to both the UPnP and the CoAP discovery.

## Thing Configuration

Discovered things do not need any configuration.

| Parameter         | Type    | Required | Default | Description                                                                                                        |
|-------------------|---------|----------|---------|--------------------------------------------------------------------------------------------------------------------|
| host              | text    | yes      |         | IP address or hostname of the device. Set automatically upon discovery.                                            |
| key               | text    | no       |         | HTTP devices only: encryption key. Exchanged with the device automatically when empty.                             |
| deviceUUID        | text    | no       |         | Device ID. Set automatically upon discovery and used to identify discovered things.                               |
| refreshInterval   | integer | no       | 60      | Refresh interval in seconds (minimum 5). For CoAP devices the interval at which the connection is checked.         |
| deviceProfile     | text    | no       | auto    | CoAP devices only (advanced): the model profile, see [Device Profiles](#device-profiles). Detected when `auto`.    |
| humidityOffset    | decimal | no       | 0       | Offset in % added to the humidity readings (-100 to 100).                                                          |
| temperatureOffset | decimal | no       | 0       | Offset in °C added to the temperature readings (-50 to 50).                                                        |

HTTP devices are polled at the refresh interval.
When the device rejects the key, for example after a reset, the binding exchanges a new key automatically.

CoAP devices push their state changes, so they are not polled.
For these devices the refresh interval is only used to check the connection.
When no update has arrived for the refresh interval (at least 30 seconds), the binding checks whether the device still answers.
A device that sends no updates, for example in standby, stays online as long as it answers this check.
The thing goes offline when the device neither sends an update nor answers within twice the refresh interval (at least 60 seconds).

## Channels

The channels are organized in the groups `controls`, `controls-ui`, `sensors` and `filters`.

| Channel Group | Channel ID            | Item Type            | Read/Write | Description                                                                              |
|---------------|-----------------------|----------------------|------------|------------------------------------------------------------------------------------------|
| controls      | power                 | Switch               | RW         | Device power                                                                             |
| controls      | fan-speed             | String               | RW         | Fan speed: `s` (silent), `1`, `2`, `3`, `t` (turbo). Setting the fan speed also switches the device to manual mode. |
| controls      | mode                  | String               | RW         | Mode: `P` (auto), `A` (allergen), `S` (sleep), `M` (manual), `B` (bacteria), `N` (night) |
| controls      | timer                 | Number               | RW         | Switch-off timer in hours (0 is off, up to 5 or 12 depending on the model)               |
| controls      | timer-remaining       | Number:Time          | R          | Time left until the timer switches the device off                                        |
| controls      | child-lock            | Switch               | RW         | Child lock                                                                               |
| controls      | target-humidity       | Number:Dimensionless | RW         | Humidity setpoint (40-70 %, in steps of 10 %)                                            |
| controls      | function              | String               | RW         | Function: `P` (purification), `PH` (purification and humidification)                    |
| controls-ui   | button-light          | Switch               | RW         | Button light                                                                             |
| controls-ui   | light-level           | Number:Dimensionless | RW         | Display light level (0, 25, 50, 75, 100 %)                                               |
| controls      | standby-sensors       | Switch               | RW         | The sensors keep measuring in standby (advanced)                                         |
| controls      | allergy-sleep         | Switch               | RW         | Sleep mode that is gentle for allergic people (advanced)                                |
| controls-ui   | displayed-index       | String               | RW         | Index shown on the display: `0` (allergen index), `1` (PM2.5), `2` (gas, only offered on models with a gas sensor) |
| controls-ui   | display-brightness    | String               | RW         | Brightness of the display, with the steps of the model (see the device profiles)         |
| controls-ui   | display               | Switch               | RW         | The display is on (advanced)                                                             |
| controls-ui   | lamp-mode             | String               | RW         | What the lamp shows: `0` (off), `1` (air quality), `2` (ambient)                         |
| controls-ui   | beep                  | Switch               | RW         | The device beeps when a button is pressed (advanced)                                     |
| sensors       | pm25                  | Number:Density       | R          | PM2.5 particle concentration                                                             |
| sensors       | allergen-index        | Number               | R          | Allergen index                                                                           |
| sensors       | air-quality-threshold | Number               | RW         | Air quality level at which the Philips app sends a notification: `1` (good), `4` (fair), `7` (poor), `10` (very poor); on the AC4373 and AC4375 `13`, `19`, `29`, `40` |
| sensors       | error-code            | String               | R          | Error code, e.g. `0` (no error), `49408` (no water), `32768` (water tank open), `49155` (clean pre-filter) |
| sensors       | humidity              | Number:Dimensionless | R          | Current humidity, corrected by `humidityOffset`                                          |
| sensors       | temperature           | Number:Temperature   | R          | Current temperature, corrected by `temperatureOffset`                                    |
| sensors       | water-level           | Number:Dimensionless | R          | Water tank level                                                                         |
| sensors       | tvoc                  | Number               | R          | TVOC level; recent models report it as an index from 1 to 4                              |
| sensors       | rssi                  | Number:Power         | R          | Wi-Fi signal strength (advanced)                                                         |
| filters       | pre-filter-life       | Number:Time          | R          | Time until the pre-filter needs to be cleaned                                            |
| filters       | hepa-filter-life      | Number:Time          | R          | Remaining lifetime of the HEPA filter                                                    |
| filters       | carbon-filter-life    | Number:Time          | R          | Remaining lifetime of the active carbon filter                                           |
| filters       | wick-filter-life      | Number:Time          | R          | Remaining lifetime of the humidifier wick                                                |

The channels `target-humidity`, `function`, `humidity`, `temperature`, `water-level` and `wick-filter-life` are only available on models with a humidifier or the corresponding sensors.
The channels `timer` and `timer-remaining` are only available on models with a switch-off timer.
The `ac2729` and `ac3829-10` thing types always have these channels.
For the other thing types, in particular `universal` and `coap`, they are added automatically once the device reports the corresponding value.
The channels `tvoc` and `rssi` are added the same way on all thing types, as only some models report them.
The channels `beep`, `display`, `display-brightness`, `lamp-mode`, `standby-sensors` and `allergy-sleep` are added the same way, for the CoAP devices that have these settings.

## Thing Properties

| Property        | Description                                                           |
|-----------------|-----------------------------------------------------------------------|
| vendor          | Always `Philips`                                                      |
| modelId         | Model reported by the device, e.g. `AC2889/10`                        |
| firmwareVersion | Firmware version of the device                                        |
| name            | Name of the device as configured in the Philips app                   |
| deviceType      | Device type reported during discovery                                 |
| manufacturer    | Manufacturer reported during discovery                                |
| macAddress      | MAC address of the device, reported during UPnP discovery             |
| preFilterType   | Type code of the pre-filter, if reported by the device                |
| hepaFilterType  | Type code of the HEPA filter, e.g. `A3`, if reported by the device    |
| carbonFilterType | Type code of the active carbon filter, e.g. `C7`, if reported by the device |
| deviceProfile   | Name of the device profile resolved for a CoAP device, e.g. `UNICORN` or `CLASSIC` |
| deviceProfileHost | Host the `deviceProfile` was resolved for                           |

The properties `deviceType`, `manufacturer` and `macAddress` are only set on discovered things.
The properties `deviceProfile` and `deviceProfileHost` are stored for every CoAP device once it has reported its status.
After a restart, the stored profile is used until the device reports its status, as long as the configured host is still the host in `deviceProfileHost`.

## Full Example

### Thing Configuration

```java
Thing philipsair:ac2889-10:livingroom "Air Purifier Living Room" @ "Living Room" [ host="192.168.1.10", refreshInterval=15 ]
Thing philipsair:ac3829-10:bedroom    "Air Purifier Bedroom"     @ "Bedroom"     [ host="192.168.1.11", refreshInterval=15 ]
Thing philipsair:coap:office          "Air Purifier Office"      @ "Office"      [ host="192.168.1.12" ]
```

### Item Configuration

```java
Switch                LivingRoom_AP_Power          "Power"                  <switch>       { channel="philipsair:ac2889-10:livingroom:controls#power" }
String                LivingRoom_AP_FanSpeed       "Fan Speed"              <fan>          { channel="philipsair:ac2889-10:livingroom:controls#fan-speed" }
String                LivingRoom_AP_Mode           "Mode"                   <text>         { channel="philipsair:ac2889-10:livingroom:controls#mode" }
Number                LivingRoom_AP_Timer          "Timer [%d h]"           <time>         { channel="philipsair:ac2889-10:livingroom:controls#timer" }
Number:Time           LivingRoom_AP_TimerLeft      "Timer Remaining"        <time>         { channel="philipsair:ac2889-10:livingroom:controls#timer-remaining" }
Switch                LivingRoom_AP_ButtonLight    "Button Light"           <lightbulb>    { channel="philipsair:ac2889-10:livingroom:controls-ui#button-light" }
Number:Dimensionless  LivingRoom_AP_LightLevel     "Light Level"            <lightbulb>    { channel="philipsair:ac2889-10:livingroom:controls-ui#light-level" }
String                LivingRoom_AP_Index          "Displayed Index"        <text>         { channel="philipsair:ac2889-10:livingroom:controls-ui#displayed-index" }
Number:Density        LivingRoom_AP_PM25           "PM2.5"                  <smoke>        { channel="philipsair:ac2889-10:livingroom:sensors#pm25" }
Number                LivingRoom_AP_Allergen       "Allergen Index"         <text>         { channel="philipsair:ac2889-10:livingroom:sensors#allergen-index" }
String                LivingRoom_AP_Error          "Error"                  <error>        { channel="philipsair:ac2889-10:livingroom:sensors#error-code" }
Number:Time           LivingRoom_AP_PreFilter      "Pre-filter"             <text>         { channel="philipsair:ac2889-10:livingroom:filters#pre-filter-life" }
Number:Time           LivingRoom_AP_HepaFilter     "HEPA Filter"            <text>         { channel="philipsair:ac2889-10:livingroom:filters#hepa-filter-life" }
Number:Time           LivingRoom_AP_CarbonFilter   "Carbon Filter"          <text>         { channel="philipsair:ac2889-10:livingroom:filters#carbon-filter-life" }

Switch                Bedroom_AP_Power             "Power"                  <switch>       { channel="philipsair:ac3829-10:bedroom:controls#power" }
Switch                Bedroom_AP_ChildLock         "Child Lock"             <lock>         { channel="philipsair:ac3829-10:bedroom:controls#child-lock" }
String                Bedroom_AP_Function          "Function"               <text>         { channel="philipsair:ac3829-10:bedroom:controls#function" }
Number:Dimensionless  Bedroom_AP_TargetHumidity    "Humidity Setpoint"      <humidity>     { channel="philipsair:ac3829-10:bedroom:controls#target-humidity" }
Number:Dimensionless  Bedroom_AP_Humidity          "Humidity"               <humidity>     { channel="philipsair:ac3829-10:bedroom:sensors#humidity" }
Number:Temperature    Bedroom_AP_Temperature       "Temperature"            <temperature>  { channel="philipsair:ac3829-10:bedroom:sensors#temperature" }
Number:Dimensionless  Bedroom_AP_WaterLevel        "Water Level"            <cistern>      { channel="philipsair:ac3829-10:bedroom:sensors#water-level" }
Number:Time           Bedroom_AP_WickFilter        "Wick"                   <text>         { channel="philipsair:ac3829-10:bedroom:filters#wick-filter-life" }

Switch                Office_AP_Power              "Power"                  <switch>       { channel="philipsair:coap:office:controls#power" }
Number:Density        Office_AP_PM25               "PM2.5"                  <smoke>        { channel="philipsair:coap:office:sensors#pm25" }
```

### Sitemap Configuration

```perl
sitemap philipsair label="Air Purifiers" {
    Frame label="Living Room" {
        Switch    item=LivingRoom_AP_Power
        Selection item=LivingRoom_AP_FanSpeed
        Selection item=LivingRoom_AP_Mode
        Setpoint  item=LivingRoom_AP_Timer minValue=0 maxValue=5 step=1
        Text      item=LivingRoom_AP_TimerLeft
        Switch    item=LivingRoom_AP_ButtonLight
        Selection item=LivingRoom_AP_Index
        Text      item=LivingRoom_AP_PM25
        Text      item=LivingRoom_AP_Allergen
        Text      item=LivingRoom_AP_PreFilter
        Text      item=LivingRoom_AP_HepaFilter
        Text      item=LivingRoom_AP_CarbonFilter
    }
    Frame label="Bedroom" {
        Switch    item=Bedroom_AP_Power
        Selection item=Bedroom_AP_Function
        Setpoint  item=Bedroom_AP_TargetHumidity minValue=40 maxValue=70 step=10
        Text      item=Bedroom_AP_Humidity
        Text      item=Bedroom_AP_Temperature
        Text      item=Bedroom_AP_WaterLevel
    }
    Frame label="Office" {
        Switch    item=Office_AP_Power
        Text      item=Office_AP_PM25
    }
}
```
