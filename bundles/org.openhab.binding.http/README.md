# HTTP Binding

This binding allows using HTTP to bring external data into openHAB or execute HTTP requests on commands.  

## Supported Things

Only one thing named `url` is available.
It can be extended with different channels.

## Thing Configuration

| parameter         | optional | default | description |
|-------------------|----------|---------|-------------|
| `baseURL`         | no       |    -    | The base URL for this thing. Can be extended in channel-configuration. |
| `refresh`         | no       |   30    | Time in seconds between two refresh calls for the channels of this thing. |
| `timeout`         | no       |  3000   | Timeout for HTTP requests in ms. |
| `username`        | yes      |    -    | Username for authentication (advanced parameter). |
| `password`        | yes      |    -    | Password for authentication (advanced parameter). |
| `authMode`        | no       |  BASIC  | Authentication mode, `BASIC` or `DIGEST` (advanced parameter). |
| `commandMethod`   | no       |   GET   | Method used for sending commands `GET`, `PUT`, `POST`. |
| `contentType`     | yes      |    -    | MIME content-type of the command requests. Only used for  `PUT` and `POST`. |
| `encoding`        | yes      |    -    | Encoding to be used if no encoding is found in responses (advanced parameter). |  
| `headers`         | yes      |    -    | Additional headers that are sent along with the request. Format is "header=value".| 
| `ignoreSSLErrors` | no       |  false  | If set to true ignores invalid SSL certificate errors. This is potentially dangerous.|

*Note:* optional "no" means that you have to configure a value unless a default is provided and you are ok with that setting.

## Channels

Each item type has its own channel-type.
Depending on the channel-type, channels have different configuration options.
All channel-types (except `image`) have `stateExtension`, `commandExtension`, `stateTransformation`, `commandTransformation` and `mode` parameters.
The `image` channel-type supports `stateExtension` only.

| parameter               | optional | default     | description |
|-------------------------|----------|-------------|-------------|
| `stateExtension`        | yes      |      -      | Appended to the `baseURL` for requesting states. |
| `commandExtension`      | yes      |      -      | Appended to the `baseURL` for sending commands. If empty, same as `stateExtension`. |
| `stateTransformation  ` | yes      |      -      | One or more transformation (concatenated with `∩`) applied to received values before updating channel. |
| `commandTransformation` | yes      |      -      | One or more transformation (concatenated with `∩`) applied to channel value before sending to a remote. |
| `mode`                  | no       | `READWRITE` | Mode this channel is allowed to operate. `READ` means receive state, `WRITE` means send commands. |

Some channels have additional parameters.
When concatenating the `baseURL` and `stateExtions` or `commandExtension` the binding checks if a proper URL part separator (`/`, `&` or `?`) is present and adds a `/` if missing.

### `color`

| parameter               | optional | default     | description |
|-------------------------|----------|-------------|-------------|
| `onValue`               | yes      |      -      | A special value that represents `ON` |
| `offValue`              | yes      |      -      | A special value that represents `OFF` |
| `increaseValue`         | yes      |      -      | A special value that represents `INCREASE` |
| `decreaseValue`         | yes      |      -      | A special value that represents `DECREASE` |
| `step`                  | no       |      1      | The amount the brightness is increased/decreased on `INCREASE`/`DECREASE` |
| `colorMode`             | no       |    RGB      | Mode for color values: `RGB` or `HSB` |

All values that are not `onValue`, `offValue`, `increaseValue`, `decreaseValue` are interpreted as color value (according to the color mode) in the format `r,g,b` or `h,s,v`.

### `contact`

| parameter               | optional | default     | description |
|-------------------------|----------|-------------|-------------|
| `openValue`             | no      |      -      | A special value that represents `OPEN` |
| `closedValue`           | no      |      -      | A special value that represents `CLOSED` |

### `dimmer`

| parameter               | optional | default     | description |
|-------------------------|----------|-------------|-------------|
| `onValue`               | yes      |      -      | A special value that represents `ON` |
| `offValue`              | yes      |      -      | A special value that represents `OFF` |
| `increaseValue`         | yes      |      -      | A special value that represents `INCREASE` |
| `decreaseValue`         | yes      |      -      | A special value that represents `DECREASE` |
| `step`                  | no       |      1      | The amount the brightness is increased/decreased on `INCREASE`/`DECREASE` |

All values that are not `onValue`, `offValue`, `increaseValue`, `decreaseValue` are interpreted as brightness 0-100% and need to be numeric only.

### `rollershutter`

| parameter               | optional | default     | description |
|-------------------------|----------|-------------|-------------|
| `upValue`               | yes      |      -      | A special value that represents `UP` |
| `downValue`             | yes      |      -      | A special value that represents `DOWN` |
| `stopValue`             | yes      |      -      | A special value that represents `STOP` |
| `moveValue`             | yes      |      -      | A special value that represents `MOVE` |

All values that are not `upValue`, `downValue`, `stopValue`, `moveValue` are interpreted as position 0-100% and need to be numeric only.
                    
### `switch`

| parameter               | optional | default     | description |
|-------------------------|----------|-------------|-------------|
| `onValue`               | no       |      -      | A special value that represents `ON` |
| `offValue`              | no       |      -      | A special value that represents `OFF` |

## URL Formatting

After concatenation of the `baseURL` and the `commandExtension` or the `stateExtension` (if provided) the URL is formatted using the [java.util.Formatter](http://docs.oracle.com/javase/6/docs/api/java/util/Formatter.html).
The URL is used as format string and two parameters are added:

- the current date (referenced as `%1$`)
- the transformed command (referenced as `%2$`)

After the parameter reference the format needs to be appended.
See the link above for more information about the available format parameters (e.g. to use the string representation, you need to append `s` to the reference).
When sending an OFF command on 2020-07-06, the URL

```
http://www.domain.org/home/lights/23871/?status=%2$s&date=%1$tY-%1$tm-%1$td
``` 

is transformed to 

```
http://www.domain.org/home/lights/23871/?status=OFF&date=2020-07-06
```

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._
