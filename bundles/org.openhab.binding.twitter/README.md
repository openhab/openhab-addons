# Twitter Binding

The Twitter binding allows your home to Tweet 280 characters at a time. It also supports direct messages and tweeting with media.

## Supported Things

```
account - Twitter Account.

```

## Thing Configuration

The Twitter Account Thing requires you to create a Twitter App in the Twitter Developer Page.

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

twitter.things:

```
Thing twitter:account:sampleaccount [ consumerKey="11111", consumerSecret="22222", accessToken="33333", accessTokenSecret="444444" ]

```

twitter.items:

```
String sample_tweet   "Latest Tweet: [%s]" { channel="twitter:account:sampleaccount:lasttweet" }

```

## Rule Action

This binding includes rule actions for sending tweets and direct messages.

* `boolean success = sendTweet(String text)`
* `boolean success = sendTweetWithAttachment(String text, String URL)`
* `boolean success = sendDirectMessage(String recipientID, String text)`

Examples:

```
val tweetActions = getActions("twitter","twitter:account:sampleaccount")
val success  = tweetActions.sendTweet("This is A Tweet")
val success2 = tweetActions.sendTweetWithAttachment("This is A Tweet with a Pic", file:///tmp/201601011031.jpg)
val success3 = tweetActions.sendTweetWithAttachment("Windows Picture", "D:\\Test.png" )
val success4 = tweetActions.sendTweetWithAttachment("HTTP Picture", "http://www.mywebsite.com/Test.png" )
val success5 = tweetActions.sendDirectMessage("1234567", "Wake Up" )

```
