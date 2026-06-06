# Xtend Examples

## Secure connection

In a first example a very secure connection to a broker is defined. It pins the returned certificate and public key.
If someone tries a man in the middle attack later on, this broker connection will recognize it and refuse a connection.
Be aware that if your brokers certificate changes, you need to remove the connection entry and add it again.

`mqttConnections.things`:

```java
mqtt:broker:mySecureBroker [ host="192.168.0.41", secure=true, certificatepin=true, publickeypin=true ]
```

## Plain, unsecured connection

The second connection is a plain, unsecured one. Unsecure connections are default, if you do not provide the "secure" parameter. Use this only for local MQTT Brokers.

`mqttConnections.things`:

```java
mqtt:broker:myUnsecureBroker [ host="192.168.0.42", secure=false ]
```

## Authentication with username and password

A third connection uses a username and password for authentication.
Secure is set to false in this example. This is a bad idea!
The credentials are plain values on the wire, therefore you should only use this on a secure connection.

`mqttConnections.things`:

```java
mqtt:broker:myAuthentificatedBroker [ host="192.168.0.43", secure=false, username="user", password="password" ]
```

## Public key pinning

In a fourth connection, the public key pinning is enabled again.
This time, a public key hash is provided to pin the connection to a specific server.
It follows the form "hashname:hashvalue". Valid _hashnames_ are SHA-1, SHA-224, SHA-256, SHA-384, SHA-512 and all others listed
in [Java MessageDigest Algorithms](https://docs.oracle.com/en/java/javase/21/docs/specs/security/standard-names.html#messagedigest-algorithms).

`mqttConnections.things`:

```java
mqtt:broker:pinToPublicKey [ host="192.168.0.44", secure=true, publickeypin=true, publickey="SHA-256:9a6f30e67ae9723579da2575c35daf7da3b370b04ac0bde031f5e1f5e4617eb8" ]
```

### A broker Thing with a Generic MQTT Thing and a few channels

demo1.things:

```java
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

demo2.things:

```java
Bridge mqtt:broker:WorkBroker "Work Broker" [ host="localhost", port="1883", secure=false, username="openhabian", password="ohmqtt", clientID="WORKOPENHAB24" ]

Thing mqtt:topic:WorkBroker:WorkSonoff "Work Sonoff" (mqtt:broker:WorkBroker) @ "Home" {
    Channels:
        Type switch : WorkLight "Work Light" [ stateTopic="stat/worklight/POWER", commandTopic="cmnd/worklight/POWER" ]
        Type switch : WorkLightTele "Work Tele" [ stateTopic="tele/worklight/STATE", transformationPattern="JSONPATH:$.POWER" ]
}
```

tasmota.things: Example of a Tasmota Device with Availablity-Topic state and standard Online/Offline message-payload

```java
Bridge mqtt:broker:mybroker [ host="192.168.0.42", secure=false ]
{
    Thing mqtt:topic:SP111 "SP111" [availabilityTopic="tele/tasmota/LWT", payloadAvailable="Online", payloadNotAvailable="Offline"]{
    Channels:
        Type switch : power "Power" [ stateTopic="tele/tasmota/STATE", commandTopic="cmnd/tasmota/POWER", transformationPattern="JSONPATH:$.POWER", on="ON", off="OFF" ]
        Type number : powerload "Power load"             [ stateTopic="tele/tasmota/SENSOR", transformationPattern="JSONPATH:$.ENERGY.Power"]
        Type number : voltage   "Line voltage"           [ stateTopic="tele/tasmota/SENSOR", transformationPattern="JSONPATH:$.ENERGY.Voltage"]
        Type number : current   "Line current"           [ stateTopic="tele/tasmota/SENSOR", transformationPattern="JSONPATH:$.ENERGY.Current"]
        Type number : total     "Total energy today"     [ stateTopic="tele/tasmota/SENSOR", transformationPattern="JSONPATH:$.ENERGY.Today"]
        Type number : totalyest "Total energy yesterday" [ stateTopic="tele/tasmota/SENSOR", transformationPattern="JSONPATH:$.ENERGY.Yesterday"]
        Type number : rssi      "WiFi Signal Strength"   [ stateTopic="tele/tasmota/STATE", transformationPattern="JSONPATH:$.Wifi.RSSI"]
       }
}
```

When using .things and .items files for configuration, items and channels follow the format of:

```java
<ITEM-TYPE> <ITEM-NAME> "<FRIENDLY-NAME>" { channel="mqtt:topic:<BROKER-NAME>:<THING-NAME>:<CHANNEL-NAME>" }
```

demo1.items:

```java
Switch Kitchen_Light "Kitchen Light" { channel="mqtt:topic:myUnsecureBroker:mything:lamp" }
Rollershutter shutter "Blind" { channel="mqtt:topic:myUnsecureBroker:mything:blind" }
```

demo2.items:

```java
Switch SW_WorkLight "Work Light Switch" { channel="mqtt:topic:WorkBroker:WorkSonoff:WorkLight", channel="mqtt:topic:WorkBroker:WorkSonoff:WorkLightTele" }
```

### Publish an MQTT value on startup

An example "demo.rules" rule to publish to `system/started` with the value `true` on every start:

```java
rule "Send startup message"
when
  System started
then
  val actions = getActions("mqtt","mqtt:broker:myUnsecureBroker")
  actions.publishMQTT("system/started","true")    
end
```

### Synchronize two instances

To synchronize item items from a SOURCE openHAB instance to a DESTINATION instance, do the following:

Define a broker and a trigger channel for your DESTINATION openHAB installation (`thing` file):

```java
Bridge mqtt:broker:myUnsecureBroker [ host="192.168.0.42", secure=false ]
{
    Channels:
        Type publishTrigger : myTriggerChannel "Receive everything" [ stateTopic="allItems/#", separator="#" ]
}
```

The trigger channel will trigger for each received message on the MQTT topic "allItems/".
Now push those changes to your items in a `rules` file:

```java
rule "Receive all"
when 
      Channel "mqtt:broker:myUnsecureBroker:myTriggerChannel" triggered
then 
    //The receivedEvent String contains unneeded elements like the mqtt topic, we only need everything after the "/" as this is were item name and state are
    val parts1 = receivedEvent.toString.split("/").get(1)
    val parts2 = parts1.split("#")
    sendCommand(parts2.get(0), parts2.get(1))
end
```

On your SOURCE openHAB installation, you need to define a group `myGroupOfItems` and add all items
to it that you want to synchronize. Then add this rule to a `rule` file:

```java
rule "Publish all"
when 
      Member of myGroupOfItems changed
then
   val actions = getActions("mqtt","mqtt:broker:myUnsecureBroker")
   actions.publishMQTT("allItems/"+triggeringItem.name,triggeringItem.state.toString)
end
```

## Converting an MQTT1 installation

The conversion is straight forward, but need to be done for each item.
You do not need to convert everything in one go. MQTT1 and MQTT2 can coexist.

> For mqtt1 make sure you have enabled the Legacy 1.x repository and installed "mqtt1".

### 1 Command / 1 State topic

Assume you have this item:

```java
Switch ExampleItem "Heatpump Power" { mqtt=">[mosquitto:heatpump/set:command:*:DEFAULT)],<[mosquitto:heatpump:JSONPATH($.power)]" }
```

This converts to an entry in your *.things file with a **Broker Thing** and a **Generic MQTT Thing** that uses the bridge:

```java
Bridge mqtt:broker:myUnsecureBroker [ host="192.168.0.42", secure=false ]
{
    Thing topic mything "My Thing" {
    Channels:
        Type switch : heatpumpChannel "Heatpump Power" [ stateTopic="heatpump", commandTopic="heatpump/set", transformationPattern="JSONPATH:$.power" ]
    }
}
```

Add as many channels as you have items and add the _stateTopic_ and _commandTopic_ accordingly.

Your items change to:

```java
Switch ExampleItem "Heatpump Power" { channel="mqtt:topic:myUnsecureBroker:mything:heatpumpChannel" }
```

### 1 Command / 2 State topics

If you receive updates from two different topics, you need to create multiple channels now, 1 for each MQTT receive topic.

```java
Switch ExampleItem "Heatpump Power" { mqtt=">[mosquitto:heatpump/set:command:*:DEFAULT)],<[mosquitto:heatpump/state1:state:*:DEFAULT,<[mosquitto:heatpump/state2:state:*:DEFAULT" }
```

This converts to:

```java
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

```java
Switch ExampleItem "Heatpump Power" { channel="mqtt:topic:myUnsecureBroker:mything:heatpumpChannel",
                                      channel="mqtt:topic:myUnsecureBroker:mything:heatpumpChannel2" }
```
