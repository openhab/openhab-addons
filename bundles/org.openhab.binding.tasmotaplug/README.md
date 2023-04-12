# TasmotaPlug Binding

This binding connects smart plugs flashed with Tasmota to openHAB.
The plug must report the status of the relay via the url `http://$PLUG_IP/cm?cmnd=Power` in order for the binding to work.

## Supported Things

There is exactly one supported thing type, which represents any supported Tasmota smart plug.
It has the `plug` id.
Multiple Things can be added if more than one plug is to be controlled.

## Discovery

Discovery is not supported. All things must be added manually.

## Thing Configuration

The thing has only two configuration parameters:

| Parameter | Description                                                                                                                                                                                                                                                                                                                |
|-----------|-----------------------------------------------------------------------------------------|
| hostName  | The host name or IP address of the plug. Mandatory.                                     |
| refresh   | Overrides the refresh interval of the plug status. Optional, the default is 30 seconds. |

## Channels

There is only one channel that controls the smart plug relay:

| Channel ID | Item Type | Description                          |
|------------|-----------|--------------------------------------|
| power      | Switch    | Turns the smart plug relay ON or OFF |

## Full Example

tasmotaplug.things:

```java
tasmotaplug:plug:plug1 "Plug 1" [ hostName="192.168.10.1", refresh=30 ]
tasmotaplug:plug:plug2 "Plug 2" [ hostName="myplug2", refresh=30 ]
```

tasmotaplug.items:

```java
Switch Plug1 "Plug 1 Power" { channel="tasmotaplug:plug:plug1:power" }
Switch Plug2 "Plug 2 Power" { channel="tasmotaplug:plug:plug2:power" }
```

tasmotaplug.sitemap:

```perl
sitemap tasmotaplug label="My Tasmota Plugs" {
    Frame label="Plugs" {
        Switch item=Plug1
		Switch item=Plug2
    }
}
```