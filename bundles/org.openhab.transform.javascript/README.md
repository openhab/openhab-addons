# JavaScript Transformation Service

Transform an input to an output using JavaScript. 

It expects the transformation rule to be read from a file which is stored under the `transform` folder. 
To organize the various transformations, one should use subfolders.

Simple transformation rules can also be given as a inline script.
Inline script should be start by `|` character following the JavaScript.
Beware that complex inline script could cause issues to e.g. item file parsing.

## Examples

Let's assume we have received a string containing `foo bar baz` and we're looking for a length of the last word (`baz`).

transform/getValue.js:

```
(function(i) {
    var array = i.split(" ");
    return array[array.length - 1].length;
})(input)
```

JavaScript transformation syntax also support additional parameters which can be passed to the script. 
This can prevent redundancy when transformation is needed for several use cases, but with small adaptations.
additional parameters can be passed to the script via [URI](https://en.wikipedia.org/wiki/Uniform_Resource_Identifier) query syntax.

As `input` name is reserved for transformed data, it can't be used in query parameters. 
Also `?` and `&` characters are reserved, but if they need to passed as additional data, they can be escaped according to URI syntax.


transform/scale.js:
```
(function(data, cf, d) {
    return parseFloat(data) * parseFloat(cf) / parseFloat(d);
})(input, correctionFactor, divider)
```

`transform/scale.js?correctionFactor=1.1&divider=10`

Following example will return value `23.54` when `input` data is `214`.

### Inline script example:

Normally JavaScript transformation is given by filename, e.g. `JS(transform/getValue.js)`.
Inline script can be given by `|` character following the JavaScript, e.g. `JS(| input / 10)`.
   
## Test JavaScript

You can use online JavaScript testers to validate your script.
E.g. https://www.webtoolkitonline.com/javascript-tester.html

`Input` variable need to be replaced by the test string, e.g. earlier test string `foo bar baz`

```
(function(i) {
    var array = i.split(" ");
    return array[array.length - 1].length;
})("foo bar baz")
```

When you press execute button, tester will show the result returned by the script or error if script contains any.

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
