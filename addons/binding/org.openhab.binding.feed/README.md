# Feed Binding

This binding allows you to integrate frequently updated Web feed content in the Openhab environment.
The Feed binding downloads the content, track for changes and transform the data into one output format (RSS or Atom).
It can be used in combination with openHAB rules to provide XML feed data to different devices.
It supports a wide range of popular feed formats.

## Supported Things

This binding supports on Thing type: feed. 

## Discovery

Discovery is not necessary.

## Binding Configuration

No binding configuration required.

## Thing Configuration

*Required configuration:
    * **URL** - the URL of the feed (http://example.com/path/file). The binding uses this URL to download data.
*Optional configuration:
    * **refresh** - a refresh interval defines after how many minutes the binding will check, if new content is available. Default value is 20 minutes.
    * **length** - this is the number of feed entries (news) stored in the channel state. Default value is 20 entries.
    * **format** - the binding transforms all supported feed formats to one output format. The following options are possible: `rss_0.9, rss_0.91U, rss_0.91N, rss_0.92, rss_0.93, rss_0.94, rss_1.0, rss_2.0, atom_0.3, atom_1.0`. Default value is atom_1.0.
    
## Channels

The binding supports  one channel

| Channel Type ID | Item Type    | Description  |
|------------------|------------------------|--------------|----------------- |------------- |
| feed_channel | String       | This channel contains the latest feed content |

## Full Example

Things:
```
feed:feed:24news  [ URL="http://24news.com/news/rss.xml?edition=int"]
feed:feed:technews1 [ URL="http://techgeek.de/feed", format="rss_2.0", refresh=60, length=60] 
```
Items:
```
String Feed_24news                "Feed content[%-10s.10]"      {channel="feed:feed:24news:feed-channel"}
```
