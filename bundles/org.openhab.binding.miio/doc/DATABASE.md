# Mi IO Binding Database File Format

[Back to Overview](../README.md#advanced-adding-local-database-files-to-support-new-devices)

This document describes the JSON "database" files that define the channels, commands and value conversions of `miio:basic` things.
It is meant for contributors who add or improve the database file of a device.
The same files are used by `miio:lumi` and `miio:gateway` things (Xiaomi gateway and its sub devices).
Vacuum things (`miio:vacuum`) are implemented in code and do not use database files.
The examples are taken from the files in `src/main/resources/database/`, sometimes shortened.

- [Overview](#overview)
- [Files, Matching and Reloading](#files-matching-and-reloading)
- [Field Reference](#field-reference)
- [Reading Values](#reading-values)
- [Writing Values](#writing-values)
- [MIoT Devices](#miot-devices)
- [Channel Presentation](#channel-presentation)
- [Cookbook](#cookbook)
- [Testing Your File](#testing-your-file)
- [Pitfalls and Submission Checklist](#pitfalls-and-submission-checklist)

## Overview

Contributor workflow:

1. Discover the device and get its token, see [Discovery](../README.md#discovery) and [Tokens](../README.md#tokens).
   The model id is the thing property `modelId` (also in the `miIO.info` response).
1. Check that the model is not supported yet: `grep -rl '"<model>"' src/main/resources/database` and look in `MiIoDevices.java`.
   To try the device quickly without a file, use the model of a similar device, see [Substitute Model for Unsupported Devices](../README.md#substitute-model-for-unsupported-devices).
1. Create the file.
   Newer (MIoT) devices: let the binding or the tool generate it, see [MIoT Devices](#miot-devices).
   Older devices: start from the file of a similar device, or use the `(experimental) Create channels / test properties for unsupported devices (legacy protocol)` channel of an unsupported thing.
   Property and action names (legacy) or numbers (MIoT) can be found on <https://home.miot-spec.com/>.
1. Test the file, see [Testing Your File](#testing-your-file), and submit it, see [Pitfalls and Submission Checklist](#pitfalls-and-submission-checklist).

How the binding uses a file:

1. The thing reads the `model` from `miIO.info` and stores it in the thing configuration (it can also be set manually).
1. The file whose `id` list contains the model defines the device.
1. During the first update cycle after initialization, all channels except the standard ones (`actions#commands`, `actions#rpc`, `network#...`) are removed and rebuilt from the file.
   For every channel a channel type `miio:<MODEL>_<channel>` is generated (model in upper case, `.` replaced by `_`), unless the file refers to an existing `channelType`.
1. At every polling interval, the channels with `refresh` that are linked to an item are read and converted according to their `type`.
1. A command sent to a channel is converted to device commands by the `actions` of that channel.

Legacy (miIO) devices use named properties (`get_prop`) and named commands (for example `set_power`).
MIoT devices address properties by `siid`/`piid` and actions by `siid`/`aiid`, see [MIoT Devices](#miot-devices).

A minimal file for a device with one switch:

```json
{
  "deviceMapping": {
    "id": ["your.vendor.model"],
    "channels": [
      {
        "property": "power",
        "friendlyName": "Power",
        "channel": "power",
        "type": "Switch",
        "refresh": true,
        "actions": [{"command": "set_power", "parameterType": "ONOFF"}],
        "category": "switch",
        "tags": ["Switch"]
      }
    ]
  }
}
```

## Files, Matching and Reloading

| Location                             | Use                                                                                              |
|--------------------------------------|--------------------------------------------------------------------------------------------------|
| `src/main/resources/database/*.json` | Files delivered with the binding. Subfolders are not scanned.                                    |
| `<openHAB conf>/misc/miio/*.json`    | Local files, for testing or private devices. The exact path is logged at DEBUG level at startup. |

- Files are UTF-8 encoded JSON with the extension `.json`, containing a single object with the key `deviceMapping`.
- A file is used when the model exactly equals (case sensitive) one of the entries in `deviceMapping.id`; one file can list many models.
  The file name is not used for matching, but by convention it is the (first) model, with the suffix `-miot` for MIoT devices (`zhimi.fan.za5-miot.json`).
  The file name is also part of the translation keys of the channel labels.
- If two files list the same model, a local file wins over a bundled file; otherwise the winner is undefined, so avoid duplicate model ids.
- Bundled models must also be registered in `MiIoDevices.java`.
  For a local file this is not needed: an unsupported thing switches to `miio:basic` when a file exists for its model.
- The experimental channels of an unsupported thing write `<model>-experimental.json` (legacy) or `<model>-miot-experimental.json` (MIoT) to `conf/misc/miio`.
  Rename the file when you submit it.

Reloading:

- The files are read when the binding starts, and again when a local `.json` file is added or changed.
  If a new file is not picked up, restart the binding (Karaf console: `bundle:list | grep -i miio`, then `bundle:restart <id>`).
- The channels are built from the file when the thing is initialized: after editing a file, disable and enable the thing.
- A file that cannot be parsed is skipped with a warning naming the file.
  A model without a file gives the warning `Database entry for model '...' cannot be found.`.

Format rules:

- Key names are case sensitive (for example `ChannelGroup`); unknown or misspelled keys are silently ignored.
- `parameterType` values are not case sensitive, but write them in upper case.
  An unrecognized value behaves like `UNKNOWN`.
- Use tabs for indentation, like the existing files (the examples here use spaces).

## Field Reference

### Device (`deviceMapping`)

| Field            | Type             | Default    | Description                                                                        |
|------------------|------------------|------------|------------------------------------------------------------------------------------|
| `id`             | list of strings  | empty      | Model ids this file applies to. A file without ids is never used.                  |
| `propertyMethod` | string           | `get_prop` | Command used to read the properties, see [Reading Values](#reading-values).        |
| `maxProperties`  | integer          | 5          | Maximum number of properties requested in one command.                             |
| `channels`       | list of channels | empty      | The channel definitions.                                                           |
| `readmeComment`  | string           | none       | `Remark` column of the README device list. Not used at runtime.                    |
| `experimental`   | boolean          | false      | Marks the device as `Experimental` in the README device list. Not used at runtime. |

### Channel

| Field                     | Type            | Default    | Description                                                                                                                      |
|---------------------------|-----------------|------------|----------------------------------------------------------------------------------------------------------------------------------|
| `channel`                 | string          | none       | Required. Channel id, unique within the file. Allowed characters: `A-Z`, `a-z`, `0-9`, `_` and `-`.                              |
| `type`                    | string          | none       | Required. The item type, see [Converting the Response](#converting-the-response).                                                |
| `friendlyName`            | string          | channel id | Label of the channel.                                                                                                            |
| `property`                | string          | empty      | Property to read; also matches the response to the channel, so unique among polled channels. Empty for channels without polling. |
| `siid`, `piid`            | integer         | none       | MIoT service and property id. A MIoT channel has both, not both zero.                                                            |
| `refresh`                 | boolean         | false      | Read this channel at every polling interval. Also required with `customRefreshCommand`.                                          |
| `refreshInterval`         | integer         | 1          | Read only every n-th polling cycle; negative (`-1`) reads once. Not the thing `refreshInterval` (seconds).                       |
| `customRefreshCommand`    | string          | none       | Read with this command instead of the `propertyMethod`, see [Custom Refresh Commands](#custom-refresh-commands).                 |
| `customRefreshParameters` | JSON            | none       | Parameters (object or array) appended to the `customRefreshCommand`.                                                             |
| `transformation`          | string          | none       | Conversion of the value read, see [Transformations](#transformations).                                                           |
| `unit`                    | string          | none       | Unit of the value, see [Units](#units).                                                                                          |
| `stateDescription`        | object          | none       | Presentation of the channel, see below.                                                                                          |
| `channelType`             | string          | none       | Existing channel type (for example `system:battery-level`); `miio:` is prepended without `system` prefix. Falls back if unknown. |
| `category`                | string          | none       | Category (icon) of the generated channel type.                                                                                   |
| `tags`                    | list of strings | none       | Semantic tags of the channel.                                                                                                    |
| `actions`                 | list of actions | empty      | How to send commands, see [Action](#action). Use an empty list for read only channels.                                           |
| `readmeComment`           | string          | none       | `Comment` column of the README channel list. A comment starting with `Value mapping` is regenerated from the `options`.          |

The fields `ChannelGroup`, `description` and `transformations` in older files are ignored, do not use them.

### State Description

| Field                        | Type                      | Description                                                                                     |
|------------------------------|---------------------------|-------------------------------------------------------------------------------------------------|
| `minimum`, `maximum`, `step` | number                    | Limits and step of the value.                                                                   |
| `pattern`                    | string                    | Display pattern, for example `%.1f %unit%`.                                                     |
| `readOnly`                   | boolean                   | Read only in the UIs. Commands are still processed.                                             |
| `options`                    | list of `value` / `label` | Selection list. `value` is always a string, also for numbers: `{"value": "1", "label": "Low"}`. |

The state description is part of the generated channel type, so it is ignored when `channelType` is used.

### Action

| Field           | Type        | Default | Description                                                                                               |
|-----------------|-------------|---------|-----------------------------------------------------------------------------------------------------------|
| `command`       | string      | empty   | The command (method) to send. For `ONOFFPARA`, `*` is replaced by `on` or `off`.                          |
| `parameterType` | enumeration | `EMPTY` | How the openHAB command is converted, see [Parameter Types](#parameter-types).                            |
| `parameters`    | list        | empty   | Fixed parameters. The value is inserted at `$value$`.                                                     |
| `siid`, `aiid`  | integer     | none    | MIoT service and action id. A MIoT action has both, not both zero.                                        |
| `condition`     | object      | none    | `name` (not case sensitive) and `parameters` of a [condition](#conditions). Unknown names have no effect. |

## Reading Values

### What Is Polled

- A channel is polled when `refresh` is `true` and the channel is linked to an item.
- The properties are requested with the `propertyMethod`, at most `maxProperties` per command, for example `get_prop["power","bright"]` or `get_properties[{"did":"on","siid":2,"piid":1}]`.
- The answer values are matched to the channels in request order through the `property`.
  A `null` value is ignored; if the number of values differs from the request, the whole answer is ignored.
- The polling interval is the thing `refreshInterval` (default 30 seconds, 0 disables polling).
  A `REFRESH` command updates all channels (at most once every 5 seconds), and all channels are updated 3 seconds after a command.
- A channel `refreshInterval` of 2 reads the channel every second cycle.
  A negative value reads it once, for values that do not change such as a serial number: after initialization, and again on a `REFRESH` of that channel.
  A failed read or an answer without value is retried at the next cycle.

| `propertyMethod`      | Used for                                                                                                      |
|-----------------------|---------------------------------------------------------------------------------------------------------------|
| `get_prop`            | Most legacy devices (default).                                                                                |
| `get_properties`      | MIoT devices.                                                                                                 |
| `get_value`           | A few sensors, for example `cgllc.airmonitor.b1.json`.                                                        |
| `get_device_prop_exp` | Sub devices of the gateway. Sent as nested list; for `miio:lumi` the sub device id is added as first element. |

Answers to other methods are not processed.

### Custom Refresh Commands

A channel with `customRefreshCommand` (and `"refresh": true`, linked) is read with its own command, once per polling cycle, with the `customRefreshParameters` appended, for example `get_arming_time` (see `lumi.gateway.mieu01.json`).

- A command starting with `/` is a Xiaomi cloud request and needs the `cloudServer` of the thing, see `lumi.lock.json`.
  [Variables](#variables) are substituted in the command and parameters.
- An answer without result or with a `null` result (or a list starting with `null`) is ignored.
  Without `transformation`, the first element of a list answer is used; with a `transformation` the complete list is passed.
  A non-list answer is converted to JSON text first.
- Do not use a property method (`get_prop`, `get_properties`, `get_value`, `get_device_prop_exp`) as custom command, its answer would not reach the channel.
- Commands are identified by their text only, so each `customRefreshCommand` must be unique within a file, also when the parameters differ.

### Converting the Response

| `type`               | Value from device                                                | State                          |
|----------------------|------------------------------------------------------------------|--------------------------------|
| `Number`             | number or numeric text                                           | Number                         |
| `Number:<Dimension>` | number or numeric text                                           | Quantity with the channel unit |
| `Dimmer`             | number from 0 to 100                                             | Percent                        |
| `Switch`             | number > 0, or text/boolean `on`, `true`, `1`                    | ON, anything else OFF          |
| `Contact`            | number > 0, or text/boolean `open`, `on`, `true`, `1`            | OPEN, anything else CLOSED     |
| `String`             | text, number or boolean; JSON object or list as JSON text        | Text                           |
| `Color`              | RGB integer (`0xRRGGBB`), or text `h,s,b` (with or without `[]`) | HSB color                      |

Other item types are not updated.
A `unit` makes every `Number` channel a quantity, whatever the dimension; always set it for `Number:<Dimension>`.
Without a `unit` only `Temperature` (°C), `ElectricCurrent` (A), `Energy` (W, historical) and `Time` (hour) get a default unit.

### Transformations

Applied to the value read, before the type conversion.
The name is not case sensitive, only one per channel is possible, and an unknown name or unsuitable value leaves the value unchanged.
There is no reverse conversion when writing.

| Transformation          | Description                                                                                  | Example file                     |
|-------------------------|----------------------------------------------------------------------------------------------|----------------------------------|
| `/10`, `/100`           | Divide by 10 or 100                                                                          | `zhimi.airpurifier.m1.json`      |
| `SecondsToHours`        | Divide by 3600                                                                               | `zhimi.airfresh.va4.json`        |
| `tankLevel`             | Humidifier tank: 127 (no tank) gives -1, otherwise value / 1.2                               | `zhimi.humidifier.ca4.json`      |
| `YeelightSceneId`       | Scene 1 to 6 to `color`, `hsv`, `ct`, `nightlight`, `cf`, `auto_delay_off`, others `unknown` | -                                |
| `bRGBtoHSV`             | Integer with brightness in the highest byte and RGB in the lower bytes to HSB                | `lumi.gateway.mieu01.json`       |
| `addBrightToHSV`        | RGB integer to HSB, with the last value of property `bright` (100 if unknown)                | -                                |
| `addBrightToHSVPower`   | As `addBrightToHSV`, brightness 0 when the last value of property `power` is `off`           | `yeelink.light.color1.json`      |
| `deviceDataTab`         | Cloud response of `/v2/user/getuserdevicedatatab` to a list of the `value` entries           | `lumi.lock.json`                 |
| `getJsonElement-<name>` | Member `<name>` (case sensitive) of a JSON object, or a JSON path, see below                 | `chuangmi.plug.212a01-miot.json` |
| `getDiDElement`         | Member of a JSON object named after the device id of the thing                               | `chuangmi.plug.212a01-miot.json` |

#### Selecting Parts of a JSON Response

The input of `getJsonElement-` can be a JSON object, array, or a string containing JSON.
If the top level object has a member with exactly the given name, it is returned; otherwise the text is evaluated as a path.
If nothing can be selected, the input is returned unchanged.

| Path element | Meaning                                                                                                                   |
|--------------|---------------------------------------------------------------------------------------------------------------------------|
| `a.b`        | Member `b` of the object `a`.                                                                                             |
| `a[n]`       | Element `n` (from 0) of the array `a`.                                                                                    |
| `a[*]`       | Apply the rest of the path to every element of `a`, return an array of the results that resolve.                          |
| `[*]`, `[n]` | As above, when the input itself is an array.                                                                              |
| `{x,y}`      | Last segment only: only the listed members, of an object or each object of an array. `{id,cook.time}` returns `time` too. |

Strings on the path that contain JSON are parsed; a string at the end is returned as is.
Member names in a path cannot contain `.`, `[`, `]`, `{` or `}`.
A `{...}` that finds no member returns the input, a `[*]` that finds nothing an empty array.

For `{"recipes":[{"recipeID":1,"recipeName":"Fries"},{"recipeID":2,"recipeName":"Pizza"}],"hasMore":false}`:

| Transformation                         | Result              |
|----------------------------------------|---------------------|
| `getJsonElement-hasMore`               | `false`             |
| `getJsonElement-recipes[*].recipeName` | `["Fries","Pizza"]` |
| `getJsonElement-recipes[1].recipeName` | `"Pizza"`           |

### Units

The `unit` creates quantities when reading and converts quantity commands to a plain number when writing.
Names and aliases are not case sensitive.
Other symbols are logged at DEBUG and only work when openHAB knows the symbol.

| Unit                              | Aliases                               |
|-----------------------------------|---------------------------------------|
| `CELSIUS`                         | `C`, `celcius`                        |
| `FAHRENHEIT`, `KELVIN`            | `K` for kelvin                        |
| `PASCAL`, `HPA`                   | `pa` for pascal                       |
| `SECOND`, `MINUTE`, `HOUR`, `DAY` | `seconds`, `minutes`, `hours`, `days` |
| `AMPERE`, `MILLI_AMPERE`          | `mA` for milli ampere                 |
| `VOLT`, `MILLI_VOLT`              | `mV` for milli volt                   |
| `WATT`                            | `W`                                   |
| `KILOWATT_HOUR`                   | `kwh`                                 |
| `LITRE`, `LITER`                  | `liter`, `litre`, `L`                 |
| `LUX`                             |                                       |
| `RADIANS`                         | `radians`                             |
| `DEGREE` (angle)                  | `degree`                              |
| `SQUARE_METRE`                    | `square_meter`, `squaremeter`         |
| `PERCENT`                         | `percentage`                          |
| `PPM`                             | `parts_per_million`                   |
| `KGM3`                            | `kilogram_per_cubicmeter`             |
| `UGM3`                            | `microgram_per_cubicmeter`, `μg/m3`   |
| `M3`                              | `cubic_meter`, `cubic_metre`          |

## Writing Values

### How a Command Is Processed

All `actions` of the channel are processed in order.
For each action:

1. A quantity command is converted to the channel `unit` and continues as plain number.
1. The command is converted to a value according to the `parameterType`.
1. The `condition` may change or reject the value.
1. For MIoT channels and actions the value is wrapped in the MIoT structure.
1. The value is placed in the `parameters` and sent as `<command><parameters as JSON list>`, for example `set_ct_abx[3000,"smooth",500]`.
   Without a value (no match with `parameterType` or condition) the action is skipped.

### Parameter Types

| `parameterType`   | Command   | Value                                                                                                                           |
|-------------------|-----------|---------------------------------------------------------------------------------------------------------------------------------|
| `ONOFF`           | ON, OFF   | `"on"`, `"off"`                                                                                                                 |
| `ONOFFBOOL`       | ON, OFF   | `true`, `false`                                                                                                                 |
| `ONOFFBOOLSTRING` | ON, OFF   | `"true"`, `"false"`                                                                                                             |
| `ONOFFNUMBER`     | ON, OFF   | `1`, `0`                                                                                                                        |
| `ONOFFPARA`       | ON, OFF   | No value: `*` in `command` is replaced by `on`/`off`, parameter list empty.                                                     |
| `NUMBER`          | number    | The number.                                                                                                                     |
| `STRING`          | text      | The text as typed.                                                                                                              |
| `COLOR`           | HSB color | RGB integer (`0xRRGGBB`). A number is passed as brightness, ON/OFF as 100/0.                                                    |
| `EMPTY`           | any       | Command ignored; the `parameters` (or the condition `returnValue`) are sent, for legacy as one nested element `command[[...]]`. |
| `NONE`            | any       | Value only available for the `condition`, not placed in the `parameters`.                                                       |
| `UNKNOWN`         | any       | No value for ON/OFF and text. Used by generated placeholder actions that never send.                                            |
| `CUSTOMSTRING`    | text      | Replaces only the `$value$` token inside its entry (`"color,$value$"` gives `"color,red"`). Otherwise like `STRING`.            |

- A number or percent command always results in the number, except for `COLOR` and `EMPTY`; ON/OFF are only converted by `ONOFF...` and `COLOR`.
  So a `Switch` or `String` channel with `NUMBER` sends nothing unless a `matchValue` `returnValue` supplies the value.
  Other commands (UP, DOWN, STOP, ...) are sent as lower case text.
- Text is sent exactly as typed (`Auto` stays `Auto`); add the `LowerCase` condition for devices that only accept lower case.
- A missing `parameterType` is `EMPTY`, which ignores the command value.

### Parameters and `$value$`

- Without `parameters` the value is the only parameter: `set_power["on"]`.
- With `parameters` the value replaces the entry that contains `$value$` (case insensitive, per action), for example `[0, "$value$"]` gives `cron_add[0,30]`.
  Without such an entry the value replaces the first entry, so always write `$value$` explicitly.
- `$value$` can also be nested in an object or list, for legacy and MIoT commands.
  A text that is only the token gets the value with its type (`"value": "$value$"` sends `"value": 18`); a token inside a longer text is replaced as text and must result in valid content there.
  All tokens get the same value, after `parameterType` and `condition`.
- A command that still contains `$value$` is not sent (DEBUG log).
  This happens with `EMPTY`, `ONOFFPARA` and `NONE`, which never insert a value; `NONE` and `ONOFFPARA` still send their `parameters` as written once a value was determined (ON/OFF or number for `ONOFFPARA`, number for `NONE`, or a condition value).

### Variables

`$name$` is replaced in every command text before sending (command, parameters, custom refresh commands).
Quotes directly around it are removed; a variable without value stays as is.

| Variable                                                                                       | Value                                                                                                                                 |
|------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| `deviceId`                                                                                     | Device id of the thing, as quoted string.                                                                                             |
| `modelId`, `firmwareVersion`, `hardwareVersion`, `wifiFirmware`, `mcuFirmware`, `serialNumber` | Thing properties as quoted string. Any other set thing property works as well.                                                        |
| `timestamp`                                                                                    | Time of the last update or command in seconds since 1970, as number.                                                                  |
| property name                                                                                  | Last value read (before transformation), unquoted, so only for numbers and booleans. For a custom refresh channel use the channel id. |

### Conditions

| Name                 | Effect                                                                                                                                                                                                                        |
|----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `matchValue`         | Sends only when the command text fully matches (regular expression, case sensitive, before `LowerCase`) the `matchValue` of a `parameters` entry. Its optional `returnValue` (any JSON) replaces the value. First match wins. |
| `BrightnessExisting` | Passes numbers 1 to 100 (larger becomes 100), rejects 0 and negative. Other values pass.                                                                                                                                      |
| `BrightnessOnOff`    | Number lower than 1 becomes `"off"`, otherwise `"on"`. Other values pass.                                                                                                                                                     |
| `HSBOnly`            | Passes only HSB color commands.                                                                                                                                                                                               |
| `HSVTOBRGB`          | HSB color to integer with brightness in the highest byte and RGB in the lower bytes. Rejects others.                                                                                                                          |
| `LowerCase`          | Converts text to lower case, see the `mode` channel of `zhimi.airpurifier.v6.json`.                                                                                                                                           |

## MIoT Devices

MIoT channels use the same fields plus `siid`/`piid` (property) or `siid`/`aiid` (action).

- Reading: use `"propertyMethod": "get_properties"`.
  The binding requests `{"did":"<property>","siid":..,"piid":..}` and takes the `value` of each answer entry.
  The `property` is freely chosen but must be unique and not empty.
  Generated files use `"maxProperties": 1`; more is faster, but a mismatched answer loses all values.
- Writing a property: an action `"command": "set_properties"` without `parameters` sends `set_properties[{"did":"<channel>","siid":..,"piid":..,"value":<value>}]`.
  Use `ONOFFBOOL` for booleans, `NUMBER` for numbers and enumerations (with `options`) and `STRING` for text.
- Actions are sent as `action{"did":"<channel>","siid":..,"aiid":..,"in":[..]}`.
  The usual pattern is a `String` channel without `siid`/`piid`, `property` or `refresh`, with `options` and one action per option selected by `matchValue`.
  The `in` list is the `parameters` of an `EMPTY` action or the `returnValue` of the matching condition (otherwise empty).

```json
{
  "property": "",
  "friendlyName": "Vacuum Action",
  "channel": "vacuumaction",
  "type": "String",
  "stateDescription": {"options": [{"value": "vacuum", "label": "Vacuum"}, {"value": "stop", "label": "Stop"}]},
  "actions": [
    {
      "command": "action", "parameterType": "EMPTY", "siid": 18, "aiid": 1,
      "condition": {"name": "matchValue", "parameters": [{"matchValue": "vacuum", "returnValue": [{"piid": 1, "value": 2}]}]}
    },
    {
      "command": "action", "parameterType": "EMPTY", "siid": 18, "aiid": 2,
      "condition": {"name": "matchValue", "parameters": [{"matchValue": "stop"}]}
    }
  ]
}
```

An action that needs user input (for example the rooms to clean) uses `NUMBER` or `STRING` with `$value$` in the input list; `EMPTY` would not send it:

```json
{"command": "action", "parameterType": "NUMBER", "siid": 4, "aiid": 1,
 "parameters": [{"piid": 1, "value": 18}, {"piid": 10, "value": "{\"selects\":[[$value$,1,1,2,1]]}"}]}
```

### Creating a File for a MIoT Device

- The experimental channel `Create channels for new/unsupported devices (MIOT protocol)` of an unsupported thing reads the published spec, writes `<model>-miot-experimental.json` to `conf/misc/miio` and tests all readable properties (result in `userdata/miio/test-<model>-<timestamp>.txt`).
- Developers can use `MiotJsonFileCreator` (needs internet), from the binding folder:
  `mvn test-compile exec:java -Dexec.mainClass="org.openhab.binding.miio.internal.MiotJsonFileCreator" -Dexec.classpathScope="test" -Dexec.args="<model>"`.
  It writes `src/main/resources/database/<model>-miot.json` (never overwrites, adds a counter instead) and tries the model with 1 to 12 appended when it does not end with a digit.
  The newest released spec is used (else the newest preview or debug); append `:<version>` to the model for another one.
- Every readable or writable property except service 1 gets a channel (writable with `set_properties`, else `readOnly`).
  An action with exactly one input that is a property of its service gets its own channel `<service>_<action>` using `$value$`; all other actions are options of the `actions` channel.
- Review the generated file:
  - Correct the (often machine translated) names and option labels, the `type` (on/off is a `Switch`), `unit` and `stateDescription`, and remove useless channels.
  - Do not change `siid`, `piid`, `aiid` or option `value`s.
  - `"parameterType": "UNKNOWN"` actions are never-sending placeholders for actions with several or non-property inputs, with the spec input list as `parameters`.
    Replace them with a working definition or remove them.
    The tool class already removes them (with a warning), the experimental channel keeps them.
  - The generator sets `"experimental": true`.

## Channel Presentation

- The label is the `friendlyName`.
  Bundled files get translation keys `ch.<file name without .json>.<channel>` in `OH-INF/i18n/basic.properties`; local files are not translated.
- Use quantity types with a `unit` for physical values, and `%unit%` in the `pattern`.
- `category` is the icon, `tags` are semantic tags (case sensitive, must exist in the openHAB semantic model).
  Use at most one point tag (`Switch`, `Control`, `Setpoint`, `Measurement`, `Status`, ...) and one property tag (`Light`, `Temperature`, `Duration`, ...).
- With `channelType` (for example `"channelType": "system:battery-level"`) only the `tags` are taken from the file.

## Cookbook

Each snippet is one element of the `channels` list.
Complete files worth reading: `chuangmi.plug.m1.json` (small legacy device), `yeelink.light.color1.json` (light with conditions and variables), `lumi.gateway.mieu01.json` (custom refresh commands), `dmaker.fan.p8-miot.json` (MIoT properties and actions) and `dreame.vacuum.mc1808-miot.json` (MIoT actions with input).

### On/Off Command Words

`ONOFF` sends `set_power["on"]`.
For devices with separate commands, `ONOFFPARA` sends `set_on[]` and `set_off[]`:

```json
{"property": "on", "friendlyName": "Power", "channel": "power", "type": "Switch", "refresh": true,
 "actions": [{"command": "set_*", "parameterType": "ONOFFPARA"}]}
```

### Read Only Value with Scaling, Unit and Tags

```json
{
  "property": "temp_dec",
  "friendlyName": "Temperature",
  "channel": "temperature",
  "type": "Number:Temperature",
  "unit": "CELSIUS",
  "stateDescription": {"pattern": "%.1f %unit%", "readOnly": true},
  "refresh": true,
  "transformation": "/10",
  "actions": [],
  "category": "temperature",
  "tags": ["Measurement", "Temperature"]
}
```

### Number with Unit and Fixed Parameters

`2 h` or `120 min` both send `cron_add[0,120]`:

```json
{
  "property": "delayoff",
  "friendlyName": "Shutdown Timer",
  "channel": "delayoff",
  "type": "Number:Time",
  "unit": "minutes",
  "stateDescription": {"pattern": "%.0f %unit%"},
  "refresh": true,
  "actions": [{"command": "cron_add", "parameterType": "NUMBER", "parameters": [0, "$value$"]}],
  "tags": ["Setpoint", "Duration"]
}
```

### Selection List

Sends the option value, for example `set_mode["auto"]`:

```json
{
  "property": "mode",
  "friendlyName": "Mode",
  "channel": "mode",
  "type": "String",
  "stateDescription": {"options": [{"value": "auto", "label": "Auto"}, {"value": "silent", "label": "Silent"}]},
  "refresh": true,
  "actions": [{"command": "set_mode", "parameterType": "STRING"}],
  "tags": ["Control"]
}
```

### Dimmer and Color with On/Off

`BrightnessExisting` passes only brightness 1 to 100 to `set_bright`; `BrightnessOnOff` turns 0/OFF into `off` and other values/ON into `on` for `set_power`.
An HSB command is also a number, so all three actions receive it: `HSBOnly` lets only the color reach `set_rgb`, the other two get its brightness.
A `Dimmer` channel uses only the last two actions.

```json
{
  "property": "rgb",
  "friendlyName": "RGB Color",
  "channel": "rgbColor",
  "type": "Color",
  "refresh": true,
  "transformation": "addBrightToHSVPower",
  "actions": [
    {"command": "set_rgb", "parameterType": "COLOR", "parameters": ["$value$", "smooth", 500], "condition": {"name": "HSBOnly"}},
    {"command": "set_bright", "parameterType": "NUMBER", "condition": {"name": "BrightnessExisting"}},
    {"command": "set_power", "parameterType": "ONOFF", "condition": {"name": "BrightnessOnOff"}}
  ]
}
```

`HSVTOBRGB` (see `lumi.gateway.mieu01.json`) is for devices that want brightness and color in one number.

### A Fixed Parameter per Option

`returnValue` makes the value depend on the command (shortened to one option).
The `colorflow` channel of the same file uses this on a `Switch`.

```json
{
  "property": "",
  "friendlyName": "Color Flow Scene",
  "channel": "colorflowScene",
  "type": "String",
  "stateDescription": {"options": [{"value": "sunrise", "label": "Sunrise"}]},
  "actions": [
    {
      "command": "start_cf", "parameterType": "STRING", "parameters": [3, 1, "$value$"],
      "condition": {"name": "matchValue", "parameters": [
        {"matchValue": "sunrise", "returnValue": "50,1,16731392,1,360000,2,1700,10,540000,2,2700,100"}]}
    }
  ]
}
```

### Command Without Value and Variables from Other Properties

`NONE` does not send the value; the parameters use the last values of `rgb` and `ct`, and `matchValue` selects the action:

```json
{
  "property": "color_mode",
  "friendlyName": "Color Mode",
  "channel": "colorMode",
  "type": "Number",
  "stateDescription": {"options": [{"value": "0", "label": "Default"}, {"value": "2", "label": "CT mode"}]},
  "refresh": true,
  "actions": [
    {"command": "set_rgb", "parameterType": "NONE", "parameters": ["$rgb$", "smooth", 500],
     "condition": {"name": "matchValue", "parameters": [{"matchValue": "1"}]}},
    {"command": "set_ct_abx", "parameterType": "NONE", "parameters": ["$ct$", "smooth", 500],
     "condition": {"name": "matchValue", "parameters": [{"matchValue": "2"}]}}
  ]
}
```

### Cloud Data with Variables and Transformation

A command starting with `/` goes to the Xiaomi cloud; `$timestamp$` and `$deviceId$` are replaced and `deviceDataTab` converts the answer.
Similarly, `chuangmi.plug.212a01-miot.json` reads `/device/get_bledevice_by_gateway` with `getDiDElement` and `"refreshInterval": 2`.

```json
{
  "property": "log",
  "friendlyName": "Device Log",
  "channel": "log",
  "type": "String",
  "refresh": true,
  "customRefreshCommand": "/v2/user/getuserdevicedatatab",
  "customRefreshParameters": {"limit": 10, "timestamp": "$timestamp$", "did": "$deviceId$", "type": "prop", "key": "device_log"},
  "transformation": "deviceDataTab",
  "actions": []
}
```

### Devices Behind the Xiaomi Gateway

Sub devices are `miio:lumi` things and use `get_device_prop_exp`.
`lumi.plug.mmeu01.json` shows commands that are JSON objects as text, with variables.

```json
{
  "deviceMapping": {
    "id": ["lumi.light.aqcn02", "ikea.light.led1545g12"],
    "propertyMethod": "get_device_prop_exp",
    "maxProperties": 3,
    "readmeComment": "Needs to have the Xiaomi gateway configured in the binding as bridge.",
    "channels": []
  }
}
```

## Testing Your File

1. Put the file in `conf/misc/miio` (create the folder if needed), check that it is valid JSON, and restart the binding, see [Reloading](#files-matching-and-reloading).
1. Enable DEBUG logging: `log:set DEBUG org.openhab.binding.miio`.
   The startup log shows the folder in `... Watching for database files at path: ...`.
1. Create a `miio:basic` thing with the model of your file and link items (only linked channels are read).
   The log shows `Using device database: <file> for device <model>`.
1. Useful log lines:
   - `Command added to Queue ...` and `Received response for device ...`: every command and answer.
   - `Property '...' returned null (is it supported?)`: unknown property.
   - `Channel not found for ...`: an answer name matches no `property`.
   - `Conditional command ... not send, condition '...' not met` or `Command not send. Value null`: `parameterType` or `condition` did not match.
1. Try raw commands with the `actions#commands` channel, for example `get_prop["power"]` or `action{"did":"x","siid":2,"aiid":1,"in":[]}`.
1. After changing the file, disable and enable the thing.

## Pitfalls and Submission Checklist

Common mistakes:

- A channel without `refresh` is never read, an unlinked one neither.
- Duplicate `property` values of polled channels, channel ids or `customRefreshCommand` texts.
- A `String` channel needs `STRING` or `EMPTY` and a `Switch` an `ONOFF...` type or `EMPTY` (or a `matchValue` with `returnValue`), otherwise nothing is sent.
- `readOnly` only affects the UI; use `"actions": []` for channels that cannot be written.
- An old `<model>[-miot]-experimental.json` in `conf/misc/miio` with the same model id: edit or delete it instead of adding a second file.

Checklist for a pull request:

1. The file is in `src/main/resources/database/`, valid JSON, named after the model, without tokens, IP addresses, device ids or other personal data (also not in `readmeComment`).
1. The channels return data on a real device, otherwise `"experimental": true` is set.
1. Labels are clear English; `type`, `unit`, `stateDescription` and `tags` are consistent; channel ids follow the style of similar files.
1. Every model id is added in alphabetical order to `MiIoDevices.java` with product name and thing type (`THING_TYPE_BASIC`, `THING_TYPE_LUMI` for gateway sub devices, `THING_TYPE_GATEWAY` for gateways).
1. README device and channel lists and `OH-INF/i18n/basic.properties` are regenerated from the binding folder with
   `mvn test-compile exec:java -Dexec.mainClass="org.openhab.binding.miio.internal.ReadmeHelper" -Dexec.classpathScope="test"`.
   It also prints the `MiIoDevices.java` lines (names from `src/main/resources/misc/device_names.json`) and a change log text.
   Edit `README.base.md`, not `README.md`, and do not edit the `basic_xx.properties` translations.
1. `mvn spotless:apply` and `mvn clean install -pl :org.openhab.binding.miio` succeed.
1. The pull request follows `CONTRIBUTING.md`: title `[miio] Add support for <device>`, model ids in the commit message (`* <product name> (modelId: <model>)`), signed-off commits (`git commit -s`) without merge commits, and `Fixes #<issue>` or the forum thread linked.
