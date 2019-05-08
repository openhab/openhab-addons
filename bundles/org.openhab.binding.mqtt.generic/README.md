# MQTT Things and Channels Binding

MQTT is one of the most commonly used protocols in IoT (Internet of Things) projects. It stands for Message Queuing Telemetry Transport.

It is designed as a lightweight messaging protocol that uses publish/subscribe operations to exchange data between clients and the server.

![MQTT Architecture](doc/mqtt.jpg)

MQTT servers are called brokers and the clients are simply the connected devices.

* When a device (a client) wants to send data to the broker, we call this operation a “publish”.
* When a device (a client) wants to receive data from the broker, we call this operation a “subscribe”.


![Publish and Subscribe](doc/subpub.png)

openHAB itself is not an MQTT Broker and needs to connect to one as a regular client.
Therefore you must have configured a *Broker Thing* first via the **MQTT Broker Binding**!

## MQTT Topics

If a client subscribes to a broker, it is certainly not interested in all published messages.
Instead it subscribes to specific **topics**. A topic can look like this: "mydevice/temperature".

Example:

Let's assume there is an MQTT capable light bulb.

It has a unique id amongst all light bulbs, say "device123". The manufacturer decided to accept new
brightness values on "device123/brightness/set". In openHAB we call that a **command topic**.

And now assume that we have a mobile phone (or openHAB itself) and we register with the MQTT broker,
and want to retrieve the current brightness value. The manufacturer specified that this value can
be found on "device123/brightness". In openHAB we call that a **state topic**.

This pattern is very common, that you have a command and a state topic. A sensor would only have a state topic,
naturally.

Because every manufacturer can device on his own on which topic his devices publish, this
binding can unfortunately not provide any auto-discovery means.

If you use an open source IoT device, the chances are high,
that it has the MQTT convention Homie or HomeAssistant implemented. Those conventions specify the topic
topology and allow auto discovery. Please have a look at the specific openHAB bindings.  
 
## Supported Things

Because of the very generic structure of MQTT, this binding allows you to add an arbitrary number
of so called "Generic MQTT Things" to organize yourself.

On each of those things you can add an arbitrary number of channels.

Remember that you need a configured broker Thing first!

You can add the following channels:

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

## Channel Configuration

* __stateTopic__: The MQTT topic that represents the state of the thing. This can be empty, the thing channel will be a state-less trigger then. You can use a wildcard topic like "sensors/+/event" to retrieve state from multiple MQTT topics. 
* __transformationPattern__: An optional transformation pattern like [JSONPath](http://goessner.net/articles/JsonPath/index.html#e2) that is applied to all incoming MQTT values.
* __transformationPatternOut__: An optional transformation pattern like [JSONPath](http://goessner.net/articles/JsonPath/index.html#e2) that is applied before publishing a value to MQTT.
* __commandTopic__: The MQTT topic that commands are send to. This can be empty, the thing channel will be read-only then. Transformations are not applied for sending data.
* __formatBeforePublish__: Format a value before it is published to the MQTT broker. The default is to just pass the channel/item state. If you want to apply a prefix, say "MYCOLOR,", you would use "MYCOLOR,%s". If you want to adjust the precision of a number to for example 4 digits, you would use "%.4f".
* __postCommand__: If `true`, the received MQTT value will not only update the state of linked items, but command it.
  The default is `false`.
  You usually need this to be `true` if your item is also linked to another channel, say a KNX actor, and you want a received MQTT payload to command that KNX actor. 
* __retained__: The value will be published to the command topic as retained message. A retained value stays on the broker and can even be seen by MQTT clients that are subscribing at a later point in time. 
* __trigger__: If `true`, the state topic will not update a state, but trigger a channel instead.

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
 
* __on__: A optional string (like "ON"/"Open") that is recognized as minimum.
* __off__: A optional string (like "OFF"/"Close") that is recognized as maximum.
* __min__: A required minimum value.
* __max__: A required maximum value.
* __step__: For decrease, increase commands the step needs to be known

The value is internally stored as a percentage for a value between **min** and **max**.

The channel will publish a value between `min` and `max`.

You can connect this channel to a Rollershutter or Dimmer item.

### Channel Type "contact", "switch"

* __on__: A optional number (like 1, 10) or a string (like "ON"/"Open") that is recognized as on/open state.
* __off__: A optional number (like 0, -10) or a string (like "OFF"/"Close") that is recognized as off/closed state.

The contact channel by default recognizes `"OPEN"` and `"CLOSED"`. You can connect this channel to a Contact item.
The switch channel by default recognizes `"ON"` and `"OFF"`. You can connect this channel to a Switch item.

If **on** and **off** are not configured it publishes the strings mentioned before respectively.

You can connect this channel to a Contact or Switch item.

### Channel Type "colorRGB", "colorHSB"

* __on__: An optional string (like "BRIGHT") that is recognized as on state. (ON will always be recognized.)
* __off__: An optional string (like "DARK") that is recognized as off state. (OFF will always be recognized.)
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

* __on__: An optional string (like "Open") that is recognized as UP state.
* __off__: An optional string (like "Close") that is recognized as DOWN state.
* __stop__: An optional string (like "Stop") that is recognized as STOP state.

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
for retained messages. At some point messages will just not being delivered anymore: 
Change the setting 
