# UnifiedRemote Binding

This binding integrates the [Unified Remote Server](https://www.unifiedremote.com/).

<b>Known Limitations: It needs the web interface to be enabled on the server settings to work.</b>

## Discovery

Discovery works on the default discovery UDP port 9511.

## Thing Configuration

The Unified Remote Server Thing requires the host to be correctly configured in order to work correctly.
Other properties like tcpPort and udpPort are not used in the initial implementation.

```
Thing unifiedremote:server:xx-xx-xx-xx-xx-xx [ host="192.168.1.10" ]
```

## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

| channel  | type   | description                  |
|----------|--------|------------------------------|
| mouse-move  | String | Mouse Move. Expect number JSON array ("[10,10]").   |
| send-key  | String | Use server key. Supported keys are: LEFT_CLICK, RIGHT_CLICK, LOCK, UNLOCK, SLEEP, SHUTDOWN, RESTART, LOGOFF, PLAY, PLAY, PAUSE, NEXT, PREVIOUS, STOP, VOLUME_MUTE, VOLUME_UP, VOLUME_DOWN, BRIGHTNESS_UP, BRIGHTNESS_DOWN, MONITOR_OFF, MONITOR_ON, ESCAPE, SPACE, BACK, LWIN, CONTROL, TAB, MENU, RETURN, UP, DOWN, LEFT, RIGHT |

