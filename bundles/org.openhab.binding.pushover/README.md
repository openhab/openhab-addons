# Pushover Binding

The Pushover binding allows you to notify mobile devices of a message using the [Pushover REST API](https://pushover.net/api).
To get started you first need to register (a free process) to get an API token.
Initially you have to create an application, set its name and optionally upload an icon, and get the API token in return.
Once you have the token, you need a user key (or group key) and optionally a device name for each user to which you want to push notifications.

## Supported Things

There is only one Thing available - the `pushover-account`.
You are able to create multiple instances of this Thing to broadcast to different users, groups or devices.

openHAB is listed as a _featured application_ on the [Pushover homepage](https://pushover.net/apps).
It provides a clone function to directly add a _prefilled_ application to your Pushover account and retrieve an API key.
You can reach it via [https://pushover.net/apps/clone/openHAB](https://pushover.net/apps/clone/openHAB)

## Thing Configuration

| Configuration Parameter | Type    | Description                                                                                                                                                                                                                                                                                                   |
|-------------------------|---------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `apikey`                | text    | Your API token / key (APP_TOKEN) to access the Pushover Message API. **mandatory**                                                                                                                                                                                                                            |
| `user`                  | text    | Your user key or group key (USER_KEY) to which you want to push notifications. **mandatory**                                                                                                                                                                                                                  |
| `title`                 | text    | The default title of a message (default: `openHAB`).                                                                                                                                                                                                                                                          |
| `format`                | text    | The default format (`none`, `html` or `monospace`) of a message (default: `none`).                                                                                                                                                                                                                            |
| `sound`                 | text    | The notification sound on target device (default: `default`) (see [supported notification sounds](https://pushover.net/api#sounds)). This list will be populated dynamically during runtime with 21 different sounds plus user-defined [custom sounds](https://blog.pushover.net/posts/2021/3/custom-sounds). |
| `retry`                 | integer | The retry parameter specifies how often (in seconds) the Pushover servers will send the same emergency-priority notification to the user (default: `300`). **advanced**                                                                                                                                                          |
| `expire`                | integer | The expire parameter specifies how long (in seconds) your emergency-priority notification will continue to be retried (default: `3600`). **advanced**                                                                                                                                                                            |
| `timeout`               | integer | The timeout parameter specifies maximum number of seconds a request to Pushover can take. **advanced**                                                                                                                                                                                                        |

## Channels

Currently the binding does not support any Channels.

## Thing Actions

All actions return a `Boolean` value to indicate if the message was sent successfully or not.
If the communication to Pushover servers fails the binding does not try to send the message again.
One has to take care of that on its own if it is important.
The parameter `message` is **mandatory**, the `title` parameter defaults to whatever value you defined in the `title` related configuration parameter.
Parameters declared as `@Nullable` are not optional.
One has to pass a `null` value if it should be skipped or the default value for it should be used.

- `sendMessage(String message, @Nullable String title, @Nullable String sound, @Nullable String url, @Nullable String urlTitle, @Nullable String attachment, @Nullable String contentType, @Nullable Integer priority, @Nullable String device, @Nullable Duration ttl)` - This method is used to send a plain text message providing all available parameters.

- `sendMessage(String message)` - This method is used to send a plain text message with default title.

- `sendMessage(String message, @Nullable String title)` - This method is used to send a plain text message.

- `sendMessage(String message, @Nullable String title, @Nullable Duration ttl)` - This method is used to send a plain text message with TTL.

- `sendHtmlMessage(String message, @Nullable String title)` - This method is used to send a HTML message.

- `sendHtmlMessage(String message, @Nullable String title, @Nullable Duration ttl)` - This method is used to send a HTML message with TTL.

- `sendMonospaceMessage(String message, @Nullable String title)` - This method is used to send a monospace message.

- `sendMonospaceMessage(String message, @Nullable String title, @Nullable Duration ttl)` - This method is used to send a monospace message with TTL.

- `sendAttachmentMessage(String message, @Nullable String title, String attachment, @Nullable String contentType)` - This method is used to send a message with an attachment. It takes a local path or URL to the attachment (parameter `attachment` **mandatory**). Additionally you can pass a data URI scheme to this parameter. Optionally pass a `contentType` to define the content-type of the attachment (default: `image/jpeg` or guessed from image data).

- `sendAttachmentMessage(String message, @Nullable String title, String attachment, @Nullable String contentType, @Nullable Duration ttl)` - This method is used to send a message with an attachment and TTL. See previous method for details.

- `sendURLMessage(String message, @Nullable String title, String url, @Nullable String urlTitle)` - This method is used to send a message with an URL. A supplementary `url` to show with the message and a `urlTitle` for the URL, otherwise just the URL is shown.

- `sendURLMessage(String message, @Nullable String title, String url, @Nullable String urlTitle, @Nullable Duration ttl)` - This method is used to send a message with an URL and TTL. See previous method for details.

- `sendMessageToDevice(String device, String message, @Nullable String title)` - This method is used to send a message to a specific device. Parameter `device` **mandatory** is the name of a specific device (multiple devices may be separated by a comma).

- `sendMessageToDevice(String device, String message, @Nullable String title, @Nullable Duration ttl)` - This method is used to send a message to a specific device with TTL. See previous method for details.

- `sendPriorityMessage(String message, @Nullable String title, @Nullable Integer priority)` - This method is used to send a priority message.
Parameter `priority` is the priority to be used (`-2` = lowest priority, `-1` = low priority, `0` = normal priority, `1` = high priority, `2` = emergency priority; default: `2`).
For priority `2` only, the action returns a `String` value (the `receipt`) if the message was sent successfully, otherwise `null`.
For other priorities, the action always returns an empty `String`.

- `cancelPriorityMessage(String receipt)` - This method is used to cancel an emergency priority message.
The action returns a `Boolean` value to indicate if the message was cancelled successfully or not.

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
// send message with attachment
actions.sendAttachmentMessage("Hello World!", "openHAB", "/path/to/my-local-image.png", "image/png")
actions.sendAttachmentMessage("Hello World!", "openHAB", "https://www.openhab.org/openhab-logo-square.png", null)
actions.sendAttachmentMessage("Hello World!", "openHAB", "data:[<media type>][;base64],<data>", null)
// in case you want to send the content of an Image Item (RawType)
actions.sendAttachmentMessage("Hello World!", "openHAB", myImageItem.state.toFullString, null)
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

:::: tabs

::: tab DSL

```java
val actions = getActions("pushover", "pushover:pushover-account:account")
// send expiring message
actions.sendMessage("Boost has been activated", "Recuperator", Duration.ofHours(1))
```

:::

::: tab JavaScript

```javascript
var pushoverActions = actions.thingActions('pushover', 'pushover:pushover-account:account');
// send expiring message
pushoverActions.sendMessage("Boost has been activated", "Recuperator", time.Duration.ofHours(1));
```

:::

::::
