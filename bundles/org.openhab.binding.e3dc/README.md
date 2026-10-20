# E3DC Binding

This binding integrates [E3/DC](https://www.e3dc.com/) home power plants over the RSCP interface.
RSCP is the manufacturer's own protocol, so the binding controls far more than the Modbus interface
offers, including the battery power settings and the individual wallboxes.

The connection is a TCP socket on port 5033 by default and every frame is encrypted, so the RSCP
password configured on the device is required.

The cipher is Rijndael with a 256 bit block, which is outside the AES standard and therefore not
available from the Java runtime. The binding uses the BouncyCastle engine for it, because
implementations of the 256 bit block disagree about the row shifts and only one convention interoperates
with the devices.

## Supported Things

| Thing Type   | ID            | Description                                                              |
|--------------|---------------|--------------------------------------------------------------------------|
| E3/DC Device | `bridge`      | The connection to one E3/DC device. All other things require this bridge. |
| Power Plant  | `power-plant` | Power flows, battery, inverter, energy counters and system information.   |
| Wallbox      | `wallbox`     | Values of one wallbox attached to the power plant.                        |
| Power Meter  | `power-meter` | Phase values of one power meter attached to the power plant.              |
| Battery      | `battery`     | Values of one battery, such as its voltage, current and state of health. |
| Battery Module | `battery-module` | Values of one battery cell module, such as its state of health.     |

A power plant is attached to the bridge, wallboxes and power meters are attached to the bridge as
well and are distinguished by their index.

## Discovery

The RSCP interface does not announce itself on the network, so the bridge has to be added manually.

Discovery works on a bridge, so the bridge thing has to exist first.
It needs the connection to the device: a scan only finds something while the bridge is `ONLINE`.
A bridge that is still `UNKNOWN` or `OFFLINE` (wrong host, user name or RSCP password, device not reachable) is skipped, and the scan ends without a result.

How to use it:

1. Add the E3/DC bridge manually and enter host, portal user name and password and the RSCP password.
1. Wait until the bridge is `ONLINE`.
1. Open the inbox, press `+`, choose the E3/DC binding and start the scan.
   The things appear in the inbox and can be added from there.

What is proposed:

| Thing           | How it is found                                                                                          |
|-----------------|----------------------------------------------------------------------------------------------------------|
| `power-plant`   | Always, one per bridge.                                                                                  |
| `battery`       | The device is asked which batteries it has. Every battery that answers with values is proposed.          |
| `battery-module`| The cell modules that the batteries report.                                                              |
| `power-meter`   | The device reports no count. The indexes 0 to 7 are asked for, and each one that answers with values and at least one active phase is proposed. |
| `wallbox`       | The device reports no count. The indexes 0 to 7 are asked for, and each one that answers with values is proposed. |

Background discovery is switched off by default.
When it is switched on in the discovery settings of the binding, a scan runs 5 seconds after the connection to the device came up (also after every reconnect), and after that once an hour.
Things that are already added or ignored in the inbox are not proposed again.

If a thing is missing, or one is proposed that does not exist, set the log level of the binding to `DEBUG`.
The scan then logs, for every index it asked for, whether the device answered with values and the complete answer, including the answers for indexes that were not proposed.
The lines start with "Discovery scan" and "Answer for".
They show how the device answers for an index it does not have.

## Thing Configuration

### `bridge` Thing Configuration

| Name            | Type    | Description                                                      | Default | Required | Advanced |
|-----------------|---------|------------------------------------------------------------------|---------|----------|----------|
| host            | text    | Host name or IP address of the E3/DC device.                      | N/A     | yes      | no       |
| port            | integer | TCP port of the RSCP interface.                                   | 5033    | no       | yes      |
| userName        | text    | User name of the E3/DC account, usually the e-mail address.        | N/A     | yes      | no       |
| password        | text    | Password of the E3/DC account.                                    | N/A     | yes      | no       |
| rscpPassword    | text    | Encryption key configured on the device.                          | N/A     | yes      | no       |
| refreshInterval | integer | Interval the device is polled in seconds, between 1 and 300.      | 5       | no       | no       |
| historyInterval | integer | Interval the history counters are requested in minutes, between 1 and 1440. Never shorter than `refreshInterval`. | 15      | no       | no       |
| timeout         | integer | Timeout for a single request in seconds.                          | 10      | no       | yes      |

#### The credentials

The bridge needs three values, and they are made of two things only: your E3/DC account and the RSCP
key. There is no separate account on the device.

| Parameter      | What it is                                              | Where it comes from                                       |
|----------------|----------------------------------------------------------|-----------------------------------------------------------|
| `userName`     | Your E3/DC account, usually an e-mail address             | The account you use for the E3/DC portal                   |
| `password`     | The password of that E3/DC account                        | The same account                                          |
| `rscpPassword` | The key the connection is encrypted with                  | _Main Page → Personalize → User profile → RSCP password_   |

`userName` and `password` are the E3/DC account credentials, not a device local account. The
`rscpPassword` is a separate value: it encrypts the connection and is not checked against a user, so a
wrong key makes the device drop the connection instead of answering with an authentication error.

References: the [E3/DC binding by Björn Brings](https://github.com/bjoernbrings/openhab-addons/tree/E3DC)
uses the same pair of parameters and documents the account values as `Webusername` (typically the
e-mail address) and `Webpassword` (used to log into the E3/DC portal);
[rscp2mqtt](https://github.com/pvtom/rscp2mqtt) configures the same three values.

#### If the connection stays offline

| Status detail          | Meaning                                                                    |
|------------------------|----------------------------------------------------------------------------|
| `COMMUNICATION_ERROR` with `closed after 0 of 32 bytes` | The device accepted the TCP connection and closed it without answering. Usually the `rscpPassword` does not match the key on the device, so the request cannot be decrypted. |
| `COMMUNICATION_ERROR` with a timeout                   | Nothing arrived at all. The host or port is wrong, or a firewall drops the connection. |
| `COMMUNICATION_ERROR` about the credentials              | The device answered with access denied, so `userName` or `password` is wrong. |
| `BRIDGE_OFFLINE`                                        | The bridge itself is offline; the state of the bridge explains why.         |

For a protocol level trace of the handshake, raise the log level for the RSCP client:

```text
log:set TRACE org.openhab.binding.e3dc.internal.rscp
```

At `DEBUG` the log names the host, the port, the account that is being authenticated and, on failure,
whether the device stayed silent or closed the connection. Passwords and the RSCP key are never logged.

### `power-plant`, `wallbox`, `power-meter`, `battery` and `battery-module` Thing Configuration

| Thing Type    | Name         | Type    | Description                                       | Default | Required |
|---------------|--------------|---------|---------------------------------------------------|---------|----------|
| `wallbox`     | wallboxIndex | integer | Zero based index of the wallbox on the device.     | 0       | yes      |
| `power-meter` | meterIndex   | integer | Zero based index of the power meter on the device. | 0       | yes      |
| `battery`     | batteryIndex | integer | Zero based index of the battery on the device.     | 0       | yes      |
| `battery-module` | moduleIndex | integer | Zero based index of the cell module in the battery data. | N/A | yes   |

The module index counts the cell modules of all batteries consecutively, as the device reports them.

## Channels

The channels are grouped per aspect. Power values are reported in `W`, energy counters in `kWh`.

### Channel Group `power`

| Channel          | Type                 | Description                                     |
|------------------|----------------------|-------------------------------------------------|
| solar-power      | Number:Power         | Photovoltaic power currently produced            |
| battery-power    | Number:Power         | Power flowing into or out of the battery         |
| house-power      | Number:Power         | Power currently consumed by the household        |
| grid-power       | Number:Power         | Power drawn from or fed into the grid            |
| additional-power | Number:Power         | Power of an additional producer or consumer      |
| wallbox-power | Number:Power | Power drawn by all wallboxes |
| wallbox-solar-power | Number:Power | Part of the wallbox power that comes from solar |
| autarky          | Number:Dimensionless | Share of consumption covered by the power plant  |
| self-consumption | Number:Dimensionless | Share of production used on site                 |

### Channel Group `battery`

| Channel                   | Type                 | Description                                     |
|---------------------------|----------------------|-------------------------------------------------|
| soc                       | Number:Dimensionless | Battery state of charge                          |
| rsoc                      | Number:Dimensionless | State of charge the battery reports itself       |
| remaining-charge-power    | Number:Power         | Charge power still available                     |
| remaining-discharge-power | Number:Power         | Discharge power still available                  |
| max-charge-power          | Number:Power         | Configured limit for battery charging            |
| max-discharge-power       | Number:Power         | Configured limit for battery discharging         |
| discharge-start-power     | Number:Power         | Power above which the battery is discharged      |
| charge-limit-battery      | Number:Power         | Charge power limit reported by the battery       |
| charge-limit-dcdc         | Number:Power         | Charge power limit of the DC/DC converter        |
| charge-limit-used         | Number:Power         | Charge power limit currently applied             |
| charge-limit-user         | Number:Power         | Charge power limit set by the user               |
| discharge-limit-battery   | Number:Power         | Discharge power limit reported by the battery    |
| discharge-limit-dcdc      | Number:Power         | Discharge power limit of the DC/DC converter     |
| discharge-limit-used      | Number:Power         | Discharge power limit currently applied          |
| discharge-limit-user      | Number:Power         | Discharge power limit set by the user            |
| wallbox-discharge-to-car  | Switch               | The house battery may discharge into the car     |
| wallbox-charge-before-car | Switch               | The house battery is charged before the car      |
| wallbox-discharge-until   | Number:Dimensionless | State of charge down to which the battery may discharge into the car |
| wallbox-disable-at-mix-mode | Switch             | No battery charging while the wallbox charges in mix mode |

### Channel Group `grid`

| Channel       | Type   | Description                     |
|---------------|--------|---------------------------------|
| active-phases | Number | Number of phases in use (1 to 3) |

### Channel Group `inverter`

| Channel      | Type                     | Description                                   |
|--------------|--------------------------|-----------------------------------------------|
| ac-power-l1 | Number:Power | Alternating current power of phase L1 |
| ac-power-l2 | Number:Power | Alternating current power of phase L2 |
| ac-power-l3 | Number:Power | Alternating current power of phase L3 |
| ac-voltage-l1 | Number:ElectricPotential | Alternating current voltage of phase L1 |
| ac-voltage-l2 | Number:ElectricPotential | Alternating current voltage of phase L2 |
| ac-voltage-l3 | Number:ElectricPotential | Alternating current voltage of phase L3 |
| ac-current-l1 | Number:ElectricCurrent | Alternating current of phase L1 |
| ac-current-l2 | Number:ElectricCurrent | Alternating current of phase L2 |
| ac-current-l3 | Number:ElectricCurrent | Alternating current of phase L3 |
| apparent-power-l1 | Number:Power | Apparent power of phase L1 |
| apparent-power-l2 | Number:Power | Apparent power of phase L2 |
| apparent-power-l3 | Number:Power | Apparent power of phase L3 |
| reactive-power-l1 | Number:Power | Reactive power of phase L1 |
| reactive-power-l2 | Number:Power | Reactive power of phase L2 |
| reactive-power-l3 | Number:Power | Reactive power of phase L3 |
| energy-total-l1 | Number:Energy | Energy produced on phase L1 since commissioning |
| energy-total-l2 | Number:Energy | Energy produced on phase L2 since commissioning |
| energy-total-l3 | Number:Energy | Energy produced on phase L3 since commissioning |
| energy-day-l1 | Number:Energy | Energy produced on phase L1 today |
| energy-day-l2 | Number:Energy | Energy produced on phase L2 today |
| energy-day-l3 | Number:Energy | Energy produced on phase L3 today |
| ac-frequency | Number:Frequency         | Grid frequency measured by the inverter        |
| on-grid      | Switch                   | Whether the inverter is connected to the grid   |
| string-count | Number                   | Number of photovoltaic strings in use           |

#### Trackers and temperature sensors

The number of PV trackers and temperature sensors depends on the installation, and the device reports the number
of used strings wrongly (an S10 with two connected strings reports one). The binding therefore does not rely on
that number. On every poll it asks the inverter for the indexes 0 to 5 of the trackers and of the temperature
sensors, each one with its own request. An index the inverter does not have is answered with an error, which
does not stop the others.

For every index that answers with a value the binding creates channels at runtime, numbered from 1:

| Channel                | Type                     | Description                                           |
|------------------------|--------------------------|-------------------------------------------------------|
| tracker-N-power        | Number:Power             | Direct current power of tracker N                      |
| tracker-N-voltage      | Number:ElectricPotential | Direct current voltage of tracker N                    |
| tracker-N-current      | Number:ElectricCurrent   | Direct current of tracker N                            |
| temperature-N          | Number:Temperature       | Temperature of sensor N of the inverter                |

- The channels appear with the first poll after the thing came online. A tracker that is connected later
  gets its channels as soon as the inverter answers for it.
- Channels are only added, never removed, so the link of an item survives a poll the device fails to answer.
  Delete a channel of a tracker that is gone for good by hand.
- A tracker that is installed but not connected may answer with zeros, in which case it gets channels too.
- Earlier versions declared `tracker-1`, `tracker-2` and `temperature-1` to `temperature-4` in the thing
  description. Their channels keep their ids and are converted to the generic channel types, so existing links
  stay.
- The `string-count` channel reports the number of strings the device says it uses.

### Channel Group `system`

| Channel                   | Type         | Description                                     |
|---------------------------|--------------|-------------------------------------------------|
| software-version          | String       | Firmware version of the device                   |
| serial-number             | String       | Serial number of the device                      |
| production-date           | String       | Manufacturing date of the device                 |
| time-zone                 | String       | Time zone configured on the device               |
| installed-peak-power      | Number:Power | Peak power of the installed photovoltaic system  |
| inverter-count            | Number       | Number of inverters in the system                |
| battery-count             | Number       | Number of batteries in the system                |
| mode                      | Number       | Operating mode of the energy management system   |
| coupling-mode             | Number       | How the power plant is coupled to the grid       |
| grid-in-limit             | Switch       | Grid consumption is currently limited            |
| powersave                 | Switch       | Power saving mode is active                      |
| derate-at-percent         | Number:Dimensionless | Share of the installed peak power at which the feed in is reduced |
| derate-at-power           | Number:Power | Power at which the feed in is reduced            |

### Channel Group `battery-control`

Locks, power mode, manual charge and idle periods of the battery.

> **Warning:** Battery controlling can be handled from different clients and the "mixed use" can cause trouble.
> The ways are the **AI 360** (battery handling in control of the internal E3DC power plant control), the **block charge / discharge switch** (easy handling to enable / disable charging and discharging via rules) and **manual schedules** via the MyE3DC app or rules (your own handling!).
>
> Decide for one of these ways at a time. They write to the same idle periods and power settings of the device and can overwrite each other.
| Channel                   | Type         | Description                                     |
|---------------------------|--------------|-------------------------------------------------|
| charging-lock             | Switch       | Battery charging is currently blocked            |
| discharging-lock          | Switch       | Battery discharging is currently blocked         |
| charging-throttled        | Switch       | Battery charging is currently limited            |
| charging-time-lock        | Switch       | Battery charging is blocked by a time window     |
| discharging-time-lock     | Switch       | Battery discharging is blocked by a time window  |
| weather-regulated-charge  | Switch       | Charging follows the weather forecast            |
| power-limits-used         | Switch       | Configured power limits are active               |
| idle-periods-enabled      | Switch       | Whether the device uses the configured idle periods, can be switched |
| idle-periods              | String       | The idle periods, one per line; writing replaces the whole list |
| block-charging            | Switch       | Blocks battery charging around the clock, see [The block switches](#the-block-switches) |
| block-discharging         | Switch       | Blocks battery discharging around the clock, see [The block switches](#the-block-switches) |
| manual-charge-energy      | Number:Energy | Energy of the manual charge started last        |

The `idle-periods` channel shows the periods during which the battery is locked against charging or
discharging. Each line reads `name:days:mode:active:hh:mm-hh:mm`, for example:

```text
weekend:saturday,sunday:charge:true:08:00-18:00
```

The days are a comma separated list of `monday` to `sunday`, and the mode is `charge` or `discharge`.
The device replaces the whole list on every write, so the binding offers one channel and the thing actions to change it:

- Sending text to `idle-periods` replaces the whole list. An empty text removes all periods.

A period is only written when every line is valid, the names are unique and each period ends after it
starts. The reason is logged as a warning otherwise. The list is also read and replaced with the
[thing actions](#thing-actions) `getIdlePeriods` and `setIdlePeriods`. A single period is removed with
the action `deleteIdlePeriod`. To add or change one period, read the list with `getIdlePeriods` and write
it back with `setIdlePeriods`.

#### Power mode

`battery-control#set-power-mode` forces the battery into a mode, the same way as the `set/power_mode` topic of
rscp2mqtt. It is a command channel: it carries no state and offers a few presets as command options. The text is one of:

| Text | Effect |
|------|--------|
| `auto` | The energy manager decides again. Ends a forced mode. |
| `idle:<cycles>` | The battery neither charges nor discharges. |
| `charge:<watts>:<cycles>` | The battery charges with the given power. |
| `discharge:<watts>:<cycles>` | The battery discharges with the given power. |
| `grid_charge:<watts>:<cycles>` | The battery charges with the given power from the grid. |

The device keeps a forced mode only for a short time. The binding therefore sends the mode again with every poll
cycle, as many times as `<cycles>` says. With the refresh interval of the bridge, `<cycles>` times the interval is
the duration, for example `charge:2000:10` charges with 2000 W for ten poll cycles. After that the device returns
to automatic. A new text replaces the one before, and `auto` ends it at once. The presets use 60 cycles; how long
that lasts depends on the refresh interval of the bridge.

The same is available as the thing action `setPowerMode(mode, watts, cycles)` with `mode` being `auto`, `idle`,
`charge`, `discharge` or `grid_charge`.

#### The block switches

The device has no function that simply blocks charging or discharging of the battery. The switches
`block-charging` and `block-discharging` provide it with an idle period of their own that locks the
battery on all seven days, from 00:00 to 23:59:

| Switch              | Idle period it manages                                                |
|---------------------|-----------------------------------------------------------------------|
| `block-charging`    | `OH_Block_Charge:monday,tuesday,wednesday,thursday,friday,saturday,sunday:charge:true:00:00-23:59`       |
| `block-discharging` | `OH_Block_Discharge:monday,tuesday,wednesday,thursday,friday,saturday,sunday:discharge:true:00:00-23:59` |

- `ON` adds the period to the list and switches on the use of idle periods (`idle-periods-enabled`),
  because the period has no effect otherwise.
- `OFF` removes the period again. The use of idle periods stays switched on.
- All other periods of the list are kept. The switch is `ON` as soon as the list holds an active entry of the
  right mode, no matter who created it or on which days and at which times it applies, and `OFF` when it
  holds none.
  If another active entry still exists, `OFF` removes only the period of the switch and the switch stays `ON`.
- The names differ from the `Charge_Limiter` and `Discharge_Limiter` periods of rscp2mqtt, so both can run
  next to each other.
- The device counts in minutes and has no 24:00, which is why the period ends at 23:59.

The same list can be read and replaced as a whole with the [thing actions](#thing-actions), for example to
add periods for single weekdays next to the block switches.

### Channel Group `history`

The history group reports the energy counters and the two shares of a whole period. The device answers
the history containers only when the request states the period, which the binding derives from the
current date.

> **Persistence:** The binding publishes the earlier periods as time series entries with a date in the past,
> see [History Fetch](#history-fetch). Only a persistence service with a real time series database, for example
> InfluxDB or a SQL database (JDBC), can store them. **rrd4j cannot:** it only records values at the time they
> arrive, ignores the timestamp of a time series entry and aggregates old values away. Do not use rrd4j for the
> `history` items.

| Channel                  | Type                 | Description                                        |
|--------------------------|----------------------|----------------------------------------------------|
| day-solar                | Number:Energy        | Photovoltaic energy of the completed day            |
| day-house                | Number:Energy        | Household energy of the completed day               |
| day-grid-in              | Number:Energy        | Energy drawn from the grid on the completed day     |
| day-grid-out             | Number:Energy        | Energy fed into the grid on the completed day       |
| day-battery-in           | Number:Energy        | Energy charged into the battery on the completed day |
| day-battery-out          | Number:Energy        | Energy taken from the battery on the completed day  |
| day-autarky              | Number:Dimensionless | Share of consumption covered by the power plant      |
| day-consumed-production  | Number:Dimensionless | Share of production used on site                    |
| week-solar               | Number:Energy        | Photovoltaic energy of the last seven days          |
| week-house               | Number:Energy        | Household energy of the last seven days             |
| week-grid-in             | Number:Energy        | Energy drawn from the grid in the last seven days   |
| week-grid-out            | Number:Energy        | Energy fed into the grid in the last seven days     |
| week-battery-in          | Number:Energy        | Energy charged into the battery in the last seven days |
| week-battery-out         | Number:Energy        | Energy taken from the battery in the last seven days |
| week-autarky             | Number:Dimensionless | Share of consumption covered by the power plant      |
| week-consumed-production | Number:Dimensionless | Share of production used on site                    |
| month-solar              | Number:Energy        | Photovoltaic energy of the current month            |
| month-house              | Number:Energy        | Household energy of the current month               |
| month-grid-in            | Number:Energy        | Energy drawn from the grid in the current month     |
| month-grid-out           | Number:Energy        | Energy fed into the grid in the current month       |
| month-battery-in         | Number:Energy        | Energy charged into the battery in the current month |
| month-battery-out        | Number:Energy        | Energy taken from the battery in the current month  |
| month-autarky            | Number:Dimensionless | Share of consumption covered by the power plant      |
| month-consumed-production | Number:Dimensionless | Share of production used on site                   |
| year-solar               | Number:Energy        | Photovoltaic energy of the current year             |
| year-house               | Number:Energy        | Household energy of the current year                |
| year-grid-in             | Number:Energy        | Energy drawn from the grid in the current year      |
| year-grid-out            | Number:Energy        | Energy fed into the grid in the current year        |
| year-battery-in          | Number:Energy        | Energy charged into the battery in the current year |
| year-battery-out         | Number:Energy        | Energy taken from the battery in the current year   |
| year-autarky             | Number:Dimensionless | Share of consumption covered by the power plant      |
| year-consumed-production | Number:Dimensionless | Share of production used on site                    |

### Channel Group `emergency`

The group holds the reserve, the part of the battery the device keeps available for emergency power, and the state of the emergency power supply.

| Channel    | Type                 | Description                                              |
|------------|----------------------|----------------------------------------------------------|
| percent    | Number:Dimensionless | Share of the battery kept for emergency power             |
| energy     | Number:Energy        | Energy kept for emergency power                           |
| energy-max | Number:Energy        | Highest reserve the battery can hold for emergency power  |
| last-soc   | Number:Dimensionless | State of charge the battery was left with                 |
| power-status | Number       | State of the emergency power supply              |
| power-available | Switch       | Emergency power reserve is available             |

### Writable Channels

The following channels accept commands and write the value to the device. A value that the device reports
is set through the channel that reports it, so there is one channel per value and no second one for
writing it. The charge and discharge limits are written as one container because switching the power
limits off resets the enclosed limits: a write for one of them always carries the other values that were
last read from the device.

| Channel Group | Channel                      | Type         | Description                                                                 |
|---------------|------------------------------|--------------|-----------------------------------------------------------------------------|
| `battery`     | max-charge-power             | Number:Power | Configured limit for battery charging                                        |
| `battery`     | max-discharge-power          | Number:Power | Configured limit for battery discharging                                     |
| `battery`     | discharge-start-power        | Number:Power | Power above which the battery is discharged                                  |
| `battery`     | wallbox-discharge-to-car     | Switch       | The house battery may discharge into the car                                 |
| `battery`     | wallbox-charge-before-car    | Switch       | The house battery is charged before the car                                  |
| `battery`     | wallbox-discharge-until      | Number:Dimensionless | State of charge down to which the battery may discharge into the car |
| `battery`     | wallbox-disable-at-mix-mode  | Switch       | No battery charging while the wallbox charges in mix mode                    |
| `battery-control` | power-limits-used            | Switch       | Whether the configured power limits are active                               |
| `battery-control` | weather-regulated-charge     | Switch       | Whether charging follows the weather forecast                                |
| `battery-control` | set-power-mode               | String       | Command only: forces a power mode, `auto`, `idle:<cycles>` or `charge`/`discharge`/`grid_charge:<watts>:<cycles>` |
| `battery-control` | set-manual-charge            | Number:Energy | Command only: starts a manual battery charge with this energy, offered as 500 Wh to 3000 Wh in steps of 500 Wh |
| `battery-control` | block-charging               | Switch       | Blocks battery charging on all days, see [The block switches](#the-block-switches) |
| `battery-control` | block-discharging            | Switch       | Blocks battery discharging on all days, see [The block switches](#the-block-switches) |
| `emergency`   | percent                      | Number:Dimensionless | Share of the battery to keep for emergency power                    |
| `emergency`   | energy                       | Number:Energy | Energy to keep for emergency power                                          |
| `control`     | sun-mode                     | Switch       | Whether charging uses surplus photovoltaic power                             |
| `control`     | suspended                    | Switch       | Whether the charging session is suspended                                    |
| `control`     | toggle                       | Switch       | `ON` suspends a running session and resumes a suspended one, then returns to `OFF` |
| `settings`    | auto-phase-switch            | Switch       | Whether the wallbox changes the number of phases by itself                    |
| `settings`    | min-charge-current           | Number:ElectricCurrent | Lowest charging current allowed, 6 to 16 A                  |
| `settings`    | max-charge-current           | Number:ElectricCurrent | Highest charging current allowed, 6 to 16 A                        |
| `settings`    | number-phases                | Number       | Number of phases used for charging, 1 or 3                                   |

A channel state is only updated after the device acknowledged the write, so a rejected command leaves
the channel untouched.

A wallbox value is written as the parameter of the request that names it, which is also how the value is
read: every wallbox control is one channel that reports and sets its value. The `plugged`, `locked` and
`charging` channels report states the device decides, so they are read only.

### Channel Groups of the `wallbox` Thing

The channels of a wallbox are spread over three groups.
All of them are read from the same request, so the grouping costs nothing at the device.

#### Group `status-control`

| Channel            | Type                   | Description                                          |
|--------------------|------------------------|------------------------------------------------------|
| plugged            | Switch                 | A vehicle is connected                               |
| locked             | Switch                 | The connector is locked                              |
| charging           | Switch                 | A charging session is running                        |
| key-state          | Switch                 | State of the wallbox key switch                      |
| soc                | Number:Dimensionless   | State of charge of the connected vehicle             |
| sun-mode           | Switch                 | Charging uses surplus photovoltaic power             |
| suspended          | Switch                 | Charging is currently suspended                      |
| toggle             | Switch                 | Suspends or resumes the charging, goes back to `OFF` |
| min-charge-current | Number:ElectricCurrent | Lowest charging current allowed                      |
| max-charge-current | Number:ElectricCurrent | Highest charging current allowed                     |
| number-phases      | Number                 | Number of phases used for charging                   |
| auto-phase-switch  | Switch                 | Wallbox changes the number of phases by itself       |

#### Group `session`

The wallbox reports the phase and energy values for the last charging session.
They are not live values and not daily or lifetime counters.
The power values are live.

| Channel   | Type          | Description                                                          |
|-----------|---------------|----------------------------------------------------------------------|
| power-l1  | Number:Power  | Power on phase L1                                                    |
| power-l2  | Number:Power  | Power on phase L2                                                    |
| power-l3  | Number:Power  | Power on phase L3                                                    |
| phase-l1  | Switch        | Whether the wallbox charged on phase L1 in the last charging session |
| phase-l2  | Switch        | Whether the wallbox charged on phase L2 in the last charging session |
| phase-l3  | Switch        | Whether the wallbox charged on phase L3 in the last charging session |
| energy-l1 | Number:Energy | Energy drawn on phase L1 in the last charging session                |
| energy-l2 | Number:Energy | Energy drawn on phase L2 in the last charging session                |
| energy-l3 | Number:Energy | Energy drawn on phase L3 in the last charging session                |

#### Group `counters`

| Channel | Type          | Description                               |
|---------|---------------|-------------------------------------------|
| total   | Number:Energy | Energy charged through this wallbox       |
| solar   | Number:Energy | Energy taken from the photovoltaic system |

### Channel Group `meter`

| Channel         | Type                     | Description                         |
|-----------------|--------------------------|-------------------------------------|
| power-l1        | Number:Power             | Power on phase L1                   |
| power-l2        | Number:Power             | Power on phase L2                   |
| power-l3        | Number:Power             | Power on phase L3                   |
| voltage-l1      | Number:ElectricPotential | Voltage on phase L1                 |
| voltage-l2      | Number:ElectricPotential | Voltage on phase L2                 |
| voltage-l3      | Number:ElectricPotential | Voltage on phase L3                 |
| active-phases   | Number                   | Number of phases in use (1 to 3)   |
| energy-l1       | Number:Energy            | Energy measured on phase L1         |
| energy-l2       | Number:Energy            | Energy measured on phase L2         |
| energy-l3       | Number:Energy            | Energy measured on phase L3         |
| max-phase-power | Number:Power             | Highest power measured on one phase |

### Channel Group `pack` (battery)

| Channel | Type | Description |
|---------|------|-------------|
| voltage | Number:ElectricPotential | Voltage of the battery |
| current | Number:ElectricCurrent | Current of the battery, negative while discharging |
| charge-cycles | Number | Number of charge cycles of the battery |
| module-count | Number | Number of battery modules |
| soh | Number:Dimensionless | Absolute state of charge of the battery, a measure of its state of health |
| status-code | Number | Status code of the battery |
| error-code | Number | Error code of the battery |
| device-name | String | Name of the battery |
| training-mode | Number | Training mode of the battery |
| design-capacity | Number | Design capacity of the battery |
| usable-capacity | Number | Usable capacity of the battery |
| usable-remaining-capacity | Number | Usable capacity left in the battery |
| design-energy | Number:Energy | Design capacity in kWh, calculated as design capacity (Ah) × battery voltage (V) |
| usable-energy | Number:Energy | Usable capacity in kWh, calculated as usable capacity (Ah) × battery voltage (V) |
| usable-remaining-energy | Number:Energy | Usable energy left in kWh, calculated as usable remaining capacity (Ah) × battery voltage (V) |

The three energy channels are not reported by the device. The binding multiplies the capacity in `Ah` by the
battery `voltage` of the same poll. The voltage is the present terminal voltage and moves with the state of
charge and the load, so the kWh values vary by a few percent over a day. Use `design-energy` and `usable-energy`
as rough guides and the ratio `usable-remaining-energy / usable-energy` (or the SOC) for trends.

### Channel Group `module` (battery module)

The module group reports the values of one battery cell module. The capacities are published in `Ah`
without a unit suffix on the item type, because the item type for electric charge is based on the
coulomb.

| Channel               | Type                     | Description                                  |
|-----------------------|--------------------------|----------------------------------------------|
| soc                   | Number:Dimensionless     | State of charge of the module                 |
| soh                   | Number:Dimensionless     | State of health of the module                 |
| voltage               | Number:ElectricPotential | Voltage of the module                         |
| voltage-avg-30s       | Number:ElectricPotential | Voltage averaged over 30 seconds               |
| current               | Number:ElectricCurrent   | Current of the module                          |
| current-avg-30s       | Number:ElectricCurrent   | Current averaged over 30 seconds               |
| remaining-capacity    | Number                   | Remaining capacity in Ah                       |
| full-charge-capacity  | Number                   | Capacity at full charge in Ah                  |
| design-capacity       | Number                   | Design capacity in Ah                          |
| remaining-energy      | Number:Energy            | Remaining energy in kWh, remaining capacity (Ah) × module voltage (V) |
| full-charge-energy    | Number:Energy            | Energy at full charge in kWh, full charge capacity (Ah) × module voltage (V) |
| design-energy         | Number:Energy            | Design energy in kWh, design capacity (Ah) × module voltage (V) |
| design-voltage        | Number:ElectricPotential | Design voltage of the module                   |
| max-charge-voltage    | Number:ElectricPotential | Highest voltage allowed for charging           |
| max-charge-current    | Number:ElectricCurrent   | Highest current allowed for charging           |
| max-discharge-current | Number:ElectricCurrent   | Highest current allowed for discharging        |
| charge-high-temperature | Number:Temperature     | Upper temperature limit for charging           |
| charge-low-temperature | Number:Temperature      | Lower temperature limit for charging           |
| cycles                | Number                   | Charge cycles the module has completed         |
| status                | Number                   | Status code reported by the module             |
| warning               | Number                   | Warning code reported by the module            |
| alarm                 | Number                   | Alarm code reported by the module              |
| error                 | Number                   | Error code reported by the module              |
| nr-sensor             | Number                   | Number of temperature sensors                  |
| nr-series-cell        | Number                   | Number of cells in series                      |
| nr-parallel-cell      | Number                   | Number of cells in parallel                    |
| firmware-version      | String                   | Firmware version of the module                 |
| protocol-version      | String                   | Protocol version of the module                 |
| table-version         | String                   | Data table version of the module               |
| pcb-version           | String                   | PCB version of the module                      |
| device-name           | String                   | Device name of the module                      |
| manufacturer          | String                   | Manufacturer of the module                     |
| manufacture-date      | String                   | Manufacturing date of the module               |
| serial-number         | String                   | Serial number of the module                    |
| serial-code           | String                   | Serial code of the module                      |

### Channel Group `statistics` (bridge)

The bridge reports how much it occupies the device. All channels are advanced and read only. They show the
last poll and, for the loop ratio, the summary of the last 20 polls.

| Channel            | Type                 | Description                                                                 |
|--------------------|----------------------|-----------------------------------------------------------------------------|
| loop-ratio         | Number:Dimensionless | Time the device was occupied by the connection during the last poll, in percent of `refreshInterval` |
| loop-ratio-average | Number:Dimensionless | Average of the loop ratio over the last 20 polls                           |
| loop-ratio-peak    | Number:Dimensionless | Highest loop ratio of the last 20 polls                                    |
| poll-duration      | Number:Time          | Time the last request spent on the wire, from sending to the complete answer |
| device-wait        | Number:Time          | Time the device needed to start its answer                                 |
| dispatch-time      | Number:Time          | Time openHAB needed to hand the answer to the things                       |
| answer-size        | Number:DataAmount    | Size of the answer to the last request                                     |

The loop ratio is the time of all requests on the connection (polls, commands and history fetches) divided by
the refresh interval. A value below 20 % is the working target; above 100 % the interval is shorter than the
device needs to answer. The values are only published on linked channels. The log output
(`E3DCBridgeHandler` at DEBUG and TRACE) does not depend on that.

## Thing Actions

The `power-plant` thing provides actions to read and replace the idle periods of the second version, the
named time windows during which the battery is locked against charging or discharging. They work on the
same list as the `idle-periods` channel and the [block switches](#the-block-switches).

| Action                          | Returns   | Description                                                      |
|---------------------------------|-----------|------------------------------------------------------------------|
| `getIdlePeriods()`              | `String`  | The periods in the device, one per line, or `null` if the device could not be asked |
| `setIdlePeriods(String periods)` | `boolean` | Replaces the whole list and tells whether the device accepted it |
| `setPowerMode(String mode, int watts, int cycles)` | `boolean` | Forces a power mode, see [Power mode](#power-mode), and tells whether the device accepted it |
| `deleteIdlePeriod(String name)` | `boolean` | Removes the period of that name, keeps the others and tells whether the device accepted it |
| `fetchPeriodHistory(String period, int count)` | `int` | Fetches the finished `day`, `week`, `month` or `year` periods, see [History Fetch](#history-fetch), and returns the number of periods that had data, or `-1` if the arguments are not valid or the device is not connected |
| `fetchHistory()` | `int` | Fetches the yearly history of all earlier years once and returns the number of years that had data, or `-1` if the device is not connected |

A period is one line `name:days:mode:active:hh:mm-hh:mm`, as in the `idle-periods` channel:

```text
weekend:saturday,sunday:charge:true:08:00-18:00
```

- The days are a comma separated list of `monday` to `sunday`, the mode is `charge` or `discharge`.
- `setIdlePeriods` **replaces** the whole list, because the device does it that way. Read the list first
  with `getIdlePeriods`, change it and write it back to keep the other periods, including the periods
  `OH_Block_Charge` and `OH_Block_Discharge` of the [block switches](#the-block-switches).
- An empty text removes all periods. Nothing is sent when a line is malformed, a name is used twice or a
  period does not end after it starts. The reason is logged as a warning and the action returns `false`.
- The periods only take effect while the device uses idle periods, see the channel
  `idle-periods-enabled`. The actions leave this setting alone; the block switches and the channel switch it on.

### History Fetch

The `history#year-*` channels hold the running year as their state. The earlier years are only requested when
you ask for them with `fetchHistory`, so the device is not asked again and again for values that no longer
change.

- The action takes no parameters. It starts at the last completed year and goes back year by year.
- Each year is requested on its own and published as one entry of a time series, stamped with the start of
  the year. Persistence stores the years under their date, so the yearly values form one series.
- It stops after two years in a row without data, which is the case before the device was installed. A single
  year without data in between is tolerated. A year in which every value is zero counts as without data.
  The oldest year tried is 2000.
- Fetching again replaces the entries of the years found.
- The channels have to be linked to items that a persistence service stores, otherwise the entries are lost.
  The service has to be a time series database such as InfluxDB or a SQL database (JDBC). **rrd4j is not
  suitable:** it cannot store values with a date in the past, so the fetched entries would be lost.

```java
val actions = getActions("e3dc", "e3dc:power-plant:device:plant")
val years = actions.fetchHistory()
logInfo("e3dc", "History fetched for " + years + " years")
```

`fetchPeriodHistory(period, count)` does the same for the `history#day-*`, `history#week-*` and `history#month-*` channels
(and for `year`):

- It goes back from the last finished period, at most `count` periods (1 to 400). A week runs from Monday to
  Sunday, a month is a calendar month. Each entry is stamped with the first day of its period.
- It stops after three periods in a row without data.
- Every period is a request to the device, so a large `count` takes a while. The call returns when it is done.
- The week channels hold the last seven days as their state, so the calendar weeks of the fetch are separate entries.

```java
val days = actions.fetchPeriodHistory("day", 90)
```

### Rules DSL

```java
val actions = getActions("e3dc", "e3dc:power-plant:device:plant")

// read, add a period and write the list back
var periods = actions.getIdlePeriods()
if (periods !== null) {
    periods = periods + "\nweekend:saturday,sunday:charge:true:08:00-18:00"
    val accepted = actions.setIdlePeriods(periods.trim())
    logInfo("e3dc", "Idle periods written: " + accepted)
}
```

### JavaScript

```javascript
const plant = actions.thingActions('e3dc', 'e3dc:power-plant:device:plant');
let periods = plant.getIdlePeriods();
if (periods !== null) {
  const accepted = plant.setIdlePeriods((periods + '\nweekend:saturday,sunday:charge:true:08:00-18:00').trim());
  console.log('Idle periods written: ' + accepted);
}
```

To only block or release the battery, use the switches `battery-control#block-charging` and
`battery-control#block-discharging` instead, see [The block switches](#the-block-switches).

## Full Example

The example sets up every thing type and uses at least one channel of every channel group.
Adapt the host, the credentials and the indexes to your installation. The thing ids `device`, `plant`,
`wallbox0`, `meter0`, `battery0` and `module0` are free to choose; they appear in the channel ids of the items.

### Thing Configuration

```java
Bridge e3dc:bridge:device "E3/DC Device" [ host="192.168.1.50", userName="owner@example.com", password="secret", rscpPassword="rscpSecret", refreshInterval=5, historyInterval=15 ] {
    Thing power-plant    plant    "E3/DC Power Plant"
    Thing wallbox        wallbox0 "E3/DC Wallbox"        [ wallboxIndex=0 ]
    Thing power-meter    meter0   "E3/DC Power Meter"    [ meterIndex=0 ]
    Thing battery        battery0 "E3/DC Battery"        [ batteryIndex=0 ]
    Thing battery-module module0  "E3/DC Battery Module" [ moduleIndex=0 ]
}
```

The `power-plant` thing is the one to start with. Add the other things only for the parts your installation has.
The channels `tracker-N-*` and `temperature-N` of the inverter are created at runtime, see
[Trackers and temperature sensors](#trackers-and-temperature-sensors). Link them from the UI or add them to the
items file once they exist.

### Item Configuration

```java
// power - the current power flows
Number:Power         E3DC_SolarPower         "Solar Power [%.0f %unit%]"            { channel="e3dc:power-plant:device:plant:power#solar-power" }
Number:Power         E3DC_BatteryPower       "Battery Power [%.0f %unit%]"          { channel="e3dc:power-plant:device:plant:power#battery-power" }
Number:Power         E3DC_HousePower         "House Power [%.0f %unit%]"            { channel="e3dc:power-plant:device:plant:power#house-power" }
Number:Power         E3DC_GridPower          "Grid Power [%.0f %unit%]"             { channel="e3dc:power-plant:device:plant:power#grid-power" }
Number:Power         E3DC_AdditionalPower    "Additional Power [%.0f %unit%]"       { channel="e3dc:power-plant:device:plant:power#additional-power" }
Number:Power         E3DC_WallboxPower       "Wallbox Power [%.0f %unit%]"          { channel="e3dc:power-plant:device:plant:power#wallbox-power" }
Number:Power         E3DC_WallboxSolarPower  "Wallbox Solar Power [%.0f %unit%]"    { channel="e3dc:power-plant:device:plant:power#wallbox-solar-power" }
Number:Dimensionless E3DC_Autarky            "Autarky [%.0f %%]"                    { channel="e3dc:power-plant:device:plant:power#autarky" }
Number:Dimensionless E3DC_SelfConsumption    "Self Consumption [%.0f %%]"           { channel="e3dc:power-plant:device:plant:power#self-consumption" }

// battery - state and the limits that can be changed
Number:Dimensionless E3DC_BatterySoc         "Battery SOC [%.0f %%]"                { channel="e3dc:power-plant:device:plant:battery#soc" }
Number:Dimensionless E3DC_BatteryRsoc        "Battery Real SOC [%.0f %%]"           { channel="e3dc:power-plant:device:plant:battery#rsoc" }
Number:Power         E3DC_RemainingCharge    "Remaining Charge Power [%.0f %unit%]" { channel="e3dc:power-plant:device:plant:battery#remaining-charge-power" }
Number:Power         E3DC_MaxChargePower     "Max Charge Power [%.0f %unit%]"       { channel="e3dc:power-plant:device:plant:battery#max-charge-power" }
Number:Power         E3DC_MaxDischargePower  "Max Discharge Power [%.0f %unit%]"    { channel="e3dc:power-plant:device:plant:battery#max-discharge-power" }
Number:Power         E3DC_DischargeStart     "Discharge Start Power [%.0f %unit%]"  { channel="e3dc:power-plant:device:plant:battery#discharge-start-power" }
Switch               E3DC_DischargeToCar     "Discharge Into Car [%s]"              { channel="e3dc:power-plant:device:plant:battery#wallbox-discharge-to-car" }
Number:Dimensionless E3DC_DischargeUntil     "Discharge Into Car Until [%.0f %%]"   { channel="e3dc:power-plant:device:plant:battery#wallbox-discharge-until" }

// grid, inverter, system
Number               E3DC_GridPhases         "Grid Phases [%d]"                     { channel="e3dc:power-plant:device:plant:grid#active-phases" }
Switch               E3DC_OnGrid             "On Grid [%s]"                         { channel="e3dc:power-plant:device:plant:inverter#on-grid" }
Number:Frequency     E3DC_AcFrequency        "AC Frequency [%.2f %unit%]"           { channel="e3dc:power-plant:device:plant:inverter#ac-frequency" }
Number:Power         E3DC_AcPowerL1          "AC Power L1 [%.0f %unit%]"            { channel="e3dc:power-plant:device:plant:inverter#ac-power-l1" }
Number:Energy        E3DC_EnergyTotalL1      "Energy Total L1 [%.1f %unit%]"        { channel="e3dc:power-plant:device:plant:inverter#energy-total-l1" }
Number:Power         E3DC_Tracker1Power      "Tracker 1 Power [%.0f %unit%]"        { channel="e3dc:power-plant:device:plant:inverter#tracker-1-power" }
Number:Temperature   E3DC_Temperature1       "Inverter Temperature [%.1f %unit%]"   { channel="e3dc:power-plant:device:plant:inverter#temperature-1" }
String               E3DC_SerialNumber       "Serial Number [%s]"                   { channel="e3dc:power-plant:device:plant:system#serial-number" }
String               E3DC_SoftwareVersion    "Software Version [%s]"                { channel="e3dc:power-plant:device:plant:system#software-version" }
Number:Power         E3DC_InstalledPeak      "Installed Peak Power [%.0f %unit%]"   { channel="e3dc:power-plant:device:plant:system#installed-peak-power" }

// history - counters of the day, the week, the month and the year
Number:Energy        E3DC_DaySolar           "Solar Day [%.1f %unit%]"              { channel="e3dc:power-plant:device:plant:history#day-solar" }
Number:Energy        E3DC_DayHouse           "House Day [%.1f %unit%]"              { channel="e3dc:power-plant:device:plant:history#day-house" }
Number:Energy        E3DC_DayGridIn          "Grid Supply Day [%.1f %unit%]"        { channel="e3dc:power-plant:device:plant:history#day-grid-in" }
Number:Energy        E3DC_DayGridOut         "Grid Feed Day [%.1f %unit%]"          { channel="e3dc:power-plant:device:plant:history#day-grid-out" }
Number:Dimensionless E3DC_DayAutarky         "Autarky Day [%.0f %%]"                { channel="e3dc:power-plant:device:plant:history#day-autarky" }
Number:Energy        E3DC_WeekSolar          "Solar Week [%.1f %unit%]"             { channel="e3dc:power-plant:device:plant:history#week-solar" }
Number:Energy        E3DC_MonthSolar         "Solar Month [%.1f %unit%]"            { channel="e3dc:power-plant:device:plant:history#month-solar" }
Number:Energy        E3DC_YearSolar          "Solar Year [%.0f %unit%]"             { channel="e3dc:power-plant:device:plant:history#year-solar" }

// emergency power
Number:Dimensionless E3DC_EmergencyPercent   "Emergency Reserve [%.0f %%]"          { channel="e3dc:power-plant:device:plant:emergency#percent" }
Number:Energy        E3DC_EmergencyEnergy    "Emergency Reserve [%.1f %unit%]"      { channel="e3dc:power-plant:device:plant:emergency#energy" }
Switch               E3DC_EmergencyAvailable "Emergency Power Available [%s]"       { channel="e3dc:power-plant:device:plant:emergency#power-available" }

// battery-control - locks, power mode, manual charge and idle periods
Switch               E3DC_ChargingLock       "Charging Lock [%s]"                   { channel="e3dc:power-plant:device:plant:battery-control#charging-lock" }
Switch               E3DC_DischargingLock    "Discharging Lock [%s]"                { channel="e3dc:power-plant:device:plant:battery-control#discharging-lock" }
Switch               E3DC_WeatherRegulated   "Weather Regulated Charge [%s]"        { channel="e3dc:power-plant:device:plant:battery-control#weather-regulated-charge" }
Switch               E3DC_PowerLimitsUsed    "Power Limits Used [%s]"               { channel="e3dc:power-plant:device:plant:battery-control#power-limits-used" }
Switch               E3DC_BlockCharging      "Block Charging [%s]"                  { channel="e3dc:power-plant:device:plant:battery-control#block-charging" }
Switch               E3DC_BlockDischarging   "Block Discharging [%s]"               { channel="e3dc:power-plant:device:plant:battery-control#block-discharging" }
Switch               E3DC_IdlePeriodsOn      "Idle Periods Enabled [%s]"            { channel="e3dc:power-plant:device:plant:battery-control#idle-periods-enabled" }
String               E3DC_IdlePeriods        "Idle Periods [%s]"                    { channel="e3dc:power-plant:device:plant:battery-control#idle-periods" }
String               E3DC_SetPowerMode       "Power Mode"                           { channel="e3dc:power-plant:device:plant:battery-control#set-power-mode" }
Number:Energy        E3DC_SetManualCharge    "Manual Charge"                        { channel="e3dc:power-plant:device:plant:battery-control#set-manual-charge" }
Number:Energy        E3DC_ManualChargeEnergy "Manual Charge Energy [%.0f %unit%]"   { channel="e3dc:power-plant:device:plant:battery-control#manual-charge-energy" }

// wallbox
Switch               E3DC_WallboxPlugged     "Wallbox Plugged [%s]"                 { channel="e3dc:wallbox:device:wallbox0:status-control#plugged" }
Switch               E3DC_WallboxLocked      "Wallbox Locked [%s]"                  { channel="e3dc:wallbox:device:wallbox0:status-control#locked" }
Switch               E3DC_WallboxCharging    "Wallbox Charging [%s]"                { channel="e3dc:wallbox:device:wallbox0:status-control#charging" }
Switch               E3DC_WallboxSunMode     "Wallbox Sun Mode [%s]"                { channel="e3dc:wallbox:device:wallbox0:status-control#sun-mode" }
Switch               E3DC_WallboxToggle      "Wallbox Suspend / Resume"             { channel="e3dc:wallbox:device:wallbox0:status-control#toggle" }
Number:ElectricCurrent E3DC_WallboxMaxCurrent "Wallbox Max Current [%.0f %unit%]"   { channel="e3dc:wallbox:device:wallbox0:status-control#max-charge-current" }
Number               E3DC_WallboxPhases      "Wallbox Phases [%d]"                  { channel="e3dc:wallbox:device:wallbox0:status-control#number-phases" }
Number:Power         E3DC_WallboxPowerL1     "Wallbox Power L1 [%.0f %unit%]"       { channel="e3dc:wallbox:device:wallbox0:session#power-l1" }
Number:Energy        E3DC_WallboxEnergyL1    "Wallbox Energy L1 [%.2f %unit%]"      { channel="e3dc:wallbox:device:wallbox0:session#energy-l1" }
Number:Energy        E3DC_WallboxTotal       "Wallbox Total [%.1f %unit%]"          { channel="e3dc:wallbox:device:wallbox0:counters#total" }
Number:Energy        E3DC_WallboxSolar       "Wallbox Solar Total [%.1f %unit%]"    { channel="e3dc:wallbox:device:wallbox0:counters#solar" }

// power meter
Number:Power         E3DC_MeterPowerL1       "Meter Power L1 [%.0f %unit%]"         { channel="e3dc:power-meter:device:meter0:meter#power-l1" }
Number:ElectricPotential E3DC_MeterVoltageL1 "Meter Voltage L1 [%.1f %unit%]"       { channel="e3dc:power-meter:device:meter0:meter#voltage-l1" }
Number:Energy        E3DC_MeterEnergyL1      "Meter Energy L1 [%.1f %unit%]"        { channel="e3dc:power-meter:device:meter0:meter#energy-l1" }

// battery pack and battery module
Number:ElectricPotential E3DC_PackVoltage    "Battery Voltage [%.1f %unit%]"        { channel="e3dc:battery:device:battery0:pack#voltage" }
Number:ElectricCurrent E3DC_PackCurrent      "Battery Current [%.1f %unit%]"        { channel="e3dc:battery:device:battery0:pack#current" }
Number               E3DC_PackCycles         "Battery Charge Cycles [%d]"           { channel="e3dc:battery:device:battery0:pack#charge-cycles" }
Number:Dimensionless E3DC_PackSoh            "Battery SOH [%.0f %%]"                { channel="e3dc:battery:device:battery0:pack#soh" }
Number:Dimensionless E3DC_ModuleSoc          "Module SOC [%.0f %%]"                 { channel="e3dc:battery-module:device:module0:module#soc" }
Number:ElectricPotential E3DC_ModuleVoltage  "Module Voltage [%.1f %unit%]"         { channel="e3dc:battery-module:device:module0:module#voltage" }
Number:Temperature   E3DC_ModuleChargeHigh   "Module Max Charge Temperature [%.0f %unit%]" { channel="e3dc:battery-module:device:module0:module#charge-high-temperature" }

// load the bridge puts on the device
Number:Dimensionless E3DC_LoopRatio          "Loop Ratio [%.0f %%]"                 { channel="e3dc:bridge:device:statistics#loop-ratio" }
Number:Time          E3DC_PollDuration       "Poll Duration [%.2f %unit%]"          { channel="e3dc:bridge:device:statistics#poll-duration" }
```

### Sitemap Configuration

```perl
sitemap e3dc label="E3/DC" {
    Frame label="Power" {
        Text item=E3DC_SolarPower       label="Solar [%.0f %unit%]"
        Text item=E3DC_BatteryPower     label="Battery [%.0f %unit%]"
        Text item=E3DC_HousePower       label="House [%.0f %unit%]"
        Text item=E3DC_GridPower        label="Grid [%.0f %unit%]"
        Text item=E3DC_Autarky          label="Autarky [%.0f %%]"
        Text item=E3DC_SelfConsumption  label="Self Consumption [%.0f %%]"
    }
    Frame label="Battery" {
        Text item=E3DC_BatterySoc       label="SOC [%.0f %%]"
        Setpoint item=E3DC_MaxChargePower    label="Max Charge Power [%.0f %unit%]"    minValue=0 maxValue=6000 step=100
        Setpoint item=E3DC_MaxDischargePower label="Max Discharge Power [%.0f %unit%]" minValue=0 maxValue=6000 step=100
        Switch item=E3DC_DischargeToCar label="Discharge Into Car"
        Text item=E3DC_ChargingLock     label="Charging Lock [%s]"
        Text item=E3DC_DischargingLock  label="Discharging Lock [%s]"
    }
    Frame label="Battery Control" {
        Switch item=E3DC_BlockCharging    label="Block Charging"
        Switch item=E3DC_BlockDischarging label="Block Discharging"
        Switch item=E3DC_SetPowerMode     label="Power Mode" mappings=[ "auto"="Auto", "idle:60"="Idle", "charge:2000:60"="Charge 2 kW", "discharge:2000:60"="Discharge 2 kW" ]
        Switch item=E3DC_SetManualCharge  label="Manual Charge" mappings=[ "500 Wh"="0.5 kWh", "1000 Wh"="1 kWh", "2000 Wh"="2 kWh", "3000 Wh"="3 kWh" ]
        Text item=E3DC_IdlePeriods        label="Idle Periods"
    }
    Frame label="Emergency Power" {
        Setpoint item=E3DC_EmergencyPercent label="Reserve [%.0f %%]" minValue=0 maxValue=100 step=5
        Text item=E3DC_EmergencyAvailable   label="Available [%s]"
    }
    Frame label="Energy Day" {
        Text item=E3DC_DaySolar         label="Solar [%.1f %unit%]"
        Text item=E3DC_DayHouse         label="House [%.1f %unit%]"
        Text item=E3DC_DayGridIn        label="Grid Supply [%.1f %unit%]"
        Text item=E3DC_DayGridOut       label="Grid Feed [%.1f %unit%]"
        Text item=E3DC_DayAutarky       label="Autarky [%.0f %%]"
    }
    Frame label="Wallbox" {
        Text item=E3DC_WallboxPlugged   label="Plugged [%s]"
        Text item=E3DC_WallboxCharging  label="Charging [%s]"
        Switch item=E3DC_WallboxSunMode label="Sun Mode"
        Switch item=E3DC_WallboxToggle  label="Suspend / Resume" mappings=[ ON="Toggle" ]
        Setpoint item=E3DC_WallboxMaxCurrent label="Max Current [%.0f %unit%]" minValue=6 maxValue=16 step=1
        Text item=E3DC_WallboxPowerL1   label="Power L1 [%.0f %unit%]"
        Text item=E3DC_WallboxTotal     label="Total [%.1f %unit%]"
    }
    Frame label="Power Meter" {
        Text item=E3DC_MeterPowerL1     label="Power L1 [%.0f %unit%]"
        Text item=E3DC_MeterVoltageL1   label="Voltage L1 [%.1f %unit%]"
    }
    Frame label="Battery Pack" {
        Text item=E3DC_PackVoltage      label="Voltage [%.1f %unit%]"
        Text item=E3DC_PackSoh          label="SOH [%.0f %%]"
        Text item=E3DC_ModuleSoc        label="Module SOC [%.0f %%]"
    }
}
```

### Rules

The writable command channels need no thing action. A rule sends the command to the item:

```java
// force the battery to charge with 2000 W for 60 poll cycles, then return to automatic
E3DC_SetPowerMode.sendCommand("charge:2000:60")

// end a forced mode at once
E3DC_SetPowerMode.sendCommand("auto")

// start a manual charge with 1500 Wh
E3DC_SetManualCharge.sendCommand("1500 Wh")

// block battery charging around the clock, for example while the electricity price is high
E3DC_BlockCharging.sendCommand(ON)

// release it again
E3DC_BlockCharging.sendCommand(OFF)

// keep 30 % of the battery for emergency power
E3DC_EmergencyPercent.sendCommand(30)

// replace all idle periods
E3DC_IdlePeriods.sendCommand("weekend:saturday,sunday:charge:true:08:00-18:00")
```

A rule that charges the battery from the grid for one hour at night:

```java
rule "E3DC night charge"
when
    Time cron "0 0 2 * * ?"
then
    E3DC_SetPowerMode.sendCommand("grid_charge:3000:720")
end
```

With a refresh interval of 5 seconds, 720 cycles last one hour; after that the device returns to automatic. The idle periods can also be read and replaced with the
[thing actions](#thing-actions), see the examples there.

## Notes on Device Support

The RSCP interface is not published in a complete specification, so not every device implements every
data point listed above. The binding requests the values it knows and stops requesting a value after
the device has answered with an error several times in a row. Such a channel simply stays without a
value on that device instead of being reported as zero.
