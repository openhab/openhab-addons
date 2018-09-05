# LG HomBot Binding

The binding integrates hacked LG HomBot VR6260 based vacuum robots.
Details on how to modify your HomBot can be [found here](https://www.roboter-forum.com/index.php?thread/10009-lg-hombot-3-0-wlan-kamera-steuerung-per-weboberfläche/).
The binding uses the HTTP service on port 6260 right now.

## Supported Things

### Hacked LG HomBot series 62XX are supported.
The service that the binding connects to is actually lg.srv [found here](https://sourceforge.net/projects/lgsrv/) running on a HomBot.

## Discovery

The auto-discovery should hopefully find your HomBot, be aware that it will try to connect to port 6260 on all IP-addresses on your subnet.
If you already know the IP-address of your HomBot you might as well just plug it in.

## Thing Configuration

The thing only requires an IP-address to function, this should hopefully be found using the discovery method.

## Channels


| Channel Type ID | Item Type | Description                                                                                                                                                                                                             | Read/Write |
|-----------------|-----------|-----------------------------------------------------------------------------------------------|------------|
| clean           | Switch    | Start cleaning / pause.                                                                       | RW         |
| home            | Switch    | Send HomBot home / pause.                                                                     | RW         |
| pause           | Switch    | Pause current activity.                                                                       | RW         |
| state           | String    | Current state of the HomBot.                                                                  | R          |
| battery         | Number    | Current battery charge.                                                                       | R          |
| cpuLoad         | Number    | Current CPU load.                                                                             | R          |
| mode            | String    | Current cleaning mode.                                                                        | RW         |
| turbo           | Switch    | Turn turbo on/off.                                                                            | RW         |
| repeat          | Switch    | Turn repeat cleaning on/off.                                                                  | RW         |
| nickname        | String    | Nickname of the HomBot.                                                                       | R          |
| camera          | Image     | Image from the top camera.                                                                    | R          |

## Full Example

Here are some examples on how to map the channels to items.


demo.items:

```
String HomBot_State "State [%s]" <CleaningRobot> { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:state" }
Number HomBot_Battery "Battery [%d%%]"           { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:battery" }
Switch HomBot_Clean "Clean"                      { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:start" }
Switch HomBot_Home  "Home"                       { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:home" }
Switch HomBot_Pause "Pause"                      { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:pause" }
Switch HomBot_Turbo "Turbo"                      { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:turbo" }
Switch HomBot_Repeat "Repeat"                    { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:repeat" }
String HomBot_CleanMode "Clean mode [%s]"        { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:mode" }
String HomBot_Nickname                           { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:nickname" }
Image HomBot_Image                               { channel="lghombot:LGHomBot:a4_24_56_8f_2c_5b:camera" }
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
    Frame label="HomBot" {
        Switch item=HomBot_Clean
        Switch item=HomBot_Home
        Switch item=HomBot_Pause
        Text item=HomBot_State
        Text item=HomBot_Battery
        Switch item=HomBot_Turbo
        Switch item=HomBot_Repeat
        Selection item=HomBot_CleanMode mappings=[
            "ZZ"="Zigzag mode",
            "SB"="Cell by cell mode",
            "SPOT"="Spiral spot mode",
            "MACRO_SECTOR" = "My space mode" ]
        Text item=HomBot_Nickname
        Image item=HomBot_Image
    }
}
```


## Any custom content here!

Please take care when modding your HomBot, this binding is a complement to a modded HomBot not an excuse to do the modding.
I take no responsibility if You brick Your HomBot.
