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
| energyTotal       | Number:Energy | The total amount of energy generated |

## Full Example

### demo.things

```
Thing omnikinverter:omnik:70ecb4f0 "Solar Inverter" [ hostname="igen-wifi.lan",serial=604455290]
```

### demo.items

```
Number OmnikInverterBindingThing_InstantaneousPower "Solar Power" {channel="omnikinverter:omnik:70ecb4f0:power"}
Number OmnikInverterBindingThing_TotalGeneratedEnergyToday "Solar Energy Today"  {channel="omnikinverter:omnik:70ecb4f0:energyToday"}
Number OmnikInverterBindingThing_TotalGeneratedEnergy "Solar Energy Total"  {channel="omnikinverter:omnik:70ecb4f0:energyTotal"}
```

### Sitemap

```
Text item=OmnikInverterBindingThing_InstantaneousPower
Text item=OmnikInverterBindingThing_TotalGeneratedEnergyToday label="Today"
Text item=OmnikInverterBindingThing_TotalGeneratedEnergy label="Total"
```

## References

Based on the work of https://github.com/Woutrrr/Omnik-Data-Logger
