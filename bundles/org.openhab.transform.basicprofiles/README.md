# Basic Profiles

This bundle provides a list of useful Profiles.

## Generic Command Profile

This Profile can be used to send a Command towards the Item when one event of a specified event list is triggered.
The given Command value is parsed either to `IncreaseDecreaseType`, `NextPreviousType`, `OnOffType`, `PlayPauseType`, `RewindFastforwardType`, `StopMoveType`, `UpDownType` or a `StringType` is used.

### Generic Command Profile Configuration

| Configuration Parameter | Type | Description                                                                      |
|-------------------------|------|----------------------------------------------------------------------------------|
| `events`                | text | Comma separated list of events to which the profile should listen. **mandatory** |
| `command`               | text | Command which should be sent if the event is triggered. **mandatory**            |

### Generic Command Profile Example

```java
Switch lightsStatus {
    channel="hue:0200:XXX:1:color",
    channel="deconz:switch:YYY:1:buttonevent" [profile="basic-profiles:generic-command", events="1002,1003", command="ON"]
}
```

## Generic Toggle Switch Profile

The Generic Toggle Switch Profile is a specialization of the Generic Command Profile and toggles the State of a Switch Item whenever one of the specified events is triggered.

### Generic Toggle Switch Profile Configuration

| Configuration Parameter | Type | Description                                                                      |
|-------------------------|------|----------------------------------------------------------------------------------|
| `events`                | text | Comma separated list of events to which the profile should listen. **mandatory** |

### Generic Toggle Switch Profile Example

```java
Switch lightsStatus {
    channel="hue:0200:XXX:1:color",
    channel="deconz:switch:YYY:1:buttonevent" [profile="basic-profiles:toggle-switch", events="1002,1003"]
}
```

## Debounce (Counting) Profile

This Profile counts and skips a user-defined number of State changes before it sends an update to the Item.
It can be used to debounce Item States.

### Debounce (Counting) Profile Configuration

| Configuration Parameter | Type    | Description                                   |
|-------------------------|---------|-----------------------------------------------|
| `numberOfChanges`       | integer | Number of changes before updating Item State. |

### Debounce (Counting) Profile Example

```java
Switch debouncedSwitch { channel="xxx" [profile="basic-profiles:debounce-counting", numberOfChanges=2] }
```

## Debounce (Time) Profile

In `LAST` mode this profile delays commands or state updates for a configured number of milliseconds and only send the value if no other value is received with that timespan.
In `FIRST` mode this profile discards values for the configured time after a value is sent.

It can be used to debounce Item States/Commands or prevent excessive load on networks.

### Debounce (Time) Profile Configuration

| Configuration Parameter | Type    | Description                                   |
|-------------------------|---------|-----------------------------------------------|
| `toItemDelay`           | integer | Timespan in ms before a received value is send to the item. |
| `toHandlerDelay`        | integer | Timespan in ms before a received command is passed to the handler. |
| `mode`                  | text    | `FIRST` (sends the first value received and discards later values), `LAST` (sends the last value received, discarding earlier values). |

### Debounce (Time) Profile Example

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

### Invert / Negate Profile Example

```java
Switch invertedSwitch { channel="xxx" [profile="basic-profiles:invert"] }
```

## Round Profile

The Round Profile scales the State to a specific number of decimal places based on the power of ten.
Optionally the [Rounding mode](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/math/RoundingMode.html) can be set.
Source Channels should accept Item Type `Number`.

### Round Profile Configuration

| Configuration Parameter | Type    | Description                                                                                                     |
|-------------------------|---------|-----------------------------------------------------------------------------------------------------------------|
| `scale`                 | integer | Scale to indicate the resulting number of decimal places (min: -16, max: 16, STEP: 1) **mandatory**.            |
| `mode`                  | text    | Rounding mode to be used (e.g. "UP", "DOWN", "CEILING", "FLOOR", "HALF_UP" or "HALF_DOWN" (default: "HALF_UP"). |

### Round Profile Example

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

### Threshold Profile Configuration

| Configuration Parameter | Type    | Description                                                                                         |
|-------------------------|---------|-----------------------------------------------------------------------------------------------------|
| `threshold`             | integer | Triggers `ON` if value is below the given threshold, otherwise OFF (default: 10, min: 0, max: 100). |

### Threshold Profile Example

```java
Switch thresholdItem { channel="xxx" [profile="basic-profiles:threshold", threshold=15] }
```

## Time Range Command Profile

This is an enhanced implementation of a follow profile which converts `OnOffType` to a `PercentType`.
The value of the percent type can be different between a specific time of the day.
A possible use-case is switching lights (using a presence detector) with different intensities at day and at night.
Be aware: a range beyond midnight (e.g. start="23:00", end="01:00") is not yet supported.

### Time Range Profile Configuration

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

### Time Range Profile Example

```java
Switch motionSensorFirstFloor {
    channel="deconz:presencesensor:XXX:YYY:presence",
    channel="deconz:colortemperaturelight:AAA:BBB:brightness" [profile="basic-profiles:time-range-command", inRangeValue=100, outOfRangeValue=15, start="08:00", end="23:00", restoreValue="PREVIOUS"]
}
```

## State Filter Profile

This filter passes on state updates from the (binding) handler to the item if and only if all listed conditions are met (conditions are ANDed together).
In case the conditions are not met, a fixed predefined state can be passed to the item instead of ignoring the update.

Use cases:

- Ignore values from the binding unless some other item(s) have a specific state.
- Filter out invalid values from the binding.

### State Filter Configuration

| Configuration Parameter | Type | Description                                                                                                                                                              |
| ----------------------- | ---- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `conditions`            | text | A list of conditions to check before posting an update from the binding to the item. When all the conditions are met, the update from the binding is passed to the item. |
| `mismatchState`         | text | What to pass to the item when `conditions` aren't met. Use single quotes to treat as `StringType`. When undefined (the default), updates from the binding are ignored.   |
| `separator`             | text | Optional separator string to separate multiple expressions. Defaults to `,`.                                                                                             |

#### State Filter Conditions

The conditions are defined in the format `[ITEM_NAME] OPERATOR VALUE_OR_ITEM_NAME`, e.g. `MyItem EQ OFF`.
Multiple conditions can be entered on separate lines in the UI, or in a single line separated with the `separator` character/string.

The state of one item can be compared against the state of another item by having item names on both sides of the comparison, e.g.: `Item1 > Item2`.
When `ITEM_NAME` is omitted, e.g. `> 10, < 100`, the comparisons are applied against the input data from the binding.
In this case, the value can also be replaced with an item name, which will result in comparing the input state against the state of that item, e.g. `> LowerLimitItem, < UpperLimitItem`.
This can be used to filter out unwanted data, e.g. to ensure that incoming data are within a reasonable range.

Some tips:

- When dealing with QuantityType data, the unit must be included in the comparison value, e.g.: `PowerItem > 1 kW`.
- Use single quotes around the `VALUE` to perform a string comparison, e.g. `'UNDEF'` is not equal to `UNDEF` (of type `UnDefType`).
  This will distinguish between a string literal and an item name or a constant such as `UNDEF`, `ON`/`OFF`, `OPEN`, etc.
- `VALUE` cannot be on the left hand side of the operator.

##### State Filter Operators

| Name  |    Symbol    |                           |
| :---: | :----------: | ------------------------- |
| `EQ`  |     `==`     | Equals                    |
| `NEQ` | `!=` or `<>` | Not equals                |
| `GT`  |     `>`      | Greater than              |
| `GTE` |     `>=`     | Greater than or equals to |
| `LT`  |     `<`      | Less than                 |
| `LTE` |     `<=`     | Less than or equals to    |

Notes:

- The operator names must be surrounded by spaces, i.e.: `Item EQ 10`
- The operator symbols do not need to be surrounded by spaces, e.g.: `Item==10` and `Item == 10` are both fine.

### State Filter Examples

Condition based on the state of other items:

```java
Number:Temperature airconTemperature {
  channel="mybinding:mything:mychannel" [ profile="basic-profiles:state-filter", conditions="airconPower_item EQ ON", mismatchState="UNDEF" ]
}
```

Check against the incoming state, to discard incoming data outside a fixed range:

```java
Number:Power PowerUsage {
  channel="mybinding:mything:mychannel" [ profile="basic-profiles:state-filter", conditions=">= 0 kW", "< 20 kW" ]
}
```

The incoming state can be compared against other items:

```java
Number:Power MinimumPowerLimit { unit="W" }
Number:Power MaximumPowerLimit { unit="W" }

Number:Power PowerUsage {
  channel="mybinding:mything:mychannel" [ profile="basic-profiles:state-filter", conditions=">= MinimumPowerLimit", "< MaximumPowerLimit" ]
}
```
