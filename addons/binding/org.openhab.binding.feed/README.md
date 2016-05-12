# Feed Binding

This binding allows you to integrate feeds in the OpenHAB environment.
The Feed binding downloads the content, tracks for changes, and displays information like feed author, feed title and description, number of entries, last update date.

It can be used in combination with openHAB rules to trigger events on feed change.
It uses the [ROME library](http://rometools.github.io/rome/index.html) for parsing and supports a wide range of popular feed formats - RSS 2.00, RSS 1.00, RSS 0.94, RSS 0.93, RSS 0.92, RSS 0.91 UserLand, RSS 0.91 Netscape, RSS 0.90, Atom 1.0, Atom 0.3 .

## Supported Things

This binding supports one Thing type: ``feed``. 

## Discovery

Discovery is not necessary.

## Binding Configuration

No binding configuration required.

## Thing Configuration

*Required configuration:
    * **URL** - the URL of the feed (e.g http://example.com/path/file). The binding uses this URL to download data.
    
*Optional configuration:
    * **refresh** - a refresh interval defines after how many minutes the binding will check, if new content is available. Default value is 20 minutes.
    
## Channels

The binding supports following channels

| Channel Type ID | Item Type    | Description  | 
|------------------|------------------------|--------------|----------------- |------------- |
| latest-entry | String       | Contains title, description and published date for the last feed entry. |
| author | String       | The name of the feed author, if author is present |
| title | String       | The title of the feed |
| description | String       | Description of the feed |
| last-update | DateTime       | The last update date of the feed |
| number-of-entries | Number       | Number of entries in the feed |

## Full Example

Things:
```
feed:feed:news  [ URL="http://24news.com/news/rss.xml?edition=int"]
feed:feed:technews1 [ URL="http://techgeek.de/feed", refresh=60] 
```
Items:
```
String latest_content         {channel="feed:feed:news:latest-entry"}
Number number_of_entries      {channel="feed:feed:news:number-of-entries"}
String description            {channel="feed:feed:news:description"}
String author                 {channel="feed:feed:news:author"}
DateTime published_date       {channel="feed:feed:news:last-update"}
String title                  {channel="feed:feed:news:title"}

```
