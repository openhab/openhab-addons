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
It is used to view and change the current system mode.

### Heating zone

The heating zone thing represents the evohome heating zone.
It displays the current temperature, the temperature set point and the status of the set point.
It also allows you to permanently override the current temperature set point as well as canceling any active overrides.

## Discovery

After setting up the evohome account, the evotouch and heating zones available to your account will be discovered after a manual scan.

## Thing Configuration

Thing configuration is optional, it is easier to use discovery which will automatically add all your zones and displays to the inbox, once the account Thing is online.

### Account

| Name            | Required | Description                                            |
|-----------------|----------|--------------------------------------------------------|
| username        | yes      | The username of your TCC account                       |
| password        | yes      | The password of your TCC account                       |
| refreshInterval | no       | The amount of time in seconds between updates (0-3000) |

### Display &amp; Zone

| Name | Required | Description                                                                            |
|------|----------|----------------------------------------------------------------------------------------|
| id   | yes      | The id which can be found by auto-discovery or the response data (using TRACE logging) |
| name | no       | A friendly name for use in the UI                                                      |

## Channels

### Account

None

### Display

| Channel Type ID | Item Type | Description                                                                                                        |
|-----------------|-----------|--------------------------------------------------------------------------------------------------------------------|
| Mode            | String    | Allows to view or set the system mode. Supported values are: Auto, AutoWithEco, Away, DayOff, HeatingOff, Custom |

### Zone

| Channel Type ID | Item Type          | Description                                                                                                                            |
|-----------------|--------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| Temperature     | Number:Temperature | Allows for viewing the current actual temperature of the zone.                                                                         |
| SetPointStatus  | String             | Allows for viewing the current set point mode of the zone.                                                                             |
| SetPoint        | Number:Temperature | Allows for viewing and permanently overriding the temperature set point of the zone. Sending 0 cancels any active set point overrides. |

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

// evohome Heatingzone
Number:Temperature DemoZoneTemperature  { channel="evohome:heatingzone:your_account_alias:your_zone_alias:Temperature" }
String DemoZoneSetPointStatus           { channel="evohome:heatingzone:your_account_alias:your_zone_alias:SetPointStatus" }
Number:Temperature DemoZoneSetPoint     { channel="evohome:heatingzone:your_account_alias:your_zone_alias:SetPoint" }
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
    }

    Frame label="evohome heating zone" {
        Text     label="Temperature"      item=DemoZoneTemperature
        Text     label="Status"           item=DemoZoneSetPointStatus
        Setpoint label="Zone set point"   item=DemoZoneSetPoint minValue=5 maxValue=35 step=0.5
    }
}
```
