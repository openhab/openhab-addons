# Viessmann Binding

This binding connects Viessmann devices via the new Viessmann API.
It provides features similar to the ViCare App.

---

## Important Notes

You must register your ViCare account at the [Viessmann developer portal](https://developer.viessmann-climatesolutions.com/) and create a Client ID.

- `name` – e.g., `openhab`
- `Google reCAPTCHA` – `off`
- `Redirect URI` – `http://localhost:8080/viessmann/authcode/` (*)

(*) If your openHAB system runs on a different port than `8080`, adjust the Redirect URI.

**Hint:** You can add multiple Redirect URIs on the Viessmann developer portal by clicking the plus sign.

---

## Supported Things

- `account` – Connects to the Viessmann API to link the `gateway` thing.
- `bridge` – Connects directly to the Viessmann API and links the first installed gateway.
- `gateway` – Connects to the `account` thing (Discovery).
- `device` – Represents individual devices (Discovery).

---

## Binding Configuration

### Account Thing

| Parameter           | Required | Default | Description |
|--------------------|----------|---------|-------------|
| `apiKey`            | Yes      | –       | Client ID from the Viessmann developer portal |
| `user`              | Yes      | –       | E-Mail registered for the ViCare App |
| `password`          | Yes      | –       | Password registered for the ViCare App |
| `apiCallLimit`      | No       | 1450    | Limit for API calls (*) |
| `bufferApiCommands` | No       | 450     | Buffer for commands (*) |
| `pollingInterval`   | No       | 0       | Interval in seconds to query available devices (**) |
| `pollingIntervalErrors` | No   | 60      | Interval in minutes to query errors |
| `disablePolling`    | No       | OFF     | Disables automatic polling |

### Bridge Thing

| Parameter           | Required | Default | Description |
|--------------------|----------|---------|-------------|
| `apiKey`            | Yes      | –       | Client ID from the Viessmann developer portal |
| `user`              | Yes      | –       | E-Mail registered for the ViCare App |
| `password`          | Yes      | –       | Password registered for the ViCare App |
| `installationId`    | No       | –       | Optional, will be discovered |
| `gatewaySerial`     | No       | –       | Optional, will be discovered |
| `apiCallLimit`      | No       | 1450    | Limit for API calls (*) |
| `bufferApiCommands` | No       | 450     | Buffer for commands (*) |
| `pollingInterval`   | No       | 0       | Interval in seconds to query available devices (**) |
| `pollingIntervalErrors` | No   | 60      | Interval in minutes to query errors |
| `disablePolling`    | No       | OFF     | Disables automatic polling |

### Gateway Thing

| Parameter           | Required | Default | Description |
|--------------------|----------|---------|-------------|
| `installationId`    | No       | –       | Optional, will be discovered |
| `gatewaySerial`     | No       | –       | Optional, will be discovered |
| `pollingIntervalErrors` | No   | 60      | Interval in minutes to query errors |
| `disablePolling`    | No       | OFF     | Disables automatic polling |

(*) Used to calculate refresh time in seconds  
(**) If set to 0, interval is calculated automatically by the binding.

---

## Channels

### Account

| Channel             | Type   | RO/RW | Description |
|--------------------|--------|-------|-------------|
| `count-api-calls`  | Number | RO    | Number of API calls today |

### Bridge

| Channel               | Type   | RO/RW | Description |
|----------------------|--------|-------|-------------|
| `count-api-calls`    | Number | RO    | Number of API calls today |
| `error-is-active`    | Switch | RO    | Indicates active error |
| `last-error-message` | String | RO    | Last error message from installation |
| `run-query-once`     | Switch | W     | Run device query once |
| `run-error-query-once` | Switch | W   | Run error query once |

### Gateway

| Channel               | Type   | RO/RW | Description |
|----------------------|--------|-------|-------------|
| `error-is-active`    | Switch | RO    | Indicates active error |
| `last-error-message` | String | RO    | Last error message from installation |
| `run-query-once`     | Switch | W     | Run device query once |
| `run-error-query-once` | Switch | W   | Run error query once |

### Device

Channels are generated automatically for available features.

---

## Thing Hierarchy

```
Account Thing
   │
   └── Gateway Thing (discovered via Account)
          │
          └── Device Things (discovered via Gateway)

Bridge Thing (connects directly to API)
   │
   └── Device Things (discovered via Bridge)
```

### Explanation

- **Account Thing** – Central connection to Viessmann API; discovers gateways and devices.
- **Gateway Thing** – Discovered via Account; devices discovered automatically.
- **Bridge Thing** – Connects directly to API; discovers devices via first installed gateway at your heating system.
- **Device Thing** – Represents individual devices; connected via Gateway or Bridge.

---

## Examples: Thing Definitions in openHAB

### Account Thing

```javascript
Thing viessmann:account:myaccount "Viessmann Account" @ "Home" [
    apiKey="YOUR_CLIENT_ID",
    user="YOUR_EMAIL",
    password="YOUR_PASSWORD"
]
```

### Bridge Thing

```javascript
Thing viessmann:bridge:mybridge "Viessmann Bridge" @ "Home" [
    apiKey="YOUR_CLIENT_ID",
    user="YOUR_EMAIL",
    password="YOUR_PASSWORD"
]
```

### Gateway Thing

```javascript
Thing viessmann:gateway:mygateway "Viessmann Gateway" @ "Home" [
    installationId="YOUR_INSTALLATION_ID",
    gatewaySerial="YOUR_GATEWAY_SERIAL"
]
```

### Device Thing

```javascript
Thing viessmann:device:heating "Heating Device" @ "Home" [
    deviceId="YOUR_DEVICE_ID"
]
```

---

## Breaking Changes

### Version 5.1.0

- Added new `account` and `gateway` things for gateway selection.  
  Existing `device` things can be switched manually to the new `gateway` as bridge.  
  After that, the `bridge` thing can be removed.


