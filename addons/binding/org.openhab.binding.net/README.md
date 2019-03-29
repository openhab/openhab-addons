# Network Protocol Binding

This binding provides features to receive data via network protocols like UDP, TCP and HTTP.
Binding support both binary and text data.

## Supported Things

This binding supports following Bridge ThingTypes:

| ThingType     | Description                                                            |
| --------------|------------------------------------------------------------------------|
| `udp`         | UDP server.                                                             |
| `tcp`         | TCP and TCP with TLS servers.                                           |
| `http`        | HTTP and HTTPS servers.                                                 |

This binding supports following ThingTypes:


| ThingType     | Description                                                            |
| --------------|------------------------------------------------------------------------|
| `data-handler` | Data handler to parse incoming data from `udp`, `tcp` and `http` things. |


Data-handler thing can be use also stand alone thing and data can be inject via rule action.
 
## Thing Configuration

The `udp` Thing has the following configuration parameters: 

| Parameter      | Type    | Required | Default if omitted   | Description                                                  |
| ---------------| ------- | -------- | -------------------- |--------------------------------------------------------------|
| `port`         | integer |   no     | `5000`               | Server UDP port.                                              |
| `convertTo`     | string |   yes     |                     | Convert incoming data to ASCII, BINARY, HEXASTRING or UTF8.   |

The `tcp` and `http` Thing has the following configuration parameters: 

| Parameter      | Type    | Required | Default if omitted   | Description                                    |
| ---------------| ------- | -------- | -------------------- |------------------------------------------------|
| `port`         | integer |   no     | `5000`               | Server UDP port.                                              |
| `convertTo`     | string |   yes     |                     | Convert incoming data to ASCII, BINARY, HEXASTRING or UTF8.   |
| `tls`          | boolean |   no     | `false`              | Secure server with TLS.   |

The `data-handler` Thing doesn't support any configuration parameters.


## Channels

The `udp`, `tcp` and `http` support following channels

| Channel Type ID | Item Type    | Description                                                                                                                |
| --------------- | ------------ | -------------------------------------------------------------------------------------------------------------------------- |
| `dataReceived`   | -            | Trigger channel for received UDP datagram. Received datagram is converted to hexdecimal string format, e.g. `104FA67890`   |

The `data-handler` support following channels

| Channel Type    | Item Type     | Read-only |
|-----------------|---------------|-----------|
| `color`         | Color         | no        |
| `contact`       | Contact       | yes       |
| `datetime`      | DateTime      | no        |
| `dimmer`        | Dimmer        | no        |
| `image`         | Image         | yes       |
| `location`      | Location      | no        |
| `number`        | Number        | no        |
| `player`        | Player        | no        |
| `rollershutter` | Rollershutter | no        |
| `string`        | String        | no        |
| `switch`        | Switch        | no        |

## Actions

The `data-handler` support following actions.

`Long calculateCrc(String algorithm, String hexaString)` : Calculate CRC chekcum over binary data in hexastring format.
`injectData(String hexaString)` : Inject binary data to .

Supported CRC algorithms:



## Examples

### example.things

```xtend

net:udp-server:myudpserver  [ port=5000 ]

net:data-handler:dataHandlerX {
  Channels:
    Type number  : valueB  "Value B" [ transform="BIN2JSON:byte a;byte b;byte c;∩JSONPATH:$.b" ]
}

Bridge net:udp-server:myudpserver1   [ port=6001, convertTo="HEXASTRING" ] {
  Thing data-handler dataHandler1 {
      Channels:
        Type number  : test_number1  "test number 1" [ transform="BIN2JSON:
            byte a;
            byte b;
            byte c;
            bit:1 f1;
            bit:2 f2;
            bit:1 f3;
            bit:4 f4;∩JSONPATH:$.b" ]
        
        Type number  : test_number2  "test number 1" [ transform="BIN2JSON:
            byte a;
            byte b;
            byte c;
            bit:1 f1;
            bit:2 f2;
            bit:1 f3;
            bit:4 f4;∩JSONPATH:$.c" ]
  }
}  
```

### example.rules

```xtend
rule "UDP datagram receiver"
    when
        Channel "net:udp-server:myudpserver:dataReceived" triggered
    then
        val String data = receivedEvent.getEvent()
        logInfo("Test", "UDP server received data: {}", data)

        val net = getActions("net","net:data-handler:dataHandlerX")
        val crc = net.calculateCrc("CRC-16/CCITT-FALSE", data)
        logInfo("Test", "crc=" + crc)

        net.injectData(data)
    end
```

```
[DEBUG] [o.b.n.i.h.AbstractServerBridge:53   ] - Received datagram: 68656C6C6F
[DEBUG] [.m.r.r.i.engine.RuleEngineImpl:338  ] - Executing rule 'UDP datagram receiver'
[INFO ] [s.event.ChannelTriggeredEvent :53   ] - net:udp-server:myudpserver:dataReceived triggered 68656C6C6F
[INFO ] [.e.smarthome.model.script.Test:53   ] - UDP server received data: 68656C6C6F
[DEBUG] [n.i.a.m.NetThingActionsService:71   ] - calculateCrc called with algorithm: 'CRC-16/CCITT-FALSE' data: '68656C6C6F'
[INFO ] [.e.smarthome.model.script.Test:53   ] - crc=53870
[DEBUG] [n.i.a.m.NetThingActionsService:46   ] - injectData called, data: '68656C6C6F'
[DEBUG] [n.internal.handler.DataHandler:145  ] - Channel 'net:data-handler:dataHandlerX:valueB' : params: Configuration[{key=transform; type=String; value=BIN2JSON:byte a;byte b;byte c;∩JSONPATH:$.b}]
[DEBUG] [.Bin2JsonTransformationService:44   ] - About to transform '68656C6C6F' by the Bin2Json syntax 'byte a;byte b;byte c;'
[DEBUG] [.JSonPathTransformationService:59   ] - about to transform '{"a":104,"b":101,"c":108}' by the function '$.b'
[DEBUG] [.JSonPathTransformationService:63   ] - transformation resulted in '101'
[INFO ] [smarthome.event.ItemStateEvent:53   ] - test3 updated to 101
```

### Thing status

Check thing status for errors.

### Verbose logging

Enable DEBUG logging in karaf console to see more precise error messages:

`log:set DEBUG org.openhab.binding.net`

See [openHAB2 logging docs](https://www.openhab.org/docs/administration/logging.html#defining-what-to-log) for more help.
