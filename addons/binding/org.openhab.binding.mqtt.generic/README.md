# MQTT Generic Thing Binding

> MQTT is a machine-to-machine (M2M)/"Internet of Things" connectivity protocol. It was designed as an extremely lightweight publish/subscribe messaging transport.

This binding allows to link MQTT topics to Things.

## Supported Things

There a few Things dedicated to MQTT conventions available and a Generic MQTT Thing.
The last one is comparable to what was found in the mqtt 1.x binding. 

### Homie Thing

Devices that follow the [Homie convention](https://homieiot.github.io/) 3.x and better
are auto-discovered and represented by this Homie Thing.

Find the next table to understand the topology mapping from Homie to the Framework: 

| Homie    | Framework     | Example MQTT topic                 |
|----------|---------------|------------------------------------|
| Device   | Thing         | homie/super-car                    |
| Node     | Channel Group | homie/super-car/engine             |
| Property | Channel       | homie/super-car/engine/temperature |

System trigger channels are supported using non-retained properties, with *enum* data type and with the following formats:
* Format: "PRESSED,RELEASED" -> system.rawbutton
* Format: "SHORT\_PRESSED,DOUBLE\_PRESSED,LONG\_PRESSED" -> system.button
* Format: "DIR1\_PRESSED,DIR1\_RELEASED,DIR2\_PRESSED,DIR2\_RELEASED" -> system.rawrocker

### HomeAssistant Thing

HomeAssistant MQTT Components are recognised as well. The base topic needs to be **homeassistant**. 
The mapping is structured like this:


| HA MQTT               | Framework     | Example MQTT topic                 |
|-----------------------|---------------|------------------------------------|
| Object                | Thing         | homeassistant/../../object         |
| Component+Node        | Channel Group | homeassistant/component/node/object|
| -> Component Features | Channel       | state/topic/defined/in/comp/config |

### Generic MQTT Thing

A generic MQTT Thing has no configuration and is a pure shell for channels that you add yourself.

You can manually add the following channels:

#### Supported Channels

* **string**: This channel can show the received text on the given topic and can send text to a given topic.
* **number**: This channel can show the received number on the given topic and can send a number to a given topic. It can have a min, max and step values.
* **dimmer**: This channel handles numeric values as percentages. It can have min, max and step values.
* **contact**: This channel represents a open/close state of a given topic.
* **switch**: This channel represents a on/off state of a given topic and can send an on/off value to a given topic.
* **colorRGB**: This channel handles color values in RGB format.
* **colorHSB**: This channel handles color values in HSB format.
* **location**: This channel handles a location.
* **image**: This channel handles binary images in common java supported formats (bmp,jpg,png).
* **datetime**: This channel handles date/time values.
* **rollershutter**: This channel is for rollershutters.

## Thing and Channel Configuration

All things require a configured broker.

### Common Channel Configuration Parameters

* __stateTopic__: The MQTT topic that represents the state of the thing. This can be empty, the thing channel will be a state-less trigger then. You can use a wildcard topic like "sensors/+/event" to retrieve state from multiple MQTT topics. 
* __transformationPattern__: An optional transformation pattern like [JSONPath](http://goessner.net/articles/JsonPath/index.html#e2) that is applied to all incoming MQTT values.
* __transformationPatternOut__: An optional transformation pattern like [JSONPath](http://goessner.net/articles/JsonPath/index.html#e2) that is applied before publishing a value to MQTT.
* __commandTopic__: The MQTT topic that commands are send to. This can be empty, the thing channel will be read-only then. Transformations are not applied for sending data.
* __formatBeforePublish__: Format a value before it is published to the MQTT broker. The default is to just pass the channel/item state. If you want to apply a prefix, say "MYCOLOR,", you would use "MYCOLOR,%s". If you want to adjust the precision of a number to for example 4 digits, you would use "%.4f".
* __postCommand__: If the received MQTT value should not only update the state of linked items, but command them, enable this option. You usually need this enabled if your item is also linked to another channel, say a KNX actor, and you want a received MQTT payload to command that KNX actor. 
* __retained__: The value will be published to the command topic as retained message. A retained value stays on the broker and can even be seen by MQTT clients that are subscribing at a later point in time. 
* __trigger__: If true, the state topic will not update a state, but trigger a channel instead.

### Channel Type "string"

* __allowedStates__: An optional comma separated list of allowed states. Example: "ONE,TWO,THREE"

You can connect this channel to a String item.

### Channel Type "number"

* __min__: An optional minimum value.
* __max__: An optional maximum value.
* __step__: For decrease, increase commands the step needs to be known

A decimal value (like 0.2) is send to the MQTT topic if the number has a fractional part.
If you always require an integer, please use the formatter.

You can connect this channel to a Number item.

### Channel Type "dimmer"
 
* __on__: A optional string (like "ON"/"Open") that is recognised as minimum.
* __off__: A optional string (like "OFF"/"Close") that is recognised as maximum.
* __min__: A required minimum value.
* __max__: A required maximum value.
* __step__: For decrease, increase commands the step needs to be known

The value is internally stored as a percentage for a value between **min** and **max**.

The channel will publish a value between `min` and `max`.

You can connect this channel to a Rollershutter or Dimmer item.

### Channel Type "contact", "switch"

* __on__: A optional number (like 1, 10) or a string (like "ON"/"Open") that is recognised as on/open state.
* __off__: A optional number (like 0, -10) or a string (like "OFF"/"Close") that is recognised as off/closed state.

The contact channel by default recognises `"OPEN"` and `"CLOSED"`. You can connect this channel to a Contact item.
The switch channel by default recognises `"ON"` and `"OFF"`. You can connect this channel to a Switch item.

If **on** and **off** are not configured it publishes the strings mentioned before respectively.

You can connect this channel to a Contact or Switch item.

### Channel Type "colorRGB", "colorHSB"

* __on__: An optional string (like "BRIGHT") that is recognised as on state. (ON will always be recognised.)
* __off__: An optional string (like "DARK") that is recognised as off state. (OFF will always be recognised.)
* __onBrightness__: If you connect this channel to a Switch item and turn it on,
color and saturation are preserved from the last state, but
the brightness will be set to this configured initial brightness (default: 10%).

You can connect this channel to a Color, Dimmer and Switch item.

This channel will publish the color as comma separated list to the MQTT broker,
e.g. "112,54,123" for an RGB channel (0-255 per component) and "360,100,100" for a HSB channel (0-359 for hue and 0-100 for saturation and brightness).

The channel expects values on the corresponding MQTT topic to be in this format as well.

### Channel Type "location"

You can connect this channel to a Location item.

The channel will publish the location as comma separated list to the MQTT broker,
e.g. "112,54,123" for latitude, longitude, altitude. The altitude is optional. 

The channel expects values on the corresponding MQTT topic to be in this format as well. 

### Channel Type "image"

You can connect this channel to an Image item. This is a read-only channel.

The channel expects values on the corresponding MQTT topic to contain the binary
data of a bmp, jpg, png or any other format that the installed java runtime supports. 

### Channel Type "datetime"

You can connect this channel to a DateTime item.

The channel will publish the date/time in the format "yyyy-MM-dd'T'HH:mm"
for example 2018-01-01T12:14:00. If you require another format, please use the formatter.

The channel expects values on the corresponding MQTT topic to be in this format as well. 

### Channel Type "rollershutter"

* __on__: An optional string (like "Open") that is recognised as UP state.
* __off__: An optional string (like "Close") that is recognised as DOWN state.
* __stop__: An optional string (like "Stop") that is recognised as STOP state.

You can connect this channel to a Rollershutter or Dimmer item.

## Rule Actions

This binding includes a rule action, which allows to publish MQTT messages from within rules.
There is a separate instance for each MQTT broker (i.e. bridge), which can be retrieved through

```
val mqttActions = getActions("mqtt","mqtt:systemBroker:embedded-mqtt-broker")
```

where the first parameter always has to be `mqtt` and the second (`mqtt:systemBroker:embedded-mqtt-broker`) is the Thing UID of the broker that should be used.
Once this action instance is retrieved, you can invoke the `publishMQTT(String topic, String value)` method on it:

```
mqttActions.publishMQTT("mytopic","myvalue")
```

## Limitations

* The HomeAssistant Fan Components only support ON/OFF.
* The HomeAssistant Cover Components only support OPEN/CLOSE/STOP.
* The HomeAssistant Light Component does not support XY color changes.
* The HomeAssistant Climate Components is not yet supported.

## Incoming Value Transformation

All mentioned channels allow an optional transformation for incoming MQTT topic values.

This is required if your received value is wrapped in a JSON or XML response.

Here are a few examples to unwrap a value from a complex response:

| Received value                                                      | Tr. Service | Transformation                            |
|---------------------------------------------------------------------|-------------|-------------------------------------------|
| `{device: {status: { temperature: 23.2 }}}`                         | JSONPATH    | `JSONPATH:$.device.status.temperature`    |
| `<device><status><temperature>23.2</temperature></status></device>` | XPath       | `XPath:/device/status/temperature/text()` |
| `THEVALUE:23.2°C`                                                   | REGEX       | `REGEX::(.*?)°`                           |

Transformations can be chained by separating them with the mathematical intersection character "∩".

## Outgoing Value Transformation

All mentioned channels allow an optional transformation for outgoing values.
Please prefer formatting as described in the next section whenever possible.

## Format before Publish

This feature is quite powerful in transforming an item state before it is published to the MQTT broker.
It has the syntax: `%[flags][width]conversion`.
Find the full documentation on the [Java](https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html) web page.

The default is "%s" which means: Output the item state as string.

Here are a few examples:

* All uppercase: "%S". Just use the upper case letter for the conversion argument.
* Apply a prefix: "myprefix%s"
* Apply a suffix: "%s suffix"
* Number precision: ".4f" for a 4 digit precision. Use the "+" flag to always add a sign: "+.4f".
* Decimal to Hexadecimal/Octal/Scientific: For example "60" with "%x", "%o", "%e" becomes "74", "3C", "60".
* Date/Time: To reference the item state multiple times, use "%1$". Use the "tX" conversion where "X" can be any of [h,H,m,M,I,k,l,S,p,B,b,A,a,y,Y,d,e].
  - For an output of *May 23, 1995* use "%1$**tb** %1$**te**,%1$**tY**".
  - For an output of *23.05.1995* use "%1$**td**.%1$**tm**.%1$**tY**".
  - For an output of *23:15* use "%1$**tH**:%1$**tM**".

## Troubleshooting

* If you get the error "No MQTT client": Please update your installation.
* If you use the Mosquitto broker: Please be aware that there is a relatively low setting
  for retained messages. At some point messages will just not being delivered
  anymore: Change the setting 

## Examples

Have a look at the following textual examples.

### A broker Thing with a Generic MQTT Thing and a few channels 

demo.Things:

```xtend
Bridge mqtt:broker:myUnsecureBroker [ host="192.168.0.42", secure=false ]
{
    Thing topic mything {
    Channels:
        Type switch : lamp "Kitchen Lamp" [ stateTopic="lamp/enabled", commandTopic="lamp/enabled/set" ]
        Type switch : fancylamp "Fancy Lamp" [ stateTopic="fancy/lamp/state", commandTopic="fancy/lamp/command", on="i-am-on", off="i-am-off" ]
        Type string : alarmpanel "Alarm system" [ stateTopic="alarm/panel/state", commandTopic="alarm/panel/set", allowedStates="ARMED_HOME,ARMED_AWAY,UNARMED" ]
        Type color : lampcolor "Kitchen Lamp color" [ stateTopic="lamp/color", commandTopic="lamp/color/set", rgb=true ]
        Type dimmer : blind "Blind" [ stateTopic="blind/state", commandTopic="blind/set", min=0, max=5, step=1 ]
    }
}
```

demo.items:

Generic configuration .items file (when using .things file to define your broker and things channels):
```xtend
<ITEM-TYPE> <ITEM-NAME> "<FRIENDLY-NAME>" { channel="mqtt:topic:<BROKER-NAME>:<THING-NAME>:<CHANNEL-NAME>" }
```

```xtend
Switch Kitchen_Light "Kitchen Light" { channel="mqtt:topic:myUnsecureBroker:mything:lamp" }
Rollershutter shutter "Blind" { channel="mqtt:topic:myUnsecureBroker:mything:blind" }
```

### Publish an MQTT value on startup

An example "demo.rules" rule to publish to `system/started` with the value `true` on every start:

```xtend
rule "Send startup message"
when
  System started
then
  val actions = getActions("mqtt","mqtt:broker:myUnsecureBroker")
  actions.publishMQTT("system/started","true")    
end
```

### Synchronise two instances

Define a broker and a trigger channel on that broker in a "demo.Things" file:

```xtend
Bridge mqtt:broker:myUnsecureBroker [ host="192.168.0.42", secure=false ]
{
    Channels:
        Type publishTrigger : myTriggerChannel "Receive everything" [ stateTopic="allItems/#", separator="#" ]
}
```

If you want to publish all item changes to an MQTT topic "allItems/",
group items into a `myGroupOfItems` and do this in a "publishAll.rules" file:

```xtend
rule "Publish all"
when 
      Member of myGroupOfItems changed
then
   val actions = getActions("mqtt","mqtt:broker:myUnsecureBroker")
   actions.publishMQTT("allItems/"+triggeringItem.name,triggeringItem.state)
end
```

If you want to receive all item changes from an MQTT topic "allItems/",
do this in a "ReceiveAll.rules" file:

```xtend
rule "Publish all"
when 
      Channel "mqtt:broker:myUnsecureBroker:myTriggerChannel" triggered
then
   // TODO
end
```

## Converting an MQTT1 installation

The conversion is straight forward, but need to be done for each item.
You do not need to convert everything in one go. MQTT1 and MQTT2 can coexist.

> For mqtt1 make sure you have enabled the Legacy 1.x repository and installed "mqtt1".

### 1 Command / 1 State topic 

Assume you have this item:

```xtend
Switch ExampleItem "Heatpump Power" { mqtt=">[mosquitto:heatpump/set:command:*:DEFAULT)],<[mosquitto:heatpump:JSONPATH($.power)]" }
```

This converts to an entry in your *.things file with a **Broker Thing** and a **Generic MQTT Thing** that uses the bridge:

```xtend
Bridge mqtt:broker:myUnsecureBroker [ host="192.168.0.42", secure=false ]
{
    Thing topic mything "My Thing" {
    Channels:
        Type switch : heatpumpChannel "Heatpump Power" [ stateTopic="heatpump", commandTopic="heatpump/set", transformationPattern="JSONPATH:$.power" ]
    }
}
```

Add as many channels as you have items and add the *stateTopic* and *commandTopic* accordingly. 

Your items change to:

```xtend
Switch ExampleItem "Heatpump Power" { channel="mqtt:topic:myUnsecureBroker:mything:heatpumpChannel" }
```


### 1 Command / 2 State topics 

If you receive updates from two different topics, you need to create multiple channels now, 1 for each MQTT receive topic.

```xtend
Switch ExampleItem "Heatpump Power" { mqtt=">[mosquitto:heatpump/set:command:*:DEFAULT)],<[mosquitto:heatpump/state1:state:*:DEFAULT",<[mosquitto:heatpump/state2:state:*:DEFAULT" }
```

This converts to:

```xtend
Bridge mqtt:broker:myUnsecureBroker [ host="192.168.0.42", secure=false ]
{
    Thing topic mything "My Thing" {
    Channels:
        Type switch : heatpumpChannel "Heatpump Power" [ stateTopic="heatpump/state1", commandTopic="heatpump/set" ]
        Type switch : heatpumpChannel2 "Heatpump Power" [ stateTopic="heatpump/state2" ]
    }
}
```

Link both channels to one item. That item will publish to "heatpump/set" on a change and
receive values from "heatpump/state1" and "heatpump/state2".

```xtend
Switch ExampleItem "Heatpump Power" { channel="mqtt:topic:myUnsecureBroker:mything:heatpumpChannel",
                                      channel="mqtt:topic:myUnsecureBroker:mything:heatpumpChannel2" }
```
