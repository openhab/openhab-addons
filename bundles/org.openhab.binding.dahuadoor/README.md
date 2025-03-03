# DahuaDoor Binding

A small binding for VTO2202F Villastation Dahua Door controller.


## Supported Things

Currently only VTO2202F Villastation is supported.



## Discovery

not supported


## Thing Configuration

Following configuration is required. Please note that you have to use a linux path where openhab has sufficient access rights.
E.g. /var/lib/openhab/door-images. Windows is not supported yet.

### Thing Configuration

| Name            | Type    | Description                                           
|-----------------|---------|-------------------------------------------------------|
| hostname        | text    | Hostname or IP address of the device                  |
| username        | text    | Username to access the device                         |
| password        | text    | Password to access the device                         |
| path            | text    | Path where image files are stored Linux path required |   

## Channels

The following channels are provided:

| Channel     | Type     | Description                              |
|-------------|----------|------------------------------------------|
| bell_button | Switch   | Trigger channel for Dahua Door button    |
| door_image  | Image    | Camera image when bell button is pressed | 
| openDoor1   | Switch   | Switch to open door 1                    |
| openDoor2   | Switch   | Switch to open door 2                    |


## Full Example


### Item Configuration

```java
Switch DoorOpener "Tueroeffner" <switch> (Eingang) ["Switch"]
Image DahuaDoor_Binding_Thing_Door_Image "Eingangstuere" <camera> (Eingang)
```
### Rule Configuration

If you have installed the openhab cloud service openhab can send you a notification to your smartphone with the captured image.

```java
rule "Türklingel"
    when 
        Channel "dahuadoor:dahua_vth:frontdoor:bell_button" triggered PRESSED
    then
    sendBroadcastNotification("Besucher hat geklingelt", "door", 
       "TagEingang", "Eingang", "eingangsnachrichten", null, "item:DahuaDoor_Binding_Thing_Door_Image", "Haustüre öffnen=command:DoorOpener:ON","Garage öffnen=command:Garage_Garagentor:ON",null)
end
```



