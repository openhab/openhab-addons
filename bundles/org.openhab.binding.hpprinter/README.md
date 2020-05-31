# HP Printer Binding

Get HP Printer statistics from an _Embedded Web Server_ over Network.

## Supported Things

There is only one thing named `printer`. All channels are added dynamically depending on the type of printer and its capabilities.

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

| Channel                                    | Group       | Name                  | Data Type            |
| ------------------------------------------ | ----------- | --------------------- | -------------------- |
| Printer Status                             | status      | status                | String               |
| Tray Empty/Open                            | status      | trayEmptyOrOpen       | Switch               |
| Scanner Status                             | status      | scannerStatus         | String               |
| ADF Loaded                                 | status      | scannerAdfLoaded      | Switch               |
| Black Colour Level                         | usage       | blackLevel            | Number:Dimensionless |
| Colour Level                               | usage       | colorLevel            | Number:Dimensionless |
| Cyan Colour Level                          | usage       | cyanLevel             | Number:Dimensionless |
| Magenta Colour Level                       | usage       | magentaLevel          | Number:Dimensionless |
| Yellow Colour Level                        | usage       | yellowLevel           | Number:Dimensionless |
| Black Marking Used                         | usage       | blackMarker           | Number:Volume        |
| Colour Marking Used                        | usage       | colorMarker           | Number:Volume        |
| Cyan Marking Used                          | usage       | cyanMarker            | Number:Volume        |
| Magenta Marking Used                       | usage       | magentaMarker         | Number:Volume        |
| Yellow Marking Used                        | usage       | yellowMarker          | Number:Volume        |
| Black Pages Remaining                      | usage       | blackPagesRemaining   | Number               |
| Colour Pages Remaining                     | usage       | colorPagesRemaining   | Number               |
| Cyan Pages Remaining                       | usage       | cyanPagesRemaining    | Number               |
| Magenta Pages Remaining                    | usage       | magentaPagesRemaining | Number               |
| Yellow Pages Remaining                     | usage       | yellowPagesRemaining  | Number               |
| Total Number of Pages Printed Lifetime     | usage       | totalCount            | Number               |
| Total Number of Colour Pages Printed       | usage       | totalColorCount       | Number               |
| Total Number of Monochrome Pages Printed   | usage       | totalMonochromeCount  | Number               |
| Paper Jams                                 | usage       | jamEvents             | Number               |
| Missed Pick Events                         | usage       | mispickEvents         | Number               |
| Front Panel Cancel Count                   | usage       | fpCancelCount         | Number               |
| Subscription Count                         | usage       | subscriptionCount     | Number               |
| Scanner Document Feeder Count              | scanner     | totalAdf              | Number               |
| Scanner Flatbed Count                      | scanner     | totalFlatbed          | Number               |
| Scanner Paper Jams                         | scanner     | jamEvents             | Number               |
| Scanner Missed Picks                       | scanner     | mispickEvents         | Number               |
| Scan Document Feeder Count                 | scan        | totalAdf              | Number               |
| Scan Flatbed Count                         | scan        | totalFlatbed          | Number               |
| Scan to Email Count                        | scan        | totalToEmail          | Number               |
| Scan to Folder Count                       | scan        | totalToFolder         | Number               |
| Scan to Host Count                         | scan        | totalToHost           | Number               |
| Copy Document Feeder Count                 | copy        | totalAdf              | Number               |
| Copy Flatbed Count                         | copy        | totalFlatbed          | Number               |
| Copy Total Pages Count                     | copy        | totalCount            | Number               |
| Copy Total Colour Pages Count              | copy        | totalColorCount       | Number               |
| Copy Total Monochrome Pages Count          | copy        | totalMonochromeCount  | Number               |
| Windows Page Count                         | app         | totalWin              | Number               |
| Android Page Count                         | app         | totalAndroid          | Number               |
| iOS Page Count                             | app         | totalIos              | Number               |
| OSX Page Count                             | app         | totalOsx              | Number               |
| Samsung Page Count                         | app         | totalSamsung          | Number               |
| Chrome Page Count                          | app         | totalChrome           | Number               |
| Google Cloud Print Count                   | other       | cloudPrint            | Number               |

Note:

* All channels are dynamic and will not be added or visible in Paper UI if not supported.
* The word colour in channel names are American spelling ("color").
* The `colorLevel`, `colorMarkingUsed` and `colorPagesRemaining` channels are used on Printers that have only a single colour cartridge instead of separate Cyan, Magenta and Yellow cartridges.
* The `scanner` group is for the Scanning Engine which consists of Scan, Copy and other operations; the `scan` group is for scanner operations only.
* If no `status` group channels are selected, then those relevant data endpoints on the *Embedded Web Server* will not be polled for status information.
* Some channels are *Advanced channels* not selected by default in Paper UI. To add them, click the *Show More* button in the thing configuration. 

### Sample Items

```
String PrinterStatus "Status" { channel="pprinter:printer:djprinter:status#status" }

Number:Dimensionless PrinterBlackLevel "Black Level" { channel="hpprinter:printer:djprinter:ink#blackLevel" }
Number:Dimensionless PrinterCyanLevel "Cyan Level" { channel="hpprinter:printer:djprinter:ink#cyanLevel" }
Number:Dimensionless PrinterMagentaLevel "Magenta Level" { channel="hpprinter:printer:djprinter:ink#magentaLevel" }
Number:Dimensionless PrinterYellowLevel "Yellow Level" { channel="hpprinter:printer:djprinter:ink#yellowLevel" }

Number PrinterTotalPages "Total Pages" { channel="hpprinter:printer:djprinter:usage#totalCount" }
Number PrinterTotalColourPages "Total Colour Pages" { channel="hpprinter:printer:djprinter:usage#totalColorCount" }
Number PrinterTotalMonochromePages "Total Monochrome Pages" { channel="hpprinter:printer:djprinter:usage#totalMonochromeCount" }
```

### Sample Sitemap Items

Black Ink displayed as a whole percentage - `60 %`

```
Text item=hpprinter_printer_djprinter_ink_blackLevel label="Black [%.0f %unit%]"
```

Black Marker displayed in millilitres - `21 ml`  
*Default*

```
Text item=hpprinter_printer_djprinter_usage_blackMarker label="Black Marker [%.0f %unit%]"
```

Black Marker displayed in litres - `0.021 l`

```
Text item=hpprinter_printer_djprinter_usage_blackMarker label="Black Marker [%.3f l]"
```

Black Marker displayed in microlitres - `21000 µl`

```
Text item=hpprinter_printer_djprinter_usage_blackMarker label="Black Marker [%.0f µl]"
```

Scanner Document Feeder loaded with text status display - `ON` or `OFF`

```
Text item=hpprinter_printer_djprinter_status_scannerAdfLoaded label="ADF Loaded [%s]"
```

> If you want the *Scanner Document Feeder* or *Tray Empty/Open* channels to display a different message, you can use the *Map Transformations*. Search for `Map Transformation Service` in the openHAB documentation for more information.
