# Fronius Binding

This binding uses the [Fronius Solar API V1](https://www.fronius.com/en/solar-energy/installers-partners/technical-data/all-products/system-monitoring/open-interfaces/fronius-solar-api-json-) to obtain data from Fronius devices.

It supports Fronius inverters, smart meters and Ohmpilot devices connected to a Fronius Datamanager 1.0 / 2.0, Fronius Datalogger or with integrated Solar API V1 support.

Inverters with integrated Solar API V1 support include:

- Fronius Galvo
- Fronius Primo
- Fronius Symo
- Fronius Symo Gen24
- Fronius Symo Gen24 Plus

## Supported Things

| Thing Type      | Description                                                                                                                                                    |
|-----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `bridge`        | The Bridge                                                                                                                                                     |
| `powerinverter` | Fronius Galvo, Symo and other Fronius inverters: You can add multiple inverters that depend on the same datalogger with different device ids. (default id = 1) |
| `battery`       | Fronius Battery: Battery devices that map to storage controller fields (default id = 0)                                                                        |
| `meter`         | Fronius Smart Meter: You can add multiple smart meters with different device ids. (default id = 0)                                                             |
| `ohmpilot`      | Fronius Ohmpilot (default id = 0)                                                                                                                              |

## Discovery

There is no discovery implemented. You have to create your Things manually and specify the hostname or IP address of the Datalogger and the device id.

## Binding Configuration

The binding has no configuration options, all configuration is done at `bridge`, `powerinverter`, `meter` or `ohmpilot` level.

## Thing Configuration

### Bridge Thing Configuration

| Parameter         | Description                                                                    | Required |
|-------------------|--------------------------------------------------------------------------------|----------|
| `hostname`        | The hostname or IP address of your Fronius Datamanager, Datalogger or inverter | Yes      |
| `username`        | The username to authenticate with the inverter settings for battery control    | No       |
| `password`        | The password to authenticate with the inverter settings for battery control    | No       |
| `refreshInterval` | Refresh interval in seconds                                                    | No       |
| `scheme`          | Set the protocol scheme that is used to connect to your device (default: http) | No       |

### Powerinverter Thing Configuration

| Parameter  | Description                                |
|------------|--------------------------------------------|
| `deviceId` | The identifier of your device (Default: 1) |

### Battery Thing Configuration

| Parameter                        | Description                                                                                                                                                                                    |
|----------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `deviceId`                       | The identifier of your battery device (Default: 0)                                                                                                                                             |
| `batterySettingsRefreshInterval` | The interval in minutes at which the battery settings channels are read from the inverter's config API. Reading them requires a login, therefore the interval should be kept high. Set to 0 to disable reading the battery settings from the inverter. (Default: 5) |

### Meter Thing Configuration

| Parameter  | Description                                     |
|------------|-------------------------------------------------|
| `deviceId` | The identifier of your smart meter (Default: 0) |

### Ohmpilot Thing Configuration

| Parameter  | Description                                  |
|------------|----------------------------------------------|
| `deviceId` | The identifier of your ohmpilot (Default: 0) |

## Channels

Most channel ids have been renamed to follow the naming conventions.
The old channel ids remain functional for backward compatibility, but are deprecated and hidden as advanced channels - please migrate your item links to the new channel ids.

### `powerinverter` Thing Channels

| Channel ID                           | Item Type                | Description                                                                                                       |
|--------------------------------------|--------------------------|-------------------------------------------------------------------------------------------------------------------|
| `ac-power`             | Number:Power             | AC Power generated                                                                                                |
| `dc-power`             | Number:Power             | DC Power calculated from DC voltage * DC current                                                                  |
| `dc-power-2`            | Number:Power             | DC Power generated by MPPT tracker 2                                                                              |
| `dc-power-3`            | Number:Power             | DC Power generated by MPPT tracker 3                                                                              |
| `ac-frequency`             | Number:Frequency         | AC frequency                                                                                                      |
| `ac-current`             | Number:ElectricCurrent   | AC current                                                                                                        |
| `dc-current`             | Number:ElectricCurrent   | DC current                                                                                                        |
| `dc-current-2`            | Number:ElectricCurrent   | DC current of MPPT tracker 2                                                                                      |
| `dc-current-3`            | Number:ElectricCurrent   | DC current of MPPT tracker 3                                                                                      |
| `ac-voltage`             | Number:ElectricPotential | AC voltage                                                                                                        |
| `dc-voltage`             | Number:ElectricPotential | DC voltage                                                                                                        |
| `dc-voltage-2`            | Number:ElectricPotential | DC voltage of MPPT tracker 2                                                                                      |
| `dc-voltage-3`            | Number:ElectricPotential | DC voltage of MPPT tracker 3                                                                                      |
| `day-energy`       | Number:Energy            | Energy generated on current day (GEN24/Tauro/Verto will always report null)                                       |
| `year-energy`            | Number:Energy            | Energy generated in current year (GEN24/Tauro/Verto will always report null)                                      |
| `total-energy`           | Number:Energy            | Energy generated overall                                                                                          |
| `error-code`  | Number                   | Device error code                                                                                                 |
| `status-code` | Number                   | Device status code<br />`0` - `6` Startup<br />`7` Running <br />`8` Standby<br />`9` Bootloading<br />`10` Error |
| `grid-power`              | Number:Power             | Grid Power (+ from grid, - to grid)                                                                               |
| `load-power`              | Number:Power             | Load Power (+ generator, - consumer)                                                                              |
| `battery-power`              | Number:Power             | Battery Power (+ discharge, - charge)                                                                             |
| `solar-power`                | Number:Power             | Solar Power (+ production)                                                                                        |
| `autonomy`                  | Number:Dimensionless     | The current relative autonomy in %                                                                                |
| `self-consumption`           | Number:Dimensionless     | The current relative self consumption in %                                                                        |
| `inverter-power`             | Number:Power             | Current power of the inverter, null if not running (+ produce/export, - consume/import)                           |
| `battery-soc`               | Number:Dimensionless     | Current state of charge of the battery connected to the inverter in percent.                                      |
| `powerflowinverter1power`            | Number:Power             | Current power of inverter 1, null if not running (+ produce/export, - consume/import) - DEPRECATED                |
| `powerflowinverter1soc`              | Number:Dimensionless     | Current state of charge of inverter 1 in percent - DEPRECATED                                                     |
| `backup-mode`                | Switch                   | Whether the inverter is currently operating in backup power mode (island operation), read-only                    |
| `battery-standby`            | Switch                   | Whether the battery is currently in standby, read-only                                                            |

### `battery` Thing Channels

| Channel ID         | Item Type                | Description                            |
|--------------------|--------------------------|----------------------------------------|
| `maximum-capacity`  | Number:Energy            | Current maximum battery capacity in Wh |
| `designed-capacity` | Number:Energy            | Designed battery capacity in Wh        |
| `dc-current`        | Number:ElectricCurrent   | DC current                             |
| `dc-voltage`        | Number:ElectricPotential | DC voltage                             |
| `soc`              | Number:Dimensionless     | State of charge in percent             |
| `status`           | String                   | Battery cell status code               |
| `enable`           | Number                   | Enable flag for the battery controller |
| `temperature`      | Number:Temperature       | Cell temperature                       |
| `timestamp`        | DateTime                 | Timestamp of the last measurement      |
| `soc-min`                 | Number:Dimensionless | Minimum state of charge of the battery in percent (writable, requires battery control)                                              |
| `soc-max`                 | Number:Dimensionless | Maximum state of charge of the battery in percent (writable, requires battery control)                                              |
| `backup-reserved-capacity` | Number:Dimensionless | Reserved battery capacity for backup power; not discharged below in normal operation (writable, requires battery control)           |
| `backup-critical-soc`      | Number:Dimensionless | State of charge at which the inverter warns about the battery running low in backup power mode (writable, requires battery control) |
| `charge-from-grid`         | Switch               | Whether charging the battery from the grid is allowed (writable, requires battery control)                                          |
| `calibration`            | Switch               | Whether the battery is currently performing a calibration charge (read-only, requires battery control)                              |
| `night-preservation-limit` | Number:Dimensionless | State of charge preserved over night to keep the battery system operational (read-only, requires battery control)                   |

The battery settings channels (`soc-min`, `soc-max`, `backup-reserved-capacity`, `backup-critical-soc`, `charge-from-grid`, `calibration` and `night-preservation-limit`) require the username and password to be configured in the bridge, see [Actions](#actions).
They read from and write to the inverter's settings through its config API.
Since reading these settings requires a login to the inverter, they are not part of the fast polling cycle, but refreshed every `batterySettingsRefreshInterval` minutes (and after each write), so changes made through the inverter's web UI show up with a delay.
Commands sent to the writable channels are applied to the inverter immediately.

### `meter` Thing Channels

| Channel ID              | Item Type                | Description                                                                                                                                                                                                              |
|-------------------------|--------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `enable`                | Number                   | 1 = enabled, 0 = disabled                                                                                                                                                                                                |
| `location`              | Number                   | 0 = grid interconnection point (primary meter)<br/> 1 = load (primary meter)   <br />3 = external generator (secondary meters)(multiple)<br />256-511 = subloads (secondary meters)(unique). Refer to Fronius Solar API. |
| `ac-current-phase-1`       | Number:ElectricCurrent   | AC Current on Phase 1                                                                                                                                                                                                    |
| `ac-current-phase-2`       | Number:ElectricCurrent   | AC Current on Phase 2                                                                                                                                                                                                    |
| `ac-current-phase-3`       | Number:ElectricCurrent   | AC Current on Phase 3                                                                                                                                                                                                    |
| `ac-voltage-phase-1`       | Number:ElectricPotential | AC Voltage on Phase 1                                                                                                                                                                                                    |
| `ac-voltage-phase-2`       | Number:ElectricPotential | AC Voltage on Phase 2                                                                                                                                                                                                    |
| `ac-voltage-phase-3`       | Number:ElectricPotential | AC Voltage on Phase 3                                                                                                                                                                                                    |
| `real-power-phase-1`       | Number:Power             | Real Power on Phase 1                                                                                                                                                                                                    |
| `real-power-phase-2`       | Number:Power             | Real Power on Phase 2                                                                                                                                                                                                    |
| `real-power-phase-3`       | Number:Power             | Real Power on Phase 3                                                                                                                                                                                                    |
| `real-power-sum`          | Number:Power             | Real Power summed up                                                                                                                                                                                                     |
| `power-factor-phase-1`     | Number                   | Power Factor on Phase 1                                                                                                                                                                                                  |
| `power-factor-phase-2`     | Number                   | Power Factor on Phase 2                                                                                                                                                                                                  |
| `power-factor-phase-3`     | Number                   | Power Factor on Phase 3                                                                                                                                                                                                  |
| `real-energy-consumed` | Number:Energy            | Real Energy consumed                                                                                                                                                                                                     |
| `real-energy-produced` | Number:Energy            | Real Energy produced                                                                                                                                                                                                     |

### `ohmpilot` Thing Channels

| Channel ID              | Item Type          | Description                                                                                                                                                              |
|-------------------------|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `real-energy-consumed` | Number:Energy      | Real Energy consumed                                                                                                                                                     |
| `real-power-sum`          | Number:Power       | Real Power                                                                                                                                                               |
| `temperature-1`   | Number:Temperature | Temperature                                                                                                                                                              |
| `error-code`             | Number             | Device error code                                                                                                                                                        |
| `status-code`             | Number             | Device state code<br />`0` up and running <br />`1` keep minimum temperature <br />`2` legionella protection <br />`3` critical fault<br />`4` fault<br />`5` boost mode |

## Properties

### `battery` Thing Properties

| Property       | Description                      |
|----------------|----------------------------------|
| `vendor`       | The manufacturer of the battery  |
| `modelId`      | The model name of the battery    |
| `serialNumber` | The serial number of the battery |

### `meter` Thing Properties

| Property       | Description                    |
|----------------|--------------------------------|
| `modelId`      | The model name of the meter    |
| `serialNumber` | The serial number of the meter |

### `ohmpilot` Thing Properties

| Property       | Description                       |
|----------------|-----------------------------------|
| `modelId`      | The model name of the ohmpilot    |
| `serialNumber` | The serial number of the ohmpilot |

## Actions

:::warning
Battery control uses the battery management's time-dependent battery control settings of the inverter settings and therefore overrides user-specified time of use settings.
Please note that user-specified time of use plans cannot be used together with battery control, as battery control will override the user-specified time of use settings.
:::

The `battery` Thing provides actions to control the battery charging and discharging behaviour of hybrid inverters, such as Symo Gen24 Plus, if username and password are provided in the bridge configuration.
The inverter must have the battery time of use plan settings available in the web interface.

For backward compatibility, the actions can also be retrieved through the `powerinverter` Thing, but this is deprecated - please retrieve them through the `battery` Thing instead.

You can retrieve the actions as follows:

:::: tabs

::: tab DSL

```java
val froniusBatteryActions = getActions("fronius", "fronius:battery:mybridge:mybattery")
```

:::

::: tab JS

```javascript
var froniusBatteryActions = actions.thingActions('fronius', 'fronius:battery:mybridge:mybattery');
```

:::

::: tab JRuby

In JRuby, the action methods are attached to the Thing object.

```rb
my_battery = things["fronius:battery:mybridge:mybattery"]

# call some actions
my_battery.prevent_battery_charging
```

:::

::::

Where the first parameter must always be `fronius` and the second must be the full Thing UID of the inverter.

### Available Actions

Once the actions instance has been retrieved, you can invoke the following methods:

- `resetBatteryControl()`: Remove all battery control schedules from the inverter.
- `holdBatteryCharge()`: Prevent the battery from discharging (removes all battery control schedules first and applies all the time).
- `addHoldBatteryChargeSchedule(LocalTime from, LocalTime until)`: Add a schedule to prevent the battery from discharging in the specified time range.
- `addHoldBatteryChargeSchedule(ZonedDateTime from, ZonedDateTime until)`: Add a schedule to prevent the battery from discharging in the specified time range.
- `forceBatteryCharging(QuantityType<Power> power)`: Force the battery to charge with the specified power (removes all battery control schedules first and applies all the time).
- `limitBatteryCharging(QuantityType<Power> power)`: Limit the battery charging power to at most the specified power (removes all battery control schedules first and applies all the time).
- `limitBatteryDischarging(QuantityType<Power> power)`: Limit the battery discharging power to at most the specified power (removes all battery control schedules first and applies all the time).
- `addForcedBatteryChargingSchedule(LocalTime from, LocalTime until, QuantityType<Power> power)`: Add a schedule to force the battery to charge with the specified power in the specified time range.
- `addForcedBatteryChargingSchedule(ZonedDateTime from, ZonedDateTime until, QuantityType<Power> power)`: Add a schedule to force the battery to charge with the specified power in the specified time range.
- `preventBatteryCharging()`: Prevent the battery from charging (removes all battery control schedules first and applies all the time).
- `addPreventBatteryChargingSchedule(LocalTime from, LocalTime until)`: Add a schedule to prevent the battery from charging in the specified time range.
- `addPreventBatteryChargingSchedule(ZonedDateTime from, ZonedDateTime until)`: Add a schedule to prevent the battery from charging in the specified time range.
- `forceBatteryDischarging(QuantityType<Power> power)`: Force the battery to discharge with the specified power (removes all battery control schedules first and applies all the time).
- `addForcedBatteryDischargingSchedule(LocalTime from, LocalTime until, QuantityType<Power> power)`: Add a schedule to force the battery to discharge with the specified power in the specified time range.
- `addForcedBatteryDischargingSchedule(ZonedDateTime from, ZonedDateTime until, QuantityType<Power> power)`: Add a schedule to force the battery to discharge with the specified power in the specified time range.
- `addSchedule(LocalTime from, LocalTime until, ScheduleType scheduleType, QuantityType<Power> power)`: Add a custom schedule with the specified type and power in the specified time range.
- `addSchedule(ZonedDateTime from, ZonedDateTime until, ScheduleType scheduleType, QuantityType<Power> power)`: Add a custom schedule with the specified type and power in the specified time range.
- `addBatteryChargingLimitSchedule(LocalTime from, LocalTime until, QuantityType<Power> power)`: Add a schedule to limit the battery charging power to at most the specified power in the specified time range.
- `addBatteryChargingLimitSchedule(ZonedDateTime from, ZonedDateTime until, QuantityType<Power> power)`: Add a schedule to limit the battery charging power to at most the specified power in the specified time range.
- `addBatteryDischargingLimitSchedule(LocalTime from, LocalTime until, QuantityType<Power> power)`: Add a schedule to limit the battery discharging power to at most the specified power in the specified time range.
- `addBatteryDischargingLimitSchedule(ZonedDateTime from, ZonedDateTime until, QuantityType<Power> power)`: Add a schedule to limit the battery discharging power to at most the specified power in the specified time range.
- `setBackupReservedBatteryCapacity(int percent)`: Set the reserved battery capacity for backup power.
- `setBackupReservedBatteryCapacity(PercentType percent)`: Set the reserved battery capacity for backup power.

All `add...Schedule` actions accept an optional trailing `weekdays` string parameter to restrict the schedule to specific weekdays, e.g. `addHoldBatteryChargeSchedule(from, until, "MON,TUE,SAT")`.
Weekdays are given as a comma-separated list of three-letter English weekday abbreviations or full names (e.g. `"MON,TUE"` or `"MONDAY,TUESDAY"`); omitting the parameter or passing `null` or an empty string applies the schedule to all days.

The `ScheduleType` enum has the following members:

- `CHARGE_MIN`
- `CHARGE_MAX`
- `DISCHARGE_MIN`
- `DISCHARGE_MAX`

Its full class name is `org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.ScheduleType`.
You can also provide the name of the enum member as string and the binding will parse the enum member from it.

All methods return a boolean value indicating whether the action was successful.

### Examples

:::: tabs

::: tab JS

```javascript
var froniusBatteryActions = actions.thingActions('fronius', 'fronius:battery:mybridge:mybattery');

froniusBatteryActions.resetBatteryControl();
froniusBatteryActions.holdBatteryCharge();
froniusBatteryActions.forceBatteryCharging(Quantity('5 kW'));

froniusBatteryActions.resetBatteryControl();
froniusBatteryActions.addHoldBatteryChargeSchedule(time.toZDT('18:00'), time.toZDT('22:00'));
froniusBatteryActions.addForcedBatteryChargingSchedule(time.toZDT('22:00'), time.toZDT('23:59'), Quantity('5 kW'));
froniusBatteryActions.addForcedBatteryChargingSchedule(time.toZDT('00:00'), time.toZDT('06:00'), Quantity('5 kW'));
froniusBatteryActions.addForcedBatteryDischargingSchedule(time.toZDT('07:00'), time.toZDT('09:00'));
froniusBatteryActions.addPreventBatteryChargingSchedule(time.toZDT('09:00'), time.toZDT('12:00'));

froniusBatteryActions.addSchedule(time.toZDT('10:00'), time.toZDT('11:00'), 'DISCHARGE_MAX', Quantity('500 W'));

froniusBatteryActions.setBackupReservedBatteryCapacity(50);
```

:::

::: tab JRuby

```rb
battery = things["fronius:battery:mybridge:mybattery"]

battery.reset_battery_control
battery.hold_battery_charge
battery.force_battery_charging(5 | "kW")

battery.reset_battery_control
battery.add_hold_battery_charge_schedule(LocalTime.parse("18:00"), LocalTime.parse("22:00"))
battery.add_forced_battery_charging_schedule(LocalTime.parse("22:00"), LocalTime.parse("23:59"), 5 | "kW")
battery.add_forced_battery_charging_schedule(LocalTime.parse("00:00"), LocalTime.parse("06:00"), 5 | "kW")
battery.add_forced_battery_discharging_schedule(LocalTime.parse("07:00"), LocalTime.parse("09:00"))
battery.add_prevent_battery_charging_schedule(LocalTime.parse("09:00"), LocalTime.parse("12:00"))

battery.add_schedule(LocalTime.parse("10:00"), LocalTime.parse("11:00"), 'DISCHARGE_MAX', 500 | "W")

battery.set_backup_reserved_battery_capacity(50)
```

:::

::::

## Full Example

demo.things:

```java
Bridge fronius:bridge:mybridge [hostname="192.168.66.148", refreshInterval=5, username="customer", password="someSecretPassword", scheme="http"] {
    Thing powerinverter myinverter [deviceId=1]
    Thing battery mybattery [deviceId=0]
    Thing meter mymeter [deviceId=0]
    Thing ohmpilot myohmpilot [deviceId=0]
}
```

Please note that `username` and `password` are only required if you want to use battery control Thing actions.

demo.items:

```java
Number:Power AC_Power { channel="fronius:powerinverter:mybridge:myinverter:ac-power" }
Number:Energy Day_Energy { channel="fronius:powerinverter:mybridge:myinverter:day-energy" }
Number:Energy Total_Energy { channel="fronius:powerinverter:mybridge:myinverter:total-energy" }
Number:Energy Year_Energy { channel="fronius:powerinverter:mybridge:myinverter:year-energy" }
Number:Frequency FAC { channel="fronius:powerinverter:mybridge:myinverter:ac-frequency" }
Number:ElectricCurrent IAC { channel="fronius:powerinverter:mybridge:myinverter:ac-current" }
Number:ElectricCurrent IDC { channel="fronius:powerinverter:mybridge:myinverter:dc-current" }
Number:ElectricPotential UAC { channel="fronius:powerinverter:mybridge:myinverter:ac-voltage" }
Number:ElectricPotential UDC { channel="fronius:powerinverter:mybridge:myinverter:dc-voltage" }
Number ErrorCode { channel="fronius:powerinverter:mybridge:myinverter:error-code" }
Number StatusCode { channel="fronius:powerinverter:mybridge:myinverter:status-code" }
Number:Power Grid_Power { channel="fronius:powerinverter:mybridge:myinverter:grid-power" }
Number:Power Load_Power { channel="fronius:powerinverter:mybridge:myinverter:load-power" }
Number:Power Battery_Power { channel="fronius:powerinverter:mybridge:myinverter:battery-power" }
Number:Power Production_Power { channel="fronius:powerinverter:mybridge:myinverter:solar-power" }
Number:Dimensionless Power_Autonomy { channel="fronius:powerinverter:mybridge:myinverter:autonomy" }
Number:Dimensionless Power_SelfConsumption { channel="fronius:powerinverter:mybridge:myinverter:self-consumption" }
Number:Power Inverter1_Power { channel="fronius:powerinverter:mybridge:myinverter:powerflowinverter1power" }
Number:Dimensionless Inverter1_SOC { channel="fronius:powerinverter:mybridge:myinverter:powerflowinverter1soc" }

Number Meter_Enable { channel="fronius:meter:mybridge:mymeter:enable" }
Number Meter_Location { channel="fronius:meter:mybridge:mymeter:location" }
Number:ElectricCurrent Meter_CurrentPhase1 { channel="fronius:meter:mybridge:mymeter:ac-current-phase-1" }
Number:ElectricCurrent Meter_CurrentPhase2 { channel="fronius:meter:mybridge:mymeter:ac-current-phase-2" }
Number:ElectricCurrent Meter_CurrentPhase3 { channel="fronius:meter:mybridge:mymeter:ac-current-phase-3" }
Number:Voltage Meter_VoltagePhase1 { channel="fronius:meter:mybridge:mymeter:ac-voltage-phase-1" }
Number:Voltage Meter_VoltagePhase2 { channel="fronius:meter:mybridge:mymeter:ac-voltage-phase-2" }
Number:Voltage Meter_VoltagePhase3 { channel="fronius:meter:mybridge:mymeter:ac-voltage-phase-3" }
Number:Power Meter_PowerPhase1 { channel="fronius:meter:mybridge:mymeter:real-power-phase-1" }
Number:Power Meter_PowerPhase2 { channel="fronius:meter:mybridge:mymeter:real-power-phase-2" }
Number:Power Meter_PowerPhase3 { channel="fronius:meter:mybridge:mymeter:real-power-phase-3" }
Number:Power Meter_PowerSum    { channel="fronius:meter:mybridge:mymeter:real-power-sum" }
Number Meter_PowerFactorPhase1 { channel="fronius:meter:mybridge:mymeter:power-factor-phase-1" }
Number Meter_PowerFactorPhase2 { channel="fronius:meter:mybridge:mymeter:power-factor-phase-2" }
Number Meter_PowerFactorPhase3 { channel="fronius:meter:mybridge:mymeter:power-factor-phase-3" }
Number:Energy Meter_EnergyConsumed { channel="fronius:meter:mybridge:mymeter:real-energy-consumed" }
Number:Energy Meter_EnergyProduced { channel="fronius:meter:mybridge:mymeter:real-energy-produced" }

Number:Energy Ohmpilot_EnergyConsumed { channel="fronius:ohmpilot:mybridge:myohmpilot:real-energy-consumed" }
Number:Power Ohmpilot_PowerSum { channel="fronius:ohmpilot:mybridge:myohmpilot:real-power-sum" }
Number:Temperature Ohmpilot_Temperature { channel="fronius:ohmpilot:mybridge:myohmpilot:temperature-1" }
Number Ohmpilot_State { channel="fronius:ohmpilot:mybridge:myohmpilot:status-code" }
Number Ohmpilot_Errorcode { channel="fronius:ohmpilot:mybridge:myohmpilot:error-code" }

Number:Energy Battery_MaxCapacity { channel="fronius:battery:mybridge:mybattery:maximum-capacity" }
Number:Energy Battery_DesignedCapacity { channel="fronius:battery:mybridge:mybattery:designed-capacity" }
Number:ElectricCurrent Battery_CurrentDC { channel="fronius:battery:mybridge:mybattery:dc-current" }
Number:ElectricPotential Battery_VoltageDC { channel="fronius:battery:mybridge:mybattery:dc-voltage" }
Number:Dimensionless Battery_SOC { channel="fronius:battery:mybridge:mybattery:soc" }
Number:Temperature Battery_Temperature { channel="fronius:battery:mybridge:mybattery:temperature" }
DateTime Battery_Timestamp { channel="fronius:battery:mybridge:mybattery:timestamp" }
```

Note: Make sure to turn on the **Night Mode** in the Display Settings on the Fronius inverter to keep it from going offline at night.
