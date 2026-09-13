# Liberty Global Horizon Binding

The Liberty Global Horizon binding integrates set-top boxes running the **Liberty Global Horizon** platform.

| Provider               | Country     | Auth              |
|------------------------|-------------|-------------------|
| BASE TV                | Belgium     | refresh token     |
| Telenet TV             | Belgium     | refresh token     |
| UPC / Sunrise          | Switzerland | refresh token     |
| Virgin Media           | Ireland     | username/password |
| Virgin Media           | UK          | refresh token     |
| Ziggo                  | Netherlands | username/password |
| UPC                    | Poland      | username/password |

Status and control both go over the same cloud MQTT channel the official apps use.
There is no local API on the box itself.

## Supported Things

| Thing   | Type   | Description                                                                              |
|---------|--------|------------------------------------------------------------------------------------------|
| account | Bridge | One LG Horizon account/household. Owns the login session and the shared MQTT connection. |
| box     | Thing  | One physical set-top box. Discovered automatically under an `account` bridge.            |

## Discovery

Once an `account` is configured and active, the binding will discover available set-top boxes.

## Thing Configuration

### `account` (Bridge)

| Parameter       | Required | Description                                                                 |
|-----------------|----------|-----------------------------------------------------------------------------|
| provider        | no       | Provider: `telenet`, `basetv`, `ziggo`, `upc-sunrise`, `virginmedia-gb`, `virginmedia-ie`, `upc-poland`. Leave empty ("Custom") to configure an unlisted provider manually with the three advanced fields below. |
| country         | no       | Advanced. Only used when `provider` is empty: two-letter locale used in the service-discovery URL path (e.g. `be`) - not necessarily the same region as the API URL. Known automatically when `provider` is set. |
| apiUrl          | no       | Advanced. Only used when `provider` is empty: base URL of the provider's "spark" REST API. Known automatically when `provider` is set. |
| useRefreshToken | no       | Advanced. Only used when `provider` is empty: whether the provider needs refresh-token auth instead of username/password. Default `false`. Known automatically when `provider` is set. |
| username        | no       | Only for password-based providers                                           |
| password        | no       | Only for password-based providers                                           |
| refreshToken    | no       | Only for refresh-token-based providers                                      |

### Refresh token based authentication (BASE TV, Telenet, UPC/Sunrise, Virgin Media GB)

These providers don't expose a public login endpoint for third-party apps, so the refresh token has to be extracted once from the browser.
Username and password are not required.

1. Open your provider's web player in a Chromium-based browser (e.g. `https://www.telenet.tv`) and log in.
1. Press `F12` to open Developer Tools → **Application** (Chrome) / **Storage** (Firefox) tab.
1. Expand **Local Storage** → select the provider's domain.
1. Find the key containing `refresh_token` / `refreshToken` and copy its full value (starts with `eyJ`, 200+ characters).
1. Paste it into the `account` thing's **Refresh Token** configuration field.

The binding will keep the refresh token current automatically from then on (the backend rotates it on every use), persisting the new value back into the
bridge configuration.
You should not need to repeat this process unless the token is revoked (e.g. after a very long period offline, or a password change).

### Username/password based authentication (Virgin Media Ireland, Ziggo, UPC Poland)

These providers use your regular account username and password instead.
A refresh token is not needed.

### Advanced configuration without provider preset

`country`, `apiUrl` and `useRefreshToken` are advanced parameters.
When `provider` is set, the binding resolves these three from known provider presets and the advanced parameters are ignored.
When `provider` is left empty, the advanced parameters are required and used as-is.
This allows supporting a provider that isn't in the providers list yet - a new white-label deployment, or a provider's preprod/test backend.

### `box` (Thing)

| Parameter              | Required | Description                                                                      |
|------------------------|----------|----------------------------------------------------------------------------------|
| deviceId               | yes      | LG Horizon device id (filled in by discovery)                                    |
| profileId              | no       | Household profile to use for favorites/language; defaults to the box default one |

## Channels

| Channel                    | Type   | R/W | Description                                                                       |
|----------------------------|--------|-----|-----------------------------------------------------------------------------------|
| power                      | Switch | RW  | ON wakes the box from standby, OFF puts it into standby                           |
| source-type                | String | R   | linear / replay / nDVR / localDVR / reviewBuffer / VOD / app                      |
| channel-name               | String | RW  | Name of the channel currently playing                                             |
| channel-number             | Number | RW  | Logical channel number, send a number to change the channel                       |
| favorite-channel-number    | Number | RW  | Same, limited to favorite channels                                                |
| program-title              | String | R   | The primary title for whatever's playing - a show name, movie name, or app name   |
| series-title               | String | R   | TV series/show name, only set for genuinely episodic content (see table below)    |
| episode-title              | String | R   | Episode title, falling back to "Episode N" if only a number is known              |
| season                     | Number | R   | Season number, when known                                                         |
| episode                    | Number | R   | Episode number, when known                                                        |
| media-image                | Image  | R   | Poster/background image for the current channel, program, VOD title, or recording |
| player                     | Player | RW  | PLAY / PAUSE / REWIND / FASTFORWARD                                               |
| stop                       | Switch | W   | Stop playback and return to live TV                                               |
| record                     | Switch | RW  | Record the current live TV program                                                |
| channel-up                 | Switch | W   | Move to the next channel                                                          |
| channel-down               | Switch | W   | Move to the previous channel                                                      |
| arrow-up                   | Switch | W   | Navigate up in the on-screen menu                                                 |
| arrow-down                 | Switch | W   | Navigate down in the on-screen menu                                               |
| arrow-left                 | Switch | W   | Navigate left in the on-screen menu                                               |
| arrow-right                | Switch | W   | Navigate right in the on-screen menu                                              |
| top-menu                   | Switch | W   | Open the main menu / jump to the top of the screen                                |
| info                       | Switch | W   | Show info about the current program                                               |
| context-menu               | Switch | W   | Open the context menu (subtitles, favourites, watchlist, ...)                     |
| tv                         | Switch | W   | Switch to live TV                                                                 |
| enter                      | Switch | W   | Confirm selection / open the channel zap bar                                      |
| escape                     | Switch | W   | Back / stop and return to live TV                                                 |
| key-code                   | String | W   | Send any remote-control key by its W3C key name (see below)                       |

### Favorite channels

Favorite channels not available with the default profile.
You need to create a specific profile and set the box `profileId` parameter for them to be available.

### Media content

`program-title`/`series-title`/`episode-title`/`season`/`episode`/`media-image` are resolved per source type.
`program-title` is always the primary title for whatever's playing; `series-title` (and `episode-title`/`season`/`episode`)
are only populated when the content is genuinely episodic - a movie or standalone broadcast leaves them undefined, so a
rule can check whether `series-title` is set as a simple "is this a TV series episode" signal.

| Source type             | Title/episode from                         | Image from                            |
|-------------------------|--------------------------------------------|---------------------------------------|
| live / time shift       | EPG lookup by event id                     | the channel's own logo/preview image  |
| replay                  | EPG lookup by event id                     | a separate per-event image lookup     |
| video on demand         | VOD detail-screen lookup                   | a separate per-title image lookup     |
| recordings              | recording detail lookup                    | a separate per-recording image lookup |
| app (e.g. Netflix)      | the app's own name (as program-title only) | the app's own logo                    |

### `key-code` values

Any of the values from the box's `CPE.KeyEvent` protocol, for example:
`Power`, `Standby`, `WakeUp`, `MediaPlay`, `MediaPause`, `MediaPlayPause`,
`MediaStop`, `MediaRecord`, `MediaFastForward`, `MediaRewind`, `ChannelUp`,
`ChannelDown`, `Guide`, `Info`, `ContextMenu`, `ArrowUp`, `ArrowDown`,
`ArrowLeft`, `ArrowRight`, `Enter`, `Escape`, `Backspace`, `Red`, `Green`,
`Yellow`, `Blue`.

## Full Example

### Thing Configuration

```java
Bridge lghorizon:account:home "Telenet Account" [ country="be-nl", refreshToken="eyJ..." ] {
    Thing box livingroom "Living Room Box" [ deviceId="AB12CD34EF56" ]
}
```

### Item Configuration

```java
Switch  TV_Power           "TV Power"          { channel="lghorizon:box:home:livingroom:power" }
String  TV_State           "TV State"          { channel="lghorizon:box:home:livingroom:state" }
String  TV_Channel         "TV Channel"        { channel="lghorizon:box:home:livingroom:channel-name" }
Number  TV_ChannelNumber   "TV Channel Number" { channel="lghorizon:box:home:livingroom:channel-number" }
String  TV_Program         "TV Program"        { channel="lghorizon:box:home:livingroom:program-title" }
Player  TV_Player          "TV Playback"       { channel="lghorizon:box:home:livingroom:player" }
String  TV_SendKey         "TV Send Key"       { channel="lghorizon:box:home:livingroom:key-code" }
```

## Advanced Configuration for Unknown Provider

The binding supports manually configuring a provider that is not in the preset list yet.

```java
Bridge lghorizon:account:preprod "Telenet Account (preprod)" [
    country="be",
    apiUrl="https://spark-preprod-be.gnp.cloud.telenet.tv",
    useRefreshToken=true,
    refreshToken="eyJ..."
]
```

Leaving `provider` empty is what activates the advanced fields; `country` and `apiUrl` are required in that case.
The same pattern works for any backend not in the preset list - just supply the three fields that a preset would normally fill in for you.

## Actions

```java
displayMessage(message, duration)
```

Shows a short text message as an on-screen overlay on the TV.
`duration` is optional and defaults to `10` seconds when omitted or `null`.

```java
val actions = getActions("lghorizon", "lghorizon:box:home:livingroom")
actions.displayMessage("Doorbell")
actions.displayMessage("Doorbell", "Front Door", 15)
```

## Console Commands

A number of commands are supported from the console.
Accounts and boxes are identified by their own `customerId`/`deviceId`, not the openHAB thing UID.

```java
lghorizon profiles [<customerId>]
lghorizon boxes [<customerId>]
lghorizon fingerprint [<customerId>]
lghorizon capture <customerId> <deviceId> <durationSeconds>
```

`accounts`, `profiles` and `boxes` take no arguments to list across all accounts, or a `customerId` to limit the output to one account.
`boxes` lists every device known from the account's REST data, annotated with its box thing's UID for whichever ones actually have one configured (`(no thing)` otherwise) - useful both to find the `deviceId` for `capture` and to spot a device you haven't added a thing for yet.

`fingerprint` and `capture` are separate commands with different purposes:

- `fingerprint` is a quick, mostly-instant REST-only snapshot (customer/entitlements/channels/service config), plus a passive wait for each box's next (or already-cached) `.../status` message - not an active request for a fresh one.
  Useful for a first look at an unsupported provider/device, or to check what the REST side of things looks like.
- `capture <customerId> <deviceId> <durationSeconds>` actively records live traffic for **one specific box** over a fixed window: every MQTT status/uiStatus message, every REST call the binding makes.
  Requires that device to already have a box thing configured - interact with the box (change channels, rewind, launch an app, start a recording,
  ...) for the duration of the capture to get a meaningful sequence of events.

Both commands write their output as a zip file in the user's home `lghorizon` directory.
All personal information is masked.

## Known Limitations / Roadmap

The binding focuses on live status + control.
Not implemented:

- EPG / program guide browsing
- Replay/catch-up TV channel list
- Ad-break detection/skipping
- localDVR (locally-recorded content) title/image resolution

## Credits

The Liberty Global Horizon cloud protocol used by this binding (REST auth flow, service discovery, MQTT-over-WebSockets status/control channel) was reverse engineered and documented by:

- [Sholofly/lghorizon-python](https://github.com/Sholofly/lghorizon-python) and the [lghorizon](https://github.com/Sholofly/lghorizon) Home Assistant integration
- [mase1981/uc-intg-horizon](https://github.com/mase1981/uc-intg-horizon) (Unfolded Circle Remote integration)

This binding is an independent Java reimplementation of the protocol those projects document.
It shares no code with them.
It is not affiliated with or supported by Telenet, Ziggo, Virgin Media, UPC, Sunrise, BASE, or Liberty Global.
