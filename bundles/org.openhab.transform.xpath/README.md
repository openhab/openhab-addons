# XPath Transformation Service

Transforms an [XML](https://www.w3.org/XML/) input using an [XPath](https://www.w3.org/TR/xpath/#section-Expressions) expression.

## Examples

### Basic Example

Given a retrieved XML

```xml
<?xml version="1.0" encoding="UTF-8"?>
<PTZStatus version="2.0" >
    <AbsoluteHigh>
        <elevation>0</elevation>
        <azimuth>450</azimuth>
        <absoluteZoom>10</absoluteZoom>
    </AbsoluteHigh>
</PTZStatus>
```

The XPath `/PTZStatus/AbsoluteHigh/azimuth/text()` returns

```text
450
```

## Advanced Example

Given a retrieved XML (e.g., from a Hikvision device with the namespace `xmlns="http://www.hikvision.com/ver20/XMLSchema"`):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<PTZStatus version="2.0" xmlns="http://www.hikvision.com/ver20/XMLSchema">
    <AbsoluteHigh>
        <elevation>0</elevation>
        <azimuth>450</azimuth>
        <absoluteZoom>10</absoluteZoom>
    </AbsoluteHigh>
</PTZStatus>
```

A simple XPath query to fetch the azimuth value does not work because it does not address the namespace.

There are two ways to address the namespace:

- Simple path, which may not work in complex XML.
- Fully qualified path.

The XPath

- `[name()='PTZStatus']/*[name()='AbsoluteHigh']/*[name()='azimuth']/`
- `/*[local-name()='PTZStatus' and namespace-uri()='http://www.hikvision.com/ver20/XMLSchema']/*[local-name()='AbsoluteHigh' and namespace-uri()='http://www.hikvision.com/ver20/XMLSchema']/*[local-name()='azimuth' and namespace-uri()='http://www.hikvision.com/ver20/XMLSchema']`

returns

```xml
<azimuth>450</azimuth>
```

### Example Setup

#### .items

```java
String  Temperature_xml "Temperature [XPATH(/*[name()='PTZStatus']/*[name()='AbsoluteHigh']/*[name()='azimuth']/text()):%s °C]" {...}
Number  Temperature "Temperature [%.1f °C]"
```

### .rules

```java
rule "Convert XML to Item Type Number"
when
    Item Temperature_xml changed
then
    // use the transformation service to retrieve the value
    // Simple
    val mytest = transform("XPATH", "/*[name()='PTZStatus']
                                     /*[name()='AbsoluteHigh']
                                     /*[name()='azimuth']
                                     /text()",
                                    Temperature_xml.state.toString )
    // Fully qualified
    val mytest = transform("XPATH", "/*[local-name()='PTZStatus'    and namespace-uri()='http://www.hikvision.com/ver20/XMLSchema']
                                     /*[local-name()='AbsoluteHigh' and namespace-uri()='http://www.hikvision.com/ver20/XMLSchema']
                                     /*[local-name()='azimuth'      and namespace-uri()='http://www.hikvision.com/ver20/XMLSchema']
                                     /text()",
                                    Temperature_xml.state.toString )

    // post the new value to the Number Item
    Temperature.postUpdate( newValue )
end
```

Now the resulting Number can also be used in the label to [change the color](https://www.openhab.org/docs/ui/sitemaps.html#label-value-and-icon-colors) or in a rule as a value for comparison.

## Usage as a Profile

The functionality of this `TransformationService` can be used in a `Profile` on an `ItemChannelLink` too.
To do so, it can be configured in the `.items` file as follows:

```java
String <itemName> { channel="<channelUID>"[profile="transform:XPATH", function="<xpath>", sourceFormat="<valueFormat>"]}
```

The XPath expression to be executed has to be set in the `function` parameter.
The parameter `sourceFormat` is optional and can be used to format the input value **before** the transformation, e.g., `%.3f`.
If omitted, the default is `%s`, so the input value will be put into the transformation without any format changes.

Please note: This profile is a one-way transformation, i.e., only values from a device towards the item are changed; the other direction is left untouched.

## Further Reading

- An [introduction](https://www.w3schools.com/xml/xpath_intro.asp) to XPath at W3Schools.
- An informative explanation of [common mistakes](https://qxf2.com/blog/common-xpath-mistakes/).
- Online validation tools like [this](https://www.freeformatter.com/xpath-tester.html) to check the syntax.
