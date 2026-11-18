# BYD Battery-Box Binding

This binding reads the BYD Battery-Box Premium HVS/HVM home batteries over their local network interface.
Its focus is long-term monitoring of the battery's health: every cell voltage and cell temperature, balancing, state of charge and state of health, and the battery's own event logs.
All values can be persisted and used in rules, for example to be alerted when the voltage spread between the cells grows.

The binding is read-only: it does not change any setting of the battery.
It reads data, and sends only the two commands that make the battery measure its cells and load its event log.
The BYD configuration tool sends the same commands.
For towers with more than 128 cells, a third command can be enabled, see [Cells Beyond 128](#cells-beyond-128).

## Supported Things

| Thing Type | Description                                                                          |
|------------|--------------------------------------------------------------------------------------|
| `battery`  | One BYD Battery-Box Premium system, addressed through its BMU (battery control unit) |

Towers and modules are not separate Things, but channel groups of the `battery` Thing.
Channel groups are defined for up to 3 towers with up to 8 modules each.
After the first reading, the binding removes the channels of towers and modules the battery does not have.

| Battery                                   | Status                                                                                               |
|-------------------------------------------|------------------------------------------------------------------------------------------------------|
| Battery-Box Premium HVS, BMU firmware 3.x | Tested (HVS 7.7, 1 tower with 3 modules)                                                             |
| Battery-Box Premium HVS with 5 modules    | Tested with recorded data of 2 towers with 5 modules each; see [Cells Beyond 128](#cells-beyond-128) |
| Battery-Box Premium HVM                   | Untested. Supported by the protocol (16 cells and 8 temperature sensors per module).                 |
| Several towers in parallel                | Tested with recorded data of 2 towers                                                                |
| Battery-Box Premium LVS, HVL              | Not supported. Status values may work, cell details are not available.                               |
| Battery-Box HVB / BCU 2.0                 | Not supported                                                                                        |

## Prerequisites

### Only One Connection at a Time

The battery accepts **only one TCP connection at a time**.
While the BYD app (Be Connect), another tool or another home automation system is connected, the binding cannot read the battery and the Thing goes `OFFLINE` with the message that another client is probably connected.
The binding opens a connection only for the few seconds each reading takes, and closes it right after, so other tools can be used between two readings.
It retries automatically, waiting longer after each failed attempt (at most 15 minutes).

### Network

The battery listens on TCP port 8080.
It can be reached in two ways:

- **Ethernet**: the BMU's LAN port, connected to your home network. Look up the address the battery received from your DHCP server.
- **Wi-Fi access point**: the BMU's own access point, with the battery at `192.168.16.254`. The openHAB host, or another host in between, needs a Wi-Fi interface connected to this access point with a static address, e.g. `192.168.16.50/24`, and no default route.

If openHAB runs on a different host than the one connected to the battery's access point, forward the port on the connected host, for example with `socat`:

```shell
socat TCP-LISTEN:8080,fork,reuseaddr,max-children=1 TCP:192.168.16.254:8080
```

Then configure the address of the forwarding host as `hostname`.
The forward does not lift the one-connection limit: clients connected through the forward and directly to the battery still exclude each other.

## Discovery

Discovery is not supported.
The battery offers no way to be found on the network.
Add the Thing manually with its hostname.

## Thing Configuration

### `battery` Thing Configuration

| Name                | Type    | Description                                                                                     | Default | Required | Advanced |
|---------------------|---------|-------------------------------------------------------------------------------------------------|---------|----------|----------|
| `hostname`          | text    | Hostname or IP address of the battery, or of a forwarding host                                  | N/A     | yes      | no       |
| `port`              | integer | TCP port                                                                                        | 8080    | no       | yes      |
| `refreshInterval`   | integer | Interval in seconds for reading the status (minimum 30)                                         | 60      | no       | no       |
| `detailInterval`    | integer | Interval in seconds for measuring the cell voltages and temperatures, `0` disables it           | 600     | no       | no       |
| `eventLogInterval`  | integer | Interval in seconds for reading the event logs, `0` disables it                                 | 600     | no       | no       |
| `readExtendedCells` | boolean | Also read cells 129–160 of HVS towers with 5 modules, see [Cells Beyond 128](#cells-beyond-128) | false   | no       | yes      |
| `connectionTimeout` | integer | Timeout in seconds for connecting and for each reply                                            | 5       | no       | yes      |

Cell details and event logs are read in the same connection as the status, so their intervals are rounded up to a multiple of `refreshInterval`.
A cell measurement keeps the connection busy for about 2–3 seconds per tower.

### Cells Beyond 128

A cell measurement returns at most 128 cell voltages per tower.
An HVS tower with 5 modules has 160 cells, so the voltages of its 5th module are missing; its temperatures are always read.
With `readExtendedCells` enabled, the binding measures such a tower a second time to read cells 129–160.
To request this second measurement, it writes the text `DEBUG` to a battery register before it, as other integrations (ioBroker, Home Assistant, smarthomeNG) do.
On the tested system, this write affected only the following measurement; the next regular measurement was normal again.
The second measurement adds about 3 seconds per tower.

Without the option, the cell voltages and the module voltage of the 5th module are `UNDEF`.
The number of balancing cells includes cells 129–160 only with the option enabled; where their balancing flags are stored is assumed, not yet verified.
Towers with at most 128 cells are never measured a second time, even with the option enabled.

### Properties

| Property                       | Description                                                        |
|--------------------------------|--------------------------------------------------------------------|
| `serialNumber`                 | Serial number of the system, as reported by the battery            |
| `modelId`                      | Module type: `HVS` or `HVM`                                        |
| `firmwareVersion`              | Firmware of the active BMU section, e.g. `3.26-B`                  |
| `bmuFirmwareA`, `bmuFirmwareB` | Firmware of the two BMU sections                                   |
| `bmsFirmware`                  | Firmware of the active BMS section                                 |
| `towers`                       | Number of towers                                                   |
| `modulesPerTower`              | Number of modules per tower                                        |
| `cellsPerModule`               | Number of cells per module                                         |
| `temperatureSensorsPerModule`  | Number of temperature sensors per module                           |
| `parameterTable`               | Version of the parameter table                                     |
| `inverterType`                 | Inverter setting of the battery (raw number)                       |
| `application`                  | Application setting of the battery (raw number)                    |
| `tower<n>SerialNumber`         | Serial number of the BMS of tower n (after the first cell reading) |
| `tower<n>Firmware`             | Firmware of the two BMS sections of tower n                        |

## Channels

The current is positive while the battery discharges and negative while it charges, as reported by the battery.
The same applies to the power.

### Group `status`

Read every `refreshInterval`.

| Channel                | Type                     | Description                                                   |
|------------------------|--------------------------|---------------------------------------------------------------|
| `state-of-charge`      | Number:Dimensionless     | State of charge (1 % resolution)                              |
| `state-of-health`      | Number:Dimensionless     | State of health                                               |
| `voltage`              | Number:ElectricPotential | Battery voltage                                               |
| `output-voltage`       | Number:ElectricPotential | Voltage behind the main relay                                 |
| `current`              | Number:ElectricCurrent   | Battery current                                               |
| `power`                | Number:Power             | Current × output voltage                                      |
| `max-cell-voltage`     | Number:ElectricPotential | Highest cell voltage (10 mV resolution)                       |
| `min-cell-voltage`     | Number:ElectricPotential | Lowest cell voltage (10 mV resolution)                        |
| `cell-voltage-spread`  | Number:ElectricPotential | Highest minus lowest cell voltage (10 mV resolution)          |
| `max-cell-temperature` | Number:Temperature       | Highest cell temperature                                      |
| `min-cell-temperature` | Number:Temperature       | Lowest cell temperature                                       |
| `bmu-temperature`      | Number:Temperature       | Temperature of the BMU                                        |
| `charged-energy`       | Number:Energy            | Lifetime charged energy                                       |
| `discharged-energy`    | Number:Energy            | Lifetime discharged energy                                    |
| `efficiency`           | Number:Dimensionless     | Lifetime discharged energy divided by lifetime charged energy |
| `error`                | Switch                   | `ON` while the battery reports a system error                 |
| `errors`               | String                   | The system errors as text, or `None`                          |

### Group `tower-<n>`

One group per tower, read every `detailInterval`.
Minimum, maximum, average and spread are calculated from all cells of the tower; cells and sensors are numbered from 1 within the tower.

| Channel                       | Type                     | Description                                             |
|-------------------------------|--------------------------|---------------------------------------------------------|
| `state-of-charge`             | Number:Dimensionless     | State of charge (0.1 % resolution)                      |
| `state-of-health`             | Number:Dimensionless     | State of health                                         |
| `voltage`                     | Number:ElectricPotential | Tower voltage                                           |
| `output-voltage`              | Number:ElectricPotential | Voltage behind the main relay                           |
| `current`                     | Number:ElectricCurrent   | Tower current                                           |
| `charged-energy`              | Number:Energy            | Lifetime charged energy of the tower (Wh resolution)    |
| `discharged-energy`           | Number:Energy            | Lifetime discharged energy of the tower (Wh resolution) |
| `max-cell-voltage`            | Number:ElectricPotential | Highest cell voltage                                    |
| `max-cell-voltage-cell`       | Number                   | Number of the cell with the highest voltage             |
| `min-cell-voltage`            | Number:ElectricPotential | Lowest cell voltage                                     |
| `min-cell-voltage-cell`       | Number                   | Number of the cell with the lowest voltage              |
| `average-cell-voltage`        | Number:ElectricPotential | Average cell voltage                                    |
| `cell-voltage-spread`         | Number:ElectricPotential | Highest minus lowest cell voltage (1 mV resolution)     |
| `max-cell-temperature`        | Number:Temperature       | Highest cell temperature                                |
| `max-cell-temperature-sensor` | Number                   | Number of the sensor with the highest temperature       |
| `min-cell-temperature`        | Number:Temperature       | Lowest cell temperature                                 |
| `min-cell-temperature-sensor` | Number                   | Number of the sensor with the lowest temperature        |
| `balancing-cells`             | Number                   | Number of cells currently being balanced                |
| `warning`                     | Switch                   | `ON` while the tower reports a warning                  |
| `warnings`                    | String                   | The tower warnings as text, or `None`                   |

### Group `tower-<n>-module-<m>`

One group per module, read every `detailInterval`.
Module m holds cells `(m-1) × 32 + 1` to `m × 32` of the tower for HVS (16 cells per module for HVM), in the order the battery reports them.
HVM modules only have the channels `cell-voltage-01` … `cell-voltage-16` and `cell-temperature-01` … `cell-temperature-08`.

| Channel                                       | Type                     | Description                                     |
|-----------------------------------------------|--------------------------|-------------------------------------------------|
| `voltage`                                     | Number:ElectricPotential | Sum of the cell voltages of the module          |
| `max-cell-voltage`                            | Number:ElectricPotential | Highest cell voltage of the module              |
| `min-cell-voltage`                            | Number:ElectricPotential | Lowest cell voltage of the module               |
| `average-cell-voltage`                        | Number:ElectricPotential | Average cell voltage of the module              |
| `cell-voltage-spread`                         | Number:ElectricPotential | Highest minus lowest cell voltage of the module |
| `cell-voltage-01` … `cell-voltage-32`         | Number:ElectricPotential | Voltage of each cell (HVM: 01 … 16)             |
| `cell-temperature-01` … `cell-temperature-12` | Number:Temperature       | Temperature of each sensor (HVM: 01 … 08)       |

### Group `events`

Read every `eventLogInterval` from the BMU and from each tower's BMS.

| Channel           | Type     | Description                                                                                  |
|-------------------|----------|----------------------------------------------------------------------------------------------|
| `last-event`      | String   | Newest log entry, e.g. `BMS 1: Stop discharging (SoC 7 %, 305.3 V, 0.0 A)`                   |
| `last-event-time` | DateTime | Time of the newest log entry                                                                 |
| `event`           | Trigger  | Fired once for every new log entry, with the source and event code as payload, e.g. `BMS1:7` |

The battery keeps the latest 20 entries per unit.
After a restart of the binding, only entries that are newer than the first reading fire the trigger.
The battery clock is not synchronized; the binding corrects the log times by the clock offset it measures (see `bmu-clock-offset`).

### Group `diagnostics`

| Channel              | Type        | Description                                                                              |
|----------------------|-------------|------------------------------------------------------------------------------------------|
| `bmu-clock-offset`   | Number:Time | Deviation of the battery clock, read as UTC, from the openHAB clock; positive when ahead |
| `last-detail-update` | DateTime    | Time of the last successful cell measurement                                             |

## Full Example

### Thing Configuration

```java
Thing bydbatterybox:battery:home "BYD Battery-Box" [ hostname="192.168.69.9", refreshInterval=60, detailInterval=600, eventLogInterval=600 ]
```

### Item Configuration

```java
Group gBattery "Battery" ["Battery"]

Number:Dimensionless     BYD_SoC          "State of Charge [%.0f %%]"     <batterylevel> (gBattery) ["Measurement", "StateOfCharge"] { channel="bydbatterybox:battery:home:status#state-of-charge" }
Number:Dimensionless     BYD_SoH          "State of Health [%.0f %%]"                    (gBattery) ["Measurement"]                  { channel="bydbatterybox:battery:home:status#state-of-health" }
Number:Power             BYD_Power        "Power [%.0f W]"                               (gBattery) ["Measurement", "Power"]         { channel="bydbatterybox:battery:home:status#power" }
Switch                   BYD_Error        "Error"                                        (gBattery) ["Alarm"]                        { channel="bydbatterybox:battery:home:status#error" }
String                   BYD_Errors       "Errors [%s]"                                  (gBattery) ["Status"]                       { channel="bydbatterybox:battery:home:status#errors" }

Number:ElectricPotential BYD_CellSpread   "Cell Spread [%.0f mV]"                        (gBattery) ["Measurement", "Voltage"]       { channel="bydbatterybox:battery:home:tower-1#cell-voltage-spread", unit="mV" }
Number:ElectricPotential BYD_CellMin      "Lowest Cell [%.0f mV]"                        (gBattery) ["Measurement", "Voltage"]       { channel="bydbatterybox:battery:home:tower-1#min-cell-voltage", unit="mV" }
Number                   BYD_CellMinNo    "Lowest Cell No. [%d]"                         (gBattery)                                  { channel="bydbatterybox:battery:home:tower-1#min-cell-voltage-cell" }
Number:ElectricPotential BYD_M1_Spread    "Module 1 Spread [%.0f mV]"                    (gBattery) ["Measurement", "Voltage"]       { channel="bydbatterybox:battery:home:tower-1-module-1#cell-voltage-spread", unit="mV" }
Number:ElectricPotential BYD_M1_Cell01    "Module 1 Cell 1 [%.0f mV]"                    (gBattery) ["Measurement", "Voltage"]       { channel="bydbatterybox:battery:home:tower-1-module-1#cell-voltage-01", unit="mV" }
Number:Temperature       BYD_M1_Temp01    "Module 1 Sensor 1 [%.0f °C]"                  (gBattery) ["Measurement", "Temperature"]   { channel="bydbatterybox:battery:home:tower-1-module-1#cell-temperature-01" }

String                   BYD_LastEvent    "Last Event [%s]"                              (gBattery) ["Status"]                       { channel="bydbatterybox:battery:home:events#last-event" }
DateTime                 BYD_LastEventTime "Last Event Time [%1$tF %1$tR]"               (gBattery) ["Status", "Timestamp"]          { channel="bydbatterybox:battery:home:events#last-event-time" }
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
    Channel "bydbatterybox:battery:home:events#event" triggered
then
    logInfo("battery", "Battery event {}: {}", receivedEvent, BYD_LastEvent.state)
end
```

The spread depends on the state of charge: near empty and near full, LiFePO4 cells show a much larger spread than in the middle range.
Compare values taken at a similar state of charge, or use a lower threshold in the middle range.

## Troubleshooting

| Thing status message                 | Meaning                                                                                                 |
|--------------------------------------|---------------------------------------------------------------------------------------------------------|
| Another client is probably connected | The battery accepted the connection and closed it right away. Close the BYD app or other tools.         |
| The battery cannot be reached        | Wrong hostname or port, the battery or the forwarding host is offline, or the Wi-Fi connection is down. |
| The battery did not answer in time   | The connection is slow or interrupted; increase `connectionTimeout` if this happens regularly.          |

If the battery cannot be reached or does not answer, the binding tries once more 20 seconds later, before it reports the Thing as `OFFLINE`.
After that, it retries at the next readings, waiting longer after each failed attempt.

Enable `TRACE` logging for `org.openhab.binding.bydbatterybox` to see every frame sent and received.

### Short Outages Without Internet Access

A battery that is blocked from the internet, for example by a router or firewall rule, was seen to stop answering on the network for about 15 seconds every 100 seconds, even to ping.
The retry after 20 seconds bridges such an outage.
If the Thing still changes between `OFFLINE` and `ONLINE` regularly with the message that the battery cannot be reached, check whether the battery is blocked from the internet: a `ping` to the battery that runs for a few minutes shows the outages.
Allowing the battery to access the internet made the outages disappear.

## Credits

BYD does not publish the protocol.
The binding was written from scratch, using the following projects to understand the protocol:

- [ioBroker.bydhvs](https://github.com/christianh17/ioBroker.bydhvs)
- [python-bydhvs](https://github.com/bbr111/python-bydhvs)
- [smarthomeNG plugin byd_bat](https://github.com/smarthomeNG/plugins/tree/develop/byd_bat), which also documents the event log

Readings were cross-checked with "BYD-Logger" by Olli from the Photovoltaik-Forum.
