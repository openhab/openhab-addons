# Silvercrest Wi-Fi Plug Binding

This binding integrates the Silvercrest Wi-Fi Socket SWS-A1 sold by Lidl and the EasyHome Wi-Fi Socket DIS-124 sold by Aldi.

## Supported Things

- Silvercrest Wi-Fi Socket SWS-A1 - [(Owner Manual)](https://www.lidl-service.com/static/118127777/103043_FI.pdf)   --   Tested with firmware version: 1.41, 1.60, 1.70
- EasyHome Wi-Fi Socket DIS-124 <https://www.aldi-sued.de/de/infos/aldi-sued-a-bis-z/s/serviceportal/ergebnisliste/sis/si/wifi-steckdose/>

## Discovery

The discovery of Wi-Fi Sockets is always running in the background.
If a command is sent to the Wi-Fi socket using the Android/iOS app or if the physical button on the device is pressed, the device will be recognized and placed in the Inbox.

## Binding Configuration

The binding does not require any special configuration.
The Wi-Fi Socket should be connected to the same Wi-Fi network.

## Thing Configuration

To configure a Wi-Fi Socket manually, the MAC address and the vendor are required.
You can check the Wi-Fi Socket MAC address in your router or using a mobile app.
Supported vendors are either Silvercrest (Lidl) or EasyHome (Aldi).

Wi-Fi Socket Thing parameters:

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

The Silvercrest Wi-Fi Socket supports the following channel:

| Channel Type ID | Item Type | Description         |
|-----------------|-----------|---------------------|
| switch          | Switch    | Wi-Fi Socket Switch. |
