# Pushbullet Binding

The Pushbullet binding allows you to notify supported devices of a message using the [Pushbullet API](https://docs.pushbullet.com).
To get started you need to [create an account](#creating-an-account) and [obtain an access token](#obtaining-an-access-token).

## Supported Things

| Thing | Type  | Description               |
| ----- | ----- | ------------------------- |
| bot   | Thing | Bot to send messages with |

## Thing Configuration

### `bot`

| Parameter | Required | Description                                          |
| --------- | :------: | ---------------------------------------------------- |
| token     |   Yes    | Access token obtained from the account settings page |

## Channels

Currently the binding does not support any channels.

## Rule Action

This binding includes rule actions for sending notes.
Two different actions available:

- `sendPushbulletNote(String recipient, String messsage)`
- `sendPushbulletNote(String recipient, String title, String messsage)`
- `sendPushbulletLink(String recipient, String url)`
- `sendPushbulletLink(String recipient, String title, String messsage, String url)`
- `sendPushbulletFile(String recipient, String content)`
- `sendPushbulletFile(String recipient, String title, String messsage, String content)`
- `sendPushbulletFile(String recipient, String title, String messsage, String content, String fileName)`

Since there is a separate rule action instance for each `bot` thing, this needs to be retrieved through `getActions(scope, thingUID)`.
The first parameter always has to be `pushbullet` and the second is the full Thing UID of the bot that should be used.
Once this action instance is retrieved, you can invoke the action method on it.

The recipient can either be an email address, a channel tag or `null`.
If it is not specified or properly formatted, the note will be broadcast to all of the user account's devices.

The file content can be an image URL, a local file path or an Image item state.

The file name is used in the upload link and how it appears in the push message for non-image content.
If it is not specified, it is automatically determined from the image URL or file path.
For Image item state content, it defaults to `image.jpg`.

For the `sendPushbulletNote` action, parameter `message` is required.
Likewise, for `sendPushbulletLink`, `url` and for `sendPushbulletFile`, `content` parameters are required.
Any other parameters for these actions are optional and can be set to `null`.

## Full Example

demo.things:

```java
Thing pushbullet:bot:r2d2 [ token="<ACCESS_TOKEN>" ]

```

demo.rules

```java
val actions = getActions("pushbullet", "pushbullet:bot:r2d2")
// push a note
actions.sendPushbulletNote("someone@example.com", "Note Example", "This is the pushed note.")
// push a link
actions.sendPushbulletLink("someone@example.com", "Link Example", "This is the pushed link", "https://example.com")
// push a file
actions.sendPushbulletFile("someone@example.com", "File Example", "This is the pushed file", "https://example.com/image.png")
actions.sendPushbulletFile("someone@example.com", null, null, "/path/to/somefile.pdf", "document.pdf")
// use toFullString method to push the content of an Image item
actions.sendPushbulletFile("someone@example.com", ImageItem.state.toFullString)
```

## Creating an account

A Pushbullet account is linked to either a Google or Facebook account.

- Go to the [Pushbullet](https://www.pushbullet.com) website.
- Select either "Sign up with Google" or "Sign up with Facebook".
- Complete the signup process as guided by the pushbullet web site.

## Obtaining an access token

An access token is linked to a Pushbullet account.

- Go to your [Pushbullet account settings](https://www.pushbullet.com/#settings/account) page.
- Click on "Create Access Token".
- Copy the created access token.

## Rate limits

As of 2024, free accounts are [limited](https://docs.pushbullet.com/#limits) to 500 pushes per month.
Going over will result in a warning message being logged indicating when your account rate limit will reset.
