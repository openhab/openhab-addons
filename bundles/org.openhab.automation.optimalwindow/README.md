# Optimal Window Automation

Run devices during the cheapest, cleanest or sunniest period.

This automation finds the time window with the lowest or highest values in a forecast and triggers rules when the window starts and ends.
It works with any Number Item that receives a forecast as time series, for example:

- electricity prices from the aWATTar, Energy-Charts, ENTSO-E, Energi Data Service or Tibber bindings
- CO2 intensity of the grid
- PV power forecasts, using the goal `maximum`

## Requirements

The forecast values are read from persistence.
Persist the forecast Item with the `forecast` strategy in a persistence service that can store future values, e.g. InfluxDB, JDBC or In-Memory (rrd4j cannot).

```java
Items {
    Electricity_Price : strategy = forecast
}
```

## Modules

### Trigger "an optimal window starts or ends"

Fires when the window starts and when it ends.
With `consecutive` set to `false`, the window can consist of several parts, and the trigger fires at the start and end of each part.

The window is checked every minute.
It is recalculated when the forecast Item receives a new time series, when the range moves on, and at least every 15 minutes.
Once a window has started, it is kept until the end of its range, so new forecast values cannot interrupt a running window.
If the next range starts before that, e.g. with `rangeDuration` above `24h`, the window is kept until it has ended.

When the rule starts, e.g. after a restart of openHAB or when the rule is saved, the trigger fires once with the current state (`START` or `END`).
So a device is switched off if openHAB was down when a window ended.

| Parameter          | Type    | Description                                                                                                | Default   |
| ------------------ | ------- | ---------------------------------------------------------------------------------------------------------- | --------- |
| forecastItem       | Item    | Number Item with the forecast values                                                                       | required  |
| rangeStart         | Text    | Start time of the range to search the window in, e.g. `22:00` or `06:30`                                   | `00:00`   |
| rangeDuration      | Text    | Duration of the range, e.g. `8h` or `10h30m`, at most `48h`                                                | `24h`     |
| length             | Text    | Length of the window, e.g. `3h`, `45m` or `1h30m`. Must not be longer than the range                       | required  |
| consecutive        | Boolean | Find one consecutive window. If `false`, the best intervals with a total duration of `length` are selected | `true`    |
| goal               | Text    | `minimum` for the lowest values, e.g. prices, or `maximum` for the highest values, e.g. PV power           | `minimum` |
| preferStart        | Boolean | Weight the start of a consecutive window higher, decreasing linearly towards the end (advanced)            | `false`   |
| persistenceService | Text    | Persistence service to read the forecast from. Uses the default service if empty (advanced)                |           |
| activeItem         | Item    | Optional Switch Item, `ON` while the window is active. Also receives the planned window as time series     |           |
| startItem          | Item    | Optional DateTime Item for the start of the window                                                         |           |
| endItem            | Item    | Optional DateTime Item for the end of the window                                                           |           |
| countdownItem      | Item    | Optional Number:Time Item for the time until the next start of the window                                  |           |
| remainingItem      | Item    | Optional Number:Time Item for the time until the end of the active part of the window                      |           |
| windowTextItem     | Item    | Optional String Item for the window as text, e.g. `10:45–14:45` or `02:00–04:00, 23:00–00:00`              |           |

The range is searched from `rangeStart` for `rangeDuration`.
For example, `rangeStart=22:00` and `rangeDuration=8h` searches from 22:00 to 06:00.
Times and durations are given in minutes, independent of the forecast interval.
If the range starts or ends within a forecast interval, only the part of the interval within the range is used.
The window can only be calculated when the forecast covers the whole range.
Each forecast value is valid until the next one.
If values are missing, i.e. the spacing to the next value is longer than both neighboring spacings, the forecast has a gap there, and a consecutive window cannot span it.
Day-ahead electricity prices for the next day are usually published around 13:00, so a range from 12:00 to 20:00 can only be calculated in the afternoon.

The range is calculated in local time and lasts exactly `rangeDuration`.
In the nights when daylight saving time starts or ends, a range from 22:00 lasting 8 hours therefore ends at 07:00 or 05:00 local time.

The window length does not have to be a multiple of the forecast interval.
With hourly prices and `length=90m`, the window may start or end in the middle of an hour, e.g. 02:30–04:00 if 03:00 is the cheapest hour.
With `consecutive=false`, the last selected interval is used partially, next to the other selected intervals if possible.

The `activeItem` also receives the planned window as time series, from now until the end of the range.
Persist it with the `forecast` strategy to show the planned window in a chart next to the prices.
When the window cannot be calculated any more, or the rule is disabled or removed, the planned window is replaced with `OFF`.

`preferStart` is useful for devices that often finish before the end of the window, like a boiler or a car charger.
The weight decreases linearly from the start to the end of the window, so with `length=4h` and hourly prices the four hours are weighted 7, 5, 3 and 1.
With `preferStart`, the window can also start within a forecast interval, e.g. a few minutes before a cheap hour.

The trigger has the following outputs:

| Output  | Description                                     |
| ------- | ----------------------------------------------- |
| event   | `START` or `END`                                |
| command | `ON` when the window starts, `OFF` when it ends |
| start   | Start of the window                             |
| end     | End of the window                               |
| average | Average forecast value within the window        |

The `command` output is connected automatically to an "Item Action" ("send a command") with an empty command.

### Condition "it is within the optimal window"

Satisfied while the current time is within the window.
It has the same window parameters as the trigger, without the status Items.
It doesn't keep a started window like the trigger does, so after new forecast values arrive it can differ from the trigger until the running window ends.
Use it to restrict other rules, e.g. to only start the dishwasher on PV surplus while the prices are low.

### Rule Template "Run during optimal window"

Switches a target Item `ON` when the window starts and `OFF` when it ends.
It asks for the target Item and the window parameters, and creates a rule with the trigger and an Item Action.

## Examples

### Rule in the UI

Add a rule with the trigger "an optimal window starts or ends" and the action "send a command" to the Item that should be switched, and leave the command empty.
The code tab of such a rule looks like this:

```yaml
triggers:
  - id: "1"
    configuration:
      forecastItem: Electricity_Price
      rangeStart: "22:00"
      rangeDuration: 8h
      length: 4h
      activeItem: CarLoader_Active
      windowTextItem: CarLoader_Window
      remainingItem: CarLoader_Remaining
    type: optimalwindow.WindowTrigger
conditions: []
actions:
  - id: "2"
    configuration:
      itemName: CarLoader_Power
    type: core.ItemCommandAction
```

The status Items are plain Items without a channel link:

```java
Switch      CarLoader_Active    "Car loader active"
String      CarLoader_Window    "Car loader window [%s]"
Number:Time CarLoader_Remaining "Car loader ends in [%.0f min]"
```

With hourly prices `CarLoader_Window` shows e.g. `01:00–05:00`, with quarter-hour prices e.g. `00:45–04:45`.

### Typical configurations

| Use case                                              | Configuration                                                                                    |
| ----------------------------------------------------- | ------------------------------------------------------------------------------------------------ |
| Charge the car for 4 hours during the night           | `rangeStart=22:00`, `rangeDuration=8h`, `length=4h`                                              |
| Run the heat pump during the 12 cheapest hours        | `length=12h`, `consecutive=false`                                                                |
| Heat water for up to 3.5 hours, cheapest at the start | `length=3h30m`, `preferStart=true`                                                               |
| Run the dishwasher during the highest PV forecast     | `forecastItem=PV_Forecast`, `rangeStart=08:00`, `rangeDuration=10h`, `length=3h`, `goal=maximum` |
