# Radio Browser Binding

Radio Browser is a community driven database of internet radio and TV stations that has an open API that is free to use.
With this binding you can use their API and database of stations to apply filters and find many internet radio streams.

If you enjoy the binding, please consider sponsoring or a once off tip as a thank you via the links.
This allows me to purchase software and hardware to contributing more bindings.
Also some coffee to keep me coding faster never hurts :slight_smile:

Sponsor @Skinah on GitHub
<https://github.com/sponsors/Skinah/>

Paypal can also be used via
matt A-T pcmus D-O-T C-O-M

## Supported Things

- `radio`: Add one of these manually and it should come online after fetching language and country data.

## Thing Configuration

_Describe what is needed to manually configure a thing, either through the UI or via a thing-file._
_This should be mainly about its mandatory and optional configuration parameters._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

### `radio` Thing Configuration

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

## Any custom content here!

If you enjoy the binding, please consider sponsoring or a once off tip as a thank you via the links.
This allows me to purchase software and hardware to contributing more bindings.
Also some coffee to keep me coding faster never hurts :slight_smile:

Sponsor @Skinah on GitHub
<https://github.com/sponsors/Skinah/>

Paypal can also be used via
matt A-T pcmus D-O-T C-O-M
