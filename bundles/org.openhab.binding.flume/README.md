# Flume Binding

This binding will interface with the cloud API to retrieve water usage from your [Flume](https://flumewater.com/) water monitor.

## Introduction

The Cloud Connector is required as a "bridge" to interface to the cloud service from Flume.
While the Flume API supports a rich querying of historical usage data, this binding only retrieves the cumulative water used and instantaneous water used, thus relying on Openhab's rich persistence services for exploring historical values.

## Supported Things

This binding supports the following things:

| thing                     | type          | description                  |
|----------                 |--------       |------------------------------|
| Flume Cloud Connector     | Bridge        | This represents the cloud account to interface with the Flume API.  |
| Flume Meter Device        | Device        | This interfaces to a specific Flume water monitor associated with the account. |

This binding should work with multiple Flume monitors associated with the account, however it is currently only tested with a single device.

## Discovery

Once a Flume Cloud Connector is created and established, the binding will automatically discover any Flume Meter Devices' associated with the account.

## Flume Cloud Connector (Bridge) Configuration

The only configuration required is to create a Flume Cloud Connector thing and fill in the appropriate configuration parameters.
The client id and client secret can be found under Settings/API access from the [Flume portal online](https://portal.flumewater.com/settings).
Note, there is a rate limit of 120 queries per hour imposed by Flume so use caution when selecting the Refresh Interfacl.

| Name                          | Type      | Description                                                                           | Default | Required | Advanced |
|-------                        |---------  |---------                                                                              |-------  |------    |-----     |
| Flume Username                | text      | Username to access Flume cloud                                                        | N/A     | yes      | no       |
| Flume Password                | text      | Password to access Flume cloud                                                        | N/A     | yes      | no       |
| Flume Client ID               | text      | ID retrieved from Flume cloud                                                         | N/A     | yes      | no       |
| Flume Client Secret           | text      | Secret retrieved from Flume cloud                                                     | N/A     | yes      | no       |
| Instantaneous Refresh Interval| integer   | Polling interval (seconds) for instantaneous usage (rate limited to 120 queries/sec)  | 60      | no       | yes      |
| Cumulative Refresh Interval   | integer   | Polling interval (seconds) for cumulative usage (rate-limited with above)             | 300     | no       | yes      |

## Flume Meter Device Configuration

| Name                  | Type      | Description                                | Default   | Required  | Advanced |
|-------                |---------  |---------                                   |-------    |------     |-----     |
| ID                    | text      | ID of the Flume device                     | N/A       | yes       | no       |

## Flume Meter Device Channels

| Channel               | Type                      | Read/Write | Description |
|----------             |--------                   |--------    |
| Instant Water Usage   | Number:VolumetricFlowRate | R          | Flow rate of water over the last minute  |
| Cumulative Used       | Number:Volume             | R          | Total volume of water used since the beginning of Flume install |
| Battery Level         | Number:Dimensionless      | R          | Estimate of percent of remaining battery level |
| Low Battery           | Switch                    | R          | Indicator of low battery level |
| Last Seen             | DateTime                  | R          | Date/Time when meter was last seen on the network |
| Usage Alert           | Trigger                   | n/a        | Trigger channel for usage alert notification | 

## Full Example

### Thing Configuration

Please note that the device meter ID is only available through the API and not available on the Flume portal.
When the Bridge device is first created, there will be a log message with the ID of the discovered device which can be used in further configuring the device via the text files.

```
Bridge flume:cloud:cloudconnector [ username="xxx", password="xxx",
    clientId="xxx", clientSecret="xxx" ] {
    
    device meter [ id="xxx" ]
}
```

### Item Configuration

```
Number:VolumetricFlowRate     InstantUsage     "Instant Usage"         { channel = "flume:device:cloud:meter:instantUsage" }
Number:Volume                 CumulativeUsed   "Cumulative Used"       { channel = "flume:device:cloud:meter:cumulativeUsage" }
Number:Dimensionless          BatteryLevel     "Battery Level"         { channel = "flume:device:cloud:meter:batteryLevel" }   
DateTime                      LastSeen         "Last Seen"             { channel = "flume:device:cloud:meter:lastSeen" }
Switch                        LowPower         "Battery Low Power"     { channel = "flume:device:cloud:meter:lowBattery" }   

```

### Rules

```
rule "Flume Usage Alert"
when
    Channel 'flume:device:cloud:meter:usageAlert' triggered
then
    logInfo("Flume Usage Alert", "Message: {}", receivedEvent)
end
```

## Rule Actions

There is an action where you can query the Flume Cloud for water usage as shown in the blow example:

```
val flumeActions = getActions("flume", "flume:device:cloud:meter")

if(null === flumeActions) {
    logInfo("actions", "flumeActions not found, check thing ID")
    return
}

val LocalDateTime untilDateTime = LocalDateTime.now
val LocalDateTime sinceDateTime = untilDateTime.minusHours(24)

val usage = flumeActions.queryWaterUsage(sinceDateTime, untilDateTime, "MIN", "SUM")
logInfo("Flume", "Water usage is {}", usage.toString())
```

### queryWaterUsage(sinceDateTime, untilDateTime, bucket, operation)

Queries the cloud for water usage between the two dates.

- sinceDateTime (LocalDateTime): begin date/time of query range
- untilDateTime (LocalDateTime): end date/time of query range
- bucket (String), values: YR, MON, DAY, HR, MIN
- operation (String), values: SUM, AVG, MIN, MAX, CNT
