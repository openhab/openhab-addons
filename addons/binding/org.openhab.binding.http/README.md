# HTTP Binding

This binding can be used to make HTTP requests to fetch and update device state.

## Supported Things

This binding supports a single thing type: `http`.

## Channels

There are no built-in channels; instead channels are added dynamically by
the user.  Each channel can be configured with different URLs to fetch and
update state.

These channel types are supported:

| Channel Type    | Item Type     | Read-only |
|-----------------|---------------|-----------|
| `color`         | Color         | no        |
| `contact`       | Contact       | yes       |
| `datetime`      | DateTime      | yes       |
| `dimmer`        | Dimmer        | no        |
| `image`         | Image         | yes       |
| `location`      | Location      | yes       |
| `number`        | Number        | no        |
| `player`        | Player        | no        |
| `rollershutter` | Rollershutter | no        |
| `string`        | String        | no        |
| `switch`        | Switch        | no        |

  
Configuration is as follows.

### State Configiration

| Parameter              | Name                          | Description                                                                                    | Default Value |
|------------------------|-------------------------------|------------------------------------------------------------------------------------------------|---------------|
| stateUrl               | State URL                     | URL to fetch to retrieve the state of the Channel.                                             |               |
| stateConnectTimeout    | State Connect Timeout         | Milliseconds before HTTP client will give up trying to connect.                                | 3000ms        |
| stateRequestTimeout    | State Request Timeout         | Milliseconds before HTTP client will give up waiting for a response.                           | 5000ms        |
| stateRefreshInterval   | State Refresh Interval        | How often (in milliseconds) to refresh state by fetching the State URL.                        | 60000ms       |
| stateResponseTransform | State Response Transformation | A transformation function that will transform the HTTP response into a recognized state value. |               |

The expected State values differ depending on Thing type:

* color: `ON`, `OFF`, or a comma-separated Hue,Saturation,Brightness (HSB) value such as `250,80,100`
* contact: `OPEN` or `CLOSED`
* datetime: an ISO-8601-formatted date/time string
* dimmer: `ON`, `OFF`, or a percentage value such as `42`
* image: raw image data
* location: a comma-separated Latitude,Longitude,Altitude value such as `42.1256,-120.4316,88.22`
* number: a number, such as `42` or `3.14`
* player: `PLAY` or `PAUSE`
* rollershutter: `OPEN`, `CLOSED`, `ON`, `OFF`, or a number indicating how open/closed the shutter is
* string: a bare string, such as `I like home automation`
* switch: `ON` or `OFF`

### Command Configuration

| Parameter                | Name                            | Description                                                                               | Default Value             |
|--------------------------|---------------------------------|-------------------------------------------------------------------------------------------|---------------------------|
| commandMethod            | Command HTTP Method             | HTTP Method to use when sending a command to the Channel.                                 | POST                      |
| commandUrl               | Command URL                     | URL to request to change the state of the Channel.                                        |                           |
| commandConnectTimeout    | Command Connect Timeout         | Milliseconds before HTTP client will give up trying to connect.                           | 3000ms        |
| commandRequestTimeout    | Command Request Timeout         | Milliseconds before HTTP client will give up waiting for a response.                      | 5000ms        |
| commandContentType       | Command Content Type            | The content type to use for the HTTP request.                                             | text/plain; charset=utf-8 |
| commandRequestTransform  | Command Request Transformation  | A transformation rule used to transform the command before it is sent to the Command URL. |                           |
| commandResponseTransform | Command Response Transformation | A transformation rule used to transform the HTTP response from the Command URL.           |                           |

If `commandMethod` is set to POST, the command to be sent will be provided
in the entity body of the request.  If set to GET, the command can be
interpolated into the `commandUrl` by specifying a `%s` somewhere in the URL.

The expected Command values differ depending on Channel type:

* color: `ON`, `OFF`, `INCREASE`, `DECREASE`, or a comma-separated Hue,Saturation,Brightness (HSB) value such as `250,80,100`
* dimmer: `ON`, `OFF`, `INCREASE`, `DECREASE`, or a percentage value such as `42`
* number: a number, such as `42` or `3.14`
* player: `PLAY`, `PAUSE`, `NEXT`, `PREVIOUS`, `REWIND`, or `FASTFORWARD`
* rollershutter: `UP`, `DOWN`, `STOP`, or `MOVE`, or a number indicating how open/closed the shutter should be
* string: a bare string, such as `I like home automation`
* switch: `ON` or `OFF`

## Full Example

Say we have a building with a door security system that allows you to send a
HTTP request to unlock the door, and also has a camera that exports a JPEG
image to the HTTP server every few seconds.  We could represent that as
follows:

Things:

```
Thing http:http:front_door_switch {
    Channels:
        Type switch : door_open "Door Open" [ stateUrl="https://example.com/door_open" commandMethod="POST" commandUrl="https://example.com/door_open" ]
        Type image : door_camera "Door Camera" [ stateUrl="https://example.com/door_camera.jpg" refreshInterval="5000" ]
}
```

Items:

```
Switch DoorOpen { channel="http:front_door_switch:door_open" }
Image Image1 { channel="http:front_door:door_camera" }
```

Sitemap:

```
Frame label="Front Door" {
    Swith item=Open Door
    Image item=Exterior Camera
}
```
