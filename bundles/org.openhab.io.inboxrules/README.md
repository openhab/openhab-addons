# Inbox Rules

When a new device is automatically discovered by any of the installed bindings, it appears in the inbox.
Normally, these discovered devices require manual approval before they can be added as active Things in the system.

**Inbox Rules** automates this workflow using configurable rules.
Matching discovery results are automatically approved by the core inbox processor, and supported post-approval actions are then applied to the new Thing through the Thing registry.

## Configuration

Configure the add-on through Main UI under **Settings → Add-on Settings → Inbox Rules**, or set `rules` in ConfigAdmin. Each entry is one rule:

Set `rulesEnabled` to `false` to disable all rules; it defaults to `true`.

```sh
criteriaKey=value; actionKey=value
```

Rules are evaluated in order; the first matching rule approves the result.
A rule without action keys only approves it.
Lines beginning with `#` or `//` are comments.
Invalid rules are ignored.

### Criteria

- `thingUid`, `bridgeUid`, and `label` match the corresponding discovery result values.
- `representationProperty` matches the value stored at the result's representation property.
- `propertyMatch` and `configMatch` accept comma-separated `key=value` or `key:value` comparisons against discovery properties.
- Criteria support `*` (zero or more characters) and `?` (one character).

### Actions

- `setLabel` changes the approved Thing's label.
- `setLocation` sets its location.
- `setProperties` and `setConfig` accept comma-separated assignments, such as `setProperties=room=Kitchen,source=${properties.id}`.

Action values support `${label}`, `${thingUID}`, `${bridgeUID}`, and `${properties.name}` interpolation.
Unknown interpolation variables become empty strings.
Actions are applied once when the approved Thing is added to the registry.

**Example:**

```text
# Approve a discovered device and enrich it
thingUid=zwave:device:*; label=Front Door Sensor; setLabel=${label}; setLocation=Ground Floor; setProperties=room=Entry,serial=${properties.serialNumber}
```
