# EyeOnWater Binding

This binding integrates with the EyeOnWater smart water meter service, allowing you to monitor water consumption, flow rates, and alerts directly from openHAB.

## Supported Things

| Parameter         | Type    | Required | Default          | Description                                                                  |
|-------------------|---------|----------|------------------|------------------------------------------------------------------------------|
| `username`        | Text    | Yes      |                  | The email address or username for your EyeOnWater account                    |
| `password`        | Text    | Yes      |                  | The password for your EyeOnWater account                                     |
| `hostname`        | Text    | Yes      | `eyeonwater.com` | EyeOnWater hostname (e.g., `eyeonwater.com` or `eyeonwater.ca`)              |
| `refreshInterval` | Integer | No       | `15`             | Polling frequency in minutes (minimum of 5)                                  |
| `preferNewSearch` | Boolean | No       | `true`           | Enable to use the modern search API instead of scraping the legacy dashboard |

This binding supports the following thing types:

- `bridge`: Represents the EyeOnWater account connection. It handles authentication, cookie-based session persistence, and schedules background polling.
- `meter`: Represents a physical water meter registered under the EyeOnWater account.

## Discovery

Meters associated with your account are automatically discovered and placed into your openHAB Inbox once an EyeOnWater Account Bridge is added and goes ONLINE.
The discovery service queries the EyeOnWater REST API to retrieve all physical meters, automatically mapping their unique `meter_uuid` and `meter_id` parameters.

## Thing Configuration

### EyeOnWater Account Bridge (`bridge`)

To configure the bridge, the following parameters are available:

### EyeOnWater Water Meter (`meter`)

To manually configure a water meter, the following parameters are available:

| Parameter   | Type | Required | Description                                        |
|-------------|------|----------|----------------------------------------------------|
| `meterUuid` | Text | Yes      | The internal unique identifier (UUID) of the meter |
| `meterId`   | Text | Yes      | The physical serial number / ID of the meter       |

## Channels

The `meter` thing type exposes the following channels:

| Channel ID       | Item Type                   | Label              | Description                                           |
|------------------|-----------------------------|--------------------|-------------------------------------------------------|
| `reading`        | `Number:Volume`             | Water Reading      | The latest total volume consumption reading           |
| `leak-flow-rate` | `Number:VolumetricFlowRate` | Leak Flow Rate     | Active water leak rate, if any, detected by the meter |
| `leak-alert`     | `Switch`                    | Leak Alert         | ON if an active water leak has been detected          |
| `low-battery`    | `Switch`                    | Low Battery Alert  | ON if the meter battery is running low                |
| `reverse-flow`   | `Switch`                    | Reverse Flow Alert | ON if reverse water flow is detected                  |
| `last-read-time` | `DateTime`                  | Last Read Time     | Exact timestamp of the latest water meter reading     |

## Full Example

### `eyeonwater.things` File Configuration

```java
Bridge eyeonwater:bridge:myaccount [ username="user@example.com", password="secretpassword", hostname="eyeonwater.com", refreshInterval=15 ] {
    Thing meter mymeter [ meterUuid="abcdef12-3456-7890-abcd-ef1234567890", meterId="12345678" ]
}
```

### `eyeonwater.items` File Configuration

```java
Number:Volume          WaterMeterReading   "Water Meter Reading [%.2f gal]" <water> { channel="eyeonwater:meter:myaccount:mymeter:reading" }
Number:VolumetricFlowRate  WaterLeakFlowRate   "Water Leak Flow Rate [%.2f gal/min]" <water> { channel="eyeonwater:meter:myaccount:mymeter:leak-flow-rate" }
Switch                 WaterLeakAlert      "Water Leak Alert [%s]" <alarm> { channel="eyeonwater:meter:myaccount:mymeter:leak-alert" }
Switch                 WaterLowBattery     "Water Meter Low Battery [%s]" <battery> { channel="eyeonwater:meter:myaccount:mymeter:low-battery" }
Switch                 WaterReverseFlow    "Water Reverse Flow Alert [%s]" <alarm> { channel="eyeonwater:meter:myaccount:mymeter:reverse-flow" }
DateTime               WaterLastReadTime   "Water Last Read Time [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]" <time> { channel="eyeonwater:meter:myaccount:mymeter:last-read-time" }
```
