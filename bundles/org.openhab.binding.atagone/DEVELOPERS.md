# ATAG ONE Local API — Developer Reference

This document specifies the ATAG ONE thermostat's local HTTP/JSON API as reverse-engineered and
live-tested during development of this binding. It is the authoritative reference for anyone
extending the binding — README.md covers user-facing setup and channels; this covers the wire
protocol underneath.

## Verification status legend

Every field and behavioural claim below carries exactly one status:

- **VERIFIED** — observed on a live device with a specific, reproducible result. The observation is
  cited.
- **INFERRED** — from the cloud portal's HAR capture, a reference library (`pyatag`, the Home
  Assistant ATAG integration, `kozmoz/atag-one-api`), or field naming. Plausible, not device-tested.
  Treat as a hypothesis, not a fact, until retested.
- **UNKNOWN** — the field's value is recorded but its meaning is undetermined. No source, live or
  documented, explains it.

**An INFERRED item is never stated as fact in this document or in code comments derived from it.**
That discipline is not procedural box-ticking — extend mode's write semantics went through three
contradictory theories in one day earlier in this project specifically because an inferred
assumption (`control.extend_duration` mirrors `configuration.ch_mode_extend`) was carried forward
as settled fact. Retest before relying on anything marked INFERRED.

All data below is cross-checked against a full `/retrieve` snapshot taken 2026-08-27
(`atag-full-retrieve-snapshot.json`, not committed to the repository — device-specific capture, kept
alongside this doc during development only).

## Transport

| | |
|---|---|
| Base URL | `http://<hostname>:<port>` — `port` defaults to `10000` |
| Method | `POST`, all three endpoints |
| Content type | `application/json` |
| HTTP version | **HTTP/1.0**, verified |
| Connection | **`Connection: close`**, verified — the device closes every connection after responding |
| Rate limit | **2000 ms minimum between requests** (`MIN_INTERVAL_MS` in `AtagOneApiClient`), enforced by a synchronized rate limiter |
| Timeout | 5 s per request (`REQUEST_TIMEOUT_S`) |
| Retries | Up to 5 (`MAX_RETRIES`) on transient `EOFException`/`SocketTimeoutException`. The first `EOFException` on a request is treated as a free retry (stale pooled connection from the device's HTTP/1.0 close-per-request behaviour) and does not count against the limit |

**Discrepancy, not yet resolved**: the class javadoc in `AtagOneApiClient` states "at least 1 second"
between requests, while the enforced constant is 2000 ms. Neither figure has been independently
tested against the device's actual tolerance — 2000 ms is simply what the shipped code enforces.
INFERRED that either figure is the device's true minimum; UNKNOWN what the device actually requires.

Curl commands used for manual testing must explicitly force `--http1.0 -H "Connection: close"` —
curl's default (HTTP/1.1, keep-alive) does not match this device's behaviour and was not used
consistently during earlier manual testing sessions, which is a plausible confound for some
intermittent instability observed during that testing.

## Discovery (UDP)

| | |
|---|---|
| Port | `11000` |
| Payload | ASCII, prefix `"ONE "` followed by `<device_id>` and an optional status suffix, e.g. `ONE 6808-1500-1805_17-09-001-042 ...` |
| Broadcast interval | ~10 s, VERIFIED |
| Background scan interval | 30 s (`BACKGROUND_SCAN_INTERVAL_S`) |
| Manual scan window | 30 s (`MANUAL_DISCOVERY_TIME_S`), socket timeout 15 s (`SOCKET_TIMEOUT_MS`) |

## Endpoints

### `POST /pair`

```json
{"pair_message": {"seqnr": 0, "accounts": [
  {"user_account": "", "mac_address": "<client_id>", "device_name": "openHAB", "account_type": 0}
]}}
```

Response: `{"pair_reply": {"seqnr": 0, "acc_status": <1|2|3>}}`

`account_type` VERIFIED present in every pairing request this binding sends; its meaning beyond `0`
is UNKNOWN — `kozmoz/atag-one-api`'s wiki documents `0 = user, 1 = service` but no local-API test has
ever paired with `1` (INFERRED only, from that library's docs, not from this device).

### `POST /retrieve`

```json
{"retrieve_message": {"seqnr": 0, "account_auth": {"user_account": "", "mac_address": "<client_id>"}, "info": <bitmask>}}
```

Response: `{"retrieve_reply": {"seqnr": 0, "acc_status": 2, "status": {...}, "report": {...}, "control": {...}, "schedules": {...}, "configuration": {...}}}`

### `POST /update`

```json
{"update_message": {"seqnr": 0, "account_auth": {"user_account": "", "mac_address": "<client_id>"}, "control": {...}, "configuration": {...}}}
```

Response: `{"update_reply": {"seqnr": 0, "acc_status": <1|2|3>}}` — `configuration` block is optional
and omitted from the request entirely when there is nothing to change in it.

### `acc_status` values (all three endpoints)

| Value | Meaning |
|---|---|
| 1 | Pending — user must press Accept on the thermostat display |
| 2 | Granted / write accepted |
| 3 | Denied |

A response body can also arrive empty (`curl: (52) Empty reply from server`) — VERIFIED to happen
both as a transient artifact unrelated to write success, and as a symptom of a broader device
unresponsiveness episode (see Known instability, below). An empty reply must never be treated as a
definitive failure signal on its own — always confirm via a follow-up `/retrieve`.

## The `info` bitmask

```text
control(1) + schedules(2) + configuration(4) + report(8) + status(16) + details(64) = 95
```

`wifi_scan(32)` is deliberately excluded — VERIFIED to trigger a nearby-AP scan that delays the
response by several seconds. The binding's `wifi-signal` channel is unaffected by this exclusion;
`rssi` is reported in the `report` block regardless. **Bit 2 (`schedules`) is requested on every
poll and its ~40%-by-size payload is parsed by Gson and silently discarded** — see Schedules, below.

## ATAG epoch

All device timestamps (`status.date_time`, `report.report_time`, `configuration.start_vacation`) are
seconds since **2000-01-01T00:00:00 UTC**. Offset from Unix epoch: `946684800` (`10957 * 86400`).
VERIFIED, round-trip tested (`AtagEpochTest`).

---

## Field reference

Columns: **Field** (device JSON key) · **Type** · **Unit** · **Access** (see Writability policy) ·
**Exposed as** (channel id or Thing property; `—` = not exposed) · **Status** (verification tier).

### `status` block (4 fields) — entirely unexposed

| Field | Type | Unit | Access | Exposed as | Status |
|---|---|---|---|---|---|
| `device_id` | string | — | R | — (used only during discovery, not read from `/retrieve`) | VERIFIED |
| `device_status` | int (bitmask) | — | R | — | UNKNOWN |
| `connection_status` | int (bitmask) | — | R | — | UNKNOWN |
| `date_time` | long (ATAG epoch) | s | R | — | VERIFIED (format only; not consumed) |

This entire block is parsed into `StatusDTO` and then never read by the handler. Not a bug — simply
unused. Candidate for exposure; see Gap analysis.

### `report` block (28 scalar fields)

| Field | Type | Unit | Access | Exposed as | Status |
|---|---|---|---|---|---|
| `report_time` | long (ATAG epoch) | s | R | `device#report-time` | VERIFIED |
| `burning_hours` | double | h | R | `heating#burning-hours` | VERIFIED |
| `device_errors` | string (CSV) | — | R | `alerts#device-errors` | VERIFIED |
| `boiler_errors` | string (CSV) | — | R | `alerts#boiler-errors` | VERIFIED |
| `room_temp` | double | °C | R | `heating#room-temperature` | VERIFIED |
| `outside_temp` | double | °C | R | `heating#outside-temperature` | VERIFIED — boiler's own estimate; documented to go stale outside the heating season |
| `dbg_outside_temp` | double | °C | R | — | UNKNOWN — a second outdoor reading alongside `outside_temp`; relationship between the two undetermined |
| `pcb_temp` | double | °C | R | `device#pcb-temperature` | VERIFIED |
| `ch_setpoint` | double | °C | R | `heating#water-setpoint` | VERIFIED — reads 0 when no heating demand active |
| `dhw_water_temp` | double | °C | R | `hotwater#temperature` | VERIFIED |
| `ch_water_temp` | double | °C | R | `heating#water-temperature` | VERIFIED |
| `dhw_water_pres` | double | bar | R | — | VERIFIED (reads); not exposed — see Gap analysis |
| `ch_water_pres` | double | bar | R | `heating#water-pressure` | VERIFIED |
| `ch_return_temp` | double | °C | R | `heating#return-temperature` | VERIFIED |
| `boiler_status` | int (bitmask) | — | R | `heating#flame`, `heating#burner-target` (decoded) | PARTIAL — see below |
| `boiler_config` | int (bitmask) | — | R | — | UNKNOWN |
| `ch_time_to_temp` | int | s | R | `heating#time-to-target` | VERIFIED |
| `shown_set_temp` | double | °C | R | `heating#shown-set-temperature` | VERIFIED |
| `power_cons` | int | ? | R | — (deliberately not exposed) | UNKNOWN unit — see below |
| `tout_avg` | double | °C | R | `heating#average-outside-temperature` | VERIFIED |
| `rssi` | int | dBm (negated) | R | `device#wifi-signal` | VERIFIED |
| `current` | int | ? | R | — (deliberately not exposed) | UNKNOWN unit — see below |
| `voltage` | int | mV or V | R | `device#voltage` | VERIFIED shape (auto-scales: >1000 treated as mV) |
| `charge_status` | int | — | R | — | UNKNOWN |
| `lmuc_burner_starts` | int | count | R | — | VERIFIED reads 0 on this device — cannot distinguish "unsupported" from "genuinely zero" |
| `dhw_flow_rate` | double | L/min | R | `hotwater#flow-rate` | VERIFIED |
| `resets` | int | count | R | `device#resets` | VERIFIED — used throughout live testing as the controller-reboot indicator |
| `memory_allocation` | int | ? | R | `device#memory-allocation` | UNKNOWN unit |

**`boiler_status` bitmask — incompletely decoded.** The binding decodes:

```java
BOILER_STATUS_CH_ACTIVE  = 0x004
BOILER_STATUS_BURNER_ON  = 0x008
BOILER_STATUS_DHW_ACTIVE = 0x010
BOILER_STATUS_FLAME      = 0x100
```

The 2026-08-27 snapshot reads `boiler_status = 512 = 0x200` — a bit **not covered by any of the
above**. The current decode is therefore not a complete model of this field; at minimum bit 0x200's
meaning is UNKNOWN.

**`current` and `power_cons` units are UNKNOWN — not exposed as channels (2026-08-27 decision).** An
earlier ÷100000 m³ gas-consumption conversion for `power_cons` was researched and never confirmed
against this specific device. `power_cons` sits in the report block alongside `current`, `voltage`,
`rssi`, and `charge_status` — device power-supply telemetry, not gas-metering fields, casting real
doubt on the "gas counter" theory; more likely both relate to the thermostat's own electrical supply,
paired with `voltage`. With no way to verify either interpretation against this device, both fields
are read into the DTO but deliberately not published as channels, rather than exposing a raw number
with a misleading or absent unit. Revisit if a verification method turns up.

### `report.details` block (25 fields) — boiler regulation internals

None of these have any counterpart in the cloud portal's EditDevice form or the app's settings tree.
Per the writability policy, **none are writable**. Most have no externally observable behaviour to
verify against, no vendor documentation, and several read as `0` or near-zero on this boiler with no
way to distinguish "not supported by this boiler model" from "genuinely zero" from a single snapshot.

| Field | Type | Unit | Access | Exposed as | Status |
|---|---|---|---|---|---|
| `boiler_temp` | double | °C | R | `heating#boiler-temperature` | VERIFIED |
| `boiler_return_temp` | double | °C | R | `heating#boiler-return-temperature` | VERIFIED |
| `min_mod_level` | int | % | R | `heating#min-modulation-level` | VERIFIED |
| `rel_mod_level` | int | % | R | `heating#modulation-level` | VERIFIED |
| `boiler_capacity` | int | kW (presumed) | R | — | UNKNOWN — reads 0 |
| `target_temp` | double | °C | R | — | UNKNOWN — regulation-algorithm internal target, distinct from `ch_mode_temp` |
| `overshoot` | double | K | R | — | UNKNOWN |
| `max_boiler_temp` | double | °C | R | `heating#max-boiler-temperature` | VERIFIED |
| `alpha_used` | double | — | R | — | UNKNOWN — regulation coefficient |
| `regulation_state` | int | — | R | — | INFERRED `0=off, 1=on` from naming; not tested |
| `ch_m_dot_c` | double | — | R | — | UNKNOWN |
| `c_house` | long | — | R | — | UNKNOWN |
| `r_rad` | double | — | R | — | UNKNOWN |
| `r_env` | double | — | R | — | UNKNOWN |
| `alpha` | double | — | R | — | UNKNOWN |
| `alpha_max` | double | — | R | — | UNKNOWN |
| `delay` | int | — | R | — | UNKNOWN |
| `mu` | double | — | R | — | UNKNOWN — reads 0 here; also appears in `configuration.mu` |
| `threshold_offs` | double | K (presumed) | R | — | UNKNOWN |
| `wd_k_factor` | double | — | R | — | UNKNOWN — duplicates `configuration.wd_k_factor` |
| `wd_exponent` | double | — | R | — | UNKNOWN — duplicates `configuration.wd_exponent` |
| `lmuc_burner_hours` | double | h | R | — | VERIFIED reads 0 — cannot distinguish unsupported from genuinely zero |
| `lmuc_dhw_hours` | double | h | R | — | VERIFIED reads 0 — same caveat |
| `KP` | double | — | R | — | UNKNOWN — PID proportional gain |
| `KI` | double | — | R | — | UNKNOWN — PID integral gain |

### `control` block (14 fields)

This is the block most incidents this project has had originate from — every field here is
VERIFIED present, but several have write semantics that took multiple live-test rounds to establish
correctly, documented in full under Write semantics below.

| Field | Type | Unit | Access | Exposed as | Status |
|---|---|---|---|---|---|
| `ch_status` | int (bitmask) | — | R | — | UNKNOWN |
| `ch_control_mode` | int enum | — | **W** (bundle only — see below) | `heating#control-mode` | VERIFIED `0=room, 1=weather` |
| `ch_mode` | int enum | — | **W** | `control#preset-mode` | VERIFIED `1=manual(R), 2=auto, 3=holiday, 4=extend, 5=fireplace` |
| `ch_mode_duration` | long | s | R for its value; **must be written as `0` to cancel any timed preset**, and **must be present (any value) to activate fireplace specifically** | `control#preset-mode-duration` | VERIFIED, mode-dependent meaning — see below |
| `ch_mode_temp` | double | °C | **W** | `heating#target-temperature`, `control#vacation-temperature` (mode-dependent) | VERIFIED |
| `dhw_temp_setp` | double | °C | R — **not writable, despite earlier documentation and code claiming otherwise** | `hotwater#target-temperature` | VERIFIED (2026-08-31): writes are silently accepted (`acc_status:2`) but never change the value. It tracks whichever `schedules.dhw_schedule` entry is currently active. The real user-settable field is `schedules.dhw_schedule.base_temp` — see the `schedules` section below and the Gap analysis |
| `dhw_status` | int (bitmask) | — | R | — | UNKNOWN |
| `dhw_mode` | int enum | — | R | — (removed from channel list — see Gap analysis) | UNKNOWN values |
| `dhw_mode_temp` | double | °C (presumed) | R | — | UNKNOWN — reads `150.0`, looks like a sentinel/unused value rather than a real temperature |
| `weather_temp` | double | °C | R | — | VERIFIED reads; not exposed — see Gap analysis |
| `weather_status` | int enum | — | R | `heating#weather-status` | VERIFIED, 14-value enum (sunny…unknown) |
| `vacation_duration` | long | s | **W** — value-setter only, does not activate holiday mode when written alone (binding design, matches confirmed device behavior) | `control#vacation-duration` | VERIFIED — genuinely honored by the device once `start_vacation` is present, see Write semantics |
| `extend_duration` | long | s | **W** — value-setter only, does not activate extend mode when written alone | `control#extend-duration` | VERIFIED — stored/echoed correctly, additive not absolute, see Write semantics |
| `fireplace_duration` | long | s | **W** — value-setter only, does not activate fireplace mode when written alone | `control#fireplace-duration` | VERIFIED |

### `configuration` block (45 fields)

| Field | Type | Unit | Access | Exposed as | Status |
|---|---|---|---|---|---|
| `report_url` | string | — | R | — | VERIFIED (read-only, do not write) |
| `download_url` | string | — | R | — | VERIFIED — firmware version embeddable (`…/R60` → `R60`); candidate Thing property |
| `boiler_id` | string | — | R | — | VERIFIED; candidate Thing property |
| `boiler_det_type` | int | — | R | — | UNKNOWN meaning; candidate Thing property |
| `language` | int enum | — | INFERRED W (app) | — | VERIFIED `0=English, 1=Dutch, 2=French, 3=Italian, 4=German` — device reads `4`, display confirmed set to German |
| `pressure_unit` | int enum | — | INFERRED W (app) | — | INFERRED `0=bar, 1=psi` from javadoc; reads 0, alternate branch untested |
| `temp_unit` | int enum | — | INFERRED W (app) | — | INFERRED `0=°C, 1=°F`; reads 0, alternate branch untested |
| `time_format` | int enum | — | INFERRED W (app) | — | INFERRED `0=24h, 1=12h`; reads 1 |
| `time_zone` | int enum | — | **W** (cloud) | — | PARTIAL — `1=Berlin` VERIFIED (cloud form + device agree); other 9 values INFERRED from dropdown order only |
| `summer_eco_mode` | int (bool-ish) | — | **W** (cloud) | — | VERIFIED shape; `1=on` INFERRED |
| `summer_eco_temp` | double | °C | **W** (cloud) | — | VERIFIED |
| `shower_time_mode` | int | — | R | — | UNKNOWN — no cloud/app surface found |
| `comfort_settings` | int (bitmask) | — | R | — | UNKNOWN |
| `room_temp_offs` | double | °C | **W** (app) | — | VERIFIED — matches app's "Indoor temperature correction" exactly (reads −1.0) |
| `outs_temp_offs` | double | °C | **W** (app) — see note | — | PARTIAL — see outdoor-correction ambiguity below |
| `ch_temp_max` | double | °C | R (installer) | — | VERIFIED reads; duplicates `heating#max-boiler-temperature`'s role — no separate channel needed |
| `ch_vacation_temp` | double | °C | **W** (cloud) | `control#vacation-temperature` (read side, when not in holiday) | VERIFIED |
| `start_vacation` | long (ATAG epoch) | s | **W** (implicit, via vacation-duration write) | `control#vacation-start` (derived) | VERIFIED |
| `wd_k_factor` | double | — | R | — | UNKNOWN — duplicates `report.details.wd_k_factor` |
| `wd_exponent` | double | — | R | — | UNKNOWN — duplicates `report.details.wd_exponent` |
| `climate_zone` | double | °C | **W** (cloud) | — | VERIFIED reads; not exposed |
| `wd_temp_offs` | double | °C | ambiguous | — | PARTIAL — see outdoor-correction ambiguity below |
| `dhw_legion_day` | int enum | — | **W** (cloud) | — | VERIFIED `1=Monday…7=Sunday` — cloud form shows `7` as "Sonntag", device agrees |
| `dhw_legion_time` | int | min since midnight | **W** (cloud) | — | VERIFIED — `420` = 07:00, matches cloud form |
| `dhw_boiler_cap` | int | kW (presumed) | R | — | UNKNOWN — reads 0 |
| `ch_building_size` | int enum | — | **W** (cloud) | — | VERIFIED `1=small, 2=medium, 3=large` — device=2, cloud shows "medium" |
| `ch_heating_type` | int enum | — | **W** (cloud) | — | VERIFIED 6-value enum — device=5, cloud shows "underfloor" |
| `ch_isolation` | int enum | — | **W** (cloud) | — | VERIFIED `1=poor, 2=average, 3=good` — device=3, cloud shows "good" |
| `installer_id` | string | — | R | — | VERIFIED (reads empty on this device) |
| `disp_brightness` | int | % | **W** (app) — untested | — | VERIFIED reads (30); write never live-tested |
| `ch_mode_vacation` | long | s | **W** (cloud, unit-translated) | (feeds `defaultVacationDurationSeconds` internally) | VERIFIED — cloud form is **days** (7), local API is **seconds** (604800) |
| `ch_mode_extend` | long | s | **W** (cloud, unit-translated) | — | VERIFIED value (3600) but **not the extend session length** — see Write semantics |
| `support_contact` | string | — | R | — | VERIFIED (read-only, do not write) |
| `privacy_mode` | int (bool-ish) | — | R (installer) | — | UNKNOWN — `1=on, disables cloud reporting` per earlier research, not device-tested |
| `ch_max_set` | double | °C | R | — | VERIFIED reads (85.0) — setpoint bound, not a user setting; candidate for dynamic state description |
| `ch_min_set` | double | °C | R | — | VERIFIED reads (20.0) — same |
| `dhw_max_set` | double | °C | R | — | VERIFIED reads (65.0) — same; `thing-types.xml` currently hardcodes this bound statically |
| `dhw_min_set` | double | °C | R | — | VERIFIED reads (10.0) — **device reports 10, `thing-types.xml` hardcodes 40** — a real, already-identified discrepancy |
| `mu` | double | — | R | — | UNKNOWN — duplicates `report.details.mu` |
| `dhw_legion_enabled` | int (bool-ish) | — | **W** (cloud) | — | VERIFIED shape |
| `frost_prot_enabled` | int enum | — | **W** (cloud) | — | VERIFIED `0=off,1=outdoor,2=indoor,3=both` — device=0, cloud shows "off" |
| `frost_prot_temp_outs` | double | °C | **W** (cloud) | — | VERIFIED reads |
| `frost_prot_temp_room` | double | °C | **W** (cloud) | — | VERIFIED reads |
| `wdr_temps_influence` | int enum | — | **W** (cloud) | — | VERIFIED `0=off,1=less,2=average,3=more,4=room` — device=2, cloud shows "medium" |
| `max_preheat` | int | min | **W** (cloud) | — | PARTIAL — `1440=Automatic` VERIFIED (device value + cloud UI agree); `180/120/60/0` (3h/2h/1h/Off) INFERRED from cloud submit values only |

**Outdoor-temperature-correction ambiguity — unresolved.** The cloud form's single field
`wdr_temps_offset` ("Aussentemperatur Korrektur") could map to either `wd_temp_offs` or
`outs_temp_offs`; **both read `0.0`** in every capture so far, so no snapshot can disambiguate them.
Current lean: `outs_temp_offs`, because `room_temp_offs` (its DTO neighbour, both under "Temperature
calibration offsets") independently and exactly matches the app's separate "Indoor temperature
correction" field, suggesting the two are a matched local pair — while the cloud's `wdr_` prefix
points toward `wd_temp_offs` instead (part of the "Weather-dependent regulation" section). **This is
a named open question, not a resolved fact** — settling it needs a live test: set the correction to
a distinctive non-zero value via the app and see which field changes.

---

## `schedules` block

**Present in every `/retrieve` response (bit 2 of the info bitmask) and currently discarded
entirely** — no `schedules` field exists on `RetrieveReplyDTO`, so Gson silently drops the whole
object on every poll. Full implementation (read and write) is a planned future phase; this section
specifies the structure as observed, since that work will build directly on it.

```json
"schedules": {
  "ch_schedule":  { "base_temp": 22.5, "entries": [ [days 0-6] ] },
  "dhw_schedule": { "base_temp": 55.0, "entries": [ [days 0-6] ] }
}
```

| | |
|---|---|
| `base_temp` | double, °C — VERIFIED to answer a previously open question: this is the cloud EditDevice form's `ch_base_temp` (22.5) and `dhw_base_temp` (55.0), which are absent from the `configuration` block entirely. Confirmed by exact value match. |
| `entries` | array of 7 elements, one per weekday (order not yet confirmed against a specific day — VERIFIED count is 7, VERIFIED all 7 are identical in this capture, so day-order has not actually been distinguished by any test) |
| each day | array of `[start, end, temp]` triples — **variable length per day**, not fixed. `ch_schedule` has 2 triples/day in this capture, `dhw_schedule` has 3. Per the user (device owner), the number of periods is user-configurable and can be arbitrary |
| `start`, `end` | int, minutes since midnight (0–1440) |
| `temp` | double, °C — the setpoint for that window |

**Fallback semantics, VERIFIED for one case, unconfirmed for the other.** Within a triple's
`[start, end)` window, `temp` applies. Outside all triples for a day, the observed behaviour differs
between the two schedules in this capture:

- `ch_schedule`: triples are `[0,240,20.5]` and `[1230,1440,20.5]` — a gap from 04:00–20:30 that
  `base_temp` (22.5) fills. This is the schedule extend mode's additive-duration write depends on —
  see Write semantics — and the boundary at minute 1230 (20:30) is independently confirmed by
  server-side timing math against a live extend activation (predicted vs. actual within 5 seconds,
  twice).
- `dhw_schedule`: triples tile the entire day (`[0,360]`, `[360,1260]`, `[1260,1440]`), so
  `base_temp` (55.0) is never reached in this capture. Whether it is a genuine fallback value or an
  inert default cannot be determined from a schedule with no gap to expose it.

**Write shape: entirely undocumented.** No schedule write has been attempted against this device.
Establishing the write payload shape is the first task of schedule implementation, not something
this document can specify yet.

---

## Write semantics

### Mode activation (`ch_mode`)

All of the following are VERIFIED by an exhaustive, isolated, single-variable-at-a-time manual API
test (raw curl, binding disabled, HTTP/1.0 + `Connection: close`, 2000 ms inter-request gap) — not
inferred. This supersedes the pre-test version of this table entirely.

| Mode | `ch_mode` | Fields required together to activate | Duration source when not explicit |
|---|---|---|---|
| Manual | 1 | **Not writable** by this binding — see the note below | — |
| Auto | 2 | `ch_mode` alone is sufficient | — |
| Holiday/vacation | 3 | `ch_mode` + `configuration.start_vacation` **must be in the same write** — confirmed to never activate without `start_vacation`, regardless of whether `vacation_duration` is preset (three independent failed attempts without it) | `control.vacation_duration` if non-zero, else `configuration.ch_mode_vacation` (7 days) — the device never applies this fallback itself, the binding does |
| Extend | 4 | `ch_mode` alone is sufficient | `control.extend_duration` — **persists across cancel**, a genuinely stable stored default |
| Fireplace | 5 | `ch_mode` alone is sufficient; `ch_mode_duration` **must additionally be present** on this mode specifically — its absence causes a confirmed ~4 minute boiler API restart | `control.fireplace_duration` — **reverts to the factory default (3600) on every cancel**, not stable the way extend's is |

**A manual-mode contradiction, not yet reconciled.** The same test report found `{"ch_mode":1}`
applied cleanly with no restart, directly contradicting the long-standing basis for this binding's
hard rejection of manual writes. That claim's origin isn't traceable in this session, the
contradicting evidence is a single test, and the current rejection is a safety behavior, not
established as a bug — so the binding's code has **not** been changed to allow it. Tracked as an
open question below; do not act on the new evidence without dedicated verification first.

`start_vacation` also supports genuine **future-scheduled** activation — confirmed by directly
observing the physical thermostat switch into vacation mode at the scheduled time, from a clean
baseline. But rewriting `start_vacation` while a schedule from an earlier write is already
pending/active is a different, unsupported operation — confirmed to trigger a real device reset
(`resets` incremented). Treat `start_vacation` as write-once until the vacation it scheduled is
cancelled.

### Cancellation

- **`ch_mode_duration` is the field that must be zeroed to cancel any timed preset — not the
  mode-specific duration field.** Isolated directly for extend: cancelling with
  `{"ch_mode":2,"extend_duration":0}` (`ch_mode_duration` omitted) left the countdown stale and
  uncleared, even though `ch_mode` itself flipped to auto correctly. None of the three mode-specific
  duration fields need to be included in a cancel write at all.
- **Fireplace cancel cannot be completed via the API alone, in any tested payload variant.**
  Confirmed three ways, including directly watching the physical display: the write is accepted
  (`acc_status:2`) but has no effect until a button is pressed on the thermostat itself. This is a
  device protocol requirement, not a payload issue.
- **Vacation's cancel payload depends on active vs. pending.** For an actively-running vacation,
  `{"ch_mode":2,"ch_mode_duration":0}` alone is sufficient — `vacation_duration` and `start_vacation`
  both self-clear automatically. For a pending/future-scheduled vacation that hasn't started counting
  down yet, `ch_mode_duration` is already `0` throughout, so that 2-field payload touches nothing and
  leaves the schedule armed — cancelling it requires explicitly adding
  `"configuration":{"start_vacation":0}`.
- Extend cancels cleanly via the API alone in every tested case, no caveats.

### Duration-field persistence across cancel

| Field | Survives cancel? |
|---|---|
| `extend_duration` | VERIFIED yes — stays at whatever was last written |
| `fireplace_duration` | VERIFIED no — always reverts to the factory default (3600) |
| `vacation_duration` | VERIFIED no — resets to `0` |

Device behavior, observed consistently — not something the binding tries to normalize. A custom
fireplace or vacation duration only survives until the next cancel; extend's does not have this
limitation.

### `ch_mode_duration` — a single field with mode-dependent meaning, VERIFIED

This field is not a uniform "requested duration" write target. It is better understood as the
device's own live "time remaining until this mode's end-criterion" computation:

- **Holiday** supplies an explicit end-criterion (`start_vacation` + `vacation_duration`), and the
  device genuinely tracks it in `ch_mode_duration`.
- **Extend** has no equivalent end-criterion field anywhere in `configuration` — no `start_extend`
  exists. `ch_mode_duration` instead reads as **(time remaining until the next `ch_schedule`
  boundary) + `extend_duration`** — confirmed three independent ways: server-side timing math
  (predicted vs. actual within 5 s, twice, using different requested durations), the cloud portal's
  own EditDevice form wording ("extend current temperature by hours", with a "New entry time" field
  computed as current schedule entry time + hours), and the raw `ch_schedule` data itself (the
  20:30 boundary from the math matches minute 1230 in the schedule exactly). Plain auto mode with no
  extend session active also shows a nonzero `ch_mode_duration` — consistent with the same
  generic "time to next transition" computation, not an explicit session length.
- **Fireplace**: VERIFIED the value is honored when non-zero and matching `fireplace_duration`; `0`
  triggers a fallback-to-stored-default rather than a literal zero-length session.

**This directly contradicts an earlier (superseded) project conclusion** that extend's duration was
"entirely device-computed and not controllable by anything the client sends" — that conclusion was
reached before the schedule-additive mechanism was identified and should be treated as historical
context, not current fact.

### `ch_control_mode` (room/weather)

**Not writable as a bare or lightly-bundled field** — VERIFIED, multiple attempts. Writable **only**
as part of the full ~19-field configuration bundle matching the cloud portal's `/Device/EditDevice`
form shape (every writable `configuration` field above, resent at its current value, alongside the
changed `ch_control_mode`). VERIFIED working in both directions (room→weather and weather→room) via
this exact shape.

## Writability policy

**Writable = only what ATAG's own app or cloud portal exposes as user-changeable.** This is a
deliberate safety boundary, not just a documentation convenience — the binding does not touch
boiler regulation/commissioning parameters (`report.details.*`, several `configuration` internals)
regardless of whether a write to them might technically succeed. Everything in `report` and
`report.details` is read-only by nature (telemetry). Within `control` and `configuration`, the
Access column above reflects this policy: **W** = confirmed present in the cloud form or app;
INFERRED W = plausible from a reference library or field grouping but unconfirmed; unmarked = no
ATAG-surfaced UI found, read-only regardless of technical writability.

Two unit translations the cloud performs, which any future write path must replicate exactly:

- `ch_mode_vacation`: cloud form is **days** (e.g. `7`); local API is **seconds** (`604800`)
- `ch_mode_extend`: cloud form is **hours** (e.g. `1`); local API is **seconds** (`3600`)

## Known instability — unresolved, not mode-specific

Across extensive live testing, the device's embedded HTTP server has intermittently become fully
unresponsive (empty replies on every request) for periods ranging from ~45 seconds to ~4.5 minutes.
VERIFIED to occur during extend-mode testing, vacation-mode testing, and idle periods with no writes
at all — **not correlated with any specific mode or duration value**. Sometimes coincides with the
`resets` counter incrementing (a real controller reboot); sometimes does not. Three untested
candidate contributors, none isolated:

1. Concurrent polling — openHAB's own scheduled poll and manual `curl` testing hitting the device
   in the same window
1. The HTTP/1.0 vs. HTTP/1.1 mismatch noted under Transport — manual `curl` testing did not
   consistently force HTTP/1.0 until late in this investigation
1. The 2000 ms rate limit not being respected by manual testing, particularly on same-second retries
   after an empty-reply failure

## Gap analysis — what the binding should expose but doesn't

**Recommended to expose as read-only channels:**

- `control.weather_temp` — the weather-service outdoor temperature, distinct from `report.outside_temp` (the boiler's own estimate, documented to go stale outside the heating season). The two are genuinely different data sources.
- `report.dhw_water_pres` — pairs with the already-exposed `heating#water-pressure`; no reason DHW pressure is missing while CH pressure is present.
- `report.details.regulation_state` — cheap, useful "is the regulation algorithm active" status, unlike the other `report.details` internals which have no external meaning.
- `schedules.ch_schedule.base_temp` / `schedules.dhw_schedule.base_temp` — once schedule support exists. Originally thought to only answer "what temperature applies when no schedule block is active"; now confirmed (2026-08-31) that `dhw_schedule.base_temp` is also the **actual writable target** for changing the DHW setpoint — `control.dhw_temp_setp` looked like that target but is read-only/derived. Writing `base_temp` requires sending the complete `dhw_schedule` object (`entries` included), not `base_temp` alone. `hotwater#target-temperature` should become writable again once this write path is implemented, targeting `base_temp` instead of `dhw_temp_setp`.

**Recommended to expose as Thing properties (static identity, not channels):**

- `configuration.boiler_id`, `configuration.installer_id`, `configuration.boiler_det_type`
- Firmware version parsed from `configuration.download_url` (`…/R60` → `R60`)

**Recommended to expose via a dynamic state description provider, not as separate channels:**

- `configuration.dhw_min_set` / `dhw_max_set` as the actual bounds for `dhw-target-temperature` —
  `thing-types.xml` currently hardcodes `min="40" max="65"`, but this device reports `min=10`. That
  is a real, already-identified discrepancy, not a hypothetical one.
- `configuration.ch_min_set` / `ch_max_set` are boiler _water_ temperature limits (20–85°C), not
  room setpoint bounds — must not be wired to `target-temperature`'s 4–30°C range.

**Recommended to remain unexposed:**

- All `report.details` regulation internals (no cloud/app surface, no external meaning established)
- `configuration.shower_time_mode`, `comfort_settings`, `report_url`, `support_contact` (no
  cloud/app surface)
- `control.dhw_mode` — removed from the channel list (2026-08-27), consistent with `current` and
  `power_cons` above: no source (this device, the cloud form, `kozmoz`'s wiki, or `pyatag`)
  documents its value meanings, and neither the app nor the cloud portal expose a setting for it
- Counters reading 0 with undetermined hardware support (`boiler_capacity`, `lmuc_burner_hours`,
  `lmuc_dhw_hours`, `lmuc_burner_starts`)

**Channel-group placement rule (Phase 1 revisit, 2026-08-28):** group = domain/subsystem
(`heating`/`hotwater`/`device`/`alerts`), with `control` as the one deliberate exception — it holds
the operating-mode/preset concern, which is cross-cutting and belongs to no single subsystem. The
status-vs-configuration axis is carried by `advanced="true"` on the channel-type, never by a separate
group — a settings group was tried and abandoned (see [[project_atagone_binding]] memory) because it
ended up holding a single channel while every other settings-shaped field belonged with its subsystem.
A measurement and its setpoint always live together (`heating#target-temperature` next to
`heating#room-temperature`; `hotwater#target-temperature` next to `hotwater#temperature`). Drop a
subsystem prefix from a channel id once its group already carries it (`hotwater#temperature`, not
`hotwater#dhw-temperature`); keep a qualifier that disambiguates within the group
(`heating#room-temperature` keeps `room`, since `heating` holds both room-air and boiler-water
readings).

**Future settings channels — placement, per the rule above (not yet implemented):**

| Fields | Group | Notes |
|---|---|---|
| `frost_prot_*`, `summer_eco_*`, `ch_heating_type`, `ch_isolation`, `ch_building_size`, `wdr_temps_influence`, `climate_zone`, `max_preheat`, outdoor-temp correction (`wd_temp_offs`/`outs_temp_offs`) | `heating` | advanced, writable |
| `dhw_legion_enabled`/`_day`/`_time` | `hotwater` | advanced, writable |
| `ch_mode_vacation`, `ch_mode_extend` | `control` | advanced — preset defaults, not subsystem settings |
| `disp_brightness` | `device` | advanced, writable **after a live write test** (currently "W (app) — untested"). The one device-level setting with real automation value: dimming the display at night |
| `time_zone` | `device` | advanced, **read-only initially** — only `1=Berlin` is verified; the other 9 enum values are inferred from dropdown order only, and writing an unverified enum to device config is the exact risk class that has caused live incidents in this project before. Worth exposing read-only regardless: schedule timing depends on it |
| `language` | `device` | advanced, **read-only** — enum is verified, but changing the thermostat's display language from openHAB has near-zero automation value |
| `dhw_min_set`/`dhw_max_set`, `ch_min_set`/`ch_max_set` | — | Not channels: dynamic state description provider (see above) |
| `boiler_id`, `installer_id`, firmware version | — | Not channels: Thing properties (see above) |

## Resolved (2026-08-27) — no longer open

The exhaustive manual test report referenced throughout the Write semantics section above settled
these:

- `ch_mode_duration` presence/value for cancellation — resolved: it's specifically
  `ch_mode_duration` (not the mode-specific field) that must be zeroed to cancel, for all three
  timed presets; the mode-specific duration fields are irrelevant to cancellation.
- Vacation's activation requirement — fully mapped: `ch_mode` + `start_vacation` is the hard
  requirement, `vacation_duration` follows the same stored-or-fresh pattern the other two modes use.
- `end_vacation` — confirmed **not** to exist as a field, checked twice via full-field greps of
  separate `/retrieve` captures. Settled negative; don't re-investigate.
- `configuration.language` — resolved: the app's language dropdown is English/Niederländisch
  (Dutch)/Französisch (French)/Italienisch (Italian)/Deutsch (German), 0-indexed. This device reads
  `language:4` and its display is confirmed set to Deutsch — index 4 lands on German, matching
  exactly. `4=German` treated as VERIFIED on the strength of that consistency check, even without a
  live test cycling every other value.

## Open questions

Every item below needs a live retest before being treated as settled. None require code changes to
investigate — all are either read-only checks or reuse an already-proven write shape with one
deliberately varied field.

1. **Manual mode (`ch_mode:1`) applied cleanly with no restart** in one live test — directly
   contradicts this binding's current hard rejection of manual writes. The rejection's original basis
   isn't traceable in this session, and it's a safety behavior, not a confirmed bug — needs dedicated
   verification before any code change; not touched by the current implementation.
1. Why does `fireplace_duration` revert to its factory default specifically after the
   physical-confirmation cancel path — would it also revert after a hypothetically successful
   API-only cancel? Not isolated; API-only cancel for fireplace has never been observed to actually
   take effect on its own.
1. Which transitions are expected to bump the `resets` counter as a normal artifact (e.g. a scheduled
   vacation's actual activation moment) versus signal a real problem? Observed inconsistently; no
   complete list exists.
1. Which field is the outdoor-temperature correction: `wd_temp_offs` or `outs_temp_offs`?
1. `max_preheat`'s non-Automatic values (3/2/1 hours, Off) and `time_zone`'s 10-city order
   (Amsterdam, Berlin, Brussels, Dublin, Edinburgh, Frankfurt, London, Luxembourg, Paris, Rome) are
   now corroborated by the app UI directly, not just the cloud form's dropdown order — two
   independent sources agreeing raises confidence, but neither source is a live device read at each
   individual setting, so the actual device-side integer for each non-Auto/Berlin value (only
   `1440=Automatic` and `1=Berlin` are device-confirmed) remains INFERRED, not VERIFIED.
1. What does `control.dhw_mode` (reads `1`) enumerate? No app or cloud surface for it exists at
   all (confirmed) — consistent with the decision to leave it unexposed rather than guess at a
   mapping with no source to check it against. Newly relevant (2026-08-31): with `dhw_schedule.entries`
   covering all 1440 minutes of the day with no gaps, `base_temp` cannot be a "no active schedule
   period" fallback under this device's schedule config — `dhw_mode` is the most likely candidate for
   whatever actually decides base_temp-vs-active-schedule-entry precedence, not tested.
1. What is `boiler_status` bit `0x200` (observed set in the 2026-08-27 snapshot, not covered by any
   currently-decoded bit)?
1. What are the true units of `report.current` and `report.power_cons`?
1. What is the actual device-required minimum inter-request interval — is it really 2000 ms, or
   does the javadoc's "1 second" reflect an earlier, more accurate figure? Findable read-only: with
   the binding disabled, send a burst of `/retrieve` calls at progressively shorter gaps (e.g. 2000 →
   1500 → 1000 → 500 ms) and find where empty replies start appearing consistently rather than
   intermittently. Confounded by the device's general flakiness (empty replies happen at any
   interval), so look for a change in *rate*, not a hard cutoff.
1. What is the write payload shape for `schedules`? (Not attempted; first task of schedule
   implementation.)
