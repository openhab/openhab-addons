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
| `bufferSize`      | no       |  2048   | The buffer size for the response data (in kB). |
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
| `stateTransformation  ` | yes      |      -      | One or more transformation applied to received values before updating channel. |
| `commandTransformation` | yes      |      -      | One or more transformation applied to channel value before sending to a remote. |
| `mode`                  | no       | `READWRITE` | Mode this channel is allowed to operate. `READ` means receive state, `WRITE` means send commands. |

Transformations need to be specified in the same format as 
Some channels have additional parameters.
When concatenating the `baseURL` and `stateExtions` or `commandExtension` the binding checks if a proper URL part separator (`/`, `&` or `?`) is present and adds a `/` if missing.

### Value Transformations (`stateTransformation`, `commandTransformation`)

Transformations can be used if the supplied value (or the required value) is different from what openHAB internal types require.
Here are a few examples to unwrap an incoming value via `stateTransformation` from a complex response:

| Received value                                                      | Tr. Service | Transformation                            |
|---------------------------------------------------------------------|-------------|-------------------------------------------|
| `{device: {status: { temperature: 23.2 }}}`                         | JSONPATH    | `JSONPATH:$.device.status.temperature`    |
| `<device><status><temperature>23.2</temperature></status></device>` | XPath       | `XPath:/device/status/temperature/text()` |
| `THEVALUE:23.2°C`                                                   | REGEX       | `REGEX::(.*?)°`                           |

Transformations can be chained by separating them with the mathematical intersection character "∩".
Please note that the values will be discarded if one transformation fails (e.g. REGEX did not match).

The same mechanism works for commands (`commandTransformation`) for outgoing values. 

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
| `openValue`             | no       |      -      | A special value that represents `OPEN` |
| `closedValue`           | no       |      -      | A special value that represents `CLOSED` |

### `dimmer`

| parameter               | optional | default     | description |
|-------------------------|----------|-------------|-------------|
| `onValue`               | yes      |      -      | A special value that represents `ON` |
| `offValue`              | yes      |      -      | A special value that represents `OFF` |
| `increaseValue`         | yes      |      -      | A special value that represents `INCREASE` |
| `decreaseValue`         | yes      |      -      | A special value that represents `DECREASE` |
| `step`                  | no       |      1      | The amount the brightness is increased/decreased on `INCREASE`/`DECREASE` |

All values that are not `onValue`, `offValue`, `increaseValue`, `decreaseValue` are interpreted as brightness 0-100% and need to be numeric only.

### `player`

| parameter               | optional | default     | description |
|-------------------------|----------|-------------|-------------|
| `play`                  | yes      |      -      | A special value that represents `PLAY` |
| `pause`                 | yes      |      -      | A special value that represents `PAUSE` |
| `next`                  | yes      |      -      | A special value that represents `NEXT` |
| `previous`              | yes      |      -      | A special value that represents `PREVIOUS` |
| `fastforward`           | yes      |      -      | A special value that represents `FASTFORWARD` |
| `rewind`                | yes      |      -      | A special value that represents `REWIND` |

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

**Note:** Special values need to be exact matches, i.e. no leading or trailing characters and comparison is case-sensitive.

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