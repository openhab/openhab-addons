# Pushsafer Binding

The Pushsafer binding allows you to notify mobile devices of a message using the [Pushsafer API](https://www.pushsafer.com/pushapi).
To get started you first need to register (a free process) to get a Private Key.
Initially you have to register a device with one of the [client apps](https://www.pushsafer.com/apps), to get a device id.

## Supported Things

There is only one Thing available - the `pushsafer-account`.
You are able to create multiple instances of this Thing to broadcast to different devices or groups with push-notification content and setting.

## Thing Configuration

| Configuration Parameter | Type    | Description                                                                                                                                           |
|-------------------------|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| `apikey`                | text    | Your private-key to access the Pushsafer [Message API](https://www.pushsafer.com/pushapi). **mandatory**                                              |
| `user`                  | text    | Your username or email address to validate against the Pushsafer Message API. **mandatory**                                                           |
| `device`                | text    | Your device or group id to which device(s) you want to push notifications. **mandatory**                                                              |
| `title`                 | text    | The default title of a message (default: `"openHAB"`).                                                                                                |
| `format`                | text    | The default format (`"none"`, `"HTML"` or `"monospace"`) of a message (default: `none`).                                                              |
| `sound`                 | text    | The default notification sound on target device (default: `1`) (see [supported notification sounds](https://www.pushsafer.com/pushapi#api-sound)).    |
| `vibration`             | text    | How often the device should vibrate. empty=device default or a number 1-3.                                                                            |
| `icon`                  | text    | The default notification icon on target device (default: `1`) (see [supported notification icons](https://www.pushsafer.com/pushapi#api-icon)).       |
| `color`                 | text    | The color (hexadecimal) of notification icon (e.g. #FF0000).                                                                                          |
| `url`                   | text    | URL or [URL Scheme](https://www.pushsafer.com/url_schemes) send with notification.                                                                    |
| `urlTitle`              | text    | Title of URL.                                                                                                                                         |
| `retry`                 | integer | The retry parameter specifies how often (in seconds) the Pushsafer servers will send the same notification to the user (default: `300`). **advanced** |
| `expire`                | integer | The expire parameter specifies how long (in seconds) your notification will continue to be retried (default: `3600`). **advanced**                    |
| `confirm`               | integer | Integer 10-10800 (10s steps) Time in seconds after which a message should be sent again before it is confirmed. (default: `0`). **advanced**          |
| `time2live`             | integer | Time in minutes, after a message automatically gets purged (default: `0`). **advanced**                                                               |
| `answer`                | integer | 1 = enables reply to push notifications (default: `0`). **advanced**                                                                                  |

The `retry` and `expire` parameters are only used for emergency-priority notifications.

## Channels

Currently the binding does not support any Channels.

## Thing Actions

All actions return a `Boolean` value to indicate if the message was sent successfully or not.
The parameter `message` is **mandatory**, the `title` parameter defaults to whatever value you defined in the `title` related configuration parameter.

- `sendPushsaferMessage(String message, @Nullable String title)` - This method is used to send a plain text message.

- `sendPushsaferHtmlMessage(String message, @Nullable String title)` - This method is used to send a HTML message.

- `sendPushsaferMonospaceMessage(String message, @Nullable String title)` - This method is used to send a monospace message.

- `sendPushsaferAttachmentMessage(String message, @Nullable String title, String attachment, @Nullable String contentType, @Nullable String authentication)` - This method is used to send a message with an image attachment. It takes a local path or URL to the image attachment (parameter `attachment` **mandatory**), an optional `contentType` to define the content-type of the attachment (default: `"jpeg"`, possible values: `"jpeg"`, `"png"`, `"gif"`) and an optional `authentication` for the given URL to define the credentials to authenticate a user if needed (default: `""`, example: `"user:password"`).

- `sendPushsaferURLMessage(String message, @Nullable String title, String url, @Nullable String urlTitle)` - This method is used to send a message with an URL. A supplementary `url` to show with the message and a `urlTitle` for the URL, otherwise just the URL is shown.

- `sendPushsaferPriorityMessage(String message, @Nullable String title, @Nullable Integer priority)` - This method is used to send a priority message. Parameter `priority` is the priority (`-2`, `-1`, `0`, `1`, `2`) to be used (default: `2`).

## Example

demo.rules

```java
```java
val actions = getActions("pushsafer", "pushsafer:pushsafer-account:account")
// send message with attachment
actions.sendPushsaferAttachmentMessage("Hello World!", "openHAB", "/path/to/my-local-image.png", "png", null)
actions.sendPushsaferAttachmentMessage("Hello World!", "openHAB", "https://www.openhab.org/openhab-logo-square.png", "png", "user:password")
actions.sendPushsaferAttachmentMessage("Hello World!", "openHAB", "data:[<media type>][;base64],<data>", null, null)
// in case you want to send the content of an Image Item (RawType)
actions.sendPushsaferAttachmentMessage("Hello World!", "openHAB", myImageItem.state.toFullString, null, null)
```
