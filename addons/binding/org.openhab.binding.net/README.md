# Network Protocol Binding

This binding provides features to communicate via network protocols like UDP.

## Supported Things

This binding supports one ThingType: `udp`.

## Thing Configuration

The `udp` Thing has the following configuration parameters: 

| Parameter      | Type    | Required | Default if omitted   | Description                                    |
| ---------------| ------- | -------- | -------------------- |------------------------------------------------|
| `port`         | integer |   no     | `5000`               | Server UDP port.                               |
| `maxpdu`       | integer |   no     | `1500`               | Maximum PDU size for received UDP datagrams.   |
| `charset`       | string |   no     | `HexString`            | .   |

## Channels

List of channels

| Channel Type ID    | Item Type    | Description                                                                                                                |
| ------------------ | ------------ | -------------------------------------------------------------------------------------------------------------------------- |
| `dataReceived`     | -            | Trigger channel for received UDP datagram. Received datagram is converted to hexdecimal string format, e.g. `104FA67890`   |

## Examples

### example.things

```xtend

net:udp:myudpserver [ port=9999, maxpdu=100 ]

```

### example.rules

```xtend
rule "UDP datagram receiver"
    when
        Channel "net:udp:myudpserver:dataReceived" triggered
    then
        logInfo("Test", "received: {}", receivedEvent)
    end
```

### Thing status

Check thing status for errors.

### Verbose logging

Enable DEBUG logging in karaf console to see more precise error messages:

`log:set DEBUG org.openhab.binding.net`

See [openHAB2 logging docs](https://www.openhab.org/docs/administration/logging.html#defining-what-to-log) for more help.
