# Sony PlayStation Binding

This binding allows you to monitor the on/off status and which application that is currently running on your PlayStation 4.
By providing your user-credentials you can also change the power, which application that is running and more.

## Supported Things

This binding should support all PS4 variants.
It can also tell if your PS3 is ON or OFF/not present.

## Discovery

Discovery should find all your PS4s within a few seconds as long as they are in standby mode and not completely turned off.
To be able to discover your PS3 you need to turn on "Connect PS Vita System Using Network" in
Settings -> System Settings -> Connect PS Vita System Using Network.

## Thing Configuration

**playstation4** parameters:

| Property            | Default | Required | Description                                                              |
|---------------------|---------|:--------:|--------------------------------------------------------------------------|
| ipAddress           |         | Yes      | The IP address of the PlayStation 4                                      |
| userCredential      |         | Yes      | A key used for authentication, get via PS4-waker.                        |
| pairingCode         |         | Yes      | This is shown on the PlayStation 4 during pairing, only needed once.     |
| passCode            |         | (Yes)    | If you use a code to log in your user on the PS4, set this.              |
| connectionTimeout   |  60     | No       | How long the connection to the PS4 is kept up, seconds.                  |
| autoConnect         |  false  | No       | If a connection should be establish to the PS4 when it's turned on.      |
| artworkSize         |  320    | No       | Width and height of downloaded artwork.                                  |
| outboundIP          |         | No       | Use this if your PS4 is not on the normal openHAB network.               |
| ipPort              |  997    | No       | The port to probe the PS4 on, no need to change normally.                |

If you want to control your PS4 the first thing you need is your user-credentials, this is a 64 characters HEX string that is easiest obtained by using PS4-waker <https://github.com/dhleong/ps4-waker>.
To run the PS4-waker you will need a Node.js command prompt (for example: <https://nodejs.org/en/download/>).
Enter "npx ps4-waker --help" int the command prompt. Agree to install by entering "y".
After that send "npx ps4-waker --check". You will get asked to connect the "PS4 Second screen" Android app to the running clone.
Do this and then you will need to get the pairing key from your PS4 --> Settings ---> Mobile device pairing settings.
On the PS4 screen you will see your pairing code and in the command prompt you will find the user credentials.

Then you need to pair your openHAB device with the PS4.
This can be done by saving the Thing while the pairing screen is open on the PS4. The code is only needed during pairing.

Then, if you have a pass code when you log in to your PS4 you have to specify that as well.

**playstation3** parameters:

| Property            | Default | Required | Description                                                              |
|---------------------|---------|:--------:|--------------------------------------------------------------------------|
| ipAddress           |         | Yes      | The IP address of the PlayStation 3                                      |

## Channels

| Channel Type ID  | Item Type | Description                                                             | Read/Write |
|------------------|-----------|-------------------------------------------------------------------------|------------|
| power            | Switch    | Shows if PlayStation is ON or in standby.                               | RW         |
| applicationName  | String    | Name of the currently running application.                              | R          |
| applicationId    | String    | Id of the currently running application.                                | RW         |
| applicationImage | Image     | Application artwork.                                                    | R          |
| oskText          | String    | The text from the OnScreenKeyboard.                                     | RW         |
| sendKey          | String    | Send a key/button push to PS4.                                          | W          |
| secondScreen     | String    | HTTP link to the second screen.                                         | R          |
| connect          | Switch    | Connect/disconnect to/from PS4.                                         | RW         |

## Full Example

Example of how to configure a thing.

demo.thing

```java
Thing playstation:PS4:123456789ABC "PlayStation4" @ "Living Room" [ ipAddress="192.168.0.2", userCredential="0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF", passCode="1234", pairingCode="12345678",
connectionTimeout="60", autoConnect="false", artworkSize="320", outboundIP="192.168.0.3", ipPort="997" ]

Thing playstation:PS3:123456789ABC "PlayStation3" @ "Living Room" [ ipAddress="192.168.0.2" ]
```

Here are some examples on how to map the channels to items.

demo.items:

```java
Switch PS4_Power "Power"                         { channel="playstation:PS4:123456789ABC:power" }
String PS4_Application "Application [%s]"        { channel="playstation:PS4:123456789ABC:applicationName" }
String PS4_ApplicationId "Application id [%s]"   { channel="playstation:PS4:123456789ABC:applicationId" }
Image PS4_ArtWork "Artwork"                      { channel="playstation:PS4:123456789ABC:applicationImage" }
String PS4_OSKText "OSK Text"                    { channel="playstation:PS4:123456789ABC:oskText" }
String PS4_SendKey "SendKey"                     { channel="playstation:PS4:123456789ABC:sendKey" }
String PS4_2ndScr "2ndScreen"                    { channel="playstation:PS4:123456789ABC:secondScreen" }
Switch PS4_Connect "Connect"                     { channel="playstation:PS4:123456789ABC:connect" }

Switch PS3_Power "Power"                         { channel="playstation:PS3:123456789ABC:power" }
```

demo.sitemap:

```perl
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
        Switch item=PS4_Connect
        String item=PS4_SendKey
        Selection item=PS4_SendKey mappings=[
            "keyUp"="Up",
            "keyDown"="Down",
            "keyRight"="Right",
            "keyLeft"="Left",
            "keySelect"="Select",
            "keyBack"="Back",
            "keyOption"="Option",
            "keyPS"="PS" ]
        Text item=PS4_2ndScr
    }
}
```

## Caveat and Limitations!

I tried my hardest to figure out how to turn on the PS3 through WakeOnLan but it looks like Sony never got it to work properly, the only way I've seen it turn on is via WiFi, but if you hook up your PS3 through WiFi to your router and enable WakeOnLan it turns itself on randomly.
