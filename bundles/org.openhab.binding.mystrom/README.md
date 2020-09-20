# myStrom Binding

This extension adds support for the myStrom devices. Currently only the smart plug is implemented.

## Supported Things

This bundle adds the following thing types:

| Thing              | ThingTypeID | Description                                        |
| ------------------ | ----------- | -------------------------------------------------- |
| myStrom Smart Plug | mystromplug | A myStrom smart plug                               |

## Discovery

This extension does not support autodiscovery. The things need to be added manually.


## Thing Configuration

The following parameters are valid for all thing types:

| Parameter | Type    | Required | Default if omitted | Description                                                                |
| --------- | ------- | -------- | ------------------ | -------------------------------------------------------------------------- |
| hostname  | string  | yes      | localhost          | The IP address or hostname of the myStrom smart plug                       |
| refresh   | integer | no       | 10                 | Poll interval in seconds. Increase this if you encounter connection errors |

## Channels

| Channel ID       | Item Type            | Read only | Description                                                   |
| ---------------- | -------------------- | --------- | ------------------------------------------------------------- |
| switch           | Switch               | false     | Turn the smart plug on or off                                 |
| power            | Number:Power         | true      | The currently delivered power                                 |
| temperature      | Number:Temperature   | true      | The temperature at the plug                                   |

## Full Example

### Thing Configuration

```
Thing mystrom:mystromplug:d6217a31 "Plug" [hostname="hostname|ip"]
```

### Item Configuration

```
Switch PlugSwitch		"Plug" 	                		 		{channel="mystrom:mystromplug:d6217a31:switch"}
Number:Temperature PlugTemperature "Temperature: [%.1f °C]"     {channel="mystrom:mystromplug:d6217a31:temperature"}  
Number:Power PlugPower "Power: [%.1f W]"                        {channel="mystrom:mystromplug:d6217a31:power"} 

```

### Sitemap Configuration

```
Frame label="myStrom Plug" { 
    Switch item=PlugSwitch
    Text item=PlugTemperature
    Text item=PlugPower
}
```
