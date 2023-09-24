# tado° Binding

The tado° binding integrates devices from [tado°](https://www.tado.com).

It requires a fully functional tado° installation.
You can then monitor and control all zone types (Heating, AC, Hot Water) as well as retrieve the HOME/AWAY status of mobile devices, and setting the HOME/AWAY status of your home.

## `home` Thing (the Bridge)

The binding supports discovery, but a `home` thing type has to be configured first.
It serves as bridge to the tado° cloud services.

Parameter | Required | Description
-|-|-
`username` | yes | Username used to log in at [my.tado](https://my.tado.com)
`password` | yes | Password of the username

Example `tado.things`

```java
Bridge tado:home:demo [ username="mail@example.com", password="secret" ]
```

Afterwards the discovery will show all zones and mobile devices associated with the user's home.

### Channels

Name | Type | Description | Read/Write
-|-|-|-
`homePresence` | String | Current presence value of the tado home. `HOME` and `AWAY` can be set | RW

## `zone` Thing

A _zone_ is an area/room of your home.
You have defined them during installation.
One zone relates to one page in the tado° mobile- or webapp.

Parameter | Required | Description | Default
-|-|-|-
`id` | yes | Zone Id | -
`refreshInterval` | no | Refresh interval of state updates in seconds | 30
`hvacChangeDebounce` | no | Duration in seconds to combine multiple HVAC changes into one | 5

Zones can either be added through discovery or manually. Following up on the above example, a zone configuration could look like this:

Example `tado.things`

```java
Bridge tado:home:demo [ username="mail@example.com", password="secret" ] {
  zone heating [id=1]
  zone ac [id=2]
  zone hotwater [id=0]
}
```

Zone id and name can be found in discovery results.

### Channels

A zone is either of type `HEATING`, `AC` or `DHW` (domestic hot water).
The availability of items as well as their allowed values depend on type and capabilities of the HVAC setup.
If you are unsure, have a look at the tado° app and see if the functionality is available and what values are supported.

Name | Type | Description | Read/Write | Zone type
-|-|-|-|-
`currentTemperature` | Number:Temperature | Current inside temperature | R | `HEATING`, `AC`
`humidity` | Number | Current relative inside humidity in percent | R | `HEATING`, `AC`
`hvacMode` | String | Active mode, one of `OFF`, `HEAT`, `COOL`, `DRY`, `FAN`, `AUTO` | RW | `HEATING` and `DHW` support `OFF` and `HEAT`, `AC` can support more
`targetTemperature` | Number:Temperature | Set point | RW | `HEATING`, `AC`, `DHW`
`operationMode` | String | Operation mode the zone is currently in. One of `SCHEDULE` (follow smart schedule), `MANUAL` (override until ended manually), `TIMER` (override for a given time), `UNTIL_CHANGE` (active until next smart schedule block or until AWAY mode becomes active) | RW | `HEATING`, `AC`, `DHW`
`overlayExpiry` | DateTime | End date and time of a timer | R | `HEATING`, `AC`, `DHW`
`timerDuration` | Number | Timer duration in minutes | RW | `HEATING`, `AC`, `DHW`
`heatingPower` | Number | Amount of heating power currently present | R | `HEATING`
`acPower` | Switch | Indicates if the Air-Conditioning is Off or On | R | `AC`
`fanspeed`<sup>1)</sup> | String | Fan speed, one of `AUTO`, `LOW`, `MIDDLE`, `HIGH` | RW | `AC`
`fanLevel`<sup>1)</sup> | String | Fan speed, one of <sup>3)</sup> `AUTO`, `SILENT`, `LEVEL1`, `LEVEL2`, `LEVEL3`, `LEVEL4`, `LEVEL5` | RW | `AC`
`swing`<sup>2)</sup> | Switch | Swing on/off | RW | `AC`
`verticalSwing`<sup>2)</sup> | String | Vertical swing state, one of <sup>3)</sup> `OFF`, `ON`, `UP`, `MID_UP`, `MID`, `MID_DOWN`, `DOWN`, `AUTO` | RW | `AC`
`horizontalSwing`<sup>2)</sup> | String | Horizontal swing state, one of <sup>3)</sup> `OFF`, `ON`, `LEFT`, `MID_LEFT`, `MID`, `MID_RIGHT`, `RIGHT`, `AUTO` | RW | `AC`
`batteryLowAlarm` | Switch | A control device in the Zone has a low battery | R | Any Zone
`openWindowDetected` | Switch | An open window has been detected in the Zone | R | Any Zone
`light` | Switch | State (`ON`, `OFF`) of the control panel light | RW | `AC`

You will see some of the above mentioned Channels only if your tado° device supports the respective function.

The `RW` items are used to either override the schedule or to return to it (if `hvacMode` is set to `SCHEDULE`).

<sup>1)</sup> Simpler A/C units have fan speed settings in the range [`LOW`, `MIDDLE`, `HIGH`].
However, more sophisticated devices have settings in the range [`SILENT`, `LEVEL1`, `LEVEL2`, `LEVEL3`, `LEVEL4`].
So you need to choose the respective channel type name that matches the features of your device.

<sup>2)</sup> Simpler A/C units have a single switch type swing function that is either `ON` or `OFF`.
However, more sophisticated devices may have either a vertical swing, a horizontal swing, or both, which could also have more complex settings.
For example the horizontal swing function could simply be `ON` or it could have more complex settings in the range [`LEFT`, `MID_LEFT`, `MID`, `MID_RIGHT`, `RIGHT`].
So you need to choose the respective channel type name that matches the features of your device.

<sup>3)</sup> The _'one of'_ list contains all possible state values supported within the tado° binding.
However, in reality different A/C units might only support a **_subset_** of those values.
And indeed the subset of supported values might depend on the current state of the `acPower` and `hvacMode` channels.
In that case, if you send a channel command value to an A/C unit which does not (currently) support that particular state value, then openHAB will report a '422' run-time error in the log.

### Item Command Collection

Item changes are not immediately applied, but instead collected and only when no change is done for 5 seconds (by default - see `hvacChangeDebounce` above), the combined HVAC change is sent to the server.
This way, you can for example set a timer for 15 minutes, with target temperature 22° and mode `HEAT` in one go, without intermediate partial overrides.
It is still fine to only change one item, like setting the target temperature to 22°, but you have the opportunity to set more items and have less defaults applied.

### Default Handling

To set an override, the tado° cloud API requires a full setting (`hvacMode`, `targetTemperature`, `fanspeed`, `swing`) and a termination condition (`operationMode`, `timerDuration`).
If only some of the properties are set, the binding fills the missing pieces automatically.
It tries to keep the current state wherever possible.

If parts of the setting are missing, then the currently active zone setting is used to fill the gap. Only if the setting is not compatible with the requested change, then hard-coded defaults are applied.

- `hvacMode` is set to `HEAT` for heating and hot water, and set to `COOL` for AC
- `targetTemperature` for heating is `22°C / 72°F`, AC is set to `20°C / 68°F` and hot water to `50°C / 122°F`
- `fanspeed` is set to first supported value, for example `AUTO`
- `swing` is set to `OFF`, if supported

If the termination condition is missing, the binding first checks if an override is active.
If that is the case, the existing termination condition is used.
An existing timer, for example, just keeps running.
In case the zone is currently in smart-schedule mode and thus doesn't have a termination condition, then the default termination condition is used, as configured in the tado° app (settings -> select zone -> manual control on tado° device).

## `mobiledevice` Thing

The `mobiledevice` thing represents a smart phone that is configured for tado°. It provides access to the geotracking functionality.

Parameter | Required | Description | Default
-|-|-|-
`id` | yes | Mobile Device Id | -
`refreshInterval` | no | Refresh interval of state updates in seconds | 60

Mobile devices are part of discovery, but can also be configured manually.
It is again easiest to refer to discovery in order to find the `id`.

Example `tado.things`:

```java
Bridge tado:home:demo [ username="mail@example.com", password="secret" ] {
  mobiledevice phone [id=12345]
}
```

### Items

Name | Type | Description | Read/Write
-|-|-|-
`atHome` | Switch | ON if mobile device is in HOME mode, OFF if AWAY | R

Group `OR` can be used to define an item for _'is any device at home'_.

# Full Example

## tado.things

```java
Bridge tado:home:demo [ username="mail@example.com", password="secret" ] {
  zone heating [id=1]
  zone ac [id=2]
  zone hotwater [id=0]

  mobiledevice phone [id=12345]
}
```

## tado.items

```java
Switch             TADO_PRESENCE_home             "Tado Presence: [MAP(presence.map):%s]"               { channel="tado:home:demo:homePresence" }
Number:Temperature HEAT_inside_temperature    "Inside Temperature"      { channel="tado:zone:demo:heating:currentTemperature" }
Number             HEAT_humidity              "Humidity"                { channel="tado:zone:demo:heating:humidity" }
Number             HEAT_heating_power         "Heating Power"           { channel="tado:zone:demo:heating:heatingPower" }
String             HEAT_hvac_mode             "HVAC Mode"               { channel="tado:zone:demo:heating:hvacMode" }
Number:Temperature HEAT_target_temperature    "Set Point"               { channel="tado:zone:demo:heating:targetTemperature" }
DateTime           HEAT_overlay_expiry        "Overlay Expiry"          { channel="tado:zone:demo:heating:overlayExpiry" }
Number             HEAT_timer_duration        "Timer Duration"          { channel="tado:zone:demo:heating:timerDuration" }
String             HEAT_operation_mode        "Operation Mode"          { channel="tado:zone:demo:heating:operationMode" }

Number:Temperature AC_inside_temperature      "Inside Temperature"      { channel="tado:zone:demo:ac:currentTemperature" }
Number             AC_humidity                "Humidity"                { channel="tado:zone:demo:ac:humidity" }
String             AC_hvac_mode               "HVMode"                  { channel="tado:zone:demo:ac:hvacMode" }
Number:Temperature AC_target_temperature      "Set Point"               { channel="tado:zone:demo:ac:targetTemperature" }
String             AC_fanspeed                "Fan Speed"               { channel="tado:zone:demo:ac:fanspeed" }
Switch             AC_swing                   "Swing"                   { channel="tado:zone:demo:ac:swing" }
DateTime           AC_overlay_expiry          "Overlay Expiry"          { channel="tado:zone:demo:ac:overlayExpiry" }
Number             AC_timer_duration          "Timer Duration"          { channel="tado:zone:demo:ac:timerDuration" }
String             AC_operation_mode          "Operation Mode"          { channel="tado:zone:demo:ac:operationMode" }
Switch             AC_power                   "Air-Con Power"           { channel="tado:zone:demo:ac:acPower" }

String             DHW_hvac_mode              "HVAC Mode"               { channel="tado:zone:demo:hotwater:hvacMode" }
Number:Temperature DHW_target_temperature     "Set Point"               { channel="tado:zone:demo:hotwater:targetTemperature" }
DateTime           DHW_overlay_expiry         "Overlay Expiry"          { channel="tado:zone:demo:hotwater:overlayExpiry" }
Number             DHW_timer_duration         "Timer Duration"          { channel="tado:zone:demo:hotwater:timerDuration" }
String             DHW_operation_mode         "Operation Mode"          { channel="tado:zone:demo:hotwater:operationMode" }

Switch             Battery_Low_Alarm          "Battery Low Alarm"       { channel="tado:zone:demo:heating:batteryLowAlarm" }

Switch             Phone_atHome               "Phone location [MAP(presence.map):%s]" { channel="tado:mobiledevice:demo:phone:atHome" }
```

## tado.sitemap

```perl
sitemap tado label="Tado"
{
    Frame label="Status" {
        Switch item=TADO_PRESENCE_home  icon="presence"
    }

    Frame label="Heating" {
        Text      item=HEAT_inside_temperature
        Text      item=HEAT_humidity
        Text      item=HEAT_heating_power

        Setpoint  item=HEAT_target_temperature  minValue=5 maxValue=25
        Selection item=HEAT_hvac_mode           mappings=[OFF=off, HEAT=on]
        Selection item=HEAT_operation_mode      mappings=[SCHEDULE=schedule, MANUAL=manual, UNTIL_CHANGE="until change", TIMER=timer]
        Setpoint  item=HEAT_timer_duration      minValue=5 maxValue=60 step=1
        Text      item=HEAT_overlay_expiry
    }

    Frame label="AC" {
        Text      item=AC_inside_temperature
        Text      item=AC_humidity

        Setpoint  item=AC_target_temperature  minValue=16 maxValue=30
        Selection item=AC_hvac_mode           mappings=[OFF=off, HEAT=heat, COOL=cool, DRY=dry, FAN=fan, AUTO=auto]
        Selection item=AC_operation_mode      mappings=[SCHEDULE=schedule, MANUAL=manual, UNTIL_CHANGE="until change", TIMER=timer]
        Selection item=AC_fanspeed            mappings=[AUTO=auto, LOW=low, MIDDLE=middle, HIGH=high]
        Switch    item=AC_swing
        Setpoint  item=AC_timer_duration      minValue=5 maxValue=60 step=1
        Text      item=AC_overlay_expiry
    }

    Frame label="Hot Water" {
        Setpoint  item=DHW_target_temperature  minValue=30 maxValue=65
        Selection item=DHW_hvac_mode           mappings=[OFF=off, HEAT=on]
        Selection item=DHW_operation_mode      mappings=[SCHEDULE=schedule, MANUAL=manual, UNTIL_CHANGE="until change", TIMER=timer]
        Setpoint  item=DHW_timer_duration      minValue=5 maxValue=60 step=1
        Text      item=DHW_overlay_expiry
    }

    Frame label="Battery Low Alarm" {
        Text item=Battery_Low_Alarm valuecolor=[OFF="green", ON="red"]
    }

    Frame label="Mobile Devices" {
        Text item=Phone_atHome
    }
}
```

## presence.map

```text
ON=at home
OFF=away
NULL=lost
```
