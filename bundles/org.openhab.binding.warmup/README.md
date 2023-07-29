# Warmup Binding

This binding integrates the Warmup 4iE Thermostat <https://www.warmup.co.uk/thermostats/smart/4ie-underfloor-heating>, via the API at <https://my.warmup.com/>.

Any Warmup 4iE device(s) must be registered at <https://my.warmup.com/> prior to usage.

This API is not known to be documented publicly.
The binding api implementation has been derived from the implementations at <https://github.com/alyc100/SmartThingsPublic/blob/master/devicetypes/alyc100/warmup-4ie.src/warmup-4ie.groovy> and <https://github.com/alex-0103/warmup4IE/blob/master/warmup4ie/warmup4ie.py>, and enhanced by inspecting the GraphQL endpoint.

## Supported Things

The Warmup binding supports the following thing types:

| Bridge         | Label             | Description                                                                            |
|----------------|-------------------|----------------------------------------------------------------------------------------|
| `my-warmup`    | My Warmup Account | The account credentials for my.warmup.com which acts as an API to the Warmup device(s) |

| Thing    | Label | Description                                                                                                          |
|----------|-------|----------------------------------------------------------------------------------------------------------------------|
| `room`   | Room  | A room containing an individual Warmup 4iE device which is a WiFi connected device which controls a heating circuit. |

### Room

The device is optimised for controlling underfloor heating (electric or hydronic), although it can also control central heating circuits.
The device reports the temperature from one of two thermostats, either a floor temperature probe or the air temperature at the device.
The separate temperatures do not appear to be reported through the API. It appears to be possible to configure two devices in a primary / secondary configuration, but it is not clear how this might be represented by the API and hasn't been implemented.

## Discovery

Once credentials are successfully added to the bridge, any rooms (devices) detected will be added as things to the inbox.

## Thing Configuration

### My Warmup Account

| config parameter | type    | description                                     | required | default |
|------------------|---------|-------------------------------------------------|----------|---------|
| username         | String  | Username for my.warmup.com                      | true     |         |
| password         | String  | Password for my.warmup.com                      | true     |         |
| refreshInterval  | Integer | Interval in seconds between automatic refreshes | true     | 300     |

### Room

Rooms are configured automatically with a Serial Number on discovery, or can be added manually using the "Device Number" from the device, excluding the last 3 characters. The only supported temperature change is an override, through a default duration configured on the thing. This defaults to 60 minutes.

| config parameter | type    | description                                                        | required | default |
|------------------|---------|--------------------------------------------------------------------|----------|---------|
| serialNumber     | String  | Device Serial Number, excluding last 3 characters                  | true     |         |
| overrideDuration | Integer | Duration in minutes of override when target temperature is changed | true     | 60      |

## Channels

| channel             | type               | description                                                                                                                                  | read only |
|---------------------|--------------------|----------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| currentTemperature  | Number:Temperature | Currently reported temperature                                                                                                               | true      |
| targetTemperature   | Number:Temperature | Target temperature                                                                                                                           | false     |
| overrideRemaining   | Number:Time        | Duration remaining of the configured override                                                                                                | true      |
| runMode             | String             | Current operating mode of the thermostat, options listed below                                                                               | true      |
| frostProtectionMode | Switch             | Toggles between the "Frost Protection" run mode and the previously configured "active" run mode (known options are either Fixed or Schedule) | false     |

### Run Mode Statuses

These run mode statuses are defined for the API. The descriptions are based on inspection of the device behaviour and are not sourced from documentation.

| api value  | ui name          | description                                                                     |
|------------|------------------|---------------------------------------------------------------------------------|
| not_set    | Not Set          | Unknown                                                                         |
| off        | Off              | Device turned off                                                               |
| schedule   | Schedule         | Device target temperature running to a programmed schedule                      |
| override   | Override         | Target temperature overridden for the remaining duration in overrideRemaining   |
| fixed      | Fixed            | Device target temperature set to a constant fixed value                         |
| anti_frost | Frost Protection | Device target temperature set to 7°C                                            |
| holiday    | Holiday          | Device target temperature set to a constant fixed value for duration of holiday |
| fil_pilote | Fil Pilote       | Unknown                                                                         |
| gradual    | Gradual          | Unknown                                                                         |
| relay      | Relay            | Unknown                                                                         |
| previous   | Previous         | Unknown                                                                         |

## Full Example

### .things file

```java
Bridge warmup:my-warmup:MyWarmup [ username="test@example.com", password="test", refreshInterval=300 ]
{
    room    bathroom    "Home - Bathroom"   [ serialNumber="AABBCCDDEEFF", overrideDuration=60 ]
}
```

### .items file

```java
Number:Temperature bathroom_temperature "Temperature [%.1f °C]" <temperature> (GF_Bathroom, Temperature)    ["Temperature"] {channel="warmup:room:MyWarmup:bathroom:currentTemperature"}
Number:Temperature bathroom_setpoint    "Set Point [%.1f °C]" <temperature> (GF_Bathroom) ["Set Point"] {channel="warmup:room:MyWarmup:bathroom:targetTemperature"}
Number:Time bathroom_overrideRemaining  "Override Remaining [%d minutes]" (GF_Bathroom) {channel="warmup:room:MyWarmup:bathroom:overrideRemaining"}
String bathroom_runMode "Run Mode [%s]" (GF_Bathroom) {channel="warmup:room:MyWarmup:bathroom:runMode"}
Switch bathroom_frostProtection "Frost Protection Mode" (GF_Bathroom) {channel="warmup:room:MyWarmup:bathroom:frostProtectionMode"}
```

### Sitemap

```perl
Text label="Bathroom" {
    Text item=bathroom_temperature
    Setpoint item=bathroom_setpoint step=0.5
    Text item=bathroom_overrideRemaining
    Text item=bathroom_runMode
    Switch item=bathroom_frostProtection
}
```
