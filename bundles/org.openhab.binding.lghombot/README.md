# LG HomBot Binding

The binding integrates a modified LG HomBot VR6260 based vacuum robots.
Details on how to modify your HomBot can be found at [roboter-forum.com](https://www.roboter-forum.com/index.php?thread/10009-lg-hombot-3-0-wlan-kamera-steuerung-per-weboberfläche/).
The binding uses the HTTP service on port 6260 right now.
Please take care when modifying your HomBot! This binding is a complement to a modified HomBot not an excuse to do the modification.
Remember, You are responsible if You brick Your HomBot.

## Supported Things

Hacked LG HomBot series 62XX are supported.

The service that the binding connects to is actually lg.srv found at [sourceforge.net](https://sourceforge.net/projects/lgsrv/) running on a HomBot.

## Discovery

The auto-discovery should hopefully find your HomBot, be aware that it will try to connect to port 6260 on all IP-addresses on your subnet.
If you already know the IP-address of your HomBot you might as well just configure it.

## Thing Configuration

The thing only requires an IP-address to function, this could also be found using the discovery method.
The parameter is called "ipAdress".

You can also configure the polling interval in seconds by setting "pollingPeriod", and the network port of the server by using "port".

## Channels

| Channel Type ID | Item Type | Description                                                              | Read/Write |
|-----------------|-----------|--------------------------------------------------------------------------|------------|
| state           | String    | Current state of the HomBot.                                             | R          |
| battery         | Number    | Current battery charge.                                                  | R          |
| cpuLoad         | Number    | Current CPU load.                                                        | R          |
| srvMem          | Number    | Current server memory load.                                              | R          |
| clean           | Switch    | Start cleaning / return home.                                            | RW         |
| start           | Switch    | Start cleaning.                                                          | RW         |
| home            | Switch    | Send HomBot home.                                                        | RW         |
| pause           | Switch    | Pause current activity.                                                  | RW         |
| turbo           | Switch    | Turn turbo on/off.                                                       | RW         |
| repeat          | Switch    | Turn repeat cleaning on/off.                                             | RW         |
| mode            | String    | Current cleaning mode.                                                   | RW         |
| nickname        | String    | Nickname of the HomBot.                                                  | R          |
| move            | String    | Manually control the HomBot.                                             | RW         |
| camera          | Image     | Image from the top camera.                                               | R          |
| lastClean       | DateTime  | Date of last clean.                                                      | R          |
| map             | Image     | Image of clean area.                                                     | R          |
| monday          | DateTime  | Time when cleaning is on Mondays.                                        | R          |
| tuesday         | DateTime  | Time when cleaning is on Tuesdays.                                       | R          |
| wednsday        | DateTime  | Time when cleaning is on Wednsdays.                                      | R          |
| thursday        | DateTime  | Time when cleaning is on Thursdays.                                      | R          |
| friday          | DateTime  | Time when cleaning is on Fridays.                                        | R          |
| saturday        | DateTime  | Time when cleaning is on Saturdays.                                      | R          |
| sunday          | DateTime  | Time when cleaning is on Sundays.                                        | R          |

## Full Example

Example of how to configure a thing.

demo.thing

```java
Thing lghombot:LGHomBot:mycleanerbot "LGHomBot" @ "Living Room" [ ipAdress="192.168.0.2", pollingPeriod="3", port="6260" ]
```

Here are some examples on how to map the channels to items.

demo.items:

```java
String HomBot_State "State [%s]" <CleaningRobot> { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:state" }
Number HomBot_Battery "Battery [%d%%]"           { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:battery" }
Switch HomBot_Clean "Clean"                      { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:clean" }
Switch HomBot_Start "Start"                      { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:start" }
Switch HomBot_Home  "Home"                       { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:home" }
Switch HomBot_Pause "Pause"                      { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:pause" }
Switch HomBot_Turbo "Turbo"                      { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:turbo" }
Switch HomBot_Repeat "Repeat"                    { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:repeat" }
String HomBot_CleanMode "Clean mode [%s]"        { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:mode" }
String HomBot_Nickname                           { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:nickname" }
Image HomBot_Camera                              { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:camera" }
DateTime HomBot_LastClean                        { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:lastClean" }
Image HomBot_Map                                 { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:map" }
```

demo.sitemap:

```perl
sitemap demo label="Main Menu"
{
    Frame label="HomBot" {
        Text item=HomBot_State
        Text item=HomBot_Battery
        Switch item=HomBot_Clean
        Switch item=HomBot_Start
        Switch item=HomBot_Home
        Switch item=HomBot_Pause
        Switch item=HomBot_Turbo
        Switch item=HomBot_Repeat
        Selection item=HomBot_CleanMode mappings=[
            "ZZ"="Zigzag mode",
            "SB"="Cell by cell mode",
            "SPOT"="Spiral spot mode",
            "MACRO_SECTOR" = "My space mode" ]
        Text item=HomBot_Nickname
        Image item=HomBot_Camera
        DateTime item=HomBot_LastClean
        Image item=HomBot_Map
    }
}
```
