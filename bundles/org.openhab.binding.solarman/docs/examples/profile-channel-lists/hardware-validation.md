# Single-phase Hardware Validation Procedure

Use this procedure with the selected model/firmware and one active logger connection.
The README records existing reported coverage; this procedure does not claim additional hardware tests.
Choose only the measurements and controls needed for the installation.

## Record the Baseline

1. Record inverter model/firmware, logger mode, selected profile, installed bundle and comparison time/source.
1. Save Thing configuration, Item links, rule state and original values of any settings to be changed.
1. Check ONLINE status and fresh updates, then compare battery, PV, grid and load readings with the inverter under comparable conditions and units.
1. Verify identification/version Strings, current/power direction, phase-versus-total meaning and energy-counter meaning separately.
1. Treat NULL/UNDEF or stale persisted values as unavailable observations; a timestamp for one linked measurement does not certify a complete poll.

## Validate Selected Controls

1. Read the README write warning, verify the profile and inspect published bounds/options; dependent limits must be available from fresh device data.
1. Temporarily suspend rules that could overwrite the tested settings. Choose a suitable temporary value, change one setting and compare requested value, binding readback, Item state and the inverter display.
1. For six SOC periods, first verify all six capacity links. If testing queued commands, keep within the bounded queue and retain evidence for each separate transaction; a final matching state alone does not qualify intermediate queue behavior.
1. A repeated equal assignment should keep the device value unchanged and emit no success INFO for an actual change; lack of INFO alone does not prove handling or absence of Modbus traffic.
1. If an acknowledged mismatch occurs naturally, retain the complete surrounding evidence. At most one repeat is permitted with fresh limits/masks. Transport failure, invalid acknowledgement or incomplete confirmation must not cause blind repeats; do not introduce device communication faults merely to test recovery.
1. If testing program times, record all original times, preserve valid ordering between neighboring periods, confirm change/repeat/restore separately and then restore all temporary settings.
1. Restore saved settings and the intended rule state, checking device and Item agreement before finishing.

## Interpret Differences

- Inverter total power at 175 is not a same-register replacement for legacy phase word 173. External CT total 172 is not phase word 170.
- Single-phase inverter current at 164 changes from signed legacy to unsigned advanced interpretation; matching positive readings do not qualify negative values.
- Some power channels change signedness; do not infer charge/export direction from a channel name alone.
- Decoded fault words 103..106 are a partial replacement for the legacy raw 101..106 alert. Validate real fault behavior only when naturally available.
- Separate String metadata does not qualify those addresses on another inverter profile.
- A model-specific legacy-only function requires a reviewed source/register meaning before any new advanced channel is proposed.

## Evidence Boundary

Record each reading, control change, equal assignment and restoration as pending, passed or failed with its actual scope.
An Item command event or write acknowledgement alone is not device readback.
Software parity, a clean log excerpt and a successful build do not establish hardware coverage, a fixed error rate or a remedy for an earlier V5 failure.
Handler-level existing-Thing tests do not replace JSONDB/UI/OSGi upgrade and Item-link checks.
This procedure sends no commands itself and requires no runtime rebuild unless a separately identified implementation change is made.
