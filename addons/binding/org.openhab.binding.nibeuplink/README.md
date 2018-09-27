# NibeUplink Binding

The NibeUplink binding is used to get "live data" from from Nibe heat pumps without plugging any custom devices into your heat pump.
This avoids the risk of losing your warranty. Instead data is retrieved from Nibe Uplink. This binding should in general be compatible with heat pump models that support Nibe Uplink.
In general read access is supported for all channels. Write access is only supported for a small subset of channels.

## Supported Things

This binding provides only one thing type: The Nibe heat pump. Create one Nibe heat pump thing per physical heat pump installation available in your home(s).
If your setup contains an outdoor unit such as F2030 or F2040 and an indoor unit such as VVM320 this is one installation where the indoor unit is the master that has access to all data produced by the outdoor unit (slave).

## Discovery

Auto-Discovery is not supported, as credentials are necessary to login into NibeUplink.

## Thing Configuration

The syntax for a heat pump thing is:

```
nibeuplink:<MODEL>:<NAME>
```

- **nibeuplink** the binding id, fixed
- **model** the heatpump model (Binding Model)
- **name** the name of the heatpump (choose any name)

Following models (indoor / main units) are currently supported:

| Nibe Model(s)     | Openhab Model     | Description                                           |
|-------------------|-------------------|-------------------------------------------------------|
| VVM310 / 500      | vvm310            | reduced set of channels based on NibeUplink website   |
| VVM320 / 325      | vvm320            | reduced set of channels based on NibeUplink website   |
| F730              | f730              | reduced set of channels based on NibeUplink website   |
| F750              | f750              | reduced set of channels based on NibeUplink website   |
| F1145 / 1245      | f1145             | reduced set of channels based on NibeUplink website   |
| F1155 / 1255      | f1155             | reduced set of channels based on NibeUplink website   |

The following configuration parameters are available for this thing:

- **user** (required)  
username used to login on NibeUplink

- **password** (required)  
password used to login on NibeUplink

- **nibeId** (required)  
Id of your heatpump in NibeUplink (can be found in the URL after successful login: https://www.nibeuplink.com/System/**<nibeId>>**/Status/Overview)

- **pollingInterval**  
interval (seconds) in which values are retrieved from NibeUplink. Setting less than 60 seconds does not make any sense as the heat pump only provides periodic updates to NibeUplink. (default = 60). 

- **houseKeepingInterval**  
interval (seconds) in which list of "dead channels" (channels that do not return any data or invalid data) should be purged (default = 3600). Usually this settings should not be changed.

- **customChannel01 - customChannel08**  
allows to define up to 8 custom channels which are not covered in the basic channel list of your model. Any number between 10000 and 50000 is allowed. 

### Examples

- minimum configuration

```
nibeuplink:vvm320:mynibe [ user="...", password="...", nibeId="..."]
```

- with pollingInterval

```
nibeuplink:vvm320:mynibe[ user="...", password="...", nibeId="...", pollingInterval=... ]
```

- multiple heat pumps

```
nibeuplink:vvm320:home1 [ user="...", password="...", nibeId="..."]
nibeuplink:vvm320:home2  [ user="...", password="...", nibeId="..."]
```

## Channels

Available channels depend on the specific heatpump model. Following models/channels are currently available

| Model          | Channels                                        |
|----------------|-------------------------------------------------|
| All models     | [List](nibe-doc/base/channels.md)               |
| VVM310 / 500   | [List](nibe-doc/vvm310/channels.md)             |
| VVM320 / 325   | [List](nibe-doc/vvm320/channels.md)             |
| F730           | [List](nibe-doc/f730/channels.md)               |
| F750           | [List](nibe-doc/f750/channels.md)               |
| F1145 / 1245   | [List](nibe-doc/f1145/channels.md)              |
| F1155 / 1255   | [List](nibe-doc/f1155/channels.md)              |


## Full Example

### Thing

```
nibeuplink:vvm320:mynibe     [ user="nibe@my-domain.de", password="secret123", nibeId="4711", pollingInterval=300, customChannel01=47376, customChannel02=48009 ]
```

### Items

As the binding supports UoM you might define units in the item's label. An automatic conversion is applied e.g. from °C to °F then.
Channels which represent two states (such as on/off) are represented as Switch.
Channels which have more than two states are internally represented as number.
You need to define a map file which also gives you the opportunity to translate the state into your preferred language.

```
Number:Temperature      NIBE_SUPPLY            "Vorlauf"                         { channel="nibeuplink:vvm320:mynibe:base#40008" }
Number:Temperature      NIBE_RETURN            "Rücklauf [%.2f °F]"              { channel="nibeuplink:vvm320:mynibe:base#40012" }
Number:Temperature      NIBE_HW_TOP            "Brauchwasser oben"               { channel="nibeuplink:vvm320:mynibe:hotwater#40013" }
Number:Energy           NIBE_HM_HEAT           "WM Heizung"                      { channel="nibeuplink:vvm320:mynibe:base#44308" }
Switch                  NIBE_COMP_DEFROST      "Enteisung"                       { channel="nibeuplink:vvm320:mynibe:compressor#44703" }
Number                  NIBE_HW_MODE           "Modus [MAP(hwmode.map):%s]"      { channel="nibeuplink:vvm320:mynibe:hotwater#47041" }

Number                  NIBE_CUSTOM_01         "Custom 01"                       { channel="nibeuplink:vvm320:mynibe:custom#CH01" }
```

### Transformations

Please define each state both as integer.

```
0=Eco
1=Norm
2=Lux
```


### Sitemaps

Please take care of the status channels. If you use selection items an automatic mapping will be applied. If you prefer switch items a mapping must be applied like this:

```
Switch item=NIBE_HW_MODE mappings=[0="Eco", 1="Norm"]
```