# Deferred: ProEM50 3-Meter Layout + Gen2 lastPower1

Deliberately excluded from `shelly_fixmeters` PR to keep it bugfix-only.
Target: a follow-up PR on a new branch derived from main after `shelly_fixmeters` merges.

---

## What was reverted

### Feature 1 — ProEM50 3-meter layout (relay PM decoupled from clamp indexing)

ProEM50 has 2 EM clamp components (`em1:0`, `em1:1`) AND 1 relay output with PM (`pm1:0`).
Without this feature, `updateRelayStatus()` writes relay data to `emeters[0]`, which
`updateEmStatus(em10)` immediately overwrites → relay output power is permanently lost.

**Fix:** when `hasEM1Clamps=true`, route relay PM data to `emeters[numEM1Clamps + rIdx]`
so clamp slots (0, 1) and relay slot (2) are independent.

**Resulting layout for ProEM50:**
- `meter1` → `em1:0` clamp data
- `meter2` → `em1:1` clamp data
- `meter3` → `pm1:0` relay output data

### Feature 2 — `hasEM1Clamps` / `numEM1Clamps` profile flags

Add to `ShellyDeviceProfile`:
```java
public boolean hasEM1Clamps = false; // true when device has dedicated em1:x clamp components
public int numEM1Clamps = 0;         // count of em1:x clamp components (typically 2 on ProEM50)
```

Set in both `Shelly2ApiClient.getDeviceProfile()` and `Shelly2ApiRpc.getDeviceProfile()`:
```java
int clampCount = (dc.em10 != null ? 1 : 0) + (dc.em11 != null ? 1 : 0);
if (clampCount > 0) {
    profile.hasEM1Clamps = true;
    profile.numEM1Clamps = clampCount;
}
// fromDeviceConfig must include clampCount:
int fromDeviceConfig = dc.pm10 != null ? 1 : dc.em0 != null ? 3 : clampCount > 0 ? clampCount : -1;
// After resolveNumMeters, override for ProEM50:
if (profile.hasEM1Clamps) {
    profile.numMeters = profile.numEM1Clamps + profile.numRelays; // e.g. 2 + 1 = 3
}
```

Slot routing in `Shelly2ApiClient.updateRelayStatus()`:
```java
int slotIdx = profile.hasEM1Clamps ? profile.numEM1Clamps + rIdx : rIdx;
ShellySettingsEMeter emeter = (status.emeters != null && slotIdx < status.emeters.size())
        ? status.emeters.get(slotIdx)
        : new ShellySettingsEMeter();
// ... populate emeter ...
updateMeter(status, slotIdx, emeter, channelUpdate);
```

### Feature 5 — `lastMinuteW` field + Gen2 `lastPower1` channel

**Motivation:** Plus 1PM, Pro 2PM etc. report `aenergy.byMinute[0]` (avg W over last minute)
in switch status, but this was never surfaced to the `lastPower1` channel.

**Add to `ShellySettingsEMeter` in `Shelly1ApiJsonDTO.java`:**
```java
public Double lastMinuteW; // Gen2: avg power last minute (Wh/min, from aenergy.byMinute[0])
```

**Populate in `Shelly2ApiClient.updateRelayStatus()`** (inside `if (rs.aenergy != null)` block):
```java
if (rs.aenergy.byMinute != null && rs.aenergy.byMinute.length > 0) {
    emeter.lastMinuteW = rs.aenergy.byMinute[0];
}
```

**Populate in cover status path** (inside `if (cs.aenergy != null)` block):
```java
if (cs.aenergy.byMinute != null && cs.aenergy.byMinute.length > 0) {
    emeter.lastMinuteW = cs.aenergy.byMinute[0];
}
```

**Update in `ShellyComponents.updateEMeters()`** (after `CHANNEL_EMETER_PFACTOR`):
```java
if (emeter.lastMinuteW != null) {
    updated |= thingHandler.updateChannel(groupName, CHANNEL_METER_LASTMIN1,
            toQuantityType(emeter.lastMinuteW, DIGITS_WATT, Units.WATT));
}
```

**Channel creation in `ShellyChannelDefinitions.createEMeterChannels()`:**
```java
addChannel(thing, newChannels, gen2 || emeter.lastMinuteW != null, group, CHANNEL_METER_LASTMIN1);
```
(Add this line after the `CHANNEL_EMETER_PFACTOR` addChannel call.)

### Feature 6 — ProEM50 XML labels + `meter3` group

In `shellyGen2_relay.xml`, thing-type `shellyproem50`:
```xml
<channel-group id="meter1" typeId="meter">
    <label>Power Meter 1 (Clamp)</label>
</channel-group>
<channel-group id="meter2" typeId="meter">
    <label>Power Meter 2 (Clamp)</label>
</channel-group>
<channel-group id="meter3" typeId="meter">
    <label>Power Meter (Relay)</label>
</channel-group>
```

---

## Tests to add

In `ShellyComponentsTest.java`:

**`lastMinuteW_updatesLastPower1Channel`** — emeter with `lastMinuteW=48.5`, assert `CHANNEL_METER_LASTMIN1` updated.

**`noLastMinuteW_doesNotUpdateLastPower1Channel`** — emeter with `lastMinuteW=null`, assert `CHANNEL_METER_LASTMIN1` never called.

**`gen2Roller_lastMinuteW_updatesLastPower1`** — roller Gen2 profile, `lastMinuteW=295.0`, assert `CHANNEL_METER_LASTMIN1` updated.

**`proem50_relayDataGoesToSlot2`** — build profile with `hasEM1Clamps=true, numEM1Clamps=2, numRelays=1, numMeters=3`; populate `emeters[0/1]` with clamp data and `emeters[2]` with relay output; assert no cross-contamination.

---

## Devices to test (manual)

- **ProEM50** (`shellyproem50`): confirm meter1/meter2 show clamp readings, meter3 shows relay output
- **EM Mini** (single clamp): confirm meter1 shows clamp, meter2 shows relay output
- **Plus 1PM** / **Pro 2PM**: confirm `lastPower1` channel now receives values
- **Plus 2PM roller**: confirm `lastPower1` works in roller mode
