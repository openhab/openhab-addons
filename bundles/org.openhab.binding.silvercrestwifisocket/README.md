# Silvercrest WiFi Plug Binding

This binding integrates the Silvercrest WiFi Socket SWS-A1 sold by Lidl and the EasyHome WiFi Socket DIS-124 sold by Aldi.

## Supported Things

- Silvercrest WiFi Socket SWS-A1 - [(Owner Manual)](https://www.lidl-service.com/static/118127777/103043_FI.pdf)   --   Tested with firmware version: 1.41, 1.60, 1.70
- EasyHome WiFi Socket DIS-124 <https://www.aldi-sued.de/de/infos/aldi-sued-a-bis-z/s/serviceportal/ergebnisliste/sis/si/wifi-steckdose/>

## Discovery

The discovery of WiFi Sockets is always running in the background.
If a command is sent to the WiFi socket using the Android/iOS app or if the physical button on the device is pressed, the device will be recognized and placed in the Inbox.

## Binding Configuration

The binding does not require any special configuration.
The WiFi Socket should be connected to the same WiFi network.

## Thing Configuration

To configure a WiFi Socket manually, the MAC address and the vendor are required.
You can check the WiFi Socket MAC address in your router or using a mobile app.
Supported vendors are either Silvercrest (Lidl) or EasyHome (Aldi).

WiFi Socket Thing parameters:

| Parameter ID   | Parameter Type | Mandatory | Description                                                                   | Default          |
| -------------- | -------------- | --------- | ----------------------------------------------------------------------------- | ---------------- |
| macAddress     | text           | true      | The socket MAC address                                                        |                  |
| hostAddress    | text           | false     | The socket Host address. The binding is capable to discover the host address. |                  |
| updateInterval | integer        | false     | Update time interval in seconds to request the status of the socket.          | 60               |
| vendor         | text           | true      | The vendor of the system ("ALDI_EASYHOME" or "LIDL_SILVERCREST")              | LIDL_SILVERCREST |

E.g.

```java
Thing silvercrestwifisocket:wifiSocket:lamp [ macAddress="ACCF23343C50", vendor="ALDI_EASYHOME" ]
```

## Channels

The Silvercrest WiFi Socket supports the following channel:

| Channel Type ID | Item Type | Description         |
|-----------------|-----------|---------------------|
| switch          | Switch    | WiFi Socket Switch. |
