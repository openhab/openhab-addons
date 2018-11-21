# HTTP Binding

This binding can be used to make HTTP requests to fetch and update device state.

## Supported Things

This binding supports Things of type:

| Thing Type      | Item Type     | Read-only |
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

## Discovery

Automatic discovery is not supported.

## Binding Configuration

The binding has the following configuration options:

| Parameter      | Name            | Description                                                          | Required | Default value |
|----------------|-----------------|----------------------------------------------------------------------|----------|---------------|
| connectTimeout | Connect Timeout | Milliseconds before HTTP client will give up trying to connect.      | yes      | 3000ms        |
| requestTimeout | Request Timeout | Milliseconds before HTTP client will give up waiting for a response. | yes      | 5000ms        |

## Thing Configuration

### State Configiration

| Parameter              | Name                          | Description                                                                                    | Default Value |
|------------------------|-------------------------------|------------------------------------------------------------------------------------------------|---------------|
| stateUrl               | State URL                     | URL to fetch to retrieve the state of the Thing.                                               |               |
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
| commandMethod            | Command HTTP Method             | HTTP Method to use when sending a command to the Thing.                                   | POST                      |
| commandUrl               | Command URL                     | URL to request to change the state of the Thing.                                          |                           |
| commandContentType       | Command Content Type            | The content type to use for the HTTP request.                                             | text/plain; charset=utf-8 |
| commandRequestTransform  | Command Request Transformation  | A transformation rule used to transform the command before it is sent to the Command URL. |                           |
| commandResponseTransform | Command Response Transformation | A transformation rule used to transform the HTTP response from the Command URL.           |                           |

If `commandMethod` is set to POST, the command to be sent will be provided
in the entity body of the request.  If set to GET, the command can be
interpolated into the `commandUrl` by specifying a `%s` somewhere in the URL.

The expected Command values differ depending on Thing type:

* color: `ON`, `OFF`, `INCREASE`, `DECREASE`, or a comma-separated Hue,Saturation,Brightness (HSB) value such as `250,80,100`
* dimmer: `ON`, `OFF`, `INCREASE`, `DECREASE`, or a percentage value such as `42`
* number: a number, such as `42` or `3.14`
* player: `PLAY`, `PAUSE`, `NEXT`, `PREVIOUS`, `REWIND`, or `FASTFORWARD`
* rollershutter: `UP`, `DOWN`, `STOP`, or `MOVE`, or a number indicating how open/closed the shutter should be
* string: a bare string, such as `I like home automation`
* switch: `ON` or `OFF`

## Channels

| Channel Type ID | Item Type    | Description                             |
|-----------------|--------------|-----------------------------------------|
| state           | (various)    | The Thing-type-dependent state received |

## Full Example

Things:

```
Thing http:dimmer:dimmer_light_1 [ stateUrl="https://example.com/dimmer_light_1" commandMethod="POST" commandUrl="https://example.com/dimmer_light_1" ]
Thing http:image:image_1 [ stateUrl="https://example.com/foo.jpg" ]
```

Items:

```
Dimmer DimmerLight1 { channel="http:dimmer_light_1:state" }
Image Image1 { channel="http:image_1:state" }
```

Sitemap:

```
Frame label="HTTP Test" {
    Dimmer item=DimmerLight1
    Image item=Image1
}
```

## Use case example

The binding can be used to adapt regular web services with arbitrary
request/response semantics to be used with openHAB. For example, say
I live in an apartment complex and the main door to the building can
be opened using a HTTP request.  I can model this with a Thing:

```
Thing http:switch:front_door [ stateUrl="https://example.com/building-front-door" stateRefreshInterval="5000" commandMethod="POST" commandUrl="https://example.com/building-front-door" ]
```

... and an Item:

```
Switch FrontDoor { channel="http:front_door:state" }
```

For display, I can also create a Sitemap:

```
Frame label="Door Control" icon="door" {
    Switch item=FrontDoor
}
```
