# XmlTV Binding

This binding takes an XMLTV file as input and creates a thing for each channel contained in it.
XmlTV channels are called Media Channels in this binding in order to avoid messing with ESH Channels.

For each thing, you'll be able to grab information regarding the current program and the next to come.

## Supported Things

## Discovery

Once the XmlTV bridge to a file is created, you can add all known channels by searching new things.

## Binding Configuration

| Configuration Parameter | Required | Description                                         | Default |
|-------------------------|----------|-----------------------------------------------------|---------|
| filePath                | X        | Full path (including filename) to an Xml TV file    |         |
| refresh                 | X        | XMLTV file reload interval in hours                 | 24h     |

## Thing Configuration

| Configuration Parameter | Required | Description                                                       | Default |
|-------------------------|----------|-------------------------------------------------------------------|---------|
| channelId               | X        | Id of the channel as presented in the XmlTV file.                 |         |
| offset                  | X        | Offset applied to programme times (forward or backward (minutes)  | 0       |
| refresh                 | X        | refresh interval in seconds                                       | 60      |

## Channels

| Channel Type ID | Item Type            | Description                         |
|-----------------|----------------------|-------------------------------------|
| iconUrl         | String               | Channel Icon URL                    |
| icon            | Image                | Icon of the channel                 |

### Current programme (currentprog) channels group

| Channel Type ID | Item Type            | Description                                   |
|-----------------|----------------------|-----------------------------------------------|
| progStart       | DateTime             | Program Start Time                            |
| progEnd         | DateTime             | Program End Time                              |
| progTitle       | String               | Program Title.                                |
| progCategory    | String               | Program Category.                             |
| progIcon        | String               | URL to an image of the programme.             |
| icon            | Image                | Icon of the programme                         |
| elapsedTime     | Number:Time          | Current time of currently playing programme   |
| remainingTime   | Number:Time          | Time remaining until end of the programme     |
| progress        | Number:Dimensionless | Relative progression of the current programme |

### Next programme (nextprog) channels group

| Channel Type ID | Item Type            | Description                                   |
|-----------------|----------------------|-----------------------------------------------|
| progStart       | DateTime             | Program Start Time                            |
| timeLeft        | Number:Time          | Time left before programme start              |
| progEnd         | DateTime             | Program End Time                              |
| progTitle       | String               | Program Title.                                |
| progCategory    | String               | Program Category.                             |
| progIcon        | String               | URL to an image of the programme.             |

## Full Example

### demo.things

```
Thing xmltv:XmlTVFile:france  [ filePath=home/sysadmin/tvguide.xml ]
```
