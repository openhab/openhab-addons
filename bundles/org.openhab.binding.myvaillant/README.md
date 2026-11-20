# myVAILLANT Binding

This binding connects Vaillant heating systems (e.g. aroTHERM plus heat pumps) with a sensoCOMFORT controller (VRC720) to openHAB through the myVAILLANT cloud, the backend of the myVAILLANT app.
The API client is based on [myPyllant](https://github.com/signalkraft/myPyllant).

## Limitations

- Only systems with the control identifier `tli` (sensoCOMFORT VRC720 with a VR920/VR921/VR940f gateway) are supported.
  Systems with a VRC700 (`vrc700`) or iQconnect (`scf`) controller go offline with a configuration error.
- ambiSENSE rooms, ventilation units and EEBUS are not supported.
  Time programs can be read and written as JSON with the actions.
- Vaillant limits the number of requests per account (call volume quota).
  When it is exceeded, the API answers with `Out of call volume quota` for up to an hour and the binding pauses all requests until then.
  All clients of an account share the quota, e.g. the myVAILLANT app and other integrations.

## Supported Things

| Thing Type | Description                                                                                |
| ---------- | ------------------------------------------------------------------------------------------ |
| `account`  | Bridge for the myVAILLANT account, logs in and pauses requests while the quota is exceeded |
| `system`   | Bridge for one system (a home in the app), reads the whole system in one update            |
| `zone`     | A zone (rooms controlled together)                                                         |
| `circuit`  | A heating circuit with flow temperature and heating curve                                  |
| `dhw`      | Domestic hot water                                                                         |
| `device`   | A device of the system, e.g. the heat pump or the electric backup heater                   |

## Discovery

After adding the `account` bridge, its systems appear in the inbox.
After adding a `system` bridge, its zones, circuits, hot water and devices appear in the inbox.
Discovery uses the data of the regular updates and causes (almost) no additional requests.

## Thing Configuration

### `account` Bridge

| Name                    | Type    | Description                                                                                                           | Default    | Required | Advanced |
| ----------------------- | ------- | --------------------------------------------------------------------------------------------------------------------- | ---------- | -------- | -------- |
| `username`              | text    | Email address of the myVAILLANT account                                                                               | N/A        | yes      | no       |
| `password`              | text    | Password of the account                                                                                               | N/A        | yes      | no       |
| `brand`                 | text    | `vaillant`, `sdbg` (Saunier Duval), `bulex`, `glow-worm` or `demirdokum`                                              | `vaillant` | yes      | no       |
| `country`               | text    | Country of the account (Vaillant and Saunier Duval) in lower case letters, e.g. `germany`, `austria`, `unitedkingdom` | `germany`  | no       | no       |
| `refreshInterval`       | integer | Seconds between updates of the systems (at least 60)                                                                  | 300        | no       | no       |
| `energyRefreshInterval` | integer | Minutes between updates of today's energy values, 0 disables them                                                     | 60         | no       | no       |
| `fetchRts`              | boolean | Read operation time and on/off cycles of the devices (one request per update)                                         | false      | no       | no       |
| `fetchMpc`              | boolean | Read the current power of the devices (one request per update)                                                        | false      | no       | no       |
| `fetchConnectionStatus` | boolean | Read whether the system is online (one request per update)                                                            | false      | no       | yes      |
| `fetchTroubleCodes`     | boolean | Read the diagnostic trouble codes (one request per update)                                                            | false      | no       | yes      |
| `refreshDelay`          | integer | Seconds to wait after a command before updating, the API takes a while to show new values                             | 10         | no       | yes      |
| `quickVetoDuration`     | decimal | Hours a temperature set outside of manual mode (quick veto) lasts                                                     | 3          | no       | no       |
| `holidayDuration`       | integer | Days away mode lasts when switched on without an end                                                                  | 365        | no       | yes      |
| `manualCoolingDuration` | integer | Days manual cooling lasts when switched on without an end                                                             | 30         | no       | yes      |

Each update of a system costs two requests (system state, and the device list at most every 30 minutes) plus one for each `fetch...` option.
Energy values cost one request per linked energy channel and update.

### `system` Bridge

| Name       | Type | Description                              | Required |
| ---------- | ---- | ---------------------------------------- | -------- |
| `systemId` | text | ID of the system, filled in by discovery | yes      |

### `zone`, `circuit` and `dhw` Things

| Name    | Type    | Description                                                                        | Required |
| ------- | ------- | ---------------------------------------------------------------------------------- | -------- |
| `index` | integer | Index of the zone or circuit (the first one is 0), hot water usually has index 255 | yes      |

### `device` Thing

| Name         | Type | Description                              | Required |
| ------------ | ---- | ---------------------------------------- | -------- |
| `deviceUuid` | text | ID of the device, filled in by discovery | yes      |

## Channels

### `account`

| Channel        | Type   | Read/Write | Description                                |
| -------------- | ------ | ---------- | ------------------------------------------ |
| `api-requests` | Number | R          | Requests sent to Vaillant today (advanced) |

### `system`

| Channel                           | Type               | Read/Write | Description                                                                  |
| --------------------------------- | ------------------ | ---------- | ---------------------------------------------------------------------------- |
| `outdoor-temperature`             | Number:Temperature | R          | Outdoor temperature                                                          |
| `outdoor-temperature-average-24h` | Number:Temperature | R          | Average outdoor temperature of the last 24 hours                             |
| `water-pressure`                  | Number:Pressure    | R          | Water pressure of the system                                                 |
| `system-flow-temperature`         | Number:Temperature | R          | Flow temperature of the system                                               |
| `cylinder-temperature-top-dhw`    | Number:Temperature | R          | Top temperature of the hot water cylinder                                    |
| `cylinder-temperature-bottom-dhw` | Number:Temperature | R          | Bottom temperature of the hot water cylinder                                 |
| `cylinder-temperature-top-ch`     | Number:Temperature | R          | Top temperature of the heating buffer                                        |
| `cylinder-temperature-bottom-ch`  | Number:Temperature | R          | Bottom temperature of the heating buffer                                     |
| `energy-manager-state`            | String             | R          | `STANDBY`, `HEATING`, `DHW` or `COOLING`                                     |
| `system-off`                      | Switch             | R          | Whether the system is switched off                                           |
| `holiday`                         | Switch             | RW         | Away mode, `ON` starts it now for `holidayDuration` days                     |
| `holiday-start`                   | DateTime           | RW         | Start of away mode                                                           |
| `holiday-end`                     | DateTime           | RW         | End of away mode                                                             |
| `manual-cooling`                  | Switch             | RW         | Cooling for days, `ON` starts it now for `manualCoolingDuration` days        |
| `manual-cooling-start`            | DateTime           | R          | Start of manual cooling                                                      |
| `manual-cooling-end`              | DateTime           | RW         | End of manual cooling                                                        |
| `ventilation-boost`               | Switch             | RW         | Ventilation boost                                                            |
| `heating-energy-efficiency`       | Number             | R          | Heat generated per consumed electrical energy for heating today, all devices |
| `firmware-update-required`        | Switch             | R          | Whether a firmware update is required                                        |
| `online`                          | Switch             | R          | Whether the system is connected (needs `fetchConnectionStatus`)              |
| `trouble-codes`                   | String             | R          | Diagnostic trouble codes (needs `fetchTroubleCodes`)                         |
| `has-trouble-codes`               | Switch             | R          | Whether there are trouble codes (needs `fetchTroubleCodes`)                  |
| `last-update`                     | DateTime           | R          | Time of the last successful update (advanced)                                |

### `zone`

| Channel                                     | Type                 | Read/Write | Description                                                                                           |
| ------------------------------------------- | -------------------- | ---------- | ----------------------------------------------------------------------------------------------------- |
| `current-room-temperature`                  | Number:Temperature   | R          | Room temperature measured by the controller                                                           |
| `current-room-humidity`                     | Number:Dimensionless | R          | Humidity measured by the controller                                                                   |
| `desired-room-temperature-setpoint`         | Number:Temperature   | RW         | Current target temperature, a command sets the manual mode setpoint in manual mode, else a quick veto |
| `desired-room-temperature-setpoint-heating` | Number:Temperature   | R          | Current target temperature for heating                                                                |
| `desired-room-temperature-setpoint-cooling` | Number:Temperature   | R          | Current target temperature for cooling                                                                |
| `operation-mode-heating`                    | String               | RW         | `TIME_CONTROLLED`, `MANUAL` or `OFF`                                                                  |
| `operation-mode-cooling`                    | String               | RW         | `TIME_CONTROLLED`, `MANUAL` or `OFF`                                                                  |
| `manual-mode-setpoint-heating`              | Number:Temperature   | RW         | Target temperature in manual mode                                                                     |
| `setpoint-cooling`                          | Number:Temperature   | RW         | Target temperature for cooling                                                                        |
| `set-back-temperature`                      | Number:Temperature   | RW         | Temperature outside of the time windows in time controlled mode                                       |
| `heating-state`                             | String               | R          | `IDLE`, `HEATING_UP` or `COOLING_DOWN`                                                                |
| `current-special-function`                  | String               | R          | `NONE`, `QUICK_VETO`, `HOLIDAY`, `SYSTEM_OFF`, `VENTILATION_BOOST` or `ONE_DAY_BANK_HOLIDAY`          |
| `hvac-action`                               | String               | R          | `idle`, `heating` or `cooling`, the state of the zone's circuit                                       |
| `quick-veto`                                | Switch               | RW         | Quick veto, `ON` starts it with the manual mode setpoint for `quickVetoDuration` hours                |
| `quick-veto-end`                            | DateTime             | R          | End of the quick veto                                                                                 |
| `quick-veto-duration`                       | Number:Time          | RW         | Remaining time of the quick veto, a command sets a new duration                                       |
| `manual-cooling-active`                     | Switch               | R          | Whether manual cooling is active                                                                      |

### `circuit`

| Channel                                      | Type               | Read/Write | Description                                              |
| -------------------------------------------- | ------------------ | ---------- | -------------------------------------------------------- |
| `circuit-state`                              | String             | R          | `HEATING`, `COOLING` or `STANDBY`                        |
| `current-flow-temperature`                   | Number:Temperature | R          | Current flow temperature                                 |
| `flow-temperature-setpoint`                  | Number:Temperature | R          | Target flow temperature                                  |
| `heating-curve`                              | Number             | RW         | Slope of the heating curve                               |
| `heat-demand-limited-by-outside-temperature` | Number:Temperature | RW         | No heat demand above this outdoor temperature            |
| `min-flow-temperature-setpoint`              | Number:Temperature | RW         | Minimum flow temperature                                 |
| `max-flow-temperature-setpoint`              | Number:Temperature | R          | Maximum flow temperature                                 |
| `room-temperature-control-mode`              | String             | R          | Influence of the room temperature, e.g. `NON` (advanced) |
| `calculated-energy-manager-state`            | String             | R          | State of the energy manager for the circuit (advanced)   |
| `cooling-allowed`                            | Switch             | R          | Whether the circuit may cool                             |

### `dhw`

| Channel                    | Type               | Read/Write | Description                                            |
| -------------------------- | ------------------ | ---------- | ------------------------------------------------------ |
| `current-temperature`      | Number:Temperature | R          | Temperature of the hot water                           |
| `tapping-setpoint`         | Number:Temperature | RW         | Target temperature, whole degrees                      |
| `operation-mode`           | String             | RW         | `TIME_CONTROLLED`, `MANUAL` or `OFF`                   |
| `current-special-function` | String             | R          | `REGULAR`, `CYLINDER_BOOST`, `HOLIDAY` or `SYSTEM_OFF` |
| `boost`                    | Switch             | RW         | Heats the cylinder once now                            |

### `device`

| Channel                     | Type            | Read/Write | Description                                                     |
| --------------------------- | --------------- | ---------- | --------------------------------------------------------------- |
| `current-power`             | Number:Power    | R          | Current electrical power (needs `fetchMpc`)                     |
| `operation-time`            | Number:Time     | R          | Total operation time in hours (needs `fetchRts`)                |
| `on-off-cycles`             | Number          | R          | Total number of starts (needs `fetchRts`)                       |
| `water-pressure`            | Number:Pressure | R          | Water pressure measured by the device, if it has a sensor       |
| `heating-energy-efficiency` | Number          | R          | Heat generated per consumed electrical energy for heating today |

In addition, the binding adds a `Number:Energy` channel with today's value for every energy value the device reports, named after value type and operation mode, e.g.

- `consumed-electrical-energy-heating`, `consumed-electrical-energy-domestic-hot-water`, `consumed-electrical-energy-cooling`
- `heat-generated-heating`, `heat-generated-domestic-hot-water`
- `earned-environment-energy-heating`, `earned-environment-energy-domestic-hot-water`
- `consumed-primary-energy-heating` (gas boilers)

Today means since midnight in the time zone of the system.
Only linked energy channels are updated.

## Actions

The actions are available for rules as `actions.thingActions('myvaillant', '<thing UID>')` (JavaScript) or `getActions("myvaillant", "<thing UID>")` (DSL).

### `system`

| Action                                                    | Description                                                              |
| --------------------------------------------------------- | ------------------------------------------------------------------------ |
| `exportEnergyData(resolution, start, end, operationMode)` | Energy values of all devices as JSON, `null` on errors                   |
| `setHoliday(start, end)`                                  | Sets away mode, empty start means now, empty end the configured duration |
| `cancelHoliday()`                                         | Ends away mode                                                           |
| `setCoolingForDays(start, end)`                           | Cools manually between start and end                                     |
| `cancelCoolingForDays()`                                  | Ends manual cooling                                                      |
| `refresh()`                                               | Updates the system after the refresh delay                               |

`resolution` is `HOUR` (at most 3 days per call), `DAY` (at most 60 days) or `MONTH`.
`start` and `end` are dates like `2026-11-01`, `2026-11-01 00:00:00` or ISO dates, without offset they are in the time zone of the system.
`operationMode` limits the export to `HEATING`, `DOMESTIC_HOT_WATER` or `COOLING`; every device and value type costs one request, so limit it where possible.

The result is a JSON array with one entry per device:

```json
[
  {
    "device": {
      "device_uuid": "...",
      "name": "aroTHERM plus",
      "device_type": "HEATPUMP",
      "type": "primary_heat_generator"
    },
    "data": [
      {
        "operation_mode": "HEATING",
        "energy_type": "CONSUMED_ELECTRICAL_ENERGY",
        "resolution": "DAY",
        "total_consumption": 33600.0,
        "data": [
          {
            "start_date": "2026-11-12T01:00:00+01:00",
            "end_date": "2026-11-13T01:00:00+01:00",
            "value": 33600.0
          }
        ]
      }
    ]
  }
]
```

Values are in Wh.

### `zone`

| Action                                     | Description                                                             |
| ------------------------------------------ | ----------------------------------------------------------------------- |
| `setQuickVeto(temperature, durationHours)` | Starts or changes a quick veto, empty duration uses `quickVetoDuration` |
| `cancelQuickVeto()`                        | Ends the quick veto                                                     |
| `setManualModeSetpoint(temperature, type)` | Sets the manual mode setpoint, type `HEATING` (default) or `COOLING`    |
| `getTimeProgram(type)`                     | Time program as JSON, type `heating` (default) or `cooling`             |
| `setTimeProgram(type, timeProgram)`        | Sets the time program from JSON in the format of `getTimeProgram`       |

Time programs contain the weekdays `monday` ... `sunday`, each with a list of slots with `startTime` and `endTime` in minutes after midnight and `setpoint` in °C.

### `dhw`

| Action                                   | Description                            |
| ---------------------------------------- | -------------------------------------- |
| `getTimeProgram()`                       | Hot water time program as JSON         |
| `setTimeProgram(timeProgram)`            | Sets the hot water time program        |
| `getCirculationTimeProgram()`            | Circulation pump time program as JSON  |
| `setCirculationTimeProgram(timeProgram)` | Sets the circulation pump time program |

## Full Example

### `demo.things`

```java
Bridge myvaillant:account:vaillant "myVAILLANT" [ username="user@example.com", password="secret", brand="vaillant", country="germany", refreshInterval=300, fetchRts=true, fetchMpc=true ] {
    Bridge system home "Heat Pump" [ systemId="0123456789abcdef" ] {
        Thing zone zone0 "Heat Pump Zone" [ index=0 ]
        Thing circuit circuit0 "Heat Pump Circuit" [ index=0 ]
        Thing device primary-heat-generator "Heat Pump" [ deviceUuid="..." ]
        Thing device electric-backup-heater "Backup Heater" [ deviceUuid="..." ]
    }
}
```

### `demo.items`

```java
Number:Temperature Outdoor_Temperature "Outdoor [%.1f °C]"         { channel="myvaillant:system:vaillant:home:outdoor-temperature" }
Number:Temperature Room_Target         "Target [%.1f °C]"          { channel="myvaillant:zone:vaillant:home:zone0:desired-room-temperature-setpoint" }
String             Heating_Mode        "Mode [%s]"                 { channel="myvaillant:zone:vaillant:home:zone0:operation-mode-heating" }
Number:Temperature Flow_Temperature    "Flow [%.1f °C]"            { channel="myvaillant:circuit:vaillant:home:circuit0:current-flow-temperature" }
Number:Power       HeatPump_Power      "Power [%d W]"              { channel="myvaillant:device:vaillant:home:primary-heat-generator:current-power" }
Number:Energy      HeatPump_Heating    "Heating today [%.1f kWh]"  { channel="myvaillant:device:vaillant:home:primary-heat-generator:consumed-electrical-energy-heating" }
```

### `demo.js`

```javascript
const vaillant = actions.thingActions(
  "myvaillant",
  "myvaillant:system:vaillant:home",
);
const json = vaillant.exportEnergyData(
  "DAY",
  "2026-11-01",
  "2026-11-20 23:59:59",
  "HEATING",
);
if (json !== null) {
  JSON.parse(json).forEach((device) =>
    console.info(device.device.name, device.data.length),
  );
}
```
