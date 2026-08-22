# LOQED Binding

The LOQED binding controls LOQED Touch and LOQED Pure smart locks either through the cloud Integrations API or directly through a LOQED Bridge on the local network.

## Supported Things

- `account`: Bridge representing a LOQED account and its personal access token.
- `local-bridge`: Direct local connection using signed commands and signed outgoing webhooks.
- `lock`: A LOQED smart lock connected through either bridge type.

## Discovery

Discovery is supported by the `account` bridge only.
After the bridge is online, start a discovery scan to add all locks available to the Inbox.
Each discovered lock is linked to the account bridge and is uniquely identified by its LOQED lock ID.
Locks connected through a `local-bridge` must be configured manually.

## `account` Configuration

Create a personal access token at [LOQED Personal Access Tokens](https://integrations.loqed.com/personal-access-tokens).
The account used to create the token must have administrator access to the locks.

| Name            | Type    | Description                                | Default | Required | Advanced |
|-----------------|---------|--------------------------------------------|---------|----------|----------|
| apiToken        | text    | LOQED personal access token.               | N/A     | yes      | no       |
| refreshInterval | integer | Interval between API refreshes in seconds. | 60      | no       | yes      |

The refresh interval cannot be shorter than 30 seconds.

## `local-bridge` Configuration

Create an incoming API key and obtain the outgoing-webhook authentication key at [LOQED API Configuration](https://app.loqed.com/API-Config).
The bridge authentication key is required for local operation, but a cloud personal access token is not.

| Name            | Type    | Description                                                       | Default | Required |
|-----------------|---------|-------------------------------------------------------------------|---------|----------|
| host            | text    | IP address, hostname, or base URL of the LOQED Bridge.            | N/A     | yes      |
| bridgeKey       | text    | Base64 bridge authentication key for webhook management/signing.  | N/A     | yes      |
| callbackBaseUrl | text    | Optional override for the automatically detected openHAB URL.     | Auto    | no       |
| refreshInterval | integer | Fallback status refresh interval in seconds.                      | 60      | no       |

The binding registers its callback below `/loqed/webhook` and verifies the `TIMESTAMP` and `HASH` headers before accepting an event.
The binding obtains the primary IPv4 address and HTTP port from the openHAB runtime.
Set the advanced callback base URL only when automatic detection returns an address that the bridge cannot reach, for example with multiple network interfaces, VLANs, or some Docker network modes.
State changes are delivered instantly by the webhook; the status endpoint is only used initially and as a one-minute fallback.

## Lock Configuration

Cloud lock Things are normally created through discovery.
Local lock Things are configured manually because their signing credentials belong to the lock and cannot be discovered from the bridge.
Use the lock ID and local API key shown on the LOQED API configuration page.

| Name       | Type    | Description                                                    | Required            |
|------------|---------|----------------------------------------------------------------|---------------------|
| lockId     | text    | Unique lock identifier from LOQED.                             | yes                 |
| keySecret  | text    | Base64 secret used to sign commands.                           | with `local-bridge` |
| localKeyId | integer | Numeric local key ID belonging to `keySecret`, from 0 to 250.  | with `local-bridge` |

## Channels

| Channel          | Type                 | Read/Write | Description                                                                                                     |
|------------------|----------------------|------------|-----------------------------------------------------------------------------------------------------------------|
| lock             | Switch               | RW         | `ON` sets night lock; `OFF` sets day lock.                                                                      |
| bolt-state       | String               | RW         | Current bolt state. `OPEN` retracts the latch; on doors with an outside handle LOQED treats it like `DAY_LOCK`. |
| battery-level    | Number:Dimensionless | R          | Remaining battery level in percent.                                                                             |
| battery-type     | String               | R          | Configured battery chemistry.                                                                                   |
| party-mode       | Switch               | R          | Whether Open House (party) mode is enabled.                                                                     |
| guest-access     | Switch               | R          | Whether guest access mode is enabled.                                                                           |
| twist-assist     | Switch               | R          | Whether twist assist is enabled.                                                                                |
| touch-to-connect | Switch               | R          | Whether the 500 m geofence restriction for Touch to Open is removed.                                            |

## Full Example

### Thing Configuration

```java
Bridge loqed:account:home "LOQED Account" [ apiToken="YOUR_PERSONAL_ACCESS_TOKEN", refreshInterval=60 ] {
    Thing lock frontdoor "Front Door" [ lockId="Yq1gK4oeE9KWe0ByxjX2" ]
}
```

Local alternative:

```java
Bridge loqed:local-bridge:home "LOQED Local Bridge" [
    host="192.168.1.20",
    bridgeKey="YOUR_BASE64_BRIDGE_AUTHENTICATION_KEY"
] {
    Thing lock frontdoor "Front Door" [
        lockId="Yq1gK4oeE9KWe0ByxjX2",
        keySecret="YOUR_BASE64_LOCAL_KEY_SECRET",
        localKeyId=3
    ]
}
```

### Item Configuration

```java
Switch FrontDoor_Lock "Front Door Lock" { channel="loqed:lock:home:frontdoor:lock" }
String FrontDoor_BoltState "Front Door [%s]" { channel="loqed:lock:home:frontdoor:bolt-state" }
Number:Dimensionless FrontDoor_Battery "Front Door Battery [%d %%]" { channel="loqed:lock:home:frontdoor:battery-level" }
```

To retract the latch from a rule, send the `OPEN` command to `FrontDoor_BoltState`.

## Security

The personal access token and local API keys grant control over the lock.
Store them as secrets, revoke unused credentials on the LOQED website, and do not include them in logs or support bundles.
