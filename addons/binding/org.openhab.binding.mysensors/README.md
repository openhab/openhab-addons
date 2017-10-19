# Configuration

If you have successfully installed the MySensors binding you're ready to configure your things, items and sitemaps.

## Handling of node ids

MySensors uses ids to distinguish between nodes. If you're using sketches from the MySensors page the newly created sensors will ask the controller for an id. This binding hands out random ids that are, according to the binding, not yet occupied. The sensor stores the id that was given by the controller and wont ask again. Another approach is to hardcode an id in the sketch of the sensor.

If you have hardcoded the id in the sketch you just have to assign the id to a thing in OH2. If the id is handed out by the OH2 binding you have to watch the debug output of OH2 to get the id and assign it.

## Configuring the gateway

The binding currently supports the MySensors SerialGateway and the EthernetGateway. You are free to choose, which gateway to use. The first step to configure the binding is to activate the gateway/bridge. 

You're able to configure the gateway via the PaperUI (Configuration->Things) or with an entry in a things file.

**If OH2 is not running with root privileges for example if you installed via apt please ensure that you are allowed to access the serial port. **

`sudo usermod -a -G dialout openhab`

Things file under "conf/things/demo.things".

SerialGateway:

```
Bridge mysensors:bridge-ser:gateway [ serialPort="/dev/pts/2", sendDelay=200 ] {
    /** define things connected to that bridge here */
  }
```

The serial gateway from MySensors works with a baud rate of 115.200. If you're using a different baud rate you need to add an additional parameter "baudRate":

```
Bridge mysensors:bridge-ser:gateway [ serialPort="/dev/pts/2", sendDelay=200, baudRate=115200 ] {
    /** define things connected to that bridge here */
  }
```

If the gateway gets stuck on a regular basis you may try `hardReset=true`. This will try to reset the attached gateway with the DTR line on a disconnect or reconnect. This only makes sense together with the network sanity checker.

  
EthernetGateway:

```
Bridge mysensors:bridge-eth:gateway [ ipAddress="127.0.0.1", tcpPort=5003, sendDelay=200 ] {
     /** define things connected to that bridge here */
  }
```

MQTTGateway:
 
```
Bridge mysensors:bridge-mqtt:gateway [ brokerName="mosquitto", topicPublish="mygateway1-in", topicSubscribe="mygateway1-out", startupCheckEnabled=false ] {
}
```

#### Common parameters for all gateway types

| Parameter | Value | Description |
| --------- | ----- | ----------- |
| `sendDelay` | **Milliseconds** | The delay between messages send to the gateway |
| `startupCheckEnabled` | **boolean** |  The software version is requested from the gateway at startup to ensure it's up and running |
| `imperial` | **boolean** | Metric answer with imperial instead of metric |
| `networkSanCheckEnabled` | **boolean** | Network sanity check periodically ensure that gateway is up and running |
| `networkSanCheckInterval` | **Minutes** | Network sanity check periodically ensure that gateway is up and running |
| `networkSanCheckConnectionFailAttempts` | **Number** | Number of retries to establish a connection to the gateway |
| `networkSanCheckSendHeartbeat` | **boolean** | If network sanity checker is running, send heartbeat to all nodes |
| `networkSanCheckSendHeartbeatFailAttempts` | **Number** | If nodes do not respond to heartbeat, you could configure how many retries the sanity check will do before disconnecting them  |

### Skip startup check

In some cases you need to skip the startup check for I_VERSION. The binding asks the gateway for its version and if no answer is given the connection to the thing will fail.

A reason could be if you see a message like this:

```
[WARN ] [ome.core.thing.internal.ThingManager] - Initializing handler for thing 'mysensors:bridge-ser:MySGWKeller' takes more than 5000ms.
```

In this case the hardware OH2 is running on takes a bit too long to startup (due to the lack of CPU power).

To skip the startup check add an option **startupCheckEnabled=false** to the gateway/thing:

```
Bridge mysensors:bridge-eth:gateway [ ipAddress="127.0.0.1", tcpPort=5003, sendDelay=200, startupCheckEnabled=false ] {
     /** define things connected to that bridge here */
  }
```

The default is **startupCheckEnabled=true**!


### Use Imperial instead of Metric

Some sensors, like temperature sensors, ask the controller on startup if values should be reported in metric or imperial. If you would like to receive imperial values you may add the option **imperial=true** in the corresponding gateway definition:

```
Bridge mysensors:bridge-eth:gateway [ ipAddress="127.0.0.1", tcpPort=5003, sendDelay=200, imperial=true ] {
     /** define things connected to that bridge here */
  }
```

Default is **imperial=false**!

## Configuring things

Assuming you have configured a bridge, the next step is to configure things. We use the place holder in the bridge configuration and fill it with content:

conf/things/demo.things:

```
  Bridge mysensors:bridge-ser:gateway [ serialPort="/dev/pts/2", sendDelay=200 ] {
    humidity        hum01   [ nodeId=172, childId=0 ]
    temperature     temp01  [ nodeId=172, childId=1 ]
        light           light01 [ nodeId=105, childId=0, requestAck=true ]
        light           light02 [ nodeId=105, childId=1, requestAck=true ]
  }
```
  
Now, we've added an humidity sensor with a nodeId of 172 and childId of 0 according to your Arduino sketch, and a temperature sensor with (172,1).

## Configuring items

Now we need the corresponding items:

conf/items/demo.items:

```  
  Number hum01    "Humidity" { channel="mysensors:humidity:gateway:hum01:hum" }
  Number temp01   "Temperature" { channel="mysensors:temperature:gateway:temp01:temp" }
  Switch light01  "Light01" { channel="mysensors:light:gateway:light01:status" }
  Switch light02  "Light02" { channel="mysensors:light:gateway:light02:status" }
  
```

In the channel configuration:

```
  mysensors: name of the binding
  humidity: thing type
  gateway: the bridge
  hum01: thing connected to the bridge
  hum: channel (there is at least one channel per thing)
```

If you want to know which things are supported and which channels they could listen to, have a look at: <https://github.com/tobof/openhab2-addons/blob/MySensors/addons/binding/org.openhab.binding.mysensors/ESH-INF/thing/thing-types.xml>

## Configuring a sitemap
  
Last but not least we create or modify our sitemap:

conf/sitemaps/demo.sitemap:

```
  sitemap demo label="Main Menu" { 
    Frame { 
        Text item=hum01
        Text item=temp01 
                Switch item=light01
                Switch item=light02
    } 
    
  }
```

## Enable ACK request for things

Only tested with light/status yet!

If you would like to receive an ACK for a command sent to an actuator add the requestAck option:

```
Bridge mysensors:bridge-ser:gateway [ serialPort="/dev/pts/6", sendDelay=200 ] {
    light           light01     [ nodeId=172, childId=2, requestAck=true ]
  }
```

If a message is NOT acknowledged by a node the binding retries to send the message five times. If no ACK is received after that the binding will try to revert the item to its old state. This only works, if a state is known (not NULL, for example at startup).

If the binding should not try to revert to the old status if no ACK was received use the option **revertState=false**.

## Enable smart sleep for a node

In the MySensors network smart sleep may be used for nodes that are mostly sleeping. As a controller is not able to send a command to a sleeping node the node may activate smart sleep. If smart sleep is enabled a I_HEARTBEAT_RESPONSE message is send to the controller. The node will wait a short time (default 500 ms) if a message is received from the controller.

If smart sleep is enabled in the binding and a message is send by an item (button activated / light on, text send ...) the message is stored in a separate queue. If a I_HEARTBEAT_RESPONSE message is received from the node the queued message is send immediately.

Configuration in *.things:

```
light workLight01 [ nodeId=104, childId=2, smartSleep=true ]
```

The log output should look like:

```
20:36:41.093 [INFO ] [smarthome.event.ItemCommandEvent    ] - Item 'workLight01' received command OFF
20:36:41.095 [INFO ] [marthome.event.ItemStateChangedEvent] - workLight01 changed from ON to OFF
20:37:15.089 [DEBUG] [rs.internal.protocol.MySensorsReader] - 104;255;3;0;22;1251640
20:37:15.089 [DEBUG] [col.serial.MySensorsSerialConnection] - I_HEARTBEAT_RESPONSE received from 104.
20:37:15.089 [DEBUG] [col.serial.MySensorsSerialConnection] - Message for nodeId: 104 in queue needs to be send immediately!
20:37:15.089 [DEBUG] [rs.internal.protocol.MySensorsWriter] - Sending to MySensors: 104;2;1;0;2;0
```

## Network Sanity Check

The sanity check tries to ensure if the connection to the MySensors bridge is still alive. It sends an I_VERSION message to the gateway and expects an answer. If no answer from the MySensors gateway is received the binding will try to reconnect to the gateway to establish a connection. The default is **enableNetworkSanCheck=false**, the sanity check is disabled. 

To enable the sanity check insert:

```
Bridge mysensors:bridge-ser:gateway [ serialPort="/dev/pts/6", sendDelay=200, enableNetworkSanCheck=true ] {
...    
}
```

# Examples

Definition in **.things**:

```
// SensebenderMicro
humidity bathHum01 [ nodeId=101, childId=0 ]
temperature bathTemp01 [ nodeId=101, childId=1 ]
humidity bathBat01 [ nodeId=101, childId=255 ]

// Light / Relay
light childLight01 [ nodeId=107, childId=0 ]

// Motion
motion gardenMotion01 [ nodeId=110, childId=3 ]

// RollerShutter / Cover
cover kitchenShutter02 [ nodeId=0, childId=0 ]

// Power
power corridorLight01watt [ nodeId=0, childId=7 ]

// Text
text v_text_test [ nodeId=123, childId=123 ]

// IR Send & Receive
irSend          ir_test_send        [ nodeId=111, childId=112 ]
irReceive       ir_test_receive     [ nodeId=111, childId=111 ]
```

Definition in **.items**:

```
// SensebenderMicro
Number bathTemp01 "Temp. Bad [%.1f °C]" <temperature> (gTemp,gHumAndTemp,gBath) { channel="mysensors:temperature:MySGW:bathTemp01:temp" }
Number bathHum01 "Feucht. Bad [%.1f %%]" <humidity> (gHumidity,gHumAndTemp,gBath) { channel="mysensors:humidity:MySGW:bathHum01:hum" }
Number bathBat01 "Battery Bad [%.1f %%]" <battery> (gBattery,gBath) { channel="mysensors:humidity:MySGW:bathBat01:battery" }

// Light / Relay
Switch childLight01 "Deckenleuchte LED 1" (gChild) { channel="mysensors:light:MySGW:childLight01:status" }

// Motion
Contact gardenMotion01 "Bewegung Garten" (gGarden) { channel="mysensors:motion:MySGW:gardenMotion01:tripped" }

// RollerShutter / Cover
Rollershutter kitchenShutter02 "Rollade Küche Tür Ost" (gShutterGroup,gKitchen) { channel="mysensors:cover:MySGWKeller:kitchenShutter02:cover" }

// Power
Number corridorLight01watt "Power [%.1f Watt]" <status> (gCorridor,gPower) { channel="mysensors:power:MySGWKeller:corridorLight01watt:watt" }

// Last Update
DateTime   lastUpdate01 "Last Update" { channel="mysensors:baro:gateway:baro01:lastupdate" }

// Text
String v_text_test "v text test" { channel="mysensors:text:gateway:v_text_test:text" }

// IR Send & Receive
String ir_test_send "ir test send"          { channel="mysensors:irSend:gateway:ir_test_send:irSend" }
String ir_test_receive "ir test receive"    { channel="mysensors:irReceive:gateway:ir_test_receive:irReceive" }
```

Definition in **.sitemap**:

```
// Text
Switch item=v_text_test mappings=["text to send"="SEND"] // Send something to the MySensors network
Text   item=v_text_test // Read the current status/content

// IR Send & Receive
// both channels work in both directions in OH2!!
Switch item=ir_test_send mappings=["ir test send"="SEND"]
Text   item=ir_test_send
Switch item=ir_test_receive mappings=["ir test receive"="SEND"]
Text   item=ir_test_receive
```

# Expert mode (rule based)

In some rare cases you may want to directly work with the message received from the MySensors Message. The message format is documented here: https://www.mysensors.org/download/serial_api_20

It is possible to create a thing that receives *ALL* MySensors Messages (not the debug output!) that were received by the binding. It is not relevant if there already is a thing, that receives and reads this message.

This special thing has a fixed node Id of 999 and child Id of 999 which are not used by the MySensors network itself.

Create a thing in *.things:

```
mySensorsMessage mySMsg01   [ nodeId=999, childId=999 ]
```

Create an item in *.items:

```
String mySMsg01  "MySensorsMessage"   { channel="mysensors:mySensorsMessage:gateway:mySMsg01:mySensorsMessage" }
```

Now you are good to go! If you receive a message it may look like this:

```
2016-09-19 20:32:37.190 [DEBUG] [o.b.m.protocol.MySensorsReader:62   ] - 172;0;1;0;1;87
2016-09-19 20:32:37.212 [INFO ] [smarthome.event.ItemStateEvent:43   ] - hum01 updated to 87
2016-09-19 20:32:37.215 [INFO ] [smarthome.event.ItemStateEvent:43   ] - mySMsg01 updated to 172;0;1;0;1;87
2016-09-19 20:32:37.246 [INFO ] [s.event.ItemStateChangedEvent :43   ] - mySMsg01 changed from 172;255;3;0;12;1.0 to 172;0;1;0;1;87
```

So the item hum01 (humidity sensor) now contains the value 87, while mySMsg01 contains the complete MySensors message. As you may see the thing hum01 is not needed but it does no harm.

In a rule you may now do what you want with the string.

Sending a message via a rule is also possible.

In this example I have a switch light01 to trigger the sending.

```
rule "Send a message to the MySensorsMessage"
    when
        Item light01 received command
    then
        sendCommand(mySMsg01, "1;2;3;4;5;ipsumlorum");
end
```

Please be aware that this is *EXPERT* mode. If the syntax of the message is not correct the parser of the binding will throw an exception.
