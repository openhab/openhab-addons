# Solarman Logger Binding

Binding used to communicate with Solarman (IGEN-Tech) v5 based solar inverter data loggers in direct-mode over the local network.
More information about the different types of stick loggers is available on the [Solarman](https://www.solarmanpv.com/products/data-logger/stick-logger/) site.

These data loggers are used by inverters from a lot of manufacturers, just to name a few:

- Deye
- Sofar
- Solis
- ZCS Azzurro
- KStar

## Contents

- [Supported Things](#supported-things)
- [Hardware Test Coverage](#hardware-test-coverage)
- [Thing Configuration](#thing-configuration)
- [Channels](#channels), including [advanced channels](#advanced-channels) and the [complete catalog](docs/sunsynk-channels.md)
- [Writable Channels](#writable-channels)
- [Write Confirmation and Logging](#write-confirmation-and-logging)
- [Full Example](#full-example) for the legacy logger
- [Advanced Full Example](#advanced-full-example), including six SOC sliders
- [Local Installation and Development](#local-installation-and-development)

## Supported Things

The `solarman:logger` Thing supports reading data from a Solarman LSW-3 Stick Logger (it might also work with LSE-3 and maybe others) when connected to a supported inverter.

Legacy device coverage comes from the upstream inverter definitions and test reports.
See [Hardware Test Coverage](#hardware-test-coverage) for the reported devices and the limits of advanced-profile testing.

### Deye/Sunsynk Advanced Profiles

The legacy selections already cover some of the same inverter families and registers.
The advanced profiles add coverage and different conversion/control semantics; their entry count is not a count of entirely new functions.
See the [legacy/profile comparison](docs/profile-comparison.md) for the measured overlap and architecture options.
Select an opt-in advanced profile through inverterType on the same Solarman Logger Thing.
Existing legacy selections retain their definitions and channel IDs.

The logger includes opt-in advanced profiles based on the pinned [kellerza/sunsynk](https://github.com/kellerza/sunsynk) register definitions.
They provide monitoring and asynchronous control of supported Deye/Sunsynk inverter functions.
The existing `solarman:logger` Thing, YAML definitions, configuration and dynamic channel identifiers remain available.

| inverterType                | Profile                   | Channels, including source aliases |
| --------------------------- | ------------------------- | ---------------------------------- |
| `sunsynk_single_phase`      | Single-phase hybrid       | 150                                |
| `sunsynk_single_phase_16kw` | Single-phase 16 kW hybrid | 147                                |
| `sunsynk_three_phase_lv`    | Three-phase low voltage   | 251                                |
| `sunsynk_three_phase_hv`    | Three-phase high voltage  | 294                                |
| `sunsynk_micro_hybrid_ess`  | Micro hybrid ESS          | 86                                 |

Choose the inverterType matching the inverter model; profiles have different addresses, scaling and write limits.
Legacy selections accept refresh intervals of at least 1 second; advanced profiles require at least 5 seconds, although the shared logger configuration exposes the legacy minimum.
No existing Thing is automatically converted.
The full [channel and register catalog](docs/sunsynk-channels.md) lists every channel, Item dimension, ordered register tuple, scale, access flag, mask and bound.
The exported data includes all source aliases; aliases observing the same register do not add polling requests.
The runtime is Java-only.
Python 3.12 or newer is needed only to regenerate pinned definitions and differential test fixtures with the development tools.

Supported channel families include battery/BMS measurements and limits, grid and per-phase measurements, MPPT readings and computed PV totals, energy counters, faults and state, export and peak-shaving settings, generator/smart-load controls, six time-of-use periods and the device clock.
Only controls marked writable in the selected Python profile accept commands.
For example, micro ESS load limit and protection settings remain read-only.
The pinned micro ESS profile does not define individual MPPT power registers, so its common PV-total channel remains UNDEF rather than reporting a fabricated zero.

### Hardware Test Coverage

| Profile or family            | Reported device evidence                                                                                                                                            | Scope                                                                                                                               |
| ---------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| Legacy three-phase           | SUN-12K-SG04LP3-EU with LSE-3 RAW Modbus and a Wi-Fi V5 logger, as reported in the upstream README                                                                  | Does not qualify every legacy inverter definition                                                                                   |
| Legacy Sunwoda               | SW10KH3UT with LSW-5, as identified in the upstream Sunwoda definition                                                                                              | The definition uses FC04 input-register reads                                                                                       |
| Advanced single-phase        | User-reported Deye SUN-8K-SG01LP1-EU readings, dashboard/rule operation, six SOC controls and Solar Export write/readback on an earlier 5.2-based development build | Exact firmware and installed-bundle correlation were not supplied; program-time writes and actual-fault decoding remain unqualified |
| Other four advanced profiles | Conversion, write and lifecycle software regression coverage                                                                                                        | No physical qualification established                                                                                               |

The current main-based source passed software checks, but its 5.3 artifact has not been installed on a physical inverter as part of this work.
User-reported results on the earlier development build do not establish cross-version compatibility or all raw-value ranges.

## Thing Configuration

To connect the logger you need the IP address of the logger and its serial number.
The IP address can be obtained from your router and the serial number can either be read from the label of the logger, or by connecting to the logger with a browser (default user/pass: admin/admin) and getting it from the Status page.
**Please note** that you need the "Device serial number" from the "Device information" section, not the "Inverter serial number".

### `logger` Thing Configuration

| Name                 | Type      | Description                                                                                                         | Default     | Required   | Advanced   |
| -------------------- | --------- | ------------------------------------------------------------------------------------------------------------------- | ----------- | ---------- | ---------- |
| hostname             | text      | Hostname or IP address of the Solarman logger                                                                       | N/A         | yes        | no         |
| serialNumber         | text      | Serial number of the Solarman logger                                                                                | N/A         | yes        | no         |
| inverterType         | text      | The type of inverter connected to the logger                                                                        | N/A         | yes        | no         |
| port                 | integer   | Port of the Solarman logger                                                                                         | 8899        | no         | yes        |
| refreshInterval      | integer   | Interval the device is polled in sec.                                                                               | 60          | no         | yes        |
| solarmanLoggerMode   | option    | RAW Modbus for LAN Stick LSE-3 and V5 MODBUS for most Wifi Sticks. If your Wifi stick uses Raw Modbus choose RAW.   | V5 MODBUS   | no         | yes        |
| additionalRequests   | text      | Additional requests besides the ones in the definition                                                              | N/A         | no         | yes        |

The `inverterType` parameter governs what registers the binding will read from the logger and what channels it will expose.

Available legacy values:

| Inverter Type        | Inverters supported                                  | Notes                                                              |
| -------------------- | ---------------------------------------------------- | ------------------------------------------------------------------ |
| deye_hybrid          | DEYE/Sunsynk/SolArk Hybrid inverters                 | Choose this profile explicitly                                     |
| deye_sg04lp3         | DEYE/Sunsynk/SolArk Hybrid 8/12K-SG04LP3             | e.g. 12K-SG04LP3-EU                                                |
| deye_sg01hp3         | DEYE/Sunsynk/SolArk Hybrid HighVoltage 30K-SG01HP3   | e.g. 30K-SG01HP3-EU                                                |
| deye_string          | DEYE/Sunsynk/SolArk String inverters                 | e.g. SUN-4/5/6/7/8/10/12K-G03 Plus                                 |
| deye_2mppt           | DEYE Microinverter with 2 MPPT Trackers              | e.g. SUN600G3-EU-230 / SUN800G3-EU-230 / SUN1000G3-EU-230          |
| deye_4mppt           | DEYE Microinverter with 4 MPPT Trackers              | e.g. SUN1300G3-EU-230 / SUN1600G3-EU-230 / SUN2000G3-EU-230        |
| sofar_lsw3           | SOFAR Inverters                                      |                                                                    |
| sofar_g3hyd          | SOFAR Hybrid Three-Phase inverter                    | HYD 6000 or rebranded (three-phase), ex. ZCS Azzurro 3PH HYD-ZSS   |
| sofar_hyd3k-6k-es    | SOFAR Hybrid Single-Phase inverter                   | HYD 6000 or rebranded (single-phase), ex. ZCS Azzurro HYD-ZSS      |
| solis_hybrid         | SOLIS Hybrid inverter                                |                                                                    |
| solis_1p8k-5g        | SOLIS 1P8K-5G                                        |                                                                    |
| solis_3p-4g          | SOLIS Three-Phase Inverter 4G Series                 |                                                                    |
| solis_s6-gr1p        | SOLIS Single-Phase Inverter S6-GR1P                  |                                                                    |
| hyd-zss-hp-3k-6k     | ZCS Azzurro Hybrid HP 3K-6K inverters                | Rebranded Sofar models                                             |
| kstar_hybrid         | KSTAR Hybrid inverters                               |                                                                    |
| sofar_wifikit        | SOFAR WiFi Kit                                       |                                                                    |
| zcs_azzurro-ktl-v3   | ZCS Azzurro KTL-V3 inverters                         | ZCS Azzurro 3.3/4.4/5.5/6.6 KTL-V3 (rebranded Sofar KTLX-G3)       |
| sunwoda_hybrid       | SUNWODA Hybrid Three-Phase inverters                 | e.g. SW10KH3UT with LSW-5 stick                                    |

The `additionalRequests` allows the user to specify additional address ranges to be polled.
The format of the value is `mb_functioncode1:start1-end1, mb_functioncode2:start2-end2,...`
For example `"0x03:0x27D-0x27E"` will issue an additional read for Holding Registers between `0x27D` and `0x27E`.

This is useful when coupled with user defined channels, for example a Thing definition like the one below will also read the register for the AC frequency on a Deye inverter, besides the ones pre-defined in the `deye_sg04lp3` inverter definition.

```java
Thing solarman:logger:local [ hostname="x.x.x.x", inverterType="deye_sg04lp3", serialNumber="1234567890", additionalRequests="0x03:0x27D-0x27E" ] {
        Channels:
            Type number : inverter-frequency [scale=0.01, uom="Hz", rule=3, registers="0x27E"]
}
```

A definition or channel list establishes software support, not physical qualification of every model or firmware.
When reporting results, include the selected profile, inverter model, firmware, logger mode and installed bundle version.

### Advanced Profile Configuration

| Parameter          | Required | Default             | Description                                                           |
| ------------------ | -------- | ------------------- | --------------------------------------------------------------------- |
| hostname           | Yes      |                     | Logger IP address or hostname                                         |
| serialNumber       | Yes      |                     | Unsigned 32-bit logger serial, not the inverter serial                |
| port               | No       | 8899                | Logger TCP port                                                       |
| refreshInterval    | No       | 60                  | Seconds between periodic polls; at least 5 for advanced profiles      |
| solarmanLoggerMode | No       | V5MODBUS            | V5MODBUS for V5 Wi-Fi loggers or RAWMODBUS for compatible LAN loggers |
| timeZone           | No       | JVM system zone     | IANA zone used to interpret and set the inverter clock                |
| slaveId            | No       | 1                   | Modbus unit address, from 1 to 247                                    |

`refreshInterval` controls periodic reading, not a delay before every command.
For example, `refreshInterval=30` requests a periodic read every 30 seconds; busy commands and communication time can affect the observed cadence.
Advanced commands run through the serialized queue and schedule confirmation at least 50 milliseconds after an accepted write acknowledgement.
That confirmation interval is separate from the five-second minimum periodic refresh setting.

The logger connection uses the configured Modbus slave ID, defaulting to 1.
Read blocks contain at most 125 consecutive registers.
The profile is read independently of linked Items so that dependent limits and calculated sensors have the required inputs.
A failed optional block publishes UNDEF for affected channels; a valid response can keep the Thing ONLINE while unsupported measurements remain unavailable.
No valid blocks or a connection failure results in OFFLINE/COMMUNICATION_ERROR.

Commands are validated for type, unit, range, register capacity, enumeration and dependent limits.
Numeric commands without a unit use the channel's declared unit; QuantityType commands are converted to that unit.
Values outside limits or between representable register steps are rejected, rather than wrapped or silently clamped.
Program times use HH:mm and must fit between neighboring periods, including intervals crossing midnight.
The device clock accepts DateTime commands and uses the configured time zone.

Polling and writes share one bounded worker per Thing.
Before writing, the worker reads a fresh snapshot, resolves limits and preserves unrelated bits in shared registers.
It validates the write acknowledgement and publishes device readback using updateState.
Commands never perform network I/O on the UI thread.
Disposal cancels the scheduled worker, clears pending commands and closes sockets.
Use a single Thing per physical logger to preserve serialization across related controls.

### Profile Selection and Compatibility

For a new Thing, choose **Solarman Logger** and select either an existing legacy inverterType or one of the five advanced values above.
Keeping `deye_hybrid` exposes its 81 current legacy entries; selecting `sunsynk_single_phase` exposes 150 advanced single-phase entries, comprising 147 Kellerza definitions and three binding metadata channels.
Neither profile silently replaces the other.

Before changing an existing Thing's profile, save its configuration and Item links.
Advanced channel IDs differ from legacy IDs, so create temporary test Items and validate readings before testing intended writes.
Reverting the selector regenerates the legacy channels with their original IDs.
Only one active Thing should communicate with a physical logger.

Advanced profiles do not support `additionalRequests` or manually added custom channels; initialization reports a configuration error instead of ignoring them.
Those facilities remain available for legacy profiles.
The advanced timeZone setting applies to advanced clock commands; legacy definitions continue using the system time zone.
The common logger metadata defaults to 60 seconds and accepts at least 1 second for legacy selections; advanced selections enforce at least 5 seconds at initialization.
Hidden advanced compatibility types retain their separate 30-second default.

The earlier `solarman:sunsynk-*` Thing types are retained as **hidden compatibility types**.
Existing configurations and their original links continue to load; they are not offered for new creation.
They retain their previous configuration defaults.
Moving one to the unified logger is an explicit migration requiring a new Thing UID and updated Item links.

Hidden compatibility Thing type IDs are `solarman:sunsynk-single-phase`, `solarman:sunsynk-single-phase-16kw`, `solarman:sunsynk-three-phase-lv`, `solarman:sunsynk-three-phase-hv` and `solarman:sunsynk-micro-hybrid-ess`.

The current upstream `deye_hybrid` definition has removed `inverter-gen-connected-status` and `inverter-gen-power`, both formerly at register 166.
This port retains that upstream change; restoring the legacy selector does not restore channels removed from the selected upstream version.

## Channels

The list of channels is not static, it is generated dynamically based on the inverter type selected.
The default channels on a Thing come from the configured `inverterType` definition; custom channels can also be added.
The tables below cover `deye_sg04lp3` and `deye_2mppt`; for other inverter types, see the [inverter definitions](https://github.com/openhab/openhab-addons/tree/main/bundles/org.openhab.binding.solarman/src/main/resources/definitions).
Channel IDs are based on the definition's group and item names; for example, `Inverter` and `Total AC Output Power (Active)` become `inverter-total-ac-output-power--active-`.

### `deye_sg04lp3` Channels

Register addresses in the following historical table are profile-specific examples, not a universal map.
The same dynamic channel ID can use different addresses for different inverterType values.
In particular, deye_hybrid AC temperature uses 91 (0x005B), while deye_sg04lp3/deye_sg01hp3 use 541 (0x021D).
For the monophase migration, use the [complete reviewed register proposals](docs/examples/profile-channel-lists/single-phase-register-review.md), which compare the actual deye_hybrid and sunsynk_single_phase definitions.

<!-- BEGIN GENERATED DEYE_SG04LP3 CHANNELS -->

| Channel                                      | Item type                | Access | Description                                                                                                                     |
| -------------------------------------------- | ------------------------ | ------ | ------------------------------------------------------------------------------------------------------------------------------- |
| `alert-alert`                                | String                   | R      | Alert `[0x0229,0x022A,0x022B,0x022C,0x022D,0x022E]`                                                                             |
| `battery-battery-absorption-v`               | Number:ElectricPotential | R      | Battery Absorption V `[0x0064]`                                                                                                 |
| `battery-battery-capacity`                   | Number:ElectricCharge    | R      | Battery Capacity `[0x0066]`                                                                                                     |
| `battery-battery-current`                    | Number:ElectricCurrent   | R      | Battery Current `[0x024F]`                                                                                                      |
| `battery-battery-empty-v`                    | Number:ElectricPotential | R      | Battery Empty V `[0x0066]`                                                                                                      |
| `battery-battery-equalization-v`             | Number:ElectricPotential | R      | Battery Equalization V `[0x0063]`                                                                                               |
| `battery-battery-float-v`                    | Number:ElectricPotential | R      | Battery Float V `[0x0065]`                                                                                                      |
| `battery-battery-max-a-charge`               | Number:ElectricCurrent   | R      | Battery Max A Charge `[0x006C]`                                                                                                 |
| `battery-battery-max-a-discharge`            | Number:ElectricCurrent   | R      | Battery Max A Discharge `[0x006D]`                                                                                              |
| `battery-battery-power`                      | Number:Power             | R      | Battery Power `[0x024E]`                                                                                                        |
| `battery-battery-soc`                        | Number:Dimensionless     | R      | Battery SOC `[0x024C]`                                                                                                          |
| `battery-battery-temperature`                | Number:Temperature       | R      | Battery Temperature `[0x024A]`                                                                                                  |
| `battery-battery-voltage`                    | Number:ElectricPotential | R      | Battery Voltage `[0x024B]`                                                                                                      |
| `battery-daily-battery-charge`               | Number:Energy            | R      | Daily Battery Charge `[0x0202]`                                                                                                 |
| `battery-daily-battery-discharge`            | Number:Energy            | R      | Daily Battery Discharge `[0x0203]`                                                                                              |
| `battery-total-battery-charge`               | Number:Energy            | R      | Total Battery Charge `[0x0204,0x0205]`                                                                                          |
| `battery-total-battery-discharge`            | Number:Energy            | R      | Total Battery Discharge `[0x0206,0x0207]`                                                                                       |
| `grid-daily-energy-bought`                   | Number:Energy            | R      | Daily Energy Bought `[0x0208]`                                                                                                  |
| `grid-daily-energy-sold`                     | Number:Energy            | R      | Daily Energy Sold `[0x0209]`                                                                                                    |
| `grid-external-ct-l1-power`                  | Number:Power             | R      | External CT L1 Power `[0x0268]`                                                                                                 |
| `grid-external-ct-l2-power`                  | Number:Power             | R      | External CT L2 Power `[0x0269]`                                                                                                 |
| `grid-external-ct-l3-power`                  | Number:Power             | R      | External CT L3 Power `[0x026A]`                                                                                                 |
| `grid-grid-voltage-l1`                       | Number:ElectricPotential | R      | Grid Voltage L1 `[0x0256]`                                                                                                      |
| `grid-grid-voltage-l2`                       | Number:ElectricPotential | R      | Grid Voltage L2 `[0x0257]`                                                                                                      |
| `grid-grid-voltage-l3`                       | Number:ElectricPotential | R      | Grid Voltage L3 `[0x0258]`                                                                                                      |
| `grid-internal-ct-l1-power`                  | Number:Power             | R      | Internal CT L1 Power `[0x025C]`                                                                                                 |
| `grid-internal-ct-l2-power`                  | Number:Power             | R      | Internal CT L2 Power `[0x025D]`                                                                                                 |
| `grid-internal-ct-l3-power`                  | Number:Power             | R      | Internal CT L3 Power `[0x025E]`                                                                                                 |
| `grid-total-energy-bought`                   | Number:Energy            | R      | Total Energy Bought `[0x020A,0x020B]`                                                                                           |
| `grid-total-energy-sold`                     | Number:Energy            | R      | Total Energy Sold `[0x020C,0x020D]`                                                                                             |
| `grid-total-grid-power`                      | Number:Power             | R      | Total Grid Power `[0x0271]`                                                                                                     |
| `grid-total-grid-production`                 | Number:Energy            | R      | Total Grid Production `[0x020C,0x020D]`                                                                                         |
| `inverter-ac-temperature`                    | Number:Temperature       | R      | AC Temperature `[0x021D]`                                                                                                       |
| `inverter-communication-board-version-no-`   | String                   | R      | Communication Board Version No. `[0x0011]`                                                                                      |
| `inverter-control-board-version-no-`         | String                   | R      | Control Board Version No. `[0x000D]`                                                                                            |
| `inverter-current-l1`                        | Number:ElectricCurrent   | R      | Current L1 `[0x0276]`                                                                                                           |
| `inverter-current-l2`                        | Number:ElectricCurrent   | R      | Current L2 `[0x0277]`                                                                                                           |
| `inverter-current-l3`                        | Number:ElectricCurrent   | R      | Current L3 `[0x0278]`                                                                                                           |
| `inverter-dc-temperature`                    | Number:Temperature       | R      | DC Temperature `[0x021C]`                                                                                                       |
| `inverter-inverter-id`                       | String                   | R      | Inverter ID `[0x0003,0x0004,0x0005,0x0006,0x0007]`                                                                              |
| `inverter-inverter-l1-power`                 | Number:Power             | R      | Inverter L1 Power `[0x0279]`                                                                                                    |
| `inverter-inverter-l2-power`                 | Number:Power             | R      | Inverter L2 Power `[0x027A]`                                                                                                    |
| `inverter-inverter-l3-power`                 | Number:Power             | R      | Inverter L3 Power `[0x027B]`                                                                                                    |
| `smartload-generator-daily-power-generation` | Number:Energy            | R      | Generator daily power generation `[0x0218]`                                                                                     |
| `smartload-generator-total-power-generation` | Number:Energy            | R      | Generator total power generation `[0x0219,0x021A]`                                                                              |
| `smartload-phase-power-of-gen-port-a`        | Number:Power             | R      | Phase power of Gen port A `[0x0298,0x029C]`                                                                                     |
| `smartload-phase-power-of-gen-port-b`        | Number:Power             | R      | Phase power of Gen port B `[0x0299,0x029D]`                                                                                     |
| `smartload-phase-power-of-gen-port-c`        | Number:Power             | R      | Phase power of Gen port C `[0x029A,0x029E]`                                                                                     |
| `smartload-phase-voltage-of-gen-port-a`      | Number:ElectricPotential | R      | Phase voltage of Gen port A `[0x0295]`                                                                                          |
| `smartload-phase-voltage-of-gen-port-b`      | Number:ElectricPotential | R      | Phase voltage of Gen port B `[0x0296]`                                                                                          |
| `smartload-phase-voltage-of-gen-port-c`      | Number:ElectricPotential | R      | Phase voltage of Gen port C `[0x0297]`                                                                                          |
| `smartload-smartload-enable-status`          | String                   | R      | SmartLoad Enable Status `[0x0085]`; options: `GEN Use`, `SMART Load output`, `Microinverter`                                    |
| `smartload-total-power-of-gen-port`          | Number:Power             | R      | Total Power of Gen port `[0x029B,0x029F]`                                                                                       |
| `solar-daily-production`                     | Number:Energy            | R      | Daily Production `[0x0211]`                                                                                                     |
| `solar-pv1-current`                          | Number:ElectricCurrent   | R      | PV1 Current `[0x02A5]`                                                                                                          |
| `solar-pv1-power`                            | Number:Power             | R      | PV1 Power `[0x02A0]`                                                                                                            |
| `solar-pv1-voltage`                          | Number:ElectricPotential | R      | PV1 Voltage `[0x02A4]`                                                                                                          |
| `solar-pv2-current`                          | Number:ElectricCurrent   | R      | PV2 Current `[0x02A7]`                                                                                                          |
| `solar-pv2-power`                            | Number:Power             | R      | PV2 Power `[0x02A1]`                                                                                                            |
| `solar-pv2-voltage`                          | Number:ElectricPotential | R      | PV2 Voltage `[0x02A6]`                                                                                                          |
| `solar-total-production`                     | Number:Energy            | R      | Total Production `[0x0216,0x0217]`                                                                                              |
| `time-of-use-tou-battery-soc-1`              | Number:Dimensionless     | R/W    | TOU Battery SOC 1 `[0x00A6]`                                                                                                    |
| `time-of-use-tou-battery-soc-2`              | Number:Dimensionless     | R/W    | TOU Battery SOC 2 `[0x00A7]`                                                                                                    |
| `time-of-use-tou-battery-soc-3`              | Number:Dimensionless     | R/W    | TOU Battery SOC 3 `[0x00A8]`                                                                                                    |
| `time-of-use-tou-battery-soc-4`              | Number:Dimensionless     | R/W    | TOU Battery SOC 4 `[0x00A9]`                                                                                                    |
| `time-of-use-tou-battery-soc-5`              | Number:Dimensionless     | R/W    | TOU Battery SOC 5 `[0x00AA]`                                                                                                    |
| `time-of-use-tou-battery-soc-6`              | Number:Dimensionless     | R/W    | TOU Battery SOC 6 `[0x00AB]`                                                                                                    |
| `time-of-use-tou-charge-enable-1`            | String                   | R/W    | TOU Charge Enable 1 `[0x00AC]`; options: `Charge disabled`, `Grid charge`, `Generator charge`, `Grid charge + Generator charge` |
| `time-of-use-tou-charge-enable-2`            | String                   | R/W    | TOU Charge Enable 2 `[0x00AD]`; options: `Charge disabled`, `Grid charge`, `Generator charge`, `Grid charge + Generator charge` |
| `time-of-use-tou-charge-enable-3`            | String                   | R/W    | TOU Charge Enable 3 `[0x00AE]`; options: `Charge disabled`, `Grid charge`, `Generator charge`, `Grid charge + Generator charge` |
| `time-of-use-tou-charge-enable-4`            | String                   | R/W    | TOU Charge Enable 4 `[0x00AF]`; options: `Charge disabled`, `Grid charge`, `Generator charge`, `Grid charge + Generator charge` |
| `time-of-use-tou-charge-enable-5`            | String                   | R/W    | TOU Charge Enable 5 `[0x00B0]`; options: `Charge disabled`, `Grid charge`, `Generator charge`, `Grid charge + Generator charge` |
| `time-of-use-tou-charge-enable-6`            | String                   | R/W    | TOU Charge Enable 6 `[0x00B1]`; options: `Charge disabled`, `Grid charge`, `Generator charge`, `Grid charge + Generator charge` |
| `time-of-use-tou-power-1`                    | Number:Power             | R/W    | TOU Power 1 `[0x009A]`                                                                                                          |
| `time-of-use-tou-power-2`                    | Number:Power             | R/W    | TOU Power 2 `[0x009B]`                                                                                                          |
| `time-of-use-tou-power-3`                    | Number:Power             | R/W    | TOU Power 3 `[0x009C]`                                                                                                          |
| `time-of-use-tou-power-4`                    | Number:Power             | R/W    | TOU Power 4 `[0x009D]`                                                                                                          |
| `time-of-use-tou-power-5`                    | Number:Power             | R/W    | TOU Power 5 `[0x009E]`                                                                                                          |
| `time-of-use-tou-power-6`                    | Number:Power             | R/W    | TOU Power 6 `[0x009F]`                                                                                                          |
| `time-of-use-tou-time-1`                     | String                   | R/W    | TOU Time 1 `[0x0094]`                                                                                                           |
| `time-of-use-tou-time-2`                     | String                   | R/W    | TOU Time 2 `[0x0095]`                                                                                                           |
| `time-of-use-tou-time-3`                     | String                   | R/W    | TOU Time 3 `[0x0096]`                                                                                                           |
| `time-of-use-tou-time-4`                     | String                   | R/W    | TOU Time 4 `[0x0097]`                                                                                                           |
| `time-of-use-tou-time-5`                     | String                   | R/W    | TOU Time 5 `[0x0098]`                                                                                                           |
| `time-of-use-tou-time-6`                     | String                   | R/W    | TOU Time 6 `[0x0099]`                                                                                                           |
| `upload-daily-load-consumption`              | Number:Energy            | R      | Daily Load Consumption `[0x020E]`                                                                                               |
| `upload-load-l1-power`                       | Number:Power             | R      | Load L1 Power `[0x028A]`                                                                                                        |
| `upload-load-l2-power`                       | Number:Power             | R      | Load L2 Power `[0x028B]`                                                                                                        |
| `upload-load-l3-power`                       | Number:Power             | R      | Load L3 Power `[0x028C]`                                                                                                        |
| `upload-load-voltage-l1`                     | Number:ElectricPotential | R      | Load Voltage L1 `[0x0284]`                                                                                                      |
| `upload-load-voltage-l2`                     | Number:ElectricPotential | R      | Load Voltage L2 `[0x0285]`                                                                                                      |
| `upload-load-voltage-l3`                     | Number:ElectricPotential | R      | Load Voltage L3 `[0x0286]`                                                                                                      |
| `upload-total-load-consumption`              | Number:Energy            | R      | Total Load Consumption `[0x020F,0x0210]`                                                                                        |
| `upload-total-load-power`                    | Number:Power             | R      | Total Load Power `[0x028D]`                                                                                                     |

<!-- END GENERATED DEYE_SG04LP3 CHANNELS -->

### `deye_2mppt` Channels

The `deye_2mppt` definition provides the following read-only channels.
For example, use `inverter-total-ac-output-power--active-` for AC output power; `grid-total-grid-power` is available only with inverter types whose definitions include it.

<!-- BEGIN GENERATED DEYE_2MPPT CHANNELS -->

| Channel                                                | Item type                | Description                                                                                |
| ------------------------------------------------------ | ------------------------ | ------------------------------------------------------------------------------------------ |
| `grid-ac-output-frequency`                             | Number:Frequency         | AC Output Frequency `[0x004F]`                                                             |
| `grid-ac-voltage`                                      | Number:ElectricPotential | AC Voltage `[0x0049]`                                                                      |
| `grid-grid-current`                                    | Number:ElectricCurrent   | Grid Current `[0x004C]`                                                                    |
| `grid-grid-frequency-lower-limit`                      | Number:Frequency         | Grid Frequency Lower Limit `[0x001E]`                                                      |
| `grid-grid-frequency-upper-limit`                      | Number:Frequency         | Grid Frequency Upper Limit `[0x001D]`                                                      |
| `grid-grid-voltage-lower-limit`                        | Number:ElectricPotential | Grid Voltage Lower Limit `[0x001C]`                                                        |
| `grid-grid-voltage-upp-limit`                          | Number:ElectricPotential | Grid Voltage Upp Limit `[0x001B]`                                                          |
| `grid-island-protection-enable`                        | String                   | Island Protection Enable `[0x002E]`; options: `Disabled`, `Enabled`                        |
| `grid-on-off-enable`                                   | String                   | ON-OFF Enable `[0x002B]`; options: `OFF`, `ON`                                             |
| `grid-overfrequency-and-load-reduction-percentage`     | Number:Dimensionless     | Overfrequency And Load Reduction Percentage `[0x0023]`                                     |
| `grid-overfrequency-and-load-reduction-starting-point` | Number:Frequency         | Overfrequency And Load Reduction Starting Point `[0x0022]`                                 |
| `grid-overfrequency-load-shedding-enable`              | String                   | Overfrequency&Load-shedding Enable `[0x0031]`; options: `Disabled`, `Enabled`              |
| `inverter-ac-version--number`                          | String                   | AC Version. Number `[0x000E]`                                                              |
| `inverter-communication-protocol-version`              | String                   | Communication Protocol Version `[0x0012]`                                                  |
| `inverter-dc-master-firmware-version`                  | String                   | DC Master Firmware Version `[0x000D]`                                                      |
| `inverter-hardware-version`                            | String                   | Hardware Version `[0x000C]`                                                                |
| `inverter-inverter-id`                                 | String                   | Inverter ID `[0x0003,0x0004,0x0005,0x0006,0x0007]`                                         |
| `inverter-power-factor-regulation`                     | Number                   | Power Factor Regulation `[0x0032]`                                                         |
| `inverter-radiator-temperature`                        | Number:Temperature       | Radiator Temperature `[0x005A]`                                                            |
| `inverter-rated-power`                                 | Number:Power             | Rated Power `[0x0010]`                                                                     |
| `inverter-restore-factory-settings`                    | String                   | Restore Factory Settings `[0x0036]`; options: `Disabled`, `Enabled`                        |
| `inverter-running-status`                              | String                   | Running Status `[0x003B]`; options: `Stand-by`, `Self-check`, `Normal`, `Warning`, `Fault` |
| `inverter-soft-start-enable`                           | String                   | Soft Start Enable `[0x002F]`; options: `Disabled`, `Enabled`                               |
| `inverter-start-up-self-checking-time`                 | Number:Time              | Start-up Self-checking Time  `[0x0015]`                                                    |
| `inverter-total-ac-output-power--active-`              | Number:Power             | Total AC Output Power (Active) `[0x0056,0x0057]`                                           |
| `inverter-update-time`                                 | String                   | Update Time `[0x0016,0x0017,0x0018]`                                                       |
| `solar-active-power-regulations`                       | Number:Dimensionless     | Active Power Regulations `[0x0028]`                                                        |
| `solar-daily-production`                               | Number:Energy            | Daily Production `[0x003C]`                                                                |
| `solar-daily-production-1`                             | Number:Energy            | Daily Production 1 `[0x0041]`                                                              |
| `solar-daily-production-2`                             | Number:Energy            | Daily Production 2 `[0x0042]`                                                              |
| `solar-pv1-current`                                    | Number:ElectricCurrent   | PV1 Current `[0x006E]`                                                                     |
| `solar-pv1-voltage`                                    | Number:ElectricPotential | PV1 Voltage `[0x006D]`                                                                     |
| `solar-pv2-current`                                    | Number:ElectricCurrent   | PV2 Current `[0x0070]`                                                                     |
| `solar-pv2-voltage`                                    | Number:ElectricPotential | PV2 Voltage `[0x006F]`                                                                     |
| `solar-total-production`                               | Number:Energy            | Total Production `[0x003F,0x0040]`                                                         |
| `solar-total-production-1`                             | Number:Energy            | Total Production 1 `[0x0045]`                                                              |
| `solar-total-production-2`                             | Number:Energy            | Total Production 2 `[0x0047]`                                                              |

<!-- END GENERATED DEYE_2MPPT CHANNELS -->

### Advanced Channels

The advanced profiles provide **928 profile-specific channel entries**, including source aliases and **351 distinct channel IDs**.
Selecting an advanced inverterType generates its channels dynamically using the profile-specific metadata.
Their IDs differ from the legacy channels above: the advanced equivalent of `time-of-use-time-of-use-soc-1` is `prog1-capacity`.
Switching profiles does not relink existing Items automatically.

The complete [channel and register catalog](docs/sunsynk-channels.md) lists every added channel, Item type, access mode, register tuple, factor, mask and bounds.
Select the section matching inverterType:

| inverterType                | Full channel list                                                | Entries |
| --------------------------- | ---------------------------------------------------------------- | ------- |
| `sunsynk_single_phase`      | [Single-phase](docs/sunsynk-channels.md#single_phase)            | 150     |
| `sunsynk_single_phase_16kw` | [Single-phase 16 kW](docs/sunsynk-channels.md#single_phase_16kw) | 147     |
| `sunsynk_three_phase_lv`    | [Three-phase LV](docs/sunsynk-channels.md#three_phase_lv)        | 251     |
| `sunsynk_three_phase_hv`    | [Three-phase HV](docs/sunsynk-channels.md#three_phase_hv)        | 294     |
| `sunsynk_micro_hybrid_ess`  | [Micro hybrid ESS](docs/sunsynk-channels.md#micro_hybrid_ess)    | 86      |

The following frequently used channels apply to **`sunsynk_single_phase`**.
Access, availability and register addresses can differ on other profiles; for example, micro ESS `prog1-charge` is a Switch rather than a String selection.

| Channel ID                      | Item type                | Access     | Holding registers (decimal)               |
| ------------------------------- | ------------------------ | ---------- | ----------------------------------------- |
| `battery-soc`                   | Number:Dimensionless     | Read       | [184]                                     |
| `battery-power`                 | Number:Power             | Read       | [190]                                     |
| `battery-voltage`               | Number:ElectricPotential | Read       | [183]                                     |
| `battery-current`               | Number:ElectricCurrent   | Read       | [191]                                     |
| `battery-temperature`           | Number:Temperature       | Read       | [182]                                     |
| `pv-power`                      | Number:Power             | Read       | Computed from available PV power channels |
| `grid-power`                    | Number:Power             | Read       | [169]                                     |
| `grid-frequency`                | Number:Frequency         | Read       | [79]                                      |
| `load-power`                    | Number:Power             | Read       | [178]                                     |
| `day-grid-import`               | Number:Energy            | Read       | [76]                                      |
| `day-grid-export`               | Number:Energy            | Read       | [77]                                      |
| `date-time`                     | DateTime                 | Read/write | [22, 23, 24]                              |
| `grid-charge-enabled`           | Switch                   | Read/write | [232]                                     |
| `use-timer`                     | Switch                   | Read/write | [248]                                     |
| `battery-max-charge-current`    | Number:ElectricCurrent   | Read/write | [210]                                     |
| `battery-max-discharge-current` | Number:ElectricCurrent   | Read/write | [211]                                     |
| `grid-charge-battery-current`   | Number:ElectricCurrent   | Read/write | [230]                                     |
| `battery-low-capacity`          | Number:Dimensionless     | Read/write | [219]                                     |
| `prog1-time`                    | String                   | Read/write | [250]                                     |
| `prog1-power`                   | Number:Power             | Read/write | [256]                                     |
| `prog1-voltage`                 | Number:ElectricPotential | Read/write | [262]                                     |
| `prog1-capacity`                | Number:Dimensionless     | Read/write | [268]                                     |
| `prog1-charge`                  | String                   | Read/write | [274]                                     |
| `prog1-mode`                    | String                   | Read/write | [274]                                     |
| `prog1-sell`                    | Switch                   | Read/write | [274]                                     |

The `prog1-*` through `prog6-*` families describe the six time-of-use periods.
Each full ID is listed separately in the catalog.
Channels cover start time, power limit, voltage, capacity/SOC, charge source, operating mode and sell permission where the profile supports them.
Other catalog entries include BMS limits, per-phase and MPPT readings, energy counters, faults, generator/smart-load settings, export limits and peak shaving.

### Complete Item Lists for Legacy-to-Advanced Migration

For migrating from deye_hybrid to sunsynk_single_phase, use the [complete lists and migration guide](docs/examples/profile-channel-lists/README.md).
It provides all [81 legacy channel definitions](docs/examples/profile-channel-lists/deye_hybrid.md), all [150 advanced channel definitions](docs/examples/profile-channel-lists/sunsynk_single_phase.md), ready-to-adapt Item files and a per-legacy-channel review map.
Preserve existing Item names where compatible; the templates use generic names and do not automatically migrate links.
Some legacy channels have no direct replacement in the advanced profile.

The binding adds three read-only String identification channels to `sunsynk_single_phase`.
They are binding metadata additions from the bundled `deye_hybrid` register map, stored separately from the 147 unchanged Kellerza entries.
They have no unit and reuse existing polling blocks, adding no Modbus requests.
Other advanced profiles do not receive these model-specific additions.

| Channel                       | Registers             | Value                                                                                       |
| ----------------------------- | --------------------- | ------------------------------------------------------------------------------------------- |
| `communication-board-version` | 14 (0x000E)           | Unsigned 16-bit decimal version code as text                                                |
| `control-board-version`       | 13 (0x000D)           | Unsigned 16-bit decimal version code as text                                                |
| `inverter-id`                 | 3..7 (0x0003..0x0007) | High-byte/low-byte character sequence, edge padding trimmed, matching the identity property |

Use `String` Items without unit metadata.
Firmware version codes are not dotted versions and are not numeric measurements.
These addresses are for this single-phase profile; the historical three-phase example uses register 17 for the communication board version.

For staged device qualification, follow the [single-phase hardware validation procedure](docs/examples/profile-channel-lists/hardware-validation.md), covering reading differences, all six SOC controls, queued commands, repeated values and unresolved legacy functions.

## Writable Channels

> **Warning:** Writing settings changes the inverter's operating configuration. Verify that the selected profile matches your inverter and that the requested settings are appropriate for your installation. Incorrect settings may affect inverter or battery operation. Use write functionality at your own risk.

If you want to write to registers of the inverter, you need to define your channels as writable, because by default channels are treated as read-only.
For example, `deye_sg04lp3` and `deye_sg01hp3` have a full set of writable channels connected to the `Time Of Use` functionality, but if you want to add custom writable channels you need to define them like this:

```java
Thing solarman:logger:local [ hostname="x.x.x.x", inverterType="deye_sg04lp3", serialNumber="1234567890", additionalRequests="0x03:0x00A6-0x00A8" ] {
        Channels:
            Type number : tou-battery-soc-1 [scale=1, uom="%", rule=1, registers="0x00A6", readOnly=false]
}
```

Note the **readOnly=false** part here, which will make the channel accept commands and write data to the inverter register.

## Write Confirmation and Logging

Writable legacy YAML channels require an explicit `isReadOnly: false`.
Lookup controls accept their published textual options.
Advanced channels accept commands only when the selected profile declares them writable.
Existing channel UIDs and Item links remain valid; advanced Things do not automatically replace legacy Things.

Commands are queued asynchronously and serialized with polling.
Numeric commands accept a compatible quantity or a number in the channel unit.
Program times accept HH:mm strings, switches accept ON/OFF, selections accept the exact published option text, and the clock accepts DateTime commands.
Dynamic limits depend on current device readings; the binding rejects unavailable or invalid values.
Advanced time-of-use capacity limits can depend on the battery low-capacity setting, so a sitemap minimum is only an example, not a device limit.

Legacy writes read the contiguous control block before assignment and again after acknowledgement.
Numeric mismatches allow one bounded repeat; transport failures and incomplete confirmation data are not automatically retried.
Advanced numeric, Switch, selection and program-time assignments also permit one repeat after an acknowledged, complete readback mismatch.
Before repeating, they obtain a fresh snapshot, revalidate dependent bounds and rebuild masked writes to preserve unrelated bits.
Missing or malformed control words, failed acknowledgements and transport errors do not trigger a repeat.
Device clock writes remain single-attempt; a ticking clock without exact confirmation stays at DEBUG.
Six separate commands remain separate transactions, not an atomic six-period update.

A successful advanced assignment publishes its complete confirmation snapshot and schedules the next periodic poll from that observation, avoiding an immediate duplicate full read.
With the single-phase four-block read plan, an ordinary confirmed command uses eight FC03 requests and one FC16 request instead of twelve FC03 plus one FC16 (one third fewer reads).
Explicit REFRESH requests still trigger polling.
Failed commands, incomplete confirmation snapshots and clock assignments without exact confirmation retain post-command refresh; legacy commands retain their existing refresh behavior.
Repeated equal assignments still perform write/readback and suppress change INFO when unchanged.
This optimization does not change profile registers, interpretations or bounded retries.

Writable channel types veto optimistic autoupdate.
With the default Item configuration, displayed state already follows device observations; the explicit `autoupdate="false"` in textual examples is optional.
After updating the bundle, check the published channels and sitemap controls.
See [Thing channel updates](docs/thing-channel-updates.md) for upgrade checks; Thing recreation is not a general requirement established by the handler tests.

The binding writes the following audit messages to `openhab.log`:

| Level | Event                                                                                                                                      |
| ----- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| INFO  | Actual change confirmed by device readback, with channel UID, hexadecimal registers, previous value, requested command and confirmed value |
| WARN  | Divergent confirmation, a bounded repeat, or a write/confirmation failure                                                                  |
| DEBUG | Modbus request/response frames, unchanged confirmations and detailed diagnostics                                                           |

Normal polling and reassigning an already matching value do not generate success INFO.
An acknowledgement alone does not generate success INFO.
Running clocks can tick between writing and reading; without an exact confirmation they produce DEBUG rather than a success INFO.

Hardware reports are summarized under [Hardware Test Coverage](#hardware-test-coverage).
Readback confirms an observed value; it does not establish the suitability of a setting for an installation.

Advanced assignments schedule readback at least 50 ms after an accepted write acknowledgement.
The openHAB scheduler handles the interval without sleeping or waiting on a worker thread.
The logger queue and connection remain reserved until confirmation (and any bounded repeat) completes, so another command or periodic/explicit poll cannot interleave.
The same delay applies after a repeated assignment.
Disposal cancels pending confirmation tasks and closes the transaction connection.
No additional Modbus requests are introduced; ordinary confirmed single-phase commands still use eight FC03 requests and one FC16 request.
This interval may allow delayed device application to settle, but its benefit on a specific inverter requires hardware observation.

Rejected V5 responses identify the failed field: frame size, start/end marker, control code, echoed request sequence, logger serial, declared length or checksum.
Messages include expected/received values and read/write function/register context.
Advanced command failure WARN entries also identify the stage (`pre-read`, `write`, `readback` or `retry-pre-read`).
Missing or malformed control words identify the register in decimal and hexadecimal and the received byte length or absence.
The same WARN includes rejected FC03 block ranges and their original protocol error messages in `readFailures`.
Transport failures identify the interrupted block and retain the original cause.
Rejected polling blocks retain their existing DEBUG logging.
These details use the request and response already in memory; they add no Modbus requests, retries or frame acceptance exceptions.
A rejected write acknowledgement still leaves the result unconfirmed.

## Full Example

This is an example for a DEYE 12kW (SUN-12K-SG04LP3-EU) hybrid inverter

### `solarman.things`

Please replace the `hostname` and `serialNumber` with the correct values for your logger.

```java
Thing solarman:logger:local [
    hostname="192.168.1.100",
    inverterType="deye_sg04lp3",
    serialNumber="1234567890",
    additionalRequests="0x03:0x027E-0x027E"
] {
    Channels:
        Type number : inverter-frequency [scale=0.01, uom="Hz", rule=3, registers="0x027E"]
}
```

### `solarman.items`

Items file example for a SUN-12K-SG04LP3-EU inverter.
The Thing above explicitly adds the custom `inverter-frequency` channel used below.

```java
Group solarman "Solarman"

Number:Temperature        AC_Temperature                  "AC Temperature [%.1f °C]"             (solarman)  {channel="solarman:logger:local:inverter-ac-temperature", unit="°C"}
String                    Alert                           "Alert [%s]"                           (solarman)  {channel="solarman:logger:local:alert-alert"}
Number:ElectricPotential  Battery_Absorption_V            "Battery Absorption V [%.2f V]"        (solarman)  {channel="solarman:logger:local:battery-battery-absorption-v", unit="V"}
Number:ElectricCharge     Battery_Capacity                "Battery Capacity [%d Ah]"             (solarman)  {channel="solarman:logger:local:battery-battery-capacity", unit="Ah"}
Number:ElectricCurrent    Battery_Current                 "Battery Current [%.1f A]"             (solarman)  {channel="solarman:logger:local:battery-battery-current", unit="A"}
Number:Energy             Daily_Battery_Charge            "Daily Battery Charge [%.1f kWh]"      (solarman)  {channel="solarman:logger:local:battery-daily-battery-charge", unit="kWh"}
Number:Energy             Daily_Battery_Discharge         "Daily Battery Discharge [%.1f kWh]"   (solarman)  {channel="solarman:logger:local:battery-daily-battery-discharge", unit="kWh"}
Number:ElectricPotential  Battery_Empty_V                 "Battery Empty V [%.2f V]"             (solarman)  {channel="solarman:logger:local:battery-battery-empty-v", unit="V"}
Number:ElectricPotential  Battery_Equalization_V          "Battery Equalization V [%.2f V]"      (solarman)  {channel="solarman:logger:local:battery-battery-equalization-v", unit="V"}
Number:ElectricPotential  Battery_Float_V                 "Battery Float V [%.2f V]"             (solarman)  {channel="solarman:logger:local:battery-battery-float-v", unit="V"}
Number:ElectricCurrent    Battery_Max_A_Charge            "Battery Max A Charge [%d A]"          (solarman)  {channel="solarman:logger:local:battery-battery-max-a-charge", unit="A"}
Number:ElectricCurrent    Battery_Max_A_Discharge         "Battery Max A Discharge [%d A]"       (solarman)  {channel="solarman:logger:local:battery-battery-max-a-discharge", unit="A"}
Number:Dimensionless      Battery_SOC                     "Battery SOC [%d %%]"                  (solarman)  {channel="solarman:logger:local:battery-battery-soc", unit="%"}
Number:Power              Battery_Power                   "Battery Power [%d W]"                 (solarman)  {channel="solarman:logger:local:battery-battery-power", unit="W"}
Number:Temperature        Battery_Temperature             "Battery Temperature [%.1f °C]"        (solarman)  {channel="solarman:logger:local:battery-battery-temperature", unit="°C"}
Number:ElectricPotential  Battery_Voltage                 "Battery Voltage [%.2f V]"             (solarman)  {channel="solarman:logger:local:battery-battery-voltage", unit="V"}
String                    Communication_Board_Version_No  "Communication Board Version No [%s]"  (solarman)  {channel="solarman:logger:local:inverter-communication-board-version-no-"}
String                    Control_Board_Version_No        "Control Board Version No [%s]"        (solarman)  {channel="solarman:logger:local:inverter-control-board-version-no-"}
Number:ElectricCurrent    Current_L1                      "Current L1 [%.1f A]"                  (solarman)  {channel="solarman:logger:local:inverter-current-l1", unit="A"}
Number:ElectricCurrent    Current_L2                      "Current L2 [%.1f A]"                  (solarman)  {channel="solarman:logger:local:inverter-current-l2", unit="A"}
Number:ElectricCurrent    Current_L3                      "Current L3 [%.1f A]"                  (solarman)  {channel="solarman:logger:local:inverter-current-l3", unit="A"}
Number:Energy             Daily_Energy_Bought             "Daily Energy Bought [%d kWh]"         (solarman)  {channel="solarman:logger:local:grid-daily-energy-bought", unit="kWh"}
Number:Energy             Daily_Energy_Sold               "Daily Energy Sold [%d Wh]"            (solarman)  {channel="solarman:logger:local:grid-daily-energy-sold", unit="Wh"}
Number:Energy             Daily_Load_Consumption          "Daily Load Consumption [%.1f kWh]"    (solarman)  {channel="solarman:logger:local:upload-daily-load-consumption", unit="kWh"}
Number:Energy             Daily_Production                "Daily Production [%.1f kWh]"          (solarman)  {channel="solarman:logger:local:solar-daily-production", unit="kWh"}
Number:Temperature        DC_Temperature                  "DC Temperature [%.1f °C]"             (solarman)  {channel="solarman:logger:local:inverter-dc-temperature", unit="°C"}
Number:Power              External_CT_L1_Power            "External CT L1 Power [%d W]"          (solarman)  {channel="solarman:logger:local:grid-external-ct-l1-power", unit="W"}
Number:Power              External_CT_L2_Power            "External CT L2 Power [%d W]"          (solarman)  {channel="solarman:logger:local:grid-external-ct-l2-power", unit="W"}
Number:Power              External_CT_L3_Power            "External CT L3 Power [%d W]"          (solarman)  {channel="solarman:logger:local:grid-external-ct-l3-power", unit="W"}
Number:Power              Gen_Port_A_Phase_Power          "Phase Power of Gen Port A [%d W]"     (solarman)  {channel="solarman:logger:local:smartload-phase-power-of-gen-port-a", unit="W"}
Number:Power              Gen_Port_B_Phase_Power          "Phase Power of Gen Port B [%d W]"     (solarman)  {channel="solarman:logger:local:smartload-phase-power-of-gen-port-b", unit="W"}
Number:Power              Gen_Port_C_Phase_Power          "Phase Power of Gen Port C [%d W]"     (solarman)  {channel="solarman:logger:local:smartload-phase-power-of-gen-port-c", unit="W"}
Number:ElectricPotential  Gen_Port_A_Phase_Voltage        "Phase Voltage of Gen Port A [%d V]"   (solarman)  {channel="solarman:logger:local:smartload-phase-voltage-of-gen-port-a", unit="V"}
Number:ElectricPotential  Gen_Port_B_Phase_Voltage        "Phase Voltage of Gen Port B [%d V]"   (solarman)  {channel="solarman:logger:local:smartload-phase-voltage-of-gen-port-b", unit="V"}
Number:ElectricPotential  Gen_Port_C_Phase_Voltage        "Phase Voltage of Gen Port C [%d V]"   (solarman)  {channel="solarman:logger:local:smartload-phase-voltage-of-gen-port-c", unit="V"}
String                    Inverter_Id                     "Inverter Id [%s]"                     (solarman)  {channel="solarman:logger:local:inverter-inverter-id"}
Number:Power              Inverter_L1_Power               "Inverter L1 Power [%d W]"             (solarman)  {channel="solarman:logger:local:inverter-inverter-l1-power", unit="W"}
Number:Power              Inverter_L2_Power               "Inverter L2 Power [%d W]"             (solarman)  {channel="solarman:logger:local:inverter-inverter-l2-power", unit="W"}
Number:Power              Inverter_L3_Power               "Inverter L3 Power [%d W]"             (solarman)  {channel="solarman:logger:local:inverter-inverter-l3-power", unit="W"}
Number:Power              Internal_CT_L1_Power            "Internal CT L1 Power [%d W]"          (solarman)  {channel="solarman:logger:local:grid-internal-ct-l1-power", unit="W"}
Number:Power              Internal_CT_L2_Power            "Internal CT L2 Power [%d W]"          (solarman)  {channel="solarman:logger:local:grid-internal-ct-l2-power", unit="W"}
Number:Power              Internal_CT_L3_Power            "Internal CT L3 Power [%d W]"          (solarman)  {channel="solarman:logger:local:grid-internal-ct-l3-power", unit="W"}
Number:ElectricPotential  Load_Voltage_L1                 "Load Voltage L1 [%d V]"               (solarman)  {channel="solarman:logger:local:upload-load-voltage-l1", unit="V"}
Number:ElectricPotential  Load_Voltage_L2                 "Load Voltage L2 [%d V]"               (solarman)  {channel="solarman:logger:local:upload-load-voltage-l2", unit="V"}
Number:ElectricPotential  Load_Voltage_L3                 "Load Voltage L3 [%d V]"               (solarman)  {channel="solarman:logger:local:upload-load-voltage-l3", unit="V"}
Number:Power              Load_L1_Power                   "Load L1 Power [%d W]"                 (solarman)  {channel="solarman:logger:local:upload-load-l1-power", unit="W"}
Number:Power              Load_L2_Power                   "Load L2 Power [%d W]"                 (solarman)  {channel="solarman:logger:local:upload-load-l2-power", unit="W"}
Number:Power              Load_L3_Power                   "Load L3 Power [%d W]"                 (solarman)  {channel="solarman:logger:local:upload-load-l3-power", unit="W"}
Number:ElectricPotential  Grid_Voltage_L1                 "Grid Voltage L1 [%d V]"               (solarman)  {channel="solarman:logger:local:grid-grid-voltage-l1", unit="V"}
Number:ElectricPotential  Grid_Voltage_L2                 "Grid Voltage L2 [%d V]"               (solarman)  {channel="solarman:logger:local:grid-grid-voltage-l2", unit="V"}
Number:ElectricPotential  Grid_Voltage_L3                 "Grid Voltage L3 [%d V]"               (solarman)  {channel="solarman:logger:local:grid-grid-voltage-l3", unit="V"}
Number:Energy             Generator_Daily_Power_Generation "Generator Daily Power Generation [%.1f kWh]" (solarman) {channel="solarman:logger:local:smartload-generator-daily-power-generation", unit="kWh"}
Number:Energy             Generator_Total_Power_Generation "Generator Total Power Generation [%.1f kWh]" (solarman) {channel="solarman:logger:local:smartload-generator-total-power-generation", unit="kWh"}
String                    Smartload_Enable_Status         "Smartload Enable Status [%s]"         (solarman)  {channel="solarman:logger:local:smartload-smartload-enable-status"}
Number:Energy             Total_Battery_Charge            "Total Battery Charge [%d kWh]"        (solarman)  {channel="solarman:logger:local:battery-total-battery-charge", unit="kWh"}
Number:Energy             Total_Battery_Discharge         "Total Battery Discharge [%d kWh]"     (solarman)  {channel="solarman:logger:local:battery-total-battery-discharge", unit="kWh"}
Number:Energy             Total_Energy_Bought             "Total Energy Bought [%d kWh]"         (solarman)  {channel="solarman:logger:local:grid-total-energy-bought", unit="kWh"}
Number:Energy             Total_Energy_Sold               "Total Energy Sold [%d kWh]"           (solarman)  {channel="solarman:logger:local:grid-total-energy-sold", unit="kWh"}
Number:Power              Total_Gen_Port_Power            "Total Power of Gen Port [%d W]"       (solarman)  {channel="solarman:logger:local:smartload-total-power-of-gen-port", unit="W"}
Number:Power              Total_Grid_Power                "Total Instant Grid Power [%d W]"      (solarman)  {channel="solarman:logger:local:grid-total-grid-power", unit="W"}
Number:Energy             Total_Grid_Production           "Total Grid Feed-in [%.1f kWh]"        (solarman)  {channel="solarman:logger:local:grid-total-grid-production", unit="kWh"}
Number:Energy             Total_Load_Consumption          "Total Load Consumption [%d kWh]"      (solarman)  {channel="solarman:logger:local:upload-total-load-consumption", unit="kWh"}
Number:Power              Total_Load_Power                "Total Load Power [%d W]"              (solarman)  {channel="solarman:logger:local:upload-total-load-power", unit="W"}
Number:Energy             Total_Solar_Production          "Total Solar Production [%.1f kWh]"    (solarman)  {channel="solarman:logger:local:solar-total-production", unit="kWh"}
Number:Power              PV1_Power                       "PV1 Power [%d W]"                     (solarman)  {channel="solarman:logger:local:solar-pv1-power", unit="W"}
Number:ElectricCurrent    PV1_Current                     "PV1 Current [%.1f A]"                 (solarman)  {channel="solarman:logger:local:solar-pv1-current", unit="A"}
Number:ElectricPotential  PV1_Voltage                     "PV1 Voltage [%d V]"                   (solarman)  {channel="solarman:logger:local:solar-pv1-voltage", unit="V"}
Number:Power              PV2_Power                       "PV2 Power [%d W]"                     (solarman)  {channel="solarman:logger:local:solar-pv2-power", unit="W"}
Number:ElectricCurrent    PV2_Current                     "PV2 Current [%.1f A]"                 (solarman)  {channel="solarman:logger:local:solar-pv2-current", unit="A"}
Number:ElectricPotential  PV2_Voltage                     "PV2 Voltage [%d V]"                   (solarman)  {channel="solarman:logger:local:solar-pv2-voltage", unit="V"}

String                    ToU_1_Time                      "ToU 1 Time [%s]"                      (solarman)  {channel="solarman:logger:local:time-of-use-tou-time-1"}
Number:Dimensionless      ToU_1_Battery_SOC               "ToU 1 Battery SOC [%d %%]"            (solarman)  {channel="solarman:logger:local:time-of-use-tou-battery-soc-1", unit="%"}
Number:Power              ToU_1_Power                     "ToU 1 Power [%d W]"                   (solarman)  {channel="solarman:logger:local:time-of-use-tou-power-1", unit="W"}
String                    ToU_1_Chare_Enable              "ToU 1 Charge Enable [%s]"             (solarman)  {channel="solarman:logger:local:time-of-use-tou-charge-enable-1"}
String                    ToU_2_Time                      "ToU 2 Time [%s]"                      (solarman)  {channel="solarman:logger:local:time-of-use-tou-time-2"}
Number:Dimensionless      ToU_2_Battery_SOC               "ToU 2 Battery SOC [%d %%]"            (solarman)  {channel="solarman:logger:local:time-of-use-tou-battery-soc-2", unit="%"}
Number:Power              ToU_2_Power                     "ToU 2 Power [%d W]"                   (solarman)  {channel="solarman:logger:local:time-of-use-tou-power-2", unit="W"}
String                    ToU_2_Chare_Enable              "ToU 2 Charge Enable [%s]"             (solarman)  {channel="solarman:logger:local:time-of-use-tou-charge-enable-2"}
String                    ToU_3_Time                      "ToU 3 Time [%s]"                      (solarman)  {channel="solarman:logger:local:time-of-use-tou-time-3"}
Number:Dimensionless      ToU_3_Battery_SOC               "ToU 3 Battery SOC [%d %%]"            (solarman)  {channel="solarman:logger:local:time-of-use-tou-battery-soc-3", unit="%"}
Number:Power              ToU_3_Power                     "ToU 3 Power [%d W]"                   (solarman)  {channel="solarman:logger:local:time-of-use-tou-power-3", unit="W"}
String                    ToU_3_Chare_Enable              "ToU 3 Charge Enable [%s]"             (solarman)  {channel="solarman:logger:local:time-of-use-tou-charge-enable-3"}
String                    ToU_4_Time                      "ToU 4 Time [%s]"                      (solarman)  {channel="solarman:logger:local:time-of-use-tou-time-4"}
Number:Dimensionless      ToU_4_Battery_SOC               "ToU 4 Battery SOC [%d %%]"            (solarman)  {channel="solarman:logger:local:time-of-use-tou-battery-soc-4", unit="%"}
Number:Power              ToU_4_Power                     "ToU 4 Power [%d W]"                   (solarman)  {channel="solarman:logger:local:time-of-use-tou-power-4", unit="W"}
String                    ToU_4_Chare_Enable              "ToU 4 Charge Enable [%s]"             (solarman)  {channel="solarman:logger:local:time-of-use-tou-charge-enable-4"}
String                    ToU_5_Time                      "ToU 5 Time [%s]"                      (solarman)  {channel="solarman:logger:local:time-of-use-tou-time-5"}
Number:Dimensionless      ToU_5_Battery_SOC               "ToU 5 Battery SOC [%d %%]"            (solarman)  {channel="solarman:logger:local:time-of-use-tou-battery-soc-5", unit="%"}
Number:Power              ToU_5_Power                     "ToU 5 Power [%d W]"                   (solarman)  {channel="solarman:logger:local:time-of-use-tou-power-5", unit="W"}
String                    ToU_5_Chare_Enable              "ToU 5 Charge Enable [%s]"             (solarman)  {channel="solarman:logger:local:time-of-use-tou-charge-enable-5"}
String                    ToU_6_Time                      "ToU 6 Time [%s]"                      (solarman)  {channel="solarman:logger:local:time-of-use-tou-time-6"}
Number:Dimensionless      ToU_6_Battery_SOC               "ToU 6 Battery SOC [%d %%]"            (solarman)  {channel="solarman:logger:local:time-of-use-tou-battery-soc-6", unit="%"}
Number:Power              ToU_6_Power                     "ToU 6 Power [%d W]"                   (solarman)  {channel="solarman:logger:local:time-of-use-tou-power-6", unit="W"}
String                    ToU_6_Chare_Enable              "ToU 6 Charge Enable [%s]"             (solarman)  {channel="solarman:logger:local:time-of-use-tou-charge-enable-6"}

Number:Frequency          Inverter_Frequency              "Inverter Frequency [%.2f Hz]"         (solarman)  {channel="solarman:logger:local:inverter-frequency", unit="Hz"}
```

### `solarman.sitemap`

Sitemap example for a SUN-12K-SG04LP3-EU inverter

```perl
sitemap solarman label="Solarman"
{
    Frame label="Inverter"{
        Text item=Communication_Board_Version_No icon="solar"
        Text item=Control_Board_Version_No icon="solar"
        Text item=Inverter_Id icon="solar"
        Text item=AC_Temperature icon="temperature"
        Text item=DC_Temperature icon="temperature"
        Text item=Inverter_L1_Power icon="poweroutlet"
        Text item=Inverter_L2_Power icon="poweroutlet"
        Text item=Inverter_L3_Power icon="poweroutlet"
        Text item=Current_L1 icon="line"
        Text item=Current_L2 icon="line"
        Text item=Current_L3 icon="line"
        Text item=Inverter_Frequency icon="line"
    }

    Frame label="Battery"{
        Text item=Battery_SOC icon="battery"
        Text item=Battery_Current icon="current"
        Text item=Battery_Power icon="power"
        Text item=Battery_Voltage icon="voltage"
        Text item=Battery_Temperature icon="temperature"
        Text item=Daily_Battery_Charge icon="renewable"
        Text item=Daily_Battery_Discharge icon="battery"
        Text item=Total_Battery_Charge icon="renewable"
        Text item=Total_Battery_Discharge icon="battery"
        Text item=Battery_Absorption_V icon="voltage"
        Text item=Battery_Equalization_V icon="voltage"
        Text item=Battery_Float_V icon="voltage"
        Text item=Battery_Empty_V icon="voltage"
        Text item=Battery_Capacity icon="battery"
        Text item=Battery_Max_A_Charge icon="battery"
        Text item=Battery_Max_A_Discharge icon="battery"
    }

    Frame label="Solar"{
        Text item=Total_Solar_Production icon="solar"
        Text item=Daily_Production icon="solar"
        Text item=PV1_Current icon="solar"
        Text item=PV1_Power icon="solar"
        Text item=PV1_Voltage icon="solar"
        Text item=PV2_Current icon="solar"
        Text item=PV2_Power icon="solar"
        Text item=PV2_Voltage icon="solar"
    }

    Frame label="Grid"{
        Text item=Total_Grid_Production icon="power"
        Text item=Total_Grid_Power icon="power"
        Text item=External_CT_L1_Power icon="power"
        Text item=External_CT_L2_Power icon="power"
        Text item=External_CT_L3_Power icon="power"
        Text item=Internal_CT_L1_Power icon="power"
        Text item=Internal_CT_L2_Power icon="power"
        Text item=Internal_CT_L3_Power icon="power"
        Text item=Grid_Voltage_L1 icon="power"
        Text item=Grid_Voltage_L2 icon="power"
        Text item=Grid_Voltage_L3 icon="power"
        Text item=Daily_Energy_Sold icon="power"
        Text item=Total_Energy_Sold icon="power"
        Text item=Daily_Energy_Bought icon="power"
        Text item=Total_Energy_Bought icon="power"
    }

    Frame label="Load"{
        Text item=Daily_Load_Consumption icon="power"
        Text item=Total_Load_Consumption icon="power"
        Text item=Load_L1_Power icon="power"
        Text item=Load_L2_Power icon="power"
        Text item=Load_L3_Power icon="power"
        Text item=Load_Voltage_L1 icon="power"
        Text item=Load_Voltage_L2 icon="power"
        Text item=Load_Voltage_L3 icon="power"
        Text item=Total_Load_Power icon="power"
    }

    Frame label="Generator"{
        Text item=Gen_Port_A_Phase_Power icon="poweroutlet"
        Text item=Gen_Port_B_Phase_Power icon="poweroutlet"
        Text item=Gen_Port_C_Phase_Power icon="poweroutlet"
        Text item=Gen_Port_A_Phase_Voltage icon="voltage"
        Text item=Gen_Port_B_Phase_Voltage icon="voltage"
        Text item=Gen_Port_C_Phase_Voltage icon="voltage"
        Text item=Total_Gen_Port_Power icon="power"
        Text item=Generator_Daily_Power_Generation icon="power"
        Text item=Generator_Total_Power_Generation icon="power"
    }

    Frame label="Alert"{
        Text item=Alert icon="alert"
    }
}
```

## Advanced Full Example

Use this example as an alternative to the legacy configuration above; both use the same Solarman Logger Thing type.
Select the profile matching the inverter; do not run multiple Things against the same physical logger.
The example below is for `sunsynk_single_phase` and is not a universal configuration for LV/HV, 16 kW or micro ESS models.

Copy only the three `sunsynk.*` files from [docs/examples](docs/examples) into the corresponding openHAB configuration directories, or use the contents below.
The `solarman.*` files are alternative legacy examples; do not activate both Things for one physical logger.
Replace the logger address/serial before use.
UI ranges are illustrative and do not override the selected profile's limits.
Time selections must remain consistent with neighboring periods; unsupported choices are rejected.

### `sunsynk.things`

```java
// Use one Thing per physical logger. Replace the address and logger serial.
Thing solarman:logger:deye [
    inverterType="sunsynk_single_phase",
    hostname="192.168.1.100",
    serialNumber="1234567890",
    port=8899,
    refreshInterval=30,
    solarmanLoggerMode="V5MODBUS",
    timeZone="Europe/Lisbon",
    slaveId=1
]
```

### `sunsynk.items`

```java
Number:Power DeyeBatteryPower "Battery Power [%.0f W]" { channel="solarman:logger:deye:battery-power", unit="W" }
Number:Dimensionless DeyeBatterySOC "Battery SOC [%.0f %%]" { channel="solarman:logger:deye:battery-soc", unit="%" }
Number:Power DeyePVPower "PV Power [%.0f W]" { channel="solarman:logger:deye:pv-power", unit="W" }
Number:ElectricCurrent DeyeChargeCurrent "Maximum Charge Current [%.0f A]" { channel="solarman:logger:deye:battery-max-charge-current", unit="A", autoupdate="false" }
Switch DeyeGridCharge "Grid Charge" { channel="solarman:logger:deye:grid-charge-enabled", autoupdate="false" }
Switch DeyeUseTimer "Use Timer" { channel="solarman:logger:deye:use-timer", autoupdate="false" }
String DeyeProgramTime "Program 1 Start [%s]" { channel="solarman:logger:deye:prog1-time", autoupdate="false" }
String DeyeProgramCharge "Program 1 Charge Source [%s]" { channel="solarman:logger:deye:prog1-charge", autoupdate="false" }
DateTime DeyeClock "Inverter Clock [%1$tF %1$tT]" { channel="solarman:logger:deye:date-time", autoupdate="false" }
Number:Dimensionless DeyeProgramSOC1 "Program 1 SOC [%.0f %%]" { channel="solarman:logger:deye:prog1-capacity", unit="%", autoupdate="false" }
Number:Dimensionless DeyeProgramSOC2 "Program 2 SOC [%.0f %%]" { channel="solarman:logger:deye:prog2-capacity", unit="%", autoupdate="false" }
Number:Dimensionless DeyeProgramSOC3 "Program 3 SOC [%.0f %%]" { channel="solarman:logger:deye:prog3-capacity", unit="%", autoupdate="false" }
Number:Dimensionless DeyeProgramSOC4 "Program 4 SOC [%.0f %%]" { channel="solarman:logger:deye:prog4-capacity", unit="%", autoupdate="false" }
Number:Dimensionless DeyeProgramSOC5 "Program 5 SOC [%.0f %%]" { channel="solarman:logger:deye:prog5-capacity", unit="%", autoupdate="false" }
Number:Dimensionless DeyeProgramSOC6 "Program 6 SOC [%.0f %%]" { channel="solarman:logger:deye:prog6-capacity", unit="%", autoupdate="false" }
```

### `sunsynk.sitemap`

```java
sitemap sunsynk label="Deye Inverter" {
    Frame label="Measurements" {
        Text item=DeyeBatteryPower
        Text item=DeyeBatterySOC
        Text item=DeyePVPower
        Text item=DeyeClock
    }
    Frame label="Controls" {
        Switch item=DeyeGridCharge
        Switch item=DeyeUseTimer
        Setpoint item=DeyeChargeCurrent minValue=0 maxValue=185 step=1
        Selection item=DeyeProgramCharge mappings=["No Grid or Gen"="Disabled", "Allow Grid"="Grid", "Allow Gen"="Generator", "Allow Grid & Gen"="Grid and Generator"]
        Selection item=DeyeProgramTime mappings=["00:00"="00:00", "06:00"="06:00", "12:00"="12:00", "18:00"="18:00"]
    }
    Frame label="Time of Use SOC" {
        Slider item=DeyeProgramSOC1 minValue=20 maxValue=100 step=1 releaseOnly
        Slider item=DeyeProgramSOC2 minValue=20 maxValue=100 step=1 releaseOnly
        Slider item=DeyeProgramSOC3 minValue=20 maxValue=100 step=1 releaseOnly
        Slider item=DeyeProgramSOC4 minValue=20 maxValue=100 step=1 releaseOnly
        Slider item=DeyeProgramSOC5 minValue=20 maxValue=100 step=1 releaseOnly
        Slider item=DeyeProgramSOC6 minValue=20 maxValue=100 step=1 releaseOnly
    }
}
```

`releaseOnly` sends the slider value when dragging ends, avoiding a stream of writes.
Selection mapping values are exact command strings, not numeric register keys.
See the [openHAB sitemap documentation](https://www.openhab.org/docs/ui/sitemaps) for widget syntax.

### Existing Legacy TOU SOC Items

For an existing `deye_hybrid` logger Thing, keep the original `time-of-use-time-of-use-soc-1` through `-6` links.
For example, if the Thing UID is `solarman:logger:local`, this Item uses the legacy channel:

```java
Number:Dimensionless LegacyProgramSOC1 "Time of Use SOC 1 [%.0f %%]" {
    channel="solarman:logger:local:time-of-use-time-of-use-soc-1",
    unit="%",
    autoupdate="false"
}
```

Use this widget inside a Frame in the existing sitemap:

```java
Slider item=LegacyProgramSOC1 minValue=20 maxValue=100 step=1 releaseOnly
```

Replace `local` with the actual Thing ID.
This example assumes `inverterType="deye_hybrid"`; the earlier full legacy example uses `deye_sg04lp3`, whose dynamic IDs can differ.
Verify the linked channel in the Thing before copying an Item definition.

## Local Installation and Development

For a manual test of this development build, retain one installed Solarman bundle, replace its JAR with `target/org.openhab.binding.solarman-5.3.0-SNAPSHOT.jar`, restart openHAB and reload the UI.
Review the channels and saved links when retaining the same inverterType, including upstream definition changes described above.
This main-based source targets openHAB 5.3; the separately retained local 5.2 delivery has its own validation evidence.

Build this module with Java 21 and Maven using `mvn spotless:apply clean install`.
See [source provenance and developer verification](docs/source-provenance.md) for pinned sources, regeneration and validation scope.

## Acknowledgments

The code's creation draws significant inspiration from [Stephan Joubert's Home Assistant plugin](https://github.com/StephanJoubert/home_assistant_solarman), which provides the inverter definitions used in the project.
Additionally, the [pysolarmanv5 module](https://pysolarmanv5.readthedocs.io/en/latest/index.html) was a valuable resource, as it offers an excellent explanation of the Solarman V5 protocol.

The advanced profiles derive from [kellerza/sunsynk at f1ecbc28e3ccf2b9f87b1ccee85a41628567da43](https://github.com/kellerza/sunsynk/tree/f1ecbc28e3ccf2b9f87b1ccee85a41628567da43), by Johann Kellerman and the project contributors, under Apache-2.0.
Source attribution is retained in NOTICE and the original full license is bundled as `sunsynk/LICENSE`.
The separate single-phase identification/version metadata derives from this binding's EPL-2.0 `deye_hybrid` definition.
See [source provenance](docs/source-provenance.md) for the distinction and generated fixture provenance.

The [migration register index](docs/examples/profile-channel-lists/register-index.csv) supports address-first review across the declared profiles.
The migration map exposes both source and target register tuples, conversion models and automatic-link eligibility.
Identical channel names across profiles do not imply identical registers; partial tuples and semantic differences require explicit review.
