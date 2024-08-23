# Gardena Binding

This is the binding for [Gardena smart system](https://www.gardena.com/smart).
This binding allows you to integrate, view and control Gardena smart system devices in the openHAB environment.

## Supported Things

Devices connected to Gardena smart system, currently:

| Thing type               | Name                                               |
|--------------------------|----------------------------------------------------|
| bridge                   | smart Gateway                                      |
| mower                    | smart SILENO(+), SILENO city, SILENO life Mower    |
| sensor                   | smart Sensor                                       |
| pump                     | smart Pressure Pump                                |
| power                    | smart Power Adapter                                |
| water_control            | smart Water Control                                |
| irrigation_control       | smart Irrigation Control                           |

## Discovery

An account must be specified, all things for an account are discovered automatically.

## Account Configuration

There are several settings for an account:

| Name                  | Required | Description                                                                                   |
|-----------------------|----------|-----------------------------------------------------------------------------------------------|
| **apiSecret**         | yes      | The Gardena smart system integration API secret                                               |
| **apiKey**            | yes      | The Gardena smart system integration API key                                                  |
| **connectionTimeout** | no       | The timeout in seconds for connections to Gardena smart system integration API (default = 10) |

### Obtaining your API Key

1. Goto <https://developer.husqvarnagroup.cloud/>, sign in using your GARDENA smart system account and accept the terms of use
1. Create and save a new application via the 'Create application' button. The Redirect URLs do not matter, you can enter what you want (e.g. <http://localhost:8080>)
1. Connect both _Authentication API_ and _GARDENA smart system API_ to your application via the 'Connect new API' button
1. Copy the application key to use with this binding as _apiKey_

## Examples

### Things

Minimal Thing configuration:

```java
Bridge gardena:account:home [ apiSecret="...", apiKey="..." ]
```

Configuration of multiple bridges:

```java
Bridge gardena:account:home1 [ apiSecret="...", apiKey="..." ]
Bridge gardena:account:home2 [ apiSecret="...", apiKey="..." ]
```

Once a connection to an account is established, connected Things are discovered automatically.

Alternatively, you can manually configure Things:

```java
Bridge gardena:account:home [ apiSecret="...", apiKey="..." ]
{
  Thing mower myMower [ deviceId="c81ad682-6e45-42ce-bed1-6b4eff5620c8" ]
  Thing water_control myWaterControl [ deviceId="c81ad682-6e45-42ce-bed1-6b4eff5620c8" ]
  Thing sensor mySensor [ deviceId="c81ad682-6e45-42ce-bed1-6b4eff5620c8" ]
  Thing pump myEPP [ deviceId="c81ad682-6e45-42ce-bed1-6b4eff5620c8" ]
  Thing power myPowerSocket [ deviceId="c81ad682-6e45-42ce-bed1-6b4eff5620c8" ]
  Thing irrigation_control myIrrigationControl [ deviceId="c81ad682-6e45-42ce-bed1-6b4eff5620c8" ]
}
```

### Items

In the items file, you can link items to channels of your Things:

```java
Number Mower_Battery_Level "Battery [%d %%]" {channel="gardena:mower:home:myMower:common#batteryLevel"}
```

### Sensor refresh

Sensor refresh commands are not yet supported by the Gardena smart system integration API.

### Example configuration

```java
// smart Water Control
String  WC_Valve_Activity                 "Valve Activity" { channel="gardena:water_control:home:myWateringComputer:valve#activity" }
Number:Time WC_Valve_Duration             "Last Watering Duration [%d min]" { channel="gardena:water_control:home:myWateringComputer:valve#duration" }

Number:Time WC_Valve_cmd_Duration         "Command Duration [%d min]" { channel="gardena:water_control:home:myWateringComputer:valve_commands#commandDuration" }
Switch  WC_Valve_cmd_OpenWithDuration     "Start Watering Timer" { channel="gardena:water_control:home:myWateringComputer:valve_commands#start_seconds_to_override" }
Switch  WC_Valve_cmd_CloseValve           "Stop Switch" { channel="gardena:water_control:home:myWateringComputer:valve_commands#stop_until_next_task" }

openhab:status WC_Valve_Duration // returns the duration of the last watering request if still active, or 0
openhab:status WC_Valve_Activity // returns the current valve activity  (CLOSED|MANUAL_WATERING|SCHEDULED_WATERING)
```

All channels are read-only, except the command group and the lastUpdate timestamp

```shell
openhab:send WC_Valve_cmd_Duration.sendCommand(600) // set the duration for the command to 10min
openhab:send WC_Valve_cmd_OpenWithDuration.sendCommand(ON) // start watering
openhab:send WC_Valve_cmd_CloseValve.sendCommand(ON) // stop any active watering
```

If you send a REFRESH command to the last update timestamp (no matter which thing), **ALL** items from **ALL** things are updated

```java
DateTime LastUpdate "LastUpdate [%1$td.%1$tm.%1$tY %1$tH:%1$tM]" { channel="gardena:water_control:home:myWateringComputer:common#lastUpdate_timestamp" }
```

```shell
// refresh ALL items
openhab:send LastUpdate REFRESH
```

### Server Call Rate Limitation

The Gardena server imposes call rate limits to prevent malicious use of its API.
The limits are:

- On average not more than one call every 15 minutes.
- 3000 calls per month.

Normally the binding does not exceed these limits.
But from time to time the server may nevertheless consider the limits to have been exceeded, in which case it reports an HTTP 429 Error (Limit Exceeded).
If such an error occurs you will be locked out of your Gardena account for 24 hours.
In this case the binding will wait in an offline state for the respective 24 hours, after which it will automatically try to reconnect again.
Attempting to force reconnect within the 24 hours causes the call rate to be exceeded further, and therefore just exacerbates the problem.

### Debugging and Tracing

If you want to see what's going on in the binding, switch the loglevel to TRACE in the Karaf console

```shell
log:set TRACE org.openhab.binding.gardena
```

Set the logging back to normal

```shell
log:set INFO org.openhab.binding.gardena
```

**Notes and known limitations:**
When the binding sends a command to a device, it communicates only with the Gardena smart system integration API.
It has no control over whether the command is sent from the online service via your gateway to the device.
It is the same as if you send the command in the Gardena App.

Schedules, sensor-refresh commands, irrigation control master valve configuration etc. are not supported.
This binding relies on the GARDENA smart system integration API.
Further API documentation: <https://developer.1689.cloud/apis/GARDENA+smart+system+API>

### Troubleshooting

Occasionally it can happen that the API key is no longer valid.

```text
HTTP protocol violation: Authentication challenge without WWW-Authenticate header
```

If this error message appears in the log file, simply renew or delete/create a new API key as described in 'Obtaining your API Key' and restart openHAB.
