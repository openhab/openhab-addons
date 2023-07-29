# Generac MobileLink Binding

This binding communicates with the Generac MobileLink API and reports on the status of Generac manufactured generators, including versions resold under the brands Eaton, Honeywell and Siemens.

## Supported Things

### MobileLink Account

A MobileLink account bridge thing represents a user's MobileLink account and is responsible for authentication and polling for updates.

ThingTypeUID: `account`

### Generator

A Generator thing represents an individual generator linked to an account bridge. Multiple generators are supported.  

ThingTypeUID: `generator`

## Discovery

The MobileLink account bridge must be added manually. Once added, generator things will automatically be added to the inbox.  

## Thing Configuration

### MobileLink Account

| Parameter       | Description                                                                        |
|-----------------|------------------------------------------------------------------------------------|
| username        | The user name, typically an email address, used to login to the MobileLink service |
| password        | The password used to login to the MobileLink service                               |
| refreshInterval | The frequency to poll for generator updates, minimum duration is 30 seconds        |

## Channels

### Generator Channels

All channels are read-only.

| channel                 | type                 | description                               |
|-------------------------|----------------------|-------------------------------------------|
| connected               | Switch               | Connected status                          |
| greenLight              | Switch               | Green light state (typically auto mode)   |
| yellowLight             | Switch               | Yellow light state                        |
| redLight                | Switch               | Red light state (typically off mode)      |
| blueLight               | Switch               | Blue light state (typically running mode) |
| statusDate              | DateTime             | Status date (start of day)                |
| status                  | String               | General status                            |
| currentAlarmDescription | String               | Current alarm description                 |
| runHours                | Number:Time          | Number of run hours                       |
| exerciseHours           | Number:Time          | Number of exercise hours                  |
| fuelType                | Number               | Fuel type                                 |
| fuelLevel               | Number:Dimensionless | Fuel level                                |
| batteryVoltage          | String               | Battery voltage status                    |
| serviceStatus           | Switch               | Service status                            |

## Full Example

### Things

```java
Bridge generacmobilelink:account:main "MobileLink Account" [ userName="foo@bar.com", password="secret",refreshInterval=60 ] {
    Thing generator 123456 "MobileLink Generator" [ generatorId="123456" ]
}
```

### Items

```java
Switch GeneratorConnected "Connected [%s]" {channel="generacmobilelink:generator:main:123456:connected"}
Switch GeneratorGreenLight "Green Light [%s]" {channel="generacmobilelink:generator:main:123456:greenLight"}
Switch GeneratorYellowLight "Yellow Light [%s]" {channel="generacmobilelink:generator:main:123456:yellowLight"}
Switch GeneratorBlueLight "Blue Light [%s]" {channel="generacmobilelink:generator:main:123456:blueLight"}
Switch GeneratorRedLight "Red Light [%s]" {channel="generacmobilelink:generator:main:123456:redLight"}
String GeneratorStatus "Status [%s]" {channel="generacmobilelink:generator:main:123456:status"}
String GeneratorAlarm "Alarm [%s]" {channel="generacmobilelink:generator:main:123456:currentAlarmDescription"}
```

### Sitemap

```perl
sitemap MobileLink label="Demo Sitemap" {
  Frame label="Generator" {
    Switch item=GeneratorConnected
    Switch item=GeneratorGreenLight
    Switch item=GeneratorYellowLight
    Switch item=GeneratorBlueLight
    Switch item=GeneratorRedLight
    Text   item=GeneratorStatus
    Text   item=GeneratorAlarm
  }                
}
```
