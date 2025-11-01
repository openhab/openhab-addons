# XMLTV Binding

XMLTV is an XML-based file format for describing TV listings.
It's commonly used by home theater software to produce an electronic program guide (EPG).
See the XMLTV project wiki for details: <http://wiki.xmltv.org/index.php/Main_Page>

The format separates building the guide from displaying it.
The XMLTV file itself is produced by so‑called "grabbers": <http://wiki.xmltv.org/index.php/HowtoUseGrabbers>

Some websites provide updated XMLTV files that can be downloaded directly (for example, France and Switzerland: <https://xmltv.ch/>).

This binding reads an XMLTV file and creates a thing for each channel contained in it.
Channels from the XMLTV file are represented as "Media Channels" to avoid confusion with openHAB Channels.

Each channel thing exposes information about the current program and the next one.

## Supported Things

- xmltvfile — Bridge to an XMLTV file (reads and parses the file)
- channel — A TV channel defined in the XMLTV file

## Discovery

Once the XMLTV file bridge is created, you can add all known channels by scanning for new things.

## Binding Configuration

| Configuration Parameter | Required | Description                                    | Default |
|-------------------------|----------|------------------------------------------------|---------|
| filePath                | Yes      | Full path (including filename) to an XMLTV file |         |
| refresh                 | No       | XMLTV file reload interval (hours)              | 24 h    |
| encoding                | Yes      | XMLTV file encoding                              | UTF-8   |

## Thing Configuration

| Configuration Parameter | Required | Description                                          | Default |
|-------------------------|----------|------------------------------------------------------|---------|
| channelId               | Yes      | ID of the channel as presented in the XMLTV file     |         |
| offset                  | No       | Offset applied to program times (minutes; +/-)       | 0       |
| refresh                 | No       | Refresh interval (seconds)                           | 60      |

## Channels

| Channel Type ID | Item Type | Description              |
|-----------------|-----------|--------------------------|
| iconUrl         | String    | Channel icon URL         |
| icon            | Image     | Icon of the channel      |

### Current program (currentprog) channel group

| Channel Type ID | Item Type            | Description                                 |
|-----------------|----------------------|---------------------------------------------|
| progStart       | DateTime             | Program start time                          |
| progEnd         | DateTime             | Program end time                            |
| progTitle       | String               | Program title                               |
| progCategory    | String               | Program category                            |
| progIconUrl     | String               | URL to an image of the program              |
| icon            | Image                | Icon of the program                         |
| elapsedTime     | Number:Time          | Elapsed time of the current program         |
| remainingTime   | Number:Time          | Time remaining until the end of the program |
| progress        | Number:Dimensionless | Relative progression of the current program |

### Next program (nextprog) channel group

| Channel Type ID | Item Type  | Description                        |
|-----------------|------------|------------------------------------|
| progStart       | DateTime   | Program start time                 |
| timeLeft        | Number:Time| Time left before program start     |
| progEnd         | DateTime   | Program end time                   |
| progTitle       | String     | Program title                      |
| progCategory    | String     | Program category                   |
| progIconUrl     | String     | URL to an image of the program     |
| icon            | Image      | Icon of the program                |

## Full Example

### xmltv.things

```java
Bridge xmltv:xmltvfile:france "XMLTV" @ "TV" [filePath="/etc/openhab/scripts/tvguide.xml"]
{
    Thing channel france2 "France 2" @ "TV" [channelId="C4.api.telerama.fr", offset=0, refresh=60]
}
```

### xmltv.items

```java
String france2_title "Titre" {channel="xmltv:channel:france:france2:currentprog#progTitle"}
```
