# Keba Binding

This binding integrates the [Keba KeContact EV Charging Stations](https://www.keba.com).

## Supported Things

The `kecontact` Thing combines the available local interfaces of one wallbox. Modbus TCP supplies the main
measurements and commands, UDP supplies display commands and UDP-only information, and authenticated REST supplies
temperature and other REST-only information. Enable any supported combination of these protocols in the same Thing.

| Station variant                         | Ethernet                                        | UDP                           | Modbus TCP                        | REST API                                                  |
| --------------------------------------- | ----------------------------------------------- | ----------------------------- | --------------------------------- | --------------------------------------------------------- |
| KeContact P20 with network connection   | Yes                                             | Supported, firmware 2.5+      | Not supported                     | Not supported                                             |
|                                         |                                                 |                               |                                   |                                                           |
| KeContact P30 e-series                  | SKU-dependent; some variants have no Ethernet   | Not supported                 | Not supported                     | Not supported                                             |
| KeContact P30 a-series                  | SKU-dependent                                   | Not supported                 | Not supported                     | Not supported                                             |
| KeContact P30 b-series                  | SKU-dependent; some variants have no Ethernet   | SKU-dependent                 | SKU-dependent                     | Not supported                                             |
| **KeContact P30 c-series**              | Yes                                             | Supported, firmware 3.9.24+   | Supported, firmware 3.10.16+      | Not supported                                             |
| **KeContact P30 x-series**              | Yes                                             | Supported                     | Supported, firmware 1.11+         | Supported, developed and tested against firmware 2.1.0    |
| BMW wallbox (P30 x-series)              | Yes                                             | Supported                     | Supported, firmware 1.11+         | Supported, developed and tested against firmware 2.1.0    |
|                                         |                                                 |                               |                                   |                                                           |
| **KeContact P40 / P40 Pro**             | Yes                                             | Not supported                 | Supported                         | Supported                                                 |

Marketed editions such as **PV Edition** and **Dienstwagen Wallbox** are not enough on their own to determine protocol
support. Use the full KEBA product/type code to identify the underlying P30 series, then apply that series' row above;
Ethernet availability is SKU-dependent. The edition name alone does not imply UDP, Modbus TCP, or REST support.

Modbus TCP is not available on every KEBA station: P20 uses UDP instead. Protocol availability also depends on
firmware, enabled interfaces and network access; a timeout does not prove that a device lacks a protocol.
The combined Thing probes actual responses and does not enable interfaces on the wallbox automatically.

Use one combined Thing per physical wallbox. UDP and REST must address the wallbox represented by the Modbus
endpoint, not a different slave behind a master. The Modbus endpoint may be a proxy, while UDP and REST connect
directly to the wallbox. REST is optional: set `restEnabled=true` to enable it and supply REST credentials.

The REST API is available on P40/P40 Pro and P30 x-series/BMW wallboxes. It provides wallbox state, meter values,
and common commands such as unlock, start charging, and stop charging.

## Thing Configuration

### `kecontact` (Combined)

Configure `ipAddress` once with the wallbox IP address or hostname. Modbus, UDP and REST use this address.
For a Modbus proxy, set `modbusIpAddress` to the proxy address; UDP and REST still connect directly to `ipAddress`.
Enable each protocol through `modbusEnabled`, `udpEnabled` and `restEnabled`. For a UDP-only P20, disable Modbus
and leave UDP enabled. REST is disabled by default, even if a password is supplied. When enabled, its URL is
automatically generated as `https://<wallbox-address>:<restPort>`, with port `8443` by default.
The combined Thing verifies TLS certificates by default; explicitly disable
`verifyCertificate` only for a trusted local wallbox with a self-signed certificate.

Configuration is grouped with Common settings first, followed by Modbus TCP, UDP and REST API.

| Group      | Parameter           | Description                                                      | Default   |
| ---------- | ------------------- | ---------------------------------------------------------------- | --------- |
| Common     | ipAddress           | Wallbox address shared by all protocols                          | -         |
| Common     | refreshInterval     | Primary operational polling interval, minimum 10 seconds         | 12        |
| Common     | refreshIntervalSlow | Slow Modbus and supplemental polling interval, minimum 10 s      | 60        |
| Modbus TCP | modbusEnabled       | Enable Modbus TCP                                                | true      |
| Modbus TCP | modbusIpAddress     | Optional proxy address; blank uses the wallbox address           | ipAddress |
| Modbus TCP | port                | Modbus TCP port                                                  | 502       |
| Modbus TCP | unitId              | Modbus slave address                                             | 255       |
| UDP        | udpEnabled          | Enable UDP                                                       | true      |
| REST API   | restEnabled         | Enable REST                                                      | false     |
| REST API   | restPort            | REST HTTPS port                                                  | 8443      |
| REST API   | username            | REST username                                                    | admin     |
| REST API   | password            | REST password; required when REST is enabled                     | -         |
| REST API   | verifyCertificate   | Verify the REST TLS certificate and hostname                     | true      |

When updating an earlier development build, replace `udpIpAddress` with the common `ipAddress` and move any
Modbus proxy address to `modbusIpAddress`. Replace `baseUrl` with `restEnabled=true` and its port with `restPort`.

`refreshInterval` applies to fast Modbus values and primary UDP or REST operational data. The preferred
available source is Modbus, then REST, then UDP. If Modbus becomes unavailable, REST operational reads use
the fast interval; if neither Modbus nor REST is online, UDP operational reports use the fast interval.
`refreshIntervalSlow` applies to slow Modbus values and supplemental UDP or REST reads. Protocol recovery
automatically returns supplemental polling to the slow interval. Mixed reports/endpoints are read once at
the shorter interval required by their values; slower values in those responses update incidentally.
Initial identification, five-minute discovery retries, and on-demand commands do not use these polling intervals.

Polling is **not serialized across protocols**. Modbus enforces a 500 ms delay between its transactions and
five seconds between writes, but those delays do not apply to UDP or REST. The independent polling jobs can
overlap, including at startup. Avoiding duplicate reads and polling only required data reduces traffic, but does
not guarantee that the wallbox receives only one request at a time across all protocols.

To reduce device traffic:

- Modbus polls operational state and linked measurements only. Identification properties are read once per
  connection; linking or unlinking Items rebuilds the measurement polling plan without reopening the connection.
- UDP requests report 1 for identification. As the primary source, it reads report 2 at the fast interval and
  report 3 at that interval when measurements are linked. Reports 1 and 100 stay slow. With Modbus or REST
  online, report 2 is read slowly only for linked UDP-specific values. A report needed for both fast and slow
  data is not requested twice. A display action does not start cyclic polling: command availability can be
  checked with an on-demand report 2 without changing either cyclic deadline.
- REST reads the wallbox at the fast interval when Modbus is unavailable, otherwise at the slow interval.
  Session and DIP-switch interpretation endpoints stay slow and are requested only when their channels are
  linked. Phase-source configuration also stays slow and is read only when linked and Modbus is unavailable.
- Failed initial Modbus/UDP probes and failed REST initialization are retried at five-minute intervals. A failed
  optional interface does not take the Thing offline while another interface supplies baseline data.

The properties `modbusAvailable`, `udpAvailable` and `restAvailable` expose observed availability. Values include
`unknown`, `available`, `unavailable`, `disabled`, `unsupported` and UDP `identification-only`.
`unavailable` can mean an interface is disabled, unreachable, or authentication/certificate validation failed; it
does not mean the model permanently lacks that interface. P40 and identified P0 devices skip further UDP probes.
Identified P20 and P30 c-series devices stop unsuccessful REST probing; an actual working REST response takes
precedence over model assumptions.
All configured protocols belong to this single Thing; do not create separate Things for the same wallbox.

With Modbus, UDP and REST disabled, the Thing has zero channels and remains offline until a
protocol is enabled. Editing protocol settings restarts the affected adapters and reconciles the channel list to
the union of channels supported by the enabled protocols and the known wallbox model. Shared values keep one
stable channel ID rather than gaining protocol-specific duplicates.

#### Legacy Channel IDs

The migration updates existing flat `keba:kecontact` Things in place and retains legacy channel IDs where their
meaning is unchanged. `input` remains the UDP X1 signal; REST's separate X2-active value uses `restinput`. Protocol
capabilities unsupported by the configured interfaces and identified wallbox model are removed from the Thing.

## Channels

The combined `kecontact` Thing exposes one flat channel set. Channel IDs do not contain protocol prefixes, and
the same channel is updated by the preferred configured protocol with fallback to another configured protocol
that supports that value. This preserves legacy UDP channel UIDs such as `state`, `input`, `maxpilotcurrent`,
`maxsystemcurrent`, `failsafecurrent`, and `failsafetimeout`; existing Item links do not need a channel migration.

Only channels supported by at least one configured protocol and the identified wallbox model are retained.
For example, a P20 using UDP keeps UDP-supported channels and does not expose Modbus-only or unsupported REST
channels. Protocol-only channels are removed when that protocol is disabled or the model does not support it.
During initial detection, the full candidate set may be present until the wallbox model is identified.

The combined Thing defines 71 candidate channels before capability pruning. It does not expose duplicate
protocol-specific aliases for shared values. The numeric operational state is `state`; REST's textual state is
`reststate`. `triggerphaseswitch` accepts the requested phase count (1 = one phase, 3 = three phases) through
Modbus or UDP. REST exposes a separate `togglephaseswitch` Switch: send ON to toggle between single-phase and
three-phase charging. OFF is ignored, and the channel resets to OFF after a successful request. It does not
select a target phase count or emulate one through read-then-toggle. The `vehicle`, `wallbox`, and `locked`
switches use Modbus cable state when available, with UDP or REST fallback where supported.

Use `maxsystemcurrent` for the wallbox hardware limit, `maxpilotcurrent` for the current offered to the vehicle,
and `maxpresetcurrent` for the writable user setpoint. The setpoint is sent through Modbus when available and
falls back to UDP; the separate `setchargingcurrent` channel is not exposed by the combined Thing. REST's meter
values use `totalconsumption` and `maxpilotcurrent`.

### Model-Specific Channels

Model properties reported by REST, UDP, or Modbus are still detected. The combined Thing removes
`fastchargingstatus` and `activatefastcharging` after identifying a P30. The explicit Modbus family property is
used even if a longer display name is retained. Unknown models keep the candidate channel set until identified;
missing optional fields in a partial response are not evidence of an unsupported feature.

### Current Channels

| Purpose           | Recommended Channel(s)                | Meaning                                                        |
| ----------------- | ------------------------------------- | -------------------------------------------------------------- |
| Actual current    | `I1`, `I2`, `I3`                      | Measured current actually drawn on each phase                  |
| Hardware limit    | `maxsystemcurrent`                    | Maximum current supported by the wallbox hardware              |
| Offered current   | `maxpilotcurrent`                     | Current offered to the vehicle; actual draw can be lower       |
| User setpoint     | `maxpresetcurrent`                    | Writable current limit; routed through available protocols     |
| UDP percentage    | `maxpresetcurrentrange`               | Percentage representation of the UDP user setpoint             |
| Delayed setpoint  | `currtimer`, `currtimertimeout`       | Current and remaining time for the delayed UDP setpoint        |
| Failsafe setting  | `failsafecurrent`, `failsafetimeout`  | Current limit and timeout used after communication is lost     |
| Pilot PWM         | `maxpilotcurrentdutycyle`             | Control-pilot duty cycle, not a phase-current measurement      |

### Error Channels

You do not need to link all four error channels for normal monitoring. `errorcode` is the numeric Modbus
diagnostic; `error` provides the REST error name where supported. `error1` and `error2` retain the two raw UDP
diagnostic words for manufacturer troubleshooting. Their encodings are not interchangeable, so the binding does
not invent a common numeric mapping or discard either UDP word. UDP error words are only requested when linked.

### Input and Output

For the P20/P30 UDP interface, X1 is the enable input and X2 is the switched output. `input` reports the legacy
UDP X1 signal and `output` controls X2. `restinput` is labelled **REST X2 Active** and preserves the API's
`x2active` flag; it is not treated as the physical UDP X1 signal.

Only the combined `kecontact` Thing type is available.

When UDP is enabled and supported by the wallbox, the combined Thing exposes these channels:

| Channel ID              | Item Type                | Read-only | Description                                                             |
| ----------------------- | ------------------------ | --------- | ----------------------------------------------------------------------- |
| state                   | Number                   | yes       | current operational state of the wallbox                                |
| enabledsystem           | Switch                   | yes       | activation state of the wallbox (System)                                |
| enableduser             | Switch                   | no        | activation state of the wallbox (User)                                  |
| maxpresetcurrent        | Number:ElectricCurrent   | no        | maximum current the charging station should deliver to the EV in A      |
| maxpresetcurrentrange   | Number:Dimensionless     | no        | maximum current the charging station should deliver to the EV in %      |
| power                   | Number:Power             | yes       | active power delivered by the charging station                          |
| powerfactor             | Number:Dimensionless     | yes       | measured power factor (cos phi)                                         |
| wallbox                 | Switch                   | yes       | plug state of wallbox                                                   |
| vehicle                 | Switch                   | yes       | plug state of vehicle                                                   |
| locked                  | Switch                   | yes       | lock state of plug at vehicle                                           |
| I1/2/3                  | Number:ElectricCurrent   | yes       | current for the given phase                                             |
| U1/2/3                  | Number:ElectricPotential | yes       | voltage for the given phase                                             |
| output                  | Switch                   | no        | state of the X2 switched output                                         |
| input                   | Switch                   | yes       | state of the X1 enable input                                            |
| display                 | String                   | no        | display text on wallbox                                                 |
| error1                  | String                   | yes       | error code state 1, if in error (see the KeContact FAQ)                 |
| error2                  | String                   | yes       | error code state 2, if in error (see the KeContact FAQ)                 |
| backend                 | Switch                   | yes       | whether backend communication is present                                |
| timequality             | Number                   | yes       | wallbox clock synchronization quality (0-3)                             |
| bootflag                | Number                   | yes       | raw `setBoot` value; not documented in the UDP Programmer's Guide       |
| dipswitch1/2            | String                   | yes       | raw hexadecimal DIP-switch block values                                 |
| maxsystemcurrent        | Number:ElectricCurrent   | yes       | maximum current the wallbox can deliver                                 |
| failsafecurrent         | Number:ElectricCurrent   | yes       | maximum current the wallbox can deliver, if network is lost             |
| failsafetimeout         | Number:Time              | yes       | time before the failsafe current is applied                             |
| currtimer               | Number:ElectricCurrent   | yes       | delayed preset current applied when its timer expires                   |
| currtimertimeout        | Number:Time              | yes       | remaining time before the delayed preset current is applied             |
| phaseswitchsource       | Number                   | no        | communication source allowed to control phase switching                 |
| phaseswitchstate        | Number                   | no        | phase-switch state (1 or 3 phases), writable using the X2 phase command |
| triggerphaseswitch      | Number                   | no        | requests a phase count: 1 = single-phase, 3 = three-phase charging      |
| uptime                  | Number:Time              | yes       | system uptime since the last reset of the wallbox                       |
| sessionconsumption      | Number:Energy            | yes       | energy delivered in current session                                     |
| totalconsumption        | Number:Energy            | yes       | total energy delivered since the last reset of the wallbox              |
| authreq                 | Switch                   | yes       | authentication required                                                 |
| authon                  | Switch                   | yes       | authentication enabled                                                  |
| sessionrfidtag          | String                   | yes       | RFID tag used for the last charging session                             |
| sessionrfidclass        | String                   | yes       | RFID tag class used for the last charging session                       |
| sessionid               | Number                   | yes       | session ID of the last charging session                                 |
| setenergylimit          | Number:Energy            | no        | set an energy limit for an already running or the next charging session |
| authenticate            | String                   | no        | authenticate and start a session using RFID tag+RFID class              |
| maxpilotcurrent         | Number:ElectricCurrent   | yes       | current offered to the vehicle via control pilot signalization          |
| maxpilotcurrentdutycyle | Number:Dimensionless     | yes       | duty cycle of the control pilot signal                                  |

Modbus contributes these additional measurements and commands when enabled:

| Channel ID              | Item Type                | Read-only | Description                                                             |
| ----------------------- | ------------------------ | --------- | ----------------------------------------------------------------------- |
| state                   | Number                   | yes       | current operational state of the wallbox                                |
| cablestate              | Number                   | yes       | state of the charging cable                                             |
| errorcode               | Number                   | yes       | error code, if in error                                                 |
| I1/2/3                  | Number:ElectricCurrent   | yes       | current for the given phase                                             |
| U1/2/3                  | Number:ElectricPotential | yes       | voltage for the given phase                                             |
| power                   | Number:Power             | yes       | active power delivered by the charging station                          |
| powerfactor             | Number:Dimensionless     | yes       | power factor (cosphi)                                                   |
| totalconsumption        | Number:Energy            | yes       | total energy delivered since the last reset of the wallbox              |
| sessionconsumption      | Number:Energy            | yes       | energy delivered in current session                                     |
| maxpilotcurrent         | Number:ElectricCurrent   | yes       | maximum charging current currently offered to the vehicle               |
| maxsystemcurrent        | Number:ElectricCurrent   | yes       | maximum current the wallbox hardware can support                        |
| fastchargingstatus      | Number                   | yes       | raw P40 fast-charging status (register 1200)                            |
| sessionrfidtag          | String                   | yes       | RFID tag used for the last charging session (RFID enabled)              |
| phaseswitchsource       | Number                   | no        | source that is allowed to control the phase switching                   |
| phaseswitchstate        | Number                   | yes       | number of phases currently used (1 or 3)                                |
| failsafecurrent         | Number:ElectricCurrent   | no        | charging current to fall back to if the connection is lost              |
| failsafetimeout         | Number:Time              | no        | timeout after which the failsafe current is applied                     |
| failsafepersist         | Switch                   | no        | send ON to persist P30 EMS failsafe settings (register 5020)            |
| activatefastcharging    | Switch                   | no        | send ON to activate fast charging on P40 (register 5200)                |
| maxpresetcurrent        | Number:ElectricCurrent   | no        | writable current setpoint routed through Modbus when available          |
| setenergylimit          | Number:Energy            | no        | set an energy limit for an already running or the next charging session |
| unlockplug              | Switch                   | no        | send ON to unlock the plug (charging must be stopped first)             |
| enableduser             | Switch                   | no        | enable or disable the wallbox                                           |
| triggerphaseswitch      | Number                   | no        | requests a phase count: 1 = single-phase, 3 = three-phase charging      |

REST contributes these additional values and commands when configured:

| Channel ID              | Item Type                | Read-only | Description                                                                                               |
| ----------------------- | ------------------------ | --------- | --------------------------------------------------------------------------------------------------------- |
| reststate               | String                   | yes       | current operational state of the wallbox                                                                  |
| vehicle                 | Switch                   | yes       | whether a vehicle is connected                                                                            |
| session                 | Switch                   | yes       | whether a charging session is active                                                                      |
| reserved                | Switch                   | yes       | whether the wallbox has an active reservation                                                             |
| error                   | String                   | yes       | current wallbox error code                                                                                |
| maxpilotcurrent         | Number:ElectricCurrent   | yes       | charging current currently offered to the vehicle                                                         |
| maxsystemcurrent        | Number:ElectricCurrent   | yes       | maximum current supported by the wallbox                                                                  |
| power                   | Number:Power             | yes       | total active charging power                                                                               |
| totalconsumption        | Number:Energy            | yes       | total energy measured by the wallbox                                                                      |
| sessionstart            | DateTime                 | yes       | start time of the latest charging session                                                                 |
| sessionduration         | Number:Time              | yes       | duration of the latest charging session                                                                   |
| sessionconsumption      | Number:Energy            | yes       | energy consumed during the latest charging session                                                        |
| powerfactor             | Number:Dimensionless     | yes       | total power factor                                                                                        |
| temperature             | Number:Temperature       | yes       | internal wallbox temperature                                                                              |
| I1/2/3                  | Number:ElectricCurrent   | yes       | current for the given phase                                                                               |
| U1/2/3                  | Number:ElectricPotential | yes       | voltage for the given phase                                                                               |
| restinput               | Switch                   | yes       | raw REST x2active flag; not assumed to be the physical UDP X1 input                                       |
| authon                  | Switch                   | yes       | whether authorization is enabled                                                                          |
| externalmeter           | Switch                   | yes       | whether an external meter is present                                                                      |
| maxphases               | Number                   | yes       | maximum number of supported phases                                                                        |
| phaseswitchstate        | Number                   | yes       | number of phases currently used                                                                           |
| phaseswitchsource       | Number                   | no        | selects the phase-switch control source: disabled, OCPP profiles, REST direct control, Modbus TCP, or UDP |
| phaseconfiguration      | String                   | yes       | configured phase order                                                                                    |
| dipswitchsettings       | String                   | yes       | raw JSON array of DIP-switch values; null entries are preserved                                           |
| dipswitchinterpretation | String                   | yes       | named DIP-switch settings represented as a JSON object                                                    |
| enableduser             | Switch                   | no        | make the wallbox available or unavailable for charging                                                    |
| permanentlylocked       | Switch                   | no        | enable or disable permanent locking                                                                       |
| unlock                  | Switch                   | no        | send ON to unlock the wallbox                                                                             |
| start                   | Switch                   | no        | send ON to start charging                                                                                 |
| stop                    | Switch                   | no        | send ON to stop charging                                                                                  |
| togglephaseswitch       | Switch                   | no        | send ON to toggle single-/three-phase charging; OFF ignored; resets to OFF after success                  |
| reboot                  | Switch                   | no        | send ON to reboot the wallbox                                                                             |

### REST State and Error Values

The `state` channel returns one of these values:

| Value                    | Meaning                                                             |
|--------------------------|---------------------------------------------------------------------|
| `CHARGING`               | The socket is charging.                                             |
| `IDLE`                   | The socket is idling.                                               |
| `READY_FOR_CHARGING`     | The socket is ready for charging.                                   |
| `RECOVER_FROM_ERROR`     | The socket is recovering from an error.                             |
| `SERVICE_MODE`           | The socket is in service mode.                                      |
| `SUSPENDED`              | The socket is suspended.                                            |
| `TOKEN_PROGRAMMING_MODE` | The socket is in RFID programming mode.                             |
| `UNRECOVERABLE_ERROR`    | The socket is in an unrecoverable error state.                      |
| `UNAVAILABLE`            | The socket is out of order following a change-availability command. |
| `OFFLINE`                | The wallbox is not currently connected to the CPM.                  |
| `DEGRADED`               | The wallbox has an error; check the `error` channel.                |

The `error` channel returns an error-code value. The OpenAPI specification lists these values but does not define
their detailed causes:

`TEMPERATURE_DERATING`, `POWER_CONTROL`, `ISO_COMMUNICATION_NOT_WORKING`, `PLUG_LOST`, `PLUG_UNDEFINED`,
`OVERHEAT`, `LOCK_FAILED`, `OHMIC_LOAD`, `POWER_MISMATCH`, `IBN_MODE`, `SHORTCIRCUIT`, `CONFIGURATION_ERROR`,
`HARDWARE_FAULT`, `VOLTAGE_OUT_OF_RANGE`, `OVERLOAD`, `PILOT_NOK`, `RCD`, `METERING_MALFUNCTION`,
`CONTACTOR_FB_NOK`, `MISSIN_CFG`, `WATCHDOG_TRIGGERED`, `TEMPERATURE_THRESHOLD`,
`WRONG_CONTACTOR_SWITCHING`, `FSM_STATE_CHANGE_FAILURE`, `UNKNOWN_STATE`.

The REST API does not expose every value available through UDP or Modbus TCP. In particular, it does not provide
session RFID data, pilot PWM, uptime, relay output, failsafe settings, charging-current or energy-limit
setpoints. These channels are removed when no other enabled protocol and the identified model support them.

## Rule Actions

Certain KEBA models support setting display text through UDP. The combined Thing checks UDP command availability
without starting cyclic UDP polling.
The text can be set via a rule action `setDisplay`. It comes in two variants:

The REST API only supports changing persistent display templates for predefined wallbox states; it does not
provide an equivalent transient display-text command.

```java
rule "Set Display Text"
when
  System reached start level 100
then
   val keContactActions = getActions("keba", "keba:kecontact:1")
   // Default duration
   keContactActions.setDisplay("TEXT$1")
   // Explicit duration set
   keContactActions.setDisplay("TEXT$2", 5, 10)
end
```

| Parameter                | Description                                                                                                                                                                         |
| ------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| text                     | Text shown on the display. Maximum 23 ASCII characters can be used. `~` == Σ, `$` == blank, `,` == comma                                                                            |
| durationMin _(optional)_ | Defines the duration in seconds how long the text will be displayed before another display command will be processed (internal MID metering relevant information may overrule this) |
| durationMax _(optional)_ | Defines the duration in seconds how long the text will be displayed if no additional display command follows.                                                                       |

## Example

demo.Things:

```java
Thing keba:kecontact:combined [ipAddress="192.168.0.67", modbusEnabled=true, udpEnabled=true, restEnabled=true, restPort=8443, password="secret"]
Thing keba:kecontact:p20      [ipAddress="192.168.0.64", modbusEnabled=false, udpEnabled=true, restEnabled=false]
Thing keba:kecontact:proxied  [ipAddress="192.168.0.69", modbusIpAddress="192.168.0.20", modbusEnabled=true, unitId=255]
```

demo.items:

```java
Number:Dimensionless      KebaCurrentRange      "Maximum supply current [%.1f %%]"        {channel="keba:kecontact:combined:maxpresetcurrentrange"}
Number:ElectricCurrent    KebaCurrent           "Maximum supply current [%.3f A]"         {channel="keba:kecontact:combined:maxpresetcurrent"}
Number:ElectricCurrent    KebaSystemCurrent     "Maximum system supply current [%.3f A]"  {channel="keba:kecontact:combined:maxsystemcurrent"}
Number:ElectricCurrent    KebaFailSafeCurrent   "Failsafe supply current [%.3f A]"        {channel="keba:kecontact:combined:failsafecurrent"}
Number                    KebaState             "Operating State [%s]"                    {channel="keba:kecontact:combined:state"}
Switch                    KebaEnabledSystem     "Enabled (System)"                        {channel="keba:kecontact:combined:enabledsystem"}
Switch                    KebaEnabledUser       "Enabled (User)"                          {channel="keba:kecontact:combined:enableduser"}
Switch                    KebaWallboxPlugged    "Plugged into wallbox"                    {channel="keba:kecontact:combined:wallbox"}
Switch                    KebaVehiclePlugged    "Plugged into vehicle"                    {channel="keba:kecontact:combined:vehicle"}
Switch                    KebaPlugLocked        "Plug locked"                             {channel="keba:kecontact:combined:locked"}
DateTime                  KebaUptime            "Uptime [%s s]"                           {channel="keba:kecontact:combined:uptime"}
Number:ElectricCurrent    KebaI1                                                          {channel="keba:kecontact:combined:I1"}
Number:ElectricCurrent    KebaI2                                                          {channel="keba:kecontact:combined:I2"}
Number:ElectricCurrent    KebaI3                                                          {channel="keba:kecontact:combined:I3"}
Number:ElectricPotential  KebaU1                                                          {channel="keba:kecontact:combined:U1"}
Number:ElectricPotential  KebaU2                                                          {channel="keba:kecontact:combined:U2"}
Number:ElectricPotential  KebaU3                                                          {channel="keba:kecontact:combined:U3"}
Number:Power              KebaPower             "Energy during current session [%.1f W]"  {channel="keba:kecontact:combined:power"}
Number:Energy             KebaSessionEnergy                                               {channel="keba:kecontact:combined:sessionconsumption"}
Number:Energy             KebaTotalEnergy       "Energy during all sessions [%.1f Wh]"    {channel="keba:kecontact:combined:totalconsumption"}
Switch                    KebaInputSwitch                                                 {channel="keba:kecontact:combined:input"}
Switch                    KebaOutputSwitch                                                {channel="keba:kecontact:combined:output"}
Number:Energy             KebaSetEnergyLimit    "Set charge energy limit [%.1f Wh]"       {channel="keba:kecontact:combined:setenergylimit"}

```

demo.sitemap:

```perl
sitemap demo label="Main Menu"
{
  Text label="Charging Station"
  {
    Text    item=KebaState
    Text    item=KebaUptime
    Switch  item=KebaEnabledSystem
    Switch  item=KebaEnabledUser
    Switch  item=KebaWallboxPlugged
    Switch  item=KebaVehiclePlugged
    Switch  item=KebaPlugLocked
    Slider  item=KebaCurrentRange
    Text    item=KebaCurrent
    Text    item=KebaSystemCurrent
    Text    item=KebaFailSafeCurrent
    Text    item=KebaSessionEnergy
    Text    item=KebaTotalEnergy
    Switch  item=KebaSetEnergyLimit
  }
}
```

## Troubleshooting

### Enable Verbose Logging

Enable `DEBUG` or `TRACE` (even more verbose) logging for the logger named:

```text
org.openhab.binding.keba
```

For Modbus TCP request and response diagnostics, also enable `DEBUG` or `TRACE` logging for the core Modbus
transport logger:

```text
org.openhab.core.io.transport.modbus
```

With UDP enabled, check the binding log for UDP reports requested by linked channels. A display action does not
start cyclic UDP polling; report 1 is used for identification. Operational reports are fast when UDP is primary,
while supplemental reports stay slow.
For the combined `kecontact` Thing, fewer UDP reports are expected: check the availability properties and which
protocol-specific channels have linked Items before expecting a report to be polled.

The combined Thing remains `UNKNOWN` while initial protocol probes are pending. It becomes `ONLINE` when a
protocol supplies operational data, or `OFFLINE` if all enabled protocols finish without usable operational data.
If `udpAvailable` is `unavailable`, check that `ipAddress` addresses the wallbox
directly rather than a Modbus proxy, that UDP port 7090 is reachable, and that the UDP interface is enabled.
Failed UDP discovery is retried after five minutes. DEBUG logging distinguishes missing, invalid, and unexpected
report responses.

REST warnings about trusting all certificates and missing hostname verification are expected when
`verifyCertificate=false`. They indicate disabled TLS verification, not a UDP failure. Enable verification only
when the wallbox certificate is trusted and matches the REST URL hostname.

With Modbus enabled there is no equivalent UDP `report` message; with the core Modbus logger enabled, you see the
individual Modbus read/write requests and their responses (or, on failure, read/write errors), one register at a
time, at the configured fast or slow refresh interval.

### REST API Troubleshooting

The REST integration was developed and tested with a KeContact P30 x-series wallbox running firmware `2.1.0` and
KeMove REST API `v2.5.0`. Other firmware/API versions may also work, but have not necessarily been verified.

Before troubleshooting openHAB, use the KEBA eMobility App to confirm that the REST API login works with the same
username and password configured on the Thing. Verify that REST is enabled and that
`https://<ipAddress>:<restPort>` reaches the intended wallbox REST API. The serial number is discovered automatically.

This binding consumes the REST API's **v2** wallbox and session data. In particular, the v2 JSON field names and
values used for wallbox state, meter readings, and session details must be present and compatible. A firmware/API
version that changes or omits those v2 data elements may cause corresponding channels to remain undefined or the
Thing to report a communication error.

For local installations with a self-signed certificate, leave `verifyCertificate` disabled unless the certificate
is trusted by openHAB and matches the configured host. Enable `DEBUG` logging for `org.openhab.binding.keba` to
inspect REST initialization, polling, and request failures; never include passwords or bearer tokens when sharing
logs.

### UDP Ports Used

```text
Send port = UDP 7090
```

The Keba station is the server

```text
Receive port = UDP 7090
```

This binding is providing the server

UDP port 7090 needs to be available/free on the openHAB server.

In order to enable the UDP port 7090 on the Keba station with full functionality, `DIP switch 1.3` must be `ON`.
With `DIP switch 1.3 OFF` only ident-data can be read (`i` and `report 1`) but not the other reports as well as the commands needed for the write access.
After setting the DIP switch, you need to `power OFF` and `ON` the station. SW-reset via WebGUI seems not to be sufficient in order to apply the new configuration.

The right configuration can be validated as follows:

- WebGUI DSW Settings:
  - `DIP 1.3 | ON | UDP interface (SmartHome)`
- UDP response of `report 1`:
  - `DIP-Sw1` `0x20` Bit is set (enable at least `DEBUG` log-level for the binding)

### Modbus TCP Port Used

The default Modbus TCP port is `502` (configurable via the `port` parameter); the default Unit ID is `255` (configurable via the `unitId` parameter).
Unlike the UDP interface, the Modbus TCP interface is disabled by default and must be enabled explicitly:

- On the KeContact P30, set `DIP switch 1.3` to `ON`, then select Modbus TCP via the WebGUI or Installation Manual instructions. Depending on model, firmware, and configuration, UDP may also remain available for commands such as `setDisplay`; disable UDP probing in the combined Thing if its reports are not needed.
- On the KeContact P40, there are no DIP switches; enable the Modbus TCP interface and configure its port/Unit ID via the KEBA eMobility App, OCPP or REST API.

After enabling or changing the interface, power-cycle the station; a WebGUI/App SW-reset alone may not be sufficient to apply the new configuration.
