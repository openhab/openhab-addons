# Shelly Light Device Overview

Feature/profile comparison for all light-capable devices supported by the binding, grouped by hardware family. Values are taken from the current channel definitions and thing-type XML, not just the README tables (a few README rows are stale/incomplete compared to the actual code).

## RGBW2 (Gen1 LED Strip Controller)

| Device                    | Thing-type          | Profile | Color capability                     | Color temp range | Meters                        | Effects | Notes                                                                      |
| ------------------------- | ------------------- | ------- | ------------------------------------ | ---------------- | ----------------------------- | ------- | -------------------------------------------------------------------------- |
| Shelly RGBW2 (Color Mode) | `shellyrgbw2-color` | Color   | RGB + W (4 combined LED outputs)     | — (no CCT)       | 1 (aggregate)                 | 4 (0–3) | Selected via device firmware setting; requires re-discovery to switch mode |
| Shelly RGBW2 (White Mode) | `shellyrgbw2-white` | White   | 4× independent white dimmer channels | — (no CCT)       | 1 (aggregate, all 4 channels) | —       | `channel1`–`channel4` deprecated, replaced by `light1`–`light4`            |

### Channel groups & channels — RGBW2

| Profile                     | Group                                     | Channels                                                                                                         |
| --------------------------- | ----------------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| Color (`shellyrgbw2-color`) | `control`                                 | `power`, `autoOn`, `autoOff`, `timerActive`                                                                      |
|                             | `color`                                   | `hsb`, `full`, `red`, `green`, `blue`, `white`, `gain`, `effect`                                                 |
| White (`shellyrgbw2-white`) | `light1`–`light4` (one group per channel) | `brightness`, `autoOn`, `autoOff`, `timerActive` (no `power` — brightness `0` = off)                             |
|                             | `channel1`–`channel4`                     | deprecated mirror of `light1`–`light4`, only present on Things discovered before the `light1`–`light4` migration |

No CCT/`temperature` channel in either profile — RGBW2 has no color-temperature component. `control` is declared but carries no per-channel content in White mode (the per-light channels live in `light1`–`light4` instead).

## Shelly Plus RGBW PM

| Device              | Thing-type         | Profile                     | Color capability                                         | Color temp range | Meters               | Effects | Notes                                                                                |
| ------------------- | ------------------ | --------------------------- | -------------------------------------------------------- | ---------------- | -------------------- | ------- | ------------------------------------------------------------------------------------ |
| Shelly Plus RGBW PM | `shellyplusrgbwpm` | `rgbw` / `rgb` (color mode) | RGB + W (combined)                                       | — (no CCT)       | 1 (aggregate)        | —       | Profile picked in Shelly App; changing it requires deleting/re-discovering the Thing |
|                     | `shellyplusrgbwpm` | `light` (white mode)        | 4× independent white dimmer channels (`light1`–`light4`) | — (no CCT)       | 1 (shared/aggregate) | —       | Same Thing, different firmware profile                                               |

### Channel groups & channels — Shelly Plus RGBW PM

| Profile        | Group                                     | Channels                                                              |
| -------------- | ----------------------------------------- | --------------------------------------------------------------------- |
| `rgbw` / `rgb` | `control`                                 | `power`, `autoOn`, `autoOff`, `timerActive`                           |
|                | `color`                                   | `hsb`, `full`, `red`, `green`, `blue`, `white` (no `gain`/`effect`)   |
| `light`        | `light1`–`light4` (one group per channel) | `brightness`, `autoOn`, `autoOff`, `timerActive` (no `power`, no CCT) |

## Shelly Pro RGBWW PM

| Device              | Thing-type         | Profile      | Color capability                                                            | Color temp range              | Meters                                              | Effects | Notes                                                                            |
| ------------------- | ------------------ | ------------ | --------------------------------------------------------------------------- | ----------------------------- | --------------------------------------------------- | ------- | -------------------------------------------------------------------------------- |
| Shelly Pro RGBWW PM | `shellyprorgbwwpm` | `rgbcct`     | RGB (color group) + 1 independent CCT channel (`light1`)                    | 3000–6500K (CCT channel only) | 2 (`meter1` RGB, `meter2` CCT)                      | —       | Aggregated `device#accumulatedPower`/`totalEnergy` channels added since >1 meter |
|                     | `shellyprorgbwwpm` | `rgbx2light` | RGB (color group) + 2 independent plain-dimmer channels (`light1`,`light2`) | — (no CCT)                    | 3 (`meter1`–`meter3`)                               | —       | Aggregated device meter channels added                                           |
|                     | `shellyprorgbwwpm` | `cctx2`      | 2× independent CCT channels (`light1`,`light2`), no RGB                     | 3000–6500K (each channel)     | 2 (`meter1`,`meter2`)                               | —       | No color/RGB component in this profile                                           |
|                     | `shellyprorgbwwpm` | `light`      | Up to 5 independent plain-dimmer channels (`light1`–`light5`)               | — (no CCT)                    | Up to 5 (`meter1`–`meter5`, per configured channel) | —       | Plain on/off + brightness per channel, no color/CCT                              |

### Channel groups & channels — Shelly Pro RGBWW PM

| Profile      | Group                                                | Channels                                                                                                                                                             |
| ------------ | ---------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `rgbcct`     | `control`                                            | `power`, `autoOn`, `autoOff`, `timerActive`                                                                                                                          |
|              | `color`                                              | `hsb`, `full`, `red`, `green`, `blue` (no `white`/`gain`/`effect`)                                                                                                   |
|              | `light1` (CCT component)                             | `brightness`, `temperature` (`whiteTemp`, 3000–6500K), `autoOn`, `autoOff`, `timerActive` (no `power`)                                                               |
| `rgbx2light` | `control`                                            | `power`, `autoOn`, `autoOff`, `timerActive`                                                                                                                          |
|              | `color`                                              | `hsb`, `full`, `red`, `green`, `blue`                                                                                                                                |
|              | `light1`, `light2`                                   | `brightness`, `autoOn`, `autoOff`, `timerActive` (no `power`, no CCT)                                                                                                |
| `cctx2`      | `light1`, `light2`                                   | `brightness`, `temperature` (`whiteTemp`, 3000–6500K), `autoOn`, `autoOff`, `timerActive` (no `power`) — no `control`/`color` group used, there's no color component |
| `light`      | `light1`–`light5` (one group per configured channel) | `brightness`, `autoOn`, `autoOff`, `timerActive` (no `power`, no CCT)                                                                                                |

## Gen1 Bulbs

| Device                       | Thing-type        | Profile                          | Color capability                  | Color temp range      | Meters             | Effects | Notes                                                                          |
| ---------------------------- | ----------------- | -------------------------------- | --------------------------------- | --------------------- | ------------------ | ------- | ------------------------------------------------------------------------------ |
| Shelly Bulb                  | `shellybulb`      | Color or White (mode-switchable) | RGB + W (color) / CCT (white)     | 3000–6500K            | 0 (no power meter) | 7 (0–6) | Original E27 RGBW bulb; only Gen1 bulb without a meter                         |
| Shelly Duo                   | `shellybulbduo`   | White only                       | CCT only, no RGB                  | 2700–6500K            | 1                  | —       | Also covers the Duo White G10 socket variant                                   |
| Shelly Vintage               | `shellyvintage`   | White only                       | Fixed warm-white, brightness only | — (no adjustable CCT) | 1                  | —       | No color temperature control — fixed vintage-style tone                        |
| Shelly Duo RGBW / Color Bulb | `shellycolorbulb` | Color or White (mode-switchable) | RGB + W (color) / CCT (white)     | 2700–6500K            | 1                  | 4 (0–3) | `color`/`white` channel groups are mutually exclusive depending on active mode |

### Channel groups & channels — Gen1 Bulbs

| Device                       | Group     | Channels                                                               |
| ---------------------------- | --------- | ---------------------------------------------------------------------- |
| Shelly Bulb                  | `control` | `power`, `mode`                                                        |
|                              | `color`   | `hsb`, `full`, `red`, `green`, `blue`, `white`, `gain`, `effect`       |
|                              | `white`   | `brightness`, `temperature` (`whiteTemp`, 3000–6500K)                  |
| Shelly Duo                   | `control` | `autoOn`, `autoOff`, `timerActive` (no `power` — brightness `0` = off) |
|                              | `white`   | `brightness`, `temperature` (`whiteTempDuo`, 2700–6500K)               |
| Shelly Vintage               | `control` | `autoOn`, `autoOff`, `timerActive` (no `power`)                        |
|                              | `white`   | `brightness` only — no `temperature` channel (fixed warm-white tone)   |
| Shelly Duo RGBW / Color Bulb | `control` | `power`, `autoOn`, `autoOff`, `timerActive`                            |
|                              | `color`   | `hsb`, `full`, `red`, `green`, `blue`, `white`, `gain`, `effect`       |
|                              | `white`   | `brightness`, `temperature` (`whiteTempDuo`, 2700–6500K)               |

## Gen2+ Bulbs (Gen3, RPC API)

| Device                          | Thing-type            | Profile                              | Color capability                                        | Color temp range | Meters | Effects | Notes                                                                                                                                                                                                                     |
| ------------------------------- | --------------------- | ------------------------------------ | ------------------------------------------------------- | ---------------- | ------ | ------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Shelly Duo Bulb E27 Gen3        | `shellyplusduobulb`   | White only                           | CCT only, no RGB                                        | 2700–6500K       | 0      | —       | No separate power channel — brightness 0 turns it off                                                                                                                                                                     |
| Shelly Multicolor Bulb E27 Gen3 | `shellypluscolorbulb` | Color and White (both always active) | RGB (color group, no white channel) + CCT (white group) | 2700–6500K       | 0      | —       | Unlike Gen1 Duo Color Bulb, `color` and `white` groups are both live simultaneously; switching between them is driven by which channel is written (`color#full`/`white#temperature`), there's no dedicated `mode` channel |

### Channel groups & channels — Gen2+ Bulbs

| Device                          | Group     | Channels                                                                                                                                                         |
| ------------------------------- | --------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Shelly Duo Bulb E27 Gen3        | `control` | `autoOn`, `autoOff`, `timerActive` (no `power` — brightness `0` = off)                                                                                           |
|                                 | `white`   | `brightness`, `temperature` (`whiteTempDuo`, 2700–6500K)                                                                                                         |
| Shelly Multicolor Bulb E27 Gen3 | `control` | `autoOn`, `autoOff`, `timerActive` (no `power`, even though the color component would otherwise qualify — deliberately excluded for this Gen3 Duo-family device) |
|                                 | `color`   | `hsb`, `full`, `red`, `green`, `blue` (no `white`/`gain`/`effect`)                                                                                               |
|                                 | `white`   | `brightness`, `temperature` (`whiteTempDuo`, 2700–6500K)                                                                                                         |

## Switching between Color and White mode

Which mechanism switches a device between its color and white capability depends on the device family — only two families support it live, from openHAB:

| Device                                                  | Mechanism                                                                                                                                                                                                                                                                                                  |
| ------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Shelly Bulb (`shellybulb`)                              | Dedicated `control#mode` Switch channel (ON = color, OFF = white), or implicitly by writing `color#full` with a color word (`"white"` switches to white mode, any other color word switches to color mode) — takes effect immediately, no re-discovery.                                                    |
| Shelly Multicolor Bulb E27 Gen3 (`shellypluscolorbulb`) | No dedicated mode channel. Writing `color#full` (a color word) or `white#temperature` implicitly switches mode — takes effect immediately, no re-discovery. Both `color` and `white` channel groups stay populated regardless of the active mode.                                                          |
| Shelly Duo RGBW / Color Bulb (`shellycolorbulb`)        | **Not switchable from openHAB.** The `color`/`white` channels exist, but the binding never sends a mode-switch request for this device — mode must be changed on the device itself (Shelly app/firmware setting), then the Thing must be deleted and re-discovered so the correct channel group populates. |
| Shelly RGBW2 (`shellyrgbw2-color`/`shellyrgbw2-white`)  | Not switchable at all — color vs. white are two separate Thing-types, matching a device-level firmware setting. Change it on the device, then delete and re-discover the Thing.                                                                                                                            |
| Shelly Plus RGBW PM (`shellyplusrgbwpm`)                | Not switchable from openHAB — `rgbw`/`rgb` vs. `light` is a firmware profile selected in the Shelly App. Change it there, then delete and re-discover the Thing.                                                                                                                                           |
| Shelly Pro RGBWW PM (`shellyprorgbwwpm`)                | Not switchable from openHAB — `rgbcct`/`cctx2`/`rgbx2light`/`light` is a fixed firmware profile selected in the Shelly App. Change it there, then delete and re-discover the Thing.                                                                                                                        |

## Cross-cutting notes

- All devices in this table support `autoOn`/`autoOff` timer channels and a `timerActive` indicator — omitted from the tables above since it's universal.
- "Meters" counts distinct meter channel groups exposed by the binding, not physical power-metering ICs.
- Color temperature ranges come from the binding's channel definitions (`whiteTemp` = 3000–6500K, `whiteTempDuo` = 2700–6500K in `ShellyChannelDefinitions`/`shellyGen1_lights.xml`), not the README's per-device tables, which have some stale/incomplete rows (e.g. `shellycolorbulb`'s white/CCT group and the Pro RGBWW PM `cctx2` range).
- Every device also exposes a `device` channel group (standard status channels — uptime, heartbeat, update-available, etc., identical across all light devices) and, where noted in the "Meters" column, one or more `meter`/`meter1`–`meter5` groups (`currentPower`, `totalEnergy`, energy-history channels, `lastUpdate`). Both are omitted from the per-device channel-group tables above since they're not part of the color profile itself.
- Group naming for multi-component devices (RGBW2, Plus RGBW PM, Pro RGBWW PM): a single/color component always lives in the bare `control` group; additional independent components are numbered `light1`, `light2`, ... in device order, with the color component (if present) implicitly occupying "slot 0" so the numbering of the remaining components starts at 1 regardless of whether a color component exists.
