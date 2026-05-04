# Ntfy Binding

The Ntfy binding enables openHAB to publish notifications to Ntfy-compatible servers (for example [ntfy.sh](https://ntfy.sh) or a self-hosted ntfy-compatible endpoint).

Ntfy is a simple HTTP-based notification service and message broker; see [ntfy.sh](https://ntfy.sh) for details and public servers.

It is intended for integrations where openHAB should push alert or informational messages to mobile or desktop clients that support the Ntfy protocol. The binding supports basic text messages as well as common rich features supported by the protocol: message priority, tags, icon URLs, attachments, click actions and simple action buttons.

Typical uses include doorbell alerts, security notifications, system health messages, or any automation that should notify users in real time via an Ntfy-compatible notification channel.

## Supported Things

This binding provides the following Thing types. The Thing type IDs match the definitions in `src/main/resources/OH-INF/thing/thing-types.xml`.

- `ntfy:ntfyConnection` (bridge) — Represents a connection to an Ntfy server. Configure the bridge with the server hostname (for example `https://ntfy.sh`) and optional credentials (username/password). The bridge holds shared connection settings (hostname, username, password, connectionTimeout) which are used by topic Things.
- `ntfy:ntfyTopic` (Thing) — Represents a topic/channel on a configured Ntfy server. Each topic Thing must be associated with an `ntfyConnection` bridge and requires the `topicname` configuration parameter.

## Thing Configuration

### `ntfyConnection` Bridge Configuration

| Name              | Type    | Description                                                                              | Default                            | Required | Advanced |
|-------------------|---------|------------------------------------------------------------------------------------------|------------------------------------|----------|----------|
| hostname          | text    | Base URL of the ntfy server                                                              | [https://ntfy.sh](https://ntfy.sh) | yes      | no       |
| username          | text    | Optional username for basic auth                                                         | N/A                                | no       | no       |
| password          | text    | Optional password - if username is provided basic auth is used else Bearer token is used | N/A                                | no       | no       |
| connectionTimeout | integer | WebSocket / HTTP connection timeout ms                                                   | 60000                              | no       | yes      |

Configure the `ntfyConnection` as a bridge to hold shared server and authentication settings. Topic Things reference the bridge to reuse these settings. For authentication with access token only set the password and leave the username empty.

### `ntfyTopic` Thing Configuration

| Name      | Type | Description               | Default | Required | Advanced |
|-----------|------|---------------------------|---------|----------|----------|
| topicname | text | Name of the topic/channel | N/A     | yes      | no       |

## Channels

### `ntfyTopic` Channels

| Channel         | Type     | Read/Write | Description                                 |
|-----------------|----------|------------|---------------------------------------------|
| lastMessage     | String   | R          | Last received message payload (read-only)   |
| lastMessageTime | DateTime | R          | Timestamp of the last message (read-only)   |
| lastMessageId   | String   | R          | ID of the last message (read-only)          |

## Full Example

Below are examples for textual configuration files showing a bridge (`ntfyConnection`), a topic Thing (`ntfyTopic`), Items bound to the Thing's channels, and a simple sitemap to display the last message and its timestamp.

### Thing Configuration

```things
Bridge ntfy:ntfyConnection:myConn "Ntfy Server" [ hostname="https://ntfy.sh", connectionTimeout=60000 ]

Thing ntfy:ntfyTopic:home "Front Door Notifications" (ntfy:ntfyConnection:myConn) [ topicname="home" ]
```

If your server requires authentication, supply `username` and `password` in the bridge configuration. The `topicname` must be provided for each `ntfyTopic` Thing.

### Item Configuration (items file)

Bind Items to the read-only channels exposed by the topic Thing to show the last received message and its timestamp:

```items
String FrontDoorLastMessage "Front Door Message" { channel="ntfy:ntfyTopic:home:lastMessage" }
DateTime FrontDoorMessageTime "Last message received" { channel="ntfy:ntfyTopic:home:messageTime" }
```

Note: Sending notifications from openHAB to ntfy is done via the binding's Actions from rules (the binding exposes Actions to publish/delete messages). The channels above are read-only and show incoming or last-state values.

### Sitemap Configuration (sitemap file)

Simple sitemap demonstrating how to display the last message and its timestamp:

```sitemap
 sitemap notifications label="Notifications"
{
    <Frame label="Front Door">
        Text item=FrontDoorLastMessage label="Last message [%s]"
        Text item=FrontDoorMessageTime label="Received [%1$td.%1$tm.%1$tY %1$tR]"
    </Frame>
}
```

## Actions (Rules DSL and JavaScript)

This binding exposes Rule Actions to send and manage messages on a configured topic. Actions are available for use from the Rules DSL and the ECMAScript/JavaScript automation scripts. The actions support a builder-style API to configure a message (message text, priority, tags, icon, attachments, actions, sequence id, ...) and then send it.

Important: Always check that the returned actions object is not null (the Thing must exist and be handled by the binding) before invoking methods.

### Available action methods

Below is a quick reference of the builder-style methods provided by the binding actions. Use these to construct messages before calling `send()`.

| Method              | Parameters                                                                                        | Description                                                          | Ntfy docs                                                                    |
|---------------------|---------------------------------------------------------------------------------------------------|----------------------------------------------------------------------|------------------------------------------------------------------------------|
| withMessage         | (String message)                                                                                  | Set the main message text.                                           | [publish](https://docs.ntfy.sh/publish)                                      |
| withPriority        | (int priority)                                                                                    | Set message priority (1-5).                                          | [priority](https://ntfy.sh/docs/publish/#priority)                           |
| withTitle           | (String title)                                                                                    | Set title.                                                           | [title](https://docs.ntfy.sh/publish/#message-title)                         |
| withTag             | (String tag)                                                                                      | Add a tag to the message.                                            | [tags](https://ntfy.sh/docs/publish/#tags)                                   |
| withIcon            | (String url)                                                                                      | Set an icon URL for the notification.                                | [icons](https://ntfy.sh/docs/publish/#icons)                                 |
| withAttachment      | (String url, String filename)                                                                     | Attach a file or resource URL with optional filename.                | [attachments](https://docs.ntfy.sh/publish/#attach-file-from-a-url)          |
| withViewAction      | (String label, Boolean clearNotification, String url)                                             | Add a view action (opens URL) with optional clear flag.              | [actions](https://docs.ntfy.sh/publish/#open-websiteapp)                     |
| withCopyAction      | (String label, Boolean clearNotification, String value)                                           | Add an action that copies text to clipboard on the client.           | [actions](https://docs.ntfy.sh/publish/#copy-to-clipboard)                   |
| withHttpAction      | (String label, Boolean clearNotification, String url, String method, String headers, String body) | Add an HTTP action with optional method, headers and body.           | [actions](https://docs.ntfy.sh/publish/#send-http-request)                   |
| withBroadcastAction | (String label, Boolean clearNotification, String params)                                          | Add a broadcast action to trigger local apps/handlers.               | [actions](https://docs.ntfy.sh/publish/#send-android-broadcast)              |
| withDelay           | (String delay)                                                                                    | Assign a delay.                                                      | [delay](https://docs.ntfy.sh/publish/#scheduled-delivery)                    |
| withSequenceId      | (String sequenceId)                                                                               | Assign a sequence id for later reference (useful for delete/update). | [sequence id](https://docs.ntfy.sh/publish/#updating-deleting-notifications) |
| send                | ()                                                                                                | Send the built message; returns a message ID string.                 | [publish](https://ntfy.sh/docs/publish/)                                     |
| send                | (String file, String filename, String sequenceId)                                                 | Send a file; returns a message ID string.                            | [publish_local_file](https://docs.ntfy.sh/publish/#attach-local-file)        |
| delete              | (String sequenceId)                                                                               | Delete a message previously sent with the given sequence id.         | [delete](https://ntfy.sh/docs/publish/#deleting-notifications)               |

For more information about ntfy features and the notification format, see the ntfy project documentation: [https://ntfy.sh/docs/](https://ntfy.sh/docs/)

### Rules DSL example

```rules
// Obtain the Thing-specific actions and use the builder to send a message
val bindingActions = getActions("ntfy", "ntfy:ntfyTopic:frontdoor")
if (bindingActions !== null) {
    // simple one-liner: send a message
    val msgId = bindingActions.withMessage("Someone is at the door").withPriority(4).send()

    // or build up a more complex message
    bindingActions.withMessage("Doorbell pressed")
                  .withSequenceId("doorbell-1")
                  .withTag("door")
                  .withIcon("https://example.org/icons/door.png")
                  .withViewAction("Open UI", true, "https://example.org/ui")
                  .send()

    // delete a previously sent message by sequence id
    bindingActions.delete("doorbell-1")
}
```

The builder methods available include: withMessage(String), withPriority(int), withTag(String), withIcon(String), withAttachment(String, String), withViewAction(...), withCopyAction(...), withHttpAction(...), withBroadcastAction(...), withSequenceId(String), send(), and delete(String).

### ECMAScript / JavaScript example

In ECMAScript scripts you can obtain the Thing Actions and use the same builder API. The example below assumes the automation engine provides an `actions` helper (typical in openHAB JavaScript environment):

```javascript
// get the Thing actions for the topic Thing
var bindingActions = actions.get("ntfy", "ntfy:ntfyTopic:frontdoor");
if (bindingActions) {
    // send a simple message
    var id = bindingActions.withMessage("Garage opened").withPriority(3).send();

    // build and send a message with extra features
    bindingActions.withMessage("Motion detected in garage")
                  .withSequenceId("garage-1")
                  .withTag("motion")
                  .withHttpAction("Open Camera", true, "https://camera/local/snap", "POST", null, null)
                  .send();
}
```

If your scripting environment exposes a different API to retrieve Thing Actions, adapt the call accordingly. The important part is to obtain the Thing-specific actions object for the binding and then use the builder methods shown above.
