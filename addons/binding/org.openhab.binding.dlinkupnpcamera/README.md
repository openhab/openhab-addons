# DlinkUpnpCamera Binding

This is an openHAB2 binding that can discover D-Link cameras present in a network.
the cameras can then be added as a thing in openHAB2 in order to be used later.

## Discovery

The cameras are discovered through UPnP in the local network and are put in the inbox.

## Supported Things

The binding supports a single type of Thing which is the camera Thing.

## Binding Configuration

The binding does not require any specific configuration.

## Thing Configuration

A camera Thing requires an UDN which is the Unique Device Name used in UPnP protocol.
Moreover, the camera Thing needs to be configured with the following parameters:
 - a parameter for the status and image update, connection refresh
 - an username which is a required parameter, used for authentication to the camera
 - a password also required, used for authentication.
 - a command request URL repertory, which is used to build the HTTP request needed to command the camera
 - an image request URL repertory to build the URL used for retrieving an image from the camera

## Channels

Depending of the camera model, some channels are not supported.
Overall, a camera Thing supports the following channels:

| Channel Type ID | Item Type    | Description  |
|-----------------|------------------------|--------------|----------------- |------------- |
| Pan | Player       | Pan control and patrol mode of the camera, NEXT command pans the camera to the right, PREVIOUS command pans the camera to the left. PLAY command starts the patrol mode if available and PAUSE command stops the patrol mode. |
| Tilt | Player       | Tilt control and patrol mode of the camera, NEXT command tilts the camera up, PREVIOUS command tilts the camera down. PLAY command starts the patrol mode if available and PAUSE command stops the patrol mode. |
| Image | Image       | The image from the camera is updated if a REFRESH command is given. It is updated every time a status update is done. |

## Example to display video from the camera

demo.sitemap:

```
sitemap default label="Main Menu" {
    Text label="UPnP Camera display" {
     Webview url="http://192.168.0.48/video/mjpg.cgi" height=10
    }
}
```
