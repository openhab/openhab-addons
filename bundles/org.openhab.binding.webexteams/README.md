# WebexTeams Binding

The Webex Team binding allows to send messages to [Webex Teams](https://web.webex.com/) users and rooms through a number of actions.

Messages can use markdown syntax, and attachments are supported.

## Supported Things

- `account`: A Webex Teams account

## Discovery

No Things are being discovered by this binding.


## Thing Configuration

Webex Teams supports two main types of app integration:

* Bot: a separate identity that can be used to communicate with people and rooms.
* Integration: OAuth integration that allows the binding to act on a persons behalf.

Both of these integrations must be first configured on the [Webex Developers](https://developer.webex.com/my-apps) 
website.

To use the bot identity, only configure the `token` (Authentication token).

To use the OAuth interation, configure `clientId`, `clientSecret` and `authCode`.  To make life easier, use the following redirect URL: `https://files.ducbase.com/authcode/index.html` when creating the Webex Teams app at the URL above.  This will provide you with a convenient way to copy the auth code once redirected by Webex Teams.  The auth code is obtained by using the Authorization URL from the Webex Developers integration setup page.

As long as the binding is configured with credentials it will refresh tokens.  When the binding was down for a prolongued time
you may have to obtain a new auth code.  In this case, delete the refresh token from the configuration and apply the new auth token.

You shouldn't configure both an authentication token (used for bots) AND clientId/clientSecret (used for integrations).  In that
case the binding will use the clientId/clientSecret.

A default room id is required for use with the `sendMessage` action.

### `account` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| token           | password| (Bot) authentication token            | N/A     | no       | no       |
| clientId        | text    | (OAuth) client id                     | N/A     | no       | no       |
| clientSecret    | password| (OAuth) client secret                 | N/A     | no       | no       |
| authCode        | text    | (OAuth) auth code                     | N/A     | no       | no       |
| refreshToken    | password| (OAuth) refresh token                 | N/A     | no       | yes      |
| roomId          | text    | ID of the default room                | N/A     | no       | no       |

## Channels

| Thing              | channel      | type      | description                                                  |
|--------------------|--------------|-----------|--------------------------------------------------------------|
| WebexTeams Account | botname      | String    | Name of the bot as configured at developer.webex.com/my-apps |
| WebexTeams Account | status       | String    | Account presence status: active, call, inactive, ...         |
| WebexTeams Account | lastactivity | DateTime  | The date and time of the person's last activity within Webex |

## Full Example


webexteams.things:

```
Thing webexteams:account:bot [ token="XXXXXX", roomId="YYYYYY" ]

```

DSL rules use `getActions` to get a reference to the thing.

    val botActions = getActions("webexteams", "webexteams:account:bot")

In a DSL rule you can use following actions:

## Rule Action

This binding includes rule actions for sending messages:


* `var success = botActions.sendMessage(String markdown)`
* `var success = botActions.sendMessage(String markdown, String attach)`
* `var success = botActions.sendRoomMessage(String roomId, String markdown)`
* `var success = botActions.sendRoomMessage(String roomId, String markdown, String attach)`
* `var success = botActions.sendPersonMessage(String personEmail, String markdown)`
* `var success = botActions.sendPersonMessage(String personEmail, String markdown, String attach)`

Attachments must be URLs.  Sending local files is not supported at this moment.


