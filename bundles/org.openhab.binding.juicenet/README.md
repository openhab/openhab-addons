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
| target_time       | DateTime          | Y         | “Start charging” start time, or time to start when overriding smart charging. |
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
