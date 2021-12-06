# blink Binding

With this binding, you can use blink security cameras in OpenHAB.

Communication with the cameras is done using the blink API, as used by the official blink app.

Since the API can only be used for polling information, status information from the server is not received in real time.
A refresh interval can be set to poll the server for device information.

## Supported Things

## Discovery

_Describe the available auto-discovery features here. Mention for what it works and what needs to be kept in mind when
using it._

## Thing Configuration

### blink Account (used as a bridge)

The blink account is used to authenticate against the API. blink does not support OAuth or any other authorization
protocol, so the credentials have to be provided in the bridge configuration.

Configuration parameters are:

| Parameter         | Description                                    |
| ---------         | ---------------------------------------------- |
| email             | E-Mail address which is used in the blink app. |
| password          | Password which is used in the blink app.       |
| refreshInterval   | Refresh interval for device status. This should be used with caution, since there is the possibility that blink might enforce a lockout if the server gets hit too often. | 

blink has implemented a 2-factor-authentication, so after the first login, a email or text message (as configured in
your app settings)
will be sent to you. This pin code needs to be entered into a form generated for each blink account bridge.

The URL for pin verification is `<youropenhaburl>/blink/<accountUID>`. The easiest way to get this, is to copy the
validationURL thing property:

![](doc/verification-url.png)

### blink Camera

One single blink camera, belonging to a blink network (see below).

Configuration parameters should be set by discovery after configuration of an account exclusively. For completeness,
configuration parameters are:

| Parameter         | Description                       |
| ---------         | ----------------------------------|
| networkId         | Internal blink network ID         |
| cameraId          | Internal blink camera ID          |

### blink Network

A blink network basically corresponds to the blink sync modules and groups cameras. A blink network can be armed,
activating all cameras which have motion detection enabled. Cameras with motion detection in a disarmed network won't
trigger alerts.

Configuration parameters should be set by discovery after configuration of an account exclusively. For completeness,
configuration parameters are:

| Parameter         | Description                       |
| ---------         | ----------------------------------|
| networkId         | Internal blink network ID         |

## Channels

### blink Camera

| channel  | type   | description                  |
|----------|--------|------------------------------|
| motiondetection  | Switch | Enables/disables motion detection for this camera.  |
| battery | LowBattery | Read-only channel, triggering ON when battery status is low |
| temperature | Number | Read-only channel, outputting camera temperature |
| setThumbnail | Switch | Write-only channel, triggering taking a new snapshot as thumbnail |
| getThumbnail | Image | Read-only channel, return the current thumbnail |

### blink Network

| channel  | type   | description                  |
|----------|--------|------------------------------|
| armed  | Switch | Arms/disarms the network. Overrides schedules which are set in the app.  |
