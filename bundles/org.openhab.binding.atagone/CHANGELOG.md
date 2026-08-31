# Changelog

Version numbers here are informal, for tracking this binding independently of openHAB's own
release train (this binding lives in the official `openhab-addons` repo, which doesn't version
individual bindings on their own). Use these when discussing builds on the community forum.

## 0.2.0-beta — 2026-08-31

Changes since 0.1.0-beta: a redesign of how modes are activated/cancelled, new Thing Actions, a
regrouped and renamed channel taxonomy, and several bugs found and fixed via live verification
against the device.

### What's changed

- **42 channels, regrouped by subsystem instead of protocol block.** `control` is now "Operating
  Mode" (preset/timed-mode only — target-temperature and dhw-target-temperature moved to their
  own subsystems). `settings` is gone; `ch-control-mode` moved into Central Heating. Several
  channels dropped redundant subsystem prefixes (`dhw-temperature` → `hotwater#temperature`,
  `ch-water-temperature` → `heating#water-temperature`, etc). All descriptions rewritten for end
  users. **Breaking** — see Known limitations below for the upgrade path.
- **Trigger model:** `preset-mode` is now the only channel that can activate or cancel a mode.
  Writing a duration channel only updates the stored value for next time — it never triggers
  activation on its own. Matches how the device itself treats a duration field written alone, and
  avoids a duration write accidentally flipping the active mode.
- **New Thing Actions** for single-write custom-duration control, since duration channels no
  longer trigger activation: `activateVacation(seconds)`, `activateExtend(seconds)`,
  `activateFireplace(seconds)`, `cancelMode()`.
- Timed-preset durations must now be whole units (hours for extend/fireplace, days for vacation) —
  enforced client-side, both on the channel and the action path. A non-conforming value doesn't
  fail safely on the device (it triggers the same physical-confirmation/reboot pathway as a
  cancel), so it's rejected before ever reaching the device.
- **Fixed:** `cancelMode()` against a pending (future-scheduled, not-yet-active) vacation used to
  report "nothing to cancel" and leave the schedule fully armed. Now correctly clears it.
- **Fixed:** `hotwater#target-temperature` writes were silently accepted but never took effect —
  the device field behind it turned out to be read-only/derived, not the actual control target.
  See Known limitations.
- Loosened the local API client's connection timeout, retry count, and rate-limit gap — the
  previous values were tuned defensively without a reference point and are the likely cause of the
  binding going `OFFLINE` more often than other integrations polling the same device.

### Full current channel list

- **Operating Mode** (`control#`) — active preset (`auto`/`holiday`/`extend`/`fireplace`,
  `manual` read-only), timed-preset durations and remaining-time countdowns, vacation
  setpoint/start/end.
- **Central Heating** (`heating#`) — room/target temperature, outside temperature, weather
  status, circuit water temperature/pressure/return, control mode (room vs. weather-compensated),
  flame, burner target, modulation level, burning hours, and advanced diagnostics (boiler flow/
  return temperature, PCB temperature, min modulation level).
- **Hot Water** (`hotwater#`) — current temperature, flow rate. (See Known limitations below
  for the target-temperature channel.)
- **Device** (`device#`) — WiFi signal, supply voltage, controller resets, memory allocation,
  report timestamp.
- **Alerts** (`alerts#`) — device and boiler error codes.

### Known limitations

- **`hotwater#target-temperature` is read-only.** The obvious device field for it
  (`control.dhw_temp_setp`) turned out to be read-only/derived — it tracks whichever hot-water
  schedule period is currently active, and writes to it are silently accepted but have no effect.
  The real user-settable field lives in the device's schedule data, which this binding doesn't
  read or write yet (see Not yet implemented). Exposed read-only in the meantime rather than
  shipped as a write that silently does nothing.
- **Cancelling fireplace mode always needs a button press on the thermostat display.** Confirmed
  device behavior, not a binding limitation — no API payload avoids it. The binding logs a
  warning when this happens, and `cancelMode()` reports it via its return value.
- **Manual mode (`ch_mode=1`) is rejected on write**, as a conservative safety choice — writing it
  is believed to destabilize the boiler API, though this hasn't been re-verified against current
  firmware. Set manually on the thermostat display instead.
- **Upgrading from a previous build that used ungrouped or differently-grouped channels requires
  deleting and re-adding the Thing**, not just disabling/re-enabling it — confirmed live that an
  already-initialized Thing doesn't pick up a new channel-group structure from a jar swap alone.

### Not yet implemented

- Reading or writing the device's hot-water/heating schedules.
- Additional device settings as channels (frost protection, summer eco mode, legionella
  protection, heating curve/isolation/building-size, display brightness, time zone, language) —
  the device fields are documented in `DEVELOPERS.md` but not yet wired to channels.
- Thing properties for static device identity (boiler ID, installer ID, firmware version).

Full field-by-field API documentation, including what's verified vs. inferred, lives in
`DEVELOPERS.md` in this bundle.

## 0.1.0-beta — 2026-08-20 to 2026-08-25

Initial release. Auto-discovery via UDP broadcast (port 11000) and local-API pairing — no cloud
account, no MQTT broker. Ungrouped channels for room/target temperature, central heating circuit
status (water temperature/pressure/return, flame, burner target, modulation level), hot water
temperature, weather status, device diagnostics (WiFi signal, voltage, resets), and device/boiler
error codes. Preset-mode writes for holiday/extend/fireplace, including vacation start/end/
remaining-duration channels. Superseded by 0.2.0-beta's channel regrouping and trigger-model
redesign — see above for the current, non-breaking-if-you're-just-installing-now state.
