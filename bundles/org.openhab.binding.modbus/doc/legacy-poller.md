# Legacy `poller` + `data` Configuration

> **Legacy only:** This page preserves the original `poller` bridge plus child
> `data` Thing topology. New configurations should use
> [`poller2`](../README.md#poller2-configuration). For an opt-in, review-first
> conversion, see [Legacy Poller Migration](legacy-poller-migration.md).

Legacy Things remain supported for existing installations. They are not part of
`poller2`, which configures channels directly on its poller bridge.

## Topology

A legacy endpoint (`tcp` or `serial`) contains one `poller` bridge per Modbus
read request. Each `poller` contains one or more `data` Things that decode the
returned bits or registers. A write-only `data` Thing can instead be a direct
child of the endpoint.

```java
Bridge modbus:tcp:plant [ host="192.0.2.10", port=502, id=1 ] {
    Bridge poller holding [ start=100, length=2, refresh=1000, type="holding" ] {
        Thing data setpoint [ readStart="100", readValueType="int16",
            writeStart="100", writeValueType="int16", writeType="holding" ]
    }
}
```

## Legacy configuration

| Resource | Required settings | Optional settings |
| --- | --- | --- |
| `poller` | `length`, `type` | `start` (default `0`), `refresh` (default `500` ms), `maxTries` (default `3`), `cacheMillis` (default `50` ms) |
| readable `data` | `readStart`, `readValueType` | `readTransform`, `updateUnchangedValuesEveryMillis` |
| writable `data` | `writeStart`, `writeType`; `writeValueType` for holding registers | `writeTransform`, `writeMaxTries`, `writeMultipleEvenWithSingleRegisterOrCoil` |

All Modbus addresses are zero-based data-frame addresses. For coils and discrete
inputs use `readValueType="bit"`. For a bit within a holding/input register,
use `X.Y`, where `Y=0` is the least-significant bit. A readable `data` address
must be within the parent poller's requested range.

The legacy `data` Thing offers `number`, `switch`, `contact`, `dimmer`,
`datetime`, `string`, and `rollershutter` channels, plus last read/write
success/error diagnostics. Send `REFRESH` to a linked Item to request a poll;
`cacheMillis` can serve a recent response instead of issuing a new request.

## JSON write transformations (legacy `data` Things only)

`poller2` does **not** support JSON-producing write transformations. Use its
configured scalar writes or Thing actions instead.

A legacy `data` Thing's `writeTransform` may return a JSON array of write
requests. This bypasses that Thing's `writeStart`, `writeValueType`, and
`writeType` settings. Each object requires `functionCode` (`5`, `6`, `15`, or
`16`), `address` (zero-based), and `value` (an array; coils use `0` or `1`).
`maxTries` is optional and defaults to `3`.

```json
[
  { "functionCode": 16, "address": 5412, "value": [1, 0, 5] },
  { "functionCode": 6, "address": 555, "value": [3], "maxTries": 10 }
]
```

An empty array (`[]`) suppresses writes. Treat JSON transforms as manual work
when migrating: the migration preview reports them but does not generate or
apply a `poller2` replacement.

## Maintaining a legacy installation

Keep each poll window small enough for the device and avoid ranges that include
reserved registers. Verify values after any address or value-type change,
including byte/word-order variants. For a staged migration, retain legacy
Things and links through live comparison, then remove them only as a separate,
backed-up cleanup step described in [Legacy Poller Migration](legacy-poller-migration.md).
