# XmlTV Binding

XMLTV is an XML based file format for describing TV listings.
This format is often used by Home Theater software to produce their Electronic Program Guide (<http://wiki.xmltv.org/index.php/Main_Page>)

The norm allows to separate program display from its building.
The building of the XMLTV file itself is taken in charge by so called "grabbers" (<http://wiki.xmltv.org/index.php/HowtoUseGrabbers>).

Some websites provides updated XMLTV files than can be directly downloaded.

Here is a sample for France and Switzerland : <https://xmltv.ch/>

This binding takes an XMLTV file as input and creates a thing for each channel contained in it.
XmlTV channels are called Media Channels in this binding in order to avoid messing with openHAB Channels.

For each thing, you will be able to get information regarding the current program and the next to come.

## Supported Things

## Discovery

Once the XmlTV bridge to a file is created, you can add all known channels by searching new things.

## Binding Configuration

| Configuration Parameter | Required | Description                                         | Default |
|-------------------------|----------|-----------------------------------------------------|---------|
| filePath                | X        | Full path (including filename) to an Xml TV file    |         |
| refresh                 | X        | XMLTV file reload interval in hours                 | 24h     |
| encoding                | X        | XMLTV file encoding                                 | UTF8    |

## Thing Configuration

| Configuration Parameter | Required | Description                                                    | Default |
|-------------------------|----------|----------------------------------------------------------------|---------|
| channelId               | X        | Id of the channel as presented in the XmlTV file               |         |
| offset                  | X        | Offset applied to program times (forward or backward (minutes) | 0       |
| refresh                 | X        | Refresh interval in seconds                                    | 60      |

## Channels

| Channel Type ID | Item Type            | Description                         |
|-----------------|----------------------|-------------------------------------|
| iconUrl         | String               | Channel Icon URL                    |
| icon            | Image                | Icon of the channel                 |

### Current program (currentprog) Channels Group

| Channel Type ID | Item Type            | Description                                 |
|-----------------|----------------------|---------------------------------------------|
| progStart       | DateTime             | Program Start Time                          |
| progEnd         | DateTime             | Program End Time                            |
| progTitle       | String               | Program Title                               |
| progCategory    | String               | Program Category                            |
| progIconUrl     | String               | URL to an image of the program              |
| icon            | Image                | Icon of the program                         |
| elapsedTime     | Number:Time          | Current time of currently playing program   |
| remainingTime   | Number:Time          | Time remaining until end of the program     |
| progress        | Number:Dimensionless | Relative progression of the current program |

### Next program (nextprog) Channels Group

| Channel Type ID | Item Type            | Description                                 |
|-----------------|----------------------|---------------------------------------------|
| progStart       | DateTime             | Program Start Time                          |
| timeLeft        | Number:Time          | Time left before program start              |
| progEnd         | DateTime             | Program End Time                            |
| progTitle       | String               | Program Title                               |
| progCategory    | String               | Program Category                            |
| progIconUrl     | String               | URL to an image of the program              |
| icon            | Image                | Icon of the program                         |

## Full Example

### xmltv.things

```java
Bridge xmltv:xmltvfile:france "XmlTV" @ "TV" [filePath="/etc/openhab/scripts/tvguide.xml"]
{
    Thing channel france2 "France 2" @ "TV" [channelId="C4.api.telerama.fr", offset=0, refresh=60]
}
```

### xmltv.items

```java
String france2_title "Titre" {channel="xmltv:channel:france:france2:currentprog#progTitle"}
```
