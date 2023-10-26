# X Binding

The X (formerly known as Twitter) binding allows your home to Tweet 280 characters at a time. It also supports direct messages and tweeting with media.

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
| refresh           | 30      |   No     | Tweet refresh interval in minutes |

## Channels

| channel    | type   | description                                    |
|------------|--------|------------------------------------------------|
| lasttweet  | String | This channel provides the Latest Tweet message |

## Full Example

x.things:

```java
Thing x:account:sampleaccount [ consumerKey="11111", consumerSecret="22222", accessToken="33333", accessTokenSecret="444444" ]

```

x.items:

```java
String sample_tweet   "Latest Tweet: [%s]" { channel="x:account:sampleaccount:lasttweet" }

```

## Rule Action

This binding includes rule actions for sending tweets and direct messages.

- `boolean success = sendTweet(String text)`
- `boolean success = sendTweetWithAttachment(String text, String URL)`
- `boolean success = sendDirectMessage(String recipientID, String text)`

Examples:

```java
val tweetActions = getActions("x","x:account:sampleaccount")
val success  = tweetActions.sendTweet("This is A Tweet")
val success2 = tweetActions.sendTweetWithAttachment("This is A Tweet with a Pic", file:///tmp/201601011031.jpg)
val success3 = tweetActions.sendTweetWithAttachment("Windows Picture", "D:\\Test.png" )
val success4 = tweetActions.sendTweetWithAttachment("HTTP Picture", "http://www.mywebsite.com/Test.png" )
val success5 = tweetActions.sendDirectMessage("1234567", "Wake Up" )

```
