# Network Camera Motion Detection Binding

This binding integrates large number of network cameras which can send image to FTP server when motion or sound is detected. Binding acts as a FTP server. Images are not saved to file system, therefore binding shouldn't cause any problems on flash based devices.


## Supported Things

This binding supports ```networkcamera``` Thing. Every camera is identified by FTP user name. Therefore, every camera should use unique user name to login FTP server. 

## Discovery

Automatic discovery is not supported.

## Binding Configuration

Bindings FTP server listening 2121 TCP port by default, but port can be configured. Also idle timeout can be configured.

## Channels

This binding currently supports following channels:

| Channel Type ID | Item Type    | Description  |
|-----------------|------------------------|--------------|
| image | Image | Image received from network camera. |
| motion | Switch | Motion detection sensor state. Updated to ON state when image is received from camera. |


### Trigger Channels

| Channel Type ID | Options | Description  |
|-----------------|------------------------|--------------|
| motion-trigger | MOTION_DETECTED | Triggered when image received from network camera. |


## Full Example

Things:

```
Thing networkcameramotiondetection:networkcamera:garage [ userName="garage", password="12345" ]
```

Items:

```
Switch Garage_NetworkCamera_Motion { channel="networkcameramotiondetection:networkcamera:garage:motion" } 
```

Rules:

```
rule "example trigger rule"
when
    Channel 'networkcameramotiondetection:NetworkCamera:garage:motion-trigger' triggered MOTION_DETECTED 
then
    logInfo("Test","MOTION DETECTED trigger example")
end

rule "example rule"
when
    Item Garage_NetworkCamera_Motion received update ON
then
    logInfo("Test", "motion detected item example")
    NetworkCamera_Motion.postUpdate(OFF)
end
```