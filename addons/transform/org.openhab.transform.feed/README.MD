# Feed Transformation Service

Feed transformation service transforms a feed between some of the supported feed formats: RSS 0.9, RSS 0.91 Netscape, RSS 0.91 Userland, RSS 0.92, RSS 0.93, RSS 0.94, RSS 1.0, RSS 2.0, Atom 0.3, Atom 1.0.

## Example

input:

```xml
<?xml version="1.0"?>
<rss version="2.0">
  <channel>
    <title>Example Channel</title>
    <link>http://example.com/</link>
    <description>My example channel</description>
    <item>
       <title>News for September the Second</title>
       <link>http://example.com/2002/09/01</link>
       <description>other things happened today</description>
    </item>
    <item>
       <title>News for September the First</title>
       <link>http://example.com/2002/09/02</link>
    </item>
  </channel>
</rss>
```

parameter:

```atom_1.0```

output:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom" >
  <title>ExampleChannel</title>
  <link rel ="alternate" href="http://example.com/" />
  <subtitle>My example channel</subtitle>
  <entry>
    <title>News for September the Second</title>
    <link rel="alternate" href="http://example.com/2002/09/01" />
    <author>
      <name />
    </author>
    <id>http://example.com/2002/09/01</id>
    <summary type ="html">other things happened today</summary>
  </entry>
  <entry>
    <title>NewsforSeptembertheFirst</title>
    <link rel ="alternate" href="http://example.com/2002/09/02" />
    <author>
      <name />
    </author>
    <id>http://example.com/2002/09/02</id>
  </entry>
</feed>
```