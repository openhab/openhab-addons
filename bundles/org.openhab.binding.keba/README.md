# Keba Binding

This binding connects openHAB to KEBA KeContact EV charging stations.

## Supported Models

Create one `kecontact` Thing per wallbox and enable interfaces supported by that model.

| Model                        | Ethernet      | UDP              | Modbus TCP        | REST |
|------------------------------|---------------|------------------|-------------------|------|
| P20 with network connection  | Yes           | Firmware 2.5+    | No                | No   |
| P30 e-series                 | SKU-dependent | No               | No                | No   |
| P30 a-series                 | SKU-dependent | No               | No                | No   |
| P30 b-series                 | SKU-dependent | SKU-dependent    | SKU-dependent     | No   |
| P30 c-series                 | Yes           | Firmware 3.9.24+ | Firmware 3.10.16+ | No   |
| P30 x-series and BMW wallbox | Yes           | Yes              | Firmware 1.11+    | Yes  |
| P40 / P40 Pro                | Yes           | No               | Yes               | Yes  |

Support depends on the full product code, firmware and wallbox configuration.

## Thing Configuration

Set `ipAddress` to the wallbox address or hostname.
For a Modbus proxy, set `modbusIpAddress`; UDP and REST still connect directly to the wallbox.

| Group      | Parameter             | Default     | Description                                                           |
|------------|-----------------------|-------------|-----------------------------------------------------------------------|
| Common     | `ipAddress`           | -           | Wallbox address used by UDP and REST, and by Modbus unless overridden |
| Common     | `refreshInterval`     | 12 s        | Fast polling interval; minimum 10 s                                   |
| Common     | `refreshIntervalSlow` | 60 s        | Slow and supplemental polling interval; minimum 10 s                  |
| Modbus TCP | `modbusEnabled`       | true        | Enable Modbus TCP in the binding                                      |
| Modbus TCP | `modbusIpAddress`     | `ipAddress` | Optional Modbus proxy address                                         |
| Modbus TCP | `port`                | 502         | Modbus TCP port                                                       |
| Modbus TCP | `unitId`              | 255         | Modbus unit ID                                                        |
| UDP        | `udpEnabled`          | true        | Enable UDP                                                            |
| UDP        | `udpDisplayOnly`      | false       | Use UDP only for display commands                                     |
| REST       | `restEnabled`         | false       | Enable authenticated REST                                             |
| REST       | `restPort`            | 8443        | REST HTTPS port                                                       |
| REST       | `username`            | admin       | REST username                                                         |
| REST       | `password`            | -           | Required when REST is enabled                                         |
| REST       | `verifyCertificate`   | true        | Verify the REST certificate and hostname                              |

The binding enables Modbus and UDP by default, but Modbus must also be enabled on the wallbox.
On P20 models with firmware 2.5 or later, disable Modbus and use UDP.
REST is optional and requires credentials.
Disable `verifyCertificate` only for a trusted local wallbox with a self-signed certificate.

For display commands without UDP measurements, set `udpEnabled=true` and `udpDisplayOnly=true`.
This mode keeps the `display` channel and `setDisplay` action available without UDP polling.
UDP must remain enabled for display commands; `udpDisplayOnly` disables cyclic reports, not commands and actions.
It does not provide UDP measurement fallback.
Shared polling intervals must still be at least 10 seconds.

## Availability and Polling

For shared values, the preferred source is Modbus, then REST, then UDP.
The binding falls back to another enabled protocol when the preferred source is unavailable.
`refreshInterval` controls primary operational data; `refreshIntervalSlow` controls slow and supplemental data.

The Thing stays online while an enabled protocol supplies operational data.
After UDP or REST has connected, three consecutive failed operational polls mark that protocol unavailable.
The next successful poll restores it.
Initial connection failures are reported immediately and retried after five minutes.
Items retain their last values during brief outages, so those values can be stale.
Commands are not automatically retried.

The Thing properties `modbusAvailable`, `udpAvailable` and `restAvailable` show protocol status.
Possible values include `unknown`, `available`, `unavailable`, `disabled` and `unsupported`.
`identification-only` means UDP report 1 identified the wallbox, but command access is unavailable; check that DIP switch 1.3 enables UDP.
`display-only` means UDP is configured for display commands without cyclic reports.

## Channels

Available channels depend on enabled protocols and the detected wallbox model.
Channels unsupported by the selected protocols or detected model are removed from the Thing.

The first table lists every declared channel; protocol tables list channels unique to that protocol.
Shared channels retain one ID and use protocol fallback where supported.

### All Channels

| Channel ID                | Item Type                  | Read-only | Description                                                                                                                                        |
|---------------------------|----------------------------|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| `state`                   | `Number`                   | Yes       | Current operational state of the wallbox, read from Modbus register 1000.                                                                          |
| `enableduser`             | `Switch`                   | No        | Enable or disable charging. Commands use Modbus; availability readback comes from REST or UDP when available.                                      |
| `wallbox`                 | `Switch`                   | Yes       | Whether the cable is plugged into the wallbox, derived from cable state when Modbus is available.                                                  |
| `vehicle`                 | `Switch`                   | Yes       | Whether a vehicle is connected, derived from cable state when Modbus is available.                                                                 |
| `locked`                  | `Switch`                   | Yes       | Whether the charging plug is locked, derived from cable state when Modbus is available.                                                            |
| `unlockplug`              | `Switch`                   | No        | Send ON to unlock the charging plug through Modbus, UDP, or REST. Charging must be stopped before unlocking.                                       |
| `errorcode`               | `Number`                   | Yes       | Raw numeric wallbox error code from Modbus register 1006; zero means no error. Its encoding differs from REST error names and UDP error words.     |
| `I1`                      | `Number:ElectricCurrent`   | Yes       | Actual current drawn by the vehicle on phase 1, in amperes; not a current limit or setpoint.                                                       |
| `I2`                      | `Number:ElectricCurrent`   | Yes       | Actual current drawn by the vehicle on phase 2, in amperes; not a current limit or setpoint.                                                       |
| `I3`                      | `Number:ElectricCurrent`   | Yes       | Actual current drawn by the vehicle on phase 3, in amperes; not a current limit or setpoint.                                                       |
| `maxsystemcurrent`        | `Number:ElectricCurrent`   | Yes       | Maximum current supported by the wallbox hardware, reported by Modbus or UDP. This is not the user setpoint or actual vehicle current.             |
| `maxpilotcurrent`         | `Number:ElectricCurrent`   | Yes       | Maximum current currently offered to the vehicle, reported by Modbus or UDP. The vehicle may draw less; I1, I2 and I3 report actual current.       |
| `U1`                      | `Number:ElectricPotential` | Yes       | Measured voltage of phase 1, in volts.                                                                                                             |
| `U2`                      | `Number:ElectricPotential` | Yes       | Measured voltage of phase 2, in volts.                                                                                                             |
| `U3`                      | `Number:ElectricPotential` | Yes       | Measured voltage of phase 3, in volts.                                                                                                             |
| `power`                   | `Number:Power`             | Yes       | Total measured active charging power, in watts.                                                                                                    |
| `powerfactor`             | `Number:Dimensionless`     | Yes       | Measured charging power factor (cos phi), expressed as a percentage.                                                                               |
| `totalconsumption`        | `Number:Energy`            | Yes       | Total energy reported by the wallbox meter, in watt-hours, accumulated after completed charging sessions.                                          |
| `sessionconsumption`      | `Number:Energy`            | Yes       | Energy delivered in the current charging session, in watt-hours.                                                                                   |
| `sessionrfidtag`          | `String`                   | Yes       | RFID tag for the last charging session, represented by the hexadecimal Modbus RFID register. Available only when RFID is enabled.                  |
| `setenergylimit`          | `Number:Energy`            | No        | Energy limit for an active or next charging session. Send an energy quantity, for example in watt-hours.                                           |
| `phaseswitchsource`       | `Number`                   | No        | Communication source allowed to control phase switching: disabled, OCPP, REST, Modbus or UDP.                                                      |
| `phaseswitchstate`        | `Number`                   | No        | Phase-switch state: 1 for single-phase or 3 for three-phase charging. Writable through Modbus or UDP; REST is read-only.                           |
| `failsafecurrent`         | `Number:ElectricCurrent`   | No        | Failsafe current in amperes. Modbus reads and writes it; UDP writes combine it with the timeout from report 2.                                     |
| `failsafetimeout`         | `Number:Time`              | No        | Timeout in seconds before failsafe current applies. Modbus reads and writes it; UDP writes combine it with failsafe current from report 2.         |
| `failsafepersist`         | `Switch`                   | No        | Send ON to save active failsafe settings permanently on a P30 wallbox through Modbus or UDP.                                                       |
| `fastchargingstatus`      | `Number`                   | Yes       | P40-only raw fast-charging status from Modbus register 1200. Removed after a P30 model is identified.                                              |
| `activatefastcharging`    | `Switch`                   | No        | Send ON to activate fast charging on P40. Removed after a P30 model is identified.                                                                 |
| `enabledsystem`           | `Switch`                   | Yes       | Wallbox activation state imposed by the system, reported by UDP Enable sys. This is not the writable user enable command.                          |
| `maxpresetcurrent`        | `Number:ElectricCurrent`   | No        | User-requested maximum charging current in amperes. Commands use Modbus when available, otherwise UDP; this is a setpoint, not measured current.   |
| `maxpresetcurrentrange`   | `Number:Dimensionless`     | No        | Percentage representation of `maxpresetcurrent`, mapped between 6 A and the hardware current limit. Both channels control the same UDP setpoint.   |
| `currtimer`               | `Number:ElectricCurrent`   | Yes       | Current that replaces the UDP user-current setpoint when `currtimertimeout` expires. This is the delayed `currtime` setting, not measured current. |
| `currtimertimeout`        | `Number:Time`              | Yes       | Remaining time in seconds before the delayed UDP user-current setpoint becomes active.                                                             |
| `maxpilotcurrentdutycyle` | `Number:Dimensionless`     | Yes       | Control-pilot PWM duty cycle, expressed as a percentage. It encodes offered current; it is not measured phase current.                             |
| `authreq`                 | `Switch`                   | Yes       | Whether authentication is required before a charging session can begin.                                                                            |
| `authenticate`            | `String`                   | No        | Authenticate and start a charging session using an RFID tag and class already accepted by the wallbox.                                             |
| `udpstop`                 | `String`                   | No        | Send a UDP stop command with the explicitly supplied 16-digit session RFID tag. No RFID report is read to construct the command.                   |
| `sessionid`               | `Number`                   | Yes       | Identifier of the last charging session from UDP report 100.                                                                                       |
| `sessionrfidclass`        | `String`                   | Yes       | RFID authorization class used for the last charging session, from UDP report 100.                                                                  |
| `input`                   | `Switch`                   | Yes       | State of the physical X1 enable input reported by UDP. This is separate from the REST X2 output state.                                             |
| `output`                  | `Switch`                   | No        | State of the physical X2 switched output. Send ON or OFF through UDP; X1 is the separate enable input.                                             |
| `display`                 | `String`                   | No        | Transient wallbox display text sent through UDP on supported models. `setDisplay` also supports display-duration limits.                           |
| `error1`                  | `String`                   | Yes       | Raw UDP Error1 diagnostic value. Interpret with Error2; this is not the Modbus numeric error code.                                                 |
| `error2`                  | `String`                   | Yes       | Raw UDP Error2 diagnostic value. Interpret together with Error1 using KEBA error documentation.                                                    |
| `backend`                 | `Switch`                   | Yes       | Whether communication with the charging backend is present.                                                                                        |
| `uptime`                  | `Number:Time`              | Yes       | System uptime since the wallbox was last reset, in seconds.                                                                                        |
| `timequality`             | `Number`                   | Yes       | Wallbox clock synchronization quality reported by UDP `timeQ`, from 0 to 3.                                                                        |
| `bootflag`                | `Number`                   | Yes       | Raw UDP `setBoot` diagnostic value; its semantics are not documented in the UDP Programmer's Guide.                                                |
| `dipswitch1`              | `String`                   | Yes       | Raw hexadecimal value of DIP-switch block 1, including the UDP enable bit.                                                                         |
| `dipswitch2`              | `String`                   | Yes       | Raw hexadecimal value of DIP-switch block 2.                                                                                                       |
| `reststate`               | `String`                   | Yes       | Textual operational state from REST, for example CHARGING or READY_FOR_CHARGING; retained alongside the numeric state.                             |
| `error`                   | `String`                   | Yes       | Wallbox error-code name from REST, for example TEMPERATURE_DERATING. It is not numerically equivalent to the Modbus error code.                    |
| `reserved`                | `Switch`                   | Yes       | Whether the wallbox has an active reservation, reported by REST.                                                                                   |
| `restoutput`              | `Switch`                   | Yes       | Whether X2 is active according to REST. This is separate from UDP X1 input and the UDP output command.                                             |
| `temperature`             | `Number:Temperature`       | Yes       | Internal wallbox temperature from REST meter data, in degrees Celsius.                                                                             |
| `externalmeter`           | `Switch`                   | Yes       | Whether REST reports an external meter; this is not an external-meter energy reading.                                                              |
| `session`                 | `Switch`                   | Yes       | Whether a charging session is active, reported by REST.                                                                                            |
| `sessionstart`            | `DateTime`                 | Yes       | Start time of the latest charging session returned by the REST session endpoint.                                                                   |
| `sessionduration`         | `Number:Time`              | Yes       | Duration of the latest charging session, in seconds, from the REST session endpoint.                                                               |
| `start`                   | `Switch`                   | No        | Send ON to request that REST starts a charging session.                                                                                            |
| `stop`                    | `Switch`                   | No        | Send ON to request that REST stops the active charging session.                                                                                    |
| `togglephaseswitch`       | `Switch`                   | No        | Send ON to toggle single- and three-phase charging through REST. OFF is ignored; the channel resets after success.                                 |
| `authon`                  | `Switch`                   | Yes       | Whether charging authorization is enabled, reported by REST when available.                                                                        |
| `permanentlylocked`       | `Switch`                   | No        | Enable or disable permanent plug locking through REST. This is a locking policy, not the current physical lock state.                              |
| `maxphases`               | `Number`                   | Yes       | Maximum number of charging phases supported by the wallbox, reported by REST.                                                                      |
| `phaseconfiguration`      | `String`                   | Yes       | Configured phase order reported by REST, rather than just the number of active phases.                                                             |
| `dipswitchsettings`       | `String`                   | Yes       | Raw JSON array of DIP-switch settings reported by REST. Null entries are preserved.                                                                |
| `dipswitchinterpretation` | `String`                   | Yes       | Named DIP-switch settings returned by the REST interpretation endpoint as a JSON object.                                                           |
| `reboot`                  | `Switch`                   | No        | Send ON to request a wallbox reboot through REST.                                                                                                  |

### UDP-Specific Channels

These channels are unique to UDP; availability still depends on the model and UDP mode.

| Channel ID                | Item Type                | Read-only | Description                                                                                 |
|---------------------------|--------------------------|-----------|---------------------------------------------------------------------------------------------|
| `backend`                 | `Switch`                 | Yes       | Whether communication with the charging backend is present.                                 |
| `timequality`             | `Number`                 | Yes       | Wallbox clock synchronization quality, from 0 to 3.                                         |
| `bootflag`                | `Number`                 | Yes       | Raw `setBoot` diagnostic value; semantics are not documented in the UDP Programmer's Guide. |
| `dipswitch1`              | `String`                 | Yes       | Raw hexadecimal value of DIP-switch block 1, including the UDP enable bit.                  |
| `dipswitch2`              | `String`                 | Yes       | Raw hexadecimal value of DIP-switch block 2.                                                |
| `enabledsystem`           | `Switch`                 | Yes       | Wallbox activation state imposed by the system.                                             |
| `maxpresetcurrentrange`   | `Number:Dimensionless`   | No        | User-current setpoint as a percentage between 6 A and the hardware current limit.           |
| `error1`                  | `String`                 | Yes       | Raw UDP Error1 diagnostic value; interpret with Error2.                                     |
| `error2`                  | `String`                 | Yes       | Raw UDP Error2 diagnostic value; interpret with Error1 using KEBA error documentation.      |
| `maxpilotcurrentdutycyle` | `Number:Dimensionless`   | Yes       | Control-pilot PWM duty cycle, expressed as a percentage.                                    |
| `currtimer`               | `Number:ElectricCurrent` | Yes       | Delayed user-current setpoint that becomes active when `currtimertimeout` expires.          |
| `currtimertimeout`        | `Number:Time`            | Yes       | Remaining time before the delayed user-current setpoint becomes active, in seconds.         |
| `input`                   | `Switch`                 | Yes       | State of the physical X1 enable input.                                                      |
| `output`                  | `Switch`                 | No        | Control the physical X2 switched output by sending ON or OFF.                               |
| `uptime`                  | `Number:Time`            | Yes       | System uptime since the wallbox was last reset, in seconds.                                 |
| `authreq`                 | `Switch`                 | Yes       | Whether authentication is required before charging can begin.                               |
| `sessionrfidclass`        | `String`                 | Yes       | RFID authorization class used for the last session, from UDP report 100.                    |
| `sessionid`               | `Number`                 | Yes       | Identifier of the last charging session from UDP report 100.                                |
| `display`                 | `String`                 | No        | Send transient display text through UDP on supported models.                                |
| `authenticate`            | `String`                 | No        | Authenticate and start a session with an accepted RFID tag and class.                       |
| `udpstop`                 | `String`                 | No        | Stop a session using the explicitly supplied 16-digit RFID tag.                             |

### Modbus TCP-Specific Channels

These channels are unique to Modbus TCP; the fast-charging channels are available only on P40.

| Channel ID             | Item Type | Read-only | Description                                                                    |
|------------------------|-----------|-----------|--------------------------------------------------------------------------------|
| `errorcode`            | `Number`  | Yes       | Raw numeric wallbox error code from Modbus register 1006; zero means no error. |
| `fastchargingstatus`   | `Number`  | Yes       | P40-only raw fast-charging status from Modbus register 1200.                   |
| `activatefastcharging` | `Switch`  | No        | Send ON to activate fast charging on P40.                                      |

### REST-Specific Channels

These channels are unique to REST and require a supported model and successful authenticated connection.

| Channel ID                | Item Type            | Read-only | Description                                                                                     |
|---------------------------|----------------------|-----------|-------------------------------------------------------------------------------------------------|
| `reststate`               | `String`             | Yes       | Textual REST operational state, for example CHARGING or READY_FOR_CHARGING.                     |
| `session`                 | `Switch`             | Yes       | Whether a charging session is active.                                                           |
| `error`                   | `String`             | Yes       | REST error-code name, complementary to but not numerically equivalent to the Modbus error code. |
| `reserved`                | `Switch`             | Yes       | Whether the wallbox has an active reservation.                                                  |
| `temperature`             | `Number:Temperature` | Yes       | Internal wallbox temperature in degrees Celsius.                                                |
| `restoutput`              | `Switch`             | Yes       | Whether X2 is active according to REST.                                                         |
| `sessionstart`            | `DateTime`           | Yes       | Start time of the latest charging session.                                                      |
| `sessionduration`         | `Number:Time`        | Yes       | Duration of the latest charging session, in seconds.                                            |
| `externalmeter`           | `Switch`             | Yes       | Whether REST reports an external meter.                                                         |
| `maxphases`               | `Number`             | Yes       | Maximum number of charging phases supported by the wallbox.                                     |
| `phaseconfiguration`      | `String`             | Yes       | Configured phase order reported by REST.                                                        |
| `dipswitchsettings`       | `String`             | Yes       | Raw JSON array of DIP-switch settings; null entries are preserved.                              |
| `dipswitchinterpretation` | `String`             | Yes       | Named DIP-switch settings returned as a JSON object.                                            |
| `permanentlylocked`       | `Switch`             | No        | Enable or disable permanent plug-locking policy.                                                |
| `start`                   | `Switch`             | No        | Send ON to request that REST starts a charging session.                                         |
| `stop`                    | `Switch`             | No        | Send ON to request that REST stops the active charging session.                                 |
| `togglephaseswitch`       | `Switch`             | No        | Send ON to toggle between single- and three-phase charging; resets after success.               |
| `reboot`                  | `Switch`             | No        | Send ON to request a wallbox reboot through REST.                                               |

#### REST State and Error Values

The REST `reststate` channel reports one of these values:

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

The REST `error` channel returns an error-code name.
The OpenAPI specification lists these values but does not define their detailed causes:

`TEMPERATURE_DERATING`, `POWER_CONTROL`, `ISO_COMMUNICATION_NOT_WORKING`, `PLUG_LOST`, `PLUG_UNDEFINED`,
`OVERHEAT`, `LOCK_FAILED`, `OHMIC_LOAD`, `POWER_MISMATCH`, `IBN_MODE`, `SHORTCIRCUIT`, `CONFIGURATION_ERROR`,
`HARDWARE_FAULT`, `VOLTAGE_OUT_OF_RANGE`, `OVERLOAD`, `PILOT_NOK`, `RCD`, `METERING_MALFUNCTION`,
`CONTACTOR_FB_NOK`, `MISSIN_CFG`, `WATCHDOG_TRIGGERED`, `TEMPERATURE_THRESHOLD`, `WRONG_CONTACTOR_SWITCHING`,
`FSM_STATE_CHANGE_FAILURE`, `UNKNOWN_STATE`.

`maxsystemcurrent` is the wallbox hardware limit; `maxpilotcurrent` is the current offered to the vehicle;
`maxpresetcurrent` is the writable user setpoint.

`phaseswitchstate` accepts 1 or 3 through Modbus or UDP and is read-only through REST.
Use REST's `togglephaseswitch` to toggle phases; send ON to toggle, and the channel resets after success.
`udpstop` is a String command that requires the session's 16-digit RFID tag.

## Rule Actions

Use `setDisplay` to send transient display text through UDP.
The REST API does not provide an equivalent transient display command.

The other actions require full-data UDP and are unavailable in display-only mode.

### `setDisplay` Parameters

| Parameter                  | Description                                                                                                                |
|----------------------------|----------------------------------------------------------------------------------------------------------------------------|
| `text`                     | Text to show on the display, up to 23 ASCII characters. `~` represents Σ, `$` inserts a blank, and `,` represents a comma. |
| `durationMin` _(optional)_ | Minimum display time in seconds before another display command can replace the text.                                       |
| `durationMax` _(optional)_ | Maximum display time in seconds if no further display command arrives.                                                     |

### `setChargingCurrent` Parameters

| Parameter      | Description                                                                                                            |
|----------------|------------------------------------------------------------------------------------------------------------------------|
| `current`      | Charging-current setpoint in milliamps: `0` to stop charging, or 6.000 mA up to the wallbox limit (maximum 63.000 mA). |
| `delaySeconds` | Non-negative activation delay in seconds before applying the current setpoint; this is not a duration.                 |

### `setFailsafe` Parameters

| Parameter                | Description                                                                                                          |
|--------------------------|----------------------------------------------------------------------------------------------------------------------|
| `current`                | Failsafe current in milliamps: `0`, or 6.000–63.000 mA.                                                              |
| `timeoutSeconds`         | Timeout in seconds: `0` or 5–600.                                                                                    |
| `persist`                | `true` saves the supplied failsafe current and timeout permanently; `false` applies them without saving permanently. |

## Example

demo.Actions:

```java
rule "Set Display Text"
when
  System reached start level 100
then
  val keContactActions = getActions("keba", "keba:kecontact:1")
  keContactActions.setDisplay("TEXT$1")           // Default duration
  keContactActions.setDisplay("TEXT$2", 5, 10)    // Explicit duration set
  keContactActions.setChargingCurrent(16000, 10)  // 16 A in 10 sec
  keContactActions.setFailsafe(16000, 60, true)   // 16 A after 60 sec timeout, persistent
end
```

demo.Things:

```java
Thing keba:kecontact:combined    [ipAddress="192.168.0.60", modbusEnabled=true,  udpEnabled=true, restEnabled=true, username="admin", password="secret"]
Thing keba:kecontact:restdisplay [ipAddress="192.168.0.61", modbusEnabled=false, udpEnabled=true, restEnabled=true, username="admin", password="secret", udpDisplayOnly=true]   // data via rest + display action
Thing keba:kecontact:p20         [ipAddress="192.168.0.62", modbusEnabled=false, udpEnabled=true]
Thing keba:kecontact:proxied     [ipAddress="192.168.0.63", modbusEnabled=true,  udpEnabled=true, restEnabled=false, modbusIpAddress="192.168.0.20", unitId=255]  // UDP via *.63, Modbus via *.20
```

demo.Items:

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
Number:Time               KebaUptime            "Uptime [%.0f s]"                         {channel="keba:kecontact:combined:uptime"}
Number:ElectricCurrent    KebaI1                                                          {channel="keba:kecontact:combined:I1"}
Number:ElectricCurrent    KebaI2                                                          {channel="keba:kecontact:combined:I2"}
Number:ElectricCurrent    KebaI3                                                          {channel="keba:kecontact:combined:I3"}
Number:ElectricPotential  KebaU1                                                          {channel="keba:kecontact:combined:U1"}
Number:ElectricPotential  KebaU2                                                          {channel="keba:kecontact:combined:U2"}
Number:ElectricPotential  KebaU3                                                          {channel="keba:kecontact:combined:U3"}
Number:Power              KebaPower             "Charging power [%.1f W]"                 {channel="keba:kecontact:combined:power"}
Number:Energy             KebaSessionEnergy                                               {channel="keba:kecontact:combined:sessionconsumption"}
Number:Energy             KebaTotalEnergy       "Energy during all sessions [%.1f Wh]"    {channel="keba:kecontact:combined:totalconsumption"}
Switch                    KebaInputSwitch                                                 {channel="keba:kecontact:combined:input"}
Switch                    KebaOutputSwitch                                                {channel="keba:kecontact:combined:output"}
Number:Energy             KebaSetEnergyLimit    "Set charge energy limit [%.1f Wh]"       {channel="keba:kecontact:combined:setenergylimit"}
```

demo.Sitemap:

```perl
sitemap demo label="Main Menu"
{
  Text label="Charging Station"
  {
    Text     item=KebaState
    Text     item=KebaUptime
    Switch   item=KebaEnabledSystem
    Switch   item=KebaEnabledUser
    Switch   item=KebaWallboxPlugged
    Switch   item=KebaVehiclePlugged
    Switch   item=KebaPlugLocked
    Slider   item=KebaCurrentRange
    Text     item=KebaCurrent
    Text     item=KebaSystemCurrent
    Text     item=KebaFailSafeCurrent
    Text     item=KebaSessionEnergy
    Text     item=KebaTotalEnergy
    Setpoint item=KebaSetEnergyLimit
  }
}
```

## Troubleshooting

Enable `DEBUG` logging for `org.openhab.binding.keba`.
For Modbus requests, also enable `DEBUG` logging for `org.openhab.core.io.transport.modbus`.
UDP uses port 7090 on both the wallbox and openHAB host.
On wallboxes equipped with DIP switches, switch 1.3 (`0x20` in `DIP-Sw1`) enables UDP command access.
Consult the model's installation manual for the switch's default setting and location.
After changing DIP switches, power-cycle the station; a software reset may not apply the new configuration.
The KeContact P40 has no DIP switches; enable Modbus TCP and configure its port and Unit ID via the KEBA eMobility App, OCPP or REST API.
The default Modbus TCP port is `502` (configurable via the `port` parameter); the default Unit ID is `255` (configurable via the `unitId` parameter).

The REST API uses HTTPS and was tested with P30 x-series firmware 2.1.0 and KeMove API v2.5.0.
Check REST credentials with the KEBA eMobility App and verify `https://<ipAddress>:<restPort>` reaches the wallbox.
