# MyQ Binding

This binding integrates with the [The Chamberlain Group MyQ](https://www.myq.com) cloud service. It allows monitoring and control over [MyQ](https://www.myq.com) enabled garage doors manufactured by LiftMaster, Chamberlain and Craftsman.

## Supported Things

### Account

This represents the MyQ cloud account and uses the same credentials needed when using the MyQ mobile application.

ThingTypeUID: `account`

### Garage Door

This represents a garage door associated with an account. Multiple garage doors are supported.

ThingTypeUID: `garagedoor`

### Lamp

This represents a lamp associated with an account. Multiple lamps are supported.

ThingTypeUID: `lamp`

## Discovery

Once an account has been added, garage doors and lamps will automatically be discovered and added to the inbox.

## Channels

| Channel       | Item Type     | Thing Type       | States                                                 |
|---------------|---------------|------------------|--------------------------------------------------------|
| status        | String        | garagedoor       | opening, closed, closing, stopped, transition, unknown |
| rollershutter | Rollershutter | garagedoor       | UP, DOWN, 0%, 100%                                     |
| closeError    | Switch        | garagedoor       | ON (has error), OFF (doesn't have error)               |
| openError     | Switch        | garagedoor       | ON (has error), OFF (doesn't have error)               |
| switch        | Switch        | garagedoor, lamp | ON (open), OFF (closed)                                |

## Full Example

### Thing Configuration

```xtend
Bridge myq:account:home "MyQ Account" [ username="foo@bar.com", password="secret", refreshInterval=60 ] {
    Thing garagedoor abcd12345 "MyQ Garage Door" [ serialNumber="abcd12345" ]
    Thing lamp efgh6789 "MyQ Lamp" [ serialNumber="efgh6789" ]
}
```

### Items

```xtend
String MyQGarageDoor1Status "Door Status [%s]" {channel = "myq:garagedoor:home:abcd12345:status"}
Switch MyQGarageDoor1Switch "Door Switch [%s]" {channel = "myq:garagedoor:home:abcd12345:switch"}
Switch MyQGarageDoor1CloseError "Door Close Error [%s]" {channel = "myq:garagedoor:home:abcd12345:closeError"}
Switch MyQGarageDoor1OpenError "Door OpenError [%s]" {channel = "myq:garagedoor:home:abcd12345:openError"}
Rollershutter MyQGarageDoor1Rollershutter "Door Rollershutter [%s]" {channel = "myq:garagedoor:home:abcd12345:rollershutter"}
Switch MyQGarageDoorLamp "Lamp [%s]" {channel = "myq:lamp:home:efgh6789:switch"}
}
```

### Sitemaps

```xtend
sitemap MyQ label="MyQ Demo Sitemap" {
  Frame label="Garage Door" {
    String item=MyQGarageDoor1Status
    Switch item=MyQGarageDoor1Switch
    Rollershutter item=MyQGarageDoor1Rollershutter
  }                
}
```
