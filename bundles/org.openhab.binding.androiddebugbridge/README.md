# AndroidDebugBridge Binding

This binding allows to connect to android devices through the adb protocol. The device needs to have **usb debugging enabled** and **allow debugging over tcp**, some devices allow to enable this in the device options but others need a previous connection through adb or even be rooted. If you are not familiar with adb I suggest you to search "How to enable adb over wifi on <device name>" or something like that.

## Supported Things

As I said this binding allow to connect openHAB with android devices on the current network.

This binding was tested on the FireStick (android version 7.1.2, volume control not working) and Nexus5x (android version 8.1.0, everything works nice), please update this document if you test with other android versions to reflect the compatibility of the biding. 

## Discovery

As I can not find a way to identify android devices in the network the discovery will try to connect through adb to all the reachable ip in the defined range, you could customize the discovery process through the binding options. **Your device will prop a message requesting you to authorize the connection, you should check the option "Always allow connections from this device" (or something similar) and accept**.

## Binding Configuration

| Config   |  Type  | description                  |
|----------|----------|------------------------------|
| discoveryPort | int | Port used on discovery to connect to the device through adb |
| discoveryReachableMs | int | Milliseconds to wait while discovering to determine if the ip is reachable |
| discoveryIpRangeMin | int | Used to limit the numbers of ips checked while discovering |
| discoveryIpRangeMax | int | Used to limit the numbers of ips checked while discovering |

## Thing Configuration

| ThinTypeID   | description                  |
|----------|------------------------------|
| android | Android device |

| Config   |  Type  | description                  |
|----------|----------|------------------------------|
| ip | String | Device ip address |
| port | int | Device port listening to adb connections |
| refreshTimeSec | int | Seconds between device status refreshes (default: 30) |

## Channels

| channel  | type   | description                  |
|----------|--------|------------------------------|
| key-event  | String | Send key event to android device |
| text  | String | Send text to android device |
| media-volume  | Dimmer | Set or get media volume level on android device |
| media-control  | Player | Control media on android device |
| start-package  | String | Run application by package name |
| stop-package  | String | Stop application by package name |
| current-package  | String | Package name of the top application in screen |

## Full Example


### Sample Thing

```
Thing androiddebugbridge:android:xxxxxxxxxxxx [ ip="192.168.1.10" port=5555 refreshTimeSec=30 ]
```

### Sample Items

```
Group   androidDevice    "Android TV"
String  device_SendKey       "Send Key"                            (androidDevice)   {  channel="androiddebugbridge:android:xxxxxxxxxxxx:key-event" }
String  device_CurrentApp       "Current App"                            (androidDevice)   { channel="androiddebugbridge:android:xxxxxxxxxxxx:current-package" }
```
