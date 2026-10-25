# Candy Binding

This binding monitors Candy Bianca washing machines and provides optional washing controls over the local simply-Fi Wi-Fi protocol.
Configure your simply-Fi account credentials and the washer's LAN address; the binding retrieves and matches the existing appliance key automatically.
Normal monitoring and washing commands use the LAN after account setup.

## Supported Things

| Thing Type | Description | Tested Model |
| --- | --- | --- |
| `bianca` | Candy Bianca washing machine with simply-Fi Wi-Fi | BWM4 137PH6/1-S, type F3E1, rated at 1300 rpm |

The full Thing type UID is `candy:bianca`.
Basic local monitoring and account-assisted setup have been verified with the listed model in openHAB 5.3.0-M3.
Physical starts, stops and a complete wash cycle remain unverified; controls are disabled by default.
Other Candy/Hoover products, hOn, Smart Touch/NFC and Bluetooth control are outside the supported scope.

## Discovery

Automatic discovery is not supported.
Add a `bianca` Thing manually in Main UI or a `.things` file, using the appliance address from your router's connected-device list.
Each washer is configured as a separate Thing; no bridge is required.

## Thing Configuration

Enroll the washer in simply-Fi using the manufacturer's application before configuring the binding.
Use a stable LAN address, preferably through a DHCP reservation, and the account credentials used by the simply-Fi application.
This binding targets Java 21 and openHAB 5.3.

### `bianca` Thing Configuration

| Name | Type | Description | Default | Required | Advanced |
| --- | --- | --- | --- | --- | --- |
| `hostName` | text | Appliance hostname or IPv4/IPv6 address; no scheme, port, path or credentials | N/A | Yes | No |
| `username` | text | Simply-Fi username/email; also needed for server program options when using a manual key | N/A | Without a manual key | No |
| `password` | text | Simply-Fi account password; masked in the UI | N/A | Without a manual key | No |
| `encryptionKey` | text | Manual 16-character key override; skips account-assisted key retrieval | N/A | No | Yes |
| `applianceId` | text | Restrict account lookup to one appliance; normally the correct key is matched automatically | N/A | No | Yes |
| `enableControl` | boolean | Allow explicit start/stop commands after a fresh device permission and state check | `false` | No | No |
| `refreshInterval` | integer | Seconds between completed ordinary polls; minimum 15 | `30` | No | No |
| `requestTimeout` | integer | Total local HTTP deadline in seconds; range 1–10 | `10` | No | Yes |

### Account Setup

An encryption key is not required when account credentials are configured.
The binding checks candidate account keys against the local washer's status response and keeps the matching key in memory.
Candidate keys are retained while the washer is unreachable, allowing local retries without repeating account login.
Restarting openHAB or changing Thing configuration requires account access again unless a manual key is supplied.
The binding does not enroll or reconfigure the appliance.

Additional verification or consent may need to be completed in the manufacturer's application for some accounts.
Keep account credentials and manual keys private, including in configuration backups.
Password fields mask their contents in the UI; this does not imply encrypted storage.
The local HTTP/XOR protocol should be accessible only from your trusted network.

### Optional Manual Key Setup

Existing local setups can supply `encryptionKey` instead of account credentials.
The key must contain exactly 16 printable ASCII characters without spaces and is independent of the Wi-Fi and account passwords.
A newly generated key or one derived from the SSID will not unlock an already enrolled washer.
Account credentials are still needed to retrieve the server program catalog; local monitoring and stop can use a manual key alone.

If the existing key is unavailable, the upstream [CandySimplyFi-tool](https://github.com/MelvinGr/CandySimplyFi-tool) can attempt recovery from a status response.
Its [`getkey` implementation](https://github.com/MelvinGr/CandySimplyFi-tool/blob/master/CandySimplyFi.cpp) requests `http-read.json` without configuring the appliance or sending washing commands.
This recovery method has not been verified on the listed model.

On Linux or WSL, with Git and a C++ compiler installed:

```bash
git clone https://github.com/MelvinGr/CandySimplyFi-tool.git /tmp/candy-key-tool
cd /tmp/candy-key-tool
g++ -std=c++11 CandySimplyFi.cpp -o simplyfi
timeout 15s ./simplyfi 192.0.2.10 getkey
```

Replace `192.0.2.10` with the washer's address.
If a candidate key is found, configure it privately and verify that the Thing can read valid status.
A recovered candidate alone does not establish compatibility.

## Channels

### Monitoring

| Channel | Type | Read/Write | Description |
| --- | --- | --- | --- |
| `machine-state` | String | R | `NO_STATE`, `SELECTING`, `RUNNING`, `PAUSED`, `DELAY_SELECTION`, `DELAYED`, `ERROR`, `FINISHED` |
| `program-phase` | String | R | `NONE`, `PREWASH`, `WASH`, `RINSE`, `LAST_RINSE`, `FINISHED`, `DRYING`, `ERROR`, `GOOD_NIGHT_PAUSE` |
| `program-number` | String | R | Bianca selector/program number, preserving identifier representation |
| `program-code` | String | R | Program code, separate from program number |
| `remaining-time` | Number:Time | R | Remaining wash duration, reported in seconds |
| `target-temperature` | Number:Temperature | R | Reported program temperature setting in °C |
| `target-spin-speed` | Number:Frequency | R | Reported program spin setting in rpm |
| `remote-control-enabled` | Switch | R | Device remote-operation permission, distinct from network availability |
| `error-code` | String | R | Advanced: raw device error code; absence does not imply no error |
| `last-update` | DateTime | R | Advanced: last successfully parsed local response time |
| `machine-mode-code` | Number | R | Advanced: raw mode code, including unknown codes |
| `program-phase-code` | Number | R | Advanced: raw phase code, including unknown codes |

`REFRESH` requests on monitoring channels share one device read, with request starts at least 15 seconds apart.
Missing or malformed fields become `UNDEF`; values from earlier responses are not retained as current telemetry.
Unknown normalized mode/phase codes become `UNDEF`, while raw numeric channels retain their codes.
Temperature and spin are settings, not measured water temperature or live motor speed.
Parsed `PAUSED` or `DRYING` states do not imply support for those controls.

### Programs and Controls

Use the [Thing Actions](#actions) to query programs and request starts or stops from rules without linking command Items.
The following channels remain available for UI widgets and existing Item-based rules.

| Channel | Type | Read/Write | Description |
| --- | --- | --- | --- |
| `program-catalog` | String | RW | Send `LIST` or `REFRESH` to request server options; state is a JSON array of program IDs, labels, startability, defaults and limits |
| `catalog-status` | String | R | `NOT_READY`, `ACCOUNT_REQUIRED`, `LOADING`, `READY`, `RATE_LIMITED`, `FAILED` |
| `selected-program` | String | RW | Select a program ID from the dropdown without starting the washer |
| `start` | Switch | W | Send `ON` to start the selected program with its server defaults |
| `start-program` | String | W | Advanced: send a supported program ID directly to start it with its server defaults |
| `stop` | Switch | W | Send `ON` to stop a running cycle or cancel a delayed start |
| `last-command-result` | String | R | `PENDING`, `SENT`, `CONFIRMED`, `REJECTED`, `DISABLED`, `BUSY`, `INVALID_PROGRAM`, `NOT_READY`, `REMOTE_DISABLED`, `FAILED`, `UNKNOWN` |

## Full Example

### `candy.things`

Replace the example address and account credentials.
Set `enableControl=true` when you want to allow explicit start/stop commands.

```java
Thing candy:bianca:washer "Candy Bianca" [
    hostName="192.0.2.10",
    username="owner@example.com",
    password="your-simply-fi-password",
    enableControl=false,
    refreshInterval=30,
    requestTimeout=10
]
```

### `candy.items`

```java
String CandyState "Washer [%s]" { channel="candy:bianca:washer:machine-state" }
String CandyPhase "Phase [%s]" { channel="candy:bianca:washer:program-phase" }
String CandyProgramNumber "Program number [%s]" { channel="candy:bianca:washer:program-number" }
String CandyProgramCode "Program code [%s]" { channel="candy:bianca:washer:program-code" }
Number:Time CandyRemaining "Remaining [%.0f min]" { channel="candy:bianca:washer:remaining-time", unit="min" }
Number:Temperature CandyTemperature "Temperature [%.0f °C]" { channel="candy:bianca:washer:target-temperature", unit="°C" }
Number:Frequency CandySpin "Spin [%.0f rpm]" { channel="candy:bianca:washer:target-spin-speed", unit="rpm" }
Switch CandyRemote "Remote permission [%s]" { channel="candy:bianca:washer:remote-control-enabled" }
String CandyError "Error code [%s]" { channel="candy:bianca:washer:error-code" }
DateTime CandyLastUpdate "Last read [%1$tF %1$tT]" { channel="candy:bianca:washer:last-update" }
Number CandyModeCode "Raw mode [%s]" { channel="candy:bianca:washer:machine-mode-code" }
Number CandyPhaseCode "Raw phase [%s]" { channel="candy:bianca:washer:program-phase-code" }
String CandyCatalogStatus "Program catalog [%s]" { channel="candy:bianca:washer:catalog-status" }
String CandyCommandResult "Command result [%s]" { channel="candy:bianca:washer:last-command-result" }
```

### `candy.rules`

These examples inspect cached server programs and react to a device-reported completion transition.

```java
rule "Candy programs available"
when
    Item CandyCatalogStatus changed to "READY"
then
    val candy = getActions("candy", "candy:bianca:washer")
    if (candy !== null) {
        logInfo("laundry", "Available programs: {}", candy.getPrograms())
    }
end

rule "Candy cycle finished"
when
    Item CandyState changed from "RUNNING" to "FINISHED"
then
    logInfo("laundry", "The washing machine reports that its cycle has finished")
end
```

## Actions

Each `bianca` Thing provides actions in the `candy` scope.
Obtain its actions with `getActions("candy", "candy:bianca:washer")` in Rules DSL, using your Thing UID.
The same actions are available in Main UI rule actions and other scripting languages through openHAB's Thing Actions API.
No command Item links are required.

| Action | Result | Description |
| --- | --- | --- |
| `getPrograms()` | String | Formatted JSON with a localized summary and cached public program options |
| `refreshPrograms()` | String | Localized queue acknowledgement; the server request remains subject to the five-minute limit |
| `getCatalogStatus()` | String | Localized description of the latest catalog status |
| `startProgram(String programId)` | String | Localized queue acknowledgement for a start with the chosen program's server defaults |
| `stopProgram()` | String | Localized queue acknowledgement for a stop or cancellation of a delayed start |
| `getLastCommandResult()` | String | Localized description of the latest command result |

Every Action returns a String through the `result` output so Main UI can display the outcome.
Messages use the configured openHAB locale.
Start, stop and refresh acknowledge queuing for validation; they do not claim that the server request or washing command succeeded.
An unavailable or disposed handler reports that no request was queued.
An empty program ID is rejected immediately.

`getPrograms()` returns formatted JSON with a localized `message` and a `programs` array.
Each program contains `id` (String), `label` (String), `startable` (Boolean) and `parameters` (map of String values).
Parameters contain server defaults and limits such as `default_temperature` in °C and `default_spin_speed` in rpm; raw server sentinel values are preserved.
The array is empty until a catalog has loaded.
For scripts that need structured options, parse the returned JSON and read its `programs` property; the `program-catalog` channel also retains its original JSON-array representation.
Use the `catalog-status` and `last-command-result` channels when rules need stable status codes rather than localized descriptions.

To request a catalog refresh from a rule's `then` block:

```java
val candy = getActions("candy", "candy:bianca:washer")
if (candy !== null) {
    logInfo("laundry", "{}", candy.refreshPrograms())
}
```

The request completes asynchronously.
Observe `CandyCatalogStatus` changing to `READY`, as in the full example, before using the updated options.
Calling `getPrograms()` immediately after `refreshPrograms()` reads the previous cached list.
A failed refresh retains that list; inspect the description from `getCatalogStatus()` to distinguish stale options from a successful refresh.

To start a program from a separately triggered rule, replace the placeholder with an exact `id` whose `startable` value is `true`:

```java
val candy = getActions("candy", "candy:bianca:washer")
if (candy !== null) {
    logInfo("laundry", "{}", candy.startProgram("program-id-from-getPrograms"))
}
```

To stop the current cycle from another rule:

```java
val candy = getActions("candy", "candy:bianca:washer")
if (candy !== null) {
    logInfo("laundry", "{}", candy.stopProgram())
}
```

Start and stop return localized acknowledgement text after queuing the request.
Observe `CandyCommandResult` updates or call `getLastCommandResult()` later; returning from an action does not confirm device acceptance.

### Program Selection and Control

The catalog loads automatically after the first successful local read when account credentials are available.
It belongs to the appliance whose key matched the local washer; the binding does not select an appliance by its name.
A catalog failure does not take working local monitoring offline.

Server requests are limited to one attempt every five minutes per Thing.
A failed refresh retains the last usable catalog until reconfiguration or restart; `catalog-status` distinguishes that result from a successful refresh.
Program options include temperature and spin defaults/limits, soil limits and available option masks.
A maximum or capability mask does not establish that every intermediate value or option combination is allowed.
Hidden entries are omitted, while incomplete or special-purpose entries remain visible with `startable=false`.

Pass a supported ID to `startProgram(programId)` to start, or call `stopProgram()` to stop a running cycle or cancel a delayed start.
Start uses server defaults with no delay or additional optional features; arbitrary overrides are not accepted.

Controls require `enableControl=true` and physical remote-operation permission on the washer.
Every write follows a fresh status check.
A start is rejected while the machine is running, paused, delayed, in an error state or not reporting a ready state.
Only one command can be pending; additional requests report `BUSY`.
`OFF`, `REFRESH`, Item state updates and restored Item states never start or stop a cycle.
The selected program is forgotten when the handler restarts.
Pause is not supported for Bianca.

`PENDING` includes the fresh status check.
`SENT` indicates HTTP success, not proof that the appliance accepted or started the program.
`CONFIRMED` requires a subsequent matching device state and, for start, matching program number and code.
A write timeout or missing confirmation within 60 seconds of write completion produces `UNKNOWN`.
The binding continues observing status and never automatically replays a write or switches it to the cloud.
Check the washer before issuing another command after an uncertain result.

### Item-Based Controls

For UI widgets or existing rules, link Items to `program-catalog`, `selected-program`, `start` and `stop`.
Send `LIST` or `REFRESH` to `program-catalog` and read its JSON array of options.
Select a supported ID through `selected-program`, then send `ON` to `start`.
Alternatively, send the ID directly to `start-program`.
Send `ON` to `stop` to stop a running cycle or cancel a delayed start.
Set `autoupdate="false"` on command Items.
These channels use the same command validation, rate limits and confirmation tracking as the Actions.

## Troubleshooting

- `CONFIGURATION_ERROR`: check the address, account credentials, optional manual key and polling bounds. Account login may also require verification or consent in simply-Fi.
- `COMMUNICATION_ERROR`: check LAN reachability and the existing key. Invalid or unreadable status can indicate an incorrect key or incompatible firmware; it does not establish which caused the failure.
- `ONLINE` with an unknown optional channel: the response omitted that field or used an unsupported representation. Other monitoring channels remain available.
- Remote permission `OFF`: check the washer's physical remote-operation selector and app instructions. Monitoring can remain available when remote operation is disabled.
- `DISABLED` command result: enable washing controls in the Thing configuration.
- `ACCOUNT_REQUIRED` catalog status: configure account credentials to browse server programs, including when using a manual key.
- Keep appliance keys, tokens, serial numbers and unredacted responses out of public issue reports.

### Availability and Recovery

A failed HTTP read or unusable status sets `OFFLINE / COMMUNICATION_ERROR` and clears live channels to `UNDEF`.
`last-update` retains the last successful read time until reinitialization.
Polling continues with backoff up to five minutes, or the configured interval if longer, and resets after success.
Temporary account setup failures retry no more frequently than every five minutes.
Authentication failures await a configuration change or an explicit monitoring refresh.
Configuration changes cancel the old operation context; obsolete responses cannot restore old state.

### Debug Logging

Enable binding diagnostics in the openHAB console:

```text
log:set DEBUG org.openhab.binding.candy
```

Debug output describes account setup, local reads, catalog requests, command results, retries and cleanup.
Credentials, keys, tokens, cookies and response bodies are excluded.
Routine communication failures use debug logging and Thing status; warnings identify unsupported login flows or unexpected cleanup/executor failures.

Restore the inherited log level afterward:

```text
log:set DEFAULT org.openhab.binding.candy
```

## Developer Documentation

Implementation details and encoding rules are documented in the [Bianca protocol contract](doc/protocol.md).
APK evidence and architecture are documented in the [simply-Fi reverse-engineering report](DEVE_README.md).
