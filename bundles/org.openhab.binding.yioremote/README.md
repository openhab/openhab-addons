# YIOremote Binding

This binding will control a YIO Dock/Remote combination. YIO Remote/Dock is a smart home solution that includes an IP based remote. More information can be found at [yio-remote](https://www.yio-remote.com/) or in the forums at [yio-remote](https://community.yio-remote.com/). 

This binding has been designed to compliment the YIO websocket Transport Protocol.

Since this binding allows actual you to trigger IR send/receive actions on YIO Dock, this allows you to use the YIO Dock as an IR solution to openHAB and even learn new IR codes from your remotes. In other words, if the IR code is known then openHAB can use the YIO Dock to control that Device regardless if there is an openHAB binding for it or not.

## Supported Things

* Thing: YIO Device.

The following are the configurations available to each of the bridges/things:

### YIO Dock

| Name                 | Type    | Required | Default | Description                                                                                                    |
|----------------------|---------|----------|---------|----------------------------------------------------------------------------------------------------------------|
| yiodockhostip        | string  | Yes      | (None)  | IP Address or host name of the YIO Dock                                                                      |
| yiodockaccesstoken   | string  | Yes      | 0       | The authentication token for the access currently 0
                                                      
## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/ESH-INF/thing``` of your binding._

| channel  | type   | description                  |
|----------|--------|------------------------------|
| control  | Switch | This is the control channel  |

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
