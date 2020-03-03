# Sony PlayStation Binding

This binding allows you to monitor the on/off status and which application that is currently running on your PlayStation 4.
By providing your user-credentials you can also change the power, which application that is running and more.

## Supported Things

This binding should support all PS4 variants.

## Discovery

Discovery should find all your PS4s within a few seconds as long as they are in standby mode and not completely turned off.

## Thing Configuration

If you want to control your PS4 the first thing you need is your user-credentials, this is a 64 characters HEX string that is easiest obtained by using PS4-waker https://github.com/dhleong/ps4-waker. The result file is called ".ps4-wake.credentials.json" in your home directory.
Then you need to pair your OpenHAB device with the PS4.
Then, if you have a pass code when you log in to your PS4 you have specify that as well.
 
_Describe what is needed to manually configure a thing, either through the (Paper) UI or via a thing-file. This should be mainly about its mandatory and optional configuration parameters. A short example entry for a thing file can help!_

_Note that it is planned to generate some part of this based on the XML files within ```ESH-INF/thing``` of your binding._

## Channels

| Channel Type ID | Item Type | Description                                                              | Read/Write |
|-----------------|-----------|--------------------------------------------------------------------------|------------|
| power           | Switch    | Shows if PlayStation is ON or in standby.                                | RW         |
| applicationName | String    | Name of the currently running application.                               | R          |
| applicationId   | String    | Id of the currently running application.                                 | RW         |
| applicationImage| Image     | Application artwork.                                                     | R          |
| oskText         | String    | The text from the OnScreenKeyboard.                                      | RW         |
| disconnect      | Switch    | Disconnect from PS4.                                                     | W          |
| keyUp           | Switch    | Push Up button.                                                          | W          |
| keyDown         | Switch    | Push Down button.                                                        | W          |
| keyRight        | Switch    | Push Right button.                                                       | W          |
| keyLeft         | Switch    | Push Left button.                                                        | W          |
| keyEnter        | String    | Push Enter button.                                                       | W          |
| keyBack         | Switch    | Push Back button.                                                        | W          |
| keyOption       | Switch    | Push Option button.                                                      | W          |
| keyPS           | Switch    | Push PS button.                                                          | W          |
| secondScreen    | String    | HTTP link to the second screen.                                          | R          |

## Full Example

Example of how to configure a thing.

demo.thing

```
Thing playstation:PS4:myplaystation4 "PlayStation4" @ "Living Room" [ ipAdress="192.168.0.2", userCredential="0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF", passCode="1234", pairingCode="12345678" ]
```

Here are some examples on how to map the channels to items.

demo.items:

```
Switch PS4_Power "Power"                         { channel="playstation:PS4:a4_24_56_8f_2c_5b:power" }
String PS4_Application "Application [%s]"        { channel="playstation:PS4:a4_24_56_8f_2c_5b:applicationName" }
String PS4_ApplicationId "Application id [%s]"   { channel="playstation:PS4:a4_24_56_8f_2c_5b:applicationId" }
Image PS4_ArtWork "Artwork"                      { channel="playstation:PS4:a4_24_56_8f_2c_5b:applicationImage" }
String PS4_OSKText "OSK Text"                    { channel="playstation:PS4:a4_24_56_8f_2c_5b:oskText" }
Switch PS4_Disconnect "Disconnect"               { channel="playstation:PS4:a4_24_56_8f_2c_5b:disconnect" }
Switch PS4_Up "Up"                               { channel="playstation:PS4:a4_24_56_8f_2c_5b:keyUp" }
Switch PS4_Down "Down"                           { channel="playstation:PS4:a4_24_56_8f_2c_5b:keyDown" }
Switch PS4_Right "Right"                         { channel="playstation:PS4:a4_24_56_8f_2c_5b:keyRight" }
Switch PS4_Left "Left"                           { channel="playstation:PS4:a4_24_56_8f_2c_5b:keyLeft" }
Switch PS4_Enter "Enter"                         { channel="playstation:PS4:a4_24_56_8f_2c_5b:keyEnter" }
Switch PS4_Back "Back"                           { channel="playstation:PS4:a4_24_56_8f_2c_5b:keyBack" }
Switch PS4_Option "Option"                       { channel="playstation:PS4:a4_24_56_8f_2c_5b:keyOption" }
Switch PS4_PS "PS"                               { channel="playstation:PS4:a4_24_56_8f_2c_5b:keyPS" }
String PS4_2ndScr "2ndScreen"                    { channel="playstation:PS4:a4_24_56_8f_2c_5b:secondScreen" }
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
    Frame label="PlayStation 4" {
        Switch item=PS4_Power
        Text item=PS4_Application
        Text item=PS4_ApplicationId
        Selection item=PS4_ApplicationId mappings=[
            "CUSA00127"="Netflix",
            "CUSA01116"="Youtube",
            "CUSA02827"="HBO",
            "CUSA01780"="Spotify",
            "CUSA11993"="Marvel's Spider-Man" ]
        Image item=PS4_Artwork
        Text item=PS4_OSKText
        Switch item=PS4_Disconnect
        Switch item=PS4_Up
        Switch item=PS4_Down
        Switch item=PS4_Right
        Switch item=PS4_Left
        Switch item=PS4_Enter
        Switch item=PS4_Back
        Switch item=PS4_Option
        Switch item=PS4_PS
        Text item=PS4_2ndScr
    }
}
```

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
