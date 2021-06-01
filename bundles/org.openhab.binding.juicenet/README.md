# JuiceNet Binding

The JuiceNet binding will interface with the cloud portal to get status and manage your JuiceBox EV charger(s). In addition to getting the status of various items from the ev-charger, it is also possible to start and stop charging sessions.

## Supported Things

This binding supports the following things:

| thing  | type   | description                  |
|----------|--------|------------------------------|
| JuiceNet Account  | Bridge | This represents the cloud account to interface with the JuiceNet API.  |
| JuiceBox EV-Charger | Device | This interfaces to a specific JuiceBox EV-charger associated with the JuiceNet account. |

This binding should work with multiple JuiceBox EV-chargers associated with the account, however it is currently only tested with a single EV-charger.

## Thing Configuration

The only configuration required is to create a JuiceNet account thing and fill in the appropriate API Token.  The API token can be found on the Account page at https://home.juice.net/Manage.

Once, the JuiceNet Account thing has been created, all JuiceBox EV-chargers associated with that account will be discovered and added to the inbox.

## Channels

| channel           | type              | read-only | description                  |
|----------         |--------           |------------------------------|
| charging_state    | String            | N         | Current charging state (Start Charging, Smart Charging, Stop Charging). |
| state             | String            | Y         | This is the current device state (Available, Plugged-In, Charging, Error, Disconnected).  |
| override          | Switch            | Y         | Smart charging is overridden. |
| charging_time_left | Number:Time      | Y         | Charging time left (seconds). |
| plug_unplug_time  | DateTime          | Y         | Last time of either plug-in or plug-out. |
| target_time       | DateTime          | N         | “Start charging” start time, or time to start when overriding smart charging. |
| unit_time         | DateTime          | Y         | Current time on the unit. |
| temperature       | Number:Temperature | Y        | Current temperature at the unit. |
| amps_limit        | Number:ElectricCurrent | N    | Max charging current allowed. (A) |
| amps_current      | Number:ElectricCurrent | Y    | Current charging current. (A) |
| voltage           | Number:ElectricPotential | Y  | Current voltage. (V) |
| wh_energy         | Number:Energy     | Y         | Current amount of energy poured to the vehicle. (Wh) |
| savings           | Number            | Y         | Current session EV savings. |
| watt_power        | Number:Power      | Y         | Current charging power. (W) |
| seconds_charging  | Number:Time       | Y         | Charging time since plug-in time. (s) |
| wh_energy_at_plugin | Number:Energy   | Y         | Energy value at the plugging time. (Wh) |
| wh_energy_to_add  | Number:Energy     | N         | Amount of energy to be added in current session. (Wh) |
| lifetime_wh_energy_type | Number:Energy | Y |     | Total energy poured to vehicles during lifetime. (Wh) |
| lifetime_savings  | Number            | Y         | EV driving saving during lifetime. |
| gascost           | Number            | Y         | Cost of gasoline used in savings calculations. |
| mpg               | Number            | Y         | Miles per gallon used in savings calculations. |
| ecost             | Number            | Y         | Cost of electricity from utility company. ($/kWh) |
| whpermile         | Number            | Y         | Watts per mile. |
| car_description   | String            | Y         | Car description of vehicle currently or last charged. |
| car_battery_size_wh   | Number:Energy | Y         | Car battery pack size. (Wh) |
| car_battery_range_m   | Number:Length | Y         | Car mileage range. (miles) |
| car_charging_rate_w   | Number:Power  | Y         | Car charging rate. (W) |

## Using the binding

<TODO>

## Widget

The following custom widget can be used with this binding.

![JuiceBox Widget](images/widget.png)

```
uid: widget_JuiceBox
tags: []
props:
  parameters:
    - description: Prefix for the items with the data
      label: Item prefix
      name: prefix
      required: false
      type: TEXT
  parameterGroups: []
timestamp: May 10, 2021, 2:38:55 PM
component: f7-card
config:
  title: =items[props.prefix + "_Name"].state
  style:
    border-radius: var(--f7-card-expandable-border-radius)
    --f7-card-header-border-color: none
slots:
  default:
    - component: f7-card-content
      slots:
        default:
          - component: f7-row
            config:
              class:
                - display-flex
                - align-content-stretch
                - align-items-center
            slots:
              default:
                - component: f7-gauge
                  config:
                    type: semicircle
                    size: 270
                    value: =Number.parseFloat(items[props.prefix + "_CurrentEnergy"].state) / Number.parseFloat(items[props.prefix + "_CarBatteryPackSize"].state)
                    bg-color: transparent
                    border-bg-color: '=(items[props.prefix + "_DeviceState"].state === "charging") ? "#577543" : (items[props.prefix + "_DeviceState"].state === "plugged") ? "#8f6c2f" : "#595959"'
                    border-color: '=(items[props.prefix + "_DeviceState"].state === "charging") ? "#90d164" : (items[props.prefix + "_DeviceState"].state === "plugged") ? "#ed9c11" : "#adadad"'
                    borderWidth: 40
                    value-text: =items[props.prefix + "_CurrentEnergy"].displayState
                    value-text-color: '=(items[props.prefix + "_DeviceState"].state === "charging") ? "#90d164" : (items[props.prefix + "_DeviceState"].state === "plugged") ? "#ed9c11" : "#adadad"'
                    value-font-size: 20
                    value-font-weight: 500
                    label-text: =items[props.prefix + "_DeviceState"].displayState
                    label-text-color: white
                    label-font-size: 18
                    label-font-weight: 400
                    noBorder: true
                    outline: true
          - component: f7-row
            config:
              class:
                - display-flex
                - justify-content-center
                - align-content-stretch
                - align-items-center
                - margin-left
            slots:
              default:
                - component: f7-segmented
                  config:
                    strong: true
                    style:
                      width: 80%
                  slots:
                    default:
                      - component: oh-button
                        config:
                          text: Start
                          color: blue
                          size: 24
                          active: =(items[props.prefix + "_ChargingState"].state === "start")
                          action: command
                          actionItem: =props.prefix + "_ChargingState"
                          actionCommand: start
                      - component: oh-button
                        config:
                          text: Smart
                          color: blue
                          size: 24
                          active: =(items[props.prefix + "_ChargingState"].state === 'smart')
                          action: command
                          actionItem: =props.prefix + "_ChargingState"
                          actionCommand: smart
                      - component: oh-button
                        config:
                          text: Stop
                          color: blue
                          size: 24
                          active: =(items[props.prefix + "_ChargingState"].state === "stop")
                          action: command
                          actionItem: =props.prefix + "_ChargingState"
                          actionCommand: stop
          - component: f7-row
            config:
              class:
                - display-flex
                - justify-content-space-evenly
                - align-content-stretch
                - align-items-center
                - height: 40px
              style:
                --f7-chip-font-size: 14px
                --f7-chip-height: 28px
                padding-top: 12px
            slots:
              default:
                - component: f7-chip
                  config:
                    visible: =(items[props.prefix + "_DeviceState"].state === "charging")
                    text: '="Power: " + items[props.prefix + "_Power"].state'
                    iconF7: bolt_fill
                    media-bg-color: blue
                    bg-color: gray
                    label: hello
                    style:
                      padding-rightc: 12px
                - component: f7-chip
                  config:
                    visible: =(items[props.prefix + "_DeviceState"].state === "charging")
                    text: '="Current: " + items[props.prefix + "_Current"].state'
                    iconF7: arrow_up_circl
                    media-bg-color: blue
                    bg-color: gray
                - component: f7-chip
                  config:
                    text: '="Voltage: " + items[props.prefix + "_Voltage"].state'
                    iconF7: plusminus
                    media-bg-color: blue
                    bg-color: gray
                - component: f7-chip
                  config:
                    visible: =(items[props.prefix + "_ChargingState"].state === 'smart')
                    text: '="Charge at: " + items[props.prefix + "_TargetTime"].displayState'
                    iconF7: clock
                    media-bg-color: blue
                    bg-color: gray
                - component: f7-chip
                  config:
                    visible: =(items[props.prefix + "_DeviceState"].state === 'charging')
                    text: '="Charge Time Left: " + items[props.prefix + "_ChargingTimeLeft"].displayState'
                    iconF7: timer
                    media-bg-color: blue
                    bg-color: gray
    - component: f7-card-footer
      slots:
        default:
          - component: Label
            config:
              text: =items[props.prefix + "_CarDescription"].state
```

