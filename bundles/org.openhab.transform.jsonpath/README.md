# JsonPath Transformation Service

Extracts values from a JSON string using a [JsonPath](https://github.com/jayway/JsonPath#jayway-jsonpath) expression.

Given the following JSON string:

```json
[{ "device": { "location": "Outside", "status": { "temperature": 23.2 }}}]
```

The expression `$.device.location` extracts the string `Outside`.
The JsonPath expression `$.device.status.temperature` extracts the string `23.2`.

## Examples

### Items

```java
String  Temperature_json "Temperature [JSONPATH($.device.status.temperature):%s °C]" {...}
Number  Temperature "Temperature [%.1f °C]"
```

### Rules

```java
rule "Convert JSON to Item Type Number"
when
    Item Temperature_json changed
then
    // use the transformation service to retrieve the value
    val newValue = transform("JSONPATH", "$.device.status.temperature", Temperature_json.state.toString)

    // post the new value to the Number Item
    Temperature.postUpdate( newValue )
end
```

Now the resulting Number can also be used in the label to [change the color](https://docs.openhab.org/configuration/sitemaps.html#label-and-value-colors) or in a rule as a value to compare.

## Differences to standard JsonPath

Compared to standard JsonPath, the transformation returns single values instead of arrays.
The transformation also will not ever return `null`.
This makes it possible to use the transform in labels or output channels of Things.

If the JsonPath expression provided results in no matches, the transformation will return the entire original JSON string.

## Usage as a Profile

The transformation can be used in a `Profile` on an `ItemChannelLink` too.

One example for configuring it in the `.items` file:

```java
String <itemName> { channel="<channelUID>"[profile="transform:JSONPATH", function="<jsonPath>", sourceFormat="<valueFormat>"]}
```

The JsonPath expression to be used has to be set in the `function` parameter.
The parameter `sourceFormat` is optional and can be used to format the input value **before** the transformation, i.e. `%.3f`.
If omitted, the default is `%s`, so the input value will be returned from the transformation without any format changes.

This profile is a one-way transformation; only values from a device toward the item are changed.

## Further Reading

- An extended [introduction](https://www.w3schools.com/js/js_json_intro.asp) can be found at W3School.
- As JsonPath transformation is based on [Jayway](https://github.com/json-path/JsonPath), using an [online validator](https://jsonpath.fly.dev/) which also uses Jayway will give the most similar results.
