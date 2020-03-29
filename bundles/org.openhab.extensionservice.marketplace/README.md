# Eclipse marketplace Extension service

This is an `ExtensionService`, which accesses the Eclipse IoT Marketplace and makes its content available as
extensions.

The internally used http address is "https://marketplace.eclipse.org/taxonomy/term/4988%2C4396/api/p?client=org.eclipse.smarthome".

The default refresh value is 1 hour.
If the eclipse server is not responding, another attempt happens 60 seconds later.

## Marketplace format

The received extensions description is XML. The schema is like in the following example:

```xml
<marketplace>
  <category id="4988" marketid="4396" name="Eclipse SmartHome" url="https://marketplace.eclipse.org/category/markets/iot">
    <node id="3305842" name="Energy Meter" url="https://marketplace.eclipse.org/content/energy-meter">
        <type>iot_package</type>
        <categories>
            <category id="4988" name="Eclipse SmartHome" url="https://marketplace.eclipse.org/category/categories/eclipse-smarthome"/>
        </categories>
        <owner>Kai Kreuzer</owner>
        <favorited>0</favorited>
        <installstotal>0</installstotal>
        <installsrecent>0</installsrecent>
        <shortdescription>
            Desc
        </shortdescription>
        <body>
            Example body
        </body>
        <created>1487690446</created>
        <changed>1489494314</changed>
        <foundationmember>1</foundationmember>
        <homepageurl>https://www.openhabfoundation.org</homepageurl>
        <image>
            https://marketplace.eclipse.org/sites/default/files/styles/ds_medium/public/iot-package/logo/heating.png?itok=qMbbIXEU
        </image>
        <license>EPL</license>
        <companyname>
            openHAB Foundation
        </companyname>
        <status>Alpha</status>
        <supporturl></supporturl>
        <version/>
        <updateurl>
            https://raw.githubusercontent.com/kaikreuzer/esh-templates/master/energymeter.json
        </updateurl>
        <packagetypes>rule_template</packagetypes>
        <sourceurl/>
        <versioncompatibility>
        <from/>
        <to/>
        </versioncompatibility>
        <packageformat>json</packageformat>
    </node>
  </category>
</marketplace>
```
