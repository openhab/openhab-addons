# volumio Binding

This binding integrates the open-source Music Player [Volumio](https://www.volumio.com).. 

## Supported Things


All available Volumio (playback) modes are supported by this binding.

## Discovery

The volumio devices are discovered through UPnP in the local network and all devices are put in the Inbox.

## Binding Configuration

## Binding Configuration

The binding has the following configuration options, which can be set for "binding:sonos":

| Parameter   | Name             | Description                                                                | Required |
| ----------- | ---------------- | -------------------------------------------------------------------------- | -------- |
| hostname    | Hostanem         | The hostname of the Volumio player.                                        | yes      |
| port        | Port             | The port of your volumio2 device (default is 3000)                         | yes      |
| protocol    | Protocol         | The protocol of your volumio2 device (default is http)                     | yes      |


_Note that it is planned to generate some part of this based on the information that is available within ```src/main/resources/OH-INF/binding``` of your binding._

_If your binding does not offer any generic configurations, you can remove this section completely._

## Thing Configuration

_Describe what is needed to manually configure a thing, either through the UI or via a thing-file._
_This should be mainly about its mandatory and optional configuration parameters._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

### `sample` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| hostname        | text    | Hostname or IP address of the device  | N/A     | yes      | no       |
| password        | text    | Password to access the device         | N/A     | yes      | no       |
| refreshInterval | integer | Interval the device is polled in sec. | 600     | no       | yes      |

## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

| Channel | Type   | Read/Write | Description                 |
|---------|--------|------------|-----------------------------|
| control | Switch | RW         | This is the control channel |

## Full Example

_Provide a full usage example based on textual configuration files._
_*.things, *.items examples are mandatory as textual configuration is well used by many users._
_*.sitemap examples are optional._

### Thing Configuration

```java
Example thing configuration goes here.
```
### Item Configuration

```java
Example item configuration goes here.
```

### Sitemap Configuration

```perl
Optional Sitemap configuration goes here.
Remove this section, if not needed.
```

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
