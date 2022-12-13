# Enigma2 Binding

The binding integrates Enigma2 devices.

## Supported Things

### Enigma2 devices

Enigma2 based set-top boxes with an installed OpenWebIf are supported.

#### Device Settings

The Device must be connected to the same network as openHAB.

## Discovery

Devices are auto discovered through HTTP in the local network.

If automatic discovery is not possible you may still manually configure a device based on the hostname.

## Thing Configuration

Enigma2 has the following configuration parameters:

| Name            | Description                                        | Mandatory |
|-----------------|----------------------------------------------------|-----------|
| host            | Hostname or IP address of the Enigma2 device       | yes       |
| refreshInterval | The refresh interval in seconds                    | yes       |
| timeout         | The timeout for reading from the device in seconds | yes       |
| user            | Optional: The Username of the Enigma2 Web API      | no        |
| password        | Optional: The Password of the Enigma2 Web API      | no        |

### Configuration in .things file

Set the parameters as in the following example:

```java
Thing enigma2:device:192_168_0_3 [host="192.168.1.3", refreshInterval="5", timeout="5", user="usename" , password="***"]
```

## Channels

| Channel Type ID | Item Type | Description                                                                                                                                                                                                             | Read/Write |
|-----------------|-----------|----------------------------------------------------------------------------------------------|------------|
| power           | Switch    | Current power setting.                                                                       | RW         |
| mute            | Switch    | Current mute setting.                                                                        | RW         |
| volume          | Dimmer    | Current volume setting.                                                                      | RW         |
| channel         | String    | Current channel. Use only the channel text as command to update the channel.                 | RW         |
| title           | String    | Current program title of the current channel.                                                | R          |
| description     | String    | Current program description of the current channel.                                          | R          |
| mediaPlayer     | Player    | Media control player.                                                                        | RW         |
| mediaStop       | Switch    | Media control stop.                                                                          | RW         |
| answer          | String    | Receives an answer to a send question of the device.                                         | R          |

## Example

demo.things:

```java
Thing enigma2:device:192_168_0_3 [host="192.168.1.3", refreshInterval="5"]
```

demo.items:

```java
Switch  Enigma2_Power              "Power: [%s]"          <switch>           { channel="enigma2:device:192_168_0_3:power" }
Dimmer  Enigma2_Volume             "Volume: [%d %%]"      <soundvolume>      { channel="enigma2:device:192_168_0_3:volume" }
Switch  Enigma2_Mute               "Mute: [%s]"           <soundvolume_mute> { channel="enigma2:device:192_168_0_3:mute" }
Switch  Enigma2_Stop               "Stop: [%s]"           <mediacontrol>     { channel="enigma2:device:192_168_0_3:mediaStop", autoupdate="false" }
Player  Enigma2_PlayerControl      "Mode: [%s]"           <mediacontrol>     { channel="enigma2:device:192_168_0_3:mediaPlayer" }
String  Enigma2_Channel            "Channel: [%s]"        <receiver>         { channel="enigma2:device:192_168_0_3:channel" }
String  Enigma2_Title              "Title: [%s]"          <receiver>         { channel="enigma2:device:192_168_0_3:title" }
String  Enigma2_Description        "Description: [%s]"    <receiver>         { channel="enigma2:device:192_168_0_3:description" }
String  Enigma2_Answer             "Answer: [%s]"         <text>             { channel="enigma2:device:192_168_0_3:answer" }
String  Enigma2_RemoteKeys         "[]"                   <receiver>         { autoupdate="false" }
String  Enigma2_SendError          "Error"                <text>             { autoupdate="false" }
String  Enigma2_SendWarning        "Warning"              <text>             { autoupdate="false" }
String  Enigma2_SendInfo           "Info"                 <text>             { autoupdate="false" }
```

demo.sitemap:

```perl
sitemap demo label="Enigma2 Demo"
{
  Frame label="Enigma2" { 
     Switch    item=Enigma2_Power        
     Slider    item=Enigma2_Volume step=5 minValue=0 maxValue=100
     Setpoint  item=Enigma2_Volume step=5 minValue=0 maxValue=100
     Switch    item=Enigma2_Mute
     Default   item=Enigma2_PlayerControl
     Switch    item=Enigma2_Stop mappings=[ON="Stop"]
     Text      item=Enigma2_Channel
     Text      item=Enigma2_Title
     Text      item=Enigma2_Description
  }
  Frame label="Enigma2 Remote" {
     Switch    item=Enigma2_RemoteKeys mappings=[POWER="POWER"]
     Switch    item=Enigma2_RemoteKeys mappings=[TEXT="[=]", SUBTITLE="[_]", MUTE="MUTE"]
     Switch    item=Enigma2_RemoteKeys mappings=[KEY_1="1", KEY_2="2", KEY_3="3"]
     Switch    item=Enigma2_RemoteKeys mappings=[KEY_4="4", KEY_5="5", KEY_6="6"]
     Switch    item=Enigma2_RemoteKeys mappings=[KEY_7="7", KEY_8="8", KEY_9="9"]
     Switch    item=Enigma2_RemoteKeys mappings=[ARROW_LEFT="<", KEY_0="0", ARROW_RIGHT=">"]
     Switch    item=Enigma2_RemoteKeys mappings=[RED="R", GREEN="G", YELLOW="Y", BLUE="B"]
     Switch    item=Enigma2_RemoteKeys mappings=[UP="Up"]
     Switch    item=Enigma2_RemoteKeys mappings=[LEFT="Left", OK="Ok", RIGHT="Right"]
     Switch    item=Enigma2_RemoteKeys mappings=[DOWN="Down"]
     Switch    item=Enigma2_RemoteKeys mappings=[VOLUME_UP="+", EXIT="Exit", CHANNEL_UP="+"]
     Switch    item=Enigma2_RemoteKeys mappings=[VOLUME_DOWN="-", EPG="Epg", CHANNEL_DOWN="-"]
     Switch    item=Enigma2_RemoteKeys mappings=[MENU="Menu", VIDEO="[=R]", AUDIO="Audio", HELP="Help"]
     Switch    item=Enigma2_RemoteKeys mappings=[FAST_BACKWARD="<<", PLAY=">", PAUSE="||", FAST_FORWARD=">>"]
     Switch    item=Enigma2_RemoteKeys mappings=[TV="TV", RECORD="O", STOP="[]", RADIO="Radio"]
     Switch    item=Enigma2_RemoteKeys mappings=[INFO="INFO"]
  }   
  Frame label="Enigma2 Messages" {   
     Switch    item=Enigma2_SendError mappings=[SEND="SEND"]
     Switch    item=Enigma2_SendWarning mappings=[SEND="SEND"]
     Switch    item=Enigma2_SendInfo mappings=[SEND="SEND"]
     Switch    item=Enigma2_SendQuestion mappings=[SEND="SEND"]
     Text      item=Enigma2_Answer
  }
}
```

demo.rules:

```java
rule "Enigma2_KeyS"
when Item Enigma2_RemoteKeys received command
then
   val actions = getActions("enigma2","enigma2:device:192_168_0_3")
   if(null === actions) {
      logInfo("actions", "Actions not found, check thing ID")
      return
   }
   actions.sendRcCommand(receivedCommand.toString)
end

rule "Enigma2_SendError"
when Item Enigma2_SendError received command
then
   val actions = getActions("enigma2","enigma2:device:192_168_0_3")
   if(null === actions) {
      logInfo("actions", "Actions not found, check thing ID")
      return
   }
   actions.sendError(receivedCommand.toString, 10)
end

rule "Enigma2_SendWarning"
when Item Enigma2_SendWarning received command
then
   val actions = getActions("enigma2","enigma2:device:192_168_0_3")
   if(null === actions) {
      logInfo("actions", "Actions not found, check thing ID")
      return
   }
   actions.sendWarning(receivedCommand.toString, 10)
end

rule "Enigma2_SendInfo"
when Item Enigma2_SendInfo received command
then
   val actions = getActions("enigma2","enigma2:device:192_168_0_3")
   if(null === actions) {
      logInfo("actions", "Actions not found, check thing ID")
      return
   }
   actions.sendInfo(receivedCommand.toString, 10)
end

rule "Enigma2_SendQuestion"
when Item Enigma2_SendQuestion received command
then
   val actions = getActions("enigma2","enigma2:device:192_168_0_3")
   if(null === actions) {
      logInfo("actions", "Actions not found, check thing ID")
      return
   }
   actions.sendQuestion(receivedCommand.toString, 10)
end

rule "Enigma2_Answer"
when Item Enigma2_Answer received update
then
   val actions = getActions("enigma2","enigma2:device:192_168_0_3")
   if(null === actions) {
      logInfo("actions", "Actions not found, check thing ID")
      return
   }
   logInfo("actions", "Answer is " + Enigma2_Answer.state)
end
```

## Rule Actions

Multiple actions are supported by this binding. In classic rules these are accessible as shown in this example (adjust getActions with your ThingId):

Example

```java
 val actions = getActions("enigma2","enigma2:device:192_168_0_3")
 if(null === actions) {
        logInfo("actions", "Actions not found, check thing ID")
        return
 }
```

### sendInfo(text)

Sends an info message to the device with will be shown on the TV screen for 30 seconds.

Parameters:

| Name    | Description                                                          |
|---------|----------------------------------------------------------------------|
| text    | The text to display                                                  |

Example:

```java
actions.sendInfo("Hello World")
```

### sendInfo(text, timeout)

Sends an info message to the device with will be shown on the TV screen.

Parameters:

| Name    | Description                                                          |
|---------|----------------------------------------------------------------------|
| text    | The text to display                                                  |
| timeout | The timeout in seconds                                               |

Example:

```java
actions.sendInfo("Hello World", 10)
```

### sendWarning(text)

Sends a warning message to the device with will be shown on the TV screen for 30 seconds.

Parameters:

| Name    | Description                                                          |
|---------|----------------------------------------------------------------------|
| text    | The text to display                                                  |

Example:

```java
actions.sendWarning("Hello World")
```

### sendWarning(text, timeout)

Sends a warning message to the device with will be shown on the TV screen.

Parameters:

| Name    | Description                                                          |
|---------|----------------------------------------------------------------------|
| text    | The text to display                                                  |
| timeout | The timeout in seconds                                               |

Example:

```java
actions.sendWarning("Hello World", 10)
```

### sendError(text)

Sends an error message to the device with will be shown on the TV screen for 30 seconds.

Parameters:

| Name    | Description                                                          |
|---------|----------------------------------------------------------------------|
| text    | The text to display                                                  |

Example:

```java
actions.sendError("Hello World")
```

### sendError(text, timeout)

Sends an error message to the device with will be shown on the TV screen.

Parameters:

| Name    | Description                                                          |
|---------|----------------------------------------------------------------------|
| text    | The text to display                                                  |
| timeout | The timeout in seconds                                               |

Example:

```java
actions.sendError("Hello World", 10)
```

### sendQuestion(text)

Sends a question message to the device with will be shown on the TV screen for 30 seconds.
The answer is provided to the "answer"-channel.

Parameters:

| Name    | Description                                                          |
|---------|----------------------------------------------------------------------|
| text    | The text to display                                                  |

Example:

```java
actions.sendQuestion("Say hello?")
```

### sendQuestion(text, timeout)

Sends a question message to the device with will be shown on the TV screen.
The answer is provided to the "answer"-channel.

Parameters:

| Name    | Description                                                          |
|---------|----------------------------------------------------------------------|
| text    | The text to display                                                  |
| timeout | The timeout in seconds                                               |

Example:

```java
actions.sendQuestion("Say hello?", 10)
```

### sendRcCommand(button)

Sends a button press event to the device.

Parameters:

| Name    | Description                                                            |
|---------|------------------------------------------------------------------------|
| button  | see the supported buttons in chapter 'Remote Control Buttons'          |

The button parameter has only been tested on a Vu+Solo2 and this is a list of button codes that are known to work with this device.

| Code String   |
|---------------|
| POWER         |
| KEY_0         |
| KEY_1         |
| KEY_2         |
| KEY_3         |
| KEY_4         |
| KEY_5         |
| KEY_6         |
| KEY_7         |
| KEY_8         |
| KEY_9         |
| ARROW_LEFT    |
| ARROW_RIGHT   |
| VOLUME_DOWN   |
| VOLUME_UP     |
| MUTE          |
| CHANNEL_UP    |
| CHANNEL_DOWN  |
| LEFT          |
| RIGHT         |
| UP            |
| DOWN          |
| OK            |
| EXIT          |
| RED           |
| GREEN         |
| YELLOW        |
| BLUE          |
| PLAY          |
| PAUSE         |
| STOP          |
| RECORD        |
| FAST_FORWARD  |
| FAST_BACKWARD |
| TV            |
| RADIO         |
| AUDIO         |
| VIDEO         |
| TEXT          |
| INFO          |
| MENU          |
| HELP          |
| SUBTITLE      |
| EPG           |

Example:

```java
actions.sendRcCommand("KEY_1")
```
