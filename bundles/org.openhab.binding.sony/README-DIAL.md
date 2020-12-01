# DIAL

The DIAL (DIscovery And Launch) allows you to discover the various applications available on the device and manage those applications (mainly to start or stop them).
This will apply to many of the smart tvs and bluray devices.
Generally you need to authenticate with IRCC before being able to use DIAL.
A channel will be created for each application (at startup only) and you can send ON to that channel to start the application and OFF to exit back to the main menu.

## Application status

Sony has 'broken' the API that determines which application is currently running regardless if you use DIAL or Scalar services.
The API that determines whether an application is currently running ALWAYS returns 'stopped' (regardless if it's running or not).
Because of that - you cannnot rely on the application status and there is NO CURRENT WAY to determine if any application is running.

Both DIAL/Scalar will continue to check the status in case Sony fixes this in some later date - but as of this writing - there is NO WAY to determine application status.


## Authentication

The DIAL service itself, generally, doesn't support authentication and relies on the authentication from IRCC.

Feel free to try to authentication as outlined in the main [README](README.md).

However, if that doesn't work, authenticate via the IRCC service.
Once the IRCC thing is online, the DIAL thing should come online as well and you may delete the IRCC thing (if you are not using it)

## Thing Configuration

The configuration for the DIAL Service Thing:

| Name       | Required | Default | Description                    |
| ---------- | -------- | ------- | ------------------------------ |
| accessCode | No       | RQST    | The access code for the device |

## Channels

The DIAL service will interactively create channels (based on what applications are installed on the device).
The channels will be:

| Channel Type ID | Read/Write | Item Type | Description                         |
| --------------- | ---------- | --------- | ----------------------------------- |
| state-{id}      | R  (1)     | Switch    | Whether the app is running or not   |
| icon-{id}       | R          | Image     | The icon related to the application |

1.  Please note that at the time of this writing, Sony broke the application status and this channel will not correctly reflect what is running
   
The {id} is the unique identifier that the device has assigned to the application.

Example: On my bluray device, "Netflix" is identified as "com.sony.iptv.type.NRDP".
The channels would then be:

1. "state-com.sony.iptv.type.NRDP" with a label of "Netflix"
2. "icon-com.sony.iptv.type.NRDP" with a label of "Netflix Icon"

To identify all of the channel ids - look into the log file.
This service (on startup) will provide a logging message like:
```Creating channel 'Netflix' with an id of 'com.sony.iptv.type.NRDP' ``

Note: if you install a new application on the device, this binding will need to be restarted to pickup on and create a channel for the new application.
  
## Full Example

*Really recommended to autodiscover rather than manually setup thing file*

dial.Things:

```
Thing sony:dial:home [ deviceAddress="http://192.168.1.71:50201/dial.xml", deviceMacAddress="aa:bb:cc:dd:ee:ff", refresh=-1 ]
```

dial.items:

```
Switch DIAL_Netflix "Netflix [%s]" { channel="sony:dial:home:state-com.sony.iptv.type.NRDP" }
Image DIAL_NetflixIcon "Icon" { channel="sony:dial:home:icon-com.sony.iptv.type.NRDP" }
```


dial.sitemap

```
sitemap demo label="Main Menu"
{
    Frame label="DIAL" {
        Switch item=DIAL_Netflix
        ImageItem item=DIAL_NetflixIcon
    }
}
```
