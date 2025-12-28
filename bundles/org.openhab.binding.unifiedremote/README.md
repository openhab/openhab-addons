# UnifiedRemote Binding

This binding integrates the [Unified Remote Server](https://www.unifiedremote.com/).

<b>Known limitation: The web interface must be enabled in the server settings for this binding to work.</b>

## Discovery

Discovery works on the default discovery UDP port 9511.

## Thing Configuration

The only supported Thing is the Unified Remote Server, which requires the hostname to be correctly configured to work.

| ThingTypeID | Description               |
|-------------|---------------------------|
| server      | Unified Remote Server     |

| Parameter | Type   | Description                             |
|-----------|--------|-----------------------------------------|
| host      | String | Unified Remote Server IP or hostname    |

## Channels

| Channel    | Type   | Description                                                                                                                                                                                                                                                                                                                      |
|------------|--------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| mouse-move | String | Relative mouse movement in pixels. Expects a JSON array of two numbers [x, y], e.g., "[10,10]".                                                                                                                                                                                                                                  |
| send-key   | String | Send a server key command. Supported keys: LEFT_CLICK, RIGHT_CLICK, LOCK, UNLOCK, SLEEP, SHUTDOWN, RESTART, LOGOFF, PLAY/PAUSE, NEXT, PREVIOUS, STOP, VOLUME_MUTE, VOLUME_UP, VOLUME_DOWN, BRIGHTNESS_UP, BRIGHTNESS_DOWN, MONITOR_OFF, MONITOR_ON, ESCAPE, SPACE, BACK, LWIN, CONTROL, TAB, MENU, RETURN, UP, DOWN, LEFT, RIGHT |

## Full Example

### Sample Thing

```java
Thing unifiedremote:server:xx-xx-xx-xx-xx-xx [ host="192.168.1.10" ]
```

### Sample Items

```java
Group   pcRemote    "Living room PC"
String  PC_SendKey       "Send Key"                            (pcRemote)   {  channel="unifiedremote:server:xx-xx-xx-xx-xx-xx:send-key" }
String  PC_MouseMove       "Mouse Move"                            (pcRemote)   { channel="unifiedremote:server:xx-xx-xx-xx-xx-xx:mouse-move" }
```
