# HPPrinter Binding

Get HP Printer statistics from an _Embedded Web Server_ over Network.

## Supported Things

There is only one thing named `printer`. Channels are added dynamically depending on the type of printer.

## Discovery

This Binding will auto-discover any HP Printer on the network via Bonjour. 

## Thing Configuration

The available settings are:
| Setting                           | Name            | Type    | Required | Default |
| --------------------------------- | --------------- | ------- | -------- | ------- |
| IP Address                        | ipAddress       | string  | yes      |         |
| Usage Refresh Interval (seconds)  | usageInterval   | number  |          | 30      |
| Status Refresh Interval (seconds) | statusInterval  | number  |          | 4       |

An example configuration is below:

```
Thing hpprinter:printer:djprinter "Printer" @ "Office" [ ipAddress="192.168.1.1", usageInterval="30", statusInterval="4" ]
```

## Channels

| Channel                                    | Name                 | Data Type | Dynamic |
| ------------------------------------------ | -------------------- | --------- | ------- |
| Printer Status                             | status               | String    | no      |
| Black Colour Level                         | blackLevel           | Number    | no      |
| Colour Level                               | colorLevel           | Number    | yes     |
| Cyan Colour Level                          | cyanLevel            | Number    | yes     |
| Magenta Colour Level                       | magentaLevel         | Number    | yes     |
| Yellow Colour Level                        | yellowLevel          | Number    | yes     |
| Black Marking Used                         | blackMarker          | Number    | yes     |
| Colour Marking Used                        | colorMarker          | Number    | yes     |
| Cyan Marking Used                          | cyanMarker           | Number    | yes     |
| Magenta Marking Used                       | magentaMarker        | Number    | yes     |
| Yellow Marking Used                        | yellowMarker         | Number    | yes     |
| Total Number of Pages Printed              | totalCount           | Number    | no      |
| Total Number of Colour Pages Printed       | totalColorCount      | Number    | yes     |
| Total Number of Monochrome Pages Printed   | totalMonochromeCount | Number    | yes     |
| Jam Events                                 | jamEvents            | Number    | yes     |
| Mispick Events                             | mispickEvents        | Number    | yes     |
| Front Panel Cancel Count                   | fpCount              | Number    | yes     |

> The `colorLevel` is used on Printers that have only a single colour cartridge instead of separate Cyan, Magenta and Yellow cartridges.
