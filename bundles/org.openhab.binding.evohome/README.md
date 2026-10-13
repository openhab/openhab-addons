# evohome Binding

This binding integrates the Honeywell evohome system.
It uses your Honeywell Total Connect Comfort account to access your locations and heating zones.

## Supported Things

The binding supports the following things:

- evohome Account
- evotouch control display
- Heating zones

### evohome Account

This thing functions as the bridge between all the other things.
It contains your credentials and connects to the Honeywell web API.

### evotouch

This thing represents the central display controller.
It is used to view and change the current system mode, permanently or until an expiry, and to return the controller and all of its zones to their schedules.

### Heating zone

The heating zone thing represents the evohome heating zone.
It displays the current temperature, the temperature set point and the status of the set point.
It allows permanent and temporary target temperature overrides and cancellation of an override to resume the zone's schedule.
It also exposes the weekly heating program and its next scheduled switch point and target temperature.

## Discovery

After setting up the evohome account, the evotouch and heating zones available to your account will be discovered after a manual scan.

## Thing Configuration

Thing configuration is optional, it is easier to use discovery which will automatically add all your zones and displays to the inbox, once the account Thing is online.

### Account

| Name            | Required | Description                                            |
|-----------------|----------|--------------------------------------------------------|
| username        | yes      | The username of your TCC account                       |
| password        | yes      | The password of your TCC account                       |
| refreshInterval | no       | The amount of time in seconds between updates (15-3000, default 15) |
| scheduleRefreshInterval | no | Cache interval for linked zone schedules, in minutes (1-1440, default 60). Separate from status polling. |

The interval is measured from the completion of the previous request.
Polling retrieves the latest values held by TCC; it does not force the physical sensors to take a new measurement.
After a failed login or status request, the account goes OFFLINE and retries automatically with increasing delays, up to 15 minutes (or the configured interval if longer).
Server-requested retry delays are also respected, including for commands and explicit refreshes.
Successful set point and system mode commands schedule a status read after two seconds.
Sending `REFRESH` requests a cloud update; it does not republish cached measurements and does not bypass failure backoff.
For schedule channels, `REFRESH` queues a fresh program read for that zone.

### Display &amp; Zone

| Name | Required | Description                                                                            |
|------|----------|----------------------------------------------------------------------------------------|
| id   | yes      | The id which can be found by auto-discovery |
| name | no       | A friendly name for use in the UI                                                      |

### Zone

| Name                      | Required | Description |
|---------------------------|----------|-------------|
| temporaryOverrideDuration | no       | Duration for temporary commands, in minutes (0-1440, default 120). 0 ends the override at the next switch point in the zone's weekly program. A positive value sets a fixed duration. |

The duration applies to `TemporarySetPoint` commands and to selecting `TemporaryOverride` on `SetPointStatus`.
It does not change the expiry of an already active temporary override when its temperature is adjusted through `SetPoint`.
For an exact expiry or a duration chosen per command, use the rule actions below.

## Channels

### Account

| Channel Type ID       | Item Type | Description |
|-----------------------|-----------|-------------|
| lastSuccessfulUpdate  | DateTime  | Time of the last successful retrieval of the complete installation status from TCC. This is not the sensor measurement time. |

Failed requests leave `lastSuccessfulUpdate` and the last known zone values unchanged.
Check the account status and this timestamp when evaluating whether retained values are fresh.
An unavailable or missing temperature in a successful response is published as `UNDEF`; the zone's set point can still be updated.
Managed account Things receive the additional channel through openHAB's Thing update instructions.

### Display

| Channel Type ID | Item Type | Description                                                                                                        |
|-----------------|-----------|--------------------------------------------------------------------------------------------------------------------|
| SystemMode      | String    | Gets the system mode. Commands set a permanent mode: Auto, AutoWithEco, Away, DayOff, HeatingOff or Custom, according to the controller's supported modes. |
| SystemModeUntil | DateTime  | Read only. Expiry reported by TCC for a temporary system mode, or UNDEF when the mode is permanent or the expiry is unknown. |
| SystemModePermanent | Switch | Read only. ON when TCC reports a permanent mode, OFF for a temporary mode, or UNDEF when unknown. |
| Reset           | Switch    | Command only. Send ON to select Auto and cancel every heating zone and hot water override belonging to this controller. OFF has no effect. |

Use display rule actions to set a temporary system mode with a precise expiry or a duration.
Only modes advertised by the controller as supporting temporary operation are accepted.
The read-only channels describe the status returned by TCC, rather than the last requested state.
Selecting `Auto` changes the controller's system mode while preserving individual zone overrides.
Use `Reset` to resume all normal schedules, including zones with permanent overrides.
Reset affects only the selected controller, even when the account contains multiple controllers.

### Zone

| Channel Type ID | Item Type          | Description                                                                                                                            |
|-----------------|--------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| Temperature     | Number:Temperature | Allows for viewing the current actual temperature of the zone.                                                                         |
| SetPointStatus  | String             | Gets or selects FollowSchedule, PermanentOverride or TemporaryOverride. The override modes use the current target temperature. TemporaryOverride uses the configured duration. |
| SetPoint        | Number:Temperature | Gets or sets the target temperature. If a temporary override is active, its expiry is preserved. Otherwise a nonzero command creates a permanent override. Sending 0 cancels the override. |
| TemporarySetPoint | Number:Temperature | Shows the current target temperature. Commands create a temporary override using the configured duration. Read the resulting mode and expiry from SetPointStatus and SetPointUntil. |
| SetPointUntil   | DateTime           | Expiry of the active temporary override, or UNDEF when no temporary override is active. |
| WeeklySchedule  | String             | Read only. Complete weekly heating program as canonical JSON, with days ordered Monday to Sunday and switch points sorted by local time. Temperatures are in Celsius. |
| NextSwitchpoint | DateTime           | Read only. Next switch point in the normal weekly heating program, calculated in the location's time zone. |
| NextScheduledTemperature | Number:Temperature | Read only. Target temperature at NextSwitchpoint in the normal weekly program. |
| ScheduleLastUpdated | DateTime       | Read only. Time of the last successful retrieval of this zone's weekly program from TCC. |

Commands with temperature units are converted to Celsius.
The binding validates known temperature limits, supported override modes and duration limits reported by the zone.
Choose `PermanentOverride` on `SetPointStatus` or use `setPermanentSetPoint` to replace a temporary override with a permanent one.
Choose `FollowSchedule` to cancel only this zone's override.
The zone then follows its schedule under the current controller mode; for example, `HeatingOff` still keeps heating off.

By default, a temporary command expires after two hours.
When `temporaryOverrideDuration` is 0, the command reads the zone's weekly program to determine the next switch point in the location's time zone.
It does not modify the program.
If a valid next switch point cannot be determined, the command fails without replacing the current override.
`SetPointUntil` reports the expiry returned by TCC, rather than a locally assumed expiry.

Commands are acknowledged by TCC before the physical system necessarily applies them.
The binding requests fresh status after an accepted command; controller and radiator updates can take longer than this first status read.
Managed Things receive the additional channels through openHAB's Thing update instructions, preserving existing channel links.
The program lookup supports IANA time zone identifiers and common European Windows time zone identifiers returned by TCC.
An unknown time zone rejects the next-switchpoint command; explicit times and fixed durations remain available.

## Weekly Program and Next Switch Point

The binding reads a zone's program automatically when at least one of `WeeklySchedule`, `NextSwitchpoint`, `NextScheduledTemperature` or `ScheduleLastUpdated` is linked to an Item.
Sending `REFRESH` to any of those channels queues a program read even when no schedule channel is linked.
No additional rule action is needed, and these channels accept no program changes.

Programs are cached for `scheduleRefreshInterval` minutes, 60 by default.
At most one zone's program is fetched during each status poll.
Newly linked zones therefore populate over successive status polls.
The next switch point and its temperature are recalculated from cached data on each status update, without an additional program request.
After editing a program in the Evohome app or controller, send `REFRESH` to read the change before the cache interval expires:

```java
// Request a fresh program read for the zone. Provider backoff still applies.
DemoZoneWeeklySchedule.sendCommand(REFRESH)
```

`NextSwitchpoint` and `NextScheduledTemperature` describe the normal weekly program.
Eco, Away, HeatingOff and individual zone overrides can change the effective target temperature; no Eco offset or other mode adjustment is applied to the scheduled temperature.
Read `SetPoint` and `SetPointStatus` for the current effective target and override mode.

`WeeklySchedule` contains a `dailySchedules` array with seven entries, Monday through Sunday.
Each entry contains `dayOfWeek` and a `switchpoints` array; each switch point contains `timeOfDay` in local `HH:mm:ss` format and a numeric `heatSetpoint` in Celsius.
For example, a simple weekly program can be represented as:

```json
{
  "dailySchedules": [
    { "dayOfWeek": "Monday", "switchpoints": [ { "timeOfDay": "06:00:00", "heatSetpoint": 20.5 } ] },
    { "dayOfWeek": "Tuesday", "switchpoints": [ { "timeOfDay": "06:00:00", "heatSetpoint": 20.5 } ] },
    { "dayOfWeek": "Wednesday", "switchpoints": [ { "timeOfDay": "06:00:00", "heatSetpoint": 20.5 } ] },
    { "dayOfWeek": "Thursday", "switchpoints": [ { "timeOfDay": "06:00:00", "heatSetpoint": 20.5 } ] },
    { "dayOfWeek": "Friday", "switchpoints": [ { "timeOfDay": "06:00:00", "heatSetpoint": 20.5 } ] },
    { "dayOfWeek": "Saturday", "switchpoints": [ { "timeOfDay": "08:00:00", "heatSetpoint": 21.0 } ] },
    { "dayOfWeek": "Sunday", "switchpoints": [ { "timeOfDay": "08:00:00", "heatSetpoint": 21.0 } ] }
  ]
}
```

The published JSON uses the same structure in compact form and can be parsed by rules or transformations.
For an unknown location time zone, `WeeklySchedule` and `ScheduleLastUpdated` remain available, while both next-switch-point channels are `UNDEF`.
At daylight-saving transitions, calculations follow Java's time-zone rules: a nonexistent local time moves forward by the gap, and an ambiguous local time uses the earlier offset.
These are program calculations; they do not guarantee the physical controller's behaviour at a daylight-saving transition.

A failed optional program read retains the last successfully retrieved program and leaves `ScheduleLastUpdated` unchanged.
The regular status path continues, and a schedule-only failure does not put the account OFFLINE.
A provider `Retry-After` response can delay subsequent requests, including status polls.
Check `ScheduleLastUpdated` when deciding whether cached program data is fresh.

## Rule Actions

Heating zone and display Things expose actions under the `evohome` scope.
Each action returns `true` when TCC accepts the command and `false` when it cannot be sent or accepted.
An accepted command does not guarantee that the physical controller or radiators have already applied it.
Confirm the resulting mode, target temperature and expiry through the appropriate Thing channels.

### Heating Zone

Temperature arguments are numeric values in degrees Celsius.

| Action | Parameters | Effect |
|--------|------------|--------|
| setPermanentSetPoint | Double temperature | Sets a permanent override, including when a temporary override is active. |
| setTemporarySetPoint | Double temperature, ZonedDateTime until | Sets a temporary override with a precise expiry. The date must include a time zone or UTC offset and be in the future. |
| setTemporarySetPointFor | Double temperature, Integer minutes | Sets a temporary override for 1-1440 minutes, subject to the zone's supported duration. |
| setTemporarySetPointUntilNextSwitchpoint | Double temperature | Sets a temporary override until the next switch point in the zone's weekly program, regardless of the configured duration. |
| cancelSetPointOverride | none | Cancels this zone's override and resumes its schedule under the current system mode. |

For example, a Rules DSL rule can choose a temperature and expiry together:

```java
val zone = getActions("evohome", "evohome:heatingzone:your_account_alias:your_zone_alias")

// Until the next switch point in the zone's weekly program.
val accepted = zone.setTemporarySetPointUntilNextSwitchpoint(21.0)
```

Other commands using the same action instance:

```java
// End the override two hours from now.
zone.setTemporarySetPoint(21.0, now.plusHours(2))

// End the override after 90 minutes.
zone.setTemporarySetPointFor(21.0, 90)

// Replace any active override with a permanent one.
zone.setPermanentSetPoint(20.0)

// Resume only this zone's schedule.
zone.cancelSetPointOverride()
```

### Display

| Action | Parameters | Effect |
|--------|------------|--------|
| setSystemMode | String mode | Sets a permanent controller mode. Auto preserves individual zone overrides; AutoWithReset performs a full controller reset to schedule. |
| setTemporarySystemMode | String mode, ZonedDateTime until | Sets a temporary controller mode with a precise expiry. The date must include a time zone or UTC offset and be in the future. |
| setTemporarySystemModeFor | String mode, Integer minutes | Sets a temporary controller mode for a positive duration in minutes, subject to the controller's supported modes and limits. |

Temporary modes and permitted durations depend on the controller's advertised capabilities.
A mode that supports permanent operation may not support temporary operation.
The binding sends explicit expiries in UTC and reports TCC's returned expiry on `SystemModeUntil`.
Use that channel together with `SystemModePermanent` to confirm the resulting state.

For example, when the controller advertises temporary Eco operation:

```java
val display = getActions("evohome", "evohome:display:your_account_alias:your_display_alias")

// Select Eco until two hours from now.
val accepted = display.setTemporarySystemMode("AutoWithEco", now.plusHours(2))
```

Other commands using the same display action instance:

```java
// Select Eco for 120 minutes, if this duration is supported.
display.setTemporarySystemModeFor("AutoWithEco", 120)

// Return the controller to permanent Auto while preserving zone overrides.
display.setSystemMode("Auto")
```

These actions change the controller mode.
To cancel every zone and hot water override as well, send ON to the display's `Reset` channel.

## Diagnostic Logging

TRACE logging for `org.openhab.binding.evohome` shows the retrieved zone temperatures, availability, set points and modes,
the mapping of API IDs to Things, published channel values and reasons for skipped updates.
Each polling cycle includes location, controller and zone counts, elapsed time and the next scheduled request delay.
Command results, delayed status reads and failure retries are also logged.
These messages describe channel updates submitted by the binding; they do not confirm that an Item is linked to a channel.
Authentication tokens, passwords and raw response bodies are not logged.

## Full Example

### `demo.things` Example

```java
Bridge evohome:account:your_account_alias [ username="your_user_name", password="your_password" ]
{
    display your_display_alias  [ id="1234" ]
    heatingzone your_zone_alias [ id="5678" ]
}
```

### demo.items

```java
// evohome Display
String DemoMode                 { channel="evohome:display:your_account_alias:your_display_alias:SystemMode" }
DateTime DemoModeUntil          { channel="evohome:display:your_account_alias:your_display_alias:SystemModeUntil" }
Switch DemoModePermanent        { channel="evohome:display:your_account_alias:your_display_alias:SystemModePermanent" }
Switch DemoReset                { channel="evohome:display:your_account_alias:your_display_alias:Reset" }

// evohome Heatingzone
Number:Temperature DemoZoneTemperature  { channel="evohome:heatingzone:your_account_alias:your_zone_alias:Temperature" }
String DemoZoneSetPointStatus           { channel="evohome:heatingzone:your_account_alias:your_zone_alias:SetPointStatus" }
Number:Temperature DemoZoneSetPoint     { channel="evohome:heatingzone:your_account_alias:your_zone_alias:SetPoint" }
Number:Temperature DemoZoneTemporarySetPoint { channel="evohome:heatingzone:your_account_alias:your_zone_alias:TemporarySetPoint" }
DateTime DemoZoneSetPointUntil          { channel="evohome:heatingzone:your_account_alias:your_zone_alias:SetPointUntil" }
String DemoZoneWeeklySchedule           { channel="evohome:heatingzone:your_account_alias:your_zone_alias:WeeklySchedule" }
DateTime DemoZoneNextSwitchpoint        { channel="evohome:heatingzone:your_account_alias:your_zone_alias:NextSwitchpoint" }
Number:Temperature DemoZoneNextScheduledTemperature { channel="evohome:heatingzone:your_account_alias:your_zone_alias:NextScheduledTemperature" }
DateTime DemoZoneScheduleLastUpdated    { channel="evohome:heatingzone:your_account_alias:your_zone_alias:ScheduleLastUpdated" }
```

### demo.sitemap

```perl
sitemap evohome label="evohome Menu"
{
    Frame label="evohome display" {
        Selection label="[%s]" item=DemoMode mappings=[
          "Auto"="Normal",
          "AutoWithEco"="Eco",
          "Away"="Away",
          "DayOff"="Day Off",
          "HeatingOff"="Off",
          "Custom"="Custom"
        ]
        Text item=DemoModePermanent label="Permanent mode [%s]"
        Text item=DemoModeUntil label="Mode expiry [%1$tF %1$tT]"
        Switch item=DemoReset label="Reset all zones to schedule" mappings=[ON="Reset"]
    }

    Frame label="evohome heating zone" {
        Text     label="Temperature"      item=DemoZoneTemperature
        Selection label="Mode [%s]" item=DemoZoneSetPointStatus mappings=[
          "FollowSchedule"="Follow schedule",
          "PermanentOverride"="Permanent override",
          "TemporaryOverride"="Temporary override"
        ]
        Setpoint label="Zone set point"   item=DemoZoneSetPoint minValue=5 maxValue=35 step=0.5
        Text label="Override expiry [%1$tF %1$tT]" item=DemoZoneSetPointUntil
        Text label="Next program change [%1$tF %1$tT]" item=DemoZoneNextSwitchpoint
        Text label="Next program target [%.1f %unit%]" item=DemoZoneNextScheduledTemperature
        Text label="Program read [%1$tF %1$tT]" item=DemoZoneScheduleLastUpdated
    }
}
```
