# RSS/Atom Feed transport

This transport provides the ROME APIs to interact and publish RSS/Atom Feeds.
From the ROME Website:

> ROME is a Java framework for RSS and Atom feeds.
> It's open source and licensed under the Apache 2.0 license.

> ROME includes a set of parsers and generators for the various flavors of syndication feeds,
> as well as converters to convert from one format to another.
> The parsers can give you back Java objects that are either specific for the format you want to work with,
> or a generic normalized SyndFeed class that lets you work on with the data without bothering about the incoming or outgoing feed type.

Example usage:

```java
FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
FeedFetcher feedFetcher = new HttpURLFeedFetcher(feedInfoCache);
SyndFeed feed = feedFetcher.retrieveFeed(new URL("http://blogs.sun.com/roller/rss/pat"));
System.out.println(feed);
```
