# X Binding

The X (formerly known as Twitter) binding allows your home to post 280 characters at a time. It also supports direct messages and posting with media.

## Supported Things

```text
account - X Account.
```

## Thing Configuration

The X Account Thing requires you to create a X App in the X Developer Page.

|   Property        | Default | Required | Description                       |
|-------------------|---------|:--------:|-----------------------------------|
| consumerKey       |         |   Yes    | Consumer API Key                  |
| consumerSecret    |         |   Yes    | Consumer API Secret               |
| accessToken       |         |   Yes    | Access Token                      |
| accessTokenSecret |         |   Yes    | Access Token Secret               |
| refresh           | 30      |   No     | Post refresh interval in minutes |

## Channels

| channel  | type   | description                                   |
|----------|--------|-----------------------------------------------|
| lastpost | String | This channel provides the Latest post message |

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

This binding includes rule actions for sending posts and direct messages.

- `boolean success = sendPost(String text)`
- `boolean success = sendPostWithAttachment(String text, String URL)`
- `boolean success = sendDirectMessage(String recipientID, String text)`

Examples:

```java
val postActions = getActions("x","x:account:sampleaccount")
val success  = postActions.sendPost("This is A Post")
val success2 = postActions.sendPostWithAttachment("This is A Post with a Pic", file:///tmp/201601011031.jpg)
val success3 = postActions.sendPostWithAttachment("Windows Picture", "D:\\Test.png" )
val success4 = postActions.sendPostWithAttachment("HTTP Picture", "http://www.mywebsite.com/Test.png" )
val success5 = postActions.sendDirectMessage("1234567", "Wake Up" )
```
