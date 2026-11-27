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

The table lists each channel once; `All` means Modbus TCP, UDP and REST.
Protocol support remains subject to the model and configured mode; REST requires a successful authenticated connection.
Shared channels retain one ID and use protocol fallback where supported.

### All Channels

| Channel ID                | Item Type                  | Read-only | Supported Protocols | Description                                                                                                                                    |
|---------------------------|----------------------------|-----------|---------------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| `state`                   | `Number`                   | Yes       | All                 | Current numeric operational state of the wallbox.                                                                                              |
| `reststate`               | `String`                   | Yes       | REST                | Textual operational state from REST, for example `CHARGING` or `READY_FOR_CHARGING`; retained alongside the numeric state.                     |
| `enabledsystem`           | `Switch`                   | Yes       | UDP                 | Wallbox activation state imposed by the system. This is not the writable user enable command.                                                  |
| `enableduser`             | `Switch`                   | No        | All                 | Enable or disable charging.                                                                                                                    |
| `reserved`                | `Switch`                   | Yes       | REST                | Whether the wallbox has an active reservation.                                                                                                 |
| `fastchargingstatus`      | `Number`                   | Yes       | Modbus TCP          | Raw fast-charging status. Available only on P40.                                                                                               |
| `activatefastcharging`    | `Switch`                   | No        | Modbus TCP          | Send `ON` to activate fast charging. Available only on P40.                                                                                    |
| `wallbox`                 | `Switch`                   | Yes       | Modbus TCP, UDP     | Whether the cable is plugged into the wallbox.                                                                                                 |
| `vehicle`                 | `Switch`                   | Yes       | All                 | Whether a vehicle is connected.                                                                                                                |
| `locked`                  | `Switch`                   | Yes       | Modbus TCP, UDP     | Whether the charging plug is locked.                                                                                                           |
| `unlockplug`              | `Switch`                   | No        | All                 | Send `ON` to unlock the charging plug. Charging must be stopped before unlocking.                                                              |
| `permanentlylocked`       | `Switch`                   | No        | REST                | Enable or disable permanent plug locking. This is a locking policy, not the current physical lock state.                                       |
| `maxsystemcurrent`        | `Number:ElectricCurrent`   | Yes       | All                 | Maximum current supported by the wallbox hardware. This is not the user setpoint or actual vehicle current.                                    |
| `maxpilotcurrent`         | `Number:ElectricCurrent`   | Yes       | All                 | Maximum current currently offered to the vehicle. The vehicle may draw less; I1, I2 and I3 report actual current.                              |
| `maxpresetcurrent`        | `Number:ElectricCurrent`   | No        | Modbus TCP, UDP     | User-requested maximum charging current in amperes. This is a setpoint, not measured current.                                                  |
| `maxpresetcurrentrange`   | `Number:Dimensionless`     | No        | UDP                 | Percentage representation of `maxpresetcurrent`, mapped between `6 A` and the hardware current limit. Both channels control the same setpoint. |
| `maxpilotcurrentdutycyle` | `Number:Dimensionless`     | Yes       | UDP                 | Control-pilot PWM duty cycle, expressed as a percentage. It encodes offered current; it is not measured phase current.                         |
| `currtimer`               | `Number:ElectricCurrent`   | Yes       | UDP                 | Delayed charging-current setpoint that takes effect when `currtimertimeout` expires. This is not measured current.                             |
| `currtimertimeout`        | `Number:Time`              | Yes       | UDP                 | Remaining time in seconds before the delayed charging-current setpoint becomes active.                                                         |
| `I1`                      | `Number:ElectricCurrent`   | Yes       | All                 | Actual current drawn by the vehicle on phase 1, in amperes; not a current limit or setpoint.                                                   |
| `I2`                      | `Number:ElectricCurrent`   | Yes       | All                 | Actual current drawn by the vehicle on phase 2, in amperes; not a current limit or setpoint.                                                   |
| `I3`                      | `Number:ElectricCurrent`   | Yes       | All                 | Actual current drawn by the vehicle on phase 3, in amperes; not a current limit or setpoint.                                                   |
| `U1`                      | `Number:ElectricPotential` | Yes       | All                 | Measured voltage of phase 1, in volts.                                                                                                         |
| `U2`                      | `Number:ElectricPotential` | Yes       | All                 | Measured voltage of phase 2, in volts.                                                                                                         |
| `U3`                      | `Number:ElectricPotential` | Yes       | All                 | Measured voltage of phase 3, in volts.                                                                                                         |
| `power`                   | `Number:Power`             | Yes       | All                 | Total measured active charging power, in watts.                                                                                                |
| `powerfactor`             | `Number:Dimensionless`     | Yes       | All                 | Measured charging power factor (cos phi), expressed as a percentage.                                                                           |
| `totalconsumption`        | `Number:Energy`            | Yes       | All                 | Total energy reported by the wallbox meter, in watt-hours, accumulated after completed charging sessions.                                      |
| `externalmeter`           | `Switch`                   | Yes       | REST                | Whether an external meter is present; this is not an external-meter energy reading.                                                            |
| `temperature`             | `Number:Temperature`       | Yes       | REST                | Internal wallbox temperature, in degrees Celsius.                                                                                              |
| `phaseswitchsource`       | `Number`                   | No        | All                 | Communication source allowed to control phase switching: disabled, OCPP, REST, Modbus or UDP.                                                  |
| `phaseswitchstate`        | `Number`                   | No        | All                 | Phase-switch state: `0` deactivated, `1` single-phase or `3` three-phase charging. Commands require the selected `phaseswitchsource`.          |
| `togglephaseswitch`       | `Switch`                   | No        | REST                | Send `ON` to toggle single- and three-phase charging through REST. `OFF` is ignored; the channel resets after success.                         |
| `maxphases`               | `Number`                   | Yes       | REST                | Maximum number of charging phases supported by the wallbox.                                                                                    |
| `phaseconfiguration`      | `String`                   | Yes       | REST                | Configured phase order, rather than just the number of active phases.                                                                          |
| `failsafecurrent`         | `Number:ElectricCurrent`   | No        | Modbus TCP, UDP     | Charging-current limit applied after the communication timeout, in amperes.                                                                    |
| `failsafetimeout`         | `Number:Time`              | No        | Modbus TCP, UDP     | Communication timeout in seconds before failsafe current applies. This is not the delayed current-setpoint timer.                              |
| `failsafepersist`         | `Switch`                   | No        | Modbus TCP, UDP     | Send `ON` to save active failsafe settings permanently on a P30 wallbox.                                                                       |
| `session`                 | `Switch`                   | Yes       | UDP, REST           | Whether a charging session is active.                                                                                                          |
| `sessionid`               | `Number`                   | Yes       | UDP, REST           | Identifier of the current or latest charging session, retained after completion.                                                               |
| `sessionstart`            | `DateTime`                 | Yes       | UDP, REST           | Start time of the current or latest charging session, retained after completion. Requires a valid absolute timestamp.                          |
| `sessionduration`         | `Number:Time`              | Yes       | UDP, REST           | Elapsed or final duration of the current or latest charging session, in seconds, retained after completion.                                    |
| `sessionconsumption`      | `Number:Energy`            | Yes       | All                 | Energy delivered in the current charging session, in watt-hours.                                                                               |
| `sessionrfidtag`          | `String`                   | Yes       | Modbus TCP, UDP     | Hexadecimal RFID tag used for the last charging session. Available only when RFID is enabled.                                                  |
| `sessionrfidclass`        | `String`                   | Yes       | UDP                 | RFID authorization class used for the last charging session.                                                                                   |
| `setenergylimit`          | `Number:Energy`            | No        | Modbus TCP, UDP     | Energy limit for an active or next charging session. Send an energy quantity, for example in watt-hours.                                       |
| `start`                   | `Switch`                   | No        | REST                | Send `ON` to request that REST starts a charging session.                                                                                      |
| `stop`                    | `Switch`                   | No        | Modbus TCP, REST    | Send `ON` to stop charging: Modbus TCP sets the charging current to `0` mA, otherwise REST stops the session.                                  |
| `udpstop`                 | `String`                   | No        | UDP                 | Send a UDP stop command with the explicitly supplied 16-digit session RFID tag.                                                                |
| `authreq`                 | `Switch`                   | Yes       | UDP                 | Whether authentication is required before a charging session can begin.                                                                        |
| `authon`                  | `Switch`                   | Yes       | UDP, REST           | Whether charging authorization is enabled.                                                                                                     |
| `authenticate`            | `String`                   | No        | UDP                 | Authenticate and start a charging session using an RFID tag and class already accepted by the wallbox.                                         |
| `errorcode`               | `Number`                   | Yes       | Modbus TCP          | Numeric wallbox error code; zero means no error. Its encoding differs from REST error names and UDP error words.                               |
| `error`                   | `String`                   | Yes       | REST                | Wallbox error-code name from REST, for example `TEMPERATURE_DERATING`. It is not numerically equivalent to the Modbus error code.              |
| `error1`                  | `String`                   | Yes       | UDP                 | Raw UDP Error1 diagnostic value. Interpret together with Error2 using KEBA error documentation.                                                |
| `error2`                  | `String`                   | Yes       | UDP                 | Raw UDP Error2 diagnostic value. Interpret together with Error1 using KEBA error documentation.                                                |
| `input`                   | `Switch`                   | Yes       | UDP                 | State of the physical X1 enable input.                                                                                                         |
| `output`                  | `Number`                   | No        | UDP, REST           | X2 output: `0` opens, `1` closes, and `>=10` configures pulses per kWh. REST provides readback only. Commands require DSW1.2 `OFF`.            |
| `display`                 | `String`                   | No        | UDP                 | Transient wallbox display text on supported models. `setDisplay` also supports display-duration limits.                                        |
| `backend`                 | `Switch`                   | Yes       | UDP                 | Whether communication with the charging backend is present.                                                                                    |
| `uptime`                  | `Number:Time`              | Yes       | UDP                 | System uptime since the wallbox was last reset, in seconds.                                                                                    |
| `timequality`             | `Number`                   | Yes       | UDP                 | Wallbox clock synchronization quality, from `0` to `3`.                                                                                        |
| `bootflag`                | `Number`                   | Yes       | UDP                 | Raw boot diagnostic value; its meaning is not documented by KEBA.                                                                              |
| `dipswitch1`              | `String`                   | Yes       | UDP                 | Raw hexadecimal value of DIP-switch block 1, including the UDP enable bit.                                                                     |
| `dipswitch2`              | `String`                   | Yes       | UDP                 | Raw hexadecimal value of DIP-switch block 2.                                                                                                   |
| `dipswitchsettings`       | `String`                   | Yes       | REST                | Raw DIP-switch settings as a JSON array. Null entries are preserved.                                                                           |
| `dipswitchinterpretation` | `String`                   | Yes       | REST                | Named DIP-switch settings as a JSON object.                                                                                                    |
| `reboot`                  | `Switch`                   | No        | REST                | Send `ON` to request a wallbox reboot.                                                                                                         |

### REST State and Error Values

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

`stop` prefers Modbus TCP and writes `0` mA to register 5004, which suspends the charging session.
The wallbox keeps this setpoint until a new charging current of at least `6000 mA` is sent, for example via `maxpresetcurrent` or the `setChargingCurrent` action.
Without Modbus TCP, `stop` uses the REST `stop-charging` request.

`phaseswitchstate` accepts `1` or `3` through REST, Modbus or UDP.
Commands follow the reported `phaseswitchsource`: `2` uses REST, `3` uses Modbus and `4` uses UDP, without protocol fallback.
Commands are ignored while the source is unknown, disabled (`0`), OCPP (`1`), or its selected protocol is unavailable.
The state reads `0` while phase switching is deactivated (`phaseswitchsource` `0`); otherwise it reports the active phase count.
For REST, the binding sends `POST /v2/wallboxes/{serialNumber}/phase-toggle?numberOfPhases=1` or `numberOfPhases=3`.
The phase count is read back during polling rather than assumed after a command.
KEBA requires a five-minute cooldown between UDP phase changes.

The UDP `output` command requires DSW1.2 to be OFF on wallboxes with that DIP switch.
The binding accepts `ON`/`OFF` as aliases for `1`/`0`, and integer values `>=10` configure pulses per kWh.
KEBA recommends pulse settings up to `150`; pulse configuration is stored in EEPROM, so avoid repeated writes.
The `output` channel uses a Number Item; replace existing Switch Items linked to it with Number Items.
REST `x2active` supplies the preferred readback: `false` becomes `0` and `true` becomes `1`.
When REST is unavailable, the channel falls back to the numeric UDP `Output` readback.
REST reports only the X2 switch status, not the pulse setting or phase count; commands always use UDP.
With REST alone, `output` provides readback only.

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

| Parameter      | Description                                                                                                              |
|----------------|--------------------------------------------------------------------------------------------------------------------------|
| `current`      | Charging-current setpoint in milliamps: `0` to stop charging, or `6000 mA` up to the wallbox limit (maximum `63000 mA`). |
| `delaySeconds` | Non-negative activation delay in seconds before applying the current setpoint; this is not a duration.                   |

### `setFailsafe` Parameters

| Parameter                | Description                                                                                                          |
|--------------------------|----------------------------------------------------------------------------------------------------------------------|
| `current`                | Failsafe current in milliamps: `0`, or `6000`–`63000 mA`.                                                            |
| `timeoutSeconds`         | Timeout in seconds: `0` or `5`–`600 s`.                                                                              |
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
Number                    KebaOutput                                                      {channel="keba:kecontact:combined:output"}
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
UDP uses port `7090` on both the wallbox and openHAB host.
On wallboxes equipped with DIP switches, switch 1.3 (`0x20` in `DIP-Sw1`) enables UDP command access.
Consult the model's installation manual for the switch's default setting and location.
After changing DIP switches, power-cycle the station; a software reset may not apply the new configuration.
The KeContact P40 has no DIP switches; enable Modbus TCP and configure its port and Unit ID via the KEBA eMobility App, OCPP or REST API.
The default Modbus TCP port is `502` (configurable via the `port` parameter); the default Unit ID is `255` (configurable via the `unitId` parameter).
The wallbox serves only one Modbus TCP client at a time, so an EMS cannot poll it in parallel with openHAB.
Use a Modbus TCP proxy in that case and set `modbusIpAddress` to the proxy, while UDP and REST keep using `ipAddress`.

The REST API uses HTTPS and was tested with P30 x-series firmware 2.1.0 and KeMove API v2.5.0.
For REST API access, authenticate with the wallbox's locally configured REST username and password.
KEBA eMobility Portal credentials are not valid for local REST API access.
Verify `https://<ipAddress>:<restPort>` reaches the wallbox.
