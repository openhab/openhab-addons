# Mycroft Binding

This binding will connect to Mycroft A.I. in order to control it or react to event by listening on the message bus.

Possibilies include :

- Press a button in OpenHAB to wake Mycroft without using a wake word.
- Simulate a voice command to launch a skill, as if you just spoke it
- Send some text that Mycroft will say (Using its Text To Speach service)
- Control the music player
- Control the sound volume of Mycroft
- React to all the aforementioned events ...
- ... And send/receive all other kind of messages on the message bus


## Supported Things

The only thing managed by this binding is a Mycroft instance.


## Discovery

There is no discovery service, as Mycroft doesn't announce itself on the network.


## Thing Configuration

The configuration is simple, as you just need to give the IP/hostname of the Mycroft instance accessible on the network.
The default port is 8181, but you could change it if you want.

```
Thing mycroft:mycroft:myMycroft "Mycroft A.I." @ "Living Room" [host="192.168.X.X"]
```

|   property    |              type               |         description                                                     | mandatory |
|---------------|---------------------------------|-------------------------------------------------------------------------|-----------|
| host          | IP or string                    | IP address or hostname                                                  |   Yes     |
| port          | integer                         | Port to reach Mycroft (default 8181)                                    |   No      |


## Channels

The Mycroft thing supports the following channels :


| channel type id              | Item type | description                                                                                    |
|------------------------------|-----------|------------------------------------------------------------------------------------------------|
| listen                       | Switch    | Switch to ON when Mycroft is listening. Can simulate a wake word detection to trigger the STT  |
| speak                        | String    | The last sentence Mycroft speaks, or ask Mycroft to say something                              |
| utterance                    | String    | The last utterance Mycroft receive, or ask Mycroft something                                   |
| player                       | Player    | The music player Mycroft is currently controlling                                              |
| volume                       | Dimmer    | The volume of the Mycroft speaker. NOT FUNCTIONNAL. [SEE THIS POST TO SEE EVOLUTION](https://community.mycroft.ai/t/openhab-plugin-development-audio-volume-message-types-missing/10576) |
| volume_mute                  | Switch    | Mute the Mycroft speaker                                                                       |
| volume_duck                  | Switch    | Duck the volume of the Mycroft speaker                                                         |
| full_message                 | String    | The last message (full json) seen on the Mycroft Bus. Filtered by the messageTypes properties  |


The channel 'full_message' has the following configuration available :

| property      |  type                           | description                                                             | mandatory |
|---------------|---------------------------------|-------------------------------------------------------------------------|-----------|
| messageTypes  | List of string, comma separated | Only these message types will be forwarded to the Full Message Channel  |   No      |


## Full Example

A manual setup through a `things/mycroft.things` file could look like this:

```java
Thing mycroft:mycroft:myMycroft "Mycroft A.I." @ "Living Room" [host="192.168.X.X", port=8181] { 
    Channels:
        Type full-message-channel : Text [
            messageTypes="message.type.1,message.type.4"
        ]
}
```

### Item Configuration

The `mycroft.item` file :

```java
Switch myMycroft_mute                  "Mute"                      { channel="mycroft:mycroft:myMycroft:volume_mute" }
Switch myMycroft_duck                  "Duck"                      { channel="mycroft:mycroft:myMycroft:volume_duck" }
Dimmer myMycroft_volume                "Volume [%d]"               { channel="mycroft:mycroft:myMycroft:volume" }
Player myMycroft_player                "Control"                   { channel="mycroft:mycroft:myMycroft:player" }
Switch myMycroft_listen                "Wake and listen"           { channel="mycroft:mycroft:myMycroft:listen" }
String myMycroft_speak                 "Speak STT"                 { channel="mycroft:mycroft:myMycroft:speak" }
String myMycroft_utterance             "Utterance"                 { channel="mycroft:mycroft:myMycroft:utterance" }
String myMycroft_fullmessage           "Full JSON message"         { channel="mycroft:mycroft:myMycroft:full_message" }
```

### Sitemap Configuration

A `demo.sitemap` file :

```perl
sitemap demo label="myMycroft"
{
    Frame label="myMycroft" {
        Switch    item=myMycroft_mute
        Switch    item=myMycroft_duck
        Slider    item=myMycroft_volume
        Default   item=myMycroft_player
        Switch    item=myMycroft_listen
        Text      item=myMycroft_speak
        Text      item=myMycroft_utterance
        Text      item=myMycroft_fullmessage
    }
}
