# Pushsafer Binding

The Pushsafer binding allows you to notify mobile devices of a message using the [Pushsafer API](https://www.pushsafer.com/pushapi).
To get started you first need to register (a free process) to get a Private Key.
Initially you have to register a device with one of the [client apps](https://www.pushsafer.com/apps), to get a device id.

## Supported Things

There is only one Thing available - the `pushsafer-account`.
You are able to create multiple instances of this Thing to broadcast to different devices or groups with push-notification content and setting.

## Thing Configuration

| Configuration Parameter | Type    | Description                                                                                                                                          |
|-------------------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| `apikey`                | text    | Your privtekey to access the Pushsafer Message API. **mandatory**                                                                   |
| `user`                  | text    | Your device or group id to which device(s) you want to push notifications. **mandatory**                                                         |
| `title`                 | text    | The default title of a message (default: `openHAB`).                                                                                                 |
| `format`                | text    | The default format (`none`, `HTML` or `monospace`) of a message (default: `none`).                                                                   |
| `sound`                 | text    | The default notification sound on target device (default: `default`) (see [supported notification sounds](https://www.pushsafer.com/pushapi#api-sound)).         |
| `retry`                 | integer | The retry parameter specifies how often (in seconds) the Pushsafer servers will send the same notification to the user (default: `300`). **advanced** |
| `expire`                | integer | The expire parameter specifies how long (in seconds) your notification will continue to be retried (default: `3600`). **advanced**                   |

The `retry` and `expire` parameters are only used for emergency-priority notifications.

## Channels

Currently the binding does not support any Channels.

## Thing Actions

All actions return a `Boolean` value to indicate if the message was sent successfully or not.
The parameter `message` is **mandatory**, the `title` parameter defaults to whatever value you defined in the `title` related configuration parameter.

- `sendPushsaferMessage(String message, @Nullable String title)` - This method is used to send a plain text message.

- `sendPushsaferHtmlMessage(String message, @Nullable String title)` - This method is used to send a HTML message.

- `sendPushsaferMonospaceMessage(String message, @Nullable String title)` - This method is used to send a monospace message.

- `sendPushsaferAttachmentMessage(String message, @Nullable String title, String attachment, @Nullable String contentType, @Nullable String authentfication)` - This method is used to send a message with an image attachment. It takes a local path or url to the image attachment (parameter `attachment` **mandatory**), an optional `contentType` to define the content-type of the attachment (default: `jpeg`) and an optional `authentfication` to define the authentfication if needed (default: ``, example: `user:password`).

- `sendPushsaferURLMessage(String message, @Nullable String title, String url, @Nullable String urlTitle)` - This method is used to send a message with an URL. A supplementary `url` to show with the message and a `urlTitle` for the URL, otherwise just the URL is shown.

- `sendPushsaferPriorityMessage(String message, @Nullable String title, @Nullable Integer priority)` - This method is used to send a priority message. Parameter `priority` is the priority (`-2`, `-1`, `0`, `1`, `2`) to be used (default: `2`).

## Full Example

demo.things:

```java
Thing pushsafer:pushsafer-account:account [ apikey="PRIVATE_KEY", user="DEVICE_ID" ]
```

demo.rules:

```java
val actions = getActions("pushsafer", "pushsafer:pushsafer-account:account")
// send HTML message (BBCode)
actions.sendPushsaferHtmlMessage("Hello [b]World[/b]!", "openHAB3")
```

```java
val actions = getActions("pushsafer", "pushsafer:pushsafer-account:account")
// send priority message
var response = actions.sendPushsaferPriorityMessage("Emergency!!!", "openHAB3", 2)
```

```java
val actions = getActions("pushsafer", "pushsafer:pushsafer-account:account")
// send a message with an image url from ip camera (with authentfication)
var response = actions.sendPushsaferAttachmentMessage("Motion Detection!!!", "openHAB3", "http://192.168.2.222:8088/tmpfs/snap.jpg", "jpeg", "admin:password")
```

```java
val actions = getActions("pushsafer", "pushsafer:pushsafer-account:account")
// send a message with an local image
var response = actions.sendPushsaferAttachmentMessage("Message with Image!", "openHAB3", "/openhab3/html/image.gif", "gif", "")
```
