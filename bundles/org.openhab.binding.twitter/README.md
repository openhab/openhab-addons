# Twitter Binding

The Twitter binding allows your home to Tweet it's status, 280 characters at a time.

## Supported Things

```
Account - Twitter Account.

```

## Thing Configuration

The Twitter Account Thing requires you create a Twitter App in the Developer Page.

|   Property        | Default | Required | Description                       |
|-------------------|---------|:--------:|-----------------------------------|
| consumerKey       |         |   Yes    | Consumer API Key                  |
| consumerSecret    |         |   Yes    | Consumer API Secret               |
| accessToken       |         |   Yes    | Access Token                      |
| accessTokenSecret |         |   Yes    | Access Token Secret               |
| refresh           | 30      |   No     | Tweet refresh interval in minutes |


## Channels

```
Last Tweet - Latest Tweet.
```

## Full Example

twitter.things:

```
Thing twitter:account:sampleaccount [ consumerKey="11111", consumerSecret="22222", accessToken="33333", accessTokenSecret="444444"]

```

## Rule Action

This binding includes rule actions for sending tweets and direct messages.

* `boolean success = sendTweet(String text)`
* `boolean success = sendTweet(String text, String URL)`
* `boolean success = sendDirectMessage(String recipient, String text)`

Examples:

```
val tweetActions = getActions("twitter","twitter:account:sampleaccount")
val success = tweetActions.sendTweet("This is A Tweet")
val success = tweetActions.sendTweet("This is A Tweet with a Pic", file:///tmp/201601011031.jpg)

```