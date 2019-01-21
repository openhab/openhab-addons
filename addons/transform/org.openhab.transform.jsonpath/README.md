# JsonPath Transformation Service

Transforms a JSON structure on basis of the [JsonPath](https://github.com/jayway/JsonPath#jayway-jsonpath) expression to an JSON containing the requested data.

## Examples

### Basic Example

Given the JSON

```
[{ "device": { "location": "Outside", "status": { "temperature": 23.2 }}}]
```

the JsonPath expression `$.device.location` exstracts the string instead a valid JSON `[ "Outside" ]`, see [differences](#differences-to-standard-jsonpath).

```
Outside
```

the JsonPath expression `$.device.status.temperature` exstracts the number instead a valid JSON `[ 23.2 ]`, see [differences](#differences-to-standard-jsonpath).

```
23.2
```

### In Setup

**Item**

```csv
String  Temperature_json "Temperature [JSONPATH($.device.status.temperature):%s °C]" {...}
Number  Temperature "Temperature [%.1f °C]"
```

**Rule**

```php
rule "Convert JSON to Item Type Number"
  when
    Item Temperature_json changed
 then
    // use the transformation service to retrieve the value
    val newValue = transform("JSONPATH", ".$.device.status.temperature", Temperature_json.state.toString)

    // post the new value to the Number Item
    Temperature.postUpdate( newValue )
 end
```

Now the resulting Number can also be used in the label to [change the color](https://docs.openhab.org/configuration/sitemaps.html#label-and-value-colors) or in a rule as value to compare.

## Differences to standard JsonPath

Compared to standard JSON the transformation it returns evaluated values when a single alement is retrieved from the query.
Means it does not return a valid JSON `[ 23.2 ]` but `23.2`, `[ "Outside" ]` but `Outside`.
This makes it possible to use it in labels or output channel of things and get Numbers or Strings instead of JSON arrays.
A query which returns multiple elements as list is not supported.

## Usage as a Profile

The functionality of this `TransformationService` can be used in a `Profile` on an `ItemChannelLink` too.
To do so, it can be configured in the `.items` file as follows:

```java
String <itemName> { channel="<channelUID>"[profile="transform:JSONPATH", function="<jsonPath>", sourceFormat="<valueFormat>"]}
```

The JSONPath expression to be used has to be set in the `function` parameter.
The parameter `sourceFormat` is optional and can be used to format the input value **before** the transformation, i.e. `%.3f`.
If omitted the default is `%s`, so the input value will be put into the transformation without any format changes.

Please note: This profile is a one-way transformation, i.e. only values from a device towards the item are changed, the other direction is left untouched.

## Further Reading

* An extended [introduction](https://www.w3schools.com/js/js_json_intro.asp) can be found at W3School.
* As JsonPath transformation is based on [Jayway](https://github.com/json-path/JsonPath) using a [online validator](https://jsonpath.herokuapp.com/) which also uses Jaway will give most similar results. 
