# Mi IO Binding Database File Format

[Back to Overview](../README.md#advanced-adding-local-database-files-to-support-new-devices)

This document describes the JSON "database" files that define the channels, commands and value conversions of `miio:basic` things.
It is meant for contributors who add or improve the database file of a device.
The same files and logic are used by `miio:lumi` and `miio:gateway` things (Xiaomi gateway and its sub devices).
Vacuum things (`miio:vacuum`) are implemented in code and do not use database files.

The format is defined by the classes in `org.openhab.binding.miio.internal.basic`.
The behavior described here is implemented in `MiIoBasicHandler` and its base class `MiIoAbstractHandler`.
The examples are taken from the files in `src/main/resources/database/`, sometimes shortened.

- [Contributor Workflow](#contributor-workflow)
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
- [Example Files](#example-files)

## Contributor Workflow

1. Discover the device and get its token, see [Discovery](../README.md#discovery) and [Tokens](../README.md#tokens).
1. Find the model id of the device: it is the thing property `modelId` (also in the `miIO.info` response).
1. Check that the model is not supported yet and not used by another file: `grep -rl '"<model>"' src/main/resources/database` and look in `MiIoDevices.java`.
1. To try the device quickly without a file, use the model of a similar device, see [Substitute Model for Unsupported Devices](../README.md#substitute-model-for-unsupported-devices).
1. Create the file.
   Newer (MIoT) devices: let the binding or the tool generate it, see [MIoT Devices](#miot-devices).
   Older devices: start from the file of a similar device, or use the `(experimental) Create channels / test properties for unsupported devices (legacy protocol)` channel of an unsupported thing, which tests the properties known from other devices.
1. Property and action names (legacy) or numbers (MIoT) can be found on <https://home.miot-spec.com/>.
   Commands can be tried with the `actions#commands` channel, see [Testing Your File](#testing-your-file).
1. Channel ids may contain `A-Z`, `a-z`, `0-9`, `_` and `-`, and must be unique within the file.
   Follow the style of the files of similar devices; the MIoT generator derives them from the property name with `_` instead of `-` and `.`.
1. Do not put tokens, IP addresses, device ids or other personal data in the file, also not in a `readmeComment`.
1. Set `"experimental": true` as long as no user confirmed that the file works.
1. Submit the file as described in [Pitfalls and Submission Checklist](#pitfalls-and-submission-checklist), and link the issue or forum thread of the device in the pull request.

## Overview

How the binding uses a database file:

1. The thing asks the device for its `miIO.info` and stores the reported `model` in the thing configuration (the value can also be set manually, see the README).
1. The model is looked up in the list of all `id` values of all database files.
   The matching file defines the device.
1. During the first update cycle after the thing is initialized, the channels of the thing are rebuilt from the file.
   All channels except the standard ones (`actions#commands`, `actions#rpc` and the `network#` channels) are removed first.
   For every channel a channel type `miio:<MODEL>_<channel>` is generated (model in upper case, `.` replaced by `_`), unless the file refers to an existing `channelType`.
1. At every polling interval, the channels that have `refresh` set and are linked to an item are read from the device.
   The response is converted to an openHAB state according to the channel `type`.
1. When a command is sent to a channel, the `actions` of that channel are used to build the command for the device.

Two protocol flavors use the same format:

- Legacy (miIO) devices use named properties (`get_prop`) and named commands (for example `set_power`).
- MIoT devices address properties by service and property numbers (`siid`/`piid`) and actions by `siid`/`aiid`.
  See [MIoT Devices](#miot-devices).

A minimal file for a device with one switch (the `id` is replaced by a placeholder, use the model id of your device):

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

### Location and Names

| Location | Use |
|----------|-----|
| `src/main/resources/database/*.json` | Files delivered with the binding. Subfolders are not scanned. |
| `<openHAB conf>/misc/miio/*.json` | Local files, for testing or private devices. The exact path is logged at DEBUG level when the binding starts. |

- Files must be UTF-8 encoded JSON and have the extension `.json`.
- A device is matched on the `id` values, not on the file name.
  By convention the file is named after the (first) model, for example `yeelink.light.color1.json`, and files for MIoT devices have the suffix `-miot`, for example `zhimi.fan.za5-miot.json`.
  The file name is also part of the translation keys of the channel labels, see [Channel Presentation](#channel-presentation).
- The experimental channels of an unsupported thing write local files named `<model>-experimental.json` (legacy protocol) and `<model>-miot-experimental.json` (MIoT) to `conf/misc/miio`.
  Rename the file when you submit it.

### Matching a Device to a File

- A file is used for a device when the model of the device exactly equals (case sensitive) one of the entries in `deviceMapping.id`.
  One file can serve many models, list all of them in `id`.
- If two files list the same model, one of them wins.
  A local file always wins over a bundled file.
  Between two bundled files, or two local files, the winner is not defined, so avoid duplicate model ids.
- Models of bundled files must also be registered in `MiIoDevices.java`, see [Submission Checklist](#pitfalls-and-submission-checklist).
  For a local file this is not needed: a thing that is not yet known as a basic device switches to the `miio:basic` type when a database file exists for its model.

### Reloading

- The files are read when the binding starts, and again when a local `.json` file is added or changed.
  If a new file is not picked up, restart the binding.
  In the Karaf console: `bundle:list | grep -i miio` shows the bundle id, `bundle:restart <id>` restarts the binding (or restart openHAB).
- The channels of a thing are built from the file when the thing is initialized, the file is read again each time.
  After editing a file that is already known for the model, disable and enable the thing to rebuild its channels.
- A file that cannot be parsed, or that does not contain a JSON object, is skipped and a warning with the file name is logged.
  A model without a file gives the warning `Database entry for model '...' cannot be found.`.

### Format Rules

- The file must contain a single object with the key `deviceMapping`.
- Key names are case sensitive (for example `ChannelGroup` has a capital C).
  Unknown or misspelled keys are silently ignored.
- Enumerations (`parameterType`) are not case sensitive, but write them in upper case like the existing files.
  An unrecognized value behaves like `UNKNOWN`: nothing is sent for ON/OFF and text commands.
- Use tabs for indentation, like the existing files (the examples in this document use spaces).

## Field Reference

### Device (`deviceMapping`)

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `id` | list of strings | empty | Model ids this file applies to. A file without ids is never used. |
| `propertyMethod` | string | `get_prop` | Command used to read the properties, see [Reading Values](#reading-values). |
| `maxProperties` | integer | 5 | Maximum number of properties requested in one command. |
| `channels` | list of channels | empty | The channel definitions. |
| `readmeComment` | string | none | Text for the `Remark` column of the device list in the README. Not used at runtime. |
| `experimental` | boolean | false | Marks the device as `Experimental` in the device list of the README. Not used at runtime. |

### Channel

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `channel` | string | none | Required. The channel id, unique within the file. |
| `type` | string | none | Required, a channel without type is skipped. The openHAB item type, see [Converting the Response](#converting-the-response). |
| `friendlyName` | string | channel id | Label of the channel. |
| `property` | string | empty | Name of the property to read. It is also used to match the response to the channel, so it must be unique among the polled channels. Leave empty for channels without polling. For MIoT it is a freely chosen unique name. |
| `siid`, `piid` | integer | none | MIoT service and property id. A channel is a MIoT channel when both are present and not both zero. |
| `refresh` | boolean | false | Read this channel at every polling interval. Also required for channels with a `customRefreshCommand`. |
| `refreshInterval` | integer | 1 | Read only at every n-th polling cycle. Not to be confused with the `refreshInterval` of the thing, which is in seconds. |
| `customRefreshCommand` | string | none | Read this channel with its own command instead of the `propertyMethod`, see [Custom Refresh Commands](#custom-refresh-commands). |
| `customRefreshParameters` | JSON | none | Parameters (object or array) appended to the `customRefreshCommand`. |
| `transformation` | string | none | Conversion applied to the value read, see [Transformations](#transformations). |
| `unit` | string | none | Unit of the value, see [Units](#units). |
| `stateDescription` | object | none | Presentation of the channel, see below. |
| `channelType` | string | none | Use an existing channel type (for example `system:battery-level`) instead of a generated one. Without `system` prefix `miio:` is prepended. If the type does not exist, the generated type is used. |
| `category` | string | none | Category (icon) of the generated channel type. |
| `tags` | list of strings | none | Semantic tags, applied to the channel. They must be valid openHAB semantic tags. |
| `actions` | list of actions | empty | How to send commands to the device, see [Action](#action). Use an empty list for read only channels. |
| `readmeComment` | string | none | Text for the `Comment` column of the channel list in the README. Not used at runtime. A comment that starts with `Value mapping` is regenerated from the `options` when the README is generated. |

The fields `ChannelGroup`, `description` and `transformations` appear in older files and are ignored, do not use them in new files.

### State Description

| Field | Type | Description |
|-------|------|-------------|
| `minimum`, `maximum`, `step` | number | Limits and step of the value. |
| `pattern` | string | Display pattern, for example `%.1f %unit%`. |
| `readOnly` | boolean | Marks the channel as read only in the UIs. It does not stop commands from being processed. |
| `options` | list of `value` / `label` | Allowed values, shown as selection list. `value` is always a string, also for numeric channels, for example `{"value": "1", "label": "Low"}`. An option without `value` is ignored. |

The state description is part of the generated channel type.
It is ignored when `channelType` refers to an existing channel type.

### Action

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `command` | string | empty | The command (method) to send. For `ONOFFPARA` the character `*` is replaced by `on` or `off`. |
| `parameterType` | enumeration | `EMPTY` | How the command received from openHAB is converted, see [Parameter Types](#parameter-types). |
| `parameters` | list | empty | Fixed parameters of the command. The value is inserted where an entry contains `$value$`. |
| `siid`, `aiid` | integer | none | MIoT service and action id. The action is a MIoT action when both are present and not both zero. |
| `condition` | object | none | Only send the action when the condition is met, or change the value, see [Conditions](#conditions). |

### Condition

| Field | Type | Description |
|-------|------|-------------|
| `name` | string | Name of the condition (not case sensitive). An unknown name has no effect. |
| `parameters` | list | Parameters of the condition, used by `matchValue`. |

## Reading Values

### What Is Polled

- A channel is polled when `refresh` is `true` and the channel is linked to an item.
  Unlinked channels are skipped.
- The polled properties are requested with the `propertyMethod`, at most `maxProperties` per command.
  Legacy example: `get_prop["power","bright","delayoff"]`.
  MIoT example: `get_properties[{"did":"on","siid":2,"piid":1}]`.
- The device answers with a list of values in the order of the request.
  The binding matches them to the channels through the `property` name.
  A value that is `null` is ignored.
  If the number of values differs from the number of requested properties, the whole answer is ignored.
- The polling interval is the `refreshInterval` of the thing (default 30 seconds, 0 disables the periodic polling).
  A `REFRESH` command on a channel triggers an update of all channels (at most once every 5 seconds).
  Three seconds after a command was sent, all channels are updated as well.
- With a `refreshInterval` on the channel, for example 2, the channel is read every second polling cycle.

Supported `propertyMethod` values (answers to other methods are not processed):

| Value | Used for |
|-------|----------|
| `get_prop` | Most legacy devices. This is the default. |
| `get_properties` | MIoT devices. |
| `get_value` | A few sensors, for example `cgllc.airmonitor.b1.json`. |
| `get_device_prop_exp` | Sub devices of the Xiaomi gateway. The request is sent as a nested list, and for `miio:lumi` things the id of the sub device is added as first element. |

### Custom Refresh Commands

A channel with `customRefreshCommand` is not read with the `propertyMethod`, instead the given command is sent separately (once per polling cycle per channel).
The channel also needs `"refresh": true` and must be linked to an item.

- The command is sent with the `customRefreshParameters` appended, for example `get_arming` or `get_prop_fm` (see `lumi.gateway.json`).
- A command that starts with `/` is a request to the Xiaomi cloud and needs the `cloudServer` of the thing to be set, see `lumi.lock.json`.
- Variables in the command and parameters are substituted, see [Variables](#variables).
- The response is stored in the channel.
  When the response is a list and there is no `transformation`, the first element is used.
  With a `transformation` the complete list is passed to the transformation.
  A response that is not a list is converted to JSON text first.
- Do not use one of the property methods (`get_prop`, `get_properties`, `get_value`, `get_device_prop_exp`) as custom command.
  Their answers are processed as regular property answers, which are only matched to channels without `customRefreshCommand`, so the value would not reach the channel.
- The commands are identified by their text only: each `customRefreshCommand` text must be unique within a file, also when the `customRefreshParameters` differ.
  Put parameters that distinguish two commands in the command text itself.

### Converting the Response

The channel `type` determines how the value is converted to an openHAB state.
Other item types than the ones in this table are not updated.

| `type` | Value from device | State |
|--------|-------------------|-------|
| `Number` | number or numeric text | Number. |
| `Number:<Dimension>` | number or numeric text | Quantity, with the `unit` of the channel (see [Units](#units)). |
| `Dimmer` | number from 0 to 100 | Percent. |
| `Switch` | number: greater than 0 is ON. Text or boolean: `on`, `true` or `1` is ON | ON or OFF. Anything else is OFF. |
| `Contact` | number: greater than 0 is OPEN. Text or boolean: `open`, `on`, `true` or `1` is OPEN | OPEN or CLOSED. Anything else is CLOSED. |
| `String` | text, number or boolean as text. A JSON object or list is converted to JSON text | Text. |
| `Color` | integer in RGB format (`0xRRGGBB`), or text `h,s,b` (with or without `[]`) | HSB color. |

A `unit` is applied to every `Number` and `Number:<Dimension>` channel: the state is a quantity as soon as a `unit` is set, whatever the dimension.
Always set a `unit` for `Number:<Dimension>` channels.
The dimension only selects the default unit when the `unit` is empty, so a wrong dimension silently gives a wrong default.
Without a `unit`, only the dimensions `Temperature` (°C), `ElectricCurrent` (A), `Energy` (W, historical) and `Time` (hour) get a default unit, other dimensions are set without unit.

### Transformations

The `transformation` is applied to the value read, before the type conversion.
The name is not case sensitive.
An unknown name leaves the value unchanged, a value that the transformation cannot handle is normally passed on unchanged as well.
Only one transformation per channel is possible.

| Transformation | Description | Example file |
|----------------|-------------|--------------|
| `/10` | Divide by 10 | `zhimi.airpurifier.m1.json` |
| `/100` | Divide by 100 | `chuangmi.plug.212a01-miot.json` |
| `SecondsToHours` | Divide by 3600 | `zhimi.airfresh.va4.json` |
| `tankLevel` | Water tank value of the humidifier: 127 (no tank) gives -1, otherwise the value divided by 1.2 (120 is 100%) | `zhimi.humidifier.ca4.json` |
| `YeelightSceneId` | Convert the scene number 1 to 6 to `color`, `hsv`, `ct`, `nightlight`, `cf` or `auto_delay_off`, any other to `unknown` | - |
| `bRGBtoHSV` | Convert an integer with brightness in the highest byte and RGB in the lower bytes to HSB | `lumi.gateway.mieu01.json` |
| `addBrightToHSV` | Convert an RGB integer to HSB, using the brightness of the last value read for the property `bright` (100 if not yet known) | - |
| `addBrightToHSVPower` | As `addBrightToHSV`, with brightness 0 when the last value of the property `power` is `off` | `yeelink.light.color1.json` |
| `deviceDataTab` | Convert the response of the cloud request `/v2/user/getuserdevicedatatab` to a list of the `value` entries | `lumi.lock.json` |
| `getJsonElement-<name>` | Take the member `<name>` of a JSON object, or select a part of a JSON structure with a path, for example `getJsonElement-gateway_status` or `getJsonElement-recipes[*].recipeName` (`<name>` is case sensitive, see [Selecting Parts of a JSON Response](#selecting-parts-of-a-json-response)) | `chuangmi.plug.212a01-miot.json` |
| `getDiDElement` | Take the member of a JSON object that has the device id of the thing as name | `chuangmi.plug.212a01-miot.json` |

There is no reverse conversion: a value that is scaled with a transformation when read is not scaled back when written.

#### Selecting Parts of a JSON Response

The text after `getJsonElement-` is either the name of a member, or a path.
The input can be a JSON object, a JSON array, or a string containing JSON (as the cloud and custom command responses have).
When the top level object has a member with exactly this name, that member is returned.
Otherwise the text is evaluated as a path.
If nothing can be selected, or the input is not JSON, the input is returned unchanged.

| Path element | Meaning |
|--------------|---------|
| `a.b` | Member `b` of the object `a`. Dots separate the segments. |
| `a[n]` | Element `n` (starting at 0) of the array `a`. |
| `a[*]` | Apply the rest of the path to every element of the array `a` and return the results as an array. Elements for which the path does not resolve are left out. |
| `[*]`, `[n]` | As above, when the input itself is an array. A segment without a name is only valid with an index. |
| `{x,y}` | Last segment only: return only the listed members of an object, or of every object in an array. Members that do not exist are left out. A listed member can be a path without brackets, for example `{id,cook.time}`, which is returned under the name of its last segment (`time`). |

A string value on the path that contains a JSON object or array is parsed, so its members can be selected too.
A string at the end of the path is returned as is.

For the input `{"recipes":[{"recipeID":1,"recipeName":"Fries","cookCommand":{"time":11}},{"recipeID":2,"recipeName":"Pizza","cookCommand":{"time":15}}],"hasMore":false}`:

| Transformation | Result |
|----------------|--------|
| `getJsonElement-hasMore` | `false` |
| `getJsonElement-recipes[*].recipeName` | `["Fries","Pizza"]` |
| `getJsonElement-recipes[1].recipeName` | `"Pizza"` |
| `getJsonElement-recipes[0].cookCommand` | `{"time":11}` |
| `getJsonElement-recipes[*].{recipeID,recipeName}` | `[{"recipeID":1,"recipeName":"Fries"},{"recipeID":2,"recipeName":"Pizza"}]` |

Within a path, member names cannot contain `.`, `[`, `]`, `{` or `}`; a comma is only a separator inside `{...}`.
A top level member with such a name can still be selected by its exact name.
If a `{...}` selection on an object finds none of the listed members, the input is returned unchanged; a `[*]` selection that finds nothing returns an empty array.
No bundled database file uses the path syntax yet; the examples above are covered by the binding's unit tests.

### Units

The `unit` of a channel is used to create quantities when reading and to convert quantity commands to a plain number when writing.
The name is not case sensitive, the aliases are accepted as well.
A symbol that is not in the table is logged at DEBUG level.
The binding then tries to create the state from the number and the unit as text, which only works when openHAB knows that unit symbol.

| Unit | Aliases |
|------|---------|
| `CELSIUS` | `C`, `celcius` |
| `FAHRENHEIT`, `KELVIN` | `K` for kelvin |
| `PASCAL`, `HPA` | `pa` for pascal |
| `SECOND`, `MINUTE`, `HOUR`, `DAY` | `seconds`, `minutes`, `hours`, `days` |
| `AMPERE`, `MILLI_AMPERE` | `mA` for milli ampere |
| `VOLT`, `MILLI_VOLT` | `mV` for milli volt |
| `WATT` | `W` |
| `KILOWATT_HOUR` | `kwh` |
| `LITRE`, `LITER` | `liter`, `litre`, `L` |
| `LUX` | |
| `RADIANS` | `radians` |
| `DEGREE` (angle) | `degree` |
| `SQUARE_METRE` | `square_meter`, `squaremeter` |
| `PERCENT` | `percentage` |
| `PPM` | `parts_per_million` |
| `KGM3` | `kilogram_per_cubicmeter` |
| `UGM3` | `microgram_per_cubicmeter`, `μg/m3` |
| `M3` | `cubic_meter`, `cubic_metre` |

## Writing Values

### How a Command Is Processed

For every command that is sent to a channel, the binding goes through all `actions` of that channel in order.
For each action:

1. A `QuantityType` command is converted to the `unit` of the channel and continues as plain number.
1. The command is converted to a value, depending on the `parameterType`.
1. The `condition` is applied, it can change the value or reject it.
1. For MIoT channels and actions, the value is wrapped in the MIoT structure, see [MIoT Devices](#miot-devices).
1. The value is placed in the `parameters` and the command is sent as `<command><parameters as JSON list>`, for example `set_ct_abx[3000,"smooth",500]` (a MIoT action is sent as `action{...}`).
   When there is no value (the command does not match the `parameterType` or the condition is not met), the action is skipped.

Three seconds after the command, all channels are refreshed.

### Parameter Types

| `parameterType` | Command received | Value |
|-----------------|------------------|-------|
| `ONOFF` | ON, OFF | `"on"`, `"off"` |
| `ONOFFBOOL` | ON, OFF | `true`, `false` |
| `ONOFFBOOLSTRING` | ON, OFF | `"true"`, `"false"` |
| `ONOFFNUMBER` | ON, OFF | `1`, `0` |
| `ONOFFPARA` | ON, OFF | No value. The `*` in `command` is replaced by `on` or `off` and the parameter list is empty. |
| `NUMBER` | number or percent | The number. |
| `STRING` | text | The text, converted to lower case. |
| `COLOR` | HSB color | The RGB color as integer (`0xRRGGBB`). A number or percent is passed as number (brightness) and ON or OFF as 100 or 0. |
| `EMPTY` | any | The command is ignored, the `parameters` (or the `returnValue` of a matching condition) are the value. For a legacy command the list is sent as one nested element: `command[[...]]`. For MIoT actions it is the `in` list. |
| `NONE` | any | The value is only available for the `condition`, it is not placed in the `parameters`. |
| `UNKNOWN` | any | No value for ON/OFF and text commands. Used by generated placeholder actions that never send. |
| `CUSTOMSTRING` | text | The text, converted to lower case, replaces `$value$` in the `parameters` entry that contains it, for example `"color,$value$"`. Without such an entry it behaves like `STRING`. For a fixed list of values, prefer `STRING` or `NUMBER` with a `matchValue` condition and `returnValue`. |

- A number or percent command always results in the number, whatever the `parameterType`, except for `COLOR` and `EMPTY`.
  Combine this with a condition (see the dimmer example in the cookbook) to handle both on/off and numbers on one channel.
- ON and OFF are only converted by the `ONOFF...` types and `COLOR`.
  Use an `ONOFF...` type for a `Switch`, or `EMPTY` for a switch that only triggers a command (the value is then ignored).
  A `Switch` or `String` channel with `NUMBER` sends nothing, unless a `matchValue` condition with a `returnValue` supplies the value.
- Commands of other types (for example UP, DOWN, STOP) are sent as lower case text.
- `STRING` converts the text to lower case, so define the option values of such channels in lower case.
- For legacy commands use `EMPTY` without `parameters` (sends `[[]]`) or with a single entry.
  A missing `parameterType` is `EMPTY`, which ignores the command value.

### Parameters and `$value$`

- Without `parameters`, the value is the only parameter: `set_power["on"]`.
- With `parameters`, the value replaces the entry that contains `$value$`, for example `[0, "$value$"]` gives `cron_add[0,30]`.
  Put `$value$` explicitly in every action that has `parameters`.
  If no entry of the `parameters` contains `$value$`, the value replaces the first entry.
- `NONE` and `ONOFFPARA` never insert the value, so `parameters` stay as written (including `$variables$`).
  The command is only sent when a value was determined for the command: ON/OFF or a number for `ONOFFPARA`, a number for `NONE` (ON/OFF and text send nothing), or any command when a condition supplies the value.

### Variables

Variables written as `$name$` are replaced in every command text before it is sent: the command, the parameters and the custom refresh commands.
Quotes directly around a variable (`"$name$"`) are removed.
A variable that has no value stays in the command as it is.

| Variable | Value |
|----------|-------|
| `deviceId` | The device id of the thing, inserted as quoted string. |
| `modelId`, `firmwareVersion`, `hardwareVersion`, `wifiFirmware`, `mcuFirmware`, `serialNumber` | The thing properties, inserted as quoted string. Any other property of the thing can be used as well, when it is set. |
| `timestamp` | Time of the last update or command in seconds since 1970, inserted as number. |
| property name | The last value read for a property, before any transformation, inserted as plain text without quotes, so only useful for numbers and booleans. For a channel with `customRefreshCommand` the name is the channel id. |

### Conditions

| Name | Effect |
|------|--------|
| `matchValue` | Sends the action only when the command matches one of the entries in `parameters`. Each entry has a `matchValue` (a regular expression that must match the complete command text, case sensitive, for example `ON`, `1` or `sunrise`) and optionally a `returnValue` (any JSON value) that replaces the value. The first matching entry wins. |
| `BrightnessExisting` | Passes a number from 1 to 100 (a larger number becomes 100) and rejects 0 and negative numbers. Other values pass unchanged. |
| `BrightnessOnOff` | Changes a number to `"off"` (lower than 1) or `"on"` (1 or more). Other values pass unchanged. |
| `HSBOnly` | Passes the value only if the command is an HSB color. |
| `HSVTOBRGB` | Passes for an HSB color command the integer with brightness in the highest byte and RGB in the lower bytes. Rejects other commands. |

## MIoT Devices

MIoT devices describe their properties and actions by numbers.
The channels and actions use the same fields, with the additional `siid`/`piid` (property) or `siid`/`aiid` (action).

### Reading

- Use `"propertyMethod": "get_properties"`.
  The generated files use `"maxProperties": 1`.
  A higher value reduces the number of requests, but if the answer does not have the same number of entries as the request, all values are lost.
- The binding requests `{"did":"<property>","siid":<siid>,"piid":<piid>}` and takes the `value` from each answer entry.
  The `did` is the `property` of the channel, which is used to match the answer to the channel.
  The `property` must be unique and not empty, but can be chosen freely (the generated files use the name of the MIoT spec).

### Writing a Property

An action with `"command": "set_properties"` on a MIoT channel sends `set_properties[{"did":"<channel>","siid":..,"piid":..,"value":<value>}]`.
Do not specify `parameters` for it.
Use `ONOFFBOOL` for booleans, `NUMBER` for numbers and enumerations (use `options` for the enumeration values) and `STRING` for text (which is converted to lower case).

```json
{
  "property": "fan-level",
  "siid": 2,
  "piid": 2,
  "friendlyName": "Fan-Fan Level",
  "channel": "FanLevel",
  "type": "Number",
  "stateDescription": {
    "options": [
      {"value": "1", "label": "Level1"},
      {"value": "2", "label": "Level2"},
      {"value": "3", "label": "Level3"}
    ]
  },
  "refresh": true,
  "actions": [{"command": "set_properties", "parameterType": "NUMBER"}]
}
```

### Actions

A MIoT action is sent as `action{"did":"<channel>","siid":..,"aiid":..,"in":[..]}`.
The usual pattern is a `String` channel with `options`, and one action per option that is selected with a `matchValue` condition.
The channel itself has no `siid`/`piid`, an empty `property` and no `refresh`.

```json
{
  "property": "",
  "friendlyName": "Actions",
  "channel": "actions",
  "type": "String",
  "stateDescription": {"options": [{"value": "fan-toggle", "label": "Fan Toggle"}]},
  "actions": [
    {
      "command": "action",
      "parameterType": "EMPTY",
      "siid": 2,
      "aiid": 1,
      "condition": {"name": "matchValue", "parameters": [{"matchValue": "fan-toggle"}]}
    }
  ]
}
```

- The `in` list is the `parameters` of an `EMPTY` action, or the `returnValue` of the matching condition.
  Without any of those it is an empty list.
- Actions that need input must be given a fixed list of `{"piid":..,"value":..}` entries as in the example below.
  Inserting the value of the channel in such a list (`$value$` inside the objects) is currently not supported.

```json
{
  "property": "",
  "friendlyName": "Vacuum Action",
  "channel": "vacuumaction",
  "type": "String",
  "stateDescription": {"options": [{"value": "vacuum", "label": "Vacuum"}, {"value": "stop", "label": "Stop"}]},
  "refresh": false,
  "actions": [
    {
      "command": "action",
      "parameterType": "EMPTY",
      "siid": 18,
      "aiid": 1,
      "condition": {
        "name": "matchValue",
        "parameters": [
          {"matchValue": "vacuum", "returnValue": [{"piid": 1, "value": 2}]},
          {"matchValue": "start", "returnValue": [{"piid": 1, "value": 2}]}
        ]
      }
    },
    {
      "command": "action",
      "parameterType": "EMPTY",
      "siid": 18,
      "aiid": 2,
      "condition": {"name": "matchValue", "parameters": [{"matchValue": "stop"}]}
    }
  ]
}
```

### Creating a File for a MIoT Device

- The experimental channel `Create channels for new/unsupported devices (MIOT protocol)` of an unsupported thing reads the published spec of the model, creates the file `<model>-miot-experimental.json` in `conf/misc/miio` and tests all readable properties.
  The result of the test is saved as `test-<model>-<timestamp>.txt` in the `userdata/miio` folder.
- A developer can create the same file with the tool class `MiotJsonFileCreator` in `src/test/java`.
  It needs internet access and compiled test classes, run it from the binding folder with `mvn test-compile exec:java -Dexec.mainClass="org.openhab.binding.miio.internal.MiotJsonFileCreator" -Dexec.classpathScope="test" -Dexec.args="<model>"`.
  It writes `src/main/resources/database/<model>-miot.json`.
  An existing file is never overwritten, a counter is added to the name instead (`<model>-0-miot.json`).
- The generated file needs a review:
  - The generated names and option labels come from the (often machine translated) spec, correct them.
  - Check the `type` against the spec, for example an on/off property is a `Switch`.
  - Remove channels that have no useful data and fix the `unit` and `stateDescription`.
  - Do not change `siid`, `piid`, `aiid` and the `value` of the options, they identify the property or action on the device.
  - Actions with `"parameterType": "UNKNOWN"` are placeholders of the generator, with the input list of the spec as `parameters`.
    They are never sent, replace them with a working definition as shown above or remove them.
  - The generator sets `"experimental": true`.

## Channel Presentation

- The channel label is the `friendlyName`.
  The labels of the bundled files can be translated, the keys `ch.<file name without .json>.<channel>` are generated in `OH-INF/i18n/basic.properties`.
  Local files have no translations, their labels are the `friendlyName`.
- Use quantity types (`Number:Temperature`, `Number:Time`, ...) with a `unit` for physical values.
  Use `%unit%` in the `pattern` of such channels.
- `category` is the icon name, `tags` are the semantic tags.
  A tag must exist in the openHAB semantic model and is case sensitive.
  Use at most one point tag (for example `Switch`, `Control`, `Setpoint`, `Measurement`, `Status`) and one property tag (for example `Light`, `Temperature`, `Duration`).
- All of the above is part of the generated channel type.
  When `channelType` is used, only the `tags` are taken from the file.

```json
{
  "property": "battery",
  "friendlyName": "Battery",
  "channel": "battery",
  "channelType": "system:battery-level",
  "type": "Number",
  "refresh": true,
  "actions": []
}
```

## Cookbook

Each snippet is one element of the `channels` list in `deviceMapping`.
Use them as inspiration and replace the names and numbers with those of your device.

### On/Off Command Words

`ONOFF` sends `on` or `off` as parameter: `set_power["on"]`.
Some devices have a separate command for on and off.
With `ONOFFPARA` the `*` in the command is replaced and no parameter is sent: `set_on[]` and `set_off[]`.

```json
{
  "property": "on",
  "friendlyName": "Power",
  "channel": "power",
  "type": "Switch",
  "refresh": true,
  "actions": [{"command": "set_*", "parameterType": "ONOFFPARA"}]
}
```

### Read Only Value with Scaling, Unit and Tags

The device reports the temperature in tenths of a degree.

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

A command can have fixed parameters around the value.
A quantity command is converted to the channel `unit` first: `2 h` or `120 min` both result in `cron_add[0,120]`.

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
  "category": "time",
  "tags": ["Setpoint", "Duration"]
}
```

### Selection List

A `String` channel with `options`, sent as lower case text: `set_mode["auto"]`.

```json
{
  "property": "mode",
  "friendlyName": "Mode",
  "channel": "mode",
  "type": "String",
  "stateDescription": {
    "options": [
      {"value": "auto", "label": "Auto"},
      {"value": "favorite", "label": "Favorite"},
      {"value": "silent", "label": "Silent"}
    ]
  },
  "refresh": true,
  "actions": [{"command": "set_mode", "parameterType": "STRING"}],
  "tags": ["Control"]
}
```

### Dimmer that Also Handles On/Off

A dimmer has two actions.
`BrightnessExisting` lets only a brightness from 1 to 100 pass to the brightness command.
`BrightnessOnOff` turns a brightness of 0 or the command OFF into `off` and any other brightness or the command ON into `on` for the power command.

```json
{
  "property": "bright",
  "friendlyName": "Brightness",
  "channel": "brightness",
  "type": "Dimmer",
  "refresh": true,
  "actions": [
    {
      "command": "set_bright",
      "parameterType": "NUMBER",
      "condition": {"name": "BrightnessExisting"}
    },
    {"command": "set_power", "parameterType": "ONOFF", "condition": {"name": "BrightnessOnOff"}}
  ]
}
```

### Color

A color channel with a transformation for reading.
An HSB command is also a number, so all three actions receive it: the `HSBOnly` condition lets only an HSB color pass to the color command, while the brightness and power actions receive the brightness of the color.

```json
{
  "property": "rgb",
  "friendlyName": "RGB Color",
  "channel": "rgbColor",
  "type": "Color",
  "refresh": true,
  "transformation": "addBrightToHSVPower",
  "actions": [
    {
      "command": "set_rgb",
      "parameterType": "COLOR",
      "parameters": ["$value$", "smooth", 500],
      "condition": {"name": "HSBOnly"}
    },
    {
      "command": "set_bright",
      "parameterType": "NUMBER",
      "condition": {"name": "BrightnessExisting"}
    },
    {"command": "set_power", "parameterType": "ONOFF", "condition": {"name": "BrightnessOnOff"}}
  ]
}
```

The condition `HSVTOBRGB` (see `lumi.gateway.mieu01.json`) is used for devices that want brightness and color in one number.

### A Fixed Parameter per Option

With `matchValue` and `returnValue` the value that is sent depends on the command.
The example is shortened to one of the many options.
The `colorflow` channel of the same file uses it on a `Switch`: ON sends `start_cf` with a flow definition, OFF sends `stop_cf`.

```json
{
  "property": "",
  "friendlyName": "Color Flow Scene",
  "channel": "colorflowScene",
  "type": "String",
  "stateDescription": {"options": [{"value": "sunrise", "label": "Sunrise"}]},
  "refresh": false,
  "actions": [
    {
      "command": "start_cf",
      "parameterType": "STRING",
      "parameters": [3, 1, "$value$"],
      "condition": {
        "name": "matchValue",
        "parameters": [
          {
            "matchValue": "sunrise",
            "returnValue": "50,1,16731392,1,360000,2,1700,10,540000,2,2700,100"
          }
        ]
      }
    }
  ]
}
```

### Command Without Value and Variables from Other Properties

The value is not sent (`NONE`), the parameters contain the last known values of the properties `rgb` and `ct`.
The condition makes sure that only the action for the selected mode is sent.

```json
{
  "property": "color_mode",
  "friendlyName": "Color Mode",
  "channel": "colorMode",
  "type": "Number",
  "stateDescription": {
    "minimum": 0,
    "maximum": 5,
    "step": 1,
    "options": [{"value": "0", "label": "Default"}, {"value": "2", "label": "CT mode"}]
  },
  "refresh": true,
  "actions": [
    {
      "command": "set_rgb",
      "parameterType": "NONE",
      "parameters": ["$rgb$", "smooth", 500],
      "condition": {"name": "matchValue", "parameters": [{"matchValue": "1"}]}
    },
    {
      "command": "set_ct_abx",
      "parameterType": "NONE",
      "parameters": ["$ct$", "smooth", 500],
      "condition": {"name": "matchValue", "parameters": [{"matchValue": "2"}]}
    }
  ]
}
```

### Custom Refresh Command

A property that is read with its own command.

```json
{
  "property": "get_arming_time",
  "friendlyName": "Arming Time",
  "channel": "arming_time",
  "type": "Number:Time",
  "unit": "seconds",
  "refresh": true,
  "customRefreshCommand": "get_arming_time",
  "actions": [{"command": "set_alarming_time", "parameterType": "NUMBER"}],
  "category": "time",
  "tags": ["Setpoint", "Duration"]
}
```

### Cloud Data with Variables and Transformation

A command starting with `/` is sent to the Xiaomi cloud, and the variables `$timestamp$` and `$deviceId$` are replaced.
The transformation `deviceDataTab` converts the response to a list of values.

```json
{
  "property": "log",
  "friendlyName": "Device Log",
  "channel": "log",
  "type": "String",
  "refresh": true,
  "customRefreshCommand": "/v2/user/getuserdevicedatatab",
  "customRefreshParameters": {
    "limit": 10,
    "timestamp": "$timestamp$",
    "did": "$deviceId$",
    "type": "prop",
    "key": "device_log"
  },
  "transformation": "deviceDataTab",
  "actions": [],
  "category": "setting",
  "tags": ["Point"],
  "readmeComment": "This channel uses cloud to get data. See widget market place for suitable widget to display the data"
}
```

### Cloud Data Extracted from a JSON Response, Read Less Often

The transformation `getDiDElement` takes the part of the cloud response that belongs to this device.
The channel is only read every second polling cycle.

```json
{
  "property": "",
  "friendlyName": "Connected BT Gateway Devices",
  "channel": "bt-gw-devices",
  "type": "String",
  "stateDescription": {"readOnly": true},
  "refresh": true,
  "refreshInterval": 2,
  "customRefreshCommand": "/device/get_bledevice_by_gateway",
  "customRefreshParameters": {"dids": ["$deviceId$"]},
  "transformation": "getDiDElement",
  "actions": [],
  "category": "bluetooth",
  "readmeComment": "Note, refreshes every 2nd refresh. Channel requires cloud connectivity to function. Sample widget to visualise the (json) output available from the widget market"
}
```

### Devices Behind the Xiaomi Gateway

Sub devices of the gateway are `miio:lumi` things.
They use the property method `get_device_prop_exp`, and the file mentions that a gateway bridge is required.
The commands of `lumi.plug.mmeu01.json` show that a command can also be a JSON object as text, with variables.

```json
{
  "deviceMapping": {
    "id": ["lumi.light.aqcn02", "ikea.light.led1545g12"],
    "propertyMethod": "get_device_prop_exp",
    "maxProperties": 3,
    "readmeComment": "Needs to have the Xiaomi gateway configured in the binding as bridge.",
    "experimental": true,
    "channels": [
      {
        "property": "power_status",
        "friendlyName": "Power",
        "channel": "power",
        "type": "Switch",
        "refresh": true,
        "actions": [],
        "category": "switch",
        "tags": ["Switch"]
      }
    ]
  }
}
```

## Testing Your File

1. Put the file in the `conf/misc/miio` folder (create it when it does not exist) and restart the binding, see [Reloading](#reloading).
   Check that the JSON is valid, for example with an editor or an online JSON validator.
1. Enable DEBUG logging for the binding: `log:set DEBUG org.openhab.binding.miio`.
   At startup the log shows the folder in the line `Started miio basic devices local databases watch service. Watching for database files at path: ...`.
1. Create a `miio:basic` thing for the device, with the model of your file in the configuration (an unsupported thing of a device that has a file switches to `miio:basic` by itself).
   The log shows `Using device database: <file> for device <model>` when the file is used.
   Link items to the channels, only linked channels are read.
1. Watch the log for the commands and answers:
   - `Sending command ...` shows the command that is sent for a channel command.
   - `Command added to Queue ...` shows every command that is sent to the device, including the polling requests.
   - `Received response for device ...` shows the answers.
   - `Property '...' returned null (is it supported?)` means the device does not know the property.
   - `Skip refresh of channel ... as it is not linked` means that no item is linked to the channel.
   - `Channel not found for ...` means that the answer has a name that no channel has as `property`.
   - `Conditional command ... not send, condition '...' not met` or `Command not send. Value null` mean that the command did not match the `parameterType` or `condition`.
1. Raw commands can be tried with the (advanced) `actions#commands` channel of the thing, for example `get_prop["power"]` or `action{"did":"x","siid":2,"aiid":1,"in":[]}`.
1. After changing the file, disable and enable the thing to rebuild the channels.

## Pitfalls and Submission Checklist

Common mistakes:

- A misspelled key, or a key with the wrong case, is silently ignored.
- A channel without `type` or `channel` is skipped.
- `refresh` is `false` when omitted, a channel without `refresh` is never read.
- The `property` of two polled channels must differ, a channel id may only be used once and the `customRefreshCommand` texts must be unique.
- A `String` channel needs `STRING` or `EMPTY` (or a `matchValue` with `returnValue`) and a `Switch` an `ONOFF...` type or `EMPTY`, otherwise nothing is sent.
- `STRING` converts to lower case but `matchValue` compares the command as it was received, case sensitive.
- `stateDescription` and `category` are ignored when `channelType` is used.
- `readOnly` only influences the UI, use an empty `actions` list for channels that cannot be written.
- The experimental file `<model>[-miot]-experimental.json` may still be in `conf/misc/miio`.
  Edit that file (or delete it) instead of adding a second local file with the same model id.

Checklist for a pull request:

1. The file is in `src/main/resources/database/`, is valid JSON and uses the naming convention of [Location and Names](#location-and-names).
1. The channels return data on a real device, or `"experimental": true` is set.
   Read only channels have `"actions": []`.
1. `friendlyName` and option labels are clear English, `type`, `unit` and `stateDescription` are consistent, `tags` are valid semantic tags.
1. Every model id is added in alphabetical order to `MiIoDevices.java` with the product name and the thing type: `THING_TYPE_BASIC`, `THING_TYPE_LUMI` for sub devices of the Xiaomi gateway and `THING_TYPE_GATEWAY` for the gateways themselves.
1. The README device list, the channel list and the default translation file `OH-INF/i18n/basic.properties` are generated from the database files with the `ReadmeHelper` class: `mvn test-compile exec:java -Dexec.mainClass="org.openhab.binding.miio.internal.ReadmeHelper" -Dexec.classpathScope="test"` from the binding folder.
   It also prints the suggested `MiIoDevices.java` lines (the product name comes from `src/main/resources/misc/device_names.json`, or is the model id when unknown) and a text for the change log.
   Change `README.base.md` or the database files instead of the generated `README.md`.
   Translations are managed externally, do not edit the language specific `basic_xx.properties` files.
1. The binding builds with `mvn clean install -pl :org.openhab.binding.miio`, and `mvn spotless:apply` has been run.
1. The pull request follows `CONTRIBUTING.md` and the pull request template:
   - Title `[miio] Add support for <device>`, and the model ids in the commit message, for example `Adding support for the following models:` followed by `* <product name> (modelId: <model>)`.
   - Commits are signed off (`git commit -s`), use `git rebase` and no merge commits.
   - Reference the issue with `Fixes #<issue>`, and link the forum thread if there is one.

## Example Files

Complete real files that show the format:

- `chuangmi.plug.m1.json`: small legacy device with switch, temperature and indicator light.
- `yeelink.light.color1.json`: legacy light with dimmer, color, conditions, `matchValue` and variables.
- `lumi.gateway.mieu01.json`: gateway with custom refresh commands.
- `dmaker.fan.p8-miot.json`: MIoT device with properties, options and an action channel.
- `dreame.vacuum.mc1808-miot.json`: MIoT actions with fixed input.
