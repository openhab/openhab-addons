# Map Transformation Service

Transforms the input by mapping it to another string.

## Map Syntax

The mapping is performed based on "key=value" pairs.
When the input matches a `key` in the mapping table, the corresponding `value` is given as the output of the transformation.

The format of the mapping table is documented in the [Java Properties.load() documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Properties.html#load(java.io.Reader)).

A default value can be provided if no matching entry is found by using "=value" syntax.
Defining this default value using `_source_` would then return the non transformed input string.

## File-based Map

The mapping table can be stored in a file under the `transform` folder.
The file name must have the `.map` extension.

To organize the various transformations one might use subfolders.

## Inline Map

Instead of providing the file name from which to load, the mapping table can be specified inline by prefixing it with the pipe character `|` .

The inline map entries are delimited with semicolons (`;`) by default.
For example, the following map function translates open/closed to ON/OFF: `|open=ON; closed=OFF`

The delimiters can be changed by adding `?delimiter=` immediately after the pipe character `|`.
Some examples of changing to different delimiters:

- `|?delimiter=,online=ON,offline=OFF`
- `|?delimiter=|online=ON|offline=OFF`
- `|?delimiter=##online=ON##offline=OFF`

To use `?delimiter` as an actual map key, do not place it at the beginning of the map.

## Example

transform/binary.map:

```properties
key=value
1=ON
0=OFF
ON=1
OFF=0
white\ space=using escape
=default
```

| input         | output         |
|---------------|----------------|
| `1`           | `ON`           |
| `OFF`         | `0`            |
| `key`         | `value`        |
| `white space` | `using escape` |
| `anything`    | `default`      |

## Usage as a Profile

The functionality of this `TransformationService` can be used in a `Profile` on an `ItemChannelLink` too.
To do so, it can be configured in the `.items` file as follows:

```java
String <itemName> { channel="<channelUID>" [profile="transform:MAP", function="<filename>", sourceFormat="<valueFormat>" ] }
```

The mapping filename (within the `transform` folder) has to be set in the `function` parameter.
The parameter `sourceFormat` is optional and can be used to format the input value **before** the transformation, i.e. `%.3f`.
If omitted the default is `%s`, so the input value will be put into the transformation without any format changes.

Please note: This profile is a one-way transformation, i.e. only values from a device towards the item are changed, the other direction is left untouched.

To use an inline map in the profile:

```java
String <itemName> { channel="<channelUID>" [ profile="transform:MAP", function="|open=ON;closed=OFF" ] }
```
