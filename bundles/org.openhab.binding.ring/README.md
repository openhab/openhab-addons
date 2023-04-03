## Note: Development of this binding has been put on hold for the forseeable future. Pull requests are welcomed, however.

# <bindingName> Ring

This is an experimental binding to the Ring.com API. It currently supports a Ring account
and is able to discover Ring Video Doorbells and Chimes. They need to be registered in
the Ring account before they will be detected.

It currently does *not* support live video streaming, but you can view recorded video's,
if this service is enabled in the Ring account.

## Supported Things

The binding currently supports Ring Video Doorbell and Chimes.

## Discovery

Auto-discovery is applicable to this binding. After (manually) adding a Ring Account thing, 
registered doorbells and chimes will be auto discovered.

## Binding Configuration

There is binding configuration necessary. The easiest way to do this is from the Paper UI. Just
add a new thing, select the Ring binding, then Ring Account Binding Thing, and enter username and password. 
Optionally, you can also specify a unique hardware ID and refresh interval for how often to check ring.com for
events. If hardware ID is not specified, the MAC address of the system running OpenHAB is used.

## Channels

### Control group (all things):

| Channel Type ID | Item Type | Description                           |
|-----------------|-----------|---------------------------------------|
| Enabled         | Switch    | Enable polling of this device/account |

### Events group (Ring Account Binding Thing only):

Todo: Move these to the device thing

| Channel Type ID                | Item Type | Description                                                                                  |
|--------------------------------|-----------|----------------------------------------------------------------------------------------------|
| URL to recorded video          | String    | The URL to a recorded video (only when subscribed on ring.com)                               |
| When the event was created     | DateTime  | The date and time the event was created                                                      |
| The kind of event              | String    | The kind of event, usually 'motion' or 'ding'                                                |
| The id of the doorbot          | String    | The internal id of the doorbot that generated the currently selected event                   |
| The description of the doorbot | String    | The description of the doorbot that generated the currently selected event (e.g. Front Door) |

### Device Status (Video Doorbell Binding Thing and Stickup Cam Binding Thing only):

| Channel Type ID  | Item Type | Description         |
|------------------|-----------|---------------------|
| Battery level    | Number    | Battery level in %  |

## Full Example
NOTE 1: Replace <ring_device_id> with a valid ring device ID when manually configuring. The easiest way to currently get that is to define the account thing and pull the device ID from the last event channel.

NOTE 2: Text configuration for the Things ONLY works if you DO NOT have 2 factor authentication enabled. If you are using 2 factor authentication, Things MUST be set up through PaperUI

ring.things:

```java
ring:account:ringAccount "Ring Account"     [ username="user@domain.com", password="XXXXXXX", hardwareId="AA-BB-CC-DD-EE-FF", refreshInterval=5 ]
ring:doorbell:<ring_device_id>          "Ring Doorbell"    [ refreshInterval=5, offOffset=0 ]
ring:chime:<ring_device_id>             "Ring Chime"       [ refreshInterval=5, offOffset=0 ]
ring:stickup:<ring_device_id>           "Ring Stickup Camera"       [ refreshInterval=5, offOffset=0 ]
```

ring.items:

```java
Switch     RingAccountEnabled             "Ring Account Polling Enabled"    { channel="ring:account:ringAccount:control#enabled" }
String     RingEventVideoURL              "Ring Event URL"                  { channel="ring:account:ringAccount:event#url" }
DateTime   RingEventCreated               "Ring Event Created"              { channel="ring:account:ringAccount:event#createdAt" } 
String     RingEventKind                  "Ring Event Kind"                 { channel="ring:account:ringAccount:event#kind" }
String     RingEventDeviceID              "Ring Device ID"                  { channel="ring:account:ringAccount:event#doorbotId" }
String     RingEventDeviceDescription     "Ring Device Description"         { channel="ring:account:ringAccount:event#doorbotDescription" }

Switch     RingDoorbellEnabled            "Ring Doorbell Polling Enabled"   { channel="ring:doorbell:<ring_device_id>:control#enabled" }
Number     RingDoorbellBattery            "Ring Doorbell Battery [%s]%"     { channel="ring:doorbell:<ring_device_id>:status#battery"}

Switch     RingChimeEnabled               "Ring Chime Polling Enabled"      { channel="ring:chime:<ring_device_id>:control#enabled" }

Switch     RingStickupEnabled            "Ring Stickup Polling Enabled"   { channel="ring:stickup:<ring_device_id>:control#enabled" }
Number     RingStickupBattery            "Ring Stickup Battery [%s]%"     { channel="ring:stickup:<ring_device_id>:status#battery"}

```
