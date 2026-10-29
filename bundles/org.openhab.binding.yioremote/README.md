# YIOremote Binding

This binding controls a YIO Remote/Dock setup. YIO Remote/Dock is a smart-home solution that includes an IP‑based remote. More information is available at [yio-remote.com](https://www.yio-remote.com/) or in the [community forum](https://community.yio-remote.com/).

This binding has been designed to complement the YIO WebSocket transport protocol.

Since this binding lets you trigger IR send/receive actions on the YIO Dock, you can use the dock as an IR solution for openHAB and even learn new IR codes from your remotes. In other words, if the IR code is known, openHAB can use the YIO Dock to control that device even if there is no dedicated openHAB binding for it.

## Supported Things

- Thing Type ID: `yioRemoteDock`

The following are the configurations available to each of the bridges/things:

### YIO Dock

| Name        | Type   | Required | Default | Description                                      |
| ----------- | ------ | -------- | ------- | ------------------------------------------------ |
| host        | string | Yes      | (None)  | Host/IP address or host name of the YIO Dock     |
| accessToken | string | Yes      | 0       | Authentication token of the YIO Dock (see below) |

### Access Token

The original YIO Dock firmware always uses the token `0`.
Dock firmware with an individual token per dock shows it on the dock's web page: open `http://<dock-ip>/` in a browser and copy the value from the "Token" row into the `accessToken` parameter.
If the token is wrong, the Thing goes `OFFLINE` with a configuration error and does not reconnect until the configuration is changed.

### Thing Properties

If the dock firmware reports its version, it is shown as the `firmwareVersion` property of the Thing.

## Channels

### YIO Dock

The YIO Dock has the following channels:

| Channel        | Input/Output | Item Type | Description                                                                                                                                     |
| -------------- | ------------ | --------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| receiverswitch | Input        | Switch    | Enable/disable the IR receiver diode/function                                                                                                   |
| status         | Output       | String    | Status of the YIO Dock. If the receiver is on, the recognized IR code is displayed; otherwise the send status of the last IR code is displayed. |

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
yioremote:yioRemoteDock:livingroom [ host="xxx.xxx.xxx.xxx", accessToken="0" ]
```

.items

```java
Switch receiverswitch "IR receiving switch" { channel="yioremote:yioRemoteDock:livingroom:input#receiverswitch" }
String status         "YIO Dock status [%s]" { channel="yioremote:yioRemoteDock:livingroom:output#status" }
```

.sitemap

```perl
sitemap Basic label="YIO Dock" {
    Switch item=receiverswitch
    Text item=status
}
```
