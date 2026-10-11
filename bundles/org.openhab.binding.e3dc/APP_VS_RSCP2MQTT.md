# My E3/DC App vs. rscp2mqtt - Functionality Cross Check

Comparison of the functionality offered by the E3/DC mobile app _My E3/DC_ (decompiled) and the
functionality delivered by [rscp2mqtt](https://github.com/pvtom/rscp2mqtt).

## Sources Inspected

| Source | What was inspected |
| --- | --- |
| `My+E3_DC_1.0.16_APKPure.xapk` | Decompiled with jadx; manifest, `assets/app.config`, native sources, Hermes JS bundle string table |
| `C:\Projects\rscp2mqtt` | `RscpMqttMain.cpp`, `RscpMqttMapping.h`, `RscpTags.h`, `README.md`, `TOPICS.md`, `CONFIG.md`, `WALLBOX.md`, `NEWTAGS.md` |

## Key Finding: The App Is a Shell, the Portal Holds the Logic

The decompiled app contains almost no domain logic. Its own code consists of:

- a React Native / Expo host (`com.hagerenergy.e3dc`, `MainActivity extends ReactActivity`),
- a WebView layer (`RNCWebView`, `useWebViewLogic`, `WebViewContainer`, `webViewKey`),
- an authentication path (`SSO_LOGIN_URL = https://auth.hagerenergy.com`, `LOGIN_URL = https://my.e3dc.com/login`,
  `WEBVIEW_URI = https://my.e3dc.com`, `login.hagerenergy.com`, `login.stg.hager.com`, `[?&]token=`, `sessionType`),
- OS integration (push notifications, secure store, orientation lock, sharing),
- third-party boilerplate (React Navigation, SVG, Fresco, ML Kit barcode models, OkHttp, BouncyCastle).

The current UI is loaded remotely through `expo-updates` (`https://u.expo.dev/...`), and the visible
application is the portal `https://my.e3dc.com` rendered in the WebView. **The app therefore exercises
the E3/DC cloud portal (REST/AJAX + WebSocket), not the local RSCP interface of the device.**

Conversely, rscp2mqtt is a headless bridge that talks the binary RSCP protocol to the device on the
local network (TCP port 5033, Rijndael-256 encrypted) and republishes everything as MQTT topics.

## API Coverage per Functionality

Legend for the API column:

- **RSCP** - the functionality is available over the local binary RSCP protocol (used by rscp2mqtt).
- **Cloud** - the functionality is served by the E3/DC cloud portal (HTTPS/AJAX and WebSocket).
- **RSCP + Cloud** - both paths exist, with equal or near-equal coverage.

The portal data path is observable through the public `python-e3dc` client: login is
`POST https://s10.e3dc.com/s10/phpcmd/cmd.php` with `DO=LOGIN`, followed by `DO=GETCONTENT` and the
live poll `DO=LIVEUNITDATA`, which returns a flat JSON document (see
[jhubig/python-e3dc](https://github.com/jhubig/python-e3dc)).

| # | Functionality | My E3/DC App 1.0.16 | rscp2mqtt | API / Interface |
| --- | --- | --- | --- | --- |
| 1 | Local device connection | Not offered (never contacts the device directly) | Core feature: TCP 5033, user + RSCP password, Rijndael-256 AES encryption | RSCP |
| 2 | Cloud / remote connection | Core feature: portal login via SSO, WebView on `my.e3dc.com`, token handling | Not offered (LAN only; remote use needs a VPN or the portal relay) | Cloud |
| 3 | Live solar / battery / house / grid / addon power | Yes: dashboard tiles from `LIVEUNITDATA` | Yes: `e3dc/solar/power`, `e3dc/battery/power`, `e3dc/home/power`, `e3dc/grid/power`, `e3dc/addon/power` | RSCP + Cloud |
| 4 | Battery state of charge (SOC) | Yes (`SOC`) | Yes: `e3dc/battery/soc`, plus `rsoc_real`, `soc_float`, `soc_min`, `soc_max` | RSCP + Cloud |
| 5 | Battery state as text (`EMPTY`/`CHARGING`/`DISCHARGING`/`FULL`/`PENDING`) | Portal shows charging state | Yes: `e3dc/battery/state` (derived locally) | RSCP |
| 6 | Grid state as text (`IN`/`OUT`) | Portal shows grid direction | Yes: `e3dc/grid/state` (derived locally) | RSCP |
| 7 | Autarky (self-sufficiency) | Yes | Yes, from the device history: `e3dc/autarky`; plus day/week/month/year/yesterday and per-year history | RSCP |
| 8 | Self-consumption / consumed production | Yes | Only as history based `e3dc/consumed`; there is no live self-consumption topic | RSCP |
| 9 | Today's energy counters (solar, house, grid in/out, battery in/out) | Yes | Yes, plus yesterday, week, month, year and full historical years | RSCP |
| 10 | Historical daily values (arbitrary day) | Yes, via the portal charts | Yes: publish `e3dc/set/request/day` with `YYYY-MM-DD`, plus `request_days.sh` for ranges | RSCP |
| 11 | Inverter (PVI) detail: power/voltage/current per phase, apparent and reactive power, frequency, COS phi, temperature, on-grid | Portal shows the aggregated inverter view | Yes, and far more granular: per phase L1-L3, per string, `pvi/voltage_monitoring_*`, `pvi/cos_phi_*`, `pvi/frequency_under/over`, `pvi/temperature/1..4` | RSCP |
| 12 | Battery module (DCB) detail: per module SOC, SOH, voltage, current, cycles, temperatures, cell voltages, firmware, serial, manufacture data | Not exposed in the portal live view | Yes: `e3dc/battery/dcb/<#>/...` with roughly 30 topics per module, plus min/max cell temperature and cell voltage aggregation | RSCP |
| 13 | Battery specification and health: usable/design capacity, SOH, cycles, specified charge/discharge power | Partly | Yes: `battery/usable_capacity`, `design_capacity`, `soh`, `cycles`, `specified_*`, `training`, `status`, `error` | RSCP |
| 14 | Power meter detail: per-phase power, voltage, energy, active phases, max phase power | Portal shows totals; per-phase detail mainly in the device UI | Yes: `e3dc/pm/...` and `e3dc/pm_0`, `e3dc/pm_1` for external meters | RSCP |
| 15 | EMS internals: mode, coupling mode, charging/discharging locks, throttle, time locks, remaining charge power, used/user charge and discharge limits, DCDC limits | Portal shows the energy flow only | Yes: `e3dc/ems/*`, `e3dc/battery_charge_limit`, `e3dc/mode`, `e3dc/coupling/mode` and more | RSCP |
| 16 | System information: serial number, software release, production date, time zone, installed peak power, inverter and battery count | Yes, in the device/system section | Yes: `e3dc/system/*` | RSCP + Cloud |
| 17 | Additional tags beyond the fixed set (raw discovery, custom requests, custom topics, custom set topics) | Not applicable (fixed portal UI) | Yes: `RAW_MODE`, `ADD_NEW_REQUEST`, `ADD_NEW_TOPIC`, `ADD_NEW_SET_TOPIC` covering any tag present in `RscpTags.h` | RSCP |
| 18 | Manual battery charging start | Yes, as a portal action | Yes: `e3dc/set/manual_charge` with the energy in Wh | RSCP |
| 19 | Weather regulated charging | Yes, as a portal setting | Yes: `e3dc/set/weather_regulation`; state under `e3dc/ems/weather_regulation` | RSCP + Cloud |
| 20 | Power limits: enable/disable, max charge power, max discharge power, discharge start power | Yes, as portal settings | Yes: `e3dc/set/power_limits`, `set/max_charge_power`, `set/max_discharge_power`, `set/discharge_start_power` | RSCP + Cloud |
| 21 | Manual power management: automatic, idle, charge, discharge, grid charge with power and cycle count | Not offered in the portal | Yes: `e3dc/set/power_mode` with `auto`, `idle:60`, `charge:2000:60`, `discharge:2000:60`, `grid_charge:2000:60` | RSCP |
| 22 | Idle periods (charging/discharging time windows), V1 and V2, incl. named periods and delete/refresh | Portal exposes the setting, but not as a scriptable interface | Yes: `e3dc/set/idle_period`, `set/idle_period/delete`, `set/idle_period/refresh`, `set/idle_period/enable`, change marker under `e3dc/idle_period/change` | RSCP |
| 23 | Battery SOC limiter (min/max SOC, home power threshold, durable across day change) | Portal has no equivalent feature | Yes, a client side feature of rscp2mqtt built on idle periods: `e3dc/set/limit/charge/soc`, `limit/discharge/soc`, `limit/discharge/by_home_power`, `limit/*/durable` | RSCP |
| 24 | Emergency power reserve (set reserve in Wh or %, read last SOC and max) | Portal displays emergency power readiness | Yes: `e3dc/set/reserve/energy`, `set/reserve/percent`, state under `e3dc/reserve/*`, `e3dc/ems/emergency_power_*` | RSCP |
| 25 | Wallbox charging data: per-phase power and energy, total and solar energy, SOC, min/max current, phases, plugged, locked, charging, suspended, key state, sun mode | Yes, wallbox view in the portal | Yes: `e3dc/wallbox/*` and, for several wallboxes, `e3dc/wallbox/<#>/*` for up to 8 devices | RSCP + Cloud |
| 26 | Wallbox control: sun mode, min/max current, phases, auto phase switch, suspend and toggle, battery-to-car and battery-before-car mode, discharge battery until, disable at mix mode | Yes, as portal settings | Yes: `e3dc/set/wallbox/*` | RSCP + Cloud |
| 27 | Wallbox daily and per-session energy accounting | Portal shows totals | Yes: `wallbox/energy_start/*` (midnight reference), `energy/day/*`, `energy/last_charging/*` | RSCP |
| 28 | Error messages and status codes (EMS stored errors, battery and wallbox error codes, DCB warnings/alarms) | Portal shows messages; not machine readable | Yes: `e3dc/error_message/<#>` with `meta` type/code/source, `battery/error`, `battery/dcb/<#>/{status,warning,alarm,error}` | RSCP |
| 29 | Grid feed-in limitation (`Einspeisebegrenzung`): active flag, duration today, derate percent and power values | Portal shows it as part of the energy flow | Yes: `e3dc/grid_in_limit`, `e3dc/grid_in_limit_duration`, `e3dc/system/derate_at_percent_value`, `e3dc/system/derate_at_power_value` | RSCP |
| 30 | Sunshine duration and balanced phases per phase | Not machine readable in the portal | Yes: `e3dc/sunshine_duration`, `e3dc/ems/balanced_phases/L1..L3` | RSCP |
| 31 | SG Ready (hardware providers, state, cooldown, used power, global off) | Not offered by the app | Available by configuration, not pre-mapped: `TAG_SGR_*` via `ADD_NEW_REQUEST` and `ADD_NEW_TOPIC` (see `NEWTAGS.md`) | RSCP |
| 32 | Historical/statistical aggregation (min/max per day, statistics switch) | Portal charts only | Yes, across the app: energy counters, statistics, historical years via `request_days.sh` | RSCP |
| 33 | Delivery to a message bus / automation system | None: the app is a user interface for humans | Core purpose: MQTT publish and subscribe with configurable prefix, QoS, retain, TLS; plus optional InfluxDB export | RSCP |
| 34 | Home Assistant integration | Not applicable | Not built in, but documented through the MQTT topics (see `HA_MQTT_DISCOVERY.md`) | RSCP |
| 35 | Polling interval control and selective request switching (PM, PVI, DCB, EMS, history, daily values, statistics, raw mode) | Not offered | Yes: `e3dc/set/interval`, `e3dc/set/requests/*`, `e3dc/set/daily_values`, `e3dc/set/statistic_values`, `e3dc/set/raw_mode` | RSCP |
| 36 | Status and diagnostics of the bridge itself (connection state, version, cycle, timestamp, error and cache logs to file, health log) | Not applicable; the app has crash/analytics reporting instead | Yes: `e3dc/rscp2mqtt/{status,version,long_version,cycle,timestamp,mqtt/encryption,curl/protocol}`, `e3dc/set/log/*`, `e3dc/set/health` | RSCP |
| 37 | Push notifications | Yes, native push via Expo | Not offered (an MQTT consumer must raise notifications) | Cloud |
| 38 | Deep links and app links (`https://my.e3dc.com/...`) | Yes, intent filter with `autoVerify` | Not applicable | Cloud |
| 39 | Biometric / secure credential storage | Yes, via `expo-secure-store` | Not applicable; credentials live in `.config` or environment variables | - |
| 40 | Installer commissioning, firmware update, tariff and contract management | Available through the portal behind the same login | Not offered | Cloud |
| 41 | Network and interface configuration (IP, subnet, gateway, DHCP, DNS, MAC) | Not exposed in the app | Available by configuration: `TAG_INFO_REQ_*` via `ADD_NEW_REQUEST` and `ADD_NEW_TOPIC` (see `NEWTAGS.md`) | RSCP |
| 42 | Write acknowledgement and value symmetry | Portal handles it server side | A channel or topic is only updated after the device confirms; boolean payloads configurable as `true`/`false` or `1`/`0` | RSCP |
| 43 | Offline capability | Portal backed; no local control without internet | Works fully offline on the LAN | RSCP |
| 44 | Authentication model | Portal account, SSO with token and cookie session | Local device account plus a separate RSCP encryption password set on the device | RSCP vs. Cloud |

## Summary

| Aspect | My E3/DC App | rscp2mqtt |
| --- | --- | --- |
| Primary interface | E3/DC cloud portal over HTTPS/AJAX and WebSocket | Local RSCP over TCP |
| Scope of data | The subset the portal decides to expose, currently a flat live document with power, SOC, autarky and wallbox totals | Full RSCP surface, roughly 400 mapped topics plus arbitrary tags by configuration |
| Control surface | Portal actions only, not scriptable | Scriptable MQTT topics for battery, power management, idle periods, emergency power and wallboxes |
| Granularity | Aggregated per system | Per phase, per string, per battery module, per power meter, per wallbox |
| History | Portal charts | Today, yesterday, week, month, year, historical years and arbitrary days |
| Availability | Requires internet and an E3/DC account | Local network only, independent of the portal |
| Automation | None | MQTT, optional InfluxDB, Home Assistant recipes |
| Local-only features | None | SOC limiter, raw mode, custom tags and topics, cache and error logging, bridge diagnostics |

### Interpretation

- Where the API column says **RSCP + Cloud**, both paths deliver comparable values. The cloud path
  reaches the same device data through the portal, and it is the only way to read the device when no
  local network access exists.
- Where it says **RSCP** only, rscp2mqtt is the richer or the only source. That covers most of the
  table: granular inverter, battery module, power meter and EMS data, historical aggregation, the
  scriptable control topics, and all local-only features.
- Where it says **Cloud** only, the app has capabilities rscp2mqtt deliberately does not have, because
  they are portal or operating-system concerns rather than device telemetry: SSO login, push
  notifications, deep links, secure credential storage, and installer or firmware workflows.
- The app contributes almost no functionality of its own. Judged by delivered device functionality,
  rscp2mqtt is the substantially broader implementation, and the app is a portable front end for the
  portal that in turn exposes only a small fraction of what RSCP offers.

## Reproducing the App Analysis

The app bundle is Hermes bytecode. The React Native shell, the WebView configuration and the auth
constants were read from `assets/app.config`, the decompiled native sources and the Hermes string
table. To re-derive the string table, read the bundle header at offset 32
(`functionCount`, `stringKindCount`, `identifierCount`, `stringCount`, `overflowStringCount`,
`stringStorageSize`), then decode entry `i` of the small string table as a little endian `uint32`
with `isUTF16 = bit 0`, `offset = bits 1..23` and `length = bits 24..31`, reading the text from the
string storage blob. A cross check against the overflow table, whose entries decode to complete
JavaScript snippets, confirms the offsets used here.
