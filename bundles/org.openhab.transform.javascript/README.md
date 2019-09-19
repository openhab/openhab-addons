# JavaScript Transformation Service

Transform an input to an output using JavaScript. 

It expects the transformation rule to be read from a file which is stored under the `transform` folder. 
To organize the various transformations, one should use subfolders.

## Example

Let's assume we have received a string containing `foo bar baz` and we're looking for a length of the last word (`baz`).

transform/getValue.js:

```
(function(i) {
    var array = i.split(" ");
    return array[array.length - 1].length;
})(input)
```

## Usage as a Profile

The functionality of this `TransformationService` can be used in a `Profile` on an `ItemChannelLink` too.
To do so, it can be configured in the `.items` file as follows:

```java
String <itemName> { channel="<channelUID>"[profile="transform:JS", function="<filename>", sourceFormat="<valueFormat>"]}
```

The Javascript file (from within the `transform` folder) to be used has to be set in the `function` parameter.
The parameter `sourceFormat` is optional and can be used to format the input value **before** the transformation, i.e. `%.3f`.
If omitted the default is `%s`, so the input value will be put into the transformation without any format changes.

Please note: This profile is a one-way transformation, i.e. only values from a device towards the item are changed, the other direction is left untouched.
