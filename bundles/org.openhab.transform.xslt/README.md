# XSLT Transformation Service

Transform input using the XML Stylesheet Language for Transformations (XSLT).

XSLT is a standard method to transform an XML structure from one document into a new document with a different structure.

The transformation expects the rule to be read from a file which is stored under the `transform` folder.
To organize the various transformations one should use subfolders.

General transformation rule summary:

- The directive `xsl:output` defines how the output document should be structured.
- The directive `xsl:template` specifies matching attributes for the XML node to find.
- The `xsl:template` tag contains the rule which specifies what should be done.

The Rule uses XPath to gather the XML node information.
For more information have a look at the [XPath transformation](https://docs.openhab.org/addons/transformations/xpath/readme.html) .

## Examples

### Basic Example

A simple but complete XSLT transformation looks like in the following example, which was taken from [here](https://en.wikipedia.org/wiki/Java_API_for_XML_Processing#Example).

**input XML**

```xml
<?xml version='1.0' encoding='UTF-8'?>
<root><node val='hello'/></root>
```

**transform/helloworld.xsl**

- `xsl:output`: transform incoming document into another XML-like document, without indentation.
- `xsl:template`: `match="/"` "any type of node", so the whole document.
- The `xsl` rule does `select` the node `/root/node` and extracts the `value-of` attribute `val`.

```xml
<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version='2.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
   <xsl:output method='xml' indent='no'/>
   <xsl:template match='/'>
      <reRoot><reNode><xsl:value-of select='/root/node/@val' /> world</reNode></reRoot>
   </xsl:template>
</xsl:stylesheet>
```

**Output XML**

```xml
<reRoot><reNode>hello world</reNode></reRoot>
```

### Advanced Example

This example has a namespace defined, as you would find in real world applications, which has to be matched in the rule.

**input XML**

- The tag `<PTZStatus>` contains an attribute `xmlns=` which defines the namespace `http://www.hikvision.com/ver20/XMLSchema`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<PTZStatus version="2.0" xmlns="http://www.hikvision.com/ver20/XMLSchema">
    <AbsoluteHigh>
        <elevation>0</elevation>
        <azimuth date="Fri, 18 Dec 2009 9:38 am PST" >450</azimuth>
        <absoluteZoom>10</absoluteZoom>
    </AbsoluteHigh>
</PTZStatus>
```

**transform/azimut.xsl**

In the rule, the tag `<xsl:stylesheet>` has to have an attribute `xmlns:xsl="http://www.w3.org/1999/XSL/Transform"` and a second attribute `xmlns:`.
This attribute has to be the same as the namespace for the input document.
In the rule each step traversed along the path to the next tag has to be prepended with the `xmlns` namespace, here defined as `h`.

- `xsl:output` transform incoming document into another XML-like document, no indentation, **without XML**.
- `xsl:template`: `match="/"` whole document.
- Full path to node `azimuth` reading out `date` attribute.
- Add a linebreak by setting `&#10;` as text.
- Search for node `azimuth` by prepending `//` and get the `text`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:h="http://www.hikvision.com/ver20/XMLSchema">
   <xsl:output method="xml" indent="no" encoding="UTF-8" omit-xml-declaration="yes"  />
   <xsl:template match="/">
      <xsl:value-of select="/h:PTZStatus/h:AbsoluteHigh/h:azimuth/@date" />
      <xsl:text>&#10;</xsl:text>
      <xsl:value-of select="//h:azimuth/text()" />
   </xsl:template>
</xsl:stylesheet>
```

**Output Document**

```
Fri, 18 Dec 2009 9:38 am PST
450
```

## Usage as a Profile

The functionality of this `TransformationService` can be used in a `Profile` on an `ItemChannelLink` too.
To do so, it can be configured in the `.items` file as follows:

```java
String <itemName> { channel="<channelUID>"[profile="transform:XSLT", function="<xsltExpression>", sourceFormat="<valueFormat>"]}
```

The XSLT file (from within the `transform` folder) to be used has to be set in the `function` parameter.
The parameter `sourceFormat` is optional and can be used to format the input value **before** the transformation, i.e. `%.3f`.
If omitted the default is `%s`, so the input value will be put into the transformation without any format changes.

Please note: This profile is a one-way transformation, i.e. only values from a device towards the item are changed, the other direction is left untouched.

## Further Reading

- Extended introduction and more [examples](https://en.wikipedia.org/wiki/XSLT#XSLT_examples) at Wikipedia.
- A good [introduction](https://www.w3schools.com/xml/xsl_intro.asp) and [tutorial](https://www.w3schools.com/xml/xsl_transformation.asp) at W3School.
- An informative [tutorial](https://www.ibm.com/developerworks/library/x-xsltmistakes/) of common mistakes.
- Online XSL transformer tools like [this](https://www.freeformatter.com/xsl-transformer.html) to check the syntax.
