# FreeMobile SMS Binding

_Give some details about what this binding is meant for - a protocol, system, specific device._

_If possible, provide some resources like pictures, a YouTube video, etc. to give an impression of what can be done with this binding. You can place such resources into a `doc` folder next to this README.md._

## Supported Things

This binding supports the following thing types:

| Thing         | Thing Type | Description                                             |
|---------------|------------|---------------------------------------------------------|
| account       | Thing      | One callable account.                                   |

## Discovery

Accounts should be created manually.

## Binding Configuration

The binding does not have settings.
Future releases can ad the possibility to change the base URL, to let the user follow possible changes in the API.

## Thing Configuration

### Account

The *account* thing requires the following configuration parameters:

| Parameter Label | Parameter ID | Description           | Required | Default |
|-----------------|--------------|-----------------------|----------|---------|
| User Identifier | user         | The user identifier.  | true     | None    |
| Password        | password     | The related password. | true     | None    |

## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```ESH-INF/thing``` of your binding._

## Full Example

demo.things:

```xtend
Thing freemobilesms:account:dad "Dad" [ user="12345", password="abcde" ]
```

String DAD_SMS "Dad's SMS" { channel="freemobilesms:account:dad" }
demo.items:

```xtend
```

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
