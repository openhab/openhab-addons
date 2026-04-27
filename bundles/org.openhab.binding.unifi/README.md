# UniFi Binding

The UniFi Binding integrates Ubiquiti UniFi devices into openHAB, covering the UniFi Network, UniFi Protect, and UniFi Access product families:

<!--list-subs-->

Installing this binding provides support for all three product families at once.
Each family has its own documentation, thing types, and channels, but they share a single `unifi:controller` bridge that holds the credentials for your UniFi console.
You configure the bridge once and attach Network, Protect, and Access things to it — one login per console, no matter how many devices you have.

## Supported Bridges

- **Controller**: This bridge represents a connection to a UniFi console.

One bridge per console is recommended. Network, Protect, and Access things all attach to the same bridge and share its session.

> **Multiple Consoles** If you have more then one UniFi console, like a dedicated UNVR, each console will require its own `unifi:controller` bridge along with a user that exists on that console as each handles its own login and session management.

## Bridge Configuration

Required configuration parameters are:

- **host**: Hostname or IP address of the UniFi console.
- **username**: Local user account on the UniFi console.
- **password**: Password for the local user account.

Additional optional parameters:

- **port**: Port on which the console is listening. Defaults to `443` (modern UniFiOS consoles). Legacy stand-alone Network controllers typically use `8443` — set this explicitly for those.
- **unifios**: Whether this is a UniFiOS console (UDM/UDR/UNVR/Cloud Key Gen2 Plus). Selects `/api/auth/login` (UniFiOS) vs `/api/login` (legacy). Defaults to `true`.
- **timeoutSeconds**: HTTP request timeout in seconds. Defaults to `30`.

## Usage

This binding does not expose device things on its own.
Install one or more of the specialist child bindings listed above, then attach their things to a `unifi:controller` bridge:

- [**UniFi Network**](/addons/bindings/unifi.network/) — wireless / wired clients, WLANs, PoE ports, access points, sites, vouchers.
- [**UniFi Protect**](/addons/bindings/unifi.protect/) — cameras, floodlights, environmental sensors, doorbells, doorlocks, chimes.
- [**UniFi Access**](/addons/bindings/unifi.access/) — doors, readers, door lock rules, emergency state, access events.

## Full Example

A typical UDM-Pro setup with all three families configured against the same local user looks like this in text config:

`.things` file:

```java
Bridge unifi:controller:udm "UDM Pro" [
    host="192.168.1.1",
    username="openhab",
    password="secret"
] {
    // Network child things attach directly to unifi:controller:
    // Thing unifi:wirelessClient:phone "My Phone" [ cid="aa:bb:cc:dd:ee:ff", site="default" ]

    // Protect NVR sub-bridge (inherits host/credentials from the parent):
    Bridge unifiprotect:nvr nvr "Protect NVR" [] {
        // Thing unifiprotect:camera front "Front Camera" [ deviceId="..." ]
    }

    // Access sub-bridge (inherits host/credentials from the parent):
    Bridge unifiaccess:bridge access "UniFi Access" [ refreshInterval=300 ] {
        // Thing unifiaccess:door main "Main Door" [ deviceId="..." ]
    }
}
```

## Upgrading from earlier UniFi bindings

### Who needs to read this

If you previously ran any of the UniFi Network, UniFi Protect, or UniFi Access bindings, follow these steps after upgrading.
New installations can skip to the next section.

### Step 1 — Create a `unifi:controller` bridge

All three family bindings now require a shared `unifi:controller` bridge that holds the console credentials.
**If you already have one (from the Network binding), skip ahead to Step 2.**

1. In the openHAB UI, add a new thing from the **UniFi Binding** → **UniFi Controller**.
1. Enter the **hostname**, **username**, and **password** of your UniFi console (the same credentials you previously had on your `unifiprotect:nvr` or `unifiaccess:bridge`).
1. For a modern UniFi OS console (UDM / UDM Pro / UDR / UNVR / Cloud Key Gen2 Plus) the defaults (`port=443`, `unifios=true`) are correct.

For a **legacy stand-alone Network Controller**, set `port=8443` and `unifios=false`.

> **Multiple Consoles** If you have more then one UniFi console, like a dedicated UNVR, each console will require its own `unifi:controller` bridge along with a user that exists on that console as each handles its own login and session management.

### Step 2 — Reparent existing things under the controller bridge

**Network binding users:** Your existing Network things (`unifi:wirelessClient`, `unifi:wlan`, `unifi:site`, etc.) already use `unifi:controller` as their bridge — no changes needed.
The controller bridge now serves as the shared authentication bridge for all three families, but its thing type UID and configuration parameters are unchanged.

**Protect and Access binding users:** `unifiprotect:nvr` and `unifiaccess:bridge` now inherit host/credentials from the parent `unifi:controller` bridge.
After upgrade they go `OFFLINE` until you set their parent:

1. In the openHAB UI, open your existing `unifiprotect:nvr` (or `unifiaccess:bridge`) thing, click **Edit**, and set its **Bridge** to the `unifi:controller` you created in Step 1. Save.
1. The thing comes back online. All child things (cameras, lights, sensors, doors, readers) stay linked — no action needed on those.
1. The old `hostname`/`username`/`password` fields stored on the Protect/Access bridge are silently ignored. You can remove them from your `.things` file or leave them.

**Text-config `.things` users:** nest Network, Protect, and Access things inside the controller bridge block:

```java
Bridge unifi:controller:home "UniFi Console" [ host="192.168.1.1", username="openhab", password="secret" ] {
    // Network things attach directly:
    Thing unifi:wirelessClient phone "My Phone" [ cid="aa:bb:cc:dd:ee:ff", site="default" ]

    // Protect NVR sub-bridge:
    Bridge unifiprotect:nvr nvr "Protect NVR" [] {
        Thing unifiprotect:camera front "Front Camera" [ deviceId="..." ]
    }

    // Access sub-bridge:
    Bridge unifiaccess:bridge access "Access" [ refreshInterval=300 ] {
        Thing unifiaccess:door main "Main Door" [ deviceId="..." ]
    }
}
```

### Step 3 — Install the UniFi binding (Karaf / manual installs only)

If you install addons via the openHAB UI addon manager, skip this step — it handles names automatically.

If you use `feature:install` or pin features in `runtime.cfg`, install `openhab-binding-unifi` — this single feature includes all three child bindings (Network, Protect, Access) automatically.

| Old feature name               | New feature name           |
|--------------------------------|----------------------------|
| `openhab-binding-unifi`        | `openhab-binding-unifi`    |
| `openhab-binding-unifiprotect` | `openhab-binding-unifi`    |
| `openhab-binding-unifiaccess`  | `openhab-binding-unifi`    |

### What stays the same

- All thing type UIDs (`unifi:controller`, `unifi:wirelessClient`, `unifiprotect:nvr`, `unifiprotect:camera`, `unifiaccess:bridge`, `unifiaccess:door`, etc.) are unchanged.
- Channel UIDs, item links, rules, persistence, and UI widgets continue to work without edits.
- `.items` files do not need changes.

### Troubleshooting

- **`unifiprotect:nvr` or `unifiaccess:bridge` shows `CONFIGURATION_ERROR`:** the thing has no parent bridge. Complete Step 2 above.
- **`unifi:controller` bridge fails to authenticate:** if you have a legacy stand-alone Network Controller (not UniFi OS), add `port=8443` and `unifios=false` to the bridge configuration.
- **Things stuck in `UNINITIALIZED`:** the UniFi binding feature is not installed. Install `openhab-binding-unifi` which includes all child bindings.

## Session Sharing

The `unifi:controller` bridge authenticates once against the console when it comes online and shares that session with every child thing attached to it.
Protect and Access sub-bridges attached to the same controller bridge reuse the session directly — they never open their own login.
If the session expires the bridge re-authenticates automatically, and sessions are cached across openHAB restarts so a reboot typically does not require a fresh login.
