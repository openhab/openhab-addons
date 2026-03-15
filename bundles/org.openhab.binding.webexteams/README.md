# Webex Teams Binding

The Webex Teams binding lets you send messages to [Webex Teams](https://web.webex.com/) users and rooms via rule actions.

Messages can use Markdown, and attachments are supported.

## Supported Things

- `account`: A Webex Teams account

## Discovery

No Things are discovered by this binding.

## Thing Configuration

Webex supports two main types of app integration:

- Bot: a separate identity that can communicate with people and rooms.
- Person integration: an OAuth integration that allows the binding to act on behalf of a person.

Both account types must be created first on the [Webex Developers](https://developer.webex.com/my-apps) website.
When creating a person integration, customize the redirect URL based on your openHAB installation.
For example, if your openHAB server runs at `http://openhab:8080`, add [http://openhab:8080/connectwebex](http://openhab:8080/connectwebex) to the redirect URIs.

To use a bot account, configure only the `token` (bot access token).

To use a person integration, configure `clientId` and `clientSecret`.
After the account is configured as a Thing in openHAB, navigate to the redirect URL (as described above) and authorize your account.

Do not configure both a bot token (used for bots) and `clientId`/`clientSecret` (used for person integrations). If both are set, the binding uses the token.

A default room ID is required for the `sendMessage` action.

### `account` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| token           | text    | (Bot) authentication token            | N/A     | no       | no       |
| clientId        | text    | (Person) client ID                    | N/A     | no       | no       |
| clientSecret    | text    | (Person) client secret                | N/A     | no       | no       |
| refreshPeriod   | integer | Refresh period for channels (seconds) | 300     | no       | no       |
| roomId          | text    | ID of the default room                | N/A     | no       | no       |

## Channels

| Thing               | Channel      | Type     | Description                                                   |
|---------------------|--------------|----------|---------------------------------------------------------------|
| Webex Teams Account | status       | String   | Account presence status: active, call, inactive, ...          |
| Webex Teams Account | lastactivity | DateTime | The date and time of the person's last activity within Webex. |

Note: The `status` and `lastactivity` channels are updated only for person integrations.

## Full Example

webexteams.things:

Configure a bot account:

```java
Thing webexteams:account:bot [ token="XXXXXX", roomId="YYYYYY" ]
```

Configure a person integration account:

```java
Thing webexteams:account:person [ clientId="XXXXXX", clientSecret="YYYYYY", roomId="ZZZZZZ" ]
```

## Rule Actions

DSL rules use `getActions` to obtain a reference to the Thing.

`val botActions = getActions("webexteams", "webexteams:account:bot")`

This binding includes these rule actions for sending messages:

- `var success = botActions.sendMessage(String markdown)` - Send a message to the default room.
- `var success = botActions.sendMessage(String markdown, String attach)` - Send a message to the default room, with attachment.
- `var success = botActions.sendRoomMessage(String roomId, String markdown)` - Send a message to a specific room.
- `var success = botActions.sendRoomMessage(String roomId, String markdown, String attach)` - Send a message to a specific room, with attachment.
- `var success = botActions.sendPersonMessage(String personEmail, String markdown)` - Send a direct message to a person.
- `var success = botActions.sendPersonMessage(String personEmail, String markdown, String attach)` - Send a direct message to a person, with attachment.

Sending messages for bot or person accounts works exactly the same.
Attachments must be public URLs. Sending local files isn't supported.
