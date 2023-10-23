# AddonSuggestionFinder Addon

This is a special addon that implements an `AddonInfoProvider` service containing information about suggested Addons that could potentially be installed.
It allows developers to include information in their own addons so that the system can scan the user's network to discover potential addons that can automatically be installed.

## Addon Developer Notes

If you want to your addon to scan the user's system then you need to include additional fields in your `src/main/resources/OH-INF/addon.xml` file.

| XML Element Name    | Description                                                                   | Instances                                      |
|---------------------|-------------------------------------------------------------------------------|------------------------------------------------|
| `discovery-methods` | Wrapper for `discovery-method` elements (see below).                          | Zero or one instances per file.                |
| `discovery-method`  | Complex XML element describing an addon discovery method.                     | Zero or more instances per file.               |
| `service-type`      | The type of discovery method. May be `upnp` or `mdns`.                        | Mandatory one per `discovery-method`.          |
| `mdns-service-type` | If `service-type` is `mdns`, contains the MDNS discovery service type.        | Optional one per `discovery-method`.           |
| `match-properties`  | Wrapper for `match-property` elements (see below).                            | Zero or one instances per `discovery-method`.  |
| `match-property`    | A property name and regular expression used for matching discovery findings.  | Zero or more instances per `discovery-method`. |
| `name`              | A property name to search for.                                                | Mandatory one instance per `match-property`.   |
| `regex`             | A regular expression (or plain string) that needs to match the property name. | Mandatory one instance per `match-property`.   |

## Example `addon.xml` File

The following is an example for the discovery XML description for HP Printers.

```xml
<addon:addon id="hpprinter" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:addon="https://openhab.org/schemas/addon/v1.0.0"
    xsi:schemaLocation="https://openhab.org/schemas/addon/v1.0.0 https://openhab.org/schemas/addon-1.0.0.xsd">

    <type>binding</type>
    <name>HP Printer</name>
    <description>HP Printer Binding</description>
    <connection>local</connection>
    <discovery-methods>
        <discovery-method>
            <service-type>mdns</service-type>
            <mdns-service-type>_printer._tcp.local.</mdns-service-type>
            <match-properties>
                <match-property>
                    <name>rp</name>
                    <regex>.*</regex>
                </match-property>
                <match-property>
                    <name>ty</name>
                    <regex>hp (.*)</regex>
                </match-property>
            </match-properties>
        </discovery-method>
    </discovery-methods>
</addon:addon>
```

The following is an example for the discovery XML description for the Philips Hue bridge.

```xml
<addon:addon id="hue" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:addon="https://openhab.org/schemas/addon/v1.0.0"
    xsi:schemaLocation="https://openhab.org/schemas/addon/v1.0.0 https://openhab.org/schemas/addon-1.0.0.xsd">

    <type>binding</type>
    <name>Philips Hue</name>
    <description>Philips Hue Binding</description>
    <connection>local</connection>
    <discovery-methods>
        <discovery-method>
            <serviceType>mdns</serviceType>
            <mdnsServiceType>_hue._tcp.local.</mdnsServiceType>
        </discovery-method>
        <discovery-method>
            <service-type>upnp</service-type>
            </match-properties>
                <match-property>
                    <name>modelName</name>
                    <regex>Philips hue bridge</regex>
                </match-property>
            </match-properties>
        </discovery-method>
    </discovery-methods>
</addon:addon>
```
