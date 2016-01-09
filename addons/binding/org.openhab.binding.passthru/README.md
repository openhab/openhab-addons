# Passthru Binding

This binding forwards local messages to a remote openhab 1 or openhab 2 system especially for usage to control a remote Raspberry Pi. The passthru binding tries to recover in case the state on the employed systems are different. Within the later case the remote system will win to display the correct state on the local system.

## Supported Things

Every remote system with an ip address within the network is supported.

## Thing Configuration

passthru:device:remote [ host="192.168.1.204", port=8080, version=1, refresh=60000, monitor="MyItem1,MyItem2" ]
```
- host: IP address or hostname of the remote openhab system
- port: An open Port where the remote system can be accessed
- version: The version of the openhab target system. If version is not defined the binding
            tries to find the openhab target version by itself
- refresh: The refresh polling rate to compare target states
- monitor: The targets to be monitored between the remote and the local openhab system

## Channels

All devices support the following channel:

| Channel Type ID | Item Type    | Description  |
|-----------------|------------------------|--------------|----------------- |------------- |
| remote          | Switch       | This channel forwards the local request to the remote    |

## Full Example

demo.things:
```
passthru:device:remote [ host="192.168.1.204", port=8080, version=1, monitor="MyItem1,MyItem2" ]

demo.items:
```
Switch MyItem1 { channel="passthru:device:remote:MyItem1" }
Switch MyItem2 { channel="passthru:device:remote:MyItem2" }

```

