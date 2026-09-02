# RemehaHeating Binding

This binding integrates Remeha Home heating systems with openHAB.
It connects to the Remeha cloud service using the same API as the official Remeha Home mobile app.

The binding supports monitoring and control of Remeha boilers that are connected to the Remeha Home cloud service.
This includes most modern Remeha boilers with internet connectivity.

Key features include:

- Real-time monitoring of room and outdoor temperatures
- Target temperature control
- Hot water (DHW) temperature monitoring and mode control
- Water pressure monitoring and status
- System error status monitoring

## Supported Things

This binding supports Remeha boilers that are connected to the Remeha Home cloud service.

- `boiler`: Represents a Remeha boiler with ThingTypeUID `remehaheating:boiler`

The binding has been tested with Remeha Calenta Ace boilers but should work with any Remeha boiler that supports the Remeha Home cloud service.

## Discovery

This binding does not support automatic discovery.
Boilers must be manually configured using your Remeha Home account credentials.

Each Remeha Home account typically manages one heating system, so you will need one Thing configuration per account.

## Binding Configuration

This binding does not require any global configuration.
All configuration is done at the Thing level using your Remeha Home account credentials.

## Thing Configuration

To configure a Remeha boiler, you need your Remeha Home account credentials.
These are the same credentials you use for the Remeha Home mobile app.

### `boiler` Thing Configuration

| Name            | Type    | Description                                   | Default | Required | Advanced |
|-----------------|---------|-----------------------------------------------|---------|----------|----------|
| email           | text    | Remeha Home account email address             | N/A     | yes      | no       |
| password        | text    | Remeha Home account password                  | N/A     | yes      | no       |
| refreshInterval | integer | Interval the device is polled in seconds      | 60      | no       | yes      |

The refresh interval should be set between 30 and 3600 seconds.
A shorter interval provides more up-to-date data but may increase API usage.

## Channels

The binding provides the following channels for monitoring and controlling your Remeha heating system:

| Channel             | Type              | Read/Write | Description                                    |
|---------------------|-------------------|------------|------------------------------------------------|
| room-temperature    | Number:Temperature| Read       | Current room temperature                       |
| target-temperature  | Number:Temperature| Read/Write | Target room temperature (5-30°C)              |
| dhw-temperature     | Number:Temperature| Read       | Current hot water temperature                  |
| dhw-target          | Number:Temperature| Read       | Target hot water temperature                   |
| dhw-mode            | String            | Read/Write | DHW mode (anti-frost/schedule/continuous-comfort) |
| dhw-status          | String            | Read       | Hot water status                               |
| water-pressure      | Number:Pressure   | Read       | System water pressure                          |
| water-pressure-ok   | Switch            | Read       | Water pressure status (ON=OK, OFF=Low)        |
| outdoor-temperature | Number:Temperature| Read       | Outdoor temperature                            |
| status              | String            | Read       | Boiler error status                            |

## Full Example

### Thing Configuration

```java
Thing remehaheating:boiler:myboiler "Remeha Boiler" [
    email="<your-email@example.com>",
    password="<your-password>",
    refreshInterval=60
]
```

### Item Configuration

```java
// Temperature monitoring
Number:Temperature RoomTemp "Room Temperature [%.1f °C]" { channel="remehaheating:boiler:myboiler:room-temperature" }
Number:Temperature TargetTemp "Target Temperature [%.1f °C]" { channel="remehaheating:boiler:myboiler:target-temperature" }
Number:Temperature OutdoorTemp "Outdoor Temperature [%.1f °C]" { channel="remehaheating:boiler:myboiler:outdoor-temperature" }

// Hot water
Number:Temperature DHWTemp "Hot Water Temperature [%.1f °C]" { channel="remehaheating:boiler:myboiler:dhw-temperature" }
Number:Temperature DHWTarget "Hot Water Target [%.1f °C]" { channel="remehaheating:boiler:myboiler:dhw-target" }
String DHWMode "Hot Water Mode [%s]" { channel="remehaheating:boiler:myboiler:dhw-mode" }
String DHWStatus "Hot Water Status [%s]" { channel="remehaheating:boiler:myboiler:dhw-status" }

// System status
Number:Pressure WaterPressure "Water Pressure [%.1f bar]" { channel="remehaheating:boiler:myboiler:water-pressure" }
Switch WaterPressureOK "Water Pressure OK" { channel="remehaheating:boiler:myboiler:water-pressure-ok" }
String BoilerStatus "Boiler Status [%s]" { channel="remehaheating:boiler:myboiler:status" }
```

### Sitemap Configuration

```perl
sitemap remeha label="Remeha Heating" {
    Frame label="Temperature Control" {
        Text item=RoomTemp
        Setpoint item=TargetTemp minValue=5 maxValue=30 step=0.5
        Text item=OutdoorTemp
    }
    Frame label="Hot Water" {
        Text item=DHWTemp
        Text item=DHWTarget
        Selection item=DHWMode mappings=["anti-frost"="Anti-frost", "schedule"="Schedule", "continuous-comfort"="Continuous Comfort"]
        Text item=DHWStatus
    }
    Frame label="System Status" {
        Text item=WaterPressure
        Text item=WaterPressureOK
        Text item=BoilerStatus
    }
}
```

## Authentication

This binding uses the same OAuth2 PKCE authentication flow as the official Remeha Home mobile app.
Your credentials are used only to obtain an access token and are not stored permanently.

The binding automatically handles token refresh and re-authentication as needed.

## Limitations

- Only the first appliance from your Remeha Home account is supported
- Only the first climate zone and hot water zone are monitored
- The binding requires an active internet connection to the Remeha cloud service
- API rate limiting may apply - avoid setting very short refresh intervals

## Troubleshooting

- Ensure your Remeha Home account credentials are correct
- Check that your boiler is online in the Remeha Home mobile app
- Verify your openHAB system has internet connectivity
- Check the openHAB logs for authentication or API errors
- Try increasing the refresh interval if you experience connection issues
