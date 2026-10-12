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
| Battery Module | `battery-module` | Values of one battery cell module, such as its state of health.     |

A power plant is attached to the bridge, wallboxes and power meters are attached to the bridge as
well and are distinguished by their index.

## Discovery

There is no automatic discovery because the RSCP interface does not announce itself on the network.
The bridge and the things have to be added manually.

## Thing Configuration

### `bridge` Thing Configuration

| Name            | Type    | Description                                                      | Default | Required | Advanced |
|-----------------|---------|------------------------------------------------------------------|---------|----------|----------|
| host            | text    | Host name or IP address of the E3/DC device.                      | N/A     | yes      | no       |
| port            | integer | TCP port of the RSCP interface.                                   | 5033    | no       | yes      |
| userName        | text    | User name of the E3/DC account, usually the e-mail address.        | N/A     | yes      | no       |
| password        | text    | Password of the E3/DC account.                                    | N/A     | yes      | no       |
| rscpPassword    | text    | Encryption key configured on the device.                          | N/A     | yes      | no       |
| refreshInterval | integer | Interval the device is polled in seconds, between 1 and 300.      | 60      | no       | no       |
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

### `power-plant`, `wallbox`, `power-meter` and `battery-module` Thing Configuration

| Thing Type    | Name         | Type    | Description                                       | Default | Required |
|---------------|--------------|---------|---------------------------------------------------|---------|----------|
| `power-plant` | -            | -       | No configuration parameters.                       | -       | -        |
| `wallbox`     | wallboxIndex | integer | Zero based index of the wallbox on the device.     | 0       | yes      |
| `power-meter` | meterIndex   | integer | Zero based index of the power meter on the device. | 0       | yes      |
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

### Channel Group `grid`

| Channel       | Type   | Description                     |
|---------------|--------|---------------------------------|
| active-phases | Number | Number of phases currently in use |

### Channel Group `inverter`

| Channel      | Type                     | Description                                   |
|--------------|--------------------------|-----------------------------------------------|
| ac-power     | Number:Power             | Alternating current power of the inverter      |
| ac-voltage   | Number:ElectricPotential | Alternating current voltage of the inverter    |
| ac-current   | Number:ElectricCurrent   | Alternating current of the inverter            |
| ac-frequency | Number:Frequency         | Grid frequency measured by the inverter        |
| temperature  | Number:Temperature       | Temperature of the inverter                    |
| on-grid      | Switch                   | Whether the inverter is connected to the grid   |
| string-count | Number                   | Number of photovoltaic strings in use           |
| energy-total | Number:Energy            | Energy produced since commissioning             |
| energy-day   | Number:Energy            | Energy produced today                           |
| tracker-1-power | Number:Power          | Direct current power of the first tracker       |
| tracker-1-voltage | Number:ElectricPotential | Direct current voltage of the first tracker |
| tracker-1-current | Number:ElectricCurrent | Direct current of the first tracker           |
| tracker-2-power | Number:Power          | Direct current power of the second tracker      |
| tracker-2-voltage | Number:ElectricPotential | Direct current voltage of the second tracker |
| tracker-2-current | Number:ElectricCurrent | Direct current of the second tracker          |

The direct current values are reported once per photovoltaic tracker and carry no index of their own, so
the first two trackers are exposed as separate channels. The `string-count` channel reports how many
trackers the device actually uses.

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
| emergency-power-status    | Number       | State of the emergency power supply              |
| charging-lock             | Switch       | Battery charging is currently blocked            |
| discharging-lock          | Switch       | Battery discharging is currently blocked         |
| emergency-power-available | Switch       | Emergency power reserve is available             |
| charging-throttled        | Switch       | Battery charging is currently limited            |
| grid-in-limit             | Switch       | Grid consumption is currently limited            |
| charging-time-lock        | Switch       | Battery charging is blocked by a time window     |
| discharging-time-lock     | Switch       | Battery discharging is blocked by a time window  |
| weather-regulated-charge  | Switch       | Charging follows the weather forecast            |
| power-limits-used         | Switch       | Configured power limits are active               |
| powersave                 | Switch       | Power saving mode is active                      |
| derate-at-percent         | Number:Dimensionless | Share of the installed peak power at which the feed in is reduced |
| derate-at-power           | Number:Power | Power at which the feed in is reduced            |
| idle-periods-enabled      | Switch       | Whether the device uses the configured idle periods |
| idle-periods              | String       | The idle periods, one per line                    |
| manual-charge-energy      | Number:Energy | Energy of the manual charge started last        |

The `idle-periods` channel shows the periods during which the battery is locked against charging or
discharging. Each line reads `name:days:mode:active:hh:mm-hh:mm`, for example:

```text
weekend:saturday,sunday:charge:true:08:00-18:00
```

The days are a comma separated list of `monday` to `sunday`, and the mode is `charge` or `discharge`.
The channel is read only: the device replaces the whole list on every write, so changing it would mean
managing the list rather than setting one value.

### Channel Group `history`

The history group reports the energy counters and the two shares of a whole period. The device answers
the history containers only when the request states the period, which the binding derives from the
current date.

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

### Channel Group `reserve`

The reserve is the part of the battery the device keeps available for emergency power.

| Channel    | Type                 | Description                                              |
|------------|----------------------|----------------------------------------------------------|
| percent    | Number:Dimensionless | Share of the battery kept for emergency power             |
| energy     | Number:Energy        | Energy kept for emergency power                           |
| energy-max | Number:Energy        | Highest reserve the battery can hold for emergency power  |
| last-soc   | Number:Dimensionless | State of charge the battery was left with                 |

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
| `system`      | power-limits-used            | Switch       | Whether the configured power limits are active                               |
| `system`      | weather-regulated-charge     | Switch       | Whether charging follows the weather forecast                                |
| `system`      | mode                         | Number       | Operating mode, chosen from automatic, idle, discharge, charge and grid charge |
| `system`      | set-manual-charge            | Number:Power | Start a manual battery charge, an action rather than a value                 |
| `reserve`     | percent                      | Number:Dimensionless | Share of the battery to keep for emergency power                    |
| `reserve`     | energy                       | Number:Energy | Energy to keep for emergency power                                          |
| `wallbox`     | sun-mode                     | Switch       | Whether charging uses surplus photovoltaic power                             |
| `wallbox`     | suspended                    | Switch       | Whether the charging session is suspended                                    |
| `wallbox`     | auto-phase-switch            | Switch       | Whether the wallbox changes the number of phases by itself                    |
| `wallbox`     | min-charge-current           | Number:ElectricCurrent | Lowest charging current allowed, 1 to 32 A                  |
| `wallbox`     | max-charge-current           | Number:ElectricCurrent | Highest charging current allowed, 1 to 32 A                        |
| `wallbox`     | number-phases                | Number       | Number of phases used for charging, 1 or 3                                   |

A channel state is only updated after the device acknowledged the write, so a rejected command leaves
the channel untouched.

A wallbox value is written as the parameter of the request that names it, which is also how the value is
read: every wallbox control is one channel that reports and sets its value. The `plugged`, `locked` and
`charging` channels report states the device decides, so they are read only.

### Channel Group `wallbox`

| Channel            | Type                   | Description                               |
|--------------------|------------------------|-------------------------------------------|
| power-l1           | Number:Power           | Power on phase L1                         |
| power-l2           | Number:Power           | Power on phase L2                         |
| power-l3           | Number:Power           | Power on phase L3                         |
| energy-total       | Number:Energy          | Energy charged through this wallbox        |
| energy-solar       | Number:Energy          | Energy taken from the photovoltaic system  |
| soc                | Number:Dimensionless   | State of charge of the connected vehicle   |
| min-charge-current | Number:ElectricCurrent | Lowest charging current allowed            |
| max-charge-current | Number:ElectricCurrent | Highest charging current allowed           |
| number-phases      | Number                 | Number of phases used for charging         |
| auto-phase-switch  | Switch                 | Wallbox changes the number of phases by itself |
| key-state          | Switch                 | State of the wallbox key switch            |
| plugged            | Switch                 | A vehicle is connected                     |
| locked             | Switch                 | The connector is locked                    |
| charging           | Switch                 | A charging session is running              |
| suspended          | Switch                 | Charging is currently suspended            |
| sun-mode           | Switch                 | Charging uses surplus photovoltaic power   |

### Channel Group `meter`

| Channel         | Type                     | Description                         |
|-----------------|--------------------------|-------------------------------------|
| power-l1        | Number:Power             | Power on phase L1                   |
| power-l2        | Number:Power             | Power on phase L2                   |
| power-l3        | Number:Power             | Power on phase L3                   |
| voltage-l1      | Number:ElectricPotential | Voltage on phase L1                 |
| voltage-l2      | Number:ElectricPotential | Voltage on phase L2                 |
| voltage-l3      | Number:ElectricPotential | Voltage on phase L3                 |
| active-phases   | Number                   | Number of phases currently in use   |
| energy-l1       | Number:Energy            | Energy measured on phase L1         |
| energy-l2       | Number:Energy            | Energy measured on phase L2         |
| energy-l3       | Number:Energy            | Energy measured on phase L3         |
| max-phase-power | Number:Power             | Highest power measured on one phase |

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

## Full Example

### Thing Configuration

```java
Bridge e3dc:bridge:device "E3/DC Device" [ host="192.168.1.50", userName="owner", password="secret", rscpPassword="rscpSecret", refreshInterval=30 ] {
    Thing power-plant plant "E3/DC Power Plant"
    Thing wallbox wallbox0 "E3/DC Wallbox" [ wallboxIndex=0 ]
    Thing power-meter meter0 "E3/DC Power Meter" [ meterIndex=0 ]
}
```

### Item Configuration

```java
Number:Power         E3DC_SolarPower       "Solar Power [%.0f %unit%]"        { channel="e3dc:power-plant:device:plant:power#solar-power" }
Number:Power         E3DC_BatteryPower     "Battery Power [%.0f %unit%]"      { channel="e3dc:power-plant:device:plant:power#battery-power" }
Number:Power         E3DC_HousePower       "House Power [%.0f %unit%]"        { channel="e3dc:power-plant:device:plant:power#house-power" }
Number:Power         E3DC_GridPower        "Grid Power [%.0f %unit%]"         { channel="e3dc:power-plant:device:plant:power#grid-power" }
Number:Dimensionless E3DC_Autarky          "Autarky [%.0f %%]"                { channel="e3dc:power-plant:device:plant:power#autarky" }
Number:Dimensionless E3DC_SelfConsumption  "Self Consumption [%.0f %%]"       { channel="e3dc:power-plant:device:plant:power#self-consumption" }

Number:Dimensionless E3DC_BatterySoc       "Battery SOC [%.0f %%]"            { channel="e3dc:power-plant:device:plant:battery#soc" }

Number:Energy        E3DC_DaySolar         "Solar Day [%.1f %unit%]"          { channel="e3dc:power-plant:device:plant:history#day-solar" }
Number:Energy        E3DC_DayHouse         "House Day [%.1f %unit%]"          { channel="e3dc:power-plant:device:plant:history#day-house" }
Number:Energy        E3DC_DayGridIn        "Grid Supply Day [%.1f %unit%]"    { channel="e3dc:power-plant:device:plant:history#day-grid-in" }
Number:Energy        E3DC_DayGridOut       "Grid Feed Day [%.1f %unit%]"      { channel="e3dc:power-plant:device:plant:history#day-grid-out" }

Switch               E3DC_ChargingLock     "Charging Lock [%s]"               { channel="e3dc:power-plant:device:plant:system#charging-lock" }
String               E3DC_SerialNumber     "Serial Number [%s]"               { channel="e3dc:power-plant:device:plant:system#serial-number" }

Switch               E3DC_WallboxPlugged   "Wallbox Plugged [%s]"             { channel="e3dc:wallbox:device:wallbox0:wallbox#plugged" }
Switch               E3DC_WallboxCharging  "Wallbox Charging [%s]"            { channel="e3dc:wallbox:device:wallbox0:wallbox#charging" }
Number:Power         E3DC_WallboxPower     "Wallbox Power [%.0f %unit%]"      { channel="e3dc:wallbox:device:wallbox0:wallbox#power-l1" }

Number:Power         E3DC_MaxChargePower   "Max Charge Power [%.0f %unit%]"   { channel="e3dc:power-plant:device:plant:battery#max-charge-power" }
Number:Power         E3DC_MaxDischargePower "Max Discharge Power [%.0f %unit%]" { channel="e3dc:power-plant:device:plant:battery#max-discharge-power" }
Switch               E3DC_PowerLimits      "Power Limits [%s]"                { channel="e3dc:power-plant:device:plant:system#power-limits-used" }
Switch               E3DC_WeatherCharge    "Weather Charge [%s]"              { channel="e3dc:power-plant:device:plant:system#weather-regulated-charge" }
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
        Switch item=E3DC_ChargingLock   label="Charging Lock [%s]"
    }
    Frame label="Energy Day" {
        Text item=E3DC_DaySolar         label="Solar [%.1f %unit%]"
        Text item=E3DC_DayHouse         label="House [%.1f %unit%]"
        Text item=E3DC_DayGridIn        label="Grid Supply [%.1f %unit%]"
        Text item=E3DC_DayGridOut       label="Grid Feed [%.1f %unit%]"
    }
    Frame label="Wallbox" {
        Switch item=E3DC_WallboxPlugged  label="Plugged [%s]"
        Switch item=E3DC_WallboxCharging label="Charging [%s]"
    }
}
```

## Notes on Device Support

The RSCP interface is not published in a complete specification, so not every device implements every
data point listed above. The binding requests the values it knows and stops requesting a value after
the device has answered with an error several times in a row. Such a channel simply stays without a
value on that device instead of being reported as zero.
