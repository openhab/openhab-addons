# UnifiedRemote Binding

This binding integrates the [Unified Remote Server](https://www.unifiedremote.com/).

<b>Known Limitations: It needs the web interface to be enabled on the server settings to work.</b>

## Discovery

Discovery works on the default discovery UDP port 9511.

## Thing Configuration

Only supported thing is 'Unified Remote Server Thing' which requires the Hostname to be correctly configured in order to work.

| ThinTypeID   | description                  |
|----------|------------------------------|
| server | Unified Remote Server Thing |

| Config   |  Type  | description                  |
|----------|----------|------------------------------|
| host | String | Unified Remote Server IP  |

## Channels

| channel  | type   | description                  |
|----------|--------|------------------------------|
| mouse-move  | String | Relative mouse move in pixels. Expect number JSON array [x,y] ("[10,10]").   |
| send-key  | String | Use server key. Supported keys are: LEFT_CLICK, RIGHT_CLICK, LOCK, UNLOCK, SLEEP, SHUTDOWN, RESTART, LOGOFF, PLAY, PLAY, PAUSE, NEXT, PREVIOUS, STOP, VOLUME_MUTE, VOLUME_UP, VOLUME_DOWN, BRIGHTNESS_UP, BRIGHTNESS_DOWN, MONITOR_OFF, MONITOR_ON, ESCAPE, SPACE, BACK, LWIN, CONTROL, TAB, MENU, RETURN, UP, DOWN, LEFT, RIGHT |

## Full Example

### Sample Thing

```java
Thing unifiedremote:server:xx-xx-xx-xx-xx-xx [ host="192.168.1.10" ]
```

### Sample Items

```java
Group   pcRemote    "Living room PC"
String  PC_SendKey       "Send Key"                            (pcRemote)   {  channel="unifiedremote:server:xx-xx-xx-xx-xx-xx:send-key" }
String  PC_MouseMove       "Mouse Move"                            (pcRemote)   { channel="samsungtv:tv:livingroom:mouse-move" }
```
