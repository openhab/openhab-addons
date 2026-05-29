# 3D Printer Binding

This binding integrates FDM 3D printers into openHAB, allowing you to monitor print status, temperatures, and job progress, and to control prints (pause, resume, cancel) and adjust temperatures, print speed, and fan speed.

Three printer firmware/server platforms are supported:

- **PrusaLink** — Prusa printers running PrusaLink firmware (MK4, XL, Mini+, etc.)
- **Klipper** — Klipper firmware printers accessed via the Moonraker REST API
- **OctoPrint** — Any printer managed by an OctoPrint server

## Supported Things

| Thing ID      | Description                                          |
|---------------|------------------------------------------------------|
| `prusaprinter`| Prusa printer via the PrusaLink v1 REST API          |
| `klipper`     | Klipper printer via the Moonraker REST API           |
| `octoprint`   | Printer managed by an OctoPrint server               |

## Discovery

Auto-discovery is not supported. Things must be added manually.

## Thing Configuration

### PrusaLink (`prusaprinter`)

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

All three thing types expose the same set of channels.

| Channel ID                    | Item Type            | R/W | Description                                                                                          |
|-------------------------------|----------------------|-----|------------------------------------------------------------------------------------------------------|
| `printer-state`               | String               | R   | Current printer state: `IDLE`, `PRINTING`, `PAUSED`, `FINISHED`, `ERROR`, or `BUSY`.                |
| `job-name`                    | String               | R   | Name of the file currently loaded or being printed.                                                  |
| `job-progress`                | Number               | R   | Print completion percentage (0–100).                                                                 |
| `time-elapsed`                | Number               | R   | Seconds elapsed since the print started.                                                             |
| `time-remaining`              | Number               | R   | Estimated seconds remaining.                                                                         |
| `nozzle-temperature`          | Number:Temperature   | R   | Current nozzle (hotend) temperature.                                                                 |
| `nozzle-temperature-setpoint` | Number:Temperature   | RW  | Nozzle temperature target. Send a temperature to change it.                                          |
| `bed-temperature`             | Number:Temperature   | R   | Current heated bed temperature.                                                                      |
| `bed-temperature-setpoint`    | Number:Temperature   | RW  | Bed temperature target. Send a temperature to change it.                                             |
| `print-speed`                 | Number               | RW  | Print speed as a percentage of the configured profile speed (0–200). Read support varies by firmware.|
| `fan-speed`                   | Number               | RW  | Part-cooling fan speed percentage (0–100). Read support varies by firmware.                          |
| `pause-resume`                | Switch               | RW  | `ON` when the print is paused. Send `ON` to pause, `OFF` to resume.                                 |
| `cancel`                      | Switch               | W   | Send `ON` to cancel the current print. Resets to `OFF` automatically.                               |
| `job-preview`                 | Image                | R   | Thumbnail image of the object being printed. Only populated when the sliced file contains embedded thumbnails. For OctoPrint, requires the [PrusaSlicer Thumbnails](https://plugins.octoprint.org/plugins/prusaslicerthumbnails/) plugin. |

## Full Example

### Things

`threedprinter.things`

```java
Thing threedprinter:prusaprinter:mk4 "Prusa MK4" [
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
String   MK4_State          "Printer state [%s]"             { channel="threedprinter:prusaprinter:mk4:printer-state" }
String   MK4_JobName        "Current job [%s]"               { channel="threedprinter:prusaprinter:mk4:job-name" }
Number   MK4_Progress       "Progress [%.1f %%]"             { channel="threedprinter:prusaprinter:mk4:job-progress" }
Number   MK4_TimeElapsed    "Time elapsed [%d s]"            { channel="threedprinter:prusaprinter:mk4:time-elapsed" }
Number   MK4_TimeRemaining  "Time remaining [%d s]"          { channel="threedprinter:prusaprinter:mk4:time-remaining" }
Number:Temperature MK4_NozzleTemp    "Nozzle temp [%.1f %unit%]"  { channel="threedprinter:prusaprinter:mk4:nozzle-temperature" }
Number:Temperature MK4_NozzleTarget  "Nozzle target [%.1f %unit%]" { channel="threedprinter:prusaprinter:mk4:nozzle-temperature-setpoint" }
Number:Temperature MK4_BedTemp       "Bed temp [%.1f %unit%]"     { channel="threedprinter:prusaprinter:mk4:bed-temperature" }
Number:Temperature MK4_BedTarget     "Bed target [%.1f %unit%]"   { channel="threedprinter:prusaprinter:mk4:bed-temperature-setpoint" }
Number   MK4_PrintSpeed     "Print speed [%d %%]"            { channel="threedprinter:prusaprinter:mk4:print-speed" }
Number   MK4_FanSpeed       "Fan speed [%d %%]"              { channel="threedprinter:prusaprinter:mk4:fan-speed" }
Switch   MK4_PauseResume    "Paused"                         { channel="threedprinter:prusaprinter:mk4:pause-resume" }
Switch   MK4_Cancel         "Cancel print"                   { channel="threedprinter:prusaprinter:mk4:cancel" }
Image    MK4_Preview        "Print preview"                  { channel="threedprinter:prusaprinter:mk4:job-preview" }
```

### Sitemap

`threedprinter.sitemap`

```perl
sitemap threedprinter label="3D Printers" {
    Frame label="Prusa MK4" {
        Text  item=MK4_State
        Text  item=MK4_JobName
        Text  item=MK4_Progress
        Text  item=MK4_TimeElapsed
        Text  item=MK4_TimeRemaining
        Text  item=MK4_NozzleTemp
        Setpoint item=MK4_NozzleTarget minValue=0 maxValue=300 step=5
        Text  item=MK4_BedTemp
        Setpoint item=MK4_BedTarget minValue=0 maxValue=120 step=5
        Slider item=MK4_PrintSpeed minValue=0 maxValue=200 step=10
        Slider item=MK4_FanSpeed minValue=0 maxValue=100 step=5
        Switch item=MK4_PauseResume label="Pause/Resume"
        Switch item=MK4_Cancel label="Cancel Print"
        Image  item=MK4_Preview
    }
}
```
