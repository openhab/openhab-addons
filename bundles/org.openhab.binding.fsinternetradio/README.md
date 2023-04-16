# FS Internet Radio Binding

This binding integrates internet radios based on the [Frontier Silicon chipset](https://www.frontier-silicon.com/).

## Supported Things

Successfully tested are internet radios:

- [Hama IR100, IR110](https://de.hama.com/00054823/hama-internetradio-ir110)
- [Hama DIR3100](https://www.conrad.com/p/hama-dir3100-internet-desk-radio-dab-fm-aux-internet-radio-usb-spotify-black-1233624)
- [Medion MD87180, MD86988, MD86955, MD87528](http://internetradio.medion.com/)
- [Silvercrest SMRS18A1, SMRS30A1, SMRS35A1, SIRD 14 C2, SIRD 14 D1](https://www.silvercrest-multiroom.de/en/products/stereo-internet-radio/)
- [Roberts Stream 83i and 93i](https://www.robertsradio.com/uk/products/radio/smart-radio/)
- [Auna Connect 150, Auna KR200, Auna Connect CD](https://www.auna.de/Radios/Internetradios/)
- [TechniSat DIGITRADIO 350 IR and 850](https://www.technisat.com/en_XX/DAB+-Radios-with-Internetradio/352-10996/)
- [TechniSat VIOLA 2 C IR](https://www.technisat.com/de_DE/VIOLA-2-C-IR/352-10996-22713/?article=0010/3933)
- [TTMicro AS Pinell Supersound](https://www.ttmicro.no/radio)
- [Revo SuperConnect](https://revo.co.uk/products/)
- [Sangean WFR-28C](https://sg.sangean.com.tw/products/product_category.asp?cid=2)
- [Roku SoundBridge M1001](https://soundbridge.roku.com/soundbridge/index.php)
- [Dual IR 3a](https://www.dual.de/produkte/digitalradio/radio-station-ir-3a/)
- [Teufel 3sixty](https://www.teufel.de/stereo/radio-3sixty-p16568.html)
- [Ruark R5](https://www.ruarkaudio.com/products/r5-high-fidelity-music-system)

But in principle, all internet radios based on the [Frontier Silicon chipset](https://www.frontier-silicon.com/) should be supported because they share the same API.
So it is very likely that other internet radio models of the same manufacturers do also work.

## Community

For discussions and questions about supported radios, check out [this thread](https://community.openhab.org/t/internet-radio-i-need-your-help/2131).

## Discovery

The radios are discovered through UPnP in the local network.

If your radio is not discovered, please try to access its API via: `http://<radio-ip>/fsapi/CREATE_SESSION?pin=1234` (1234 is default pin, if you get a 403 error, check the radio menu for the correct pin).<br/>
If you get a 404 error, maybe a different port than the standard port 80 is used by your radio; try scanning the open ports of your radio.<br/>
If you get a result like `FS_OK 1902014387`, your radio is supported.

If this is the case, please [add your model to this documentation](https://github.com/openhab/openhab-addons/edit/main/bundles/org.openhab.binding.fsinternetradio/README.md) and/or provide discovery information in [this thread](https://community.openhab.org/t/internet-radio-i-need-your-help/2131).

## Binding Configuration

The binding itself does not need a configuration.

## Thing Configuration

Each radio must be configured via its ip address, port, pin, and a refresh rate.

- If the ip address is not discovered automatically, it must be manually set.
- The default port is `80` which should work for most radios.
- The default pin is `1234` for most radios, but if it does not work or if it was changed, look it up in the on-screen menu of the radio.
- The default refresh rate for the radio items is `60` seconds; `0` disables periodic refresh.

## Channels

All devices support some of the following channels:

| Channel Type ID | Item Type | Description | Access |
|-----------------|-----------|-------------|------- |
| power | Switch | Switch the radio on or off | R/W |
| volume-percent | Dimmer | Radio volume (min=0, max=100) | R/W |
| volume-absolute | Number | Radio volume (min=0, max=32) | R/W |
| mute | Switch | Mute the radio | R/W |
| mode | Number | The radio mode, e.g. FM radio, internet radio, AUX, etc. (model-specific, see list below) | R/W |
| preset | Number | Preset radio stations configured in the radio (write-only) | W |
| play-info-name | String | The name of the current radio station or track | R |
| play-info-text | String | Additional information e.g. of the current radio station | R |

The radio mode depends on the internet radio model (and its firmware version!).
This list is just an example how the mapping looks like for some of the devices, please try it out and adjust your sitemap for your particular radio.

| Radio Mode               | 0              | 1                       | 2            | 3            | 4         | 5        | 6            | 7            | 8         | 9         | 10     | 11     | 12     | 13     |
|--------------------------|----------------|-------------------------|--------------|--------------|-----------|----------|--------------|--------------|-----------|-----------|--------|--------|--------|--------|
| Hama IR100               | Internet Radio | Spotify                 | Music Player | AUX in       | -         | -        | -            | -            | -          | -         |-       | - | - | - |
| Hama IR110               | Internet Radio | Spotify                 | Music Player | AUX in       | -         | -        | -            | -            | -          | -         |-       | - | - | - |
| Hama DIR3100             | Internet Radio | Spotify                 | -            | Music Player | DAB Radio | FM Radio  | AUX in      | -            | -          | -          | -     | - | - | - |
| Medion MD87180           | Internet Radio | Music Player (USB, LAN) | DAB Radio    | FM Radio     | AUX in    | -        | -            | -            | -          | -         |-       | - | - | - |
| Medion MD 86988          | Internet Radio | Music Player            | FM Radio     | AUX in       | -         | -        | -            | -            | -          | -         |-       | - | - | - |
| Technisat DigitRadio 580 | Internet Radio | Spotify                 | -            | Music Player | DAB Radio | FM Radio | AUX in       | CD           | Bluetooth | -         |-        | - | - | - |
| Technisat VIOLA 2 C IR   | Internet Radio | Podcasts                | DAB Radio    | FM Radio     | -         | -        | -            |              |           | -         |-        | - | - | - |
| Dual IR 3a               | Internet Radio | Spotify                 | -            | Music Player | DAB Radio | FM Radio | Bluetooth    | -            | -          | -         |-       | - | - | - |
| Silvercrest SIRD 14 C1   | -              | Napster                 | Deezer       | Qobuz        | Spotify   | TIDAL    | Spotify      | Music Player | DAB Radio | FM Radio  | AUX in | - |  - | - |
| Silvercrest SIRD 14 C2   | Internet Radio | TIDAL                   | Deezer       | Qobuz        | Spotify   | -        | Music Player | DAB Radio    | FM Radio  | AUX in    |-       | - | - | - |
| Auna KR200 Kitchen Radio | Internet Radio | Spotify                 | -            | Music Player | DAB Radio | FM Radio | AUX in       | -            | -          | -         |-       | - | - | - |
| Auna Connect CD          | Internet Radio | Spotify                 | -            | Music Player | DAB Radio | FM Radio | CD           | Bluetooth    | AUX in    | -         | -      | - | - | - |
| Teufel 3sixty            | Internet Radio | Spotify                 | -            | USB/Network  | DAB Radio | FM Radio | Bluetooth    | AUX in       | -          | -         | -      | - | - | - |
| Ruark R5                 | Internet Radio | TIDAL                   | Deezer       | Amazon Music | Spotify   | Local Music | Music Player | DAB Radio | FM Radio   | Bluetooth | AUX in  | Phono | Optical | CD |

## Full Example

demo.things:

```java
fsinternetradio:radio:radioInKitchen [ ip="192.168.0.42" ]
```

demo.items:

```java
Switch RadioPower "Radio Power" { channel="fsinternetradio:radio:radioInKitchen:power" }
Switch RadioMute "Radio Mute" { channel="fsinternetradio:radio:radioInKitchen:mute" }
Dimmer RadioVolume "Radio Volume" { channel="fsinternetradio:radio:radioInKitchen:volume-percent" }
Number RadioMode "Radio Mode" { channel="fsinternetradio:radio:radioInKitchen:mode" }
Number RadioPreset "Radio Stations" { channel="fsinternetradio:radio:radioInKitchen:preset" }
String RadioInfo1 "Radio Info1" { channel="fsinternetradio:radio:radioInKitchen:play-info-name" }
String RadioInfo2 "Radio Info2" { channel="fsinternetradio:radio:radioInKitchen:play-info-text" }
```

demo.sitemap:

```perl
sitemap demo label="Main Menu"
{
    Frame {
        Switch item=RadioPower
        Slider visibility=[RadioPower==ON] item=RadioVolume
        Switch visibility=[RadioPower==ON] item=RadioMute
        Selection visibility=[RadioPower==ON] item=RadioPreset mappings=[0="Favourit 1", 1="Favourit 2", 2="Favourit 3", 3="Favourit 4"]
        Selection visibility=[RadioPower==ON] item=RadioMode mappings=[0="Internet Radio", 1="Musik Player", 2="DAB", 3="FM", 4="AUX"]
        Text visibility=[RadioPower==ON] item=RadioInfo1
        Text visibility=[RadioPower==ON] item=RadioInfo2
    }
}
```
