# Ephemeris Binding

The Ephemeris Binding makes the bridge with Ephemeris core actions.
It provides access to Ephemeris data via Items without requiring usage of a scripting language.

The binding will auto create a folder in openhab configuration folder where it expects to find your Jollyday event definition files. Eg. for a linux system : /etc/openhab/misc/ephemeris/

## Supported Things

The binding handles the following Things:

* default holiday data (`holiday`)
* personal holiday data file (`file`)
* daysets (`dayset`)
* weekend (`weekend`)

## Discovery

The binding discovers `weekend` and `holiday` things.

## Binding Configuration

There is no configuration at binding level.

## Thing Configuration


### `file` Thing Configuration

| Name            | Type    | Description                                   | Default | Required | Advanced |
|-----------------|---------|-----------------------------------------------|---------|----------|----------|
| fileName        | text    | Name of the XML file in the ephemeris folder  | N/A     | yes      | no       |

The file has to use the syntax described here : https://www.openhab.org/docs/configuration/actions.html#custom-bank-holidays

### `dayset` Thing Configuration

| Name            | Type    | Description               | Default | Required | Advanced |
|-----------------|---------|---------------------------|---------|----------|----------|
| name            | text    | Name of the dayset used   | N/A     | yes      | no       |



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
