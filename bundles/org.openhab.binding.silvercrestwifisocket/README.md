# Silvercrest Wifi Plug Binding

This binding integrates the Silvercrest Wifi Socket SWS-A1 sold by Lidl and the EasyHome Wifi Socket DIS-124 sold by Aldi.

## Supported Things

-   Silvercrest Wifi Socket SWS-A1 - [(Owner Manual)](http://www.lidl-service.com/static/118127777/103043_FI.pdf)   --   Tested with firmware version: 1.41, 1.60, 1.70
-   EasyHome Wifi Socket DIS-124 <https://www.aldi-sued.de/de/infos/aldi-sued-a-bis-z/s/serviceportal/ergebnisliste/sis/si/wifi-steckdose/>


## Discovery

The Discovery of Wifi Sockets is always running in the background. If a command is sent to wifi socket using the Android/iOS app or if the physical button in the device is pressed, the device will be recognized and will be placed in the Inbox.

## Binding Configuration

The binding does not require any special configuration. The Wifi Socket should be connected to the same wifi network.

## Thing Configuration

To configure a Wifi Socket manually the mac address and the vendor is required. You can check the Wifi Socket mac address in your router or using some mobile app. Supported vendors are either Silvercrest (Lidl) or EasyHome (Aldi).

Wifi Socket thing parameters:

| Parameter ID   | Parameter Type | Mandatory | Description                                                                   | Default          |
|----------------|----------------|-----------|-------------------------------------------------------------------------------|------------------|
| macAddress     | text           | true      | The socket MAC address                                                        |                  |
| hostAddress    | text           | false     | The socket Host address. The binding is capable to discover the host address. |                  |
| updateInterval | integer        | false     | Update time interval in seconds to request the status of the socket.          | 60               |
| vendor         | text           | true      | The vendor of the system ("ALDI_EASYHOME" or "LIDL_SILVERCREST")              | LIDL_SILVERCREST |

E.g.

```
Thing silvercrestwifisocket:wifiSocket:lamp [ macAddress="ACCF23343C50", vendor="ALDI_EASYHOME" ]
```

## Channels

The Silvercrest Wifi Socket support the following channel:

| Channel Type ID | Item Type | Description         |
|-----------------|-----------|---------------------|
| switch          | Switch    | Wifi Socket Switch. |
