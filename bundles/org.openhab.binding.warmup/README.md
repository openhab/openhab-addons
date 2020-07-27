# Warmup Binding

This binding integrates the Warmup 4iE Thermostat https://www.warmup.co.uk/thermostats/smart/4ie-underfloor-heating, via the API at https://my.warmup.com/.

Any Warmup 4iE device(s) must be registered at https://my.warmup.com/ prior to usage.

This API is not known to be documented publicly. The binding api implementation has been derived from the implementations at https://github.com/alyc100/SmartThingsPublic/blob/master/devicetypes/alyc100/warmup-4ie.src/warmup-4ie.groovy and https://github.com/alex-0103/warmup4IE/blob/master/warmup4ie/warmup4ie.py. 

## Supported Things

* Bridge - Credentials to my.warmup.com which allow the rest of the system to function
* Room - A room containing an individual Warmup 4iE device which is a WiFi connected device which controls a heating circuit. 
The device is optimised for controlling underfloor heating (electric or hydronic), although it can also control central heating circuits.
The device reports the temperature from one of two thermostats, either a floor temperature probe or the air temperature at the device.
The separate temperatures do not appear to be reported through the API. It appears to be possible to configure two devices in a primary / secondary configuration, but it is not clear how this might be represented by the API and hasn't been implemented.

## Discovery

Once credentials are successfully added to the bridge, any rooms (devices) detected will be added as things to the inbox.

## Thing Configuration

There are two types of thing, "My Warmup Account" and "Room".

My Warmup Account needs configuration with your username and password for the my.warmup.com site. You can configure the refresh interval in seconds, it defaults to 300s (5 minutes).

Rooms are configured automatically with a Serial Number on discovery, or can be added manually using the "Device Number" from the device, excluding the last 3 characters. The only supported temperature change is an override, through a default duration configured on the thing. This defaults to 60 minutes.

## Channels

| channel  | type   | description                  | read only |
| --------- | -------- | ------------------------------ | ----------- |
| currentTemperature | Number:Temperature | Currently reported temperature | true |
| targetTemperature | Number:Temperature | Target temperature | false |

## Full Example

### .things file

```
Bridge warmup:my-warmup:MyWarmup [ username="test@example.com", password="test", refresh=300 ]
{
    room    bathroom    "Home - Bathroom"   [ serialNumber="AABBCCDDEEFF", overrideDuration=60 ]
}
```

### .items file

```
Number:Temperature bathroom_temperature    "Temperature [%.1f °C]" <temperature> (GF_Bathroom, Temperature)    ["Temperature"] {channel="warmup:room:MyWarmup:bathroom:currentTemperature"}
Number:Temperature bathroom_setpoint    "Set Point [%.1f °C]" <temperature> (GF_Bathroom)    ["Set Point"] {channel="warmup:room:MyWarmup:bathroom:targetTemperature"}
```

### Sitemap

```
Text label="Bathroom" icon="sofa" {
    Text item=bathroom_temperature
    Setpoint item=bathroom_setpoint step=0.5
}
```

## Todo

- Add boost duration and status as channels.
- Support other temperature setting modes (fixed / schedule / holiday).
- Expose more status channels (roomMode, energy consumption & cost etc.)
- Expose Location as a Thing, and support channels such as energy consumption, heating on/off / holiday / frost protect modes.
- Support primary / secondary device configuration
- Explore GraphQL API for more data.