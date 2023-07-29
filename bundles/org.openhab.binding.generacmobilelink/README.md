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

| Channel ID           | Item Type                   | Description                       |
|----------------------|-----------------------------|-----------------------------------|
| heroImageUrl         | String                      | Hero Image URL                    |
| statusLabel          | String                      | Status Label                      |
| statusText           | String                      | Status Text                       |
| activationDate       | DateTime                    | Activation Date                   |
| deviceSsid           | String                      | Device SSID                       |
| status               | Number                      | Status                            |
| isConnected          | Switch                      | Is Connected                      |
| isConnecting         | Switch                      | Is Connecting                     |
| showWarning          | Switch                      | Show Warning                      |
| hasMaintenanceAlert  | Switch                      | Has Maintenance Alert             |
| lastSeen             | DateTime                    | Last Seen                         |
| connectionTime       | DateTime                    | Connection Time                   |
| runHours             | Number:Time                 | Number of Hours Run               |
| batteryVoltage       | Number:ElectricPotential    | Battery Voltage                   |
| hoursOfProtection    | Number:Time                 | Number of Hours of Protection     |
| signalStrength       | Number:Dimensionless        | Signal Strength                   |


## Full Example

### Things

```java
Bridge generacmobilelink:account:main "MobileLink Account" [ userName="foo@bar.com", password="secret",refreshInterval=60 ] {
    Thing generator 123456 "MobileLink Generator" [ generatorId="123456" ]
}
```

### Items

```java
String GeneratorHeroImageUrl "Hero Image URL [%s]" { channel="generacmobilelink:generator:main:123456:heroImageUrl" }
String GeneratorStatusLabel "Status Label [%s]" { channel="generacmobilelink:generator:main:123456:statusLabel" }
String GeneratorStatusText "Status Text [%s]" { channel="generacmobilelink:generator:main:123456:statusText" }
DateTime GeneratorActivationDate "Activation Date [%s]" { channel="generacmobilelink:generator:main:123456:activationDate" }
String GeneratorDeviceSsid "Device SSID [%s]" { channel="generacmobilelink:generator:main:123456:deviceSsid" }
Number GeneratorStatus "Status [%d]" { channel="generacmobilelink:generator:main:123456:status" }
Switch GeneratorIsConnected "Is Connected [%s]" { channel="generacmobilelink:generator:main:123456:isConnected" }
Switch GeneratorIsConnecting "Is Connecting [%s]" { channel="generacmobilelink:generator:main:123456:isConnecting" }
Switch GeneratorShowWarning "Show Warning [%s]" { channel="generacmobilelink:generator:main:123456:showWarning" }
Switch GeneratorHasMaintenanceAlert "Has Maintenance Alert [%s]" { channel="generacmobilelink:generator:main:123456:hasMaintenanceAlert" }
DateTime GeneratorLastSeen "Last Seen [%s]" { channel="generacmobilelink:generator:main:123456:lastSeen" }
DateTime GeneratorConnectionTime "Connection Time [%s]" { channel="generacmobilelink:generator:main:123456:connectionTime" }
Number:Time GeneratorRunHours "Number of Hours Run [%d]" { channel="generacmobilelink:generator:main:123456:runHours" }
Number:ElectricPotential GeneratorBatteryVoltage "Battery Voltage [%d]v" { channel="generacmobilelink:generator:main:123456:batteryVoltage" }
Number:Time GeneratorHoursOfProtection "Number of Hours of Protection [%d]" { channel="generacmobilelink:generator:main:123456:hoursOfProtection" }
Number:Dimensionless GeneratorSignalStrength "Signal Strength [%d]" { channel="generacmobilelink:generator:main:123456:signalStrength" }

```

### Sitemap

```perl
sitemap generacmobilelink label="Generac MobileLink"
{
    Frame label="Generator Status" {
        Text item=GeneratorStatus
        Text item=GeneratorStatusLabel
        Text item=GeneratorStatusText
    }

    Frame label="Generator Properties" {
        Text item=GeneratorRunHours
        Text item=GeneratorHoursOfProtection
        Text item=GeneratorBatteryVoltage
        Text item=GeneratorSignalStrength
    }
}
```
