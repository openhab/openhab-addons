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
| `delay`           | no       |    0    | Delay between two requests in ms (advanced parameter). |
| `username`        | yes      |    -    | Username for authentication (advanced parameter). |
| `password`        | yes      |    -    | Password for authentication (advanced parameter). |
| `authMode`        | no       |  BASIC  | Authentication mode, `BASIC`, `BASIC_PREEMPTIVE` or `DIGEST` (advanced parameter). |
| `stateMethod`     | no       |   GET   | Method used for requesting the state: `GET`, `PUT`, `POST`. |
| `commandMethod`   | no       |   GET   | Method used for sending commands: `GET`, `PUT`, `POST`. |
| `contentType`     | yes      |    -    | MIME content-type of the command requests. Only used for  `PUT` and `POST`. |
| `encoding`        | yes      |    -    | Encoding to be used if no encoding is found in responses (advanced parameter). |
| `headers`         | yes      |    -    | Additional headers that are sent along with the request. Format is "header=value". Multiple values can be stored as `headers="key1=value1", "key2=value2", "key3=value3",`. When using text based configuration include at minimum 2 headers to avoid parsing errors.|
| `ignoreSSLErrors` | no       |  false  | If set to true ignores invalid SSL certificate errors. This is potentially dangerous.|

_Note:_ Optional "no" means that you have to configure a value unless a default is provided and you are ok with that setting.

_Note:_ The `BASIC_PREEMPTIVE` mode adds basic authentication headers even if the server did not request authentication.
This is dangerous and might be misused.
The option exists to be able to authenticate when the server is not sending the proper 401/Unauthorized code.
Authentication might fail if redirections are involved as headers are stripper prior to redirection.

_Note:_ If you rate-limit requests by using the `delay` parameter you have to make sure that the time between two refreshes is larger than the time needed for one refresh cycle.

**Attention:** `baseUrl` (and `stateExtension`/`commandExtension`) should not normally use escaping (e.g. `%22` instead of `"` or `%2c` instead of `,`).
URLs are properly escaped by the binding itself before the request is sent.
Using escaped strings in URL parameters may lead to problems with the formatting (see below).

In certain scenarios you may need to manually escape your URL, for example if you need to include an escaped `=` (`%3D`) in this scenario include `%%3D` in the URL to preserve the `%` during formatting, and set the parameter `escapedUrl` to true on the channel.

## Channels

Each item type has its own channel-type.
Depending on the channel-type, channels have different configuration options.
All channel-types (except `image`) have `stateExtension`, `commandExtension`, `stateTransformation`, `commandTransformation` and `mode` parameters.
The `image` channel-type supports `stateExtension`, `stateContent` and `escapedUrl` only.

| parameter               | optional | default     | description |
|-------------------------|----------|-------------|-------------|
| `stateExtension`        | yes      |      -      | Appended to the `baseURL` for requesting states. |
| `commandExtension`      | yes      |      -      | Appended to the `baseURL` for sending commands. If empty, same as `stateExtension`. |
| `stateTransformation`   | yes      |      -      | One or more transformation applied to received values before updating channel. |
| `commandTransformation` | yes      |      -      | One or more transformation applied to channel value before sending to a remote. |
| `escapedUrl`            | yes      |      -      | This specifies whether the URL is already escaped. |
| `stateContent`          | yes      |      -      | Content for state requests (if method is `PUT` or `POST`) |
| `mode`                  | no       | `READWRITE` | Mode this channel is allowed to operate. `READONLY` means receive state, `WRITEONLY` means send commands. |

Transformations need to be specified in the same format as
Some channels have additional parameters.
When concatenating the `baseURL` and `stateExtension` or `commandExtension` the binding checks if a proper URL part separator (`/`, `&` or `?`) is present and adds a `/` if missing.

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

### `number`

| parameter               | optional | default     | description |
|-------------------------|----------|-------------|-------------|
| `unit`                  | yes      |      -      | The unit label for this channel |

`number` channels can be used for `DecimalType` or `QuantityType` values.
If a unit is given in the `unit` parameter, the binding tries to create a `QuantityType` state before updating the channel, if no unit is present, it creates a `DecimalType`.
Please note that incompatible units (e.g. `°C` for a `Number:Density` item) will fail silently, i.e. no error message is logged even if the state update fails.

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

After concatenation of the `baseURL` and the `commandExtension` or the `stateExtension` (if provided) the URL is formatted using the [java.util.Formatter](https://docs.oracle.com/javase/6/docs/api/java/util/Formatter.html).
The URL is used as format string and two parameters are added:

- the current date (referenced as `%1$`)
- the transformed command (referenced as `%2$`)

After the parameter reference the format needs to be appended.
See the link above for more information about the available format parameters (e.g. to use the string representation, you need to append `s` to the reference, for a timestamp `t`).
When sending an OFF command on 2020-07-06, the URL

```text
http://www.domain.org/home/lights/23871/?status=%2$s&date=%1$tY-%1$tm-%1$td
```

is transformed to

```text
http://www.domain.org/home/lights/23871/?status=OFF&date=2020-07-06
```

## Examples

### `demo.things`

```java
Thing http:url:foo "Foo" [
    baseURL="https://example.com/api/v1/metadata-api/web/metadata",
    headers="key1=value1", "key2=value2", "key3=value3",
    refresh=15] {
        Channels:
         Type string : text "Text" [ stateTransformation="JSONPATH:$.metadata.data" ]
}
```
