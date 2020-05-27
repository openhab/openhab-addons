# HP Printer Binding

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

### Sample Configuration

```
Thing hpprinter:printer:djprinter "Printer" @ "Office" [ ipAddress="192.168.1.1", usageInterval="30", statusInterval="4" ]
```

## Channels

| Channel                                    | Name                 | Data Type            | Dynamic |
| ------------------------------------------ | -------------------- | -------------------- | ------- |
| Printer Status                             | status               | String               | no      |
| Black Colour Level                         | blackLevel           | Number:Dimensionless | no      |
| Colour Level                               | colorLevel           | Number:Dimensionless | yes     |
| Cyan Colour Level                          | cyanLevel            | Number:Dimensionless | yes     |
| Magenta Colour Level                       | magentaLevel         | Number:Dimensionless | yes     |
| Yellow Colour Level                        | yellowLevel          | Number:Dimensionless | yes     |
| Black Marking Used                         | blackMarker          | Number:Volume        | yes     |
| Colour Marking Used                        | colorMarker          | Number:Volume        | yes     |
| Cyan Marking Used                          | cyanMarker           | Number:Volume        | yes     |
| Magenta Marking Used                       | magentaMarker        | Number:Volume        | yes     |
| Yellow Marking Used                        | yellowMarker         | Number:Volume        | yes     |
| Total Number of Pages Printed              | totalCount           | Number               | no      |
| Total Number of Colour Pages Printed       | totalColorCount      | Number               | yes     |
| Total Number of Monochrome Pages Printed   | totalMonochromeCount | Number               | yes     |
| Jam Events                                 | jamEvents            | Number               | yes     |
| Mispick Events                             | mispickEvents        | Number               | yes     |
| Front Panel Cancel Count                   | fpCount              | Number               | yes     |

> The `colorLevel` is used on Printers that have only a single colour cartridge instead of separate Cyan, Magenta and Yellow cartridges.

### Sample Items

```
String PrinterStatus "Status" { channel="pprinter:printer:djprinter:status#status" }
Number PrinterTotalPages "Total Pages" { channel="hpprinter:printer:djprinter:usage#totalCount" }

Number:Volume PrinterBlackMarkingUsed "Black Marking Used" { channel="hpprinter:printer:djprinter:usage#blackMarker" }
Number:Volume PrinterCyanMarkingUsed "Cyan Marking Used" { channel="hpprinter:printer:djprinter:usage#cyanMarker" }
Number:Volume PrinterMagentaMarkingUsed "Magenta Marking Used" { channel="hpprinter:printer:djprinter:usage#magentaMarker" }
Number:Volume PrinterYellowMarkingUsed "Yellow Marking Used" { channel="hpprinter:printer:djprinter:usage#yellowMarker" }

Number:Dimensionless PrinterBlackLevel "Black Level" { channel="hpprinter:printer:djprinter:ink#blackLevel" }
Number:Dimensionless PrinterCyanLevel "Cyan Level" { channel="hpprinter:printer:djprinter:ink#cyanLevel" }
Number:Dimensionless PrinterMagentaLevel "Magenta Level" { channel="hpprinter:printer:djprinter:ink#magentaLevel" }
Number:Dimensionless PrinterYellowLevel "Yellow Level" { channel="hpprinter:printer:djprinter:ink#yellowLevel" }

Number PrinterTotalColourPages "Total Colour Pages" { channel="hpprinter:printer:djprinter:usage#totalColorCount" }
Number PrinterTotalMonochromePages "Total Monochrome Pages" { channel="hpprinter:printer:djprinter:usage#totalMonochromeCount" }
Number PrinterJamEvents "Jam Events" { channel="hpprinter:printer:djprinter:usage#jamEvents" }
Number PrinterMispickEvents "Mispick Events" { channel="hpprinter:printer:djprinter:usage#mispickEvents" }
Number PrinterSubscriptionCount "Subscription Count" { channel="hpprinter:printer:djprinter:usage#subsciptionCount" }
```

### Sample Sitemap Items

Black Ink displayed as a whole percentage - `60 %`

```
Text item=hpprinter_printer_djprinter_ink_blackLevel label="Black [%.0f %unit%]"
```

Black Marker displayed in millilitres - `21 ml`

```
Text item=hpprinter_printer_djprinter_usage_blackMarker label="Black Marker [%.0f %unit%]"
```

Black Marker displayed in litres - `0.021 l`

```
Text item=hpprinter_printer_djprinter_usage_blackMarker label="Black Marker [%.3f l]"
```