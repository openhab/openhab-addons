# Optimal Window Automation

Run devices during the cheapest, cleanest or sunniest period.

This automation finds the best time window in a forecast and switches a device on when the window starts and off when it ends.
It works with any Number Item that has a forecast, for example:

- electricity prices from the aWATTar, Energy-Charts, ENTSO-E, Energi Data Service or Tibber bindings
- CO2 intensity of the grid
- PV power forecasts

## Requirements

The forecast is read from persistence.
Persist the forecast Item with the `forecast` strategy in a persistence service that can store future values, e.g. InfluxDB, JDBC or In-Memory (rrd4j cannot).

```java
Items {
    Electricity_Price : strategy = forecast
}
```

## Quick Start

1. Go to **Settings → Rules → Add Rule** and choose the template "Run during optimal window".
1. Select the Item to switch, the forecast Item and the length, e.g. `4h`.
1. Save the rule.

The Item is switched `ON` when the window starts and `OFF` when it ends.

## How It Works

You define a **range**, the period to search in, and the **length** of the window.
The automation finds the window within the range with the lowest values, e.g. the cheapest prices.

For example, `rangeStart=22:00`, `rangeDuration=8h` and `length=4h` finds the cheapest 4 hours between 22:00 and 06:00.
This is repeated every day.

- With `consecutive=false`, the window can be split into several parts, e.g. the 4 cheapest single hours.
- With `goal=maximum`, the window with the highest values is found, e.g. the most PV power.
- With `preferStart=true`, windows that start with the best values are preferred. This suits devices that often finish early, like a boiler or a car charger.

Times and lengths can be given in minutes, they don't have to match the forecast, e.g. `length=1h40m` with 15-minute prices.

## Modules

### Trigger "an optimal window starts or ends"

Fires when the window starts and when it ends.
With `consecutive=false`, it fires at the start and end of each part.

| Parameter          | Description                                                                    | Default   |
| ------------------ | ------------------------------------------------------------------------------ | --------- |
| forecastItem       | Number Item with the forecast                                                  | required  |
| rangeStart         | Start of the range, e.g. `22:00`                                               | `00:00`   |
| rangeDuration      | Duration of the range, e.g. `8h` or `10h30m`, at most `48h`                    | `24h`     |
| length             | Length of the window, e.g. `3h`, `45m` or `1h30m`                              | required  |
| consecutive        | `true` for one block, `false` to allow several parts                           | `true`    |
| goal               | `minimum` for the lowest values, e.g. prices, `maximum` for the highest values | `minimum` |
| preferStart        | Prefer windows that start with the best values (advanced)                      | `false`   |
| persistenceService | Persistence service with the forecast, the default service if empty (advanced) |           |
| activeItem         | Switch Item, `ON` while the window is active                                   |           |
| startItem          | DateTime Item for the start of the window                                      |           |
| endItem            | DateTime Item for the end of the window                                        |           |
| countdownItem      | Number:Time Item for the time until the window starts                          |           |
| remainingItem      | Number:Time Item for the time until the window (or the current part) ends      |           |
| windowTextItem     | String Item for the window as text, e.g. `01:00–05:00`                         |           |

The Items at the end of the table are optional and only show the status, e.g. in the UI.
Persist the `activeItem` with the `forecast` strategy to see the planned window in a chart next to the prices.

The trigger has these outputs, e.g. for scripts:

| Output  | Description                                     |
| ------- | ----------------------------------------------- |
| event   | `START` or `END`                                |
| command | `ON` when the window starts, `OFF` when it ends |
| start   | Start of the window                             |
| end     | End of the window                               |
| average | Average forecast value within the window        |

### Condition "it is within the optimal window"

True while the current time is within the window.
It has the same parameters as the trigger, without the status Items.
Use it to restrict other rules, e.g. to only start the dishwasher on PV surplus while the prices are low.

### Rule Template "Run during optimal window"

Creates a rule that switches an Item `ON` when the window starts and `OFF` when it ends, see [Quick Start](#quick-start).

## Good to Know

- **Prices for tomorrow:** electricity day-ahead prices are usually published around 13:00. A range that reaches into the next day, e.g. 22:00 to 06:00, can only be calculated after that.
- **Running windows are not interrupted:** once a window has started, new forecast values don't change it.
- **Restarts:** after a restart of openHAB, the trigger sends the current state once. So a device is switched off if openHAB was down when the window ended.
- **Daylight saving time:** a range always lasts exactly `rangeDuration`. In the nights when the clocks change, a range from 22:00 lasting 8 hours ends at 05:00 or 07:00.
- **Gaps in the forecast:** a consecutive window is not placed across missing forecast values, see [Known Issues](#known-issues).

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

### Typical Configurations

| Use case                                              | Configuration                                                                                    |
| ----------------------------------------------------- | ------------------------------------------------------------------------------------------------ |
| Charge the car for 4 hours during the night           | `rangeStart=22:00`, `rangeDuration=8h`, `length=4h`                                              |
| Run the heat pump during the 12 cheapest hours        | `length=12h`, `consecutive=false`                                                                |
| Heat water for up to 3.5 hours, cheapest at the start | `length=3h30m`, `preferStart=true`                                                               |
| Run the dishwasher during the highest PV forecast     | `forecastItem=PV_Forecast`, `rangeStart=08:00`, `rangeDuration=10h`, `length=3h`, `goal=maximum` |

## Known Issues

- **Every second forecast value missing:** this looks the same as a forecast with a longer interval, e.g. hourly values with every second hour missing look like values every 2 hours. Such missing values are not detected, and the value before them is used for the missing time.
- **Ranges longer than 24 hours:** they overlap with the range of the next day. A window of the earlier range is always finished first, and the window of the next range is only searched after that.
- **Condition and trigger:** both calculate the window on their own. They can differ if the condition is checked for the first time while a window is already running and the forecast has changed since the window started.
