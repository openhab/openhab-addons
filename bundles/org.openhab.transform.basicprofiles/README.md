# Basic Profiles

This bundle provides a list of useful Profiles.

## Generic Command Profile

This Profile can be used to send a Command towards the Item when one event of a specified event list is triggered.
The given Command value is parsed either to `IncreaseDecreaseType`, `NextPreviousType`, `OnOffType`, `PlayPauseType`, `RewindFastforwardType`, `StopMoveType`, `UpDownType` or a `StringType` is used.

### Configuration

| Configuration Parameter | Type | Description                                                                      |
|-------------------------|------|----------------------------------------------------------------------------------|
| `events`                | text | Comma separated list of events to which the profile should listen. **mandatory** |
| `command`               | text | Command which should be sent if the event is triggered. **mandatory**            |

### Full Example

```java
Switch lightsStatus {
    channel="hue:0200:XXX:1:color",
    channel="deconz:switch:YYY:1:buttonevent" [profile="basic-profiles:generic-command", events="1002,1003", command="ON"]
}
```

## Generic Toggle Switch Profile

The Generic Toggle Switch Profile is a specialization of the Generic Command Profile and toggles the State of a Switch Item whenever one of the specified events is triggered.

### Configuration

| Configuration Parameter | Type | Description                                                                      |
|-------------------------|------|----------------------------------------------------------------------------------|
| `events`                | text | Comma separated list of events to which the profile should listen. **mandatory** |

### Full Example

```java
Switch lightsStatus {
    channel="hue:0200:XXX:1:color",
    channel="deconz:switch:YYY:1:buttonevent" [profile="basic-profiles:toggle-switch", events="1002,1003"]
}
```

## Debounce (Counting) Profile

This Profile counts and skips a user-defined number of State changes before it sends an update to the Item.
It can be used to debounce Item States.

### Configuration

| Configuration Parameter | Type    | Description                                   |
|-------------------------|---------|-----------------------------------------------|
| `numberOfChanges`       | integer | Number of changes before updating Item State. |

### Full Example

```java
Switch debouncedSwitch { channel="xxx" [profile="basic-profiles:debounce-counting", numberOfChanges=2] }
```

## Debounce (Time) Profile

In `LAST` mode this profile delays commands or state updates for a configured number of milliseconds and only send the value if no other value is received with that timespan.
In `FIRST` mode this profile discards values for the configured time after a value is sent.

It can be used to debounce Item States/Commands or prevent excessive load on networks.

### Configuration

| Configuration Parameter | Type    | Description                                   |
|-------------------------|---------|-----------------------------------------------|
| `toItemDelay`           | integer | Timespan in ms before a received value is send to the item. |
| `toHandlerDelay`        | integer | Timespan in ms before a received command is passed to the handler. |
| `mode`                  | text    | `FIRST` (sends the first value received and discards later values), `LAST` (sends the last value received, discarding earlier values). |

### Full Example

```java
Number:Temperature debouncedSetpoint { channel="xxx" [profile="basic-profiles:debounce-time", toHandlerDelay=1000] }
```

## Invert / Negate Profile

The Invert / Negate Profile inverts or negates a Command / State.
It requires no specific configuration.

The values of `QuantityType`, `PercentType` and `DecimalTypes` are negated (multiplied by `-1`).
Otherwise the following mapping is used:
 
`IncreaseDecreaseType`: `INCREASE` <-> `DECREASE`
`NextPreviousType`: `NEXT` <-> `PREVIOUS`
`OnOffType`: `ON` <-> `OFF`
`OpenClosedType`: `OPEN` <-> `CLOSED`
`PlayPauseType`: `PLAY` <-> `PAUSE`
`RewindFastforwardType`: `REWIND` <-> `FASTFORWARD`
`StopMoveType`: `MOVE` <-> `STOP`
`UpDownType`: `UP` <-> `DOWN`

### Full Example

```java
Switch invertedSwitch { channel="xxx" [profile="basic-profiles:invert"] }
```

## Round Profile

The Round Profile scales the State to a specific number of decimal places based on the power of ten.
Optionally the [Rounding mode](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/math/RoundingMode.html) can be set.
Source Channels should accept Item Type `Number`.

### Configuration

| Configuration Parameter | Type    | Description                                                                                                     |
|-------------------------|---------|-----------------------------------------------------------------------------------------------------------------|
| `scale`                 | integer | Scale to indicate the resulting number of decimal places (min: -16, max: 16, STEP: 1) **mandatory**.            |
| `mode`                  | text    | Rounding mode to be used (e.g. "UP", "DOWN", "CEILING", "FLOOR", "HALF_UP" or "HALF_DOWN" (default: "HALF_UP"). |

### Full Example

```java
Number roundedNumber { channel="xxx" [profile="basic-profiles:round", scale=0] }
Number:Temperature roundedTemperature { channel="xxx" [profile="basic-profiles:round", scale=1] }
```

## Threshold Profile

The Threshold Profile triggers `ON` or `OFF` behavior when being linked to a Switch item if value is below a given threshold (default: 10).
A good use case for this Profile is a battery low indication.
Source Channels should accept Item Type `Dimmer` or `Number`.

::: tip Note
This profile is a shortcut for the System Hysteresis Profile.
:::

### Configuration

| Configuration Parameter | Type    | Description                                                                                         |
|-------------------------|---------|-----------------------------------------------------------------------------------------------------|
| `threshold`             | integer | Triggers `ON` if value is below the given threshold, otherwise OFF (default: 10, min: 0, max: 100). |

### Full Example

```java
Switch thresholdItem { channel="xxx" [profile="basic-profiles:threshold", threshold=15] }
```

## Time Range Command Profile

This is an enhanced implementation of a follow profile which converts `OnOffType` to a `PercentType`.
The value of the percent type can be different between a specific time of the day.
A possible use-case is switching lights (using a presence detector) with different intensities at day and at night.
Be aware: a range beyond midnight (e.g. start="23:00", end="01:00") is not yet supported.

### Configuration

| Configuration Parameter | Type    | Description                                                                                                                                       |
|-------------------------|---------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| `inRangeValue`          | integer | The value which will be send when the profile detects ON and current time is between start time and end time (default: 100, min: 0, max: 100).    |
| `outOfRangeValue`       | integer | The value which will be send when the profile detects ON and current time is NOT between start time and end time (default: 30, min: 0, max: 100). |
| `start`                 | text    | The start time of the day (hh:mm).                                                                                                                |
| `end`                   | text    | The end time of the day (hh:mm).                                                                                                                  |
| `restoreValue`          | text    | Select what should happen when the profile detects OFF again (default: OFF).                                                                      |

Possible values for parameter `restoreValue`:

- `OFF` - Turn the light off
- `NOTHING` - Do nothing
- `PREVIOUS` - Return to previous value
- `0` - `100` - Set a user-defined percent value

### Full Example

```java
Switch motionSensorFirstFloor {
    channel="deconz:presencesensor:XXX:YYY:presence",
    channel="deconz:colortemperaturelight:AAA:BBB:brightness" [profile="basic-profiles:time-range-command", inRangeValue=100, outOfRangeValue=15, start="08:00", end="23:00", restoreValue="PREVIOUS"]
}
```

## State Filter Profile

This filter passes on state updates from a (binding) handler to the item if and only if all listed item state conditions
are met (conditions are ANDed together).
Option to instead pass different state update in case the conditions are not met.
State values may be quoted to treat as `StringType`.

Use case: Ignore values from a binding unless some other item(s) have a specific state.

### Configuration

| Configuration Parameter | Type | Description                                                                                                                                                                                       |
|-------------------------|------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `conditions`            | text | Comma separated list of expressions on the format `ITEM_NAME OPERATOR ITEM_STATE`, ie `MyItem EQ OFF`. Use quotes around `ITEM_STATE` to treat value as string ie `'OFF'` and not `OnOffType.OFF` |
| `mismatchState`         | text | Optional state to pass instead if conditions are NOT met. Use single quotes to treat as `StringType`. Defaults to `UNDEF`                                                                         |
| `separator`             | text | Optional separator string to separate expressions when using multiple. Defaults to `,`                                                                                                            |

Possible values for token `OPERATOR` in `conditions`:

- `EQ` - Equals
- `NEQ` - Not equals


### Full Example

```Java
Number:Temperature airconTemperature{
        channel="mybinding:mything:mychannel"[profile="basic-profiles:state-filter",conditions="airconPower_item EQ ON",mismatchState="UNDEF"]
}
```
