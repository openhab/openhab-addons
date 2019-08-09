# HPPrinter Binding

Get HP Printer statistics from an _Embedded Web Server_ over Network.

## Supported Things

There are three different types of things:
* Monochrome HP Printer (monochrome)
* Single Color HP Printer (singlecolor)
* Multi Color HP Printer (multicolor)

## Discovery

This Binding will auto-discover any HP Printer on the network via Bonjour. If it detects a supported model then the corrosponding Thing will be chosen otherwise it will use the Unsupported HP Printer Thing.

## Thing Configuration

The available settings are:
| Setting                     | Name            | Type    | Required |
| --------------------------- | --------------- | ------- | -------- |
| IP Address                  | ipAddress       | string  | yes      |
| Secure Sockets              | useSSL          | boolean |          |
| Usage Refresh Interval      | usageInterval   | number  |          |
| Status Refresh Interval     | statusInterval  | number  |          |

An example configuration is below:

```
Thing hpprinter:multicolor:djprinter "Printer" @ "Office" [ ipAddress="192.168.1.1", useSSL=true, refreshInterval="30" ]
```

## Channels

| Channel                                    | Name                 | Data Type |
| ------------------------------------------ | -------------------- | --------- |
| Colour Level                               | colorLevel          | Number    |
| Cyan Colour Level                          | cyanLevel            | Number    |
| Magenta Colour Level                       | magentaLevel         | Number    |
| Yellow Colour Level                        | yellowLevel          | Number    |
| Black Colour Level                         | blackLevel           | Number    |
| Printer Status                             | status               | String    |
| Subscriptions                              | subsciptionCount     | Number    |
| Total Number of Pages Printed              | totalCount           | Number    |
| Total Number of Colour Pages Printed       | totalColorCount      | Number    |
| Total Number of Monochrome Pages Printed   | totalMonochromeCount | Number    |

> The `colorLevel` is used on Printers that have only a single colour cartridge (Single Color HP Printer) instead of separate Cyan, Magenta and Yellow ones (Multi Color HP Printer).
