# Regex Transformation Service

Transforms a source string based on a regular expression (regex) search pattern into a defined result string.

The simplest regex is of the form `<regex>` and transforms the input string based on the regex pattern to a result string.
A full regex is of the form `s/<regex>/<substitution>/g`, where `s` denotes substitution and the flag `g` applies it globally.

The regular expression in the format `s/<regex>/result/g` replaces all occurrences of `<regex>` in the source string with `result`.
The regular expression in the format `s/<regex>/result/` (without `g`) replaces only the first occurrence of `<regex>` in the source string with `result`.

If the regular expression contains a [capture group](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/Pattern.html#cg) defined by `()`, it returns the captured string.
Multiple capture groups can be used to retrieve multiple strings and can be combined in the `substitution`.

Use anchors to restrict where a match can occur: prepend `^` to anchor at the start of the input and append `$` to anchor at the end.
So the regex `^I.*b$` only matches when the input string starts with `I` and ends with `b`, like in `I'm Bob`. Both anchors can be used alone or in combination.

The special characters `\.[]{}()*+-?^$|` have to be escaped when they should be used as literal characters.

## Examples

### Basic Examples

| Input String                | Regular Expression       | Output String               | Explanation                                                                                                    |
|-----------------------------|--------------------------|-----------------------------|----------------------------------------------------------------------------------------------------------------|
| `My network does not work.` | `s/work/cast/g`          | `My netcast does not cast.` | Replaces all matches of the string "work" with the string "cast".                                              |
| `My network does not work.` | `.*(\snot).*`            | not (leading space)         | Returns the first capture group (" not"); `\s` matches a whitespace.                                           |
| `temp=44.0'C`               | `temp=(.*?)'C`           | `44.0`                      | Matches the whole string and returns the content of the capture group `(.*?)`.                                 |
| `48312`                     | `s/(.{2})(.{3})/$1.$2/g` | `48.312`                    | Captures two and three characters, returns the first capture group, adds a dot, then the second capture group. |

### Example setup

#### Input String

```shell
temp=44.0'C
```

The regex transformation can be used to extract the value to display it on the label.

#### `example.items`

```java
String  Temperature_str "Temperature [REGEX(.*=(\\d*\\.\\d*).*):%s °C]" {...}
Number  Temperature "Temperature [%.1f °C]"
```

The regex pattern is defined as follows:

- `.*` match any character, zero and unlimited times
- `=` match the equal sign literally, used to find the position
- `()` capture group match
  - `\d*` match a digit (equal to [0-9]), zero and unlimited times
  - `\.` match the dot literally
  - `\w*` match a word character (equal to [a-zA-Z_0-9]), zero and unlimited times
- `.*` match any character, zero and unlimited times

Note: the backslashes have to be escaped. See [string vs plain](#differences-to-plain-regex).

The result will be `44.0` and displayed on the label as `Temperature 44.0 °C`.
A better solution would be to apply the regex to the result from the binding—either in a rule or, when the binding allows it, on the output channel.
Thus the value `44.0` would be saved as a number.

#### .rules

```java
rule "Convert String to Item Number"
when
    Item Temperature_str changed
then
    // use the transformation service to retrieve the value
    val newValue = transform("REGEX", ".*=(\\d*\\.\\d*).*", Temperature_str.state.toString)

    // post the new value to the Number Item
    Temperature.postUpdate( newValue )
end
```

Now the resulting Number can also be used in the label to [change the color](https://www.openhab.org/docs/ui/sitemaps.html#label-value-and-icon-colors) or in a rule as a value for comparison.

## Differences to Plain Regex

The regex is embedded in a string, so when double quotes `"` are used in a regex they need to be escaped as `\"` to keep the string intact.
Because the escape character in strings is the backslash, it must itself be escaped.
To use a dot as a literal in the regex, escape it as `\.`; in a string this becomes `"\\."`.
The first backslash escapes the second backslash in the string so it can be used in the regex.
Using a backslash in a regex as a literal `\\` becomes `"\\\\"` in the string.

## Usage as a Profile

The functionality of this `TransformationService` can be used in a `Profile` on an `ItemChannelLink` too.
To do so, it can be configured in the `.items` file as follows:

```java
String <itemName> { channel="<channelUID>"[profile="transform:REGEX", function="<regex>", sourceFormat="<valueFormat>"]}
```

The regular expression to be executed has to be set in the `function` parameter.
The parameter `sourceFormat` is optional and can be used to format the input value **before** the transformation, e.g., `%.3f`.
If omitted, the default is `%s`, so the input value will be put into the transformation without any format changes.

Please note: This profile is a one-way transformation, i.e., only values from a device towards the item are changed; the other direction is left untouched.

## Further Reading

- A full [introduction](https://www.w3schools.com/jsref/jsref_obj_regexp.asp) to regular expressions is available at W3Schools.
- Online validators help check the syntax of a regex and provide hints on how to design it.
  - [Regex 101](https://regex101.com/)
  - [Regex R](https://regexr.com/)
  - [ExtendsClass](https://extendsclass.com/regex-tester.html)
  - [Softwium](https://softwium.com/regex-explainer/)
