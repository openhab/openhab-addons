# BYD Battery-Box Binding

This binding reads the BYD Battery-Box Premium HVS/HVM home batteries over your local network.
Its focus is long-term monitoring of the battery's health: every cell voltage and cell temperature, balancing, state of charge and state of health, and the battery's own event logs.
All values can be persisted and used in rules, for example to be alerted when the voltage spread between the cells grows.

The binding only reads from the battery and never changes its settings.
To get fresh cell values and the event log, it asks the battery to measure its cells and to load its log, as the BYD configuration tool does.
For towers with 160 cells, an additional measurement can be enabled, see [Towers With 160 Cells](#towers-with-160-cells).

## Supported Things

| Thing Type   | Description                                                                                     |
|--------------|-------------------------------------------------------------------------------------------------|
| `hv-premium` | One BYD Battery-Box Premium HVS/HVM system, addressed through its BMU (battery management unit) |

Towers and modules are not separate Things, but channel groups of the `hv-premium` Thing.
Channel groups are defined for up to 3 towers with up to 8 modules each.
After the first reading, the binding removes the channels of towers and modules your battery does not have.

| Battery                                   | Status                                                                                                                                |
|-------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| Battery-Box Premium HVS, BMU firmware 3.x | Tested (HVS 7.7, 1 tower with 3 modules)                                                                                              |
| Battery-Box Premium HVS with 5 modules    | Tested with recorded data of 2 towers with 5 modules each; see [Towers With 160 Cells](#towers-with-160-cells)                        |
| Battery-Box Premium HVM                   | Untested, but expected to work (16 cells and 8 temperature sensors per module)                                                        |
| Several towers in parallel                | Tested with recorded data of 2 towers                                                                                                 |
| Battery-Box Premium HVL                   | Partly supported: status values and event logs, no tower and cell details yet; see [Help Wanted: HVL Owners](#help-wanted-hvl-owners) |
| Battery-Box Premium LVS                   | Not supported. Status values may work, cell details are not available.                                                                |
| Battery-Box HVB / BCU 2.0                 | Not supported                                                                                                                         |

### Help Wanted: HVL Owners

With a Battery-Box Premium HVL, the binding reads the status values and the event logs, but no tower and cell details.
The cell layout of HVL modules is not known yet, and no HVL system was available for testing.
If you own an HVL and would like to help, please get in touch in the [community thread](https://community.openhab.org/t/byd-battery-box-5-2-0-6-0-0/171008).
A short recording of the battery's answers, taken with a script on any computer in your network, is enough to add tower and cell details for HVL.

## Before You Start

### Only One App at a Time

The battery accepts **only one connection at a time**.
While the BYD app (Be Connect), another tool or another home automation system is connected to the battery, openHAB cannot read it, and the Thing goes `OFFLINE` with the message that another client is probably connected.
openHAB connects only for the few seconds a reading takes, so you can use the BYD app between two readings.
If the battery is busy, the binding tries again later, waiting longer after each failed attempt (at most 15 minutes).

### Connecting the Battery to Your Network

The battery is reached on port 8080, in one of two ways:

- **Ethernet**: the LAN port of the BMU, connected to your home network. Look up the address the battery received from your router (DHCP server).
- **Wi-Fi access point**: the BMU's own access point, with the battery at `192.168.16.254`. The openHAB host, or another host in between, needs a Wi-Fi interface connected to this access point with a static address, e.g. `192.168.16.50/24`, and no default route.

If openHAB runs on a different host than the one connected to the battery's access point, forward the port on the connected host, for example with `socat`:

```shell
socat TCP-LISTEN:8080,fork,reuseaddr,max-children=1 TCP:192.168.16.254:8080
```

Then enter the address of the forwarding host as `hostname`.
The one-connection limit still applies: an app connected through the forwarding host and an app connected directly to the battery still exclude each other.

## Discovery

Discovery is not supported, because the battery cannot be found automatically on the network.
Add the Thing manually with its hostname or IP address.

## Thing Configuration

### `hv-premium` Thing Configuration

| Name                | Type    | Description                                                                                           | Default | Required | Advanced |
|---------------------|---------|-------------------------------------------------------------------------------------------------------|---------|----------|----------|
| `hostname`          | text    | Hostname or IP address of the battery, or of the forwarding host                                      | N/A     | yes      | no       |
| `port`              | integer | Port of the battery                                                                                   | 8080    | no       | yes      |
| `refreshInterval`   | integer | Seconds between two status readings (minimum 30)                                                      | 60      | no       | no       |
| `detailInterval`    | integer | Seconds between two cell measurements, `0` turns them off                                             | 600     | no       | no       |
| `eventLogInterval`  | integer | Seconds between two event log readings (at least 150), `0` turns them off                             | 600     | no       | no       |
| `readExtendedCells` | boolean | Also read cells 129–160 of towers with 5 modules, see [Towers With 160 Cells](#towers-with-160-cells) | false   | no       | yes      |
| `connectionTimeout` | integer | Seconds to wait for the battery to connect and to answer                                              | 5       | no       | yes      |

Cell measurements and event logs are read together with a status reading.
Their intervals are therefore rounded up to a multiple of `refreshInterval`: with `refreshInterval=60`, a `detailInterval` of 90 seconds results in a cell measurement every 120 seconds.
A cell measurement takes about 2–3 seconds per tower; during that time, the BYD app cannot connect.

### Towers With 160 Cells

In a regular measurement, the battery reports at most 128 cell voltages per tower.
An HVS tower with 5 modules has 160 cells, so the cell voltages and the module voltage of its 5th module stay `UNDEF`; its temperatures are always available.

With `readExtendedCells` enabled, the binding requests a second measurement for such towers to get cells 129–160, the same way other integrations (ioBroker, Home Assistant, smarthomeNG) do.
BYD does not document this measurement; on the tested system, it affected nothing beyond that one measurement.
It adds about 3 seconds per tower to each cell measurement.
Towers with at most 128 cells are never measured twice.

The number of balancing cells includes the 5th module only with the option enabled, and has not yet been verified for that module.

### Properties

| Property                       | Description                                                                                    |
|--------------------------------|------------------------------------------------------------------------------------------------|
| `serialNumber`                 | Serial number of the system, as reported by the battery                                        |
| `modelId`                      | Module type: `HVS` or `HVM`                                                                    |
| `firmwareVersion`              | BMU firmware currently in use, e.g. `3.26-B`                                                   |
| `bmuFirmwareA`, `bmuFirmwareB` | BMU firmware in slot A and slot B                                                              |
| `bmsFirmware`                  | BMS firmware currently in use                                                                  |
| `towers`                       | Number of towers                                                                               |
| `modulesPerTower`              | Number of modules per tower                                                                    |
| `cellsPerModule`               | Number of cells per module                                                                     |
| `temperatureSensorsPerModule`  | Number of temperature sensors per module                                                       |
| `nominalCapacity`              | Capacity of the system when new, e.g. `7.68 kWh` (HVS and HVM only, see [Capacity](#capacity)) |
| `parameterTable`               | Version of the parameter table                                                                 |
| `inverterType`                 | Inverter setting of the battery (as a number)                                                  |
| `application`                  | Application setting of the battery (as a number)                                               |
| `phase`                        | Phase setting of the battery (as a number)                                                     |
| `tower<n>SerialNumber`         | Serial number of the BMS of tower n (after the first cell measurement)                         |
| `tower<n>Firmware`             | BMS firmware of tower n, slot A and slot B                                                     |

## Channels

The current is positive while the battery discharges and negative while it charges, as reported by the battery.
The same applies to the power.

### Group `status`

Updated every `refreshInterval`.

| Channel                | Type                     | Description                                                                |
|------------------------|--------------------------|----------------------------------------------------------------------------|
| `state-of-charge`      | Number:Dimensionless     | State of charge (1 % resolution)                                           |
| `state-of-health`      | Number:Dimensionless     | State of health                                                            |
| `voltage`              | Number:ElectricPotential | Battery voltage                                                            |
| `output-voltage`       | Number:ElectricPotential | Voltage at the battery output, behind the main relay                       |
| `current`              | Number:ElectricCurrent   | Battery current                                                            |
| `power`                | Number:Power             | Battery power, calculated from current and output voltage                  |
| `max-cell-voltage`     | Number:ElectricPotential | Highest cell voltage (10 mV resolution)                                    |
| `min-cell-voltage`     | Number:ElectricPotential | Lowest cell voltage (10 mV resolution)                                     |
| `cell-voltage-spread`  | Number:ElectricPotential | Highest minus lowest cell voltage (10 mV resolution)                       |
| `max-cell-temperature` | Number:Temperature       | Highest cell temperature                                                   |
| `min-cell-temperature` | Number:Temperature       | Lowest cell temperature                                                    |
| `bmu-temperature`      | Number:Temperature       | Temperature of the BMU                                                     |
| `charged-energy`       | Number:Energy            | Lifetime charged energy                                                    |
| `discharged-energy`    | Number:Energy            | Lifetime discharged energy                                                 |
| `efficiency`           | Number:Dimensionless     | Lifetime discharged energy divided by lifetime charged energy              |
| `usable-capacity`      | Number:Energy            | Nominal capacity reduced by the state of health, see [Capacity](#capacity) |
| `stored-energy`        | Number:Energy            | Energy currently stored: usable capacity multiplied by the state of charge |
| `error`                | Switch                   | `ON` while the battery reports a system error                              |
| `errors`               | String                   | The system errors as text, or `None`                                       |

### Capacity

The battery does not report its capacity, so the binding calculates it from the number of towers and modules: 2.56 kWh per HVS module and 2.76 kWh per HVM module.
An HVS 7.7 with 3 modules has a nominal capacity of 7.68 kWh.

- `usable-capacity` is the nominal capacity multiplied by the state of health: with a state of health of 94 %, the HVS 7.7 has 7.22 kWh left.
- `stored-energy` is the usable capacity multiplied by the state of charge: at 50 %, it holds 3.61 kWh.
- Each tower has its own `usable-capacity`, calculated from its own state of health, which shows whether one tower ages faster than the others.

These values are only as precise as the state of charge and state of health reported by the battery, in steps of 1 %.
For batteries with an unknown module type, such as the HVL, they are `UNDEF`.

### Group `tower-<n>`

One group per tower, updated every `detailInterval`.
Minimum, maximum, average and spread cover all cells of the tower; cells and sensors are numbered from 1 within the tower.

| Channel                       | Type                     | Description                                                                               |
|-------------------------------|--------------------------|-------------------------------------------------------------------------------------------|
| `state-of-charge`             | Number:Dimensionless     | State of charge (0.1 % resolution)                                                        |
| `state-of-health`             | Number:Dimensionless     | State of health                                                                           |
| `voltage`                     | Number:ElectricPotential | Tower voltage                                                                             |
| `output-voltage`              | Number:ElectricPotential | Voltage at the tower output, behind the main relay                                        |
| `current`                     | Number:ElectricCurrent   | Tower current                                                                             |
| `charged-energy`              | Number:Energy            | Lifetime charged energy of the tower (Wh resolution)                                      |
| `discharged-energy`           | Number:Energy            | Lifetime discharged energy of the tower (Wh resolution)                                   |
| `usable-capacity`             | Number:Energy            | Nominal capacity of the tower reduced by its state of health                              |
| `efficiency`                  | Number:Dimensionless     | Lifetime discharged energy of the tower divided by its lifetime charged energy (advanced) |
| `max-cell-voltage`            | Number:ElectricPotential | Highest cell voltage                                                                      |
| `max-cell-voltage-cell`       | Number                   | Number of the cell with the highest voltage                                               |
| `min-cell-voltage`            | Number:ElectricPotential | Lowest cell voltage                                                                       |
| `min-cell-voltage-cell`       | Number                   | Number of the cell with the lowest voltage                                                |
| `average-cell-voltage`        | Number:ElectricPotential | Average cell voltage                                                                      |
| `cell-voltage-spread`         | Number:ElectricPotential | Highest minus lowest cell voltage (1 mV resolution)                                       |
| `max-cell-temperature`        | Number:Temperature       | Highest cell temperature                                                                  |
| `max-cell-temperature-sensor` | Number                   | Number of the sensor with the highest temperature                                         |
| `min-cell-temperature`        | Number:Temperature       | Lowest cell temperature                                                                   |
| `min-cell-temperature-sensor` | Number                   | Number of the sensor with the lowest temperature                                          |
| `average-cell-temperature`    | Number:Temperature       | Average temperature of all sensors of the tower                                           |
| `balancing-cells`             | Number                   | Number of cells currently being balanced                                                  |
| `warning`                     | Switch                   | `ON` while the tower reports a warning                                                    |
| `warnings`                    | String                   | The tower warnings as text, or `None`                                                     |
| `error`                       | Switch                   | `ON` while the tower reports an error                                                     |
| `errors`                      | String                   | The tower errors as text, e.g. `Main relay failure`, or `None`                            |

### Group `tower-<n>-module-<m>`

One group per module, updated every `detailInterval`.
Module m holds cells `(m-1) × 32 + 1` to `m × 32` of the tower for HVS (16 cells per module for HVM), in the order the battery reports them.
HVM modules only have the channels `cell-voltage-01` … `cell-voltage-16` and `cell-temperature-01` … `cell-temperature-08`.
A module's `balancing-cells` shows which module the battery keeps balancing, an early sign of a weak module.
It is `UNDEF` for the 5th module of a 160-cell tower without `readExtendedCells`.

| Channel                                       | Type                     | Description                                                       |
|-----------------------------------------------|--------------------------|-------------------------------------------------------------------|
| `voltage`                                     | Number:ElectricPotential | Sum of the cell voltages of the module                            |
| `max-cell-voltage`                            | Number:ElectricPotential | Highest cell voltage of the module                                |
| `min-cell-voltage`                            | Number:ElectricPotential | Lowest cell voltage of the module                                 |
| `average-cell-voltage`                        | Number:ElectricPotential | Average cell voltage of the module                                |
| `cell-voltage-spread`                         | Number:ElectricPotential | Highest minus lowest cell voltage of the module                   |
| `balancing-cells`                             | Number                   | Number of cells of the module currently being balanced (advanced) |
| `cell-voltage-01` … `cell-voltage-32`         | Number:ElectricPotential | Voltage of each cell (HVM: 01 … 16)                               |
| `cell-temperature-01` … `cell-temperature-12` | Number:Temperature       | Temperature of each sensor (HVM: 01 … 08)                         |

### Group `events`

Updated every `eventLogInterval` from the logs of the BMU and of each tower.

| Channel           | Type     | Description                                                                                  |
|-------------------|----------|----------------------------------------------------------------------------------------------|
| `last-event`      | String   | Newest log entry, e.g. `BMS 1: Stop discharging (SoC 7 %, 305.3 V, 0.0 A)`                   |
| `last-event-time` | DateTime | Time of the newest log entry                                                                 |
| `event`           | Trigger  | Fired once for every new log entry, with the source and event code as payload, e.g. `BMS1:7` |

The battery keeps several weeks of entries per unit and hands them out in pages of 20, newest first.
Each request returns the next older page, and only after a pause of about two minutes does the battery start at the newest page again.
The binding therefore reads the event logs at most every 150 seconds, also when `eventLogInterval` is set lower or a reading failed.
If more than 20 entries were written since the previous reading, it reads up to 5 pages, so that up to 100 new entries fire the trigger.
After a restart of openHAB or of the binding, entries that were already in the log do not fire the trigger again; only newer entries do.
The battery clock is not set automatically, so the binding corrects the times of the log entries by the measured deviation (see `bmu-clock-offset`).

### Group `diagnostics`

| Channel              | Type        | Description                                                                                        |
|----------------------|-------------|----------------------------------------------------------------------------------------------------|
| `bmu-clock-offset`   | Number:Time | How far the battery clock, taken as UTC, is ahead of the openHAB clock; negative when it is behind |
| `last-detail-update` | DateTime    | Time of the last successful cell measurement                                                       |

## Full Example

### Thing Configuration

```java
Thing bydbatterybox:hv-premium:home "BYD Battery-Box" [ hostname="192.168.69.9", refreshInterval=60, detailInterval=600, eventLogInterval=600 ]
```

### Item Configuration

```java
Group gBattery "Battery" ["Battery"]

Number:Dimensionless     BYD_SoC          "State of Charge [%.0f %%]"     <batterylevel> (gBattery) ["Measurement", "StateOfCharge"] { channel="bydbatterybox:hv-premium:home:status#state-of-charge" }
Number:Dimensionless     BYD_SoH          "State of Health [%.0f %%]"                    (gBattery) ["Measurement"]                  { channel="bydbatterybox:hv-premium:home:status#state-of-health" }
Number:Power             BYD_Power        "Power [%.0f W]"                               (gBattery) ["Measurement", "Power"]         { channel="bydbatterybox:hv-premium:home:status#power" }
Switch                   BYD_Error        "Error"                                        (gBattery) ["Alarm"]                        { channel="bydbatterybox:hv-premium:home:status#error" }
String                   BYD_Errors       "Errors [%s]"                                  (gBattery) ["Status"]                       { channel="bydbatterybox:hv-premium:home:status#errors" }

Number:ElectricPotential BYD_CellSpread   "Cell Spread [%.0f mV]"                        (gBattery) ["Measurement", "Voltage"]       { channel="bydbatterybox:hv-premium:home:tower-1#cell-voltage-spread", unit="mV" }
Number:ElectricPotential BYD_CellMin      "Lowest Cell [%.0f mV]"                        (gBattery) ["Measurement", "Voltage"]       { channel="bydbatterybox:hv-premium:home:tower-1#min-cell-voltage", unit="mV" }
Number                   BYD_CellMinNo    "Lowest Cell No. [%d]"                         (gBattery)                                  { channel="bydbatterybox:hv-premium:home:tower-1#min-cell-voltage-cell" }
Number:ElectricPotential BYD_M1_Spread    "Module 1 Spread [%.0f mV]"                    (gBattery) ["Measurement", "Voltage"]       { channel="bydbatterybox:hv-premium:home:tower-1-module-1#cell-voltage-spread", unit="mV" }
Number:ElectricPotential BYD_M1_Cell01    "Module 1 Cell 1 [%.0f mV]"                    (gBattery) ["Measurement", "Voltage"]       { channel="bydbatterybox:hv-premium:home:tower-1-module-1#cell-voltage-01", unit="mV" }
Number:Temperature       BYD_M1_Temp01    "Module 1 Sensor 1 [%.0f °C]"                  (gBattery) ["Measurement", "Temperature"]   { channel="bydbatterybox:hv-premium:home:tower-1-module-1#cell-temperature-01" }

String                   BYD_LastEvent    "Last Event [%s]"                              (gBattery) ["Status"]                       { channel="bydbatterybox:hv-premium:home:events#last-event" }
DateTime                 BYD_LastEventTime "Last Event Time [%1$tF %1$tR]"               (gBattery) ["Status", "Timestamp"]          { channel="bydbatterybox:hv-premium:home:events#last-event-time" }
```

To keep the history of every cell, link all `cell-voltage-*` and `cell-temperature-*` channels and persist them, e.g. with an `everyChange` strategy.
Cell voltages change in steps of 1 mV, so `everyChange` stores only real changes.

### Rule Example

Alert when the voltage spread between the cells of a tower exceeds 80 mV, and log every new battery event:

```java
rule "Battery cell spread alert"
when
    Item BYD_CellSpread changed
then
    val spread = (BYD_CellSpread.state as QuantityType<?>).toUnit("mV").doubleValue
    if (spread > 80) {
        logWarn("battery", "Cell voltage spread is {} mV, lowest cell is no. {}", spread, BYD_CellMinNo.state)
    }
end

rule "Battery event"
when
    Channel "bydbatterybox:hv-premium:home:events#event" triggered
then
    logInfo("battery", "Battery event {}: {}", receivedEvent, BYD_LastEvent.state)
end
```

The spread depends on the state of charge: near empty and near full, LiFePO4 cells show a much larger spread than in the middle range.
Compare values taken at a similar state of charge, or use a lower threshold in the middle range.

## Troubleshooting

### Thing Status Messages

| Thing status message                 | Meaning                                                                                                 |
|--------------------------------------|---------------------------------------------------------------------------------------------------------|
| Another client is probably connected | The battery refused the connection. Close the BYD app or the other tool.                                |
| The battery cannot be reached        | Wrong hostname or port, the battery or the forwarding host is offline, or the Wi-Fi connection is down. |
| The battery did not answer in time   | The connection is slow or interrupted; increase `connectionTimeout` if this happens regularly.          |

### When a Reading Fails

If the battery cannot be reached or does not answer, the binding repeats the reading 20 seconds later, before it reports the Thing as `OFFLINE`.
While the battery stays unreachable, the binding keeps trying at the next readings, waiting longer after each failed attempt.

### Short Interruptions Without Internet Access

If the battery is blocked from the internet, for example by a router or firewall rule, its network connection restarts every 103 seconds.
Each time, the battery does not answer on the network for about 19 seconds, not even to `ping`.
In a measurement over 14 hours, about one in five readings fell into such an interruption.
The repeated reading 20 seconds later always succeeded, so the values arrived only a little later and the Thing stayed `ONLINE`.
No setting is needed for this, and allowing the battery to access the internet made the interruptions disappear.

If the Thing still goes `OFFLINE` regularly with the message that the battery cannot be reached, the cause is probably a different one: check the cable or Wi-Fi connection and the battery's address.

### Logging

Set the log level of `org.openhab.binding.bydbatterybox` to `DEBUG` to see every reading with its result and duration, or to `TRACE` to also see the data exchanged with the battery.

## Credits

BYD does not publish the protocol.
The binding was written from scratch, using the following projects to understand the protocol:

- [ioBroker.bydhvs](https://github.com/christianh17/ioBroker.bydhvs)
- [python-bydhvs](https://github.com/bbr111/python-bydhvs)
- [smarthomeNG plugin byd_bat](https://github.com/smarthomeNG/plugins/tree/develop/byd_bat), which also documents the event log
- [byd_battery_box for Home Assistant](https://github.com/TimWeyand/byd_battery_box), whose code tables and recorded event logs helped to decode the tower status words and more event entries

Readings were cross-checked with "BYD-Logger" by Olli from the Photovoltaik-Forum.
