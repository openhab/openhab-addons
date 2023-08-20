# RegEx Transformation Service

Transforms a source string on basis of the regular expression (regex) search pattern to a defined result string.

The simplest regex is in the form `<regex>` and transforms the input string on basis of the regex pattern to a result string.
A full regex is in the form `s/<regex>/<substitution>/g` whereat the delimiter `s` and the regex flag `g` have a special meaning.

The regular expression in the format `s/<regex>/result/g`, replaces all occurrences of `<regex>` in the source string with `result`.
The regular expression in the format `s/<regex>/result/` (without `g`), replaces the first occurrence of `<regex>` in the source string with `result`.

If the regular expression contains a [capture group](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/regex/Pattern.html#cg) defined by `()`, it returns the captured string.
Multiple capture groups can be used to retrieve multiple strings and can be combined as a result string defined in the `substitution`.

The transformation can be set to be restricted to only match if the input string begins with a character by prepending `^` to the beginning of a pattern or to only match if the input string ends with a specified character by appending `$` at the end.
So the regex `^I.*b$` only matches when the input string starts with `I` and ends with `b`, like in `I'm Bob`. Both can be used alone or in combination.

The special characters `\.[]{}()*+-?^$|` have to be escaped when they should be used as literal characters.

## Examples

### Basic Examples

|         Input String        |    Regular Expression    |         Output String        | Explanation              |
|---------------------------|------------------------|----------------------------|--------------------------|
| `My network does not work.` | `s/work/cast/g` | `"My netcast does not cast."` | Replaces all matches of the string "work" with the string "cast". |
| `My network does not work.` | `.*(\snot).*` | `" not"` | Returns only the first match and strips of the rest, "\s" defines a  whitespace. |
| `temp=44.0'C` | `temp=(.*?)'C)`          | `44.0` | Matches whole string and returns the content of the captcha group `(.?)`. |
| `48312` | `s/(.{2})(.{3})/$1.$2/g` | `48.312` | Captures 2 and 3 character, returns first capture group adds a dot and the second capture group. This divides by 1000. |

### Example In Setup

**Input String**

```shell
temp=44.0'C
```

the regex transformation can be used to extract the value to display it on the label.

**.items**

```csv
String  Temperature_str "Temperature [REGEX(.*=(\\d*.\\d*).*):%s °C]" {...}
Number  Temperature "Temperature [%.1f °C]"
```

The regex pattern is is defined as follows

* `.*` match any character, zero and unlimited times
* `=` match the equal sign literally, used to find the position
*  `()` capture group match 
    * `\d*` match a digit (equal to [0-9]), zero and unlimited times, the backslash has to be escaped see [string vs plain](#Differences-to-plain-Regex)
    * `.` match the dot literally
    * `\w*` match a word character (equal to [a-zA-Z_0-9]), zero and unlimited times, the backslash has to be escaped see [string vs plain](#Differences-to-plain-Regex)
* `.*` match any character, zero and unlimited times

The result will be `44.0` and displayed on the label as `Temperature 44.0°C`.
A better solution would be to use the regex on the result from the binding either in a rule or when the binding allows it on the output channel. 
Thus the value `44.0` would be saved as a number.

**.rules**

```php
rule "Convert String to Item Number"
  when
    Item Temperature_str changed
 then
    // use the transformation service to retrieve the value
    val newValue = transform("REGEX", ".*=(\\d*.\\d*).*", Temperature_str.state.toString)

    // post the new value to the Number Item
    Temperature.postUpdate( newValue )
 end
```

Now the resulting Number can also be used in the label to [change the color](https://docs.openhab.org/configuration/sitemaps.html#label-and-value-colors) or in a rule as value for comparison.

## Differences to plain Regex

The regex is embedded in a string so when double quotes `"` are used in a regex they need to be escaped `\"` to keep the string intact.
As the escape character of strings is the backslash this has to be escaped additionally.
To use a dot as literal in the regex it has to be escape `\.`, but in a string it has to be escaped twice `"\\."`.
The first backslash escapes the second backslash in the string so it can be used in the regex.
Using a backslash in a Regex as literal `\\` will have this form `"\\\\"`.

## Usage as a Profile

The functionality of this `TransformationService` can be used in a `Profile` on an `ItemChannelLink` too.
To do so, it can be configured in the `.items` file as follows:

```java
String <itemName> { channel="<channelUID>"[profile="transform:REGEX", function="<regex>", sourceFormat="<valueFormat>"]}
```

The regular expression to be executed has to be set in the `function` parameter.
The parameter `sourceFormat` is optional and can be used to format the input value **before** the transformation, i.e. `%.3f`.
If omitted the default is `%s`, so the input value will be put into the transformation without any format changes.

Please note: This profile is a one-way transformation, i.e. only values from a device towards the item are changed, the other direction is left untouched.

## Further Reading

* A full [introduction](https://www.w3schools.com/jsref/jsref_obj_regexp.asp) for regular expression is available at W3School.
* Online validator help to check the syntax of a regex and give information how to design it.
    * [Regex 101](https://regex101.com/)
    * [Regex R](https://regexr.com/)
    * [ExtendsClass](https://extendsclass.com/regex-tester.html)
