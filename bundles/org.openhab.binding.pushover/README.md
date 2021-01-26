# Pushover Binding

The Pushover binding allows you to notify mobile devices of a message using the [Pushover REST API](https://pushover.net/api).
To get started you first need to register (a free process) to get an API token.
Initially you have to create an application, set its name and optionally upload an icon, and get the API token in return.
Once you have the token, you need a user key (or group key) and optionally a device name for each user to which you want to push notifications.

## Supported Things

There is only one Thing available - the `pushover-account`.
You are able to create multiple instances of this Thing to broadcast to different users, groups or devices.

## Thing Configuration

| Configuration Parameter | Type    | Description                                                                                                                                          |
|-------------------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| `apikey`                | text    | Your API token / key (APP_TOKEN) to access the Pushover Message API. **mandatory**                                                                   |
| `user`                  | text    | Your user key or group key (USER_KEY) to which you want to push notifications. **mandatory**                                                         |
| `title`                 | text    | The default title of a message (default: `openHAB`).                                                                                                 |
| `format`                | text    | The default format (`none`, `HTML` or `monospace`) of a message (default: `none`).                                                                   |
| `sound`                 | text    | The default notification sound on target device (default: `default`) (see [supported notification sounds](https://pushover.net/api#sounds)).         |
| `retry`                 | integer | The retry parameter specifies how often (in seconds) the Pushover servers will send the same notification to the user (default: `300`). **advanced** |
| `expire`                | integer | The expire parameter specifies how long (in seconds) your notification will continue to be retried (default: `3600`). **advanced**                   |

The `retry` and `expire` parameters are only used for emergency-priority notifications.

## Channels

Currently the binding does not support any Channels.

## Thing Actions

All actions return a `Boolean` value to indicate if the message was sent successfully or not.
The parameter `message` is **mandatory**, the `title` parameter defaults to whatever value you defined in the `title` related configuration parameter.

- `sendMessage(String message, @Nullable String title)` - This method is used to send a plain text message.

- `sendHtmlMessage(String message, @Nullable String title)` - This method is used to send a HTML message.

- `sendMonospaceMessage(String message, @Nullable String title)` - This method is used to send a monospace message.

- `sendAttachmentMessage(String message, @Nullable String title, String attachment, @Nullable String contentType)` - This method is used to send a message with an attachment. It takes a (local) path to the attachment (parameter `attachment` **mandatory**) and an optional `contentType` to define the content-type of the attachment (default: `image/jpeg`).

- `sendURLMessage(String message, @Nullable String title, String url, @Nullable String urlTitle)` - This method is used to send a message with an URL. A supplementary `url` to show with the message and a `urlTitle` for the URL, otherwise just the URL is shown.

- `sendMessageToDevice(String device, String message, @Nullable String title)` - This method is used to send a message to a specific device. Parameter `device` **mandatory** is the name of a specific device (multiple devices may be separated by a comma).

The `sendPriorityMessage` action returns a `String` value (the `receipt`) if the message was sent successfully, otherwise `null`.

- `sendPriorityMessage(String message, @Nullable String title, @Nullable Integer priority)` - This method is used to send a priority message. Parameter `priority` is the priority (`-2`, `-1`, `0`, `1`, `2`) to be used (default: `2`).

The `cancelPriorityMessage` returns a `Boolean` value to indicate if the message was cancelled successfully or not.

- `cancelPriorityMessage(String receipt)` - This method is used to cancel a priority message.

## Full Example

demo.things:

```java
Thing pushover:pushover-account:account [ apikey="APP_TOKEN", user="USER_KEY" ]
```

demo.rules:

```java
val actions = getActions("pushover", "pushover:pushover-account:account")
// send HTML message
actions.sendHtmlMessage("Hello <font color='green'>World</font>!", "openHAB")
```

```java
val actions = getActions("pushover", "pushover:pushover-account:account")
// send priority message
var receipt = actions.sendPriorityMessage("Emergency!!!", "openHAB", 2)

// wait for your cancel condition

if( receipt !== null ) {
    actions.cancelPriorityMessage(receipt)
    receipt = null
}
```
