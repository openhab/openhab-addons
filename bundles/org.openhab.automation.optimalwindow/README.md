# Optimal Window Automation

Run devices during the cheapest, cleanest or sunniest period.

This automation finds the best time window in a forecast according to the specified goal and switches a device on when the window starts and off when it ends.
It works with any `Number` Item that has a forecast, for example:

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

```text
price
  |  ##
  |  ##        ##                       ##
  |  ##        ##        ##             ##
  |  ##   ##   ##        ##        ##   ##
  |  ##   ##   ##   ##   ##   ##   ##   ##
  +------------------------------------------> time
     22   23   00   01   02   03   04   05
   |<---------- range: 22:00, 8h --------->|

consecutive=true:  one block of 4 hours, 01:00 to 05:00
                    ##   ##   ##   ##
consecutive=false: the 4 cheapest hours, 23:00, 01:00, 03:00 and 04:00
          ##        ##        ##   ##
```

- With `consecutive=false`, the window can be split into several parts, e.g. the 4 cheapest single hours.
- With `goal=maximum`, the window with the highest values is found, e.g. the most PV power.
- With `preferStart=true`, consecutive windows that start with the best values are preferred. This suits devices that often finish early, like a boiler or a car charger. It has no effect with `consecutive=false`.

Times and lengths can be given in minutes, they don't have to match the forecast, e.g. `length=1h40m` with 15-minute prices.

## Modules

### Trigger "an optimal window starts or ends"

Fires when the window starts and when it ends.
With `consecutive=false`, it fires at the start and end of each part.

#### Window Inputs

| Parameter          | Type            | Required | Description                                                                                 | Default   |
| ------------------ | --------------- | -------- | ------------------------------------------------------------------------------------------- | --------- |
| forecastItem       | Number Item     | x        | Item with the forecast, must have a forecast persistence strategy applied                   |           |
| rangeStart         | Text (time)     |          | Start of the range to search the window in, e.g. `22:00`                                    | `00:00`   |
| rangeDuration      | Text (duration) |          | Duration of the range that is searched for the window, e.g. `8h` or `10h30m`, at most `48h` | `24h`     |
| length             | Text (duration) | x        | Length of the window to search for, e.g. `3h`, `45m` or `1h30m`                             |           |
| consecutive        | Boolean         |          | `true` for one block, `false` to allow several parts                                        | `true`    |
| goal               | Text            |          | `minimum` for the lowest values, e.g. prices, `maximum` for the highest values              | `minimum` |
| preferStart        | Boolean         |          | Prefer windows that start with the best values, only with `consecutive=true` (advanced)     | `false`   |
| persistenceService | Text            |          | Persistence service with the forecast, the default service if empty (advanced)              |           |

#### Status Items (Outputs)

These optional parameters select the Items that receive the calculated status, e.g. to show it in the UI.

| Parameter      | Type             | Description                                                      |
| -------------- | ---------------- | ---------------------------------------------------------------- |
| activeItem     | Switch Item      | `ON` while the window is active, otherwise `OFF`                 |
| startItem      | DateTime Item    | Start of the window                                              |
| endItem        | DateTime Item    | End of the window                                                |
| countdownItem  | Number:Time Item | Time until the window starts                                     |
| remainingItem  | Number:Time Item | Time until the window (or the current part) ends                 |
| windowTextItem | String Item      | Window as text, e.g. `01:00–05:00`                               |
| averageItem    | Number Item      | Average forecast value within the window, e.g. the average price |

They are updated, not commanded, so the `activeItem` does not switch a device.
To switch a device, add an action that sends a command, or use the rule template, see [How a Rule Is Built](#how-a-rule-is-built).
Persist the `activeItem` with the `forecast` strategy to see the planned window in a chart next to the prices.

#### Trigger Outputs

These values are passed to the rule's conditions and actions, e.g. for scripts:

| Output      | Description                                     |
| ----------- | ----------------------------------------------- |
| windowEvent | `START` or `END`                                |
| command     | `ON` when the window starts, `OFF` when it ends |
| start       | Start of the window                             |
| end         | End of the window                               |
| average     | Average forecast value within the window        |

### How a Rule Is Built

| Part      | What it does                                                                    |
| --------- | ------------------------------------------------------------------------------- |
| Trigger   | finds the window, fires at its start and end, updates the optional status Items |
| Condition | optional, can stop the actions, e.g. during holidays                            |
| Action    | switches the device, e.g. "send a command" to the device Item                   |

The outputs of the trigger reach the other modules of the rule:

- "send a command" (`core.ItemCommandAction`): leave the command empty.
  The action then sends what the trigger reports: `ON` when the window starts and `OFF` when it ends.
- In script conditions and script actions, the outputs are variables: `command`, `windowEvent`, `start`, `end` and `average`.

A rule without an action is fine, too.
It only updates the status Items, e.g. to show the cheapest time for a device that is started by hand.

A condition stops `ON` and `OFF` alike.
To skip only the start, check `command` in a script condition.
This rule does not switch on the boiler during holidays, but always switches it off:

```yaml
conditions:
  - id: "3"
    type: script.ScriptCondition
    config:
      type: application/javascript
      script: |
        !(command.toString() === 'ON' && items.Holiday.state === 'ON')
actions:
  - id: "2"
    type: core.ItemCommandAction
    config:
      itemName: Boiler_Power
```

### Condition "it is within the optimal window"

Use the condition in a rule with a different trigger, to let that rule only run during the optimal window.
The rule fires as usual, e.g. when an Item changes, and the condition checks whether the current time is within the window.

The condition calculates the window on its own, with the same parameters as the trigger (without the status Items).
It does not need a rule with the trigger.

For example, start the dishwasher when there is PV surplus, but only during the 3 cheapest hours between 10:00 and 18:00:

```yaml
triggers:
  - id: "1"
    type: core.ItemStateChangeTrigger
    config:
      itemName: PV_Surplus
      state: "ON"
conditions:
  - id: "2"
    type: optimalwindow.InWindowCondition
    config:
      forecastItem: Electricity_Price
      rangeStart: "10:00"
      rangeDuration: 8h
      length: 3h
actions:
  - id: "3"
    type: core.ItemCommandAction
    config:
      itemName: Dishwasher_Start
      command: "ON"
```

### Rule Template "Run during optimal window"

Creates a rule with the trigger and one action that switches an Item `ON` when the window starts and `OFF` when it ends, see [Quick Start](#quick-start).

The template has one parameter of its own: `targetItem`, the Item to switch, e.g. the switch of the device (required).
All other parameters are the ones of the [trigger](#trigger-an-optimal-window-starts-or-ends): the window parameters, and the optional status Items under the advanced settings.

The template passes the values to the trigger.
So the trigger of a rule created from the template shows the same values again, they only need to be set in the template.

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

### Rule in a YAML File

The same rule as a file in `conf/yaml/`, e.g. `optimalwindow.yaml`:

```yaml
version: 1
rules:
  car_loader:
    label: Charge the car in the cheapest window
    triggers:
      - id: "1"
        type: optimalwindow.WindowTrigger
        config:
          forecastItem: Electricity_Price
          rangeStart: "22:00"
          rangeDuration: 8h
          length: 4h
          activeItem: CarLoader_Active
          windowTextItem: CarLoader_Window
    conditions: []
    actions:
      - id: "2"
        type: core.ItemCommandAction
        config:
          itemName: CarLoader_Power
```

Quote times like `"22:00"`, YAML can read an unquoted `22:00` as a number.

Or with the rule template, only the parameters:

```yaml
version: 1
rules:
  car_loader:
    template: OptimalWindowRuleTemplate
    label: Charge the car in the cheapest window
    config:
      targetItem: CarLoader_Power
      forecastItem: Electricity_Price
      rangeStart: "22:00"
      rangeDuration: 8h
      length: 4h
      activeItem: CarLoader_Active
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
