# X Binding

The X (formerly known as Twitter) binding lets your home post messages (up to 280 characters), send direct messages, and post with media.

## Supported Things

```text
`account` — X account
```

## Thing Configuration

The X account thing requires you to create an app in the X Developer Portal and obtain API keys/tokens.

|   Property        | Default | Required | Description                        |
|-------------------|---------|:--------:|-----------------------------------|
| consumerKey       |         |   Yes    | Consumer API Key                  |
| consumerSecret    |         |   Yes    | Consumer API Secret               |
| accessToken       |         |   Yes    | Access Token                      |
| accessTokenSecret |         |   Yes    | Access Token Secret               |
| refresh           | 30      |   No     | Refresh interval in minutes       |

## Channels

| channel  | type   | description                    |
|----------|--------|--------------------------------|
| lastpost | String | The user's latest post message |

## Full Example

x.things:

```java
Thing x:account:sampleaccount [ consumerKey="11111", consumerSecret="22222", accessToken="33333", accessTokenSecret="444444" ]
```

x.items:

```java
String sample_post   "Latest post: [%s]" { channel="x:account:sampleaccount:lastpost" }
```

## Rule Action

This binding includes rule actions for sending posts (optionally with media) and direct messages.

- `boolean success = sendPost(String text)`
- `boolean success = sendPostWithAttachment(String text, String URL)`
- `boolean success = sendDirectMessage(String recipientID, String text)`

Examples:

```java
val postActions = getActions("x", "x:account:sampleaccount")
val success  = postActions.sendPost("This is a post")
val success2 = postActions.sendPostWithAttachment("This is a post with a picture", "file:///tmp/201601011031.jpg")
val success3 = postActions.sendPostWithAttachment("Windows picture", "D:\\Test.png")
val success4 = postActions.sendPostWithAttachment("HTTP picture", "http://www.mywebsite.com/Test.png")
val success5 = postActions.sendDirectMessage("1234567", "Wake up")
```
