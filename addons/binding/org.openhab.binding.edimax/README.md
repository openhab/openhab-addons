# Edimax Binding

This binding can handle Edimax Smart Plug switches.

## Supported Things

This binding supports the following thing types:

| Thing        | Thing Type | Description                                            |
|--------------|------------|--------------------------------------------------------|
| sp1101w      | Thing      | A smart plug, that can be remote switched.             |
| sp2101w      | Thing      | Same as sp1101w, but with metering feature.            |

## Binding Configuration

There are no overall binding configuration settings that need to be set. All settings are through thing configuration parameters.<

## Thing Configuration

All things has the same configuration properties.

| Parameter Label              | Parameter ID             | Description                                        | Required |
|------------------------------|--------------------------|----------------------------------------------------|----------|
| IP Address                   | ipAddress                | The IP address of the Edimax Plug.                 | true     |
| Username                     | username                 | Username to access the Edimax Plug.                | true     |
| Password                     | password                 | Password to access the Edimax Plug.                | true     |

## Channels

SP-1101W support the following channels:

| Channel Type ID | Item Type    | Description                               |
|-----------------|--------------|-------------------------------------------|
| switch          | Switch       | On/Off Switch                             |

SP-2101W support the following channels:

| Channel Type ID | Item Type    | Description                               |
|-----------------|--------------|-------------------------------------------|
| switch          | Switch       | On/Off Switch                             |
| Current         | Number       | Current of the Switch in ampere           |
| Power           | Number       | Power of the Switch in watt               |

## Full Example

**demo.things**

```
edimax:sp1101w:switch1 "Switch SP-1101W" [ ipAddress="192.168.188.26", username="admin", password="1234" ]
edimax:sp2101w:switch2 "Switch SP-2101W" [ ipAddress="192.168.188.26", username="admin", password="1234" ]
```

**demo.items**

```
Switch Light   { channel="edimax:sp1101w:switch1:switch"} 
Switch Washer  { channel="edimax:sp2101w:switch2:switch"}
Number Current { channel="edimax:sp2101w:switch2:current"}
Number Power   { channel="edimax:sp2101w:switch2:power"}
```