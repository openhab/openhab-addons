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

Both of these integrations must be first configured on the [Webex Developers](https://developer.webex.com/) 
website.

To use the bot identity, only configure the `token` (Authentication token).

To use the OAuth interation, configure: client id, client secret, auth code and redirect URL.  
The auth code is obtained by using the Authorization URL from the Webex Developers integration setup page.  
The same goes for the redirect URL.  The URL is not effectively used, but must be configured in the binding with the same value as on the Webex Developers integration setup page.  If unsure, use *https://www.example.com*.
As long as the binding is configured with credentials it will refresh tokens.  
There's an annoying *feature* today that requires you provide a new authCode when the binding was down for a while.

When both bot auth token and OAuth details are configure, the binding will prefer the bot auth token.

A default room id is required for use with the `sendMessage` action.

### `account` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| token           | password| (Bot) authentication token            | N/A     | no      | no       |
| clientId        | text    | (OAuth) client id                     | N/A     | no      | no       |
| clientSecret    | password| (OAuth) client secret                 | N/A     | no      | no       |
| authCode        | text    | (OAuth) auth code                     | N/A     | no      | no       |
| redirectUrl     | text    | (OAuth) redirect url                  | N/A     | no      | no       |
| roomId          | text    | ID of the default room                | N/A     | no      | no       |

## Channels

No channels are implemented.

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


