# Doorbird Binding

Binding for Doorbird D101 and D210x video doorbells.

## Supported Things

The following thing types are supported:

| Device                           | Thing ID  |
|----------------------------------|-----------|
| Doorbird D101/D201/D205/D1101V  Doorbell | d101      |
| Doorbird D210x Doorbell          | d210x     |
| Doorbird A1081 Controller        | a1081     |

## Thing Configuration

### D101/D201/D205/D1101V and D210x Doorbell

The following configuration parameters are available on the Doorbird D101/D201/D205/D1101V and D210x Doorbell things:

| Parameter                | Parameter ID       | Required/Optional | Description |
|--------------------------|--------------------|-------------------|-------------|
| Hostname                 | doorbirdHost       | Required          | The hostname or IP address of the Doorbird device. |
| User ID                  | userId             | Required          | User Id of a Doorbird user that has permissions to access the API. The User ID and Password must be created using the Doorbird smart phone application. |
| Password                 | userPassword       | Required          | Password of a Doorbird user. |
| Image Refresh Rate       | imageRefreshRate   | Optional          | Rate at which image channel should be automatically updated. Leave field blank (default) to disable refresh. |
| Doorbell Off Delay       | doorbellOffDelay   | Optional          | Number of seconds to wait before setting doorbell channel OFF after a doorbell event. Leave field blank to disable. |
| Motion Off Delay         | motionOffDelay     | Optional          | Number of seconds to wait before setting motion channel OFF after a motion event. Leave field blank to disable. |
| Montage Number of Images | montageNumImages   | Required          | Number of images to include in the doorbell and motion montage images. Default is 0. |
| Montage Scale Factor     | montageScaleFactor | Required          | Percent scaling factor for montage image. Default is 100. |

### A1081 Controller

The following configuration parameters are available on the Doorbird A1081 Controller thing:

| Parameter                | Parameter ID | Required/Optional | Description |
|--------------------------|--------------|-------------------|-------------|
| Hostname                 | doorbirdHost | Required          | The hostname or IP address of the Doorbird device. |
| User ID                  | userId       | Required          | User Id of a Doorbird user that has permissions to access the API. The User ID and Password must be created using the Doorbird smart phone application. |
| Password                 | userPassword | Required          | Password of a Doorbird user. |
| Controller Id            | controllerId | Optional          | Doorbird Id of the controller to reliable target the relays of this device. E.g. "gggaaa" |

## Discovery

Auto-discovery is not supported at this time.

## Channels

The following channels are supported by the binding for the Doorbird D101/D201/D205 and D210x Doorbell thing types.

| Channel ID               | Item Type | Description                                       |
|--------------------------|-----------|---------------------------------------------------|
| doorbell                 | Trigger   | Generates PRESSED event when doorbell is pressed  |
| doorbellTimestamp        | DateTime  | Timestamp when doorbell was pressed               |
| doorbellImage            | Image     | Image captured when the doorbell was pressed      |
| doorbellHistoryIndex     | Number    | Index of historical image for doorbell press      |
| doorbellHistoryTimestamp | DateTime  | Time when doorbell was pressed for history image  |
| doorbellHistoryImage     | Image     | Historical image for doorbell press               |
| doorbellMontage          | Image     | Concatenation of first n doorbell history images  |
| motion                   | Switch    | Changes to ON when the device detects motion      |
| motionTimestamp          | DateTime  | Timestamp when motion sensor was triggered        |
| motionImage              | Image     | Image captured when motion was detected           |
| motionHistoryIndex       | Number    | Index of Historical image for motion              |
| motionHistoryTimestamp   | DateTime  | Time when motion was detected for history image   |
| motionHistoryImage       | Image     | Historical image for motion sensor                |
| motionMontage            | Image     | Concatenation of first n motion history images    |
| light                    | Switch    | Activates the light relay                         |
| openDoor1                | Switch    | Activates the door 1 relay                        |
| openDoor2                | Switch    | Activates the door 2 relay (D210x only)           |
| image                    | Image     | Image from the doorbird camera                    |
| imageTimestamp           | DateTime  | Time when image was captured from device          |

The following channels are supported by the binding for the Doorbird A1081 Controller thing type.

| Channel ID               | Item Type | Description                                       |
|--------------------------|-----------|---------------------------------------------------|
| openDoor1                | Switch    | Activates the door 1 relay                        |
| openDoor2                | Switch    | Activates the door 2 relay                        |
| openDoor3                | Switch    | Activates the door 3 relay                        |

## Profiles

Using the system default switch profile *rawbutton-on-off-switch* in a *doorbell* channel item definition will cause ON/OFF 
states to be set when the doorbell is pressed and released.
See *Items* example below.

## Rule Actions

The binding supports the following actions.
In classic rules these are accessible as shown in this example (adjust getActions with your ThingId):
 
### void restart()
 
Restarts the Doorbird device.

### void sipHangup()

Hangs up a SIP call.

### String getRingTimeLimit()

Get the value of the SIP status parameter RING_TIME_LIMIT.

### String getCallTimeLimit()

Get the value of the SIP status parameter CALL_TIME_LIMIT.

### String getLastErrorCode()

Get the value of the SIP status parameter LASTERRORCODE.

### String getLastErrorText()

Get the value of the SIP status parameter LASTERRORTEXT.

Example

```
val actions = getActions("doorbird","doorbird:d101:doorbell")
if(actions === null) {
    logInfo("actions", "Actions not found, check thing ID")
    return
 }
 actions.sipHangup()
 
 var String ringTimeLimit = actions.getRingTimeLimit()
 ```

## Known Issues

The Doorbird uses the UDP protocol on port 6524 to broadcast events for Doorbird actions, such as doorbell pressed, motion detected, etc.
If the Doorbord is on a separate subnet or VLAN from openHAB, those UDP packets will not route by default.
In that case, the Doorbird binding will not receive those events.
Either put the Doorbird and openHAB on the same subnet/VLAN, or set up your network to explicitly route those UDP packets.

## Example

### Things

```
Thing doorbird:d101:doorbell "Doorbird D101 Doorbell" [doorbirdHost="192.168.1.100",userId="dtfubb0004",userPassword="HG7afc5TvN",imageRefreshRate=60,doorbellOffDelay=3,motionOffDelay=30,montageNumImages=3,montageScaleFactor=35]

Thing doorbird:a1081:controller "Doorbird A1081 Controller" [doorbirdHost="192.168.1.100",userId="dtfubb0004",userPassword="HG7afc5TvN"]
```

### Items

```
Switch Doorbell_Pressed "Doorbell Pressed [%s]" <switch> ["Switch"] { channel="doorbird:d101:doorbell:doorbell" [profile="rawbutton-on-off-switch"] }
DateTime Doorbell_PressedTimestamp "Doorbell Pressed Timestamp [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> { channel="doorbird:d101:doorbell:doorbellTimestamp" }
Image Doorbell_PressedImage "Doorbell Pressed Image [%s]" { channel="doorbird:d101:doorbell:doorbellImage" }
Switch Doorbell_Motion "Doorbell Motion [%s]" <switch> ["Switch"] { channel="doorbird:d101:doorbell:motion" }
DateTime Doorbell_MotionTimestamp "Doorbell Motion Timestamp [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> { channel="doorbird:d101:doorbell:motionTimestamp" }
Image Doorbell_MotionDetectedImage "Motion Detected Image [%s]" { channel="doorbird:d101:doorbell:motionImage" }
Switch Doorbell_Light "Doorbell Light [%s]" <switch> ["Switch"] { channel="doorbird:d101:doorbell:light", expire="5s,command=OFF" }
Switch Doorbell_OpenDoor1 "Doorbell Open Door 1 [%s]" <switch> ["Switch"] { channel="doorbird:d101:doorbell:openDoor1", expire="5s,command=OFF" }
Image Doorbell_Image "Doorbell Image [%s]" { channel="doorbird:d101:doorbell:image" }
Number Doorbell_DoorbellHistoryIndex "Doorbell History Index [%.0f]" <none> { channel="doorbird:d101:doorbell:doorbellHistoryIndex" }
DateTime Doorbell_DoorbellHistoryTimestamp "Doorbell History Timestamp [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> { channel="doorbird:d101:doorbell:doorbellHistoryTimestamp" }
Image Doorbell_DoorbellHistoryImage "Doorbell History Image [%s]" { channel="doorbird:d101:doorbell:doorbellHistoryImage" }
Number Doorbell_MotionHistoryIndex "Motion History Index [%.0f]" <none> { channel="doorbird:d101:doorbell:motionHistoryIndex" }
DateTime Doorbell_MotionHistoryTimestamp "Motion History Timestamp [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> { channel="doorbird:d101:doorbell:motionHistoryTimestamp" }
Image Doorbell_MotionHistoryImage "Motion History Image [%s]" { channel="doorbird:d101:doorbell:motionHistoryImage" }
Image Doorbell_DoorbellMontage "Doorbell History Montage [%s]" { channel="doorbird:d101:doorbell:doorbellMontage" }
Image Doorbell_MotionMontage "Motion History Montage [%s]" { channel="doorbird:d101:doorbell:motionMontage" }
```

### Sitemap

```
Frame {
    Text label="Doorbird" {
        Frame label="Image" {
            Image item=Doorbell_Image
        }
        Frame label="Events" {
            Text item=Doorbell_Pressed
            Text item=Doorbell_PressedTimestamp
            Image item=Doorbell_PressedImage
            Text item=Doorbell_Motion
            Text item=Doorbell_MotionTimestamp
            Image item=Doorbell_MotionImage
        }
        Frame label="Actions" {
            Switch item=Doorbell_OpenDoor1
            Switch item=Doorbell_Light
        }
        Frame label="History" {
            Setpoint item=Doorbell_DoorbellHistoryIndex minValue=1 maxValue=50 step=1
            Switch item=Doorbell_DoorbellHistoryIndex label="Reset Index []" mappings=[1="Reset"]
            Text item=Doorbell_DoorbellHistoryTimestamp
            Image item=Doorbell_DoorbellHistoryImage
            Setpoint item=Doorbell_MotionHistoryIndex minValue=1 maxValue=50 step=1
            Switch item=Doorbell_MotionHistoryIndex label="Reset Index []" mappings=[1="Reset"]
            Text item=Doorbell_MotionHistoryTimestamp
            Image item=Doorbell_MotionHistoryImage
        }
        Frame label="Doorbell Montage" {
            Image item=Doorbell_DoorbellMontage
        }
        Frame label="Motion Montage" {
            Image item=Doorbell_MotionMontage
        }
    }
}
```

### Rule

Using the doorbell trigger channel to detect if the doorbell has been pressed:

```
rule "Doorbell Button Pressed"
when
    Channel "doorbird:d101:doorbell:doorbell" triggered PRESSED
then
    // Do something when the doorbell is pressed
end
```

Alternatively, detecting a doorbell press using an item that references the *rawbutton-on-off-switch* profile:

```
rule "Doorbell Button Pressed"
when
    Item Doorbell_Pressed received command ON
then
    // Do something when the doorbell is pressed
end
```

Using the doorbell motion channel to detect motion:

```
rule "Motion Detected"
when
    Item Doorbell_Motion received command ON
then
    // Do something when motion is detected
end
```
