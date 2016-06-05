# Silvercrest Binding

This binding integrates the things from [Silvercrest sold by Lidl](http://www.lidl.de/de/silvercrest/).

## Supported Things

- Silvercrest Wifi Socket SWS-A1 - [(Owner Manual)](http://www.lidl-service.com/static/118127777/103043_FI.pdf)   --   Tested with firmware version: 1.41, 1.60, 1.70


## Discovery

The Discovery of Wifi Socket is always running in background. When one command is sent to wifi socket using the android/ios app, or if the physical in the device is pressed, the device is recognized and is placed into the Inbox.

## Binding Configuration

The binding does not require any special configuration.

## Thing Configuration

To configure one Wifi Socket manually is only required the mac address from the socket. You can check it in your router or using some mobile app.

Wifi Socket thing parameters:

| Parameter ID | Parameter Type | Mandatory | Description | Default |
|--------------|----------------|------|------------------|-----|
|macAddress|text|true|The socket MAC address| -- |
|hostAddress| text|false|The socket Host address. The binding is capable to discover the host address.| -- |
|wifiSocketOutletUpdateInterval|integer|false|Update time interval in seconds to request the status of the socket| 60 |


E.g.

```
Thing silvercrest:wifiSocketOutlet:lamp [ macAddress="ACCF23343C50" ]
```

## Channels

TV's support the following channels:

| Channel Type ID | Item Type    | Description  |
|-----------------|------------------------|--------------|
| wifi-socket-outlet-switch | Switch | Wifi Socket Outlet Switch. |