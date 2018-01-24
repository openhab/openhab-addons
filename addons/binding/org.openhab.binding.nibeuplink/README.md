# NibeUplink Binding

The NibeUplink binding is used to get "live data" from from Nibe heat pumps without plugging any custom devices into your heat pump. This avoids the risk of losing your warranty. Instead data is retrieved from Nibe Uplink. This binding should in general be compatible with heat pump models that support Nibe Uplink.
Currently only read access is supported. Ability to modify heat pump configuration might be added in a future version.

## Supported Things

This binding provides only one thing type: The Nibe heat pump. Create one Nibe heat pump thing per physical heat pump installation available in your home(s). If your setup contains an outdoor unit such as F2030 or F2040 and an indoor unit such as VVM320 this is one installation where the indoor unit is the master that has access to all data produced by the outdoor unit (slave).

## Discovery

Auto-Discovery is not supported, as credentials are necessary to login into NibeUplink.

## Thing Configuration

The syntax for a heat pump thing is:

```
nibeuplink:<MODEL>:<NAME>
```

- **nibeuplink** the binding id, fixed
- **model** the heatpump model (Openhab Model)
- **name** the name of the heatpump (choose any name)

Following models (indoor / main units) are currently supported:

| Nibe Model(s)     | Openhab Model     | Description                                           |
|-------------------|-------------------|-------------------------------------------------------|
| VVM310 / 500      | vvm310            | reduced set of channels based on NibeUplink website   |
| VVM320 / 325      | vvm320            | reduced set of channels based on NibeUplink website   |
| F750              | f750              | all channels                                          |
| F1145 / 1245      | f1145             | all channels                                          |
| F1155 / 1255      | f1155             | all channels                                          |

There are a few settings this thing:

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

| Model          | All Channels                                    |
|----------------|-------------------------------------------------|
| VVM310 / 500   | [List](nibe-doc/vvm310/channels.md)             |
| VVM320 / 325   | [List](nibe-doc/vvm320/channels.md)             |
| F750           | [List](nibe-doc/f750/channels.md)               |
| F1145 / 1245   | [List](nibe-doc/f1145/channels.md)              |
| F1155 / 1255   | [List](nibe-doc/f1155/channels.md)              |

The "all channels" lists have been generated automatically from Nibe Modbus Manager database. It is very likely that the list of channels is not 100% correct.

## Full Example

### Thing

```
nibeuplink:vvm320:mynibe     [ user="nibe@my-domain.de", password="secret123", nibeId="4711", pollingInterval=300 ]
```

### Items

```
Number      Nibe_40013_BT7        "Brauchwasser oben [%.2f °C]"            {channel="nibeuplink:vvm320:mynibe:sensor#40013"}
Number      Nibe_40014_BT6        "Brauchwasserbereitung [%.2f °C]"        {channel="nibeuplink:vvm320:mynibe:sensor#40014"}
```
