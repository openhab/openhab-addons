# UniFi Binding

This binding provides a shared bridge to Ubiquiti UniFi consoles (UDM, UDR, UNVR, Cloud Key, and legacy Network controllers).
And it has specialist extensions for the following product families:

<!--list-subs-->

A single `unifi:controller` bridge owns the authenticated session used by the UniFi Network, UniFi Protect, and UniFi Access child bindings.
On a UniFi OS console where the same local user can see Network, Protect, and Access, configuring one controller bridge means one `POST /api/auth/login` per console — no matter how many child things you attach — so the console's local-user rate limits and lockout counters are not tripped.

This binding authenticates via a local user account on the console and maintains the cookie/CSRF session used by every child binding that attaches to the bridge.
The session is persisted under `$OPENHAB_USERDATA/cache/unifi/` so restarts do not re-login unless the cached session has expired.

## Supported Bridges

- **Controller**: This bridge represents a connection to a UniFi console.

One bridge per unique `(host, username)` is recommended — additional child bindings configured with the same host and username will automatically reuse the session.

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

- **UniFi Network** — wireless / wired clients, WLANs, PoE ports, access points, sites, vouchers.
- **UniFi Protect** — cameras, floodlights, environmental sensors, doorbells, doorlocks, chimes.
- **UniFi Access** — doors, readers, door lock rules, emergency state, access events.

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
If you already have one (from the Network binding), skip ahead to Step 2.

1. In the openHAB UI, add a new thing from the **UniFi Binding** → **UniFi Controller**.
1. Enter the **hostname**, **username**, and **password** of your UniFi console (the same credentials you previously had on your `unifiprotect:nvr` or `unifiaccess:bridge`).
1. For a modern UniFi OS console (UDM / UDM Pro / UDR / UNVR / Cloud Key Gen2 Plus) the defaults (`port=443`, `unifios=true`) are correct.
For a **legacy stand-alone Network Controller**, set `port=8443` and `unifios=false`.

### Step 2 — Reparent existing Protect and Access bridges

`unifiprotect:nvr` and `unifiaccess:bridge` now inherit host/credentials from the parent `unifi:controller` bridge.
After upgrade they go `OFFLINE` until you set their parent:

1. In the openHAB UI, open your existing `unifiprotect:nvr` (or `unifiaccess:bridge`) thing, click **Edit**, and set its **Bridge** to the `unifi:controller` you just created. Save.
1. The thing comes back online. All child things (cameras, lights, sensors, doors, readers) stay linked — no action needed on those.
1. The old `hostname`/`username`/`password` fields stored on the Protect/Access bridge are silently ignored. You can remove them from your `.things` file or leave them.

**Text-config `.things` users:** nest the Protect and Access bridges inside the controller bridge block:

```java
Bridge unifi:controller:home "UniFi Console" [ host="192.168.1.1", username="openhab", password="secret" ] {
    Bridge unifiprotect:nvr nvr "Protect NVR" []
    Bridge unifiaccess:bridge access "Access" [ refreshInterval=300 ]
}
```

### Step 3 — Install the new feature names (Karaf / manual installs only)

If you install addons via the openHAB UI addon manager, skip this step — it handles names automatically.

If you use `feature:install` or pin features in `runtime.cfg`, update to the new names:

| Old feature name               | New feature name                |
|--------------------------------|---------------------------------|
| `openhab-binding-unifi`        | `openhab-binding-unifi-network` |
| `openhab-binding-unifiprotect` | `openhab-binding-unifi-protect` |
| `openhab-binding-unifiaccess`  | `openhab-binding-unifi-access`  |

Installing any child feature automatically pulls in the parent (`openhab-binding-unifi`).

### What stays the same

- All thing type UIDs (`unifi:controller`, `unifi:wirelessClient`, `unifiprotect:nvr`, `unifiprotect:camera`, `unifiaccess:bridge`, `unifiaccess:door`, etc.) are unchanged.
- Channel UIDs, item links, rules, persistence, and UI widgets continue to work without edits.
- `.items` files do not need changes.

### Troubleshooting

- **`unifiprotect:nvr` or `unifiaccess:bridge` shows `CONFIGURATION_ERROR`:** the thing has no parent bridge. Complete Step 2 above.
- **`unifi:controller` bridge fails to authenticate:** if you have a legacy stand-alone Network Controller (not UniFi OS), add `port=8443` and `unifios=false` to the bridge configuration.
- **Things stuck in `UNINITIALIZED`:** the matching child feature is not installed. Install `openhab-binding-unifi-network`, `openhab-binding-unifi-protect`, or `openhab-binding-unifi-access` as needed.

## Session Sharing

The `unifi:controller` bridge performs one `POST /api/auth/login` against the console when it comes online, captures the returned session cookie and CSRF token, and publishes them to every child thing attached to it.
Protect and Access sub-bridges attached to the same controller bridge reuse that session directly — they never open their own login.
CSRF tokens rotate automatically as the console issues new ones, 401 responses trigger a single re-authentication that all children pick up, and the cached session is written to `$OPENHAB_USERDATA/cache/unifi/` so an openHAB restart typically skips the login entirely as long as the cached session is still valid.

On a UDM running all three family bindings, this means exactly one login per openHAB startup per console, no matter how many cameras, doors, wireless clients, or other things you have configured.
