# WebexTeams Binding

The Webex Team binding allows to send messages to [Webex Teams](https://web.webex.com/) users and rooms through a number of actions.

Messages can use markdown syntax, and attachments are supported.

## Supported Things

- `account`: A Webex Teams account

## Discovery

No Things are being discovered by this binding.

## Thing Configuration

Webex Teams supports two main types of app integration:

- Bot: a separate identity that can be used to communicate with people and rooms.
- Person integration: OAuth integration that allows the binding to act on behalf of a persons.

Both of these accounts must be first configured on the [Webex Developers](https://developer.webex.com/my-apps) website.
When creating a person integration, it's important you customize the redirect URL based on your openHAB installation.
For example if you run your openHAB server on `http://openhab:8080` you should add [http://openhab:8080/connectwebex](http://openhab:8080/connectwebex) to the redirect URIs.

To use a bot account, only configure the `token` (Authentication token).

To use a person integration, configure `clientId` and `clientSecret`.
When the account is configured as a Thing in OpenHab, navigate to the redirect URL (as described above) and authorize your account.

You shouldn't configure both an authentication token (used for bots) AND clientId/clientSecret (used for person integrations).  In that case the binding will use the authentication token.

A default room id is required for use with the `sendMessage` action.

### `account` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| token           | text    | (Bot) authentication token            | N/A     | no       | no       |
| clientId        | text    | (Person) client id                    | N/A     | no       | no       |
| clientSecret    | text    | (Person) client secret                | N/A     | no       | no       |
| refreshPeriod   | integer | Refresh period for channels (seconds) | 300     | no       | no       |
| roomId          | text    | ID of the default room                | N/A     | no       | no       |

## Channels

| Thing              | channel      | type      | description                                                  |
|--------------------|--------------|-----------|--------------------------------------------------------------|
| WebexTeams Account | status       | String    | Account presence status: active, call, inactive, ...         |
| WebexTeams Account | lastactivity | DateTime  | The date and time of the person's last activity within Webex |

Note: status and lastactivity are only updated for person integrations

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

## Rule Action

DSL rules use `getActions` to get a reference to the thing.

`val botActions = getActions("webexteams", "webexteams:account:bot")`

This binding includes these rule actions for sending messages:

- `var success = botActions.sendMessage(String markdown)` - Send a message to the default room.
- `var success = botActions.sendMessage(String markdown, String attach)` - Send a message to the default room, with attachment.
- `var success = botActions.sendRoomMessage(String roomId, String markdown)` - Send a message to a specific room.
- `var success = botActions.sendRoomMessage(String roomId, String markdown, String attach)` - Send a message to a specific room, with attachment.
- `var success = botActions.sendPersonMessage(String personEmail, String markdown)` - Send a direct message to a person.
- `var success = botActions.sendPersonMessage(String personEmail, String markdown, String attach)` - Send a direct message to a person, with attachment.

Sending messages for bot or person accounts works exactly the same.
Attachments must be URLs.<br>
Sending local files is not supported at this moment.
