# YIOremote Binding

This binding will control a YIO Dock/Remote combination. YIO Remote/Dock is a smart home solution that includes an IP based remote. More information can be found at [yio-remote](https://www.yio-remote.com/) or in the forums at [yio-remote](https://community.yio-remote.com/).

This binding has been designed to compliment the YIO websocket Transport Protocol.

Since this binding allows actual you to trigger IR send/receive actions on YIO Dock, this allows you to use the YIO Dock as an IR solution to openHAB and even learn new IR codes from your remotes. In other words, if the IR code is known then openHAB can use the YIO Dock to control that Device regardless if there is an openHAB binding for it or not.

## Supported Things

- Thing Type ID: `yioRemoteDock`

The following are the configurations available to each of the bridges/things:

### YIO Dock

| Name        | Type   | Required | Default | Description                                         |
| ----------- | ------ | -------- | ------- | --------------------------------------------------- |
| host        | string | Yes      | (None)  | Host/IP Address or host name of the YIO Dock        |
| accesstoken | string | Yes      | 0       | The authentication token for the access currently 0 |

## Channels

### YIO Dock

The YIO Dock has the following channels:

| Channel        | Input/Output | Item Type | Description                                                                                                                                                         |
| -------------- | ------------ | --------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| receiverswitch | Input        | Switch    | The switch to enable disable the IR receiving diode/function                                                                                                        |
| status         | Output       | String    | The status of the YIO Dock. If the reciever is on than the recognized IR code will be displayed otherwise the IR send status is displayed of the last IR code send. |

# Actions

With the YIO remote action, you can send IR Codes via the YIO Remote Dock.

## Example

```java
rule "yioremote Action Example"
when
  ...
then
    val actions = getActions("yioremote", "yioremote:yioRemoteDock:livingroom")
    if (actions === null)
    {
        ......
    }
    else
    {
        actions.sendIRCode("3;0x20DF40BF;32;0")
    }
end
```

## Full Example

.things

```java
yioremote:yioRemoteDock:livingroom [     host="xxx.xxx.xxx.xxx",    accesstoken="0"  ]
```

.items

```java
Switch     receiverswitch    "IR recieving switch"     {channel="yioremote:yioRemoteDock:livingroom:input#    receiverswitch"}
String     status            "YIO Dock status[%s]"     {channel="yioremote:yioRemoteDock:livingroom:output#    status"}
```

.sitemap

```perl
sitemap Basic label="YIO Dock" {
    Switch item=receiverswitch
    Text item=status
}
```
