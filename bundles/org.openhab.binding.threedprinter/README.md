# 3D Printer Binding

This binding integrates FDM 3D printers into openHAB, allowing you to monitor print status, temperatures, and job progress, and to control prints (pause, resume, cancel). Klipper and OctoPrint also allow adjusting temperatures, print speed, and fan speed; PrusaLink exposes these as read-only, since Buddy firmware does not expose an API to change them (see [Channels](#channels)).

Three printer firmware/server platforms are supported:

- **PrusaLink** — Prusa printers with Buddy Board running Buddy firmware (MK4(s), Core ONE, XL, Mini+, etc.)
- **Klipper** — Klipper firmware printers accessed via the Moonraker REST API
- **OctoPrint** — Any printer managed by an OctoPrint server

## Tested Hardware

This binding has been tested against:

- **Prusa MK4** running PrusaLink server 2.1.2 / Buddy firmware 6.5.7+12836 (`prusa-link` thing type)
- **Prusa Core One** running PrusaLink server 2.1.2 / Buddy firmware 6.5.7+12836 (`prusa-link` thing type)
- **Snapmaker U1** running Klipper via Moonraker 1.4.1.6 (`klipper` thing type), including its 4 toolheads (see [Multi-toolhead printers](#multi-toolhead-printers))

Other Buddy-firmware Prusa printers (MK3.5, MINI+, XL) and other Klipper/Moonraker printers are expected to work the same way, since they expose the same PrusaLink v1 and Moonraker APIs respectively, but have not been verified by the author. The `octoprint` thing type has not been tested against a live OctoPrint server; it is expected to work against any standard OctoPrint installation (the print preview additionally requires the [PrusaSlicer Thumbnails](https://plugins.octoprint.org/plugins/prusaslicerthumbnails/) plugin).

## Supported Things

| Thing ID      | Description                                          |
|---------------|------------------------------------------------------|
| `prusa-link`  | Prusa printer via the PrusaLink v1 REST API          |
| `klipper`     | Klipper printer via the Moonraker REST API           |
| `octoprint`   | Printer managed by an OctoPrint server               |

## Discovery

Auto-discovery is not supported. Things must be added manually.

## Thing Configuration

### PrusaLink (`prusa-link`)

| Parameter         | Description                                                                     | Default | Required |
|-------------------|---------------------------------------------------------------------------------|---------|----------|
| `hostname`        | Hostname or IP address of the printer.                                          | –       | Yes      |
| `port`            | HTTP port of the PrusaLink API.                                                 | `80`    | No       |
| `apiKey`          | API key shown in the printer's settings menu under **Network → API Key**.       | –       | Yes      |
| `refreshInterval` | How often to poll the printer, in seconds.                                      | `30`    | No       |

### Klipper (`klipper`)

| Parameter         | Description                                                                     | Default | Required |
|-------------------|---------------------------------------------------------------------------------|---------|----------|
| `hostname`        | Hostname or IP address of the Moonraker server.                                 | –       | Yes      |
| `port`            | HTTP port of the Moonraker API.                                                 | `7125`  | No       |
| `apiKey`          | Moonraker API key. Optional when accessing from a trusted local network.        | –       | No       |
| `refreshInterval` | How often to poll the printer, in seconds.                                      | `30`    | No       |

### OctoPrint (`octoprint`)

| Parameter         | Description                                                                     | Default | Required |
|-------------------|---------------------------------------------------------------------------------|---------|----------|
| `hostname`        | Hostname or IP address of the OctoPrint server.                                 | –       | Yes      |
| `port`            | HTTP port of the OctoPrint API.                                                 | `5000`  | No       |
| `apiKey`          | OctoPrint API key from **Settings → API → Global API Key**.                     | –       | Yes      |
| `refreshInterval` | How often to poll the printer, in seconds.                                      | `30`    | No       |

## Channels

All three thing types expose the same set of channel IDs, but `nozzle-temperature-setpoint`, `bed-temperature-setpoint`, `print-speed`, and `fan-speed` are read-only on `prusa-link` things: PrusaLink on Buddy firmware does not expose an API to change these, so commands sent to them are ignored. On `klipper` and `octoprint` things, these four channels accept commands.

| Channel ID                    | Item Type            | R/W | Description                                                                                          |
|-------------------------------|----------------------|-----|------------------------------------------------------------------------------------------------------|
| `printer-state`               | String               | R   | Current printer state: `IDLE`, `PRINTING`, `PAUSED`, `FINISHED`, `ERROR`, or `BUSY`.                |
| `job-name`                    | String               | R   | Name of the file currently loaded or being printed.                                                  |
| `job-progress`                | Number:Dimensionless  | R   | Print completion percentage (0–100).                                                                 |
| `time-elapsed`                | Number:Time           | R   | Seconds elapsed since the print started.                                                             |
| `time-remaining`              | Number:Time           | R   | Estimated seconds remaining.                                                                         |
| `nozzle-temperature`          | Number:Temperature   | R   | Current nozzle (hotend) temperature.                                                                 |
| `nozzle-temperature-setpoint` | Number:Temperature   | RW* | Nozzle temperature target. Send a temperature to change it. Read-only on `prusa-link`.               |
| `bed-temperature`             | Number:Temperature   | R   | Current heated bed temperature.                                                                      |
| `bed-temperature-setpoint`    | Number:Temperature   | RW* | Bed temperature target. Send a temperature to change it. Read-only on `prusa-link`.                  |
| `print-speed`                 | Number:Dimensionless  | RW* | Print speed as a percentage of the configured profile speed (1–200). Read-only on `prusa-link`.      |
| `fan-speed`                   | Number:Dimensionless  | RW* | Part-cooling fan speed percentage (0–100) on `klipper`/`octoprint`. On `prusa-link` this channel is `Number:Frequency` (RPM) and read-only, since Buddy firmware reports raw fan RPM and the maximum RPM varies by printer model. |
| `pause-resume`                | Switch               | RW  | `ON` when the print is paused. Send `ON` to pause, `OFF` to resume.                                 |
| `cancel`                      | Switch               | W   | Send `ON` to cancel the current print. Resets to `OFF` automatically.                               |
| `job-preview`                 | Image                | R   | Thumbnail image of the object being printed. Only populated when the sliced file contains embedded thumbnails. For OctoPrint, requires the [PrusaSlicer Thumbnails](https://plugins.octoprint.org/plugins/prusaslicerthumbnails/) plugin. |

### Multi-toolhead printers

Both `klipper` and `octoprint` things automatically detect additional toolheads and add extra channels for them; `prusa-link` does not, since PrusaLink's status API only ever reports one active nozzle even on tool-changer machines like the Prusa XL.

The added channels follow the same pattern on both platforms: `nozzle-temperature-N` / `nozzle-temperature-setpoint-N`, where `N` is the tool number. The primary toolhead is tool 1 and is exposed as the regular `nozzle-temperature`/`nozzle-temperature-setpoint` channels; the second toolhead is tool 2, the third is tool 3, and so on, with no fixed upper limit. These extra channels are not listed in the table above since they only appear on things with more than one toolhead; add them to your `.items` file the same way as the primary nozzle channels, substituting the tool number, e.g. `threedprinter:klipper:voron:nozzle-temperature-2`.

- **Klipper**: additional toolheads are configured as `extruder1`, `extruder2`, ... alongside the primary `extruder`. The binding queries Moonraker once at startup to discover how many the printer actually reports.
- **OctoPrint**: additional toolheads appear as `tool1`, `tool2`, ... in the same `temperature` object already polled every cycle (per the printer profile's configured extruder count), so no extra discovery request is needed.

## Full Example

### Things

`threedprinter.things`

```java
Thing threedprinter:prusa-link:mk4 "Prusa MK4" [
    hostname="192.168.1.50",
    apiKey="xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
    refreshInterval=15
]

Thing threedprinter:klipper:voron "Voron 2.4" [
    hostname="voron.local",
    port=7125,
    refreshInterval=10
]

Thing threedprinter:octoprint:ender "Ender 3 (OctoPrint)" [
    hostname="octopi.local",
    apiKey="xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
    refreshInterval=20
]
```

### Items

`threedprinter.items`

```java
String   Voron_State          "Printer state [%s]"             { channel="threedprinter:klipper:voron:printer-state" }
String   Voron_JobName        "Current job [%s]"               { channel="threedprinter:klipper:voron:job-name" }
Number:Dimensionless Voron_Progress       "Progress [%.1f %%]"    { channel="threedprinter:klipper:voron:job-progress" }
Number:Time Voron_TimeElapsed    "Time elapsed [%d %unit%]"      { channel="threedprinter:klipper:voron:time-elapsed" }
Number:Time Voron_TimeRemaining  "Time remaining [%d %unit%]"    { channel="threedprinter:klipper:voron:time-remaining" }
Number:Temperature Voron_NozzleTemp    "Nozzle temp [%.1f %unit%]"  { channel="threedprinter:klipper:voron:nozzle-temperature" }
Number:Temperature Voron_NozzleTarget  "Nozzle target [%.1f %unit%]" { channel="threedprinter:klipper:voron:nozzle-temperature-setpoint" }
Number:Temperature Voron_BedTemp       "Bed temp [%.1f %unit%]"     { channel="threedprinter:klipper:voron:bed-temperature" }
Number:Temperature Voron_BedTarget     "Bed target [%.1f %unit%]"   { channel="threedprinter:klipper:voron:bed-temperature-setpoint" }
Number:Dimensionless Voron_PrintSpeed     "Print speed [%d %%]"  { channel="threedprinter:klipper:voron:print-speed" }
Number:Dimensionless Voron_FanSpeed       "Fan speed [%d %%]"    { channel="threedprinter:klipper:voron:fan-speed" }
Switch   Voron_PauseResume    "Paused"                         { channel="threedprinter:klipper:voron:pause-resume" }
Switch   Voron_Cancel         "Cancel print"                   { channel="threedprinter:klipper:voron:cancel" }
Image    Voron_Preview        "Print preview"                  { channel="threedprinter:klipper:voron:job-preview" }
```

### Sitemap

`threedprinter.sitemap`

```perl
sitemap threedprinter label="3D Printers" {
    Frame label="Voron 2.4" {
        Text  item=Voron_State
        Text  item=Voron_JobName
        Text  item=Voron_Progress
        Text  item=Voron_TimeElapsed
        Text  item=Voron_TimeRemaining
        Text  item=Voron_NozzleTemp
        Setpoint item=Voron_NozzleTarget minValue=0 maxValue=300 step=5
        Text  item=Voron_BedTemp
        Setpoint item=Voron_BedTarget minValue=0 maxValue=120 step=5
        Slider item=Voron_PrintSpeed minValue=1 maxValue=200 step=10
        Slider item=Voron_FanSpeed minValue=0 maxValue=100 step=5
        Switch item=Voron_PauseResume label="Pause/Resume"
        Switch item=Voron_Cancel label="Cancel Print"
        Image  item=Voron_Preview
    }
}
```
