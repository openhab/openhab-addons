# Feed Binding

This binding allows you to integrate feeds in the openHAB environment.
The Feed binding downloads the content, tracks for changes, and displays information like feed author, feed title and description, number of entries, last update date.

It can be used in combination with openHAB rules to trigger events on feed change.
It uses the [ROME library](https://rometools.github.io/rome/index.html) for parsing
and supports a wide range of popular feed formats - RSS 2.00, RSS 1.00, RSS 0.94, RSS 0.93, RSS 0.92, RSS 0.91 UserLand,
RSS 0.91 Netscape, RSS 0.90, Atom 1.0, Atom 0.3.

## Supported Things

This binding supports one Thing type: `feed`, `feeditems`.

## Discovery

Discovery is not necessary.

## Binding Configuration

No binding configuration required.

## Thing Configuration

Required configuration:

- **URL** - the URL of the feed (e.g <http://example.com/path/file>). The binding uses this URL to download data

Optional configuration:

- **refresh** - a refresh interval defines after how many minutes the binding will check, if new content is available. Default value is 20 minutes

## Channels

The binding supports following channels

| Channel Type ID    | Item Type | Description                                         |
|--------------------|-----------|-----------------------------------------------------|
| latest-title       | String    | Contains the title of the last feed entry.          |
| latest-description | String    | Contains the description of last feed entry.        |
| latest-date        | DateTime  | Contains the published date of the last feed entry. |
| author             | String    | The name of the feed author, if author is present   |
| title              | String    | The title of the feed                               |
| description        | String    | Description of the feed                             |
| last-update        | DateTime  | The last update date of the feed                    |
| number-of-entries  | Number    | Number of entries in the feed                       |
|                    |           |                                                     |
| item-title         | String    | The title of the feed item                          |
| item-description   | String    | Description of the feed item                        |
| item-pubdate       | DateTime  | Publication date of the feed item                   |
| item-link          | String    | Link of the feed item                               |


## Example

Things:

```java
feed:feed:bbc  [ URL="http://feeds.bbci.co.uk/news/video_and_audio/news_front_page/rss.xml?edition=uk"]
feed:feed:techCrunch [ URL="http://feeds.feedburner.com/TechCrunch/", refresh=60]
feed:feeditems:tagesschau [ URL="https://www.tagesschau.de/xml/rss2", refresh=30]
```

Items:

```java
String latest_title           {channel="feed:feed:bbc:latest-title"}
String latest_description     {channel="feed:feed:bbc:latest-description"}
DateTime latest_date          {channel="feed:feed:bbc:latest-date"}
Number number_of_entries      {channel="feed:feed:bbc:number-of-entries"}
String description            {channel="feed:feed:bbc:description"}
String author                 {channel="feed:feed:bbc:author"}
DateTime published_date       {channel="feed:feed:bbc:last-update"}
String title                  {channel="feed:feed:bbc:title"}

DateTime    item01_update             {channel="feed:feeditems:tagesschau:item01#item-pubdate"}
String      item01_title              {channel="feed:feeditems:tagesschau:item01#item-title"}
String      item01_link               {channel="feed:feeditems:tagesschau:item01#item-link"}
String      item01_description        {channel="feed:feeditems:tagesschau:item01#item-description"}
DateTime    item02_update             {channel="feed:feeditems:tagesschau:item02#item-pubdate"}
String      item02_title              {channel="feed:feeditems:tagesschau:item02#item-title"}
String      item02_link               {channel="feed:feeditems:tagesschau:item02#item-link"}
String      item02_description        {channel="feed:feeditems:tagesschau:item02#item-description"}
DateTime    item03_update             {channel="feed:feeditems:tagesschau:item03#item-pubdate"}
String      item03_title              {channel="feed:feeditems:tagesschau:item03#item-title"}
String      item03_link               {channel="feed:feeditems:tagesschau:item03#item-link"}
String      item03_description        {channel="feed:feeditems:tagesschau:item03#item-description"}
DateTime    item04_update             {channel="feed:feeditems:tagesschau:item04#item-pubdate"}
String      item04_title              {channel="feed:feeditems:tagesschau:item04#item-title"}
String      item04_link               {channel="feed:feeditems:tagesschau:item04#item-link"}
String      item04_description        {channel="feed:feeditems:tagesschau:item04#item-description"}
DateTime    item05_update             {channel="feed:feeditems:tagesschau:item05#item-pubdate"}
String      item05_title              {channel="feed:feeditems:tagesschau:item05#item-title"}
String      item05_link               {channel="feed:feeditems:tagesschau:item05#item-link"}
String      item05_description        {channel="feed:feeditems:tagesschau:item05#item-description"}
DateTime    item06_update             {channel="feed:feeditems:tagesschau:item06#item-pubdate"}
String      item06_title              {channel="feed:feeditems:tagesschau:item06#item-title"}
String      item06_link               {channel="feed:feeditems:tagesschau:item06#item-link"}
String      item06_description        {channel="feed:feeditems:tagesschau:item06#item-description"}
DateTime    item07_update             {channel="feed:feeditems:tagesschau:item07#item-pubdate"}
String      item07_title              {channel="feed:feeditems:tagesschau:item07#item-title"}
String      item07_link               {channel="feed:feeditems:tagesschau:item07#item-link"}
String      item07_description        {channel="feed:feeditems:tagesschau:item07#item-description"}
DateTime    item08_update             {channel="feed:feeditems:tagesschau:item08#item-pubdate"}
String      item08_title              {channel="feed:feeditems:tagesschau:item08#item-title"}
String      item08_link               {channel="feed:feeditems:tagesschau:item08#item-link"}
String      item08_description        {channel="feed:feeditems:tagesschau:item08#item-description"}
DateTime    item09_update             {channel="feed:feeditems:tagesschau:item09#item-pubdate"}
String      item09_title              {channel="feed:feeditems:tagesschau:item09#item-title"}
String      item09_link               {channel="feed:feeditems:tagesschau:item09#item-link"}
String      item09_description        {channel="feed:feeditems:tagesschau:item09#item-description"}
DateTime    item10_update             {channel="feed:feeditems:tagesschau:item10#item-pubdate"}
String      item10_title              {channel="feed:feeditems:tagesschau:item10#item-title"}
String      item10_link               {channel="feed:feeditems:tagesschau:item10#item-link"}
String      item10_description        {channel="feed:feeditems:tagesschau:item10#item-description"}
