# Keba Binding

This binding integrates the [Keba KeContact EV Charging Stations](https://www.keba.com).

## Supported Things

The recommended `kecontact` Thing combines the available local interfaces of one wallbox. Modbus TCP supplies
the main measurements and commands, UDP supplies display commands and UDP-only information, and authenticated
REST supplies temperature and other REST-only information. The explicit `kecontact-udp`, `kecontact-modbus` and
`kecontact-rest` Things remain available for installations that need a single protocol.

| Station variant                         | Ethernet                                        | UDP (`kecontact-udp`)         | Modbus TCP (`kecontact-modbus`)   | REST API (`kecontact-rest`)                               |
| --------------------------------------- | ----------------------------------------------- | ----------------------------- | --------------------------------- | --------------------------------------------------------- |
| KeContact P20 with network connection   | Yes                                             | Supported, firmware 2.5+      | Not supported                     | Not supported                                             |
|                                         |                                                 |                               |                                   |                                                           |
| KeContact P30 e-series                  | SKU-dependent; some variants have no Ethernet   | Not supported                 | Not supported                     | Not supported                                             |
| KeContact P30 a-series                  | SKU-dependent                                   | Not supported                 | Not supported                     | Not supported                                             |
| KeContact P30 b-series                  | SKU-dependent; some variants have no Ethernet   | SKU-dependent                 | SKU-dependent                     | Not supported                                             |
| **KeContact P30 c-series**              | Yes                                             | Supported, firmware 3.9.24+   | Supported, firmware 3.10.16+      | Not supported                                             |
| **KeContact P30 x-series**              | Yes                                             | Supported                     | Supported, firmware 1.11+         | Supported, developed and tested against firmeware 2.1.0   |
| BMW wallbox (P30 x-series)              | Yes                                             | Supported                     | Supported, firmware 1.11+         | Supported, developed and tested against firmeware 2.1.0   |
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
directly to the wallbox. REST is optional: leave `baseUrl` empty to disable REST probing. Setting a REST URL
enables the interface and requires REST credentials.

The `kecontact-rest` Thing uses the authenticated HTTPS API available on P40/P40 Pro and P30 x-series/BMW wallboxes.
It provides wallbox state, meter values, and common commands such as unlock, start charging, and stop charging.

Choose the protocol that best fits your installation:

| Thing type           | Advantages                                                                              | Limitations                                                                                                    |
| -------------------- | --------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------- |
| `kecontact`          | Combines Modbus, UDP display and REST enrichment; avoids duplicate polling              | Optional interfaces must be enabled; REST enrichment needs credentials                                         |
| `kecontact-udp`      | Legacy UDP support, no credentials required, broad P20/P30 compatibility                | UDP setup/DIP switch required; no transport security; single shared UDP port                                   |
| `kecontact-modbus`   | Local register access, deterministic Modbus semantics, no REST credentials              | Requires Modbus TCP to be enabled; polling is register-based and slower; smaller documented channel set        |
| `kecontact-rest`     | Authenticated HTTPS API, wallbox state and meter data, convenient high-level commands   | Requires REST credentials; API availability depends on firmware; local HTTPS certificates may be self-signed   |

## Thing Configuration

### `kecontact` (Combined)

Configure `ipAddress` for the Modbus TCP endpoint, which may be the wallbox or a Modbus proxy. Set `udpIpAddress`
to the direct wallbox address when it differs; an empty UDP address falls back to `ipAddress`. Existing single-address
configurations remain valid. To enable REST, explicitly configure `baseUrl`, for example
`https://<wallbox-address>:8443`, and supply the credentials. A blank `baseUrl`
disables REST even if a password is supplied. The combined Thing verifies TLS certificates by default; explicitly disable
`verifyCertificate` only for a trusted local wallbox with a self-signed certificate.

Configuration is grouped with Modbus TCP first, followed by UDP, REST API, and shared polling settings.

| Group          | Parameter           | Description                                                      | Default   |
| -------------- | ------------------- | ---------------------------------------------------------------- | --------- |
| Modbus TCP     | ipAddress           | Wallbox Modbus address or Modbus proxy address                   | -         |
| Modbus TCP     | port                | Modbus TCP port                                                  | 502       |
| Modbus TCP     | unitId              | Modbus slave address                                             | 255       |
| Modbus TCP     | refreshInterval     | Fast Modbus polling interval, minimum 10 seconds                 | 12        |
| UDP            | udpIpAddress        | Direct wallbox address; leave empty to use the Modbus address    | ipAddress |
| UDP            | udpEnabled          | Probe UDP for display commands and UDP-only channels             | true      |
| REST API       | baseUrl             | HTTPS REST URL; leave empty to disable REST                      | -         |
| REST API       | username            | REST username                                                    | admin     |
| REST API       | password            | REST password; required when baseUrl is set                      | -         |
| REST API       | verifyCertificate   | Verify the REST TLS certificate and hostname                     | true      |
| Shared Polling | refreshIntervalSlow | Slow Modbus, REST and required UDP report interval, minimum 10 s | 60        |

`refreshInterval` applies only to fast Modbus values and baseline Modbus status. `refreshIntervalSlow` applies
to slow Modbus values, REST polling, and required UDP reports. Initial identification, five-minute discovery
retries, and on-demand commands do not use these polling intervals.

Polling is **not serialized across protocols**. Modbus enforces a 500 ms delay between its transactions and
five seconds between writes, but those delays do not apply to UDP or REST. The independent polling jobs can
overlap, including at startup. Avoiding duplicate reads and polling only required data reduces traffic, but does
not guarantee that the wallbox receives only one request at a time across all protocols.

To reduce device traffic:

- Modbus polls operational state and linked measurements only. Identification properties are read once per
  connection; linking or unlinking Items rebuilds the measurement polling plan without reopening the connection.
- UDP requests report 1 for identification. With Modbus active, only linked UDP-only values cause cyclic report
  requests. A display action does not start cyclic polling. UDP command availability is checked using the
  DIP-switch flag or an on-demand report 2 request.
- REST reads the wallbox once per slow interval. Session and DIP-switch interpretation endpoints are requested
  only when their channels are linked. Phase-source configuration is read only when linked and Modbus is unavailable.
- Failed initial Modbus/UDP probes and failed REST initialization are retried at five-minute intervals. A failed
  optional interface does not take the Thing offline while another interface supplies baseline data.

The properties `modbusAvailable`, `udpAvailable` and `restAvailable` expose observed availability. Values include
`unknown`, `available`, `unavailable`, `disabled`, `unsupported` and UDP `identification-only`.
`unavailable` can mean an interface is disabled, unreachable, or authentication/certificate validation failed; it
does not mean the model permanently lacks that interface. P40 and identified P0 devices skip further UDP probes.
Identified P20 and P30 c-series devices stop unsuccessful REST probing; an actual working REST response takes
precedence over model assumptions.
Do not configure an additional UDP Thing for the same wallbox alongside the combined Thing.

#### Migration from UDP-Only `kecontact`

The former UDP-only Thing is now named `kecontact-udp`. For unchanged UDP behavior, recreate the Thing using that
type and update Item links and rule-action UIDs. Textual Things can be changed to `keba:kecontact-udp:<id>`.
To adopt the combined Thing, recreate the existing `kecontact` Thing to obtain its full channel set, configure
Modbus and optional REST credentials, and ensure both refresh intervals are at least 10 seconds. The old
`refreshInterval=0` setting is specific to `kecontact-udp` and is not valid for the combined Thing.

### `kecontact-udp` (UDP)

The `kecontact-udp` Thing supports network-enabled P20 stations and compatible P30 variants through the legacy UDP
interface. It requires the IP address as the configuration parameter `ipAddress`.
The optional `refreshInterval` parameter sets the polling interval in seconds. Set it to `0` to disable all cyclic
UDP report polling while keeping the Thing initialized for commands such as the `setDisplay` rule action. This is
useful when REST or Modbus TCP is used for monitoring and control, but UDP is needed for display text. With polling
disabled, UDP channel states are not refreshed by this Thing.

### `kecontact-modbus` (Modbus TCP)

The `kecontact-modbus` Thing connects directly to the wallbox's Modbus TCP interface; it does not require a separate Modbus bridge Thing to be configured.
The Modbus TCP interface must be enabled on the wallbox beforehand (via the KEBA eMobility App, OCPP or REST API).
Interface combinations can depend on wallbox model, firmware, and configuration; installations may use the UDP
Thing alongside Modbus TCP for UDP-only commands.

| Parameter           | Description                                                                                 | Default |
| ------------------- | ------------------------------------------------------------------------------------------- | ------- |
| ipAddress           | Network address of the wallbox                                                              | -       |
| port                | TCP port of the Modbus TCP interface of the wallbox                                         | 502     |
| unitId              | Modbus unit id (slave address) of the wallbox                                               | 255     |
| refreshInterval     | Refresh interval in seconds for frequently changing registers                               | 12      |
| refreshIntervalSlow | Refresh interval in seconds for registers that change infrequently                          | 60      |

The Modbus interface requires at least five seconds between different write jobs. The binding queues writes
separately at this interval while retaining a 500 ms delay between all Modbus transactions, including reads.
The fast polling interval has a minimum of 10 seconds because the 12 frequently changing registers require at
least 6 seconds at the configured 500 ms transaction delay; the additional margin allows for request processing.

The KEBA Modbus TCP register set is smaller than what the UDP interface exposes; channels not backed by a documented register (e.g. pilot current/duty cycle, X1/X2 relay state, display text, RFID authentication) are not available on this Thing type. At startup, the binding reads product information register 1016 first. It uses the product-family code to skip P40-only polls and remove P40-only channels on P30 Things; unknown product codes retain the full channel set and permissive polling.

### `kecontact-rest` (REST API)

The `kecontact-rest` Thing connects to the KEBA REST API, normally at `https://<wallbox>:8443`, and authenticates with the configured username and password. If `serialNumber` is left empty, the binding reads the wallbox serial number from the REST API endpoint `/serialnumber`. In a master/slave setup, configure `serialNumber` explicitly to select a particular slave wallbox.

As with the UDP Thing, REST and Modbus expose the detected wallbox serial number as the Thing property `serial`, not as a channel.

The Modbus Thing exposes register 1016 as the raw `productType` Thing property and uses it to set the detected `model` property. Register 1018 is exposed as the hexadecimal `firmware` Thing property; neither value has a channel.

The P40 Modbus registers 1700 and 1702 are exposed as the `hardwareRevisionDevice` and `hardwareRevisionKcMs10` Thing properties. The UDP Thing exposes report 1's communication-module presence as the `communicationModulePresent` Thing property.

The Thing property `ipAddress` shows the host used by the configured `baseUrl`. The address returned by the REST
API is retained separately as `reportedIpAddress`; depending on the wallbox network configuration, that value may
be a static fallback address rather than the currently active address.

| Parameter         | Description                                                                                                              | Default    |
|-------------------|--------------------------------------------------------------------------------------------------------------------------|------------|
| baseUrl           | Base URL of the wallbox REST API                                                                                         | `https://` |
| username          | REST API username                                                                                                        | `admin`    |
| password          | REST API password                                                                                                        | -          |
| serialNumber      | Optional serial number; leave empty for automatic discovery, or set it to select a slave wallbox in a master/slave setup | -          |
| refreshInterval   | REST polling interval in seconds                                                                                         | 10         |
| verifyCertificate | Verify the wallbox TLS certificate and hostname                                                                          | `false`    |

The REST Thing is a good choice when authenticated HTTPS access and high-level commands are more important than the complete low-level register set. Set `verifyCertificate` to `true` only when the wallbox certificate is trusted by openHAB and contains the configured host name or IP address. Local KEBA installations commonly use a self-signed certificate; with `verifyCertificate` set to `false`, certificate trust and host name verification are both disabled.

## Channels

The combined `kecontact` Thing exposes native channel groups in Modbus, UDP, REST order:

| Group      | Prefix    | Example Channel    |
| ---------- | --------- | ------------------ |
| Modbus TCP | `modbus#` | `modbus#power`     |
| UDP        | `udp#`    | `udp#display`      |
| REST API   | `rest#`   | `rest#temperature` |

Groups identify the preferred protocol, not the protocol temporarily supplying a fallback value. Modbus-derived
switches stay in the Modbus group. Shared writable controls, such as `enableduser`,
also stay in the Modbus group even when their readback comes from REST or UDP.

The combined Thing provides 71 channels, including `udp#inputx1` for the physical X1 input. Four redundant
compatibility aliases have been removed; update their Item links using the mapping below. Other existing flat
channels remain supported; recreate the Thing
to adopt grouped channel definitions, then update Item links, for example from `keba:kecontact:1:power` to
`keba:kecontact:1:modbus#power`. The explicit UDP, Modbus and REST Thing types retain their existing flat channel IDs.
Rule actions use the Thing UID and are unaffected by channel grouping.

The channel tables below list local names without group prefixes. The combined Thing's `state` is numeric;
REST's textual state is exposed separately as `reststate`. Its
`triggerphaseswitch` uses the Modbus numeric command (0 = one phase, 1 = three phases), not the REST toggle command.
The `vehicle`, `wallbox` and `locked` switches are derived from Modbus cable state when Modbus is available.
REST `energy` and `current` use the combined names `totalconsumption` and `maxchargingcurrent`.
UDP fallback reports update the canonical Modbus-group current and failsafe channels directly. UDP user-current
setpoints remain distinct from the current offered to the vehicle.
Channels that require an unavailable protocol remain undefined; the Thing does not invent replacement values.
Use `setchargingcurrent` for Modbus current commands and `display` or the `setDisplay` action for UDP display text.

Each protocol group is ordered by function: operational state, electrical/current values, session information,
phase switching, failsafe settings, and diagnostics/configuration where applicable. Channel descriptions identify
the actual measurement or command rather than repeating a generic label.

### Model-Specific Channels

Model properties reported by REST, UDP, or Modbus are still detected. The combined Thing removes
`modbus#fastchargingstatus` and `modbus#activatefastcharging` after identifying a P30. The explicit Modbus family
property is used even if a longer display name is retained. Unknown models keep the full channel set until
identified; missing optional fields in a partial response are not evidence of an unsupported feature.

### Current Channels

| Purpose           | Recommended Channel(s)                  | Meaning                                                        |
| ----------------- | --------------------------------------- | -------------------------------------------------------------- |
| Actual current    | `modbus#I1`, `modbus#I2`, `modbus#I3`   | Measured current actually drawn on each phase                  |
| Hardware limit    | `modbus#maxsupportedcurrent`            | Maximum current supported by the wallbox hardware              |
| Offered current   | `modbus#maxchargingcurrent`             | Current offered to the vehicle; actual draw can be lower       |
| Modbus setpoint   | `modbus#setchargingcurrent`             | Requested charging-current limit sent through Modbus           |
| UDP user setpoint | `udp#maxpresetcurrent`                  | UDP user-current limit, not a measured current                 |
| UDP percentage    | `udp#maxpresetcurrentrange`             | Alternative percentage representation of the UDP user setpoint |
| Delayed setpoint  | `udp#currtimer`, `udp#currtimertimeout` | Current and remaining time for the delayed UDP setpoint        |
| Failsafe setting  | `modbus#failsafecurrentsetting`         | Current limit used after a communication timeout               |
| Pilot PWM         | `udp#maxpilotcurrentdutycyle`           | Control-pilot duty cycle, not a phase-current measurement      |

Four redundant combined channels have been removed from new and existing combined Things. Update any Item links
that use these old names; the standalone `kecontact-udp` Thing retains its original channel set.

| Removed Channel           | Replacement Channel             |
| ------------------------- | ------------------------------- |
| `modbus#maxsystemcurrent` | `modbus#maxsupportedcurrent`    |
| `modbus#maxpilotcurrent`  | `modbus#maxchargingcurrent`     |
| `modbus#failsafecurrent`  | `modbus#failsafecurrentsetting` |
| `modbus#failsafetimeout`  | `modbus#failsafetimeoutsetting` |

### Error Channels

You do not need to link all four error channels for normal monitoring. `modbus#errorcode` is the numeric Modbus
diagnostic; `rest#error` provides the REST error name where supported. `udp#error1` and `udp#error2` retain the
two raw UDP diagnostic words for manufacturer troubleshooting. Their encodings are not interchangeable, so the
binding does not invent a common numeric mapping or discard either UDP word. UDP error words are advanced channels
and are only requested when linked.

### Input and Output

For the P20/P30 UDP interface, X1 is the enable input and X2 is the switched output. `udp#inputx1` reports X1;
`udp#output` controls X2. The former reversed descriptions in this README were incorrect.

`rest#input` is labelled **REST X2 Active** and preserves the API's `x2active` flag. It is not assumed to be the
physical UDP X1 signal, and grouped Things no longer substitute one for the other. The old flat combined `input`
retains its legacy UDP fallback for compatibility. Recreate an existing grouped Thing to add `udp#inputx1` and
link it explicitly when monitoring the physical enable input.

The `kecontact-udp` (UDP) Thing type supports the following channels:

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

The `kecontact-modbus` Thing type supports the following channels instead:

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
| maxchargingcurrent      | Number:ElectricCurrent   | yes       | maximum charging current currently offered to the vehicle               |
| maxsupportedcurrent     | Number:ElectricCurrent   | yes       | maximum current the wallbox hardware can support                        |
| fastchargingstatus      | Number                   | yes       | raw P40 fast-charging status (register 1200)                            |
| sessionrfidtag          | String                   | yes       | RFID tag used for the last charging session (RFID enabled)              |
| phaseswitchsource       | Number                   | no        | source that is allowed to control the phase switching                   |
| phaseswitchstate        | Number                   | yes       | number of phases currently used (1 or 3)                                |
| failsafecurrentsetting  | Number:ElectricCurrent   | no        | charging current to fall back to if the connection is lost              |
| failsafetimeoutsetting  | Number:Time              | no        | timeout after which the failsafe current is applied                     |
| failsafepersist         | Switch                   | no        | send ON to persist P30 EMS failsafe settings (register 5020)            |
| activatefastcharging    | Switch                   | no        | send ON to activate fast charging on P40 (register 5200)                |
| setchargingcurrent      | Number:ElectricCurrent   | no        | sets the charging current the wallbox should offer to the vehicle       |
| setenergylimit          | Number:Energy            | no        | set an energy limit for an already running or the next charging session |
| unlockplug              | Switch                   | no        | send ON to unlock the plug (charging must be stopped first)             |
| enableduser             | Switch                   | no        | enable or disable the wallbox                                           |
| triggerphaseswitch      | Number                   | no        | triggers the phase switch (0 = 1 phase, 1 = 3 phases)                   |

The `kecontact-rest` Thing type supports the following channels:

| Channel ID              | Item Type                | Read-only | Description                                                                                               |
| ----------------------- | ------------------------ | --------- | --------------------------------------------------------------------------------------------------------- |
| state                   | String                   | yes       | current operational state of the wallbox                                                                  |
| vehicle                 | Switch                   | yes       | whether a vehicle is connected                                                                            |
| session                 | Switch                   | yes       | whether a charging session is active                                                                      |
| reserved                | Switch                   | yes       | whether the wallbox has an active reservation                                                             |
| error                   | String                   | yes       | current wallbox error code                                                                                |
| current                 | Number:ElectricCurrent   | yes       | charging current currently offered to the vehicle                                                         |
| maxsupportedcurrent     | Number:ElectricCurrent   | yes       | maximum current supported by the wallbox                                                                  |
| power                   | Number:Power             | yes       | total active charging power                                                                               |
| energy                  | Number:Energy            | yes       | total energy measured by the wallbox                                                                      |
| sessionstart            | DateTime                 | yes       | start time of the latest charging session                                                                 |
| sessionduration         | Number:Time              | yes       | duration of the latest charging session                                                                   |
| sessionconsumption      | Number:Energy            | yes       | energy consumed during the latest charging session                                                        |
| powerfactor             | Number:Dimensionless     | yes       | total power factor                                                                                        |
| temperature             | Number:Temperature       | yes       | internal wallbox temperature                                                                              |
| I1/2/3                  | Number:ElectricCurrent   | yes       | current for the given phase                                                                               |
| U1/2/3                  | Number:ElectricPotential | yes       | voltage for the given phase                                                                               |
| input                   | Switch                   | yes       | raw REST x2active flag; not assumed to be the physical UDP X1 input                                       |
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
| triggerphaseswitch      | Switch                   | no        | send ON to toggle between single-phase and three-phase charging                                           |
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
setpoints. These channels are therefore not offered by the REST Thing rather than being published permanently as
undefined values.

## Rule Actions

Certain Keba models using the combined `kecontact` or explicit `kecontact-udp` Thing support setting display text
through UDP. The combined Thing checks UDP command availability without starting cyclic UDP polling.
The text can be set via a rule action `setDisplay`. It comes in two variants:

This action is not available for the REST Thing. The REST API only supports changing persistent display templates
for predefined wallbox states; it does not provide an equivalent transient display-text command.

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
Thing keba:kecontact:1        [ipAddress="192.168.0.64", refreshInterval=30]
Thing keba:kecontact:combined [ipAddress="192.168.0.67", baseUrl="https://192.168.0.67:8443", password="secret", verifyCertificate=false]
Thing keba:kecontact:proxied  [ipAddress="192.168.0.20", udpIpAddress="192.168.0.69", unitId=255]
Thing keba:kecontact-udp:1    [ipAddress="192.168.0.68", refreshInterval=0]
Thing keba:kecontact-modbus:1 [ipAddress="192.168.0.65", refreshInterval=12, refreshIntervalSlow=60]
Thing keba:kecontact-rest:1   [baseUrl="https://192.168.0.66:8443", username="admin", password="secret", serialNumber="12345678", refreshInterval=10, verifyCertificate=false]
```

demo.items:

```java
Number:Dimensionless      KebaCurrentRange      "Maximum supply current [%.1f %%]"        {channel="keba:kecontact:1:udp#maxpresetcurrentrange"}
Number:ElectricCurrent    KebaCurrent           "Maximum supply current [%.3f A]"         {channel="keba:kecontact:1:udp#maxpresetcurrent"}
Number:ElectricCurrent    KebaSystemCurrent     "Maximum system supply current [%.3f A]"  {channel="keba:kecontact:1:modbus#maxsupportedcurrent"}
Number:ElectricCurrent    KebaFailSafeCurrent   "Failsafe supply current [%.3f A]"        {channel="keba:kecontact:1:modbus#failsafecurrentsetting"}
Number                    KebaState             "Operating State [%s]"                    {channel="keba:kecontact:1:modbus#state"}
Switch                    KebaEnabledSystem     "Enabled (System)"                        {channel="keba:kecontact:1:udp#enabledsystem"}
Switch                    KebaEnabledUser       "Enabled (User)"                          {channel="keba:kecontact:1:modbus#enableduser"}
Switch                    KebaWallboxPlugged    "Plugged into wallbox"                    {channel="keba:kecontact:1:modbus#wallbox"}
Switch                    KebaVehiclePlugged    "Plugged into vehicle"                    {channel="keba:kecontact:1:modbus#vehicle"}
Switch                    KebaPlugLocked        "Plug locked"                             {channel="keba:kecontact:1:modbus#locked"}
DateTime                  KebaUptime            "Uptime [%s s]"                           {channel="keba:kecontact:1:udp#uptime"}
Number:ElectricCurrent    KebaI1                                                          {channel="keba:kecontact:1:modbus#I1"}
Number:ElectricCurrent    KebaI2                                                          {channel="keba:kecontact:1:modbus#I2"}
Number:ElectricCurrent    KebaI3                                                          {channel="keba:kecontact:1:modbus#I3"}
Number:ElectricPotential  KebaU1                                                          {channel="keba:kecontact:1:modbus#U1"}
Number:ElectricPotential  KebaU2                                                          {channel="keba:kecontact:1:modbus#U2"}
Number:ElectricPotential  KebaU3                                                          {channel="keba:kecontact:1:modbus#U3"}
Number:Power              KebaPower             "Energy during current session [%.1f W]"  {channel="keba:kecontact:1:modbus#power"}
Number:Energy             KebaSessionEnergy                                               {channel="keba:kecontact:1:modbus#sessionconsumption"}
Number:Energy             KebaTotalEnergy       "Energy during all sessions [%.1f Wh]"    {channel="keba:kecontact:1:modbus#totalconsumption"}
Switch                    KebaInputSwitch                                                 {channel="keba:kecontact:1:udp#inputx1"}
Switch                    KebaOutputSwitch                                                {channel="keba:kecontact:1:udp#output"}
Number:Energy             KebaSetEnergyLimit    "Set charge energy limit [%.1f Wh]"       {channel="keba:kecontact:1:modbus#setenergylimit"}

Number                    KebaModbusState       "Operating State [%s]"                    {channel="keba:kecontact-modbus:1:state"}
Number:ElectricCurrent    KebaModbusI1                                                    {channel="keba:kecontact-modbus:1:I1"}
Number:ElectricCurrent    KebaModbusI2                                                    {channel="keba:kecontact-modbus:1:I2"}
Number:ElectricCurrent    KebaModbusI3                                                    {channel="keba:kecontact-modbus:1:I3"}
Number:Power              KebaModbusPower       "Active power [%.3f W]"                   {channel="keba:kecontact-modbus:1:power"}
Number:Energy             KebaModbusSessionEnergy                                         {channel="keba:kecontact-modbus:1:sessionconsumption"}
Number:Energy             KebaModbusTotalEnergy "Energy during all sessions [%.1f Wh]"    {channel="keba:kecontact-modbus:1:totalconsumption"}
Number:ElectricCurrent    KebaModbusSetCurrent  "Set charging current [%.3f A]"           {channel="keba:kecontact-modbus:1:setchargingcurrent"}
Switch                    KebaModbusEnabled     "Wallbox enabled"                         {channel="keba:kecontact-modbus:1:enableduser"}
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

For the `kecontact-udp` Thing type, if everything is working fine, you see the cyclic reception of `report 1`, `2` & `3` from the station. The frequency is according to the `refreshInterval` configuration.
For the combined `kecontact` Thing, fewer UDP reports are expected: check the availability properties and which
protocol-specific channels have linked Items before expecting a report to be polled.

The combined Thing remains `UNKNOWN` while initial protocol probes are pending. It becomes `ONLINE` when a
protocol supplies operational data, or `OFFLINE` if all enabled protocols finish without usable operational data.
If `udpAvailable` is `unavailable`, check that `udpIpAddress` (or the `ipAddress` fallback) addresses the wallbox
directly rather than a Modbus proxy, that UDP port 7090 is reachable, and that the UDP interface is enabled.
Failed UDP discovery is retried after five minutes. DEBUG logging distinguishes missing, invalid, and unexpected
report responses.

REST warnings about trusting all certificates and missing hostname verification are expected when
`verifyCertificate=false`. They indicate disabled TLS verification, not a UDP failure. Enable verification only
when the wallbox certificate is trusted and matches the REST URL hostname.

For the `kecontact-modbus` Thing type there is no equivalent `report` message; with the core Modbus logger enabled,
you see the individual Modbus read/write requests and their responses (or, on failure, read/write errors), one
register at a time, at the configured fast or slow refresh interval.

### REST API Troubleshooting (`kecontact-rest` Thing type only)

The REST integration was developed and tested with a KeContact P30 x-series wallbox running firmware `2.1.0` and
KeMove REST API `v2.5.0`. Other firmware/API versions may also work, but have not necessarily been verified.

Before troubleshooting openHAB, use the KEBA eMobility App to confirm that the REST API login works with the same
username and password configured on the Thing. Verify that the configured `baseUrl` reaches the wallbox REST API
and that `serialNumber` is either blank for automatic discovery or identifies the intended wallbox in a master/slave
installation.

This binding consumes the REST API's **v2** wallbox and session data. In particular, the v2 JSON field names and
values used for wallbox state, meter readings, and session details must be present and compatible. A firmware/API
version that changes or omits those v2 data elements may cause corresponding channels to remain undefined or the
Thing to report a communication error.

For local installations with a self-signed certificate, leave `verifyCertificate` disabled unless the certificate
is trusted by openHAB and matches the configured host. Enable `DEBUG` logging for `org.openhab.binding.keba` to
inspect REST initialization, polling, and request failures; never include passwords or bearer tokens when sharing
logs.

### UDP Ports Used (`kecontact` and `kecontact-udp`)

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

### Modbus TCP Port used (`kecontact-modbus` Thing type only)

The default Modbus TCP port is `502` (configurable via the `port` parameter); the default Unit ID is `255` (configurable via the `unitId` parameter).
Unlike the UDP interface, the Modbus TCP interface is disabled by default and must be enabled explicitly:

- On the KeContact P30, set `DIP switch 1.3` to `ON`, then select Modbus TCP via the WebGUI or Installation Manual instructions. Depending on model, firmware, and configuration, UDP may also remain available for commands such as `setDisplay`; set the UDP Thing's `refreshInterval` to `0` if its reports are not needed.
- On the KeContact P40, there are no DIP switches; enable the Modbus TCP interface and configure its port/Unit ID via the KEBA eMobility App, OCPP or REST API.

After enabling or changing the interface, power-cycle the station; a WebGUI/App SW-reset alone may not be sufficient to apply the new configuration.
