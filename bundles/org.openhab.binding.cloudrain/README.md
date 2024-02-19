# Cloudrain Binding

This is a binding for the [Cloudrain system](https://cloudrain.de/). 
Cloudrain offers a smart irrigation system which uses weather data to optimize irrigation schedules. 
The binding allows you to integrate, view and control Cloudrain devices in the openHAB environment.

## Supported Things

Cloudrain currently offers two device types: Controllers and wireless valves. 
The controller is the central hub controlling wired and/or wireless valves. 
These valves are not controlled directly, but through a concept of so called irrigation zones. 
An irrigation zone is a group of one or more valves which are jointly controlled in case of starting, stopping or adjusting irrigations. 
The Cloudrain developer API does not offer direct access on device-level. 
Instead irrigations can be viewed and managed through the Cloudrain account and the defined zones. 
These objects are represented as things by the binding.

| Thing type                         | Name                                                                |
|------------------------------------|---------------------------------------------------------------------|
| Cloudrain Account (bridge)         | The user's Cloudrain account for managing all connected controllers |
| Irrigation Zone                    | Irrigation zones group one or more valves and control their status  |


## Discovery

Once the user adds a Cloudrain Account with all required authentication attributes it will act as bridge and automatically discover defined zones.

## Binding Configuration

These are mandatory attributes to be configured in the Cloudrain Account's configuration:

| Name                    | Required | Description                                                                                |
|-------------------------|----------|--------------------------------------------------------------------------------------------|
| user                    | yes      | The user name / email for logging into the Cloudrain account                               |
| password                | yes      | The password for logging into the Cloudrain account                                        |
| clientId                | yes      | The client Id obtained by the Cloudrain Developer API                                      |
| clientSecret            | yes      | The client secret obtained by the Cloudrain Developer API                                  |


### Obtaining API access

1. Goto https://developer.cloudrain.com/login and sign in using your Cloudrain account user name and password.
2. Create and a new application via the 'Create application' button
3. Note the Client ID and Client Secret generated for the application
4. Copy both attributes into the respective configuration parameters of the OpenHab Cloudrain Account

### Advanced Configuration

The advanced settings allow fine-tuning of the binding's behavior, but have default values which should work for most users. 

| Name                    | Required | Description                                                                                |
|-------------------------|----------|--------------------------------------------------------------------------------------------|
| connectionTimeout       | no       | Timeout in seconds for connecting to the Cloudrain Developer API (default: 10)           |
| irrigationUpdateInterval| no       | Interval in seconds for polling the API for irrigation updates (default: 30)    |
| zoneUpdateInterval      | no       | Interval in seconds for polling the API for zone property changes (default: 300)|
| realtimeUpdate          | no       | Update remaining seconds of active irrigations in real time without API calls (default: true) |
| updateAfterIrrigation   | no       | API status update after expected irrigation end additional to polling (default: true)     |
| testMode                | no       | Test mode allows you to test rules without triggering real actions (default: false)  |

***Status Polling*** 
Irrigation and zone status updates are polled at fixed intervals from the Cloudrain API. 
As zone property updates typically happen less frequently and are typically of less interest than irrigation status updates they are fetched in a separate polling job at lower frequency. 
Both intervals can be changed to the user's needs, but values below 10 seconds are not recommended. 
The 'realtimeUpdate' setting allows to count down the remaining seconds of active irrigations in real time without additional API calls. 
This is intended for displaying the item on any UI. 
If no such behavior is desired it is possible to switch off the real time updates in order to reduce overhead. 
Additional updates of the irrigation status are performed after command execution and at the expected end of an irrigation if this is desired by the user.

***Test Mode*** 
The test mode was created to allow testing the binding without actually owning Cloudrain devices or to test rules without actually triggering real actions. 
When this setting is 'true' the account will discover three test zones regardless of the authentication information provided. All actions can be executed and the zones will reflect the actual irrigation status. 
If already real zones were added they will be shown as OFFLINE as long as the test mode is active. 
When the test mode is deactivated the account expects correct authentication settings and the test zones will go OFFLINE.

## Channels

Irrigation Zones have two groups of channels: Status channels to receive information about active irrigations and command channels to start, stop and change irrigations in a zone.

### Irrigation Zone status channels

| Channel                   | Type     | Description                                                                                  |
|---------------------------|----------|----------------------------------------------------------------------------------------------|
| status                    | Switch   | The irrigation status in the irrigation zone (ON \| OFF)                                     |
| duration                  | Integer  | The total duration in seconds of an active irrigation. NULL if no irrigation is active.      |
| remainingSeconds          | Integer  | The remaining duration in seconds of an active irrigation. NULL if no irrigation is active.  |
| startTime                 | Time     | The local time at which the irrigation was started. NULL if no irrigation is active.         |
| plannedEndTime            | Time     | The local time at which the irrigation is planned to end. NULL if no irrigation is active.   |

### Irrigation Zone command channels

| Channel                   | Type     | Description                                                                                  |
|---------------------------|----------|----------------------------------------------------------------------------------------------|
| commandDuration          | Integer  | The duration in seconds used for command execution (starting or adjusting irrigations).      |
| startIrrigation          | Switch   | Activating this switch will start a new irrigation with the defined duration in this zone.   |
| changeIrrigation         | Switch   | Activating this switch will adjust an active irrigation to the new defined duration.         |
| stopIrrigation           | Switch   | Activating this switch will stop an active irrigation. No effect if no irrigation is active. |


## Full Example

Minimal Thing configuration:

```
Bridge cloudrain:account:xyz [ user="...", password="...", clientId="...", clientSecret="..." ]
```

Once a connection to an account is established, defined zones are discovered automatically. 
It is recommended to use the zone discovery instead of manually adding zones as the mandatory zone ID is not visible in the Cloudrain App.

### Items

In the items file, you can link items to channels of your things:

```
// irrigation info channels
Switch Zone1_State {channel="cloudrain:zone:xyz:abc:irrigation#state"}
Number Zone1_Duration "Duration: [%d sec]" {channel="cloudrain:zone:xyz:abc:irrigation#duration"}
Number Zone1_Remaining "Remaining: [%d sec]" {channel="cloudrain:zone:xyz:abc:irrigation#remainingSeconds"}
DateTime Zone1_Start "Start: [%1$ta %1$tR]" {channel="cloudrain:zone:xyz:abc:irrigation#startTime"}
DateTime Zone1_End "End: [%1$ta %1$tR]" {channel="cloudrain:zone:xyz:abc:irrigation#plannedEndTime"}

// irrigation commands
Number Zone1_Cmd_Duration {channel="cloudrain:zone:xyz:abc:command#commandDuration"}
Switch Zone1_Cmd_Start {channel="cloudrain:zone:xyz:abc:command#startIrrigation"}
Switch Zone1_Cmd_Stop {channel="cloudrain:zone:xyz:abc:command#stopIrrigation"}
Switch Zone1_Cmd_Change {channel="cloudrain:zone:xyz:abc:command#changeIrrigation"}
// and so on...
```

The information channels are read-only. Commands can be sent using one of the OpenHab UI elements (e.g. switches), the console or using rules.

### Rules

This is an example of how to send commands in a rule:

```
rule "Demo rule"
when
    // your plants need some water
then
    // set the duration for the next command to 600 seconds
    Zone1_Cmd_Duration.sendCommand(600)
    // start the irrigation
    Zone1_Cmd_Start.sendCommand(ON)
end
```

### Sitemaps

This is an example of how to use the items in a sitemap:

```
sitemap demo label="Demo Sitemap" {
    Frame label="Irrigation Status" {
        Switch item=Zone1_State label="Irrigation State"
        Text item=Zone1_Remaining label="Remaining Seconds"
        Text item=Zone1_Duration label="Duration"
        Text item=Zone1_Start label="Start Time"
        Text item=Zone1_End label="End Time"
    }
    Frame label="Irrigation Commands" {
        Slider item=Zone1_Cmd_Duration label="Command Duration" minValue=60 maxValue=3600
        Switch item=Zone1_Cmd_Start label="Start Irrigation"
        Switch item=Zone1_Cmd_Stop label="Stop Irrigation"
        Switch item=Zone1_Cmd_Change label="Change Irrigation"
    }
}
```

## Notes and known limitations

When the binding sends a command it communicates only with the Cloudrain Developer API. 
It has no control over whether the command is sent from the Cloudrain backend via your Cloudrain controller to the actual device. 
It is comparable to as if you send the command through the Cloudrain App.

The Cloudrain Developer API only supports a subset of features of the Cloudrain App. 
Un-supported features include (but are not limited to): Irrigation schedules, past irrigation data, weather information, device information (battery level etc.). 
Some additional information may be retrievable through the local network, but the binding deliberately only relies on supported APIs of the Cloudrain Developer API.
Further API documentation: https://developer.cloudrain.com/documentation/api-documentation
