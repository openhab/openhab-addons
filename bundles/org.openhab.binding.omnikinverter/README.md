# Omnik Inverter Binding

This binding reads metrics from Omnik Solar Inverters.

## Supported Things

All Omniksols are expected to function, provided they have the Wifi module. At
moment of writing the _Omniksol-3.0k-TL2_ has been tested.

## Discovery

No autodiscovery available

## Thing Configuration

| Config   | Description                                                                                                                    | type    | Default   |
| :------- | :------------                                                                                                                  | :-----  | :-------- |
| hostname | The hostname or ip through which the inverter can be accessed                                                                  | string  | n/a       |
| port     | TCP port through which the inverter listens on for incoming connections                                                        | integer | 8899      |
| serial   | The serial of the wifi module. The Wifi module's SSID contains the number. This is the numerical part only, i.e. without _AP__ | integer | n/a       |

## Channels

| Channel Type Id   | Item Type     | Description                          |
| :---------------- | :----------   | :------------                        |
| power             | Number:Power  | The instantaneous power generation   |
| energyToday       | Number:Energy | The amount of energy generated today |

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._


## References

Based on the work of https://github.com/Woutrrr/Omnik-Data-Logger
